package com.amadeus.aud.services.apis.v2.endpoints;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.backend.models.v2.ReferenceEntry;
import com.amadeus.aud.libraries.backend.models.v2.ReferencesResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("References response – JSON format")
class ReferencesResponseFormatTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  @DisplayName("ReferenceEntry serializes to the expected JSON keys")
  void referenceEntryHasExpectedJsonKeys() throws Exception {
    ReferenceEntry entry = new ReferenceEntry("ns-1", "my-ns", "1A4DEV", "A namespace");

    JsonNode node = MAPPER.valueToTree(entry);

    assertThat(node.fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder("id", "scope", "name", "description");
  }

  @Test
  @DisplayName("ReferenceEntry null fields are omitted from JSON (@JsonInclude NON_NULL)")
  void referenceEntryNullFieldsAreOmitted() throws Exception {
    ReferenceEntry entry = new ReferenceEntry("comp-1", "AKM_PPT", "1A4DEV", null);

    JsonNode node = MAPPER.valueToTree(entry);

    boolean hasDescription = node.has("description");
    assertThat(hasDescription)
        .as("'description' should be absent when null due to @JsonInclude(NON_NULL)")
        .isFalse();
    assertThat(node.get("scope").asText()).isEqualTo("1A4DEV");
    assertThat(node.get("name").asText()).isEqualTo("AKM_PPT");
  }

  @Test
  @DisplayName("ReferenceEntry values round-trip through JSON")
  void referenceEntryRoundTrip() throws Exception {
    ReferenceEntry original = new ReferenceEntry("svc-1", "my-svc", "ADDS", "A service");

    String json = MAPPER.writeValueAsString(original);
    ReferenceEntry restored = MAPPER.readValue(json, ReferenceEntry.class);

    assertThat(restored.id()).isEqualTo(original.id());
    assertThat(restored.scope()).isEqualTo(original.scope());
    assertThat(restored.name()).isEqualTo(original.name());
    assertThat(restored.description()).isEqualTo(original.description());
  }

  @Test
  @DisplayName("ReferenceEntry can carry a fromParent value")
  void referenceEntryWithFromParentSerializesExtraFields() throws Exception {
    ReferenceEntry entry =
        new ReferenceEntry("svc-1", "my-svc", "ADDS", "A service", null, "MyParent");

    JsonNode node = MAPPER.valueToTree(entry);

    assertThat(node.get("fromParent").asText()).isEqualTo("MyParent");
  }

  @Test
  @DisplayName("ReferenceEntry id is stable")
  void referenceEntryid() {
    ReferenceEntry entry = new ReferenceEntry("ns-1", "my-ns", "1A4DEV", "desc");

    assertThat(entry.id()).isEqualTo("ns-1");
  }

  @Test
  @DisplayName("ReferencesResponse serializes with 'direct' and 'inherited' top-level keys")
  void referencesResponseHasExpectedTopLevelKeys() throws Exception {
    ReferencesResponse response = new ReferencesResponse(Map.of(), Map.of());

    JsonNode node = MAPPER.valueToTree(response);

    assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder("direct", "inherited");
  }

  @Test
  @DisplayName("ReferencesResponse 'direct' and 'inherited' are JSON objects keyed by type")
  void referencesResponseFieldsAreObjects() throws Exception {
    ReferenceEntry e1 = new ReferenceEntry("ns-1", "ns1", "1A4DEV", null);
    ReferenceEntry e2 = new ReferenceEntry("dep-1", "dep1", "ADDS", "desc");
    ReferencesResponse response =
        new ReferencesResponse(
            Map.of("namespaces", List.of(e1)), Map.of("dependencies", List.of(e2)));

    JsonNode node = MAPPER.valueToTree(response);

    assertThat(node.get("direct").isObject()).isTrue();
    assertThat(node.get("inherited").isObject()).isTrue();
    assertThat(node.get("direct").get("namespaces").isArray()).isTrue();
    assertThat(node.get("inherited").get("dependencies").isArray()).isTrue();
    assertThat(node.get("direct").get("namespaces")).hasSize(1);
    assertThat(node.get("inherited").get("dependencies")).hasSize(1);
  }

  @Test
  @DisplayName("ReferencesResponse preserves property order: direct before inherited")
  void referencesResponsePropertyOrder() throws Exception {
    ReferencesResponse response = new ReferencesResponse(Map.of(), Map.of());

    String json = MAPPER.writeValueAsString(response);
    int directIndex = json.indexOf("\"direct\"");
    int inheritedIndex = json.indexOf("\"inherited\"");

    assertThat(directIndex < inheritedIndex)
        .as("'direct' must appear before 'inherited' in JSON output")
        .isTrue();
  }

  @Test
  @DisplayName("ReferencesResponse round-trip preserves all entries")
  void referencesResponseRoundTrip() throws Exception {
    ReferenceEntry direct = new ReferenceEntry("ci-1", "ci-1", "1A4DEV", null);
    ReferenceEntry inherited = new ReferenceEntry("dep-1", "dep-1", "ADDS", "A dep");
    ReferencesResponse original =
        new ReferencesResponse(
            Map.of("componentinstances", List.of(direct)),
            Map.of("dependencies", List.of(inherited)));

    String json = MAPPER.writeValueAsString(original);
    ReferencesResponse restored = MAPPER.readValue(json, ReferencesResponse.class);

    assertThat(restored.direct()).containsKey("componentinstances");
    assertThat(restored.direct().get("componentinstances")).hasSize(1);
    assertThat(restored.direct().get("componentinstances").get(0).id()).isEqualTo("ci-1");

    assertThat(restored.inherited()).containsKey("dependencies");
    assertThat(restored.inherited().get("dependencies")).hasSize(1);
    assertThat(restored.inherited().get("dependencies").get(0).name()).isEqualTo("dep-1");
  }
}
