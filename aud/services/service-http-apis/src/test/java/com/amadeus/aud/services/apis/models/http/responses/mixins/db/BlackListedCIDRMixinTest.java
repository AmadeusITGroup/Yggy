package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BlackListedCIDR mixin – JSON serialization")
class BlackListedCIDRMixinTest {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().addMixIn(BlackListedCIDR.class, BlackListedCIDRMixin.class);

  @Test
  @DisplayName("Serialized JSON contains all expected field keys")
  void serializedJsonContainsExpectedKeys() throws Exception {
    BlackListedCIDR entity =
        blackListedCidr(
            "block-internal-range",
            "Blocked IP range",
            "10.0.0.0/8",
            List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP),
            List.of("22"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder(
            "id", "name", "type", "scope", "comment", "cidr", "transportLayer", "ports");
  }

  @Test
  @DisplayName("Field order follows @JsonPropertyOrder declaration")
  void fieldOrderFollowsPropertyOrderDeclaration() throws Exception {
    BlackListedCIDR entity =
        blackListedCidr(
            "block-subnet",
            null,
            "192.168.100.0/24",
            List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP),
            List.of("443"));

    String json = MAPPER.writeValueAsString(entity);
    JsonNode node = MAPPER.readTree(json);

    List<String> actualOrder = new ArrayList<>();
    node.fieldNames().forEachRemaining(actualOrder::add);

    // id, name, type, scope must appear before the type-specific fields cidr/transportLayer/ports
    assertThat(actualOrder.indexOf("id")).isLessThan(actualOrder.indexOf("cidr"));
    assertThat(actualOrder.indexOf("name")).isLessThan(actualOrder.indexOf("cidr"));
    assertThat(actualOrder.indexOf("type")).isLessThan(actualOrder.indexOf("cidr"));
    assertThat(actualOrder.indexOf("cidr")).isLessThan(actualOrder.indexOf("transportLayer"));
    assertThat(actualOrder.indexOf("transportLayer")).isLessThan(actualOrder.indexOf("ports"));
  }

  @Test
  @DisplayName("Empty or null fields are omitted from JSON (@JsonInclude NON_EMPTY)")
  void emptyAndNullFieldsAreOmitted() throws Exception {
    BlackListedCIDR entity =
        blackListedCidr(
            "block-cidr",
            null,
            "172.16.0.0/12",
            List.of(V1BlackListedCIDRSpec.TransportLayerEnum.ICMP),
            List.of("*"));

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
    BlackListedCIDR entity =
        blackListedCidr(
            "block-wide-cidr",
            null,
            "10.4.0.0/16",
            List.of(
                V1BlackListedCIDRSpec.TransportLayerEnum.TCP,
                V1BlackListedCIDRSpec.TransportLayerEnum.UDP),
            List.of("80", "443", "8080"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.get("transportLayer").isArray()).isTrue();
    assertThat(node.get("transportLayer")).hasSize(2);
    assertThat(node.get("ports").isArray()).isTrue();
    assertThat(node.get("ports")).hasSize(3);
  }

  private static BlackListedCIDR blackListedCidr(
      String name,
      String comment,
      String cidr,
      List<V1BlackListedCIDRSpec.TransportLayerEnum> transportLayers,
      List<String> ports) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope("SECURITY_OFFICE");
    metadata.setComment(comment);

    V1BlackListedCIDRSpec spec = new V1BlackListedCIDRSpec();
    spec.setCidr(cidr);
    spec.setTransportLayer(transportLayers);
    spec.setPorts(ports);

    V1BlackListedCIDR model = new V1BlackListedCIDR();
    model.setApiVersion("v1");
    model.setKind("BlackListedCIDR");
    model.setMetadata(metadata);
    model.setSpec(spec);

    BlackListedCIDR entity = new BlackListedCIDR(model);
    Scope scope = new Scope();
    scope.setName("SECURITY_OFFICE");
    entity.setScope(scope);
    return entity;
  }
}
