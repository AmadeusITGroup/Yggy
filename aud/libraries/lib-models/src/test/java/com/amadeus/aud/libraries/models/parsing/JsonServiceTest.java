package com.amadeus.aud.libraries.models.parsing;

import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.deleteOutput;
import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.outputPath;
import static com.amadeus.aud.libraries.models.parsing.ParsingServiceTestSupport.resourceFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

final class JsonServiceTest {
  private static final String SINGLE_MAPPING =
      """
      [
        {
          "apiVersion": "v1",
          "kind": "ScopeVersionMapping",
          "metadata": {
            "name": "single",
            "scope": "unit"
          },
          "spec": {
            "scope_version": "2.3.4",
            "aud_version": "9.8.7"
          }
        }
      ]
      """;

  @Nested
  class ImportFromStringTests {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n\t "})
    void blankContentReturnsEmptyData(String content) throws IOException {
      // Arrange
      JsonService sut = new JsonService();

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromString(content);

      // Assert
      assertThat(result).isEmpty();
    }

    @Test
    void singleElementArrayParsesTypeAndValues() throws IOException {
      // Arrange
      JsonService sut = new JsonService();

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromString(SINGLE_MAPPING);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class);
      List<ModelElement> elements = result.get(V1ScopeVersionMapping.class);
      assertThat(elements).hasSize(1);
      V1ScopeVersionMapping mapping = (V1ScopeVersionMapping) elements.get(0);
      assertThat(mapping.getApiVersion()).isEqualTo("v1");
      assertThat(mapping.getMetadata().getName()).isEqualTo("single");
      assertThat(mapping.getMetadata().getScope()).isEqualTo("unit");
      assertThat(mapping.getSpec()).isNotNull();
      assertThat(mapping.getSpec().getScopeVersion()).isEqualTo("2.3.4");
      assertThat(mapping.getSpec().getAudVersion()).isEqualTo("9.8.7");
    }

    @Test
    void multipleElementsGroupTypesAndRetainDuplicateEntries() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      String content =
          Files.readString(resourceFile("models.json").toPath(), StandardCharsets.UTF_8);

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromString(content);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class, V1ScopeReleaseNote.class);
      List<ModelElement> mappings = result.get(V1ScopeVersionMapping.class);
      assertThat(mappings)
          .extracting(element -> element.getMetadata().getName())
          .containsExactly("duplicate", "duplicate");
      assertThat(mappings.get(0)).isNotSameAs(mappings.get(1));

      List<ModelElement> releaseNotes = result.get(V1ScopeReleaseNote.class);
      assertThat(releaseNotes).hasSize(1);
      V1ScopeReleaseNote releaseNote = (V1ScopeReleaseNote) releaseNotes.get(0);
      assertThat(releaseNote.getMetadata().getName()).isEqualTo("release");
      assertThat(releaseNote.getSpec()).isNotNull();
      assertThat(releaseNote.getSpec().getPrs()).containsExactly("17");
      assertThat(releaseNote.getSpec().getScopeVersion()).isEqualTo("3.0.0");
    }

    @Test
    void duplicateObjectKeysUseLastValue() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      String content =
          """
          [{
            "apiVersion": "v1",
            "kind": "ScopeVersionMapping",
            "metadata": {
              "name": "first",
              "name": "last",
              "scope": "unit"
            }
          }]
          """;

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromString(content);

      // Assert
      ModelElement element = result.get(V1ScopeVersionMapping.class).get(0);
      assertThat(element.getMetadata().getName()).isEqualTo("last");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"kind\":\"ScopeVersionMapping\"}", "\"scalar\"", "42", "null"})
    void mapAndScalarRootsAreRejected(String content) {
      // Arrange
      JsonService sut = new JsonService();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromString(content))
          .isInstanceOf(IOException.class)
          .hasMessage("Expected JSON array at root level");
    }

    @ParameterizedTest
    @MethodSource("com.amadeus.aud.libraries.models.parsing.JsonServiceTest#unsupportedElements")
    void unsupportedArrayElementsFailWithDiagnostic(
        String content, Class<? extends JsonMappingException> exceptionType, String diagnostic) {
      // Arrange
      JsonService sut = new JsonService();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromString(content))
          .isInstanceOf(exceptionType)
          .hasMessageContaining(diagnostic);
    }

    @ParameterizedTest
    @MethodSource("com.amadeus.aud.libraries.models.parsing.JsonServiceTest#malformedJson")
    void invalidSyntaxAndCommentsAreRejected(String content, String diagnostic) {
      // Arrange
      JsonService sut = new JsonService();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromString(content))
          .isInstanceOf(JsonProcessingException.class)
          .hasMessageContaining(diagnostic);
    }
  }

  @Nested
  class ImportFromReaderTests {
    @Test
    void readerParsesSupportedElement() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      StringReader reader = new StringReader(SINGLE_MAPPING);

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromReader(reader);

      // Assert
      assertThat(result.get(V1ScopeVersionMapping.class))
          .singleElement()
          .satisfies(element -> assertThat(element.getMetadata().getName()).isEqualTo("single"));
    }

    @Test
    void readerRejectsNonArrayRoot() {
      // Arrange
      JsonService sut = new JsonService();
      StringReader reader = new StringReader("{\"kind\":\"ScopeVersionMapping\"}");

      // Act and assert
      assertThatThrownBy(() -> sut.importFromReader(reader))
          .isInstanceOf(IOException.class)
          .hasMessage("Expected JSON array at root level");
    }

    @Test
    void readerPropagatesElementConversionFailure() {
      // Arrange
      JsonService sut = new JsonService();
      StringReader reader = new StringReader("[[]]");

      // Act and assert
      assertThatThrownBy(() -> sut.importFromReader(reader))
          .isInstanceOf(MismatchedInputException.class)
          .hasMessageContaining("expected VALUE_STRING");
    }
  }

  @Nested
  class ImportFromFileTests {
    @Test
    void fileImportsAllEntriesUsingRegularFilePath() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      File file = resourceFile("models.json");

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromFile(file);

      // Assert
      assertThat(result.get(V1ScopeVersionMapping.class)).hasSize(2);
      assertThat(result.get(V1ScopeReleaseNote.class)).hasSize(1);
    }

    @Test
    void pathOverloadImportsFile() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      Path path = resourceFile("models.json").toPath();

      // Act
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> result =
          sut.importFromFile(path);

      // Assert
      assertThat(result).containsOnlyKeys(V1ScopeVersionMapping.class, V1ScopeReleaseNote.class);
    }

    @Test
    void reportedLargeFileUsesMemoryMappedPathAndMergesExistingGroups() throws IOException {
      // Arrange
      JsonService sut = new JsonService();
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> seed =
          sut.importFromString(SINGLE_MAPPING);
      Map<Class<? extends ModelElement>, List<ModelElement>> destination = new HashMap<>();
      destination.put(
          V1ScopeVersionMapping.class, new ArrayList<>(seed.get(V1ScopeVersionMapping.class)));
      File file = new ParsingServiceTestSupport.ReportedLargeFile(resourceFile("models.json"));

      // Act
      sut.importFromFile(file, destination);

      // Assert
      assertThat(destination.get(V1ScopeVersionMapping.class))
          .extracting(element -> element.getMetadata().getName())
          .containsExactly("single", "duplicate", "duplicate");
      assertThat(destination.get(V1ScopeReleaseNote.class))
          .singleElement()
          .satisfies(element -> assertThat(element.getMetadata().getName()).isEqualTo("release"));
    }

    @Test
    void nullFileIsRejected() {
      // Arrange
      JsonService sut = new JsonService();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromFile((File) null))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: null");
    }

    @Test
    void nonexistentFileIsRejectedWithPath() {
      // Arrange
      JsonService sut = new JsonService();
      File file = Path.of("target", "parsing-service-tests", "absent.json").toFile();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromFile(file))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: " + file.getAbsolutePath());
    }

    @Test
    void directoryIsRejectedWithPath() {
      // Arrange
      JsonService sut = new JsonService();
      File directory = Path.of("target").toFile();

      // Act and assert
      assertThatThrownBy(() -> sut.importFromFile(directory))
          .isInstanceOf(IOException.class)
          .hasMessage("Invalid file: " + directory.getAbsolutePath());
    }
  }

  @Nested
  class MapperAndDumpTests {
    @Test
    void mapperExposesJsonAndSharedDeserializationConfiguration() {
      // Arrange
      JsonService sut = new JsonService();

      // Act
      ObjectMapper mapper = sut.getMapper();

      // Assert
      assertThat(mapper.getFactory()).isInstanceOf(JsonFactory.class);
      assertThat(mapper.getFactory()).isNotInstanceOf(YAMLFactory.class);
      assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
      assertThat(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)).isTrue();
      assertThat(mapper.coercionConfigDefaults().findAction(CoercionInputShape.EmptyString))
          .isEqualTo(CoercionAction.AsEmpty);
      assertThat(mapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
    }

    @Test
    void dumpCreatesParentAndTruncatesExistingContent() throws Exception {
      // Arrange
      JsonService sut = new JsonService();
      Path output = outputPath("jsonDump", "result.json");
      deleteOutput(output);

      try {
        // Act
        sut.dump("{\"old\":true}", output);
        sut.dump("{\"new\":true}", output);

        // Assert
        assertThat(Files.readString(output, StandardCharsets.UTF_8)).isEqualTo("{\"new\":true}");
      } finally {
        deleteOutput(output);
      }
    }

    @Test
    void dumpRejectsNullContent() {
      // Arrange
      JsonService sut = new JsonService();
      Path output = outputPath("jsonNullDump", "result.json");

      // Act and assert
      assertThatThrownBy(() -> sut.dump(null, output))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Content cannot be null");
    }
  }

  static Stream<Arguments> unsupportedElements() {
    return Stream.of(
        arguments("[{}]", InvalidTypeIdException.class, "missing type id property 'kind'"),
        arguments(
            "[{\"kind\":null}]", InvalidTypeIdException.class, "missing type id property 'kind'"),
        arguments(
            "[{\"kind\":\"UnknownKind\"}]",
            InvalidDefinitionException.class,
            "Cannot construct instance of"));
  }

  static Stream<Arguments> malformedJson() {
    return Stream.of(
        arguments("[", "Unexpected end-of-input"),
        arguments("[/* comments are not enabled */]", "Unexpected character ('/'"));
  }

  private JsonServiceTest() {}
}
