package com.amadeus.aud.libraries.checker;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.checker.models.BomFromAudRepository;
import com.amadeus.aud.libraries.checker.models.FileModels;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Models;
import com.amadeus.aud.libraries.checker.types.BomCheck;
import com.amadeus.aud.libraries.checker.types.CheckType;
import com.amadeus.aud.libraries.checker.types.DomainCheck;
import com.amadeus.aud.libraries.checker.types.FileCheck;
import com.amadeus.aud.libraries.checker.types.GlobalCheck;
import com.amadeus.aud.libraries.checker.types.ModelsCheck;
import com.amadeus.aud.libraries.checker.types.OtherCheck;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.spi.CDI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Runner takes a list of checks, and runs them according to their types.
 *
 * <p>When {@code failFast} is enabled (the default), each layer is run only if no error is returned
 * by the previous layer. When {@code failFast} is disabled, subsequent layers continue to run even
 * if earlier layers encounter errors.
 *
 * <p>- FileChecks runs first, in parallel, on the provided file path (when checking local files).
 *
 * <p>- DomainChecks runs next, in parallel, on the provided domain data (when checking local
 * files).
 *
 * <p>- ModelsChecks runs next, in parallel, on the provided parsed models.
 *
 * <p>- BomChecks runs next, in parallel, on the provided BOM.
 *
 * <p>- OtherChecks runs in parallel, on the full data, including links/edges partitioned per
 * domain.
 *
 * <p>- GlobalChecks runs last, in parallel, on the full data, including full links/edges
 */
public final class Runner {
  private final Collection<CheckType> checks;
  private final Supplier<Instant> now;
  private final CheckRunner checkRunner;
  private final Supplier<CDI<Object>> cdiSupplier;
  private final Map<String, Long> timings;
  private final Class<? extends Models> modelsImplClass;
  private final boolean failFast;

  private Runner(
      Collection<CheckType> checks,
      Supplier<Instant> now,
      CheckRunner checkRunner,
      Supplier<CDI<Object>> cdiSupplier,
      Class<? extends Models> modelsImplClass,
      boolean failFast) {
    this.checks = checks;
    this.now = now;
    this.checkRunner = checkRunner;
    this.cdiSupplier = cdiSupplier;
    this.timings = new ConcurrentHashMap<>();
    this.modelsImplClass = modelsImplClass;
    this.failFast = failFast;
  }

  public boolean run(
      CharSequence version,
      SequencedCollection<String> domains,
      Optional<Path> path,
      BlockingQueue<Collection<Message>> queue)
      throws InterruptedException {
    this.timings.clear();
    Instant startTime = this.now.get();
    String firstMessage =
        new StringBuilder()
            .append("Checks against version ")
            .append(version)
            .append(" for domains ")
            .append(String.join(" and ", domains))
            .toString();
    queue.put(List.of(Message.context(firstMessage)));

    boolean hasErrors = false;
    Map<Class<? extends ModelElement>, List<ModelElement>> domainsData = new HashMap<>();

    if (path.isPresent()) {
      Path localPath = path.get();
      if (checkRunner.runChecks("FileChecks", fileChecks(domains, localPath), queue)) {
        hasErrors = true;
        if (this.failFast) {
          end(startTime, queue);
          return true;
        }
      }

      FileModels.populatedDomainsData(localPath, domainsData);

      if (domains.size() == 1) {
        // If only one domain, run DomainChecks on it
        // before loading the rest of the domains from the storage
        if (checkRunner.runChecks(
            "DomainChecks", domainChecks(domains.getFirst(), domainsData), queue)) {
          hasErrors = true;
          if (this.failFast) {
            end(startTime, queue);
            return true;
          }
        }
      }
    }

    if (this.checks.stream().noneMatch(ModelsCheck.class::isInstance)
        && this.checks.stream().noneMatch(BomCheck.class::isInstance)
        && this.checks.stream().noneMatch(OtherCheck.class::isInstance)
        && this.checks.stream().noneMatch(GlobalCheck.class::isInstance)) {
      end(startTime, queue);
      return hasErrors;
    }

    final Models models;
    try {
      // Only local files override stored data. Without them, the selected domains are checked
      // against their stored data. With them, every selected domain is replaced, even by nothing:
      // a domain without local elements was emptied on purpose.
      Collection<String> overriddenDomains = path.isPresent() ? domains : List.of();
      models = newModelsInstance(overriddenDomains, domainsData);
    } catch (RuntimeException e) {
      String message =
          new StringBuilder()
              .append("Error while trying to create models.\n")
              .append("This is likely due to a bad AUD definition:\n")
              .append(e.getMessage())
              .toString();
      Log.error(message, e);
      queue.add(List.of(Message.error("INT00001", message, new ArrayList<>())));
      end(startTime, queue);
      return true;
    }

    if (domains.isEmpty()) {
      domains = models.allDomainNames();
      Log.infof("No domains provided, using all %s domains", domains.size());
    }

    if (checkRunner.runChecks("ModelsChecks", modelsChecks(domains, models), queue)) {
      hasErrors = true;
      if (this.failFast) {
        end(startTime, queue);
        return true;
      }
    }

    // Exception to ADR-002: not performance critical
    if (this.checks.stream().noneMatch(BomCheck.class::isInstance)
        && this.checks.stream().noneMatch(OtherCheck.class::isInstance)
        && this.checks.stream().noneMatch(GlobalCheck.class::isInstance)) {
      end(startTime, queue);
      return hasErrors;
    }

    final BomFromAudRepository bom;
    try {
      bom = newBomInstance(models.repository());
    } catch (RuntimeException e) {
      String message =
          new StringBuilder()
              .append("Error while trying to create a BOM.\n")
              .append("This is likely due to a bad AUD definition:\n")
              .append(e.getMessage())
              .toString();
      Log.error(message, e);
      queue.add(List.of(Message.error("INT00001", message, new ArrayList<>())));
      end(startTime, queue);
      return true;
    }

    if (checkRunner.runChecks("BomChecks", bomChecks(domains, bom), queue)) {
      hasErrors = true;
      if (this.failFast) {
        end(startTime, queue);
        return true;
      }
    }

    // Exception to ADR-002: not performance critical
    if (this.checks.stream().noneMatch(OtherCheck.class::isInstance)
        && this.checks.stream().noneMatch(GlobalCheck.class::isInstance)) {
      end(startTime, queue);
      return hasErrors;
    }

    Map<String, List<Link>> graphPerDomain = null;
    Graph graph = null;
    try {
      graph = newGraph(bom.getVersion());
      graphPerDomain = GraphService.linksPartitionedByScopes(graph, domains);
    } catch (RuntimeException e) {
      String message =
          new StringBuilder()
              .append("Error while trying to create a graph.\n")
              .append("This is likely due to a bad AUD definition:\n")
              .append(e.getMessage())
              .toString();
      Log.error(message, e);
      queue.add(List.of(Message.error("INT00001", message, new ArrayList<>())));
      end(startTime, queue);
      return true;
    }

    if (checkRunner.runChecks("OtherChecks", otherChecks(domains, bom, graphPerDomain), queue)) {
      hasErrors = true;
      if (this.failFast) {
        end(startTime, queue);
        return true;
      }
    }

    if (this.checks.stream().noneMatch(GlobalCheck.class::isInstance)) {
      end(startTime, queue);
      return hasErrors;
    }
    if (checkRunner.runChecks(
        "GlobalChecks", globalChecks(domains, models, bom, allLinks(graph)), queue)) {
      hasErrors = true;
    }

    end(startTime, queue);
    return hasErrors;
  }

  private List<WrappedCaller> fileChecks(Collection<String> domains, Path path) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof FileCheck fileCheck) {
        for (String domain : domains) {
          Callable<Collection<Message>> collectionCallable = () -> fileCheck.check(domain, path);
          list.add(new WrappedCaller(check, collectionCallable));
        }
      }
    }
    return list;
  }

  private List<WrappedCaller> modelsChecks(Collection<String> domains, Models models) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof ModelsCheck modelsCheck) {
        for (String domain : domains) {
          Callable<Collection<Message>> collectionCallable =
              () -> modelsCheck.check(domain, models);
          list.add(new WrappedCaller(check, collectionCallable));
        }
      }
    }
    return list;
  }

  private List<WrappedCaller> bomChecks(Collection<String> domains, Bom bom) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof BomCheck bomCheck) {
        for (String domain : domains) {
          Callable<Collection<Message>> collectionCallable = () -> bomCheck.check(domain, bom);
          list.add(new WrappedCaller(check, collectionCallable));
        }
      }
    }
    return list;
  }

  private List<WrappedCaller> otherChecks(
      Collection<String> domains, Bom bom, Map<String, List<Link>> graphPerDomain) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof OtherCheck otherCheck) {
        for (String domain : domains) {
          Callable<Collection<Message>> collectionCallable =
              () -> otherCheck.check(domain, bom, graphPerDomain.getOrDefault(domain, List.of()));
          list.add(new WrappedCaller(check, collectionCallable));
        }
      }
    }
    return list;
  }

  private static List<Link> allLinks(Graph graph) {
    List<Link> links = new ArrayList<>();
    for (Edge edge : graph.getEdges().values()) {
      if (edge.getLink() != null) {
        links.add(edge.getLink());
      }
    }
    return links;
  }

  private List<WrappedCaller> globalChecks(
      Collection<String> domains, Models models, Bom bom, List<Link> links) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof GlobalCheck globalCheck) {
        Callable<Collection<Message>> collectionCallable =
            () -> globalCheck.check(domains, models, bom, links);
        list.add(new WrappedCaller(check, collectionCallable));
      }
    }
    return list;
  }

  private List<WrappedCaller> domainChecks(
      String domain, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData) {
    List<WrappedCaller> list = new ArrayList<>();
    for (CheckType check : this.checks) {
      if (check instanceof DomainCheck domainCheck) {
        Callable<Collection<Message>> collectionCallable =
            () -> domainCheck.check(domain, domainsData);
        list.add(new WrappedCaller(check, collectionCallable));
      }
    }
    return list;
  }

  private void end(Instant startTime, BlockingQueue<Collection<Message>> queue)
      throws InterruptedException {
    Instant endTime = this.now.get();
    this.timings.putAll(this.checkRunner.timings());
    this.timings.put("000-Total", Duration.between(startTime, endTime).toMillis());

    queue.put(List.of(Message.context(this.timingToMarkdownTable())));
  }

  /** convert timings to a sorted Markdown table from slowest to fastest */
  private String timingToMarkdownTable() {
    int keyLength = this.timings.keySet().stream().mapToInt(String::length).max().orElse(10);

    int valueLength =
        this.timings.values().stream().mapToInt(x -> x.toString().length()).max().orElse(10);

    String format = "| %-" + keyLength + "s | %-" + valueLength + "s |\n";

    StringBuilder buffer = new StringBuilder();
    buffer.append(String.format(format, "Check", "ms"));
    buffer.append(String.format(format, "-", "-"));

    List<Map.Entry<String, Long>> toSort = new ArrayList<>(this.timings.entrySet());
    toSort.sort(Map.Entry.<String, Long>comparingByValue().reversed());
    for (Map.Entry<String, Long> entry : toSort) {
      buffer.append(String.format(format, entry.getKey(), entry.getValue()));
    }
    return buffer.toString();
  }

  private Models newModelsInstance(
      Collection<String> overriddenDomains,
      Map<Class<? extends ModelElement>, List<ModelElement>> domainsData) {
    // Request Models instance from Quarkus CDI on the fly
    // We cannot inject it directly because we need to pass the domains and path
    // and also because if there are any syntax issues earlier, the Models cannot be created
    Instant start = Instant.now();
    final Models models = this.cdiSupplier.get().select(modelsImplClass).get();
    models.loadLatestWithDomains(new HashSet<>(overriddenDomains), domainsData);
    Instant end = Instant.now();
    Log.infof("newModelsInstance took %s", Duration.between(start, end));
    return models;
  }

  private BomFromAudRepository newBomInstance(final AudDataRepository repository) {
    // Request Bom instance from Quarkus CDI on the fly
    Instant start = Instant.now();
    final BomFromAudRepository bom =
        this.cdiSupplier.get().select(BomFromAudRepository.class).get();
    bom.initWithAudDataRepository(repository);
    Instant end = Instant.now();
    Log.infof("newBomInstance took %s", Duration.between(start, end));
    return bom;
  }

  private Graph newGraph(Version version) {
    // Request GraphService instance from Quarkus CDI on the fly
    Log.info("creating full graph...");
    Instant start = Instant.now();
    GraphBuilder builder = new GraphBuilder(version.getVersionNumber(), true);
    GraphService graphService = this.cdiSupplier.get().select(GraphService.class).get();
    graphService.populateGraphFromDependencies(builder, version);
    Graph graph = builder.build();
    Instant end = Instant.now();
    Log.infof("newGraph took %s", Duration.between(start, end));
    return graph;
  }

  public static RunnerBuilder builder() {
    return new RunnerBuilder();
  }

  public static class RunnerBuilder {
    private final Collection<CheckType> checks = new ArrayList<>();
    private Supplier<Instant> now = Instant::now;
    private CheckRunner checksRunner;
    private Supplier<CDI<Object>> cdiSupplier = CDI::current;
    private Class<? extends Models> modelsImplClass;
    private boolean failFast = true;

    public RunnerBuilder withChecks(Collection<CheckType> checks) {
      this.checks.addAll(checks);
      return this;
    }

    public RunnerBuilder withInstant(Supplier<Instant> now) {
      this.now = now;
      return this;
    }

    public RunnerBuilder withCheckRunner(CheckRunner checksRunner) {
      this.checksRunner = checksRunner;
      return this;
    }

    public RunnerBuilder withCdiSupplier(Supplier<CDI<Object>> cdiSupplier) {
      this.cdiSupplier = cdiSupplier;
      return this;
    }

    public RunnerBuilder withModelsImplClass(Class<? extends Models> modelsImplClass) {
      this.modelsImplClass = modelsImplClass;
      return this;
    }

    public RunnerBuilder withFailFast(boolean failFast) {
      this.failFast = failFast;
      return this;
    }

    public Runner build() {
      return new Runner(
          checks,
          now,
          Objects.requireNonNull(checksRunner, "checksRunner"),
          cdiSupplier,
          Objects.requireNonNull(modelsImplClass, "modelsImplClass"),
          failFast);
    }
  }
}
