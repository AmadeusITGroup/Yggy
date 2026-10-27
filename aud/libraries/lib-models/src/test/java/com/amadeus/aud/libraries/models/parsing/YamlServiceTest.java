package com.amadeus.aud.libraries.models.parsing;

import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.configuredYamlService;
import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.deleteOutput;
import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.outputPath;
import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.resourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class YamlServiceTest {
  private static final YamlService SUT = configuredYamlService(true);
  private static final YamlService NON_FIXING_SUT = configuredYamlService(false);

  private static final String SINGLE_MAPPING =
      """
      ---
      api_version: v1
      kind: ScopeVersionMapping
      metadata:
        name: single
        scope: unit
      spec:
        scope_version: 2.3.4
        aud_version: 9.8.7
      """;

  @Nested
  class ImportFromStringTests {
    @Test
    void singleDocumentNormalizesKeysAndParsesValues() throws IOException {
      // Arrange
      String content = SINGLE_MAPPING;

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromString(content);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class);
      V1ScopeVersionMapping mapping =
          (V1ScopeVersionMapping) result.get(V1ScopeVersionMapping.class).get(0);
      assertThat(mapping.getApiVersion()).isEqualTo("v1");
      assertThat(mapping.getMetadata().getName()).isEqualTo("single");
      assertThat(mapping.getMetadata().getScope()).isEqualTo("unit");
      assertThat(mapping.getSpec()).isNotNull();
      assertThat(mapping.getSpec().getScopeVersion()).isEqualTo("2.3.4");
      assertThat(mapping.getSpec().getAudVersion()).isEqualTo("9.8.7");
    }

    @Test
    void multipleDocumentsIgnoreBlankCommentsAndUnsupportedShapes() throws IOException {
      // Arrange
      String content =
          Files.readString(resourceFile("models.yml").toPath(), StandardCharsets.UTF_8);

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromString(content);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class, V1ScopeReleaseNote.class);
      assertThat(result.get(V1ScopeVersionMapping.class))
          .extracting(element -> element.getMetadata().getName())
          .containsExactly("duplicate", "duplicate");
      V1ScopeReleaseNote releaseNote =
          (V1ScopeReleaseNote) result.get(V1ScopeReleaseNote.class).get(0);
      assertThat(releaseNote.getMetadata().getName()).isEqualTo("release");
      assertThat(releaseNote.getSpec()).isNotNull();
      assertThat(releaseNote.getSpec().getScopeVersion()).isEqualTo("3.0.0");
      assertThat(releaseNote.getSpec().getPrs()).containsExactly("17", "18");
    }

    @Test
    void commentsAndBlankDocumentsProduceEmptyData() throws IOException {
      // Arrange
      String content = "# preface\n---\n# blank document\n---\n{}\n---\nscalar\n---\n- list\n";

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromString(content);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    void unknownMissingAndNullKindsAreIgnored() throws IOException {
      // Arrange
      String content =
          """
          ---
          kind: UnknownKind
          metadata:
            name: unknown
          ---
          metadata:
            name: missing
          ---
          kind: null
          metadata:
            name: null-kind
          """;

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromString(content);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    void invalidSyntaxPropagatesParserDiagnostic() {
      // Arrange
      String content = "---\nmetadata: [broken\n";

      // Act and assert
      assertThatThrownBy(() -> SUT.importFromString(content))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("expected ',' or ']'");
    }

    @Test
    void enabledFixesNormalizeBooleansCollectionsMapsAndStrings() throws IOException {
      // Arrange
      String content =
          """
          ---
          api_version: v1
          kind: Component
          metadata:
            name: true-value
            scope: unit
            provider_specific:
              cloud: azure
            labels:
              teams:
                - one
                - two
          spec:
            parents: parent
            is_hosting: TRUE
            deprecated_message:
              - old
              - value
          ---
          api_version: v1
          kind: Component
          metadata:
            name: false-value
            scope: unit
          spec:
            parents: []
            is_hosting: FALSE
          ---
          api_version: v1
          kind: Component
          metadata:
            name: fallback-value
            scope: unit
          spec:
            parents: []
            is_hosting: perhaps
          """;

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromString(content);

      // Assert
      List<ModelElement> elements = result.get(V1Component.class);
      assertThat(elements).hasSize(3);
      V1Component trueValue = (V1Component) elements.get(0);
      V1Component falseValue = (V1Component) elements.get(1);
      V1Component fallbackValue = (V1Component) elements.get(2);
      assertThat(trueValue.getSpec().getIsHosting()).isTrue();
      assertThat(falseValue.getSpec().getIsHosting()).isFalse();
      assertThat(fallbackValue.getSpec().getIsHosting()).isFalse();
      assertThat(trueValue.getSpec().getParents()).containsExactly("parent");
      assertThat(trueValue.getSpec().getDeprecatedMessage()).isEqualTo("[\"old\",\"value\"]");
      assertThat(trueValue.getMetadata().getProviderSpecific()).isEqualTo("{\"cloud\":\"azure\"}");
      assertThat(trueValue.getMetadata().getLabels()).containsEntry("teams", "[\"one\",\"two\"]");
    }

    @Test
    void disabledFixesSkipFailedConversionAndContinueWithNextDocument() throws IOException {
      // Arrange
      String content =
          """
          ---
          kind: Component
          metadata:
            name: invalid
            scope: unit
          spec:
            parents: []
            is_hosting: perhaps
          ---
          api_version: v1
          kind: ScopeVersionMapping
          metadata:
            name: retained
            scope: unit
          """;

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          NON_FIXING_SUT.importFromString(content);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class);
      assertThat(result.get(V1ScopeVersionMapping.class))
          .singleElement()
          .satisfies(element -> assertThat(element.getMetadata().getName()).isEqualTo("retained"));
    }
  }

  @Nested
  class ReaderAndFileTests {
    @Test
    void readerImportsMultipleDocumentsAndGroupsValues() throws IOException {
      // Arrange
      StringReader reader =
          new StringReader(
              SINGLE_MAPPING
                  + "\n---\n"
                  + "api_version: v1\n"
                  + "kind: ScopeVersionMapping\n"
                  + "metadata:\n"
                  + "  name: second\n"
                  + "  scope: unit\n");

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromReader(reader);

      // Assert
      assertThat(result.get(V1ScopeVersionMapping.class))
          .extracting(element -> element.getMetadata().getName())
          .containsExactly("single", "second");
    }

    @Test
    void regularFileImportsSupportedDocuments() throws IOException {
      // Arrange
      File file = resourceFile("models.yml");

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          SUT.importFromFile(file);

      // Assert
      assertThat(result.get(V1ScopeVersionMapping.class)).hasSize(2);
      assertThat(result.get(V1ScopeReleaseNote.class)).hasSize(1);
    }

    @Test
    void nullFileWithDestinationIsRejected() {
      // Arrange
      Map<Class<? extends ModelElement>, List<ModelElement>> destination = new HashMap<>();

      // Act and assert
      assertThatThrownBy(() -> SUT.importFromFile(null, destination))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: null");
      assertThat(destination).isEmpty();
    }

    @Test
    void nonexistentFileWithDestinationIsRejected() {
      // Arrange
      File file = Path.of("target", "parsing-service-tests", "absent.yml").toFile();
      Map<Class<? extends ModelElement>, List<ModelElement>> destination = new HashMap<>();

      // Act and assert
      assertThatThrownBy(() -> SUT.importFromFile(file, destination))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: " + file.getAbsolutePath());
      assertThat(destination).isEmpty();
    }

    @Test
    void directoryWithDestinationIsRejected() {
      // Arrange
      File directory = Path.of("target").toFile();
      Map<Class<? extends ModelElement>, List<ModelElement>> destination = new HashMap<>();

      // Act and assert
      assertThatThrownBy(() -> SUT.importFromFile(directory, destination))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: " + directory.getAbsolutePath());
      assertThat(destination).isEmpty();
    }
  }

  @Nested
  class CheckerImportTests {
    @Test
    void checkerAddsSourceAndFieldLabelsWhileRetainingDuplicateTypes() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerLabels", "documents.yml");
      String content =
          String.join(
                  "\r\n",
                  "  # checker preface",
                  "  ",
                  "---",
                  "api_version: v1",
                  "kind: ScopeReleaseNote",
                  "metadata:",
                  "  name: checker",
                  "  scope: null",
                  "  comment: kept",
                  "spec:",
                  "  scope_version: null",
                  "  version_datetime: retained",
                  "  prs: [17, null, 19]",
                  "  commits: []",
                  "---",
                  "api_version: v1",
                  "kind: ScopeReleaseNote",
                  "metadata:",
                  "  name: no-spec",
                  "  scope: unit",
                  "---",
                  "kind: UnknownKind",
                  "metadata: {}",
                  "---",
                  "- list",
                  "- document")
              + "\r\n";
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump(content, input);

        // Act
        YamlService.importFromFile(
            (YAMLFactory) SUT.getMapper().getFactory(), SUT.getMapper(), input, destination);

        // Assert
        assertThat(destination).containsOnlyKeys(V1ScopeReleaseNote.class);
        List<V1Base> releaseNotes = destination.get(V1ScopeReleaseNote.class);
        assertThat(releaseNotes)
            .extracting(element -> element.getMetadata().getName())
            .containsExactly("checker", "no-spec");

        V1Base checker = releaseNotes.get(0);
        assertThat(checker.getMetadata().getLabels())
            .containsEntry(YamlService.METADATA_KEYS_LABEL, "name,scope,comment")
            .containsEntry(
                YamlService.SPEC_KEYS_LABEL, "scope_version,version_datetime,prs,commits")
            .containsEntry(YamlService.NULL_FIELDS_LABEL, "metadata.scope,spec.scope_version")
            .containsEntry(YamlService.NULL_LIST_ELEMENTS_LABEL, "spec.prs")
            .containsEntry(YamlService.LOCATION_LABEL, input + ":4");

        V1Base noSpec = releaseNotes.get(1);
        assertThat(noSpec.getMetadata().getLabels())
            .containsEntry(YamlService.METADATA_KEYS_LABEL, "name,scope")
            .containsEntry(YamlService.SPEC_KEYS_LABEL, "")
            .containsEntry(YamlService.NULL_FIELDS_LABEL, "")
            .containsEntry(YamlService.NULL_LIST_ELEMENTS_LABEL, "")
            .containsEntry(YamlService.LOCATION_LABEL, input + ":16");
      } finally {
        deleteOutput(input);
      }
    }

    @Test
    void checkerAcceptsAsciiCommentsAndBlankLinesWithoutDocuments() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerComments", "comments.yml");
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump("  \n# comment with line ending\n   ", input);

        // Act
        YamlService.importFromFile(
            (YAMLFactory) SUT.getMapper().getFactory(), SUT.getMapper(), input, destination);

        // Assert
        assertThat(destination).isEmpty();
      } finally {
        deleteOutput(input);
      }
    }

    @Test
    void checkerAcceptsCommentEndingAtEndOfFile() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerEndOfFileComment", "comments.yml");
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump("# comment ending at end of file", input);

        // Act
        YamlService.importFromFile(
            (YAMLFactory) SUT.getMapper().getFactory(), SUT.getMapper(), input, destination);

        // Assert
        assertThat(destination).isEmpty();
      } finally {
        deleteOutput(input);
      }
    }

    @Test
    void checkerAcceptsEmptyFile() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerEmpty", "empty.yml");
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump("", input);

        // Act
        YamlService.importFromFile(
            (YAMLFactory) SUT.getMapper().getFactory(), SUT.getMapper(), input, destination);

        // Assert
        assertThat(destination).isEmpty();
      } finally {
        deleteOutput(input);
      }
    }

    @Test
    void checkerRejectsMissingDocumentMarkerAfterTabsAndCarriageReturn() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerMissingMarker", "missing.yml");
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump("\t\rinvalid: document", input);

        // Act and assert
        assertThatThrownBy(
                () ->
                    YamlService.importFromFile(
                        (YAMLFactory) SUT.getMapper().getFactory(),
                        SUT.getMapper(),
                        input,
                        destination))
            .isInstanceOf(YamlService.MissingDocumentStartException.class)
            .hasMessage(
                "Yaml file must start with a document-start marker '---' on its first non-comment line.");
        assertThat(destination).isEmpty();
      } finally {
        deleteOutput(input);
      }
    }

    @Test
    void checkerReportsFirstNonAsciiByteLocation() throws Exception {
      // Arrange
      Path input = outputPath("yamlCheckerNonAscii", "non-ascii.yml");
      Map<Class<? extends V1Base>, List<V1Base>> destination = new HashMap<>();
      deleteOutput(input);

      try {
        SUT.dump("# ascii\r\n---\r\nname: café\r\n", input);

        // Act and assert
        assertThatThrownBy(
                () ->
                    YamlService.importFromFile(
                        (YAMLFactory) SUT.getMapper().getFactory(),
                        SUT.getMapper(),
                        input,
                        destination))
            .isInstanceOfSatisfying(
                YamlService.NonAsciiYamlException.class,
                (YamlService.NonAsciiYamlException exception) -> {
                  assertThat(exception.getLine()).isEqualTo(3);
                  assertThat(exception)
                      .hasMessageContaining("non-ASCII byte 0xC3")
                      .hasMessageContaining("line 3, column 10")
                      .hasMessageEndingWith("Change that character.");
                });
        assertThat(destination).isEmpty();
      } finally {
        deleteOutput(input);
      }
    }
  }

  @Nested
  class MapperTests {
    @Test
    void mapperExposesYamlAndSharedDeserializationConfiguration() {
      // Arrange
      YamlService sut = new YamlService();

      // Act
      ObjectMapper mapper = sut.getMapper();

      // Assert
      assertThat(mapper.getFactory()).isInstanceOf(YAMLFactory.class);
      assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
      assertThat(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)).isTrue();
      assertThat(mapper.coercionConfigDefaults().findAction(CoercionInputShape.EmptyString))
          .isEqualTo(CoercionAction.AsEmpty);
      assertThat(mapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
    }
  }
}
