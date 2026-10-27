import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/// Regenerates ADR index tables (global + module-specific) in a consistent, deterministic format.
///
/// Usage: `java generateAdrIndexes.java [adrRoot]`
///
/// Must be run from the `aud/` directory. Defaults to `.` as the repository root.

record AdrConfig(Path indexPath, String adrSubdir, Pattern filePattern) {
  Path adrDir() {
    return indexPath.getParent().resolve(adrSubdir);
  }

  Path templatePath() {
    var baseName = indexPath.getFileName().toString().replace(".md", "");
    var preferred =
        indexPath.getParent().resolve("templates").resolve(baseName + ".index.template.md");
    var legacy = indexPath.getParent().resolve(baseName + ".index.template.md");
    if (Files.exists(preferred)) return preferred;
    if (Files.exists(legacy)) {
      Printer.printRed(
          "  ⚠️  Using legacy template location: "
              + legacy
              + " (consider moving to templates directory)");
      return legacy;
    }
    return null;
  }
}

record AdrRow(int number, String link, String title, String status) {}

static final String AUTO_NOTICE =
    "<!-- AUTO-GENERATED FILE: Do not edit directly. Run `java scripts/generateAdrIndexes` -->";

void main(String[] args) {
  try {
    var root = Path.of(args.length > 0 ? args[0] : ".").toRealPath();
    Printer.printBlue("Repository root resolved to: " + root);

    var configs =
        List.of(
            new AdrConfig(
                root.resolve("docs/ADRs.md"), "ADRs", Pattern.compile("^ADR-[0-9]{3}-.*\\.md$")),
            new AdrConfig(
                root.resolve("services/service-http-connectivity-engine/docs/ADRs.md"),
                "ADRs",
                Pattern.compile("^ADR-CONN-[0-9]{3}-.*\\.md$")));

    for (var config : configs) {
      processConfig(config);
    }

    Printer.printGreen("ADR index regeneration complete.");
  } catch (Exception e) {
    Printer.printRed(e.getMessage());
    System.exit(1);
  }
}

void processConfig(AdrConfig config) throws IOException {
  var adrDir = config.adrDir();
  if (!Files.isDirectory(adrDir)) {
    Printer.printRed("  ⚠️  Skipping missing ADR directory: " + adrDir);
    return;
  }

  var templatePath = config.templatePath();
  if (templatePath == null) {
    Printer.printRed("  ⚠️  Missing template for index " + config.indexPath());
    return;
  }

  var skippedFiles =
      List.of("ADR-000-template.md", "ADR-API-000-template.md", "ADR-COMP-000-template.md");
  var linkPrefix = config.adrSubdir() + "/";

  var rows = new ArrayList<AdrRow>();
  try (var files = Files.list(adrDir)) {
    files
        .filter(f -> f.toString().endsWith(".md"))
        .filter(f -> !skippedFiles.contains(f.getFileName().toString()))
        .filter(f -> config.filePattern().matcher(f.getFileName().toString()).matches())
        .forEach(
            f -> {
              System.out.println("  Processing: " + f);
              parseAdrRow(f, linkPrefix).ifPresent(rows::add);
            });
  }
  System.out.println("  Found " + rows.size() + " ADR entries in " + adrDir);

  rows.sort(Comparator.comparingInt(AdrRow::number));

  var template = Files.readString(templatePath);
  var table = renderTable(rows);

  // Replace the header + separator + blank line + placeholder with the full padded table
  var rendered = template.replaceAll("\\| ADR \\|.*\\n\\|[-| ]+\\|\\n*\\{\\{ADR_ROWS}}", table);

  if (!rendered.contains("<!-- AUTO-GENERATED FILE")) {
    rendered = AUTO_NOTICE + "\n\n" + rendered;
  }

  Files.writeString(config.indexPath(), rendered);
  Printer.printGreen(
      "  Updated: " + config.indexPath() + " using template " + templatePath.getFileName());
}

/// Parses an ADR markdown file and returns a table row with ID, title, and status.
static final Pattern HEADING_PATTERN =
    Pattern.compile("^#\\s+(ADR(?:-(?:API|CONN))?-([0-9]{3})):?\\s*(.*)$");

static final Pattern STATUS_PATTERN = Pattern.compile("^- Status:\\s*(.+)$");

Optional<AdrRow> parseAdrRow(Path file, String linkPrefix) {
  List<String> lines;
  try {
    lines = Files.readAllLines(file);
  } catch (IOException e) {
    Printer.printRed("  ⚠️  Failed to read " + file + ": " + e.getMessage());
    return Optional.empty();
  }

  String id = null;
  String title = null;
  int number = 0;
  String status = "Unknown";

  for (var line : lines) {
    if (id == null) {
      var hm = HEADING_PATTERN.matcher(line);
      if (hm.matches()) {
        id = hm.group(1);
        number = Integer.parseInt(hm.group(2));
        title = hm.group(3);
      }
    }
    var sm = STATUS_PATTERN.matcher(line);
    if (sm.matches()) {
      status = sm.group(1).trim();
      break;
    }
  }

  if (id == null) {
    Printer.printRed("  ⚠️  No ADR heading found in " + file);
    return Optional.empty();
  }

  var fileName = file.getFileName().toString();
  var link = "[" + id + "](" + linkPrefix + fileName + ")";
  return Optional.of(new AdrRow(number, link, title, status));
}

/// Renders a padded markdown table with header, separator and data rows.
String renderTable(List<AdrRow> rows) {
  var headers = new String[] {"ADR", "Title", "Status"};

  // Compute max width per column
  var widths = new int[headers.length];
  for (int i = 0; i < headers.length; i++) {
    widths[i] = headers[i].length();
  }
  for (var row : rows) {
    widths[0] = Math.max(widths[0], row.link().length());
    widths[1] = Math.max(widths[1], row.title().length());
    widths[2] = Math.max(widths[2], row.status().length());
  }

  var sb = new StringBuilder();

  // Header
  sb.append("| ")
      .append(pad(headers[0], widths[0]))
      .append(" | ")
      .append(pad(headers[1], widths[1]))
      .append(" | ")
      .append(pad(headers[2], widths[2]))
      .append(" |\n");

  // Separator
  sb.append("|")
      .append("-".repeat(widths[0] + 2))
      .append("|")
      .append("-".repeat(widths[1] + 2))
      .append("|")
      .append("-".repeat(widths[2] + 2))
      .append("|\n");

  // Data rows
  for (var row : rows) {
    sb.append("| ")
        .append(pad(row.link(), widths[0]))
        .append(" | ")
        .append(pad(row.title(), widths[1]))
        .append(" | ")
        .append(pad(row.status(), widths[2]))
        .append(" |\n");
  }

  return sb.toString().stripTrailing();
}

String pad(String value, int width) {
  if (value.length() >= width) return value;
  return value + " ".repeat(width - value.length());
}
