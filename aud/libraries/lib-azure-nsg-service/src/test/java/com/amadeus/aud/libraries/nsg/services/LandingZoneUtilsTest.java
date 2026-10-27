package com.amadeus.aud.libraries.nsg.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.nsg.models.Subnet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LandingZoneUtilsTest {

  @Nested
  class GetSubnetInstances {
    @Test
    void returnsEmptySetWhenLandingZoneHasNoSubnets() {
      LandingZone landingZone = mock(LandingZone.class);
      doReturn(null).when(landingZone).getSubnetInstances();

      Set<SubnetInstance> result = LandingZoneUtils.getSubnetInstances(landingZone);

      assertThat(result).isEmpty();
    }

    @Test
    void filtersOutGroupAndGlobalSubnets() {
      LandingZone landingZone = mock(LandingZone.class);
      SubnetInstance regularSubnet = subnetInstance(false, "private", null, null, null, null, null);
      SubnetInstance groupSubnet = subnetInstance(true, "private", null, null, null, null, null);
      SubnetInstance globalSubnet =
          subnetInstance(false, Constants.GLOBAL, null, null, null, null, null);
      doReturn(Set.of(regularSubnet, groupSubnet, globalSubnet))
          .when(landingZone)
          .getSubnetInstances();

      Set<SubnetInstance> result = LandingZoneUtils.getSubnetInstances(landingZone);

      assertThat(result).containsExactly(regularSubnet);
    }
  }

  @Nested
  class GetSubnets {
    @Test
    void mapsRegularSubnetAndSetsAssociatedNsg() {
      Map<String, String> tags = Map.of("env", "prod");
      Map<String, Object> providerSpecific = Map.of("delegation", "enabled");
      ZoneInstance zoneInstance = zoneInstance("app-zone-1", "AppZone", false);
      SubnetInstance subnetInstance =
          subnetInstance(
              false, "private", "app-subnet", "10.0.1.0/24", tags, providerSpecific, zoneInstance);

      Map<String, Subnet> result = LandingZoneUtils.getSubnets(List.of(subnetInstance));

      assertThat(result).containsOnlyKeys("10.0.1.0/24");
      Subnet subnet = result.get("10.0.1.0/24");
      assertThat(subnet.getName()).isEqualTo("app-subnet");
      assertThat(subnet.getSubnetType()).isEqualTo("private");
      assertThat(subnet.getCidr()).isEqualTo("10.0.1.0/24");
      assertThat(subnet.getZoneName()).isEqualTo("app-zone-1");
      assertThat(subnet.getZoneType()).isEqualTo("AppZone");
      assertThat(subnet.getAssociatedNSG()).isEqualTo("AppZone-app-zone-1");
      assertThat(subnet.getTags()).containsExactlyInAnyOrderEntriesOf(tags);
      assertThat(subnet.getProviderSpecific()).isEqualTo(providerSpecific);
    }

    @Test
    void keepsTechnicalZoneWithoutNsgAndDoesNotSetAssociatedNsg() {
      ZoneInstance zoneInstance = zoneInstance("tech-zone-1", Constants.TECHNICAL_ZONE, false);
      SubnetInstance subnetInstance =
          subnetInstance(
              false, "private", "ops-subnet", "10.0.2.0/24", Map.of(), Map.of(), zoneInstance);

      Map<String, Subnet> result = LandingZoneUtils.getSubnets(List.of(subnetInstance));

      Subnet subnet = result.get("10.0.2.0/24");
      assertThat(subnet.getZoneType()).isEqualTo(Constants.TECHNICAL_ZONE);
      assertThat(subnet.getAssociatedNSG()).isNull();
    }

    @Test
    void convertsTechnicalZoneToTechnicalZoneWithNsgWhenNeeded() {
      ZoneInstance zoneInstance = zoneInstance("tech-zone-2", Constants.TECHNICAL_ZONE, true);
      SubnetInstance subnetInstance =
          subnetInstance(
              false,
              "private",
              "ops-subnet-nsg",
              "10.0.3.0/24",
              Map.of("team", "network"),
              Map.of("dnsservers", List.of("8.8.8.8")),
              zoneInstance);

      Map<String, Subnet> result = LandingZoneUtils.getSubnets(List.of(subnetInstance));

      Subnet subnet = result.get("10.0.3.0/24");
      assertThat(subnet.getZoneType()).isEqualTo(Constants.TECHNICAL_ZONE_WITH_NSG);
      assertThat(subnet.getAssociatedNSG()).isEqualTo(Constants.TECHNICAL_ZONE + "-tech-zone-2");
    }
  }

  private SubnetInstance subnetInstance(
      boolean isGroup,
      String subnetTypeName,
      String name,
      String cidr,
      Map<String, String> tags,
      Map<String, Object> providerSpecific,
      ZoneInstance zoneInstance) {
    SubnetInstance subnetInstance = mock(SubnetInstance.class);
    doReturn(isGroup).when(subnetInstance).isGroup();
    doReturn(subnetTypeName).when(subnetInstance).getSubnetTypeName();
    doReturn(name).when(subnetInstance).getName();
    doReturn(cidr).when(subnetInstance).getCidr();
    doReturn(tags).when(subnetInstance).getTagsAsMap();
    doReturn(providerSpecific).when(subnetInstance).getProviderSpecificAsMap();
    doReturn(zoneInstance).when(subnetInstance).getZoneInstance();
    return subnetInstance;
  }

  private ZoneInstance zoneInstance(String name, String zoneTypeName, boolean isNsgNeeded) {
    ZoneInstance zoneInstance = mock(ZoneInstance.class);
    doReturn(name).when(zoneInstance).getName();
    doReturn(zoneTypeName).when(zoneInstance).getZoneTypeName();
    doReturn(isNsgNeeded).when(zoneInstance).isNsgNeeded();
    return zoneInstance;
  }
}
