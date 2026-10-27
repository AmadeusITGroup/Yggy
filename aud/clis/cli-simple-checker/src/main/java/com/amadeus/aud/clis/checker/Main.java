package com.amadeus.aud.clis.checker;

import com.amadeus.aud.libraries.checker.Runner;
import com.amadeus.aud.libraries.checker.SingleCheckTypeRunner;
import com.amadeus.aud.libraries.checker.WorkerExecutor;
import com.amadeus.aud.libraries.checker.config.Configuration;
import com.amadeus.aud.libraries.checker.config.Defaults;
import com.amadeus.aud.libraries.checker.config.TomlParser;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.ModelsFromStorageAccount;
import com.amadeus.aud.libraries.checker.reports.JsonReporter;
import com.amadeus.aud.libraries.checker.reports.MarkdownReporter;
import com.amadeus.aud.libraries.checker.reports.Reporter;
import com.amadeus.aud.libraries.checker.types.CheckType;
import io.quarkus.arc.All;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import picocli.CommandLine;

@SuppressWarnings(value = {"java:S1147", "java:S106", "java:S1309"})
@CommandLine.Command
public class Main implements Runnable {
  private static final List<Message> END_OF_QUEUE = List.of();

  @CommandLine.Option(
      names = {"-h", "--help"},
      usageHelp = true,
      description = "display this help message")
  private boolean helpRequested = false;

  @CommandLine.Option(
      names = {"-p", "--path"},
      description = "Path to check for yaml files",
      required = false)
  @Nullable
  private Path path;

  @CommandLine.Option(
      names = {"-d", "--domains"},
      description = "Comma-separated list of domains.",
      required = true)
  private String domains;

  @CommandLine.Option(
      names = {"-c", "--configuration"},
      description = "Path to the TOML configuration file",
      required = false)
  @Nullable
  private Path configurationPath;

  @CommandLine.Option(
      names = {"-r", "--report-path"},
      description = "Path where to generate the report",
      required = false)
  private Path reportPath = Path.of("/tmp/aud_checker_report.md");

  @CommandLine.Option(
      names = {"--json-report-path"},
      description = "Path where to generate the JSON report",
      required = false)
  private Path jsonReportPath = Path.of("/tmp/aud_checker_report.json");

  @CommandLine.Option(
      names = {"--check-name"},
      description = "If provided, only run the check with the given name",
      required = false)
  private String checkName;

  private Collection<CheckType> checks;
  private final WorkerExecutor executor;

  @Inject
  public Main(@All List<CheckType> checks, WorkerExecutor executor) {
    this.checks = new ArrayList<>(checks);
    this.executor = executor;
  }

  @Override
  public void run() {
    if (this.helpRequested) {
      CommandLine.usage(new Main(null, null), System.out);
      return;
    }
    final Configuration configuration = tryInitConfiguration();

    if (this.path != null && !this.path.toFile().exists()) {
      Log.errorf("Path %s does not exists", this.path.toAbsolutePath());
      System.exit(ExitCodes.NOT_FOUND);
    }

    if (this.path != null) {
      Log.infof("Checking %s", this.path.toAbsolutePath());
    } else {
      Log.info("Running without local files");
    }

    if (this.checkName != null) {
      Log.infof("Keeping only check with name: %s", this.checkName);
      this.checks.removeIf(check -> !check.getClass().getSimpleName().equals(this.checkName));
    }
    Log.info("Number of checks: " + this.checks.size());
    Runner runner =
        new Runner.RunnerBuilder()
            .withCheckRunner(new SingleCheckTypeRunner(configuration, executor))
            .withChecks(this.checks)
            .withModelsImplClass(ModelsFromStorageAccount.class)
            .build();
    SequencedCollection<String> domainList =
        Arrays.stream(this.domains.split(","))
            .filter((String domain) -> !domain.isEmpty())
            .toList();

    preJsonReport();
    preMarkdownReport();

    BlockingQueue<Collection<Message>> queue =
        new ArrayBlockingQueue<>(checks.size() * domainList.size() * 2);
    Thread consumerThread = new Thread(() -> consumer(queue));
    consumerThread.start();

    boolean hasErrors = false;
    try {
      hasErrors = runner.run("latest", domainList, Optional.ofNullable(this.path), queue);
    } catch (InterruptedException e) {
      Log.errorf("Run interrupted", e);
      Thread.currentThread().interrupt();
    } finally {
      try {
        // Send END_OF_QUEUE to stop the consumer, and wait for it to finish
        queue.put(END_OF_QUEUE);
        consumerThread.join();
      } catch (InterruptedException e) {
        Log.errorf("Run interrupted during final join", e);
        Thread.currentThread().interrupt();
      }
    }
    Log.infof("Local report at %s", this.reportPath);

    if (hasErrors) {
      Log.error("Errors found in the checked files");
      System.exit(ExitCodes.GENERIC_ERROR);
    }
  }

  private Configuration tryInitConfiguration() {
    Configuration configuration =
        new Configuration(
            new Defaults(LocalDate.parse("2099-01-01"), java.time.Duration.ofDays(180)), Map.of());
    if (this.configurationPath != null) {
      if (!this.configurationPath.toFile().exists()) {
        Log.errorf(
            "Configuration file %s does not exists", this.configurationPath.toAbsolutePath());
        System.exit(ExitCodes.NOT_FOUND);
      }
      if (!this.configurationPath.getFileName().toString().endsWith(".toml")) {
        Log.errorf(
            "Configuration file %s is not a *.toml file", this.configurationPath.toAbsolutePath());
        System.exit(ExitCodes.GENERIC_ERROR);
      }
      byte[] configurationContent = null;
      try {
        configurationContent = Files.readAllBytes(this.configurationPath);
      } catch (IOException e) {
        Log.errorf(
            "Configuration file %s cannot be read: %s",
            this.configurationPath.toAbsolutePath(), e.getMessage());
        System.exit(ExitCodes.GENERIC_ERROR);
      }
      try {
        configuration = TomlParser.parse(configurationContent);
      } catch (IOException e) {
        Log.errorf(
            "Configuration file %s cannot be parsed: %s",
            this.configurationPath.toAbsolutePath(), e.getMessage());
        System.exit(ExitCodes.GENERIC_ERROR);
      }
    }
    return configuration;
  }

  private void preJsonReport() {
    Log.infof("Generating JSON report at %s", this.jsonReportPath);
    try {
      if (this.jsonReportPath.getParent() != null) {
        Files.createDirectories(this.jsonReportPath.getParent());
      }
    } catch (IOException e) {
      Log.errorf(
          "Report file %s cannot be written to: %s", this.jsonReportPath.toAbsolutePath(), e);
      System.exit(ExitCodes.GENERIC_ERROR);
    }
  }

  private void preMarkdownReport() {
    Log.infof("Generating report at %s", this.reportPath);
    try {
      if (this.reportPath.getParent() != null) {
        Files.createDirectories(this.reportPath.getParent());
      }
    } catch (IOException e) {
      Log.errorf("Report file %s cannot be written to: %s", this.reportPath.toAbsolutePath(), e);
      System.exit(ExitCodes.GENERIC_ERROR);
    }
  }

  @SuppressWarnings("java:S1698")
  private void consumer(BlockingQueue<Collection<Message>> queue) {
    try (FileOutputStream jsonOutputStream = new FileOutputStream(this.jsonReportPath.toFile());
        FileOutputStream markDownOutputStream = new FileOutputStream(this.reportPath.toFile());
        Reporter jsonReport = new JsonReporter(jsonOutputStream);
        Reporter markdownReporter = new MarkdownReporter(LocalDate.now(), markDownOutputStream)) {

      while (true) {
        Collection<Message> messages = queue.take();
        // this is a reference comparison, not a value comparison.
        // DO NOT CHANGE TO messages.equals(END_OF_QUEUE)
        if (messages == END_OF_QUEUE) {
          break;
        }
        jsonReport.addMessages(messages);
        markdownReporter.addMessages(messages);
      }

    } catch (InterruptedException e) {
      Log.errorf("Report Interrupted", e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      Log.errorf("Report file cannot be written to", e);
      throw new RuntimeException(e);
    }
  }
}
