import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/// Builds and runs the AUD checker.
///
/// Usage: `java scripts/runChecker.java <path> <domains>`
///
/// Must be run from the `aud/` directory.

private static final Path CHECKER_TARGET = Path.of("clis", "cli-simple-checker", "target");

void main(String[] args) {
  if (args.length != 2) {
    Printer.printRed("Usage: java scripts/runChecker.java <path> <domains>");
    System.exit(2);
  }

  ScriptSupport.runMain(() -> runChecker(args[0], args[1]));
}

private void runChecker(String path, String domains)
    throws IOException, InterruptedException, ScriptSupport.ProcessFailedException {
  Printer.printBlue("## Reading environment file");
  Map<String, String> environment = ScriptSupport.loadApiEnvironment();

  Printer.printBlue("## Building checker");
  ScriptSupport.run(
      List.of(
          "mvnd",
          "--projects",
          "clis/cli-simple-checker",
          "--define",
          "jacoco.skip=true",
          "--define",
          "skipTests=true",
          "--define",
          "quarkus.package.jar.type=uber-jar",
          "package"),
      environment);

  Path runnerJar = ScriptSupport.findQuarkusRunner(CHECKER_TARGET);
  Printer.printBlue("## Running checker");
  ScriptSupport.run(
      List.of(
          ScriptSupport.javaExecutable("java").toString(),
          "-XX:+UseCompactObjectHeaders",
          "-XX:InitialRAMPercentage=75",
          "-XX:MaxRAMPercentage=85",
          "--enable-native-access=ALL-UNNAMED",
          "--add-opens",
          "java.base/java.util.concurrent=ALL-UNNAMED",
          "-jar",
          runnerJar.toString(),
          "--path",
          path,
          "--domains",
          domains,
          "--report-path",
          "/tmp/aud_checker_report.md",
          "--json-report-path",
          "/tmp/report.json"),
      environment);
}
