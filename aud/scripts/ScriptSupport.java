import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/// Shared support for Java source-file scripts.
final class ScriptSupport {
  private static final Pattern ENVIRONMENT_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
  private static final Path API_ENVIRONMENT_FILE =
      Path.of("services", "service-http-apis", ".env.local");

  private ScriptSupport() {}

  static Map<String, String> loadEnvironmentFile(Path path) throws IOException {
    if (!Files.isRegularFile(path)) {
      System.out.println("[WARNING] No environment file found at '" + path + "'. Skipping...");
      return Map.of();
    }

    Printer.printBlue("### Loading environment variables from " + path + "...");
    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    Map<String, String> environment = new LinkedHashMap<>();
    for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
      parseEnvironmentLine(path, lineNumber, lines.get(lineNumber - 1), environment);
    }
    return environment;
  }

  static Map<String, String> loadApiEnvironment() throws IOException {
    return loadEnvironmentFile(API_ENVIRONMENT_FILE);
  }

  private static void parseEnvironmentLine(
      Path path, int lineNumber, String line, Map<String, String> environment)
      throws EnvironmentFileException {
    String trimmedLine = line.trim();
    if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
      return;
    }

    int separator = trimmedLine.indexOf('=');
    if (separator <= 0) {
      throw environmentFileError(path, lineNumber, "expected NAME=VALUE");
    }

    String name = trimmedLine.substring(0, separator).trim();
    if (!ENVIRONMENT_NAME.matcher(name).matches()) {
      throw environmentFileError(
          path, lineNumber, "invalid environment variable name '" + name + "'");
    }

    String encodedValue = trimmedLine.substring(separator + 1).trim();
    environment.put(name, parseEnvironmentValue(path, lineNumber, encodedValue));
  }

  private static String parseEnvironmentValue(Path path, int lineNumber, String encodedValue)
      throws EnvironmentFileException {
    if (encodedValue.isEmpty()) {
      return "";
    }

    char first = encodedValue.charAt(0);
    char last = encodedValue.charAt(encodedValue.length() - 1);
    boolean startsWithQuote = first == '\'' || first == '"';
    boolean endsWithQuote = last == '\'' || last == '"';
    String value = encodedValue;
    if (startsWithQuote || endsWithQuote) {
      if (encodedValue.length() < 2 || first != last) {
        throw environmentFileError(path, lineNumber, "unmatched value quote");
      }
      value = encodedValue.substring(1, encodedValue.length() - 1);
    }
    if (value.indexOf('\0') >= 0) {
      throw environmentFileError(path, lineNumber, "value contains a NUL character");
    }
    return value;
  }

  static Path findQuarkusRunner(Path targetDirectory) throws IOException {
    Path normalizedTarget = targetDirectory.toAbsolutePath().normalize();
    Path artifactMetadataPath = normalizedTarget.resolve("quarkus-artifact.properties");
    if (!Files.isRegularFile(artifactMetadataPath)) {
      throw new IllegalStateException(
          "Quarkus artifact metadata does not exist: " + artifactMetadataPath);
    }

    Properties artifactMetadata = new Properties();
    try (InputStream input = Files.newInputStream(artifactMetadataPath)) {
      artifactMetadata.load(input);
    }

    String artifactPath = artifactMetadata.getProperty("path");
    if (artifactPath == null || artifactPath.isBlank()) {
      throw new IllegalStateException(
          "Quarkus artifact metadata does not define a path: " + artifactMetadataPath);
    }

    Path runnerJar = normalizedTarget.resolve(artifactPath).normalize();
    if (!runnerJar.startsWith(normalizedTarget)
        || !runnerJar.getFileName().toString().endsWith("-runner.jar")
        || !Files.isRegularFile(runnerJar)) {
      throw new IllegalStateException(
          "Quarkus artifact metadata points to an invalid runner JAR: " + artifactPath);
    }
    return runnerJar;
  }

  static void runMain(ScriptAction action) {
    try {
      action.run();
    } catch (ProcessFailedException exception) {
      Printer.printRed(exception.getMessage());
      System.exit(exception.exitCode());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      Printer.printRed("Script execution interrupted.");
      System.exit(130);
    } catch (IOException | IllegalStateException exception) {
      Printer.printRed(exception.getMessage());
      System.exit(1);
    }
  }

  static void run(List<String> command, Map<String, String> environment)
      throws IOException, InterruptedException, ProcessFailedException {
    ProcessBuilder processBuilder = processBuilder(command, environment).inheritIO();
    int exitCode = processBuilder.start().waitFor();
    if (exitCode != 0) {
      throw new ProcessFailedException(command, exitCode);
    }
  }

  static void run(String... command)
      throws IOException, InterruptedException, ProcessFailedException {
    run(List.of(command), Map.of());
  }

  static Path javaExecutable(String executable) {
    String executableName =
        System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")
            ? executable + ".exe"
            : executable;
    return Path.of(System.getProperty("java.home"), "bin", executableName);
  }

  private static ProcessBuilder processBuilder(
      List<String> command, Map<String, String> environment) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.environment().putAll(environment);
    return processBuilder;
  }

  private static EnvironmentFileException environmentFileError(
      Path path, int lineNumber, String reason) {
    return new EnvironmentFileException(path + ":" + lineNumber + ": " + reason);
  }

  static final class EnvironmentFileException extends IOException {
    EnvironmentFileException(String message) {
      super(message);
    }
  }

  static final class ProcessFailedException extends Exception {
    private final int exitCode;

    ProcessFailedException(List<String> command, int exitCode) {
      this(command, exitCode, "");
    }

    ProcessFailedException(List<String> command, int exitCode, String output) {
      super(
          "Command failed with exit code "
              + exitCode
              + ": "
              + String.join(" ", command)
              + (output.isBlank() ? "" : System.lineSeparator() + output.strip()));
      this.exitCode = exitCode;
    }

    int exitCode() {
      return exitCode;
    }
  }

  @FunctionalInterface
  interface ScriptAction {
    void run() throws IOException, InterruptedException, ProcessFailedException;
  }
}
