package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FirewallGroupFilterTest {

  private FirewallGroup fg(String name) {
    FirewallGroup fg = mock(FirewallGroup.class);
    when(fg.getName()).thenReturn(name);
    when(fg.id()).thenReturn("fg-" + name.hashCode());
    when(fg.getFirewallVendorName()).thenReturn("palo-alto");
    when(fg.getTargetName()).thenReturn("target-ams");

    ZoneInstance zi = mock(ZoneInstance.class);
    when(zi.getName()).thenReturn("zi-dmz");
    when(fg.getZoneInstances()).thenReturn(Set.of(zi));
    return fg;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new FirewallGroupFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new FirewallGroupFilter().matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterByName_match() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("main");
    f.setName(stringFilter);
    assertThat(f.matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("backup");
    f.setName(stringFilter);
    assertThat(f.matches(fg("fg-main"))).isFalse();
  }

  @Test
  void filterByFirewallVendor_match() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter firewallVendorFilter = new StringFilter();
    firewallVendorFilter.setLike("palo-alto");
    f.setFirewallVendor(firewallVendorFilter);
    assertThat(f.matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterByFirewallVendor_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter firewallVendorFilter = new StringFilter();
    firewallVendorFilter.setLike("cisco");
    f.setFirewallVendor(firewallVendorFilter);
    assertThat(f.matches(fg("fg-main"))).isFalse();
  }

  @Test
  void filterByTarget_match() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter targetFilter = new StringFilter();
    targetFilter.setLike("target-ams");
    f.setTarget(targetFilter);
    assertThat(f.matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterByTarget_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter targetFilter = new StringFilter();
    targetFilter.setLike("target-nyc");
    f.setTarget(targetFilter);
    assertThat(f.matches(fg("fg-main"))).isFalse();
  }

  @Test
  void filterByZoneInstanceName_match() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi-dmz");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    assertThat(f.matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterByZoneInstanceName_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi-internet");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    assertThat(f.matches(fg("fg-main"))).isFalse();
  }

  @Test
  void filterByZoneInstanceName_nullZoneInstances_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi-dmz");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    FirewallGroup fg = fg("fg-main");
    when(fg.getZoneInstances()).thenReturn(null);
    assertThat(f.matches(fg)).isFalse();
  }

  @Test
  void filterById_match() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("fg-");
    f.setId(idFilter);
    assertThat(f.matches(fg("fg-main"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    FirewallGroupFilter f = new FirewallGroupFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(fg("fg-main"))).isFalse();
  }
}

class FirewallVendorFilterTest {

  private FirewallVendor fv(String name) {
    FirewallGroup fg = mock(FirewallGroup.class);
    when(fg.getName()).thenReturn("fg-main");
    when(fg.getFirewallVendorName()).thenReturn(name);
    when(fg.getTargetName()).thenReturn("target-ams");
    when(fg.getZoneInstances()).thenReturn(Set.of());

    FirewallVendor fv = mock(FirewallVendor.class);
    when(fv.getName()).thenReturn(name);
    when(fv.id()).thenReturn("fv-" + name.hashCode());
    when(fv.getFirewallGroups()).thenReturn(Set.of(fg));
    return fv;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new FirewallVendorFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new FirewallVendorFilter().matches(fv("palo-alto"))).isTrue();
  }

  @Test
  void filterByName_match() {
    FirewallVendorFilter f = new FirewallVendorFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("palo");
    f.setName(stringFilter);
    assertThat(f.matches(fv("palo-alto"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    FirewallVendorFilter f = new FirewallVendorFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("cisco");
    f.setName(stringFilter);
    assertThat(f.matches(fv("palo-alto"))).isFalse();
  }

  @Test
  void filterByFirewallGroups_emptyList_matchesAll() {
    FirewallVendorFilter f = new FirewallVendorFilter();
    f.setFirewallGroups(List.of());
    assertThat(f.matches(fv("palo-alto"))).isTrue();
  }

  @Test
  void filterByFirewallGroups_match() {
    FirewallGroupFilter fgf = new FirewallGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("fg-main");
    fgf.setName(stringFilter);
    FirewallVendorFilter f = new FirewallVendorFilter();
    f.setFirewallGroups(List.of(fgf));
    assertThat(f.matches(fv("palo-alto"))).isTrue();
  }

  @Test
  void filterByFirewallGroups_noMatch() {
    FirewallGroupFilter fgf = new FirewallGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("fg-backup");
    fgf.setName(stringFilter);
    FirewallVendorFilter f = new FirewallVendorFilter();
    f.setFirewallGroups(List.of(fgf));
    assertThat(f.matches(fv("palo-alto"))).isFalse();
  }

  @Test
  void filterByFirewallGroups_nullGroups_noMatch() {
    FirewallGroupFilter fgf = new FirewallGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("fg-main");
    fgf.setName(stringFilter);
    FirewallVendorFilter f = new FirewallVendorFilter();
    f.setFirewallGroups(List.of(fgf));
    FirewallVendor fv = mock(FirewallVendor.class);
    when(fv.getName()).thenReturn("palo-alto");
    when(fv.getFirewallGroups()).thenReturn(null);
    assertThat(f.matches(fv)).isFalse();
  }

  @Test
  void firewallGroup_id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    FirewallGroupFilter f = new FirewallGroupFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void firewallVendor_id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("456");

    FirewallVendorFilter f = new FirewallVendorFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    FirewallVendorFilter f = new FirewallVendorFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("fv-");
    f.setId(idFilter);
    assertThat(f.matches(fv("palo-alto"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    FirewallVendorFilter f = new FirewallVendorFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(fv("palo-alto"))).isFalse();
  }
}
