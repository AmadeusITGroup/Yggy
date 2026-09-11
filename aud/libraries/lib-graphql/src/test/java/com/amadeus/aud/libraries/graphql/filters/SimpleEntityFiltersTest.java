package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimpleEntityFiltersTest {

  // ---------- ConnectionPhaseFilter ----------

  private ConnectionPhase cp(String name) {
    ConnectionPhase cp = mock(ConnectionPhase.class);
    when(cp.getName()).thenReturn(name);
    when(cp.getControl()).thenReturn(V1ConnectionPhaseSpec.ControlEnum.GREEN);
    when(cp.getSourcePhaseName()).thenReturn("phase-dev");
    when(cp.getDestinationPhaseName()).thenReturn("phase-prod");
    return cp;
  }

  @Test
  void connectionPhase_matchesNull_returnsFalse() {
    assertThat(new ConnectionPhaseFilter().matches(null)).isFalse();
  }

  @Test
  void connectionPhase_emptyFilter_matchesAll() {
    assertThat(new ConnectionPhaseFilter().matches(cp("cp-main"))).isTrue();
  }

  @Test
  void connectionPhase_filterByName_match() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("main");
    f.setName(stringFilter);
    assertThat(f.matches(cp("cp-main"))).isTrue();
  }

  @Test
  void connectionPhase_filterByName_noMatch() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("backup");
    f.setName(stringFilter);
    assertThat(f.matches(cp("cp-main"))).isFalse();
  }

  @Test
  void connectionPhase_filterByControl_match() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter controlFilter = new StringFilter();
    controlFilter.setLike("green");
    f.setControl(controlFilter);
    assertThat(f.matches(cp("cp-main"))).isTrue();
  }

  @Test
  void connectionPhase_filterByControl_noMatch() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter controlFilter = new StringFilter();
    controlFilter.setLike("red");
    f.setControl(controlFilter);
    assertThat(f.matches(cp("cp-main"))).isFalse();
  }

  @Test
  void connectionPhase_filterByControl_nullControl_noMatch() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter controlFilter = new StringFilter();
    controlFilter.setLike("green");
    f.setControl(controlFilter);
    ConnectionPhase cp = cp("cp-main");
    when(cp.getControl()).thenReturn(null);
    assertThat(f.matches(cp)).isFalse();
  }

  @Test
  void connectionPhase_filterBySourcePhase_match() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter sourcePhaseFilter = new StringFilter();
    sourcePhaseFilter.setLike("phase-dev");
    f.setSourcePhase(sourcePhaseFilter);
    assertThat(f.matches(cp("cp-main"))).isTrue();
  }

  @Test
  void connectionPhase_filterBySourcePhase_noMatch() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter sourcePhaseFilter = new StringFilter();
    sourcePhaseFilter.setLike("phase-staging");
    f.setSourcePhase(sourcePhaseFilter);
    assertThat(f.matches(cp("cp-main"))).isFalse();
  }

  @Test
  void connectionPhase_filterByDestinationPhase_match() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter destinationPhaseFilter = new StringFilter();
    destinationPhaseFilter.setLike("phase-prod");
    f.setDestinationPhase(destinationPhaseFilter);
    assertThat(f.matches(cp("cp-main"))).isTrue();
  }

  @Test
  void connectionPhase_filterByDestinationPhase_noMatch() {
    ConnectionPhaseFilter f = new ConnectionPhaseFilter();
    StringFilter destinationPhaseFilter = new StringFilter();
    destinationPhaseFilter.setLike("phase-staging");
    f.setDestinationPhase(destinationPhaseFilter);
    assertThat(f.matches(cp("cp-main"))).isFalse();
  }

  // ---------- IPAddressInstanceFilter ----------

  private IPAddressInstance ip(String name) {
    IPAddressInstance ip = mock(IPAddressInstance.class);
    when(ip.getName()).thenReturn(name);
    when(ip.getHost()).thenReturn("10.0.1.15");
    when(ip.getAdvertisedTo()).thenReturn(V1IPAddressInstanceSpec.AdvertisedToEnum.INTERNET);
    when(ip.getComponentInstanceName()).thenReturn("ci-app-1");
    return ip;
  }

  @Test
  void ipAddress_matchesNull_returnsFalse() {
    assertThat(new IPAddressInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void ipAddress_emptyFilter_matchesAll() {
    assertThat(new IPAddressInstanceFilter().matches(ip("ip-main"))).isTrue();
  }

  @Test
  void ipAddress_filterByName_match() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("main");
    f.setName(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isTrue();
  }

  @Test
  void ipAddress_filterByHost_match() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("10.0");
    f.setHost(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isTrue();
  }

  @Test
  void ipAddress_filterByHost_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("192.168");
    f.setHost(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isFalse();
  }

  @Test
  void ipAddress_filterByHost_nullHost_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("10.0");
    f.setHost(stringFilter);
    IPAddressInstance ip = ip("ip-main");
    when(ip.getHost()).thenReturn(null);
    assertThat(f.matches(ip)).isFalse();
  }

  @Test
  void ipAddress_filterByAdvertisedTo_match() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    f.setAdvertisedTo(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isTrue();
  }

  @Test
  void ipAddress_filterByAdvertisedTo_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("extranet");
    f.setAdvertisedTo(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isFalse();
  }

  @Test
  void ipAddress_filterByAdvertisedTo_null_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    f.setAdvertisedTo(stringFilter);
    IPAddressInstance ip = ip("ip-main");
    when(ip.getAdvertisedTo()).thenReturn(null);
    assertThat(f.matches(ip)).isFalse();
  }

  @Test
  void ipAddress_filterByComponentInstance_match() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("app-1");
    f.setComponentInstance(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isTrue();
  }

  @Test
  void ipAddress_filterByComponentInstance_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("db-1");
    f.setComponentInstance(stringFilter);
    assertThat(f.matches(ip("ip-main"))).isFalse();
  }

  @Test
  void ipAddress_filterByComponentInstance_nullName_noMatch() {
    IPAddressInstanceFilter f = new IPAddressInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("app-1");
    f.setComponentInstance(stringFilter);
    IPAddressInstance ip = ip("ip-main");
    when(ip.getComponentInstanceName()).thenReturn(null);
    assertThat(f.matches(ip)).isFalse();
  }

  // ---------- BlackListedURLFilter ----------

  private BlackListedURL url(String id, String name, String fqdn) {
    BlackListedURL url = mock(BlackListedURL.class);
    when(url.id()).thenReturn(id);
    when(url.getName()).thenReturn(name);
    when(url.getScope()).thenReturn(null);
    when(url.getFqdn()).thenReturn(fqdn);
    when(url.getTransportLayer()).thenReturn(List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP));
    when(url.getPorts()).thenReturn(List.of("443"));
    return url;
  }

  @Test
  void blackListedURL_matchesNull_returnsFalse() {
    assertThat(new BlackListedURLFilter().matches(null)).isFalse();
  }

  @Test
  void blackListedURL_emptyFilter_matchesAll() {
    assertThat(new BlackListedURLFilter().matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterById_match() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("id-1");
    f.setId(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterById_noMatch() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("id-99");
    f.setId(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isFalse();
  }

  @Test
  void blackListedURL_filterByName_match() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("block");
    f.setName(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterByName_noMatch() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("allow");
    f.setName(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isFalse();
  }

  @Test
  void blackListedURL_filterByFqdn_match() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("example");
    f.setFqdn(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterByFqdn_noMatch() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("malware");
    f.setFqdn(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isFalse();
  }

  @Test
  void blackListedURL_filterByTransportLayer_match() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Tcp");
    f.setTransportLayer(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterByTransportLayer_noMatch() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Udp");
    f.setTransportLayer(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isFalse();
  }

  @Test
  void blackListedURL_filterByPorts_match() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("443");
    f.setPorts(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isTrue();
  }

  @Test
  void blackListedURL_filterByPorts_noMatch() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("80");
    f.setPorts(sf);
    assertThat(f.matches(url("id-1", "block-ex", "example.com"))).isFalse();
  }

  @Test
  void blackListedURL_getters_returnSetValues() {
    BlackListedURLFilter f = new BlackListedURLFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("i");
    f.setId(idFilter);
    StringFilter nameFilter = new StringFilter();
    nameFilter.setLike("n");
    f.setName(nameFilter);
    StringFilter fqdnFilter = new StringFilter();
    fqdnFilter.setLike("f");
    f.setFqdn(fqdnFilter);
    StringFilter transportLayerFilter = new StringFilter();
    transportLayerFilter.setLike("t");
    f.setTransportLayer(transportLayerFilter);
    StringFilter portsFilter = new StringFilter();
    portsFilter.setLike("p");
    f.setPorts(portsFilter);
    assertThat(f.getId()).isEqualTo(idFilter);
    assertThat(f.getName()).isEqualTo(nameFilter);
    assertThat(f.getFqdn()).isEqualTo(fqdnFilter);
    assertThat(f.getTransportLayer()).isEqualTo(transportLayerFilter);
    assertThat(f.getPorts()).isEqualTo(portsFilter);
  }

  // ---------- BlackListedCIDRFilter ----------

  private BlackListedCIDR cidr(String id, String name, String cidrValue) {
    BlackListedCIDR c = mock(BlackListedCIDR.class);
    when(c.id()).thenReturn(id);
    when(c.getName()).thenReturn(name);
    when(c.getScope()).thenReturn(null);
    when(c.getCidr()).thenReturn(cidrValue);
    when(c.getTransportLayer()).thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP));
    when(c.getPorts()).thenReturn(List.of("443"));
    return c;
  }

  @Test
  void blackListedCIDR_matchesNull_returnsFalse() {
    assertThat(new BlackListedCIDRFilter().matches(null)).isFalse();
  }

  @Test
  void blackListedCIDR_emptyFilter_matchesAll() {
    assertThat(new BlackListedCIDRFilter().matches(cidr("id-1", "block-subnet", "10.0.0.0/8")))
        .isTrue();
  }

  @Test
  void blackListedCIDR_filterById_match() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("id-1");
    f.setId(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isTrue();
  }

  @Test
  void blackListedCIDR_filterById_noMatch() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("id-99");
    f.setId(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isFalse();
  }

  @Test
  void blackListedCIDR_filterByName_match() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("block");
    f.setName(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isTrue();
  }

  @Test
  void blackListedCIDR_filterByName_noMatch() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("allow");
    f.setName(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isFalse();
  }

  @Test
  void blackListedCIDR_filterByCidr_match() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("10.0");
    f.setCidr(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isTrue();
  }

  @Test
  void blackListedCIDR_filterByCidr_noMatch() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("192.168");
    f.setCidr(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isFalse();
  }

  @Test
  void blackListedCIDR_filterByTransportLayer_match() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Tcp");
    f.setTransportLayer(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isTrue();
  }

  @Test
  void blackListedCIDR_filterByTransportLayer_noMatch() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Udp");
    f.setTransportLayer(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isFalse();
  }

  @Test
  void blackListedCIDR_filterByPorts_match() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("443");
    f.setPorts(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isTrue();
  }

  @Test
  void blackListedCIDR_filterByPorts_noMatch() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("80");
    f.setPorts(sf);
    assertThat(f.matches(cidr("id-1", "block-subnet", "10.0.0.0/8"))).isFalse();
  }

  @Test
  void blackListedCIDR_getters_returnSetValues() {
    BlackListedCIDRFilter f = new BlackListedCIDRFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("i");
    f.setId(idFilter);
    StringFilter nameFilter = new StringFilter();
    nameFilter.setLike("n");
    f.setName(nameFilter);
    StringFilter cidrFilter = new StringFilter();
    cidrFilter.setLike("c");
    f.setCidr(cidrFilter);
    StringFilter transportLayerFilter = new StringFilter();
    transportLayerFilter.setLike("t");
    f.setTransportLayer(transportLayerFilter);
    StringFilter portsFilter = new StringFilter();
    portsFilter.setLike("p");
    f.setPorts(portsFilter);
    assertThat(f.getId()).isEqualTo(idFilter);
    assertThat(f.getName()).isEqualTo(nameFilter);
    assertThat(f.getCidr()).isEqualTo(cidrFilter);
    assertThat(f.getTransportLayer()).isEqualTo(transportLayerFilter);
    assertThat(f.getPorts()).isEqualTo(portsFilter);
  }

  // ---------- BlacklistFilter ----------

  private Blacklist blacklist(
      String id, String name, String sourceComponent, String destinationHostFqdn) {
    Blacklist b = mock(Blacklist.class);
    when(b.id()).thenReturn(id);
    when(b.getName()).thenReturn(name);
    when(b.getScope()).thenReturn(null);
    when(b.getSourceComponent()).thenReturn(sourceComponent);
    when(b.getDestinationHostFqdns()).thenReturn(List.of(destinationHostFqdn));
    when(b.getDestinationHostCidrs()).thenReturn(null);
    when(b.getSourceZones()).thenReturn(List.of("CSZ", "TNZ"));
    when(b.getSourceMacrophases()).thenReturn(List.of("TST", "PRD"));
    when(b.getSourceTargets()).thenReturn(List.of("prd-we"));
    when(b.getDestinationTransportProtocols()).thenReturn(List.of("Tcp"));
    when(b.getDestinationPortRanges()).thenReturn(List.of("443"));
    return b;
  }

  @Test
  void blacklist_matchesNull_returnsFalse() {
    assertThat(new BlacklistFilter().matches(null)).isFalse();
  }

  @Test
  void blacklist_emptyFilter_matchesAll() {
    assertThat(
            new BlacklistFilter().matches(blacklist("id-1", "block-ex", "my-app", "example.com")))
        .isTrue();
  }

  @Test
  void blacklist_filterById_match() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("id-1");
    f.setId(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterById_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("id-99");
    f.setId(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterByName_match() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("block");
    f.setName(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterByName_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("allow");
    f.setName(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterBySourceComponent_match() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("my-app");
    f.setSourceComponent(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterBySourceComponent_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("other-app");
    f.setSourceComponent(sf);
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterByDestinationHostFqdns_match() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationHostFqdns(List.of("example.com"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterByDestinationHostFqdns_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationHostFqdns(List.of("malware.example"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterBySourceZones_match() {
    BlacklistFilter f = new BlacklistFilter();
    f.setSourceZones(List.of("CSZ"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterBySourceZones_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    f.setSourceZones(List.of("CDE"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterByDestinationTransportProtocols_match() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationTransportProtocols(List.of("Tcp"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterByDestinationTransportProtocols_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationTransportProtocols(List.of("Udp"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }

  @Test
  void blacklist_filterByDestinationPortRanges_match() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationPortRanges(List.of("443"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isTrue();
  }

  @Test
  void blacklist_filterByDestinationPortRanges_noMatch() {
    BlacklistFilter f = new BlacklistFilter();
    f.setDestinationPortRanges(List.of("80"));
    assertThat(f.matches(blacklist("id-1", "block-ex", "my-app", "example.com"))).isFalse();
  }
}
