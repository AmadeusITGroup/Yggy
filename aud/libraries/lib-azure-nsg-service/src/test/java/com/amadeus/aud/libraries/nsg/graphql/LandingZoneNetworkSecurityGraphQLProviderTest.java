package com.amadeus.aud.libraries.nsg.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.nsg.models.Network;
import com.amadeus.aud.libraries.nsg.models.NetworkRule;
import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import com.amadeus.aud.libraries.nsg.models.Protocol;
import com.amadeus.aud.libraries.nsg.models.SecurityGroupMap;
import com.amadeus.aud.libraries.nsg.models.ServiceTagOutboundRule;
import com.amadeus.aud.libraries.nsg.models.Subnet;
import com.amadeus.aud.libraries.nsg.models.ZoneSecurityGroups;
import com.amadeus.aud.libraries.nsg.services.LandingZoneService;
import io.smallrye.mutiny.Uni;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LandingZoneNetworkSecurityGraphQLProviderTest {

  private static final String VERSION_NUMBER = "1.2.3";
  private static final String LANDING_ZONE_NAME = "lz-prod-001";
  private static final String AZURE_PROVIDER = "Microsoft - Azure";

  @Test
  void getLandingZoneNetworkSecurityReturnsMappedGraphQlPayload() throws Exception {
    AudService audService = mock(AudService.class);
    LandingZoneService landingZoneService = mock(LandingZoneService.class);
    LandingZoneNetworkSecurityGraphQLProvider provider =
        new LandingZoneNetworkSecurityGraphQLProvider(audService, landingZoneService);
    Version version = mock(Version.class);
    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target landingZoneTarget = mock(Target.class);
    NetworkSecurity networkSecurity = createNetworkSecurity();

    doReturn(Uni.createFrom().item(version)).when(audService).getVersion(VERSION_NUMBER);
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();
    doReturn(landingZone).when(landingZonesRepository).get(LANDING_ZONE_NAME);
    doReturn(landingZoneTarget).when(landingZone).getTarget();
    doReturn(AZURE_PROVIDER).when(landingZoneTarget).getProviderName();
    doReturn(networkSecurity)
        .when(landingZoneService)
        .buildNetworkSecurity(version, landingZone, landingZoneTarget);

    // Act
    PageGraphQL<NetworkSecurityGraphQL> result =
        provider.getLandingZoneNetworkSecurity(
            VERSION_NUMBER, LANDING_ZONE_NAME, null, null, null, null, null, null, 1, 10);

    assertThat(result.items()).hasSize(1);
    NetworkSecurityGraphQL item = result.items().getFirst();
    assertThat(item.getLandingZoneId()).isEqualTo(LANDING_ZONE_NAME);

    assertThat(item.getNetworks()).containsOnlyKeys("vnet-main");
    NetworkSecurityGraphQL.NetworkGraphQL network = item.getNetworks().get("vnet-main");
    assertThat(network.getCidrs()).containsExactly("10.0.0.0/16");
    assertThat(network.getTags()).containsEntry("env", "prod");
    assertThat(network.getProviderSpecific()).isNotNull();
    assertThat(network.getProviderSpecific().getDnsservers()).containsExactly("1.1.1.1", "8.8.8.8");

    assertThat(item.getSubnets()).containsOnlyKeys("subnet-app");
    NetworkSecurityGraphQL.SubnetGraphQL subnet = item.getSubnets().get("subnet-app");
    assertThat(subnet.getName()).isEqualTo("app-subnet");
    assertThat(subnet.getSubnetType()).isEqualTo("private");
    assertThat(subnet.getCidr()).isEqualTo("10.0.1.0/24");
    assertThat(subnet.getZoneName()).isEqualTo("application-zone");
    assertThat(subnet.getZoneType()).isEqualTo("AppZone");
    assertThat(subnet.getAssociatedNSG()).isEqualTo("nsg-app");

    assertThat(item.getNsgs()).isNotNull();
    assertThat(item.getNsgs().getTags()).containsEntry("owner", "platform");
    assertThat(item.getNsgs().getRules()).containsOnlyKeys("nsg-app");

    NetworkSecurityGraphQL.SecurityGroupMapGraphQL securityGroup =
        item.getNsgs().getRules().get("nsg-app");
    assertThat(securityGroup.getInbound()).hasSize(1);
    NetworkSecurityGraphQL.SubnetPortGraphQL inboundRule = securityGroup.getInbound().getFirst();
    assertThat(inboundRule.getName()).isEqualTo("allow-https-in");
    assertThat(inboundRule.getCidrs()).containsExactly("10.10.0.0/16");
    assertThat(inboundRule.getDstCidrs()).containsExactly("10.0.1.0/24");
    assertThat(inboundRule.getPorts()).containsExactly("443");
    assertThat(inboundRule.getProtocol()).isEqualTo("TCP");

    assertThat(securityGroup.getOutbound()).hasSize(1);
    NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL outboundRule =
        securityGroup.getOutbound().getFirst();
    assertThat(outboundRule.getName()).isEqualTo("allow-storage-out");
    assertThat(outboundRule.getServiceTag()).isEqualTo("Storage");
    assertThat(outboundRule.getPorts()).containsExactly("443");
    assertThat(outboundRule.getProtocol()).isEqualTo("Tcp");
    assertThat(outboundRule.getCidrs()).containsExactly("0.0.0.0/0");

    assertThat(result.pagination()).isNotNull();
    assertThat(result.pagination().count()).isOne();

    verify(landingZoneService).buildNetworkSecurity(version, landingZone, landingZoneTarget);
  }

  @Test
  void getLandingZoneNetworkSecurityReturnsItemWhenAdditionalFilterMatches() throws Exception {
    AudService audService = mock(AudService.class);
    LandingZoneService landingZoneService = mock(LandingZoneService.class);
    LandingZoneNetworkSecurityGraphQLProvider provider =
        new LandingZoneNetworkSecurityGraphQLProvider(audService, landingZoneService);
    Version version = mock(Version.class);
    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target landingZoneTarget = mock(Target.class);
    StringFilter subnetNameFilter = like("app");

    doReturn(Uni.createFrom().item(version)).when(audService).getVersion(VERSION_NUMBER);
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();
    doReturn(landingZone).when(landingZonesRepository).get(LANDING_ZONE_NAME);
    doReturn(landingZoneTarget).when(landingZone).getTarget();
    doReturn(AZURE_PROVIDER).when(landingZoneTarget).getProviderName();
    doReturn(createNetworkSecurity())
        .when(landingZoneService)
        .buildNetworkSecurity(version, landingZone, landingZoneTarget);

    // Act
    PageGraphQL<NetworkSecurityGraphQL> result =
        provider.getLandingZoneNetworkSecurity(
            VERSION_NUMBER,
            LANDING_ZONE_NAME,
            null,
            subnetNameFilter,
            null,
            null,
            null,
            null,
            1,
            10);

    assertThat(result.items()).hasSize(1);
    assertThat(result.items().getFirst().getLandingZoneId()).isEqualTo(LANDING_ZONE_NAME);
  }

  @Test
  void getLandingZoneNetworkSecurityReturnsEmptyPageWhenAdditionalFilterDoesNotMatch()
      throws Exception {
    AudService audService = mock(AudService.class);
    LandingZoneService landingZoneService = mock(LandingZoneService.class);
    LandingZoneNetworkSecurityGraphQLProvider provider =
        new LandingZoneNetworkSecurityGraphQLProvider(audService, landingZoneService);
    Version version = mock(Version.class);
    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target landingZoneTarget = mock(Target.class);
    StringFilter subnetNameFilter = like("database");

    doReturn(Uni.createFrom().item(version)).when(audService).getVersion(VERSION_NUMBER);
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();
    doReturn(landingZone).when(landingZonesRepository).get(LANDING_ZONE_NAME);
    doReturn(landingZoneTarget).when(landingZone).getTarget();
    doReturn(AZURE_PROVIDER).when(landingZoneTarget).getProviderName();
    doReturn(createNetworkSecurity())
        .when(landingZoneService)
        .buildNetworkSecurity(version, landingZone, landingZoneTarget);

    // Act
    PageGraphQL<NetworkSecurityGraphQL> result =
        provider.getLandingZoneNetworkSecurity(
            VERSION_NUMBER,
            LANDING_ZONE_NAME,
            null,
            subnetNameFilter,
            null,
            null,
            null,
            null,
            1,
            10);

    assertThat(result.items()).isEmpty();
    assertThat(result.pagination()).isNotNull();
    assertThat(result.pagination().count()).isZero();
  }

  @Test
  void getLandingZoneNetworkSecurityReturnsEmptyPageForNonAzureLandingZone() throws Exception {
    AudService audService = mock(AudService.class);
    LandingZoneService landingZoneService = mock(LandingZoneService.class);
    LandingZoneNetworkSecurityGraphQLProvider provider =
        new LandingZoneNetworkSecurityGraphQLProvider(audService, landingZoneService);
    Version version = mock(Version.class);
    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target landingZoneTarget = mock(Target.class);

    doReturn(Uni.createFrom().item(version)).when(audService).getVersion(VERSION_NUMBER);
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();
    doReturn(landingZone).when(landingZonesRepository).get(LANDING_ZONE_NAME);
    doReturn(landingZoneTarget).when(landingZone).getTarget();
    doReturn("Amazon Web Services").when(landingZoneTarget).getProviderName();

    // Act
    PageGraphQL<NetworkSecurityGraphQL> result =
        provider.getLandingZoneNetworkSecurity(
            VERSION_NUMBER, LANDING_ZONE_NAME, null, null, null, null, null, null, 1, 10);

    assertThat(result.items()).isEmpty();
    verify(landingZoneService, never())
        .buildNetworkSecurity(version, landingZone, landingZoneTarget);
  }

  private StringFilter like(String value) {
    StringFilter filter = new StringFilter();
    filter.setLike(value);
    return filter;
  }

  private NetworkSecurity createNetworkSecurity() {
    Network network = mock(Network.class);
    doReturn(List.of("10.0.0.0/16")).when(network).getCidrs();
    doReturn(Map.of("dnsservers", List.of("1.1.1.1", "8.8.8.8")))
        .when(network)
        .getProviderSpecific();
    doReturn(Map.of("env", "prod")).when(network).getTags();

    Subnet subnet = mock(Subnet.class);
    doReturn("app-subnet").when(subnet).getName();
    doReturn("private").when(subnet).getSubnetType();
    doReturn("10.0.1.0/24").when(subnet).getCidr();
    doReturn("application-zone").when(subnet).getZoneName();
    doReturn("AppZone").when(subnet).getZoneType();
    doReturn("nsg-app").when(subnet).getAssociatedNSG();

    NetworkRule inboundRule = mock(NetworkRule.class);
    doReturn("allow-https-in").when(inboundRule).getName();
    doReturn(List.of("10.10.0.0/16")).when(inboundRule).getCidrs();
    doReturn(List.of("10.0.1.0/24")).when(inboundRule).getDstCidrs();
    doReturn(List.of("443")).when(inboundRule).getPorts();
    doReturn(Protocol.TCP).when(inboundRule).getProtocol();

    ServiceTagOutboundRule outboundRule = mock(ServiceTagOutboundRule.class);
    doReturn("allow-storage-out").when(outboundRule).getName();
    doReturn("Storage").when(outboundRule).getServiceTag();
    doReturn(List.of("443")).when(outboundRule).getPorts();
    doReturn(Protocol.TCP).when(outboundRule).getProtocol();
    doReturn(List.of("0.0.0.0/0")).when(outboundRule).getCidrs();

    SecurityGroupMap securityGroupMap = new SecurityGroupMap();
    securityGroupMap.setInbound(Set.of(inboundRule));
    securityGroupMap.setOutbound(List.of(outboundRule));

    NetworkSecurity networkSecurity = new NetworkSecurity();
    networkSecurity.setNetworks(new HashMap<>(Map.of("vnet-main", network)));
    networkSecurity.setSubnets(new HashMap<>(Map.of("subnet-app", subnet)));
    networkSecurity.setNsgs(
        new ZoneSecurityGroups(
            new HashMap<>(Map.of("nsg-app", securityGroupMap)), null, Map.of("owner", "platform")));
    return networkSecurity;
  }
}
