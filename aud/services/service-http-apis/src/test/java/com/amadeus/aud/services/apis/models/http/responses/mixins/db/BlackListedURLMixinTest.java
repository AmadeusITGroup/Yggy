package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
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
    BlackListedURL entity = mock(BlackListedURL.class);

    when(entity.id()).thenReturn("block-example-com");
    when(entity.getName()).thenReturn("block-example-com");
    when(entity.getType()).thenReturn("blacklistedurl");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn("Blocked DNS entry");
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getFqdn()).thenReturn("example.com");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP));
    when(entity.getPorts()).thenReturn(List.of("443"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder(
            "id", "name", "type", "scope", "comment", "fqdn", "transportLayer", "ports");
  }

  @Test
  @DisplayName("Field order follows @JsonPropertyOrder declaration")
  void fieldOrderFollowsPropertyOrderDeclaration() throws Exception {
    BlackListedURL entity = mock(BlackListedURL.class);

    when(entity.id()).thenReturn("block-malware");
    when(entity.getName()).thenReturn("block-malware");
    when(entity.getType()).thenReturn("blacklistedurl");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null);
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getFqdn()).thenReturn("malware.example.net");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP));
    when(entity.getPorts()).thenReturn(List.of("80", "443"));

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
    BlackListedURL entity = mock(BlackListedURL.class);

    when(entity.id()).thenReturn("block-example");
    when(entity.getName()).thenReturn("block-example");
    when(entity.getType()).thenReturn("blacklistedurl");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null); // should be absent
    when(entity.getTagsAsMap()).thenReturn(null); // should be absent
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null); // should be absent
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getFqdn()).thenReturn("example.com");
    when(entity.getTransportLayer())
        .thenReturn(List.of(V1BlackListedURLSpec.TransportLayerEnum.UDP));
    when(entity.getPorts()).thenReturn(List.of("53"));

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
    BlackListedURL entity = mock(BlackListedURL.class);

    when(entity.id()).thenReturn("block-wide");
    when(entity.getName()).thenReturn("block-wide");
    when(entity.getType()).thenReturn("blacklistedurl");
    when(entity.getScopeName()).thenReturn("SECURITY_OFFICE");
    when(entity.getComment()).thenReturn(null);
    when(entity.getTagsAsMap()).thenReturn(null);
    when(entity.getTags()).thenReturn(null);
    when(entity.getProviderSpecific()).thenReturn(null);
    when(entity.getSecurityJustification()).thenReturn(null);
    when(entity.getData()).thenReturn(null);
    when(entity.getFqdn()).thenReturn("broad.example.com");
    when(entity.getTransportLayer())
        .thenReturn(
            List.of(
                V1BlackListedURLSpec.TransportLayerEnum.TCP,
                V1BlackListedURLSpec.TransportLayerEnum.UDP));
    when(entity.getPorts()).thenReturn(List.of("80", "443", "8080"));

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.get("transportLayer").isArray()).isTrue();
    assertThat(node.get("transportLayer")).hasSize(2);
    assertThat(node.get("ports").isArray()).isTrue();
    assertThat(node.get("ports")).hasSize(3);
  }
}
