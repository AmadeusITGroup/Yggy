/// Update Docker base images and Java (Maven) dependencies.
///
/// Usage: `java updateJavaDependencies.java`
///
/// Must be run from the `aud/` directory.

void main() {
  try {
    Printer.printBlue("📦 Updating Java dependencies...");
    run("mvn", "-U", "versions:update-properties");
  } catch (Exception e) {
    Printer.printRed(e.getMessage());
    System.exit(1);
  }
}

void run(String... command) throws Exception {
  var exitCode = new ProcessBuilder(command).inheritIO().start().waitFor();
  if (exitCode != 0) {
    Printer.printRed("Command failed with exit code " + exitCode);
    System.exit(exitCode);
  }
}
