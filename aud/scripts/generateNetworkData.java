import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/// Aggregates a directory of descriptor YAML files into the network data the services read at
/// startup.
///
/// Usage: `java scripts/generateNetworkData.java <data-directory>`
///
/// The directory must contain a `Scope/` directory of YAML descriptors plus one directory per
/// scope. The four aggregated files documented in `aud/README.md` are written to
/// `<data-directory>/network-data`.
///
/// The argument is required: no fixture data set ships with this repository, so there is nothing
/// sensible to default to.
///
/// Must be run from the `aud/` directory.
final String SCOPE_VERSION = "1.0.1";

final Pattern DOCUMENT_SEPARATOR = Pattern.compile("^---\\s*$");
final Pattern COMMENT = Pattern.compile("^\\s*#");
final Pattern KIND = Pattern.compile("^\\s*kind:\\s*(.*)$");
final Pattern METADATA = Pattern.compile("^\\s*metadata:\\s*$");
final Pattern TOP_LEVEL_KEY = Pattern.compile("^[^\\s][^:]*:\\s*.*$");
final Pattern METADATA_NAME = Pattern.compile("^\\s+name:\\s*(.*)$");

void main(String[] args) {
  if (args.length != 1) {
    System.err.println("Usage: java scripts/generateNetworkData.java <data-directory>");
    System.exit(2);
  }
  try {
    generateNetworkData(Path.of(args[0]));
  } catch (IOException | IllegalStateException e) {
    System.err.println("ERROR: " + e.getMessage());
    System.exit(1);
  }
}

void generateNetworkData(Path dataDirectory) throws IOException {
  Path scopeDirectory = dataDirectory.resolve("Scope");
  Path outputDirectory = dataDirectory.resolve("network-data");

  requireDirectory(dataDirectory, "data directory not found");
  requireDirectory(scopeDirectory, "Scope directory not found");

  List<Path> scopeFiles = listYamlFiles(scopeDirectory);
  if (scopeFiles.isEmpty()) {
    throw new IllegalStateException("No Scope YAML files found under " + scopeDirectory);
  }

  Set<String> discoveredScopes = discoverScopes(scopeFiles);
  if (discoveredScopes.isEmpty()) {
    throw new IllegalStateException(
        "No kind=Scope objects with metadata.name found in " + scopeDirectory);
  }

  List<String> includedScopes = new ArrayList<>();
  List<String> skippedScopes = new ArrayList<>();
  for (String scopeName : discoveredScopes) {
    if (Files.isDirectory(dataDirectory.resolve(scopeName))) {
      includedScopes.add(scopeName);
    } else {
      skippedScopes.add(scopeName);
    }
  }

  Files.createDirectories(outputDirectory);
  String runTimestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();

  Files.writeString(
      outputDirectory.resolve("metadata.yml"),
      renderMetadata(includedScopes, runTimestamp),
      StandardCharsets.UTF_8);
  int apiDocumentCount =
      aggregateApi(dataDirectory, includedScopes, outputDirectory.resolve("api.yml"));
  Files.writeString(
      outputDirectory.resolve("requirements.yml"),
      renderRequirements(includedScopes),
      StandardCharsets.UTF_8);
  Files.writeString(
      outputDirectory.resolve("release.yml"),
      renderReleaseNotes(includedScopes, runTimestamp),
      StandardCharsets.UTF_8);

  System.out.println("Done.");
  System.out.println("Discovered scopes: " + discoveredScopes.size());
  System.out.println("Included scopes (directory exists): " + includedScopes.size());
  System.out.println("Skipped scopes (missing directory): " + skippedScopes.size());
  if (!skippedScopes.isEmpty()) {
    System.out.println("Skipped list: " + String.join(" ", skippedScopes));
  }
  System.out.println("metadata.yml entries written: " + includedScopes.size());
  System.out.println("api.yml documents aggregated: " + apiDocumentCount);
  System.out.println("Output directory: " + outputDirectory.toAbsolutePath().normalize());
}

void requireDirectory(Path directory, String message) {
  if (!Files.isDirectory(directory)) {
    throw new IllegalStateException(message + ": " + directory.toAbsolutePath().normalize());
  }
}

List<Path> listYamlFiles(Path directory) throws IOException {
  try (Stream<Path> paths = Files.list(directory)) {
    return paths
        .filter(Files::isRegularFile)
        .filter(this::isYamlFile)
        .sorted(
            Comparator.comparingInt(this::extensionOrder)
                .thenComparing(path -> path.getFileName().toString()))
        .toList();
  }
}

boolean isYamlFile(Path path) {
  String fileName = path.getFileName().toString();
  return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
}

int extensionOrder(Path path) {
  return path.getFileName().toString().endsWith(".yml") ? 0 : 1;
}

Set<String> discoverScopes(List<Path> scopeFiles) throws IOException {
  Set<String> scopes = new TreeSet<>();
  ScopeDocument document = new ScopeDocument();

  for (Path scopeFile : scopeFiles) {
    for (String line : Files.readAllLines(scopeFile, StandardCharsets.UTF_8)) {
      if (DOCUMENT_SEPARATOR.matcher(line).matches()) {
        document.addScopeTo(scopes);
        document = new ScopeDocument();
        continue;
      }
      if (COMMENT.matcher(line).find()) {
        continue;
      }

      Matcher kindMatcher = KIND.matcher(line);
      if (kindMatcher.matches()) {
        document.kind = kindMatcher.group(1).trim();
        document.inMetadata = false;
        continue;
      }
      if (METADATA.matcher(line).matches()) {
        document.inMetadata = true;
        continue;
      }
      if (TOP_LEVEL_KEY.matcher(line).matches()) {
        document.inMetadata = false;
        continue;
      }

      Matcher nameMatcher = METADATA_NAME.matcher(line);
      if (document.inMetadata && nameMatcher.matches()) {
        document.name = unquote(nameMatcher.group(1).trim());
      }
    }
  }

  document.addScopeTo(scopes);
  return scopes;
}

String unquote(String value) {
  if (value.length() >= 2) {
    char first = value.charAt(0);
    char last = value.charAt(value.length() - 1);
    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
      return value.substring(1, value.length() - 1);
    }
  }
  return value;
}

String renderMetadata(List<String> includedScopes, String runTimestamp) {
  StringBuilder output = new StringBuilder();
  for (String scopeName : includedScopes) {
    output
        .append("---\n")
        .append("api_version: v1\n")
        .append("kind: ScopeVersionMapping\n")
        .append("metadata:\n")
        .append("  name: ")
        .append(scopeName)
        .append('_')
        .append(SCOPE_VERSION)
        .append('\n')
        .append("  scope: ")
        .append(scopeName)
        .append('\n')
        .append("spec:\n")
        .append("  scope_version: ")
        .append(SCOPE_VERSION)
        .append('\n')
        .append("  version_datetime: ")
        .append(runTimestamp)
        .append("\n\n");
  }
  return output.toString();
}

String renderRequirements(List<String> includedScopes) {
  StringBuilder output = new StringBuilder("---\nrequirements:\n");
  for (String scopeName : includedScopes) {
    output
        .append("  - name: ")
        .append(scopeName)
        .append('\n')
        .append("    scope: ")
        .append(scopeName)
        .append('\n')
        .append("    comment: ")
        .append(scopeName)
        .append('\n')
        .append("    repo: projects/YGGY/repos/auds-")
        .append(scopeName)
        .append("/raw/\n")
        .append("    files:\n")
        .append("      - ")
        .append(scopeName)
        .append(".yml\n")
        .append("    version: ")
        .append(SCOPE_VERSION)
        .append('\n');
  }
  return output.toString();
}

String renderReleaseNotes(List<String> includedScopes, String runTimestamp) {
  StringBuilder output = new StringBuilder();
  for (String scopeName : includedScopes) {
    output
        .append("---\n")
        .append("api_version: v1\n")
        .append("kind: ScopeReleaseNote\n")
        .append("metadata:\n")
        .append("  name: ")
        .append(scopeName)
        .append('\n')
        .append("  scope: ")
        .append(scopeName)
        .append('\n')
        .append("spec:\n")
        .append("  version_datetime: ")
        .append(runTimestamp)
        .append('\n')
        .append("  scope_version: ")
        .append(SCOPE_VERSION)
        .append('\n')
        .append("  commits:\n")
        .append("  - commitid: 42\n")
        .append("    message: initial commit\n")
        .append("  prs:\n")
        .append("  - '1'\n")
        .append("  impacted_devices: []\n\n");
  }
  return output.toString();
}

int aggregateApi(Path dataDirectory, List<String> includedScopes, Path apiFile) throws IOException {
  int documentCount = 0;
  try (OutputStream output =
      Files.newOutputStream(
          apiFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
    for (String scopeName : includedScopes) {
      for (Path yamlFile : listYamlFiles(dataDirectory.resolve(scopeName))) {
        int separatorCount = countDocumentSeparators(yamlFile);
        if (separatorCount > 0) {
          documentCount += separatorCount;
        } else if (Files.size(yamlFile) > 0) {
          documentCount++;
        }

        Files.copy(yamlFile, output);
        output.write('\n');
        output.write('\n');
      }
    }
  }
  return documentCount;
}

int countDocumentSeparators(Path yamlFile) throws IOException {
  int count = 0;
  for (String line : Files.readAllLines(yamlFile, StandardCharsets.UTF_8)) {
    if (DOCUMENT_SEPARATOR.matcher(line).matches()) {
      count++;
    }
  }
  return count;
}

final class ScopeDocument {
  String kind = "";
  String name = "";
  boolean inMetadata;

  void addScopeTo(Set<String> scopes) {
    if ("Scope".equals(kind) && !name.isEmpty()) {
      scopes.add(name);
    }
  }
}
