import java.io.IOException;
import java.util.List;
import java.util.Map;

/// Starts the AUD API in Quarkus development mode.
///
/// Usage: `java scripts/startApiQuarkusDev.java`
///
/// Must be run from the `aud/` directory.

void main() {
  ScriptSupport.runMain(this::startApiQuarkusDev);
}

private void startApiQuarkusDev()
    throws IOException, InterruptedException, ScriptSupport.ProcessFailedException {
  Printer.printBlue("## Reading environment file");
  Map<String, String> environment = ScriptSupport.loadApiEnvironment();

  Printer.printBlue("## Starting service-http-apis as quarkus:dev");
  ScriptSupport.run(
      List.of("mvn", "--also-make", "--projects", "services/service-http-apis", "quarkus:dev"),
      environment);
}
