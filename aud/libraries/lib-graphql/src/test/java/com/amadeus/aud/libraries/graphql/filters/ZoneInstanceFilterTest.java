package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ZoneInstanceFilterTest {

  private ZoneInstance zi(String name) {
    ZoneInstance zi = mock(ZoneInstance.class);
    when(zi.id()).thenReturn("zi-" + name.hashCode());
    when(zi.getName()).thenReturn(name);
    when(zi.getLandingZoneName()).thenReturn("lz-ams");
    when(zi.getZoneTypeName()).thenReturn("dmz");
    when(zi.getFirewallGroupName()).thenReturn("fg-main");
    when(zi.isNsgNeeded()).thenReturn(true);

    SubnetInstance si = mock(SubnetInstance.class);
    when(si.getName()).thenReturn("subnet-a");
    when(zi.getSubnetInstances()).thenReturn(Set.of(si));

    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn("ci-app");
    when(zi.getComponentInstances()).thenReturn(Set.of(ci));

    return zi;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ZoneInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ZoneInstanceFilter().matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("main");
    f.setName(stringFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("backup");
    f.setName(stringFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterByLandingZoneName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter landingZoneNameFilter = new StringFilter();
    landingZoneNameFilter.setLike("lz-ams");
    f.setLandingZoneName(landingZoneNameFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByLandingZoneName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter landingZoneNameFilter = new StringFilter();
    landingZoneNameFilter.setLike("lz-par");
    f.setLandingZoneName(landingZoneNameFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterByLandingZoneName_nullLandingZone_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter landingZoneNameFilter = new StringFilter();
    landingZoneNameFilter.setLike("lz-ams");
    f.setLandingZoneName(landingZoneNameFilter);
    ZoneInstance zi = zi("zi-main");
    when(zi.getLandingZoneName()).thenReturn(null);
    assertThat(f.matches(zi)).isFalse();
  }

  @Test
  void filterByZoneTypeName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter zoneTypeNameFilter = new StringFilter();
    zoneTypeNameFilter.setLike("dmz");
    f.setZoneTypeName(zoneTypeNameFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByZoneTypeName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter zoneTypeNameFilter = new StringFilter();
    zoneTypeNameFilter.setLike("internet");
    f.setZoneTypeName(zoneTypeNameFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterByFirewallGroupName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter firewallGroupNameFilter = new StringFilter();
    firewallGroupNameFilter.setLike("fg-main");
    f.setFirewallGroupName(firewallGroupNameFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByFirewallGroupName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter firewallGroupNameFilter = new StringFilter();
    firewallGroupNameFilter.setLike("fg-other");
    f.setFirewallGroupName(firewallGroupNameFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterByIsNsgNeeded_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    f.setIsNsgNeeded(true);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByIsNsgNeeded_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    f.setIsNsgNeeded(false);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterBySubnetInstanceName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter subnetInstanceNameFilter = new StringFilter();
    subnetInstanceNameFilter.setLike("subnet-a");
    f.setSubnetInstanceName(subnetInstanceNameFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterBySubnetInstanceName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter subnetInstanceNameFilter = new StringFilter();
    subnetInstanceNameFilter.setLike("subnet-z");
    f.setSubnetInstanceName(subnetInstanceNameFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void filterByComponentInstanceName_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-app");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterByComponentInstanceName_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-db");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    StringFilter landingZoneNameFilter = new StringFilter();
    landingZoneNameFilter.setLike("lz");
    f.setLandingZoneName(landingZoneNameFilter);
    StringFilter zoneTypeNameFilter = new StringFilter();
    zoneTypeNameFilter.setLike("zt");
    f.setZoneTypeName(zoneTypeNameFilter);
    StringFilter firewallGroupNameFilter = new StringFilter();
    firewallGroupNameFilter.setLike("fg");
    f.setFirewallGroupName(firewallGroupNameFilter);
    f.setIsNsgNeeded(true);
    StringFilter subnetInstanceNameFilter = new StringFilter();
    subnetInstanceNameFilter.setLike("si");
    f.setSubnetInstanceName(subnetInstanceNameFilter);
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci");
    f.setComponentInstanceName(componentInstanceNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getLandingZoneName()).isEqualTo(landingZoneNameFilter);
    assertThat(f.getZoneTypeName()).isEqualTo(zoneTypeNameFilter);
    assertThat(f.getFirewallGroupName()).isEqualTo(firewallGroupNameFilter);
    assertThat(f.getIsNsgNeeded()).isTrue();
    assertThat(f.getSubnetInstanceName()).isEqualTo(subnetInstanceNameFilter);
    assertThat(f.getComponentInstanceName()).isEqualTo(componentInstanceNameFilter);
  }

  @Test
  void filterById_match() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("zi-");
    f.setId(idFilter);
    assertThat(f.matches(zi("zi-main"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ZoneInstanceFilter f = new ZoneInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(zi("zi-main"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ZoneInstanceFilter f = new ZoneInstanceFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
