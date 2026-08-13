package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JsonPathPatternLinkTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void allFieldsButTileShouldDeserialize() throws Exception {
    Link link = new JsonPathPatternLink("$.a.b", "{value}", Display.HREF);

    String output = mapper.writeValueAsString(link);

    assertThat(output)
        .isEqualTo(
            """
          {"title":null,"path":"$.a.b","replacement":"{value}","display":"href","type":"jsonpathpattern"}""");
  }

  @Test
  void allFieldShouldDeserializeFull() throws Exception {
    Link link = new JsonPathPatternLink("Hello", "$.a[*]", "{value}", Display.HREF);

    String output = mapper.writeValueAsString(link);

    assertThat(output)
        .isEqualTo(
            """
          {"title":"Hello","path":"$.a[*]","replacement":"{value}","display":"href","type":"jsonpathpattern"}""");
  }
}
