package com.amadeus.aud.libraries.ipreport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.ipreport.models.IpReportAs;
import com.amadeus.aud.libraries.ipreport.models.IpReportCidr;
import com.amadeus.aud.libraries.ipreport.models.IpReportIp;
import com.amadeus.aud.libraries.ipreport.models.IpReportSubnet;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IpReportUtilsTest {

  @Mock private SubnetInstance mockSubnetInstance;
  @Mock private ComponentInstance mockComponentInstance;
  @Mock private IPAddressInstance mockIPAddressInstance;
  @Mock private LandingZone mockLandingZone;
  @Mock private Target mockTarget;
  @Mock private Region mockRegion;
  @Mock private ZoneInstance mockZoneInstance;
  @Mock private FirewallGroup mockFirewallGroup;

  @Nested
  class ToIpReportSubnet {

    @Test
    void withValidInput() {
      // Arrange
      when(mockSubnetInstance.getName()).thenReturn("TestSubnet");
      when(mockSubnetInstance.getCidr()).thenReturn("10.0.0.0/24");
      when(mockSubnetInstance.getScopeName()).thenReturn("TestScope");
      when(mockSubnetInstance.getLandingZone()).thenReturn(mockLandingZone);
      when(mockSubnetInstance.isShared()).thenReturn(false);
      when(mockSubnetInstance.getSubnetTypeName()).thenReturn("Private");
      when(mockSubnetInstance.getZoneInstanceName()).thenReturn("ZoneTest");
      when(mockSubnetInstance.getLandingZoneName()).thenReturn("LandingZoneTest");
      when(mockSubnetInstance.getZoneInstance()).thenReturn(mockZoneInstance);
      when(mockSubnetInstance.getURLHash()).thenReturn("hash123");
      when(mockSubnetInstance.getComponentInstances()).thenReturn(null);

      when(mockTarget.getProviderName()).thenReturn("AWS");
      when(mockTarget.isCloud()).thenReturn(true);
      when(mockTarget.getFirewallGroups()).thenReturn(Collections.singleton(mockFirewallGroup));

      when(mockFirewallGroup.getName()).thenReturn("TestFirewall");

      when(mockRegion.getContinentName()).thenReturn("Europe");
      when(mockRegion.getURLHash()).thenReturn("regionHash");

      when(mockLandingZone.getTarget()).thenReturn(mockTarget);
      when(mockLandingZone.getRegion()).thenReturn(mockRegion);
      when(mockLandingZone.getRegionName()).thenReturn("eu-west-1");
      when(mockLandingZone.getTargetName()).thenReturn("Production");
      when(mockLandingZone.getLandingZoneTypeName()).thenReturn("Standard");
      when(mockLandingZone.getMacroPhaseName()).thenReturn("Phase1");
      when(mockZoneInstance.getZoneTypeName()).thenReturn("DMZ");
      when(mockZoneInstance.getURLHash()).thenReturn("zoneHash");
      when(mockZoneInstance.getComponentInstances()).thenReturn(null);

      // Act
      IpReportSubnet result = IpReportUtils.toIpReportSubnet(mockSubnetInstance);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("TestSubnet");
      assertThat(result.getCidr()).isEqualTo("10.0.0.0/24");
      assertThat(result.getScope()).isEqualTo("TestScope");
      assertThat(result.getProvider()).isEqualTo("AWS");
      assertThat(result.getContinent()).isEqualTo("Europe");
      assertThat(result.getRegion()).isEqualTo("eu-west-1");
      assertThat(result.getTarget()).isEqualTo("Production");
      assertThat(result.getFirewallgroup()).isEqualTo("TestFirewall");
      assertThat(result.getLandingzone()).isEqualTo("LandingZoneTest");
      assertThat(result.getLandingzoneType()).isEqualTo("Standard");
      assertThat(result.getMacrophase()).isEqualTo("Phase1");
      assertThat(result.getSubnetType()).isEqualTo("Private");
      assertThat(result.getSubnetShared()).isFalse();
      assertThat(result.getImplementationZone()).isEqualTo("ZoneTest");
      assertThat(result.getSecurityZone()).isEqualTo("DMZ");
      assertThat(result.getSnUrlHash()).isEqualTo("hash123");
      assertThat(result.getReUrlHash()).isEqualTo("regionHash");
      assertThat(result.getIzUrlHash()).isEqualTo("zoneHash");
      assertThat(result.getAsIncoming()).isNotNull();
      assertThat(result.getAsOutgoing()).isNotNull();
    }

    @Test
    void withNullInput() {
      // Act
      IpReportSubnet result = IpReportUtils.toIpReportSubnet(null);

      // Assert
      assertThat(result).isNull();
    }
  }

  @Nested
  class ToIpReportCidr {

    @Test
    void withValidInput() {
      // Arrange
      when(mockComponentInstance.getName()).thenReturn("TestComponent");
      when(mockComponentInstance.getScopeName()).thenReturn("TestScope");
      when(mockComponentInstance.getLandingZone()).thenReturn(mockLandingZone);
      when(mockComponentInstance.getLandingZoneName()).thenReturn("LandingZoneTest");
      when(mockComponentInstance.getComponentName()).thenReturn("ComponentType");
      when(mockComponentInstance.getApplicationPhaseName()).thenReturn("Development");
      when(mockComponentInstance.getURLHash()).thenReturn("compHash");

      when(mockTarget.getProviderName()).thenReturn("Azure");

      when(mockRegion.getContinentName()).thenReturn("America");
      when(mockRegion.getURLHash()).thenReturn("regionHash");

      when(mockLandingZone.getTarget()).thenReturn(mockTarget);
      when(mockLandingZone.getRegion()).thenReturn(mockRegion);
      when(mockLandingZone.getRegionName()).thenReturn("us-east-1");
      when(mockLandingZone.getTargetName()).thenReturn("Staging");
      when(mockLandingZone.getMacroPhaseName()).thenReturn("Phase2");

      // Act
      IpReportCidr result = IpReportUtils.toIpReportCidr(mockComponentInstance);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("TestComponent");
      assertThat(result.getScope()).isEqualTo("TestScope");
      assertThat(result.getProvider()).isEqualTo("Azure");
      assertThat(result.getContinent()).isEqualTo("America");
      assertThat(result.getRegion()).isEqualTo("us-east-1");
      assertThat(result.getTarget()).isEqualTo("Staging");
      assertThat(result.getFirewallgroup()).isEmpty(); // Empty when no firewall groups
      assertThat(result.getLandingzone()).isEqualTo("LandingZoneTest");
      assertThat(result.getMacrophase()).isEqualTo("Phase2");
      assertThat(result.getComponent()).isEqualTo("ComponentType");
      assertThat(result.getComponentInstance()).isEqualTo("TestComponent");
      assertThat(result.getApplicationphase()).isEqualTo("Development");
      assertThat(result.getReUrlHash()).isEqualTo("regionHash");
      assertThat(result.getCiUrlHash()).isEqualTo("compHash");
    }

    @Test
    void withNullInput() {
      // Act
      IpReportCidr result = IpReportUtils.toIpReportCidr(null);

      // Assert
      assertThat(result).isNull();
    }
  }

  @Nested
  class ToIpReportAs {

    @Test
    void withValidInput() {
      // Arrange
      when(mockComponentInstance.getName()).thenReturn("TestComponent");
      when(mockComponentInstance.getURLHash()).thenReturn("compHash");

      // Act
      IpReportAs result = IpReportUtils.toIpReportAs(mockComponentInstance);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getComponentInstance()).isEqualTo("TestComponent");
      assertThat(result.getCiUrlHash()).isEqualTo("compHash");
    }

    @Test
    void withNullInput() {
      // Act
      IpReportAs result = IpReportUtils.toIpReportAs(null);

      // Assert
      assertThat(result).isNull();
    }
  }

  @Nested
  class ToIpReportIp {

    @Test
    void withValidInput() {
      // Arrange
      when(mockIPAddressInstance.getName()).thenReturn("TestIP");
      when(mockIPAddressInstance.getHost()).thenReturn("192.168.1.1");
      when(mockIPAddressInstance.getScopeName()).thenReturn("TestScope");
      when(mockIPAddressInstance.getLandingZone()).thenReturn(mockLandingZone);
      when(mockIPAddressInstance.getLandingZoneName()).thenReturn("LandingZoneTest");
      when(mockIPAddressInstance.getURLHash()).thenReturn("ipHash");
      when(mockIPAddressInstance.getComponentInstance()).thenReturn(mockComponentInstance);

      when(mockComponentInstance.getComponentName()).thenReturn("WebServer");
      when(mockComponentInstance.getName()).thenReturn("WebServer-01");
      when(mockComponentInstance.getApplicationPhaseName()).thenReturn("Production");
      when(mockComponentInstance.getURLHash()).thenReturn("compHash");
      when(mockComponentInstance.getInSubnetInstance()).thenReturn(mockSubnetInstance);

      when(mockSubnetInstance.getZoneInstance()).thenReturn(mockZoneInstance);

      when(mockZoneInstance.getName()).thenReturn("DMZZone");
      when(mockZoneInstance.getURLHash()).thenReturn("zoneHash");

      when(mockTarget.getProviderName()).thenReturn("GCP");
      when(mockTarget.isCloud()).thenReturn(true);
      when(mockTarget.getFirewallGroups()).thenReturn(Collections.singleton(mockFirewallGroup));

      when(mockFirewallGroup.getName()).thenReturn("GCPFirewall");

      when(mockRegion.getContinentName()).thenReturn("Asia");
      when(mockRegion.getURLHash()).thenReturn("regionHash");

      when(mockLandingZone.getTarget()).thenReturn(mockTarget);
      when(mockLandingZone.getRegion()).thenReturn(mockRegion);
      when(mockLandingZone.getRegionName()).thenReturn("asia-east-1");
      when(mockLandingZone.getTargetName()).thenReturn("Production");
      when(mockLandingZone.getMacroPhaseName()).thenReturn("Phase3");

      // Act
      IpReportIp result = IpReportUtils.toIpReportIp(mockIPAddressInstance);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("TestIP");
      assertThat(result.getHost()).isEqualTo("192.168.1.1");
      assertThat(result.getScope()).isEqualTo("TestScope");
      assertThat(result.getProvider()).isEqualTo("GCP");
      assertThat(result.getContinent()).isEqualTo("Asia");
      assertThat(result.getRegion()).isEqualTo("asia-east-1");
      assertThat(result.getTarget()).isEqualTo("Production");
      assertThat(result.getFirewallgroup()).isEqualTo("GCPFirewall");
      assertThat(result.getLandingzone()).isEqualTo("LandingZoneTest");
      assertThat(result.getMacrophase()).isEqualTo("Phase3");
      assertThat(result.getComponent()).isEqualTo("WebServer");
      assertThat(result.getComponentInstance()).isEqualTo("WebServer-01");
      assertThat(result.getApplicationphase()).isEqualTo("Production");
      assertThat(result.getIpaUrlHash()).isEqualTo("ipHash");
      assertThat(result.getReUrlHash()).isEqualTo("regionHash");
      assertThat(result.getCiUrlHash()).isEqualTo("compHash");
      assertThat(result.getImplementationZone()).isEqualTo("DMZZone");
      assertThat(result.getIzUrlHash()).isEqualTo("zoneHash");
    }

    @Test
    void withNullComponent() {
      // Arrange
      when(mockIPAddressInstance.getName()).thenReturn("TestIP");
      when(mockIPAddressInstance.getHost()).thenReturn("192.168.1.1");
      when(mockIPAddressInstance.getScopeName()).thenReturn("TestScope");
      when(mockIPAddressInstance.getLandingZone()).thenReturn(mockLandingZone);
      when(mockIPAddressInstance.getLandingZoneName()).thenReturn("LandingZoneTest");
      when(mockIPAddressInstance.getURLHash()).thenReturn("ipHash");
      when(mockIPAddressInstance.getComponentInstance()).thenReturn(null);

      when(mockTarget.getProviderName()).thenReturn("AWS");

      when(mockRegion.getContinentName()).thenReturn("Europe");
      when(mockRegion.getURLHash()).thenReturn("regionHash");

      when(mockLandingZone.getTarget()).thenReturn(mockTarget);
      when(mockLandingZone.getRegion()).thenReturn(mockRegion);
      when(mockLandingZone.getRegionName()).thenReturn("eu-west-1");
      when(mockLandingZone.getTargetName()).thenReturn("Test");
      when(mockLandingZone.getMacroPhaseName()).thenReturn("Phase1");

      // Act
      IpReportIp result = IpReportUtils.toIpReportIp(mockIPAddressInstance);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("TestIP");
      assertThat(result.getHost()).isEqualTo("192.168.1.1");
      assertThat(result.getComponent()).isNull();
      assertThat(result.getComponentInstance()).isNull();
      assertThat(result.getApplicationphase()).isNull();
      assertThat(result.getCiUrlHash()).isNull();
      assertThat(result.getImplementationZone()).isNull();
      assertThat(result.getIzUrlHash()).isNull();
    }

    @Test
    void withNullInput() {
      // Act
      IpReportIp result = IpReportUtils.toIpReportIp(null);

      // Assert
      assertThat(result).isNull();
    }
  }
}
