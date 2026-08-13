package com.amadeus.aud.libraries.nsg.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NetworkSecurityGraphQLTest {
  @Nested
  class NetworkSecurityGraphQLTests {

    @Test
    void shouldCreateEmptyNetworkSecurity() {
      NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();

      assertThat(dto.getLandingZoneId()).isNull();
      assertThat(dto.getNetworks()).isNull();
      assertThat(dto.getSubnets()).isNull();
      assertThat(dto.getNsgs()).isNull();
    }

    @Test
    void shouldSetAndGetLandingZoneId() {
      NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();
      dto.setLandingZoneId("lz-123");

      assertThat(dto.getLandingZoneId()).isEqualTo("lz-123");
    }

    @Test
    void shouldSetAndGetNetworks() {
      NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();
      Map<String, NetworkSecurityGraphQL.NetworkGraphQL> networks = new java.util.HashMap<>();

      NetworkSecurityGraphQL.NetworkGraphQL network = new NetworkSecurityGraphQL.NetworkGraphQL();
      network.setCidrs(List.of("10.0.0.0/16"));

      NetworkSecurityGraphQL.ProviderSpecificGraphQL providerSpecific =
          new NetworkSecurityGraphQL.ProviderSpecificGraphQL();
      // Synthetic fixture addresses, not resolvers from any real network.
      providerSpecific.setDnsservers(List.of("10.6.0.53", "10.6.1.53"));
      network.setProviderSpecific(providerSpecific);

      network.setTags(Map.of("env", "prod"));
      networks.put("network1", network);

      dto.setNetworks(networks);

      assertThat(dto.getNetworks()).hasSize(1);
      assertThat(dto.getNetworks().get("network1").getCidrs()).containsExactly("10.0.0.0/16");
      assertThat(dto.getNetworks().get("network1").getProviderSpecific()).isNotNull();
      assertThat(dto.getNetworks().get("network1").getProviderSpecific().getDnsservers())
          .containsExactly("10.6.0.53", "10.6.1.53");
      assertThat(dto.getNetworks().get("network1").getTags()).containsEntry("env", "prod");
    }

    @Test
    void shouldSetAndGetSubnets() {
      NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();
      Map<String, NetworkSecurityGraphQL.SubnetGraphQL> subnets = new java.util.HashMap<>();

      NetworkSecurityGraphQL.SubnetGraphQL subnet = new NetworkSecurityGraphQL.SubnetGraphQL();
      subnet.setName("subnet-1");
      subnet.setSubnetType("private");
      subnet.setCidr("10.0.1.0/24");
      subnet.setZoneName("zone-1");
      subnet.setZoneType("application");
      subnet.setAssociatedNSG("nsg-1");
      subnets.put("subnet1", subnet);

      dto.setSubnets(subnets);

      assertThat(dto.getSubnets()).hasSize(1);
      NetworkSecurityGraphQL.SubnetGraphQL result = dto.getSubnets().get("subnet1");
      assertThat(result.getName()).isEqualTo("subnet-1");
      assertThat(result.getSubnetType()).isEqualTo("private");
      assertThat(result.getCidr()).isEqualTo("10.0.1.0/24");
      assertThat(result.getZoneName()).isEqualTo("zone-1");
      assertThat(result.getZoneType()).isEqualTo("application");
      assertThat(result.getAssociatedNSG()).isEqualTo("nsg-1");
    }

    @Test
    void shouldSetAndGetNsgs() {
      NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();

      NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL nsgs =
          new NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL();

      Map<String, NetworkSecurityGraphQL.SecurityGroupMapGraphQL> rules = new java.util.HashMap<>();
      NetworkSecurityGraphQL.SecurityGroupMapGraphQL sgMap =
          new NetworkSecurityGraphQL.SecurityGroupMapGraphQL();

      // Add inbound rule
      NetworkSecurityGraphQL.SubnetPortGraphQL inbound =
          new NetworkSecurityGraphQL.SubnetPortGraphQL();
      inbound.setName("test-inbound");
      inbound.setCidrs(List.of("10.0.0.0/8"));
      inbound.setDstCidrs(List.of("10.1.0.0/16"));
      inbound.setPorts(List.of("80", "443"));
      inbound.setProtocol("TCP");
      sgMap.setInbound(List.of(inbound));

      // Add outbound rule
      NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL outbound =
          new NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL();
      outbound.setName("test-outbound");
      outbound.setServiceTag("Storage");
      outbound.setPorts(List.of("443"));
      outbound.setProtocol("TCP");
      outbound.setCidrs(List.of("10.0.0.0/8"));
      sgMap.setOutbound(List.of(outbound));

      rules.put("zone1-zone2", sgMap);
      nsgs.setRules(rules);
      nsgs.setTags(Map.of("region", "eu"));

      dto.setNsgs(nsgs);

      assertThat(dto.getNsgs()).isNotNull();
      assertThat(dto.getNsgs().getRules()).hasSize(1);
      assertThat(dto.getNsgs().getRules().get("zone1-zone2")).isNotNull();
      assertThat(dto.getNsgs().getRules().get("zone1-zone2").getInbound()).hasSize(1);
      assertThat(dto.getNsgs().getRules().get("zone1-zone2").getInbound().getFirst().getName())
          .isEqualTo("test-inbound");
      assertThat(dto.getNsgs().getRules().get("zone1-zone2").getOutbound()).hasSize(1);
      assertThat(dto.getNsgs().getRules().get("zone1-zone2").getOutbound().getFirst().getName())
          .isEqualTo("test-outbound");
      assertThat(dto.getNsgs().getTags()).containsEntry("region", "eu");
    }

    @Test
    void networkGraphQLShouldHandleEmptyCidrs() {
      NetworkSecurityGraphQL.NetworkGraphQL network = new NetworkSecurityGraphQL.NetworkGraphQL();

      assertThat(network.getCidrs()).isNull();
      assertThat(network.getTags()).isNull();
    }

    @Test
    void subnetGraphQLShouldHandleNullValues() {
      NetworkSecurityGraphQL.SubnetGraphQL subnet = new NetworkSecurityGraphQL.SubnetGraphQL();

      assertThat(subnet.getName()).isNull();
      assertThat(subnet.getSubnetType()).isNull();
      assertThat(subnet.getCidr()).isNull();
      assertThat(subnet.getZoneName()).isNull();
      assertThat(subnet.getZoneType()).isNull();
      assertThat(subnet.getAssociatedNSG()).isNull();
    }

    @Test
    void zoneSecurityGroupsGraphQLShouldHandleNullTags() {
      NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL nsgs =
          new NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL();

      assertThat(nsgs.getTags()).isNull();
      assertThat(nsgs.getRules()).isNull();
    }
  }
}
