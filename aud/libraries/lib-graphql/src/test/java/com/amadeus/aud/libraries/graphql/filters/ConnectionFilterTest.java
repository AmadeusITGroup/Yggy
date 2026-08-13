package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import org.junit.jupiter.api.Test;

class ConnectionFilterTest {

  private Connection conn(String name) {
    Connection c = mock(Connection.class);
    when(c.getName()).thenReturn(name);
    when(c.id()).thenReturn("conn-" + name.hashCode());
    when(c.getFirewallLevel()).thenReturn(V1ConnectionSpec.LevelEnum.L7);
    when(c.getSecurityControl()).thenReturn(V1ConnectionSpec.ControlEnum.GREEN);
    when(c.getConnectionType()).thenReturn(V1ConnectionSpec.TypeEnum.INTERNAL);

    ZoneType src = mock(ZoneType.class);
    when(src.getName()).thenReturn("dmz");
    when(c.getSourceZoneType()).thenReturn(src);

    ZoneType dst = mock(ZoneType.class);
    when(dst.getName()).thenReturn("lan");
    when(c.getDestinationZoneType()).thenReturn(dst);

    return c;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ConnectionFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ConnectionFilter().matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("conn");
    f.setName(stringFilter);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void filterByFirewallLevel_match() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter firewallLevelFilter = new StringFilter();
    firewallLevelFilter.setLike("L7");
    f.setFirewallLevel(firewallLevelFilter);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterByFirewallLevel_noMatch() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter firewallLevelFilter = new StringFilter();
    firewallLevelFilter.setLike("L4");
    f.setFirewallLevel(firewallLevelFilter);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void filterByFirewallLevel_nullOnEntity_noMatch() {
    Connection c = mock(Connection.class);
    when(c.getName()).thenReturn("conn-1");
    when(c.getFirewallLevel()).thenReturn(null);

    ConnectionFilter f = new ConnectionFilter();
    StringFilter firewallLevelFilter = new StringFilter();
    firewallLevelFilter.setLike("L7");
    f.setFirewallLevel(firewallLevelFilter);
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterBySecurityControl_match() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter securityControlFilter = new StringFilter();
    securityControlFilter.setLike("green");
    f.setSecurityControl(securityControlFilter);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterBySecurityControl_noMatch() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter securityControlFilter = new StringFilter();
    securityControlFilter.setLike("amber");
    f.setSecurityControl(securityControlFilter);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void filterBySecurityControl_nullOnEntity_noMatch() {
    Connection c = mock(Connection.class);
    when(c.getName()).thenReturn("conn-1");
    when(c.getSecurityControl()).thenReturn(null);

    ConnectionFilter f = new ConnectionFilter();
    StringFilter securityControlFilter = new StringFilter();
    securityControlFilter.setLike("green");
    f.setSecurityControl(securityControlFilter);
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByConnectionType_match() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter connectionTypeFilter = new StringFilter();
    connectionTypeFilter.setLike("Internal");
    f.setConnectionType(connectionTypeFilter);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterByConnectionType_noMatch() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter connectionTypeFilter = new StringFilter();
    connectionTypeFilter.setLike("External");
    f.setConnectionType(connectionTypeFilter);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void filterByConnectionType_nullOnEntity_noMatch() {
    Connection c = mock(Connection.class);
    when(c.getName()).thenReturn("conn-1");
    when(c.getConnectionType()).thenReturn(null);

    ConnectionFilter f = new ConnectionFilter();
    StringFilter connectionTypeFilter = new StringFilter();
    connectionTypeFilter.setLike("Internal");
    f.setConnectionType(connectionTypeFilter);
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterBySourceZoneType_match() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dmz");
    ztf.setName(stringFilter);

    ConnectionFilter f = new ConnectionFilter();
    f.setSourceZoneType(ztf);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterBySourceZoneType_noMatch() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    ztf.setName(stringFilter);

    ConnectionFilter f = new ConnectionFilter();
    f.setSourceZoneType(ztf);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void filterByDestinationZoneType_match() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lan");
    ztf.setName(stringFilter);

    ConnectionFilter f = new ConnectionFilter();
    f.setDestinationZoneType(ztf);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterByDestinationZoneType_noMatch() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    ztf.setName(stringFilter);

    ConnectionFilter f = new ConnectionFilter();
    f.setDestinationZoneType(ztf);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ZoneTypeFilter src = new ZoneTypeFilter();
    ZoneTypeFilter dst = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter firewallLevelFilter = new StringFilter();
    firewallLevelFilter.setLike("L7");
    StringFilter securityControlFilter = new StringFilter();
    securityControlFilter.setLike("green");
    StringFilter connectionTypeFilter = new StringFilter();
    connectionTypeFilter.setLike("Internal");

    ConnectionFilter f = new ConnectionFilter();
    f.setName(stringFilter);
    f.setFirewallLevel(firewallLevelFilter);
    f.setSecurityControl(securityControlFilter);
    f.setConnectionType(connectionTypeFilter);
    f.setSourceZoneType(src);
    f.setDestinationZoneType(dst);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getFirewallLevel()).isEqualTo(firewallLevelFilter);
    assertThat(f.getSecurityControl()).isEqualTo(securityControlFilter);
    assertThat(f.getConnectionType()).isEqualTo(connectionTypeFilter);
    assertThat(f.getSourceZoneType()).isSameAs(src);
    assertThat(f.getDestinationZoneType()).isSameAs(dst);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ConnectionFilter f = new ConnectionFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("conn-");
    f.setId(idFilter);
    assertThat(f.matches(conn("conn-1"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ConnectionFilter f = new ConnectionFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(conn("conn-1"))).isFalse();
  }
}
