package com.amadeus.aud.libraries.models.core.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ComponentInstance.getCidrRanges")
class ComponentInstanceCidrRangesTest {

  @Test
  @DisplayName("returns in and out subnets segregated by direction")
  void returnsInAndOutSegregatedByDirection() {
    ComponentInstance ci = new ComponentInstance();
    SubnetInstance inSubnet = mockSubnet("in-id", "in-name", "10.0.0.0/24");
    SubnetInstance outSubnet = mockSubnet("out-id", "out-name", "10.0.1.0/24");
    ci.setInSubnetInstance(inSubnet);
    ci.setOutSubnetInstance(outSubnet);

    Map<String, List<ComponentInstance.CidrRange>> result = ci.getCidrRanges();

    assertThat(result).containsOnlyKeys("in", "out");
    assertThat(result.get("in").getFirst().value()).isEqualTo("10.0.0.0/24");
    assertThat(result.get("out").getFirst().value()).isEqualTo("10.0.1.0/24");
  }

  @Test
  @DisplayName("omits empty buckets")
  void omitsEmptyBuckets() {
    ComponentInstance ci = new ComponentInstance();
    ci.setInSubnetInstance(mockSubnet("in-id", "in-name", "10.0.0.0/24"));
    ci.setOutSubnetInstance(null);

    Map<String, List<ComponentInstance.CidrRange>> result = ci.getCidrRanges();

    assertThat(result).containsOnlyKeys("in");
  }

  @Test
  @DisplayName("returns empty map when no subnets")
  void returnsEmptyMapWhenNoSubnets() {
    ComponentInstance ci = new ComponentInstance();

    Map<String, List<ComponentInstance.CidrRange>> result = ci.getCidrRanges();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("deduplicates within a bucket")
  void deduplicatesWithinBucket() {
    ComponentInstance ci = new ComponentInstance();
    SubnetInstance inSubnet = mockSubnet("in-id", "in-name", "10.0.0.0/24");
    ci.setInSubnetInstance(inSubnet);
    ci.setOutSubnetInstance(inSubnet);

    Map<String, List<ComponentInstance.CidrRange>> result = ci.getCidrRanges();

    assertThat(result).containsOnlyKeys("in", "out");
    assertThat(result.get("in").getFirst().value()).isEqualTo("10.0.0.0/24");
    assertThat(result.get("out").getFirst().value()).isEqualTo("10.0.0.0/24");
  }

  @Test
  @DisplayName("combines direct and paas subnets")
  void combinesDirectAndPaasSubnets() {
    ComponentInstance ci = spy(new ComponentInstance());

    SubnetInstance inSubnet = mockSubnet("in-id", "in-name", "10.0.0.0/24");
    ci.setInSubnetInstance(inSubnet);

    SubnetInstance paasSubnet = mockSubnet("paas-id", "paas-name", "10.0.2.0/24");
    ci.setPaasZoneInSubnetInstances(Set.of(paasSubnet));

    Map<String, List<ComponentInstance.CidrRange>> result = ci.getCidrRanges();

    assertThat(result).containsOnlyKeys("in");
    List<ComponentInstance.CidrRange> inList = result.get("in");
    assertThat(inList).hasSize(2);

    assertThat(inList.get(0).value()).isEqualTo("10.0.0.0/24");
    assertThat(inList.get(0).name()).isEqualTo("in-name");
    assertThat(inList.get(1).value()).isEqualTo("10.0.2.0/24");
    assertThat(inList.get(1).name()).isEqualTo("paas-name");
  }

  @Test
  @DisplayName("includes names from zone subnets")
  void includesNamesFromZoneSubnets() {
    ComponentInstance ci = new ComponentInstance();
    ZoneInstance zone = mock(ZoneInstance.class);
    SubnetInstance subnet = mockSubnet("subnet-id", "subnet-name", "10.0.0.0/24");
    when(zone.getSubnetInstances()).thenReturn(Set.of(subnet));
    ci.setInZoneInstance(zone);

    ComponentInstance.CidrRange range = ci.getCidrRanges().get("in").getFirst();

    assertThat(range.name()).isEqualTo("subnet-name");
  }

  @Test
  @DisplayName("includes names from the hosting platform landing zone")
  void includesNamesFromHostingPlatformLandingZone() {
    ComponentInstance ci = new ComponentInstance();
    ComponentInstance platform = mock(ComponentInstance.class);
    LandingZone landingZone = mock(LandingZone.class);
    ZoneInstance zone = mock(ZoneInstance.class);
    SubnetInstance subnet = mockSubnet("subnet-id", "subnet-name", "10.0.1.0/24");
    when(platform.getLandingZone()).thenReturn(landingZone);
    when(landingZone.getZoneInstances()).thenReturn(Set.of(zone));
    when(zone.getSubnetInstances()).thenReturn(Set.of(subnet));
    ci.setDeployComponentInstance(platform);

    ComponentInstance.CidrRange range = ci.getCidrRanges().get("in").getFirst();

    assertThat(range.name()).isEqualTo("subnet-name");
  }

  @Test
  @DisplayName("includes referenced IP address instances")
  void includesReferencedIpAddressInstances() {
    ComponentInstance ci = new ComponentInstance();
    IPAddressInstance ipAddress = mock(IPAddressInstance.class);
    when(ipAddress.getName()).thenReturn("10.0.2.7");
    when(ipAddress.getHost()).thenReturn("host.example");
    ci.setIpAddressInstances(Set.of(ipAddress));

    ComponentInstance.CidrRange range = ci.getCidrRanges().get("in").getFirst();

    assertThat(range.value()).isEqualTo("10.0.2.7/32");
    assertThat(range.name()).isEqualTo("host.example");
  }

  @Test
  @DisplayName("includes IP address instances from the hosting platform")
  void includesHostingPlatformIpAddressInstances() {
    ComponentInstance ci = new ComponentInstance();
    ComponentInstance hostingPlatform = new ComponentInstance();
    IPAddressInstance ipAddress = mock(IPAddressInstance.class);
    when(ipAddress.getName()).thenReturn("10.5.0.42");
    when(ipAddress.getHost()).thenReturn("hello.example.net");
    hostingPlatform.setIpAddressInstances(Set.of(ipAddress));
    ci.setDeployComponentInstance(hostingPlatform);

    assertThat(ci.getCidrRanges().get("in"))
        .anySatisfy(
            (ComponentInstance.CidrRange range) -> {
              assertThat(range.value()).isEqualTo("10.5.0.42/32");
              assertThat(range.name()).isEqualTo("hello.example.net");
            });
  }

  @Test
  @DisplayName("omits subnets without a CIDR")
  void omitsSubnetsWithoutCidr() {
    ComponentInstance ci = new ComponentInstance();
    ci.setInSubnetInstance(mockSubnet("subnet-id", "subnet-name", ""));
    ci.setOutSubnetInstance(mockSubnet("out-id", "out-name", " "));

    assertThat(ci.getCidrRanges()).isEmpty();
  }

  @Test
  @DisplayName("normalizes subnet CIDRs before deduplication")
  void normalizesSubnetCidrs() {
    ComponentInstance ci = new ComponentInstance();
    ci.setInSubnetInstance(mockSubnet("in-id", "in-name", " 10.0.0.0/24 "));
    ci.setPaasZoneInSubnetInstances(Set.of(mockSubnet("paas-id", "paas-name", "10.0.0.0/24")));

    assertThat(ci.getCidrRanges().get("in"))
        .singleElement()
        .extracting(ComponentInstance.CidrRange::value)
        .isEqualTo("10.0.0.0/24");
  }

  @Test
  @DisplayName("ignores IP address instances without an address")
  void ignoresIpAddressesWithoutAnAddress() {
    ComponentInstance ci = new ComponentInstance();
    IPAddressInstance ipAddress = mock(IPAddressInstance.class);
    when(ipAddress.getName()).thenReturn(null);
    ci.setIpAddressInstances(Set.of(ipAddress));

    assertThat(ci.getCidrRanges()).isEmpty();
  }

  private static SubnetInstance mockSubnet(String id, String name, String cidr) {
    SubnetInstance subnet = mock(SubnetInstance.class);
    when(subnet.id()).thenReturn(id);
    when(subnet.getName()).thenReturn(name);
    when(subnet.getCidr()).thenReturn(cidr);
    return subnet;
  }
}
