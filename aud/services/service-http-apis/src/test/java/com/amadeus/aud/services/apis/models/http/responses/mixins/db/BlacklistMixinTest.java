package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import com.amadeus.aud.libraries.yaml.models.V1BlacklistSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Blacklist mixin - JSON serialization")
class BlacklistMixinTest {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().addMixIn(Blacklist.class, BlacklistMixin.class);

  @Test
  @DisplayName("Serialized JSON contains plural destination host keys")
  void serializedJsonContainsPluralDestinationHostKeys() {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("my-app");
    spec.setDestinationHostFqdns(List.of("*.google.com"));
    spec.setDestinationHostCidrs(List.of("10.0.0.0/8"));
    spec.setSourceZones(List.of("CSZ"));
    spec.setSourceMacrophases(List.of("PRD"));
    spec.setSourceTargets(List.of("prd-we"));
    spec.setDestinationApplicationProtocols(
        List.of(V1BlacklistSpec.DestinationApplicationProtocolsEnum.HTTPS));
    spec.setDestinationTransportProtocols(
        List.of(V1BlacklistSpec.DestinationTransportProtocolsEnum.TCP));
    spec.setDestinationPortRanges(List.of("443"));
    Blacklist entity = blacklist("block-google", "Block destinations", spec);

    JsonNode node = MAPPER.valueToTree(entity);

    assertThat(node.fieldNames())
        .toIterable()
        .contains("destination_host_fqdns", "destination_host_cidrs")
        .doesNotContain("destination_host_fqdn", "destination_host_cidr");
    assertThat(node.get("destination_host_fqdns").isArray()).isTrue();
    assertThat(node.get("destination_host_cidrs").isArray()).isTrue();
  }

  @Test
  @DisplayName("Field order keeps destination host arrays before source zones")
  void fieldOrderFollowsPropertyOrderDeclaration() throws Exception {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("my-app");
    spec.setDestinationHostFqdns(List.of("example.com"));
    spec.setDestinationHostCidrs(List.of("172.16.0.0/12"));
    spec.setSourceZones(List.of("CSZ"));
    spec.setSourceMacrophases(List.of("TST"));
    spec.setSourceTargets(List.of("prd-we"));
    spec.setDestinationTransportProtocols(
        List.of(V1BlacklistSpec.DestinationTransportProtocolsEnum.TCP));
    spec.setDestinationPortRanges(List.of("443"));
    Blacklist entity = blacklist("block-order", null, spec);

    String json = MAPPER.writeValueAsString(entity);
    JsonNode node = MAPPER.readTree(json);

    List<String> actualOrder = new ArrayList<>();
    node.fieldNames().forEachRemaining(actualOrder::add);

    assertThat(actualOrder.indexOf("destination_host_fqdns"))
        .isLessThan(actualOrder.indexOf("source_zones"));
    assertThat(actualOrder.indexOf("destination_host_cidrs"))
        .isLessThan(actualOrder.indexOf("source_zones"));
  }

  private static Blacklist blacklist(String name, String comment, V1BlacklistSpec spec) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope("SECURITY_OFFICE");
    metadata.setComment(comment);

    V1Blacklist model = new V1Blacklist();
    model.setApiVersion("v1");
    model.setKind("Blacklist");
    model.setMetadata(metadata);
    model.setSpec(spec);

    Blacklist entity = new Blacklist(model);
    Scope scope = new Scope();
    scope.setName("SECURITY_OFFICE");
    entity.setScope(scope);
    return entity;
  }
}
