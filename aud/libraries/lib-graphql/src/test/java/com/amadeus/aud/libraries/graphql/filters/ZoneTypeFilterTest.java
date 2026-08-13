package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ZoneTypeFilterTest {

  private ZoneType zt(String name) {
    ZoneType zt = mock(ZoneType.class);
    when(zt.id()).thenReturn("zt-" + name.hashCode());
    when(zt.getName()).thenReturn(name);
    when(zt.isTechnical()).thenReturn(false);
    when(zt.isReal()).thenReturn(true);
    when(zt.isPaas()).thenReturn(false);

    Component comp = mock(Component.class);
    when(comp.getName()).thenReturn("comp-web");
    when(zt.getComponent()).thenReturn(comp);

    Connection conn = mock(Connection.class);
    when(conn.getName()).thenReturn("conn-internal");
    when(zt.getConnections()).thenReturn(Set.of(conn));

    ZoneInstance zi = mock(ZoneInstance.class);
    when(zi.getName()).thenReturn("zi-dmz");
    when(zt.getZoneInstances()).thenReturn(Set.of(zi));

    com.amadeus.aud.libraries.models.core.db.ComponentInstance ci =
        mock(com.amadeus.aud.libraries.models.core.db.ComponentInstance.class);
    when(ci.getName()).thenReturn("ci-app-1");
    when(zt.getComponentInstances()).thenReturn(Set.of(ci));

    return zt;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ZoneTypeFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ZoneTypeFilter().matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dmz");
    f.setName(stringFilter);
    assertThat(f.matches(zt("dmz-zone"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    f.setName(stringFilter);
    assertThat(f.matches(zt("dmz-zone"))).isFalse();
  }

  @Test
  void filterByIsTechnical_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setIsTechnical(false);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByIsTechnical_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setIsTechnical(true);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void filterByIsReal_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setIsReal(true);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByIsReal_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setIsReal(false);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void filterByIsPaas_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setIsPaas(true);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void filterByComponentName_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("comp-web");
    f.setComponentName(componentNameFilter);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByComponentName_nullComponent_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("comp-web");
    f.setComponentName(componentNameFilter);
    ZoneType zt = zt("dmz");
    when(zt.getComponent()).thenReturn(null);
    assertThat(f.matches(zt)).isFalse();
  }

  @Test
  void filterByConnectionName_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter connectionNameFilter = new StringFilter();
    connectionNameFilter.setLike("conn-internal");
    f.setConnectionName(connectionNameFilter);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByConnectionName_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter connectionNameFilter = new StringFilter();
    connectionNameFilter.setLike("conn-external");
    f.setConnectionName(connectionNameFilter);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void filterByZoneInstanceName_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi-dmz");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByZoneInstanceName_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi-internet");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void filterByComponentInstanceName_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-app-1");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterByComponentInstanceName_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-db-1");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    f.setIsTechnical(true);
    f.setIsReal(false);
    f.setIsPaas(true);
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("c");
    f.setComponentName(componentNameFilter);
    StringFilter connectionNameFilter = new StringFilter();
    connectionNameFilter.setLike("cn");
    f.setConnectionName(connectionNameFilter);
    StringFilter zoneInstanceNameFilter = new StringFilter();
    zoneInstanceNameFilter.setLike("zi");
    f.setZoneInstanceName(zoneInstanceNameFilter);
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci");
    f.setComponentInstanceName(componentInstanceNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getIsTechnical()).isTrue();
    assertThat(f.getIsReal()).isFalse();
    assertThat(f.getIsPaas()).isTrue();
    assertThat(f.getComponentName()).isEqualTo(componentNameFilter);
    assertThat(f.getConnectionName()).isEqualTo(connectionNameFilter);
    assertThat(f.getZoneInstanceName()).isEqualTo(zoneInstanceNameFilter);
    assertThat(f.getComponentInstanceName()).isEqualTo(componentInstanceNameFilter);
  }

  @Test
  void filterById_match() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("zt-");
    f.setId(idFilter);
    assertThat(f.matches(zt("dmz"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ZoneTypeFilter f = new ZoneTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(zt("dmz"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ZoneTypeFilter f = new ZoneTypeFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
