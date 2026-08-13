package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ComponentInstance mixin – cidr_ranges serialization")
class ComponentInstanceMixinTest {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().addMixIn(ComponentInstance.class, ComponentInstanceMixin.class);

  @Test
  @DisplayName("cidr_ranges is segregated by direction (in / out)")
  void cidrRangesSegregatedByDirection() throws Exception {
    ComponentInstance ci = mock(ComponentInstance.class);

    Map<String, List<ComponentInstance.CidrRange>> ranges = new LinkedHashMap<>();
    ranges.put(
        "in", new ArrayList<>(List.of(new ComponentInstance.CidrRange("10.0.0.0/24", "subnet-a"))));
    ranges.put(
        "out", new ArrayList<>(List.of(new ComponentInstance.CidrRange("10.0.1.0/32", null))));
    when(ci.getCidrRanges()).thenReturn(ranges);

    JsonNode node = MAPPER.valueToTree(ci);

    assertThat(node.has("cidr_ranges")).isTrue();
    JsonNode cidrRanges = node.get("cidr_ranges");
    assertThat(cidrRanges.isObject()).isTrue();
    assertThat(cidrRanges.fieldNames()).toIterable().containsExactlyInAnyOrder("in", "out");

    assertThat(cidrRanges.get("in").get(0).get("value").asText()).isEqualTo("10.0.0.0/24");
    assertThat(cidrRanges.get("out").get(0).get("value").asText()).isEqualTo("10.0.1.0/32");
  }

  @Test
  @DisplayName("empty buckets are omitted from cidr_ranges")
  void emptyBucketsAreOmitted() throws Exception {
    ComponentInstance ci = mock(ComponentInstance.class);

    when(ci.getCidrRanges()).thenReturn(new LinkedHashMap<>());

    JsonNode node = MAPPER.valueToTree(ci);

    assertThat(node.has("cidr_ranges")).isFalse();
  }

  @Test
  @DisplayName("typed CIDR entries expose subnet names")
  void typedCidrEntriesExposeSubnetNames() throws Exception {
    ComponentInstance ci = mock(ComponentInstance.class);
    Map<String, List<ComponentInstance.CidrRange>> ranges = new LinkedHashMap<>();
    ranges.put("in", List.of(new ComponentInstance.CidrRange("10.0.0.0/24", "subnet-a")));
    when(ci.getCidrRanges()).thenReturn(ranges);

    JsonNode node = MAPPER.valueToTree(ci);

    assertThat(node.get("cidr_ranges").get("in").get(0).get("name").asText()).isEqualTo("subnet-a");
  }
}
