package com.amadeus.aud.libraries.nsg.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NetworkSecurityFilterTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private NetworkSecurityGraphQL networkSecurity;

  @BeforeEach
  void setUp() {
    networkSecurity = new NetworkSecurityGraphQL();
    networkSecurity.setLandingZoneId("lz-prod-001");

    // Setup networks
    Map<String, NetworkSecurityGraphQL.NetworkGraphQL> networks = new HashMap<>();
    NetworkSecurityGraphQL.NetworkGraphQL network = new NetworkSecurityGraphQL.NetworkGraphQL();
    network.setCidrs(List.of("10.0.0.0/16", "192.168.1.0/24"));
    network.setTags(Map.of("env", "production", "region", "eu-west"));
    networks.put("vnet1", network);
    networkSecurity.setNetworks(networks);

    // Setup subnets
    Map<String, NetworkSecurityGraphQL.SubnetGraphQL> subnets = new HashMap<>();

    NetworkSecurityGraphQL.SubnetGraphQL subnet1 = new NetworkSecurityGraphQL.SubnetGraphQL();
    subnet1.setName("app-subnet");
    subnet1.setSubnetType("private");
    subnet1.setCidr("10.0.1.0/24");
    subnet1.setZoneName("application-zone");
    subnet1.setZoneType("AppZone");
    subnet1.setAssociatedNSG("nsg-app");
    subnets.put("subnet1", subnet1);

    NetworkSecurityGraphQL.SubnetGraphQL subnet2 = new NetworkSecurityGraphQL.SubnetGraphQL();
    subnet2.setName("db-subnet");
    subnet2.setSubnetType("private");
    subnet2.setCidr("10.0.2.0/24");
    subnet2.setZoneName("database-zone");
    subnet2.setZoneType("DataZone");
    subnet2.setAssociatedNSG("nsg-db");
    subnets.put("subnet2", subnet2);

    networkSecurity.setSubnets(subnets);
  }

  @Test
  void shouldMatchWhenAllFiltersAreNull() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldMatchLandingZoneNameCaseInsensitive() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setLandingZoneName(sf("PROD"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongLandingZoneName() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setLandingZoneName(sf("test"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchCidrInNetworks() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setCidr(sf("10.0.0.0"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldMatchCidrInSubnets() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setCidr(sf("10.0.1.0"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongCidr() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setCidr(sf("172.16.0.0"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchSubnetNamePartial() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setSubnetName(sf("app"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldMatchSubnetNameCaseInsensitive() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setSubnetName(sf("DB-SUBNET"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongSubnetName() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setSubnetName(sf("web"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchSubnetType() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setSubnetType(sf("private"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongSubnetType() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setSubnetType(sf("public"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchZoneNamePartial() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setZoneName(sf("application"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldMatchZoneNameCaseInsensitive() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setZoneName(sf("DATABASE"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongZoneName() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setZoneName(sf("web"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchZoneType() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setZoneType(sf("AppZone"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongZoneType() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setZoneType(sf("WebZone"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchLabelsInNetworks() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();

    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("env");
    labelFilter.setValue("production");
    filter.setLabels(List.of(labelFilter));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWrongLabels() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();

    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("env");
    labelFilter.setValue("development");
    filter.setLabels(List.of(labelFilter));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldMatchMultipleFilters() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setLandingZoneName(sf("prod"));
    filter.setCidr(sf("10.0"));
    filter.setSubnetType(sf("private"));

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isTrue();
  }

  @Test
  void shouldNotMatchWhenOneFilterFails() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setLandingZoneName(sf("prod")); // matches
    filter.setCidr(sf("10.0")); // matches
    filter.setSubnetType(sf("public")); // does NOT match

    boolean result = filter.matches("lz-prod-001", networkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldHandleNullNetworkSecurity() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setCidr(sf("10.0.0.0"));

    NetworkSecurityGraphQL nullNetworkSecurity = new NetworkSecurityGraphQL();

    boolean result = filter.matches("lz-test", nullNetworkSecurity);

    assertThat(result).isFalse();
  }

  @Test
  void shouldSetLabelsToEmptyListWhenNull() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();
    filter.setLabels(null);

    assertThat(filter.getLabels()).isEmpty();
  }

  @Test
  void shouldGetAndSetAllProperties() {
    NetworkSecurityFilter filter = new NetworkSecurityFilter();

    filter.setLandingZoneName(sf("test-lz"));
    filter.setCidr(sf("10.0.0.0/16"));
    filter.setSubnetName(sf("subnet-1"));
    filter.setSubnetType(sf("private"));
    filter.setZoneName(sf("zone-1"));
    filter.setZoneType(sf("AppZone"));

    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("env");
    filter.setLabels(List.of(labelFilter));

    assertThat(filter.getLandingZoneName()).isNotNull();
    assertThat(filter.getCidr()).isNotNull();
    assertThat(filter.getSubnetName()).isNotNull();
    assertThat(filter.getSubnetType()).isNotNull();
    assertThat(filter.getZoneName()).isNotNull();
    assertThat(filter.getZoneType()).isNotNull();
    assertThat(filter.getLabels()).hasSize(1);
  }
}
