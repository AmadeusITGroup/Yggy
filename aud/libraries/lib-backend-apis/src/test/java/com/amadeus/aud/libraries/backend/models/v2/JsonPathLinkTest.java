package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JsonPathLinkTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void allFieldsButTileShouldDeserialize() throws Exception {
    Link link = new JsonPathLink("file://this.txt", "$.a.b", Display.HREF);

    String output = mapper.writeValueAsString(link);

    assertThat(output)
        .isEqualTo(
            """
          {"title":null,"jsonPath":"file://this.txt","uri":"$.a.b","display":"href","type":"jsonpath"}""");
  }

  @Test
  void allFieldShouldDeserializeFull() throws Exception {
    Link link = new JsonPathLink("Hello", "file://this.txt", "$.a.b", Display.HREF);

    String output = mapper.writeValueAsString(link);

    assertThat(output)
        .isEqualTo(
            """
          {"title":"Hello","jsonPath":"file://this.txt","uri":"$.a.b","display":"href","type":"jsonpath"}""");
  }
}
