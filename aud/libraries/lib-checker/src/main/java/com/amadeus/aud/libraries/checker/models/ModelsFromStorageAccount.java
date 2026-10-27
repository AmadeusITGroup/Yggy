package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.AudDataLoader;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link Models} loaded from the latest AUD data, with the checked domains replaced by local data.
 *
 * <p>Deployment-specific data is added by {@link ModelsExtension} beans, see {@link
 * #loadLatestWithDomains}.
 *
 * <p>\@Unremovable, because used programmatically, and quarkus is eager to remove if from the jar
 * otherwise.
 */
@Singleton
@Unremovable
public final class ModelsFromStorageAccount implements Models {
  private final AudDataLoader audDataLoader;
  private final List<ModelsExtension> extensions;
  private final Class<? extends ModelElement> domainType;
  private AudDataRepository repository;
  private Map<String, Map<Class<? extends ModelElement>, List<ModelElement>>> integratedModels =
      new HashMap<>();
  // guarded by this; see scopeIndex()
  private @Nullable Map<String, List<ModelElement>> scopeIndex;
  private @Nullable ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>>
      scopeIndexData;

  @Inject
  public ModelsFromStorageAccount(
      AudDataLoader audDataLoader, Instance<ModelsExtension> extensions) {
    this(audDataLoader, toList(extensions));
  }

  ModelsFromStorageAccount(AudDataLoader audDataLoader, List<ModelsExtension> extensions) {
    this.audDataLoader = audDataLoader;
    this.extensions = List.copyOf(extensions);
    this.domainType = domainType(this.extensions);
  }

  private static List<ModelsExtension> toList(Instance<ModelsExtension> extensions) {
    List<ModelsExtension> list = new ArrayList<>();
    for (ModelsExtension extension : extensions) {
      list.add(extension);
    }
    return list;
  }

  private static Class<? extends ModelElement> domainType(List<ModelsExtension> extensions) {
    @Nullable Class<? extends ModelElement> found = null;
    for (ModelsExtension extension : extensions) {
      @Nullable Class<? extends ModelElement> type = extension.domainType();
      if (type == null) {
        continue;
      }
      if (found != null) {
        throw new IllegalStateException(
            "Several ModelsExtension define the domain type: %s and %s"
                .formatted(found.getName(), type.getName()));
      }
      found = type;
    }
    return found != null ? found : V1Scope.class;
  }

  /**
   * Loads the latest AUD data, replaces the elements of {@code domains} with {@code domainsData},
   * then adds the elements of every {@link ModelsExtension}.
   */
  @Override
  public void loadLatestWithDomains(
      Set<String> domains, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData) {
    // Request loading from yaml instead of json to prevent information loss for the checker
    boolean jsonPreferred = false;
    this.repository =
        this.audDataLoader.initObjects("latest", jsonPreferred).await().indefinitely();
    // Stored elements of every domain, before overrides: what latestIntegrated(domain) returns.
    this.integratedModels = groupByScope(this.repository.getData());
    if (!domains.isEmpty()) {
      ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> filteredData =
          new ConcurrentHashMap<>();
      for (Map.Entry<Class<? extends ModelElement>, List<? extends ModelElement>> entry :
          this.repository.getData().entrySet()) {
        List<ModelElement> values = new ArrayList<>();
        for (ModelElement value : entry.getValue()) {
          if (!domains.contains(value.getMetadata().getScope())) {
            values.add(value);
          }
        }
        List<ModelElement> domainDataForType = domainsData.get(entry.getKey());
        if (domainDataForType != null) {
          Log.infof(
              "Overriding all %s %s for this domain",
              domainDataForType.size(), entry.getKey().getSimpleName());
          values.addAll(domainDataForType);
        }
        filteredData.put(entry.getKey(), values);
      }
      this.repository.setData(filteredData);
    }
    loadExtensions();
    synchronized (this) {
      this.scopeIndex = null;
      this.scopeIndexData = null;
    }
  }

  private static Map<String, Map<Class<? extends ModelElement>, List<ModelElement>>> groupByScope(
      Map<Class<? extends ModelElement>, List<? extends ModelElement>> data) {
    Map<String, Map<Class<? extends ModelElement>, List<ModelElement>>> byScope = new HashMap<>();
    for (Map.Entry<Class<? extends ModelElement>, List<? extends ModelElement>> entry :
        data.entrySet()) {
      for (ModelElement value : entry.getValue()) {
        String scope = value.getMetadata().getScope();
        if (scope != null) {
          byScope
              .computeIfAbsent(scope, (String k) -> new HashMap<>())
              .computeIfAbsent(
                  entry.getKey(), (Class<? extends ModelElement> k) -> new ArrayList<>())
              .add(value);
        }
      }
    }
    return byScope;
  }

  private void loadExtensions() {
    Map<Class<? extends ModelElement>, String> addedBy = new HashMap<>();
    for (ModelsExtension extension : this.extensions) {
      Map<Class<? extends ModelElement>, List<? extends ModelElement>> added = extension.load();
      for (Class<? extends ModelElement> type : added.keySet()) {
        String previous = addedBy.put(type, extension.getClass().getName());
        if (previous != null) {
          throw new IllegalStateException(
              "Several ModelsExtension provide %s: %s and %s"
                  .formatted(type.getName(), previous, extension.getClass().getName()));
        }
      }
      this.repository.getData().putAll(added);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<Class<? extends ModelElement>, List<? extends ModelElement>> latestIntegrated(
      String domain) {
    return (Map<Class<? extends ModelElement>, List<? extends ModelElement>>)
        (Map<?, ?>) integratedModels.getOrDefault(domain, Map.of());
  }

  @Override
  public <T extends ModelElement> Collection<T> list(Class<T> type, @Nullable CharSequence domain) {
    @SuppressWarnings("unchecked")
    final Collection<T> collection = (Collection<T>) this.repository.getData().get(type);
    if (collection == null) {
      return List.of();
    }
    if (domain == null) {
      return collection;
    }
    // Compare by content: domain may be any CharSequence, e.g. a StringBuilder
    String domainName = domain.toString();
    List<T> output = new ArrayList<>();
    for (T item : collection) {
      if (domainName.equals(item.getMetadata().getScope())) {
        output.add(item);
      }
    }
    return output;
  }

  @Override
  public <T extends ModelElement> List<T> single(Class<T> type, CharSequence name) {
    @SuppressWarnings("unchecked")
    final Collection<T> collection = (Collection<T>) this.repository.getData().get(type);
    List<T> list = new ArrayList<>();
    if (collection == null) {
      return list;
    }
    for (T item : collection) {
      if (item.getMetadata().getName().contentEquals(name)) {
        list.add(item);
      }
    }
    return list;
  }

  @Override
  public ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> allAsMap(
      @Nullable CharSequence domain) {
    if (domain == null) {
      return this.repository.getData();
    }
    // Compare by content: domain may be any CharSequence, e.g. a StringBuilder
    String domainName = domain.toString();
    ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> filteredData =
        new ConcurrentHashMap<>();
    for (Map.Entry<Class<? extends ModelElement>, List<? extends ModelElement>> entry :
        this.repository.getData().entrySet()) {
      List<ModelElement> values = new ArrayList<>();
      for (ModelElement value : entry.getValue()) {
        if (domainName.equals(value.getMetadata().getScope())) {
          values.add(value);
        }
      }
      filteredData.put(entry.getKey(), values);
    }
    return filteredData;
  }

  @Override
  public Collection<ModelElement> all(@Nullable CharSequence domain) {
    if (domain == null) {
      List<ModelElement> list = new ArrayList<>();
      for (List<? extends ModelElement> modelElements : this.repository.getData().values()) {
        list.addAll(modelElements);
      }
      return list;
    }
    // Checks call all(domain) once per domain: index by scope instead of rescanning everything.
    return new ArrayList<>(scopeIndex().getOrDefault(domain.toString(), List.of()));
  }

  /**
   * Returns all elements grouped by scope, built on first use.
   *
   * <p>Rebuilt when the repository or its data map is replaced. In-place changes to the data map
   * are not detected: the models are not modified once loading is done, and loading resets the
   * index.
   */
  private synchronized Map<String, List<ModelElement>> scopeIndex() {
    ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data =
        this.repository.getData();
    if (this.scopeIndex == null || this.scopeIndexData != data) {
      Map<String, List<ModelElement>> index = new HashMap<>();
      for (List<? extends ModelElement> modelElements : data.values()) {
        for (ModelElement item : modelElements) {
          String scope = item.getMetadata().getScope();
          if (scope != null) {
            index.computeIfAbsent(scope, (String s) -> new ArrayList<>()).add(item);
          }
        }
      }
      this.scopeIndex = index;
      this.scopeIndexData = data;
    }
    return this.scopeIndex;
  }

  @Override
  public AudDataRepository repository() {
    return repository;
  }

  /** Names of the elements of the domain type: {@code V1Scope}, unless an extension changes it. */
  @Override
  public List<String> allDomainNames() {
    List<String> list = new ArrayList<>();
    for (ModelElement element : this.list(this.domainType, null)) {
      list.add(element.getMetadata().getName());
    }
    return list;
  }
}
