package com.amadeus.aud.services.json.schema.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.json.schema.services.V1JsonSchemaGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class YamlSchemaResourceTest {
  private final YamlSchemaResource sut =
      new YamlSchemaResource(
          new V1JsonSchemaGenerator(new ObjectMapper().findAndRegisterModules()));

  @Test
  void returnsStandaloneRootSchema() {
    JsonNode schema = sut.getSchema();

    assertThat(schema.path("$schema").asText())
        .isEqualTo("https://json-schema.org/draft/2020-12/schema");
    assertThat(schema.has("oneOf")).isTrue();
    assertThat(schema.has("components")).isFalse();
  }
}
