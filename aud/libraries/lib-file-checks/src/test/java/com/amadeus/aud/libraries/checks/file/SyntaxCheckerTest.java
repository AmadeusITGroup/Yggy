package com.amadeus.aud.libraries.checks.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.models.Level;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.FileCheck;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SyntaxCheckerTest {
  private final FileCheck sut = new SyntaxChecker();

  private static void checkMessageDetails(Message m, String details) {
    assertThat(m.code()).hasToString("SYN00001");
    assertThat(m.level()).isEqualTo(Level.ERROR);
    assertThat(m.message().toString()).hasToString(details);
    assertThat(m.links())
        .singleElement()
        .satisfies(l -> assertThat(l.toString()).startsWith("file:/"));
  }

  @Test
  void FileNotFoundShouldBeDisregarded() {
    Collection<Message> messages =
        sut.check("domain", Path.of("src/test/resources/syntax-checker-not-found"));

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    """
        Error finding yaml files:
        src/test/resources/syntax-checker-not-found"""));
  }

  @Test
  void validYamlWithDocumentStartMarkerProducesNoMessages(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("ok.yaml"), "---\nfoo: bar\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages).isEmpty();
  }

  @Test
  void yamlMissingDocumentStartMarkerProducesOneError(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("no-marker.yaml"), "foo: bar\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    "Yaml file must start with a document-start marker '---' on its first non-comment line."));
  }

  @Test
  void yamlWithNonAsciiByteProducesOneErrorPointingAtTheRightLine(@TempDir Path dir)
      throws Exception {
    Files.writeString(
        dir.resolve("non-ascii.yaml"), "---\nfoo: bar\nname: bär\n", StandardCharsets.UTF_8);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    """
          Yaml file contains non-ASCII byte 0xC3 at line 3, column 8.
          Change that character."""));
  }

  @Test
  void yamlWithMalformedSyntaxProducesParseError(@TempDir Path dir) throws Exception {
    Files.writeString(
        dir.resolve("broken.yaml"), "---\nfoo: [unbalanced\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            (Message m) -> {
              assertThat(m.code()).hasToString("SYN00001");
              assertThat(m.level()).isEqualTo(Level.ERROR);
              assertThat(m.message().toString())
                  .startsWith(
                      """
          Error parsing Yaml file:
          while parsing a flow sequence
           in 'reader', line 2, column 6:""");
            });
  }

  @Test
  void nonYamlFilesAreIgnored(@TempDir Path dir) throws Exception {
    Files.writeString(
        dir.resolve("notes.txt"), "this is not yaml and lacks marker\n", StandardCharsets.US_ASCII);
    Files.writeString(dir.resolve("README"), "still not yaml\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages).isEmpty();
  }

  @Test
  void bothYmlAndYamlExtensionsAreChecked(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("a.yml"), "foo: bar\n", StandardCharsets.US_ASCII);
    Files.writeString(dir.resolve("b.yaml"), "foo: bar\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages).hasSize(2);
  }

  @Test
  void emptyYamlFileIsAccepted(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("empty.yaml"), "", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages).isEmpty();
  }

  @Test
  void commentsAndBlankLinesBeforeMarkerAreAccepted(@TempDir Path dir) throws Exception {
    Files.writeString(
        dir.resolve("commented.yaml"),
        "# header comment\n\n   \n---\nfoo: bar\n",
        StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages).isEmpty();
  }

  @Test
  void onlyBrokenFilesInAMixedDirectoryProduceMessages(@TempDir Path dir) throws Exception {
    Files.writeString(dir.resolve("good.yaml"), "---\nfoo: bar\n", StandardCharsets.US_ASCII);
    Files.writeString(dir.resolve("bad.yaml"), "foo: bar\n", StandardCharsets.US_ASCII);
    Files.writeString(dir.resolve("readme.txt"), "ignored\n", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    "Yaml file must start with a document-start marker '---' on its first non-comment line."));
  }

  @Test
  void duplicateKeysAreRejected(@TempDir Path dir) throws Exception {
    Files.writeString(
        dir.resolve("duplicate_keys.yaml"), "---\nfoo: bar\nfoo: bar", StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    """
              Error reading Yaml file:
              Duplicate field 'foo' for `ObjectNode`: not allowed when `DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY` enabled
               at [Source: (StringReader); line: 3, column: 6]"""));
  }

  @Nested
  class NonAsciiPositionReporting {
    @Test
    void reportsExactLineWhenNonAsciiIsOnFirstSignificantLine(@TempDir Path dir) throws Exception {
      Files.writeString(dir.resolve("first-line.yaml"), "---\nname: bär\n", StandardCharsets.UTF_8);

      Collection<Message> messages = sut.check("domain", dir);

      assertThat(messages)
          .singleElement()
          .satisfies(
              m ->
                  checkMessageDetails(
                      m,
                      """
            Yaml file contains non-ASCII byte 0xC3 at line 2, column 8.
            Change that character."""));
    }
  }

  @Test
  void malformedDocumentLeadingToNullShouldBeAnError(@TempDir Path dir) throws Exception {
    Files.writeString(
        dir.resolve("malformed_quoted_document.yaml"),
        """
    # hello
    ---
    api_version: v1
    kind: Namespace
    metadata:
      comment: 'Namespace for test
      name: my-namespace
    ## a comment

    ---
    api_version: v1
    kind: Namespace
    metadata:
      comment: 'Namespace for test again'
      name: my-namespace-again
    """,
        StandardCharsets.US_ASCII);

    Collection<Message> messages = sut.check("domain", dir);

    assertThat(messages)
        .singleElement()
        .satisfies(
            m ->
                checkMessageDetails(
                    m,
                    """
              Error parsing Yaml file:
              while scanning a quoted scalar
               in 'reader', line 6, column 12:
                    comment: 'Namespace for test
                             ^
              found unexpected document separator
               in 'reader', line 10, column 1:
                  ---
                  ^

               at [Source: (StringReader); line: 6, column: 10]"""));
  }
}
