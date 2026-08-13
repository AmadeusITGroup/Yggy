package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
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
    BlackListedCIDR entity = mock(BlackListedCIDR.class);

    when(entity.id()).thenReturn("block-internal-range");
    when(entity.getName()).thenReturn("block-internal-range");
    when(entity.getType()).thenReturn("blacklistedcidr");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn("Blocked IP range");
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getCidr()).thenReturn("10.0.0.0/8");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP));
    when(entity.getPorts()).thenReturn(List.of("22"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder(
            "id", "name", "type", "scope", "comment", "cidr", "transportLayer", "ports");
  }

  @Test
  @DisplayName("Field order follows @JsonPropertyOrder declaration")
  void fieldOrderFollowsPropertyOrderDeclaration() throws Exception {
    BlackListedCIDR entity = mock(BlackListedCIDR.class);

    when(entity.id()).thenReturn("block-subnet");
    when(entity.getName()).thenReturn("block-subnet");
    when(entity.getType()).thenReturn("blacklistedcidr");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null);
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getCidr()).thenReturn("192.168.100.0/24");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP));
    when(entity.getPorts()).thenReturn(List.of("443"));

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
    BlackListedCIDR entity = mock(BlackListedCIDR.class);

    when(entity.id()).thenReturn("block-cidr");
    when(entity.getName()).thenReturn("block-cidr");
    when(entity.getType()).thenReturn("blacklistedcidr");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null); // should be absent
    when(entity.getTagsAsMap()).thenReturn(null); // should be absent
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null); // should be absent
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getCidr()).thenReturn("172.16.0.0/12");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.ICMP));
    when(entity.getPorts()).thenReturn(List.of("*"));

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
    BlackListedCIDR entity = mock(BlackListedCIDR.class);

    when(entity.id()).thenReturn("block-wide-cidr");
    when(entity.getName()).thenReturn("block-wide-cidr");
    when(entity.getType()).thenReturn("blacklistedcidr");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null);
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getCidr()).thenReturn("10.4.0.0/16");
    when(entity.getTransportLayer())
        .thenReturn(
            List.of(
                V1BlackListedCIDRSpec.TransportLayerEnum.TCP,
                V1BlackListedCIDRSpec.TransportLayerEnum.UDP));
    when(entity.getPorts()).thenReturn(List.of("80", "443", "8080"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.get("transportLayer").isArray()).isTrue();
    assertThat(node.get("transportLayer")).hasSize(2);
    assertThat(node.get("ports").isArray()).isTrue();
    assertThat(node.get("ports")).hasSize(3);
  }
}
