package com.amadeus.aud.libraries.models.parsing;

import com.amadeus.aud.libraries.common.SBUtils;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public final class YamlService extends AbstractDocumentParsingService {
  public static final String METADATA_KEYS_LABEL = "com.amadeus.aud/metadata_keys";
  public static final String SPEC_KEYS_LABEL = "com.amadeus.aud/spec_keys";
  public static final String NULL_FIELDS_LABEL = "com.amadeus.aud/null_fields";
  public static final String NULL_LIST_ELEMENTS_LABEL = "com.amadeus.aud/null_list_elements";
  public static final String LOCATION_LABEL = "com.amadeus.aud/location";

  @Inject
  public YamlService() {
    super(ObjectMapperFactory.createYamlMapper());
  }

  public ObjectMapper getMapper() {
    return this.objectMapper;
  }

  @Override
  protected ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> parseContent(
      String yaml) throws IOException {
    final ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> data =
        new ConcurrentHashMap<>();

    try (YAMLParser yamlParser = (YAMLParser) createParser(yaml)) {
      importDocumentsForModelElement(objectMapper, yamlParser, data);
    }

    return data;
  }

  @Override
  protected void parseFromRegularFile(
      File file, Map<Class<? extends ModelElement>, List<ModelElement>> data) throws IOException {
    try (YAMLParser yamlParser = (YAMLParser) createParser(file)) {
      importDocumentsForModelElement(objectMapper, yamlParser, data);
    }
  }

  public ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> importFromReader(
      Reader reader) throws IOException {
    final ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> data =
        new ConcurrentHashMap<>();
    try (YAMLParser yamlParser = (YAMLParser) objectMapper.getFactory().createParser(reader)) {
      importDocumentsForModelElement(objectMapper, yamlParser, data);
    }
    return data;
  }

  /**
   * Only used for the checker, do not use elsewhere.
   *
   * <p>Enforces two AUD-specific invariants in addition to YAML syntax:
   *
   * <ul>
   *   <li>The file must be pure US-ASCII (any byte {@code > 0x7F} is rejected).
   *   <li>The first non-blank, non-comment line must be the document-start marker {@code ---}.
   * </ul>
   *
   * Violations are reported via the typed {@link NonAsciiYamlException} / {@link
   * MissingDocumentStartException} (both extend {@link IOException}).
   */
  public static void importFromFile(
      YAMLFactory yamlFactory,
      ObjectMapper mapper,
      Path path,
      Map<Class<? extends V1Base>, List<V1Base>> data)
      throws IOException {
    String content = readStrictAsciiContent(path);
    requireDocumentStartMarker(content);
    try (Reader reader = new StringReader(content);
        YAMLParser yamlParser = yamlFactory.createParser(reader)) {
      importDocumentsForV1Base(mapper, yamlParser, path, data);
    }
  }

  private static String readStrictAsciiContent(Path path) throws IOException {
    byte[] bytes = Files.readAllBytes(path);
    int line = 1;
    int column = 1;
    for (byte b : bytes) {
      if ((b & 0x80) != 0) {
        throw new NonAsciiYamlException(line, column, b);
      }
      if (b == '\n') {
        line = line + 1;
        column = 1;
        continue;
      }
      // '\r' alone does not advance the line counter; '\n' (if any) will.
      if (b != '\r') {
        column = column + 1;
      }
    }
    return new String(bytes, StandardCharsets.US_ASCII);
  }

  private static void requireDocumentStartMarker(String content)
      throws MissingDocumentStartException {
    int len = content.length();
    int i = 0;
    while (i < len) {
      i = skipBlanks(content, i);
      if (i >= len) {
        return;
      }
      char c = content.charAt(i);
      if (c == '\n' || c == '\r') {
        i = skipLineEnd(content, i);
      } else if (c == '#') {
        i = skipLineEnd(content, skipToLineEnd(content, i));
      } else if (content.startsWith("---", i)) {
        return;
      } else {
        throw new MissingDocumentStartException();
      }
    }
  }

  private static int skipBlanks(String content, int from) {
    int len = content.length();
    int i = from;
    while (i < len && (content.charAt(i) == ' ' || content.charAt(i) == '\t')) {
      i = i + 1;
    }
    return i;
  }

  private static int skipToLineEnd(String content, int from) {
    int len = content.length();
    int i = from;
    while (i < len && content.charAt(i) != '\n' && content.charAt(i) != '\r') {
      i = i + 1;
    }
    return i;
  }

  private static int skipLineEnd(String content, int start) {
    int len = content.length();
    int i = start;
    if (i < len && content.charAt(i) == '\r') {
      i = i + 1;
    }
    if (i < len && content.charAt(i) == '\n') {
      i = i + 1;
    }
    return i;
  }

  /** Thrown when a checker YAML file contains any non-ASCII byte. */
  public static final class NonAsciiYamlException extends IOException {
    private final int line;

    NonAsciiYamlException(int line, int column, byte offendingByte) {
      super(
          String.format(
              "Yaml file contains non-ASCII byte 0x%02X at line %d, column %d.\nChange that character.",
              offendingByte & 0xFF, line, column));
      this.line = line;
    }

    public int getLine() {
      return line;
    }
  }

  /** Thrown when a checker YAML file is missing its leading {@code ---} document-start marker. */
  public static final class MissingDocumentStartException extends IOException {
    MissingDocumentStartException() {
      super(
          "Yaml file must start with a document-start marker '---' on its first non-comment line.");
    }
  }

  private static void importDocumentsForV1Base(
      ObjectMapper mapper,
      YAMLParser yamlParser,
      Path path,
      Map<Class<? extends V1Base>, List<V1Base>> data)
      throws IOException {
    String pathName = path.toString();
    final MappingIterator<JsonNode> iterator = mapper.readValues(yamlParser, JsonNode.class);

    StringBuilder nullFields = new StringBuilder();
    StringBuilder nullListFields = new StringBuilder();
    StringBuilder metadataFields = new StringBuilder();
    StringBuilder specFields = new StringBuilder();

    while (iterator.hasNext()) {
      int lineNumber = iterator.getCurrentLocation().getLineNr();
      final JsonNode node = iterator.next();
      final Object o = mapper.treeToValue(node, Object.class);

      SBUtils.reset(nullFields);
      SBUtils.reset(nullListFields);
      SBUtils.reset(specFields);
      SBUtils.reset(metadataFields);
      if (o instanceof V1Base v1Base) {
        v1Base.getMetadata().putLabelsItem(LOCATION_LABEL, pathName + ":" + lineNumber);

        collectFieldKeys(node.get("metadata"), "metadata", nullFields, null, metadataFields);
        SBUtils.removeLastChar(metadataFields);
        v1Base.getMetadata().putLabelsItem(METADATA_KEYS_LABEL, metadataFields.toString());

        collectFieldKeys(node.get("spec"), "spec", nullFields, nullListFields, specFields);
        SBUtils.removeLastChar(specFields);
        v1Base.getMetadata().putLabelsItem(SPEC_KEYS_LABEL, specFields.toString());

        SBUtils.removeLastChar(nullFields);
        v1Base.getMetadata().putLabelsItem(NULL_FIELDS_LABEL, nullFields.toString());
        SBUtils.removeLastChar(nullListFields);
        v1Base.getMetadata().putLabelsItem(NULL_LIST_ELEMENTS_LABEL, nullListFields.toString());

        data.computeIfAbsent(v1Base.getClass(), k -> new ArrayList<>()).add(v1Base);
      }
    }
  }

  private static void collectFieldKeys(
      JsonNode sectionNode,
      String sectionPrefix,
      StringBuilder nullFields,
      StringBuilder nullListFields,
      StringBuilder fieldKeys) {
    if (sectionNode == null) {
      return;
    }
    sectionNode
        .fieldNames()
        .forEachRemaining(
            (String f) -> {
              collectNullFields(sectionNode.get(f), sectionPrefix, f, nullFields, nullListFields);
              fieldKeys.append(f).append(',');
            });
  }

  private static void collectNullFields(
      JsonNode fieldNode,
      String sectionPrefix,
      String fieldName,
      StringBuilder nullFields,
      StringBuilder nullListFields) {
    if (fieldNode == null || fieldNode.isNull()) {
      nullFields.append(sectionPrefix).append('.').append(fieldName).append(',');
    } else if (nullListFields != null && fieldNode.isArray() && containsNullElement(fieldNode)) {
      nullListFields.append(sectionPrefix).append('.').append(fieldName).append(',');
    } else {
      // Field is neither null nor an array with null elements — nothing to track.
    }
  }

  private static boolean containsNullElement(JsonNode arrayNode) {
    for (JsonNode item : arrayNode) {
      if (item.isNull()) {
        return true;
      }
    }
    return false;
  }

  private static void importDocumentsForModelElement(
      ObjectMapper mapper,
      YAMLParser yamlParser,
      Map<Class<? extends ModelElement>, List<ModelElement>> data)
      throws IOException {
    final MappingIterator<Object> iterator = mapper.readValues(yamlParser, Object.class);

    while (iterator.hasNext()) {
      final Object o = iterator.next();

      if (o instanceof V1Base v1Base) {
        data.computeIfAbsent(v1Base.getClass(), k -> new ArrayList<>()).add(v1Base);
      }
    }
  }
}
