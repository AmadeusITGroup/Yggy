package com.amadeus.aud.libraries.checker.reports;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.models.Details;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Transitional;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarkdownReporterTest {

  @Test
  @DisplayName("No message should create an empty report")
  void noMessageNoReport() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 0)
        [comment]: <> (- WARNING: 0)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             0|        0|     0|

        """);
  }

  @Test
  @DisplayName(
      "Single warning message without transitional or details should be formatted correctly")
  void singleWarningNoDetailsNoTransitional() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message message = Message.warning("MSG001", "This is a warning message", List.of());

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(message));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 0)
        [comment]: <> (- WARNING: 1)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             0|        1|     0|

        ## ⚠️ WARNING MSG001

        ```text
        This is a warning message
        ```

        """);
  }

  @Test
  @DisplayName(
      "Single warning message without transitional but with details should be formatted correctly")
  void singleWarningNoTransitional() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message message =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Namy"),
            null,
            List.of());

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(message));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 0)
        [comment]: <> (- WARNING: 1)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             0|        1|     0|

        ## ⚠️ WARNING MSG001

        ```text
        In DOM, on Component/Namy:
        This is a warning message
        ```

        """);
  }

  @Test
  @DisplayName("Single warning message with all data should be formatted correctly")
  void singleWarningFull() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message message =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Namy"),
            new Transitional(LocalDate.of(2020, 1, 4)),
            List.of(
                URI.create("file://hello/file.yaml:85"),
                URI.create("https://viewer.example.com/api/_def/components/schemas/")));

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(message));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 1)
        [comment]: <> (- WARNING: 0)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             1|        0|     0|

        ## ⚠️ WARNING MSG001 **‼️** BLOCKING after 2020-01-04

        ```text
        In DOM, on Component/Namy:
        This is a warning message
        ```

        ### Links

        - file://hello/file.yaml:85
        - https://viewer.example.com/api/_def/components/schemas/

        """);
  }

  @Test
  @DisplayName(
      "Single warning message with all data should be formatted correctly even on same day of the transition")
  void singleWarningFullSameDay() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message message =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Namy"),
            new Transitional(LocalDate.of(2010, 1, 4)),
            List.of(
                URI.create("file://hello/file.yaml:85"),
                URI.create("https://viewer.example.com/api/_def/components/schemas/")));

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(message));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 1)
        [comment]: <> (- WARNING: 0)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             1|        0|     0|

        ## ⚠️ WARNING MSG001 **‼️** BLOCKING after 2010-01-04

        ```text
        In DOM, on Component/Namy:
        This is a warning message
        ```

        ### Links

        - file://hello/file.yaml:85
        - https://viewer.example.com/api/_def/components/schemas/

        """);
  }

  @Test
  @DisplayName(
      "Single warning message with all data except transitional should be formatted correctly")
  void singleWarningWithoutTransitional() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message message =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Namy"),
            null,
            List.of(URI.create("file://hello/file.yaml:85")));

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(message));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 0)
        [comment]: <> (- Future ERROR: 0)
        [comment]: <> (- WARNING: 1)
        [comment]: <> (- INFO: 0)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      0|             0|        1|     0|

        ## ⚠️ WARNING MSG001

        ```text
        In DOM, on Component/Namy:
        This is a warning message
        ```

        ### Links

        - file://hello/file.yaml:85

        """);
  }

  @Test
  @DisplayName(" Multiple messages with different levels should be sorted correctly")
  void multipleMessagesShouldBeSorted() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Message messageContext = Message.context("this is a context message");
    Message messageInfo =
        Message.info(
            "MSG001",
            "This is an info message",
            new Details("DOM", "Component", "Name info"),
            null,
            List.of());
    Message messageError =
        Message.error(
            "MSG001",
            "This is an error message",
            new Details("DOM", "Component", "Name error"),
            null,
            List.of());

    Message messageWarning =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Name warning"),
            null,
            List.of());

    Message messageWarningTransitional =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Name warning with transitional"),
            new Transitional(LocalDate.of(2020, 1, 1)),
            List.of());

    try (Reporter sut = new MarkdownReporter(LocalDate.of(2010, 1, 4), outputStream)) {
      sut.addMessages(List.of(messageInfo, messageError));
      sut.addMessages(List.of(messageContext));
      sut.addMessages(List.of());
      sut.addMessages(List.of(messageWarning, messageWarningTransitional));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream)
        .hasToString(
            """
        # AUD Report

        ## Statistics

        [comment]: <> (- ERROR: 1)
        [comment]: <> (- Future ERROR: 1)
        [comment]: <> (- WARNING: 1)
        [comment]: <> (- INFO: 1)

        | ERROR | Future ERROR | WARNING | INFO |
        |-------|--------------|---------|------|
        |      1|             1|        1|     1|

        ## ‼️ ERROR MSG001

        ```text
        In DOM, on Component/Name error:
        This is an error message
        ```

        ## ⚠️ WARNING MSG001 **‼️** BLOCKING after 2020-01-01

        ```text
        In DOM, on Component/Name warning with transitional:
        This is a warning message
        ```

        ## ⚠️ WARNING MSG001

        ```text
        In DOM, on Component/Name warning:
        This is a warning message
        ```

        ## 🪧 INFO MSG001

        ```text
        In DOM, on Component/Name info:
        This is an info message
        ```

        ## CONTEXT

        ```text
        this is a context message
        ```

        """);
  }
}
