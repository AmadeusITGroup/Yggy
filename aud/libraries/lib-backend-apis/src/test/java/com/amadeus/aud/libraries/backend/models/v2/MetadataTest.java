package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetadataTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void allNullShouldDeserializeAsEmpty() throws Exception {
    Metadata metadata = new Metadata();

    String output = mapper.writeValueAsString(metadata);

    assertThat(output).isEqualTo("{}");
  }

  @Test
  void allFieldShouldDeserializeFull() throws Exception {
    Pagination pagination = new Pagination(10, 1, 5);
    Metadata metadata =
        new Metadata()
            .withInitialColumns(List.of("a", "b"))
            .withPagination(pagination)
            .withLinks(List.of());

    String output = mapper.writeValueAsString(metadata);

    assertThat(output)
        .isEqualTo(
            "{\"initialColumns\":[\"a\",\"b\"],\"pagination\":{\"count\":10,\"pageNum\":1,\"pageSize\":5},\"links\":[]}");
  }
}
