package com.amadeus.aud.libraries.checker.reports;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.models.Details;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Transitional;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonReporterTest {

  @Test
  void noMessages() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    List<Message> data = java.util.Collections.emptyList();

    try (Reporter sut = new JsonReporter(outputStream)) {
      sut.addMessages(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream.toString(StandardCharsets.UTF_8))
        .isEqualToIgnoringWhitespace(
            """
                    {
                      "title" : "AUD Report",
                      "messages" : [ ]
                    }""");
  }

  @Test
  void nominal() {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    List<Message> data = new ArrayList<>();
    Message messageWarning =
        Message.warning(
            "MSG001",
            "This is a warning message",
            new Details("DOM", "Component", "Namy"),
            new Transitional(LocalDate.of(2020, 1, 4)),
            List.of(
                URI.create("file://hello/file.yaml:85"),
                URI.create("https://viewer.example.com/api/_def/components/schemas/")));
    data.add(messageWarning);
    Message messageError =
        Message.error(
            "MSG001",
            "This is an error message",
            new Details("DOM", "Component", "Name error"),
            null,
            List.of());
    data.add(messageError);

    try (Reporter sut = new JsonReporter(outputStream)) {
      sut.addMessages(data);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    assertThat(outputStream.toString(StandardCharsets.UTF_8))
        .isEqualToIgnoringWhitespace(
            """
                   {
                     "title" : "AUD Report",
                     "messages" : [ {
                       "code" : "MSG001",
                       "level" : "WARNING",
                       "message" : "This is a warning message",
                       "details" : {
                         "objectDomain" : "DOM",
                         "objectType" : "Component",
                         "objectName" : "Namy"
                       },
                       "transitional" : {
                         "limitDate" : [ 2020, 1, 4 ]
                       },
                       "links" : [ "file://hello/file.yaml:85", "https://viewer.example.com/api/_def/components/schemas/" ]
                     }, {
                       "code" : "MSG001",
                       "level" : "ERROR",
                       "message" : "This is an error message",
                       "details" : {
                         "objectDomain" : "DOM",
                         "objectType" : "Component",
                         "objectName" : "Name error"
                       },
                       "links" : [ ]
                     } ]
                   }""");
  }
}
