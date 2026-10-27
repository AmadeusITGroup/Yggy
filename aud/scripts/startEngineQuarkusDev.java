import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// Starts the connectivity engine in Quarkus development mode.
///
/// Usage: `java scripts/startEngineQuarkusDev.java [profile] [force-version]`
///
/// Must be run from the `aud/` directory.

void main(String[] args) {
  if (args.length > 2) {
    Printer.printRed("Usage: java scripts/startEngineQuarkusDev.java [profile] [force-version]");
    System.exit(2);
  }

  String profile = args.length == 0 || args[0].isEmpty() ? "inmemory" : args[0];
  String forceVersion = args.length < 2 ? "" : args[1];
  ScriptSupport.runMain(() -> startEngineQuarkusDev(profile, forceVersion));
}

private void startEngineQuarkusDev(String profile, String forceVersion)
    throws IOException, InterruptedException, ScriptSupport.ProcessFailedException {
  Printer.printBlue("## Reading environment file");
  Map<String, String> environment = ScriptSupport.loadApiEnvironment();

  List<String> command =
      new ArrayList<>(
          List.of(
              "mvn",
              "--also-make",
              "--projects",
              "services/service-http-connectivity-engine",
              "quarkus:dev",
              "--define",
              "quarkus.profile=" + profile,
              "--define",
              "graph.database.type=" + profile));
  if (!forceVersion.isEmpty()) {
    Printer.printBlue("## Forcing graph version: " + forceVersion);
    command.add("--define");
    command.add("graph.init.force-version=" + forceVersion);
  }
  if ("inmemory".equals(profile)) {
    Printer.printBlue("## In-memory mode: excluding Postgres repositories from Arc discovery");
    command.add("--define");
    command.add(
        "quarkus.arc.exclude-types=com.amadeus.aud.libraries.engine.graph.services.storage.postgres.*");
  }

  Printer.printBlue("## Starting service-http-connectivity-engine with profile '" + profile + "'");
  ScriptSupport.run(command, environment);
}
