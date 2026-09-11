package com.amadeus.aud.libraries.models.core.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import com.amadeus.aud.libraries.yaml.models.V1BlacklistSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlacklistTest {

  private Blacklist blacklist;

  @BeforeEach
  void setUp() {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("my-app");
    spec.setDestinationHostFqdns(List.of("*.google.com"));
    spec.setSourceZones(List.of("CSZ", "TNZ"));
    spec.setSourceMacrophases(List.of("TST", "PRD"));
    spec.setSourceTargets(List.of("prd-we"));
    spec.setDestinationApplicationProtocols(
        List.of(
            V1BlacklistSpec.DestinationApplicationProtocolsEnum.HTTP,
            V1BlacklistSpec.DestinationApplicationProtocolsEnum.HTTPS));
    spec.setDestinationTransportProtocols(
        List.of(V1BlacklistSpec.DestinationTransportProtocolsEnum.TCP));
    spec.setDestinationPortRanges(List.of("443", "8080"));
    blacklist = createBlacklist("my-app-blacklist", spec);
  }

  @Test
  void blacklistGetters() {
    assertThat(blacklist.getSourceComponent()).isEqualTo("my-app");
    assertThat(blacklist.getDestinationHostFqdns())
        .containsExactlyElementsOf(List.of("*.google.com"));
    assertThat(blacklist.getDestinationHostCidrs()).isNull();
    assertThat(blacklist.getSourceZones()).containsExactlyElementsOf(List.of("CSZ", "TNZ"));
    assertThat(blacklist.getSourceMacrophases()).containsExactlyElementsOf(List.of("TST", "PRD"));
    assertThat(blacklist.getSourceTargets()).containsExactlyElementsOf(List.of("prd-we"));
    assertThat(blacklist.getDestinationApplicationProtocols())
        .containsExactlyElementsOf(List.of("http", "https"));
    assertThat(blacklist.getDestinationTransportProtocols())
        .containsExactlyElementsOf(List.of("Tcp"));
    assertThat(blacklist.getDestinationPortRanges())
        .containsExactlyElementsOf(List.of("443", "8080"));
  }

  @Test
  void blacklistWithCIDR() {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("infra-team");
    spec.setDestinationHostCidrs(List.of("10.0.0.0/8"));
    spec.setSourceZones(List.of("*"));
    spec.setSourceMacrophases(List.of("*"));
    spec.setSourceTargets(List.of("*"));
    spec.setDestinationTransportProtocols(
        List.of(V1BlacklistSpec.DestinationTransportProtocolsEnum.STAR));
    spec.setDestinationPortRanges(List.of("*"));
    Blacklist cidrBlacklist = createBlacklist("cidr-blacklist", spec);

    assertThat(cidrBlacklist.getDestinationHostCidrs())
        .containsExactlyElementsOf(List.of("10.0.0.0/8"));
    assertThat(cidrBlacklist.getDestinationHostFqdns()).isNull();
    assertThat(cidrBlacklist.getSourceZones()).hasSize(1);
  }

  @Test
  void blacklistWithWildcards() {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("*");
    spec.setDestinationHostFqdns(List.of("*"));
    spec.setSourceZones(List.of("*"));
    spec.setSourceMacrophases(List.of("*"));
    spec.setSourceTargets(List.of("*"));
    spec.setDestinationTransportProtocols(
        List.of(V1BlacklistSpec.DestinationTransportProtocolsEnum.STAR));
    spec.setDestinationPortRanges(List.of("*"));
    Blacklist wildcardBlacklist = createBlacklist("wildcard-blacklist", spec);

    assertThat(wildcardBlacklist.getSourceComponent()).isEqualTo("*");
    assertThat(wildcardBlacklist.getSourceZones()).contains("*");
  }

  @Test
  void blacklistExcludingCDEZone() {
    V1BlacklistSpec spec = new V1BlacklistSpec();
    spec.setSourceComponent("security-team");
    spec.setDestinationHostFqdns(List.of("restricted.com"));
    spec.setSourceZones(List.of("+"));
    spec.setSourceMacrophases(List.of("TST", "STG", "PRD"));
    spec.setSourceTargets(List.of("prod-*"));
    spec.setDestinationTransportProtocols(
        List.of(
            V1BlacklistSpec.DestinationTransportProtocolsEnum.TCP,
            V1BlacklistSpec.DestinationTransportProtocolsEnum.UDP));
    spec.setDestinationPortRanges(List.of("80", "443", "8080-8090"));
    Blacklist excludeCDEBlacklist = createBlacklist("exclude-cde-blacklist", spec);

    // "+" means all except CDE
    assertThat(excludeCDEBlacklist.getSourceZones()).containsExactlyElementsOf(List.of("+"));
  }

  private static Blacklist createBlacklist(String name, V1BlacklistSpec spec) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope("SecurityOffice");

    V1Blacklist model = new V1Blacklist();
    model.setMetadata(metadata);
    model.setSpec(spec);
    model.setApiVersion("v1");
    model.setKind("Blacklist");

    return new Blacklist(model);
  }
}
