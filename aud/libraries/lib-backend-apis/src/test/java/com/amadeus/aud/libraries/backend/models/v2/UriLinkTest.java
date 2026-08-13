package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class UriLinkTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void allFieldShouldDeserializeFull() throws Exception {
    Link link = new UriLink("Hello", "file://this.txt", Display.HREF);

    String output = mapper.writeValueAsString(link);

    assertThat(output)
        .isEqualTo(
            """
         {"title":"Hello","uri":"file://this.txt","display":"href","type":"uri"}""");
  }
}
