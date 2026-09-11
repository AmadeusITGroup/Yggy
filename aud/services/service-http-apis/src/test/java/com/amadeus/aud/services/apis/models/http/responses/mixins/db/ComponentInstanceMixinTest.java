package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstanceSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ComponentInstance mixin – cidr_ranges serialization")
class ComponentInstanceMixinTest {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().addMixIn(ComponentInstance.class, ComponentInstanceMixin.class);

  @Test
  @DisplayName("cidr_ranges is segregated by direction (in / out)")
  void cidrRangesSegregatedByDirection() throws Exception {
    ComponentInstance ci = componentInstance();
    ci.setInSubnetInstance(subnetInstance("subnet-a", "10.0.0.0/24"));
    ci.setOutSubnetInstance(subnetInstance("subnet-b", "10.0.1.0/32"));

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
    ComponentInstance ci = componentInstance();

    JsonNode node = MAPPER.valueToTree(ci);

    assertThat(node.has("cidr_ranges")).isFalse();
  }

  @Test
  @DisplayName("typed CIDR entries expose subnet names")
  void typedCidrEntriesExposeSubnetNames() throws Exception {
    ComponentInstance ci = componentInstance();
    ci.setInSubnetInstance(subnetInstance("subnet-a", "10.0.0.0/24"));

    JsonNode node = MAPPER.valueToTree(ci);

    assertThat(node.get("cidr_ranges").get("in").get(0).get("name").asText()).isEqualTo("subnet-a");
  }

  private static ComponentInstance componentInstance() {
    V1ComponentInstance model = new V1ComponentInstance();
    model.setApiVersion("v1");
    model.setKind("ComponentInstance");
    model.setMetadata(instanceMetadata("component-instance"));
    model.setSpec(new V1ComponentInstanceSpec());
    return new ComponentInstance(model);
  }

  private static SubnetInstance subnetInstance(String name, String cidr) {
    V1SubnetInstanceSpec spec = new V1SubnetInstanceSpec();
    spec.setCidr(cidr);

    V1SubnetInstance model = new V1SubnetInstance();
    model.setApiVersion("v1");
    model.setKind("SubnetInstance");
    model.setMetadata(instanceMetadata(name));
    model.setSpec(spec);
    return new SubnetInstance(model);
  }

  private static V1MetadataInstance instanceMetadata(String name) {
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName(name);
    metadata.setScope("test-scope");
    metadata.setLandingzone("test-landing-zone");
    return metadata;
  }
}
