package com.amadeus.aud.libraries.models.core.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstanceSpec;
import java.util.LinkedHashSet;
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
  @DisplayName("orders CIDRs by numeric address")
  void ordersCidrsByValue() {
    ComponentInstance ci = new ComponentInstance();
    ci.setPaasZoneInSubnetInstances(
        Set.of(
            mockSubnet("subnet-10-id", "subnet-10", "10.0.10.0/24"),
            mockSubnet("subnet-3-id", "subnet-3", "10.0.3.0/24"),
            mockSubnet("subnet-1-id", "subnet-1", "10.0.1.0/24"),
            mockSubnet("subnet-2-id", "subnet-2", "10.0.2.0/24")));

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24", "10.0.10.0/24");
  }

  @Test
  @DisplayName("preserves invalid values first and orders valid CIDRs by prefix")
  void preservesInvalidValuesFirstAndOrdersCidrsByPrefix() {
    ComponentInstance ci = new ComponentInstance();
    ci.setPaasZoneInSubnetInstances(
        new LinkedHashSet<>(
            List.of(
                mockSubnet("invalid-b-id", "invalid-b", "invalid-b"),
                mockSubnet("subnet-24-id", "subnet-24", "10.0.2.0/24"),
                mockSubnet("subnet-16-id", "subnet-16", "10.0.2.0/16"),
                mockSubnet("invalid-a-id", "invalid-a", "invalid-a"))));

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("invalid-b", "invalid-a", "10.0.2.0/16", "10.0.2.0/24");
  }

  @Test
  @DisplayName("orders CIDRs by address regardless of name")
  void ordersCidrsRegardlessOfName() {
    ComponentInstance ci = new ComponentInstance();
    ci.setPaasZoneInSubnetInstances(
        Set.of(
            mockSubnet("unnamed-1-id", null, "10.0.1.0/24"),
            mockSubnet("named-id", "named-subnet", "10.0.10.0/24"),
            mockSubnet("unnamed-2-id", " ", "10.0.2.0/24")));

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.0.1.0/24", "10.0.2.0/24", "10.0.10.0/24");
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
    ci.setComponent(component(false));
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
  @DisplayName("limits container hosting platform subnets to its zone type")
  void limitsContainerHostingPlatformSubnetsToItsZoneType() {
    ComponentInstance ci = new ComponentInstance();
    ZoneType containerZoneType = zoneType("tnz");

    ci.setComponent(component(true));
    ci.setZoneType(containerZoneType);
    ci.setDeployComponentInstance(platformWithZones(containerZoneType, zoneType("app")));

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.0.0.0/24");
    assertThat(ci.getCidrRanges().get("out"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.0.0.0/24");
  }

  @Test
  @DisplayName("uses ZonedPaaS platform declared IN/OUT zones for containers")
  void usesZonedPaasPlatformDeclaredZonesForContainers() {
    ComponentInstance ci = new ComponentInstance();
    ZoneType app = zoneType("app");
    ZoneType tnz = zoneType("tnz");
    ci.setComponent(component(true));
    ci.setZoneType(app);

    ComponentInstance platform = zonedPaasPlatform(app, tnz);
    platform.setPaasZoneInSubnetInstances(
        Set.of(
            subnetInZone("app-in", "10.1.0.0/24", app),
            subnetInZone("tnz-in", "10.1.1.0/24", tnz)));
    platform.setPaasZoneOutSubnetInstances(Set.of(subnetInZone("paas-out", "10.2.0.0/24", tnz)));
    ci.setDeployComponentInstance(platform);

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.1.0.0/24");
    assertThat(ci.getCidrRanges().get("out"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.2.0.0/24");
  }

  @Test
  @DisplayName("uses ZonedPaaS platform OUT zones for TechnicalZone containers in both directions")
  void usesZonedPaasPlatformOutZonesForTechnicalZoneContainers() {
    ComponentInstance ci = new ComponentInstance();
    ZoneType technical = zoneType("TechnicalZone");
    ZoneType app = zoneType("app");
    ci.setComponent(component(true));
    ci.setZoneType(technical);

    ComponentInstance platform = zonedPaasPlatform(technical, app);
    platform.setPaasZoneInSubnetInstances(
        Set.of(
            subnetInZone("technical-in", "10.1.0.0/24", technical),
            subnetInZone("app-in", "10.1.1.0/24", app)));
    platform.setPaasZoneOutSubnetInstances(Set.of(subnetInZone("paas-out", "10.2.0.0/24", app)));
    ci.setDeployComponentInstance(platform);

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.2.0.0/24");
    assertThat(ci.getCidrRanges().get("out"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.2.0.0/24");
  }

  private static ComponentInstance zonedPaasPlatform(
      ZoneType containerZoneType, ZoneType otherZoneType) {
    ComponentInstance platform = platformWithZones(containerZoneType, otherZoneType);
    platform.setData(
        new com.amadeus.aud.libraries.yaml.models.V1ComponentInstance()
            .spec(
                new com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec()
                    .endpointType(
                        com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec
                            .EndpointTypeEnum.ZONED_PAA_S)));
    return platform;
  }

  private static SubnetInstance subnetInZone(String name, String cidr, ZoneType zoneType) {
    SubnetInstance subnet = subnet(name, cidr);
    ZoneInstance zone = new ZoneInstance();
    zone.setUniqueIdFrom(name + "-zone");
    zone.setZoneType(zoneType);
    subnet.setZoneInstance(zone);
    return subnet;
  }

  @Test
  @DisplayName("keeps all hosting platform subnets for non-containers")
  void keepsAllHostingPlatformSubnetsForNonContainers() {
    ComponentInstance ci = new ComponentInstance();
    ZoneType containerZoneType = zoneType("tnz");

    ci.setComponent(component(false));
    ci.setZoneType(containerZoneType);
    ci.setDeployComponentInstance(platformWithZones(containerZoneType, zoneType("app")));

    assertThat(ci.getCidrRanges().get("in"))
        .extracting(ComponentInstance.CidrRange::value)
        .containsExactly("10.0.0.0/24", "10.0.1.0/24");
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

  private static Component component(boolean isContainer) {
    V1Component data =
        new V1Component()
            .metadata(new V1Metadata().name("component").scope("scope"))
            .spec(new V1ComponentSpec().isContainer(isContainer));
    return new Component(data);
  }

  private static ZoneType zoneType(String name) {
    ZoneType zoneType = new ZoneType();
    zoneType.setUniqueIdFrom(name);
    zoneType.setName(name);
    return zoneType;
  }

  private static ComponentInstance platformWithZones(
      ZoneType containerZoneType, ZoneType otherZoneType) {
    ZoneInstance containerZone = new ZoneInstance();
    containerZone.setUniqueIdFrom("container-zone");
    containerZone.setZoneType(containerZoneType);
    containerZone.setSubnetInstances(Set.of(subnet("tnz-subnet", "10.0.0.0/24")));
    ZoneInstance otherZone = new ZoneInstance();
    otherZone.setUniqueIdFrom("other-zone");
    otherZone.setZoneType(otherZoneType);
    otherZone.setSubnetInstances(Set.of(subnet("app-subnet", "10.0.1.0/24")));
    LandingZone landingZone = new LandingZone();
    landingZone.setZoneInstances(Set.of(containerZone, otherZone));
    ComponentInstance platform = new ComponentInstance();
    platform.setLandingZone(landingZone);
    return platform;
  }

  private static SubnetInstance subnet(String name, String cidr) {
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName(name);
    metadata.setLandingzone("landing-zone");
    V1SubnetInstance data = new V1SubnetInstance();
    data.setMetadata(metadata);
    data.setSpec(new V1SubnetInstanceSpec().cidr(cidr));
    return new SubnetInstance(data);
  }
}
