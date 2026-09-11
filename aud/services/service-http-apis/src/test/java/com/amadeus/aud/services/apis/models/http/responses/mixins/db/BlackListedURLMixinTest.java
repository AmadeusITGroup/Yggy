package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BlackListedURL mixin – JSON serialization")
class BlackListedURLMixinTest {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().addMixIn(BlackListedURL.class, BlackListedURLMixin.class);

  @Test
  @DisplayName("Serialized JSON contains all expected field keys")
  void serializedJsonContainsExpectedKeys() throws Exception {
    BlackListedURL entity =
        blackListedUrl(
            "block-example-com",
            "Blocked DNS entry",
            "example.com",
            List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP),
            List.of("443"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder(
            "id", "name", "type", "scope", "comment", "fqdn", "transportLayer", "ports");
  }

  @Test
  @DisplayName("Field order follows @JsonPropertyOrder declaration")
  void fieldOrderFollowsPropertyOrderDeclaration() throws Exception {
    BlackListedURL entity =
        blackListedUrl(
            "block-malware",
            null,
            "malware.example.net",
            List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP),
            List.of("80", "443"));

    String json = MAPPER.writeValueAsString(entity);
    JsonNode node = MAPPER.readTree(json);

    List<String> actualOrder = new ArrayList<>();
    node.fieldNames().forEachRemaining(actualOrder::add);

    // id, name, type, scope must appear before the type-specific fields fqdn/transportLayer/ports
    assertThat(actualOrder.indexOf("id")).isLessThan(actualOrder.indexOf("fqdn"));
    assertThat(actualOrder.indexOf("name")).isLessThan(actualOrder.indexOf("fqdn"));
    assertThat(actualOrder.indexOf("type")).isLessThan(actualOrder.indexOf("fqdn"));
    assertThat(actualOrder.indexOf("fqdn")).isLessThan(actualOrder.indexOf("transportLayer"));
    assertThat(actualOrder.indexOf("transportLayer")).isLessThan(actualOrder.indexOf("ports"));
  }

  @Test
  @DisplayName("Empty or null fields are omitted from JSON (@JsonInclude NON_EMPTY)")
  void emptyAndNullFieldsAreOmitted() throws Exception {
    BlackListedURL entity =
        blackListedUrl(
            "block-example",
            null,
            "example.com",
            List.of(V1BlackListedURLSpec.TransportLayerEnum.UDP),
            List.of("53"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.has("comment")).as("null 'comment' should be absent").isFalse();
    assertThat(node.has("tags")).as("null 'tags' should be absent").isFalse();
    assertThat(node.has("provider_specific"))
        .as("null 'provider_specific' should be absent")
        .isFalse();
  }

  @Test
  @DisplayName("Multiple transport layers and ports are serialized as arrays")
  void multipleTransportLayersAndPortsAreSerializedAsArrays() throws Exception {
    BlackListedURL entity =
        blackListedUrl(
            "block-wide",
            null,
            "broad.example.com",
            List.of(
                V1BlackListedURLSpec.TransportLayerEnum.TCP,
                V1BlackListedURLSpec.TransportLayerEnum.UDP),
            List.of("80", "443", "8080"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.get("transportLayer").isArray()).isTrue();
    assertThat(node.get("transportLayer")).hasSize(2);
    assertThat(node.get("ports").isArray()).isTrue();
    assertThat(node.get("ports")).hasSize(3);
  }

  private static BlackListedURL blackListedUrl(
      String name,
      String comment,
      String fqdn,
      List<V1BlackListedURLSpec.TransportLayerEnum> transportLayers,
      List<String> ports) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope("SECURITY_OFFICE");
    metadata.setComment(comment);

    V1BlackListedURLSpec spec = new V1BlackListedURLSpec();
    spec.setFqdn(fqdn);
    spec.setTransportLayer(transportLayers);
    spec.setPorts(ports);

    V1BlackListedURL model = new V1BlackListedURL();
    model.setApiVersion("v1");
    model.setKind("BlackListedURL");
    model.setMetadata(metadata);
    model.setSpec(spec);

    BlackListedURL entity = new BlackListedURL(model);
    Scope scope = new Scope();
    scope.setName("SECURITY_OFFICE");
    entity.setScope(scope);
    return entity;
  }
}
