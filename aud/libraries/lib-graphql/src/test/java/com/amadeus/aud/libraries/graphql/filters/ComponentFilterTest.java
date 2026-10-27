package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ComponentFilterTest {

  // Helper method to create StringFilter with eq value
  private StringFilter eq(String value) {
    StringFilter sf = new StringFilter();
    sf.setEq(value);
    return sf;
  }

  private Component comp(String name) {
    Component c = mock(Component.class);
    when(c.id()).thenReturn("comp-" + name.hashCode());
    when(c.getName()).thenReturn(name);
    when(c.isPublic()).thenReturn(true);
    when(c.isPrivate()).thenReturn(false);
    when(c.isHosting()).thenReturn(false);
    when(c.isHosted()).thenReturn(false);
    when(c.isTechnical()).thenReturn(false);
    when(c.isContainer()).thenReturn(false);
    when(c.isInterface()).thenReturn(false);
    when(c.getSecurityZones()).thenReturn(Set.of("sz-internet"));
    when(c.getServiceNowIds()).thenReturn(Set.of("SN-001"));

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("scope-prod");
    when(scope.getServiceNowId()).thenReturn(null);
    when(scope.getRepositoryVersion()).thenReturn(null);
    when(scope.isDeleted()).thenReturn(false);
    when(scope.getRolesJsonRepresentation()).thenReturn(null);
    when(scope.getTargets()).thenReturn(null);
    when(scope.getLandingZones()).thenReturn(null);
    when(scope.getComponents()).thenReturn(null);
    when(scope.getComponentInstances()).thenReturn(null);
    when(scope.getZoneInstances()).thenReturn(null);
    when(scope.getSubnetInstances()).thenReturn(null);
    when(c.getScope()).thenReturn(scope);

    ZoneType zt = mock(ZoneType.class);
    when(zt.getName()).thenReturn("dmz");
    when(zt.isTechnical()).thenReturn(false);
    when(zt.isReal()).thenReturn(true);
    when(zt.isPaas()).thenReturn(false);
    when(zt.getComponent()).thenReturn(null);
    when(zt.getConnections()).thenReturn(null);
    when(zt.getZoneInstances()).thenReturn(null);
    when(zt.getComponentInstances()).thenReturn(null);
    when(c.getZoneTypes()).thenReturn(Set.of(zt));

    Component parent = mock(Component.class);
    when(parent.getName()).thenReturn("parent-comp");
    when(c.getParentsNames()).thenReturn(Set.of("parent-comp"));

    Component child = mock(Component.class);
    when(child.getName()).thenReturn("child-comp");
    when(c.getChildren()).thenReturn(Set.of(child));

    return c;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ComponentFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ComponentFilter().matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ComponentFilter f = new ComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("api");
    f.setName(stringFilter);
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ComponentFilter f = new ComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByScope_match() {
    ScopeFilter sf = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    sf.setName(stringFilter);

    ComponentFilter f = new ComponentFilter();
    f.setScope(sf);
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByScope_noMatch() {
    ScopeFilter sf = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dev");
    sf.setName(stringFilter);

    ComponentFilter f = new ComponentFilter();
    f.setScope(sf);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByDeprecatedStatus_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setDeprecatedStatus(eq("Error"));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByDeprecatedStatus_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getDeprecatedStatus()).thenReturn(null);

    ComponentFilter f = new ComponentFilter();
    f.setDeprecatedStatus(eq("Warning"));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByIsPublic_match() {
    ComponentFilter f = new ComponentFilter();
    f.setIsPublic(true);
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByIsPublic_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsPublic(false);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsPrivate_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsPrivate(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsHosting_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsHosting(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsHosted_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsHosted(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsTechnical_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsTechnical(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsContainer_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsContainer(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByIsInterface_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setIsInterface(true);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterBySecurityZone_match() {
    ComponentFilter f = new ComponentFilter();
    f.setSecurityZone(eq("sz-internet"));
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterBySecurityZone_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setSecurityZone(eq("sz-dmz"));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterBySecurityZone_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getSecurityZones()).thenReturn(null);

    ComponentFilter f = new ComponentFilter();
    f.setSecurityZone(eq("sz-internet"));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByServiceNowId_match() {
    ComponentFilter f = new ComponentFilter();
    f.setServiceNowId(eq("SN-001"));
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByServiceNowId_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setServiceNowId(eq("SN-999"));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByServiceNowId_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getServiceNowIds()).thenReturn(null);

    ComponentFilter f = new ComponentFilter();
    f.setServiceNowId(eq("SN-001"));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByZoneTypes_match() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dmz");
    ztf.setName(stringFilter);

    ComponentFilter f = new ComponentFilter();
    f.setZoneTypes(Set.of(ztf));
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByZoneTypes_noMatch() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    ztf.setName(stringFilter);

    ComponentFilter f = new ComponentFilter();
    f.setZoneTypes(Set.of(ztf));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByZoneTypes_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getZoneTypes()).thenReturn(null);

    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dmz");
    ztf.setName(stringFilter);

    ComponentFilter f = new ComponentFilter();
    f.setZoneTypes(Set.of(ztf));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByParentName_match() {
    ComponentFilter f = new ComponentFilter();
    f.setParentName(eq("parent-comp"));
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByParentName_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setParentName(eq("other-parent"));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByParentName_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getParentsNames()).thenReturn(null);

    ComponentFilter f = new ComponentFilter();
    f.setParentName(eq("parent-comp"));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByChildName_match() {
    ComponentFilter f = new ComponentFilter();
    f.setChildName(eq("child-comp"));
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterByChildName_noMatch() {
    ComponentFilter f = new ComponentFilter();
    f.setChildName(eq("other-child"));
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void filterByChildName_nullOnEntity_noMatch() {
    Component c = mock(Component.class);
    when(c.getName()).thenReturn("comp-1");
    when(c.getChildren()).thenReturn(null);

    ComponentFilter f = new ComponentFilter();
    f.setChildName(eq("child-comp"));
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ScopeFilter sf = new ScopeFilter();
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter deprecatedStatusFilter = eq("Warning");
    StringFilter securityZoneFilter = eq("sz");
    StringFilter serviceNowIdFilter = eq("sn");
    StringFilter parentNameFilter = eq("p");
    StringFilter childNameFilter = eq("c");

    ComponentFilter f = new ComponentFilter();
    f.setName(stringFilter);
    f.setScope(sf);
    f.setDeprecatedStatus(deprecatedStatusFilter);
    f.setIsPublic(true);
    f.setIsPrivate(false);
    f.setIsHosting(true);
    f.setIsHosted(false);
    f.setIsTechnical(true);
    f.setIsContainer(false);
    f.setIsInterface(true);
    f.setSecurityZone(securityZoneFilter);
    f.setServiceNowId(serviceNowIdFilter);
    f.setZoneTypes(Set.of(ztf));
    f.setParentName(parentNameFilter);
    f.setChildName(childNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getScope()).isEqualTo(sf);
    assertThat(f.getDeprecatedStatus()).isEqualTo(deprecatedStatusFilter);
    assertThat(f.getIsPublic()).isTrue();
    assertThat(f.getIsPrivate()).isFalse();
    assertThat(f.getIsHosting()).isTrue();
    assertThat(f.getIsHosted()).isFalse();
    assertThat(f.getIsTechnical()).isTrue();
    assertThat(f.getIsContainer()).isFalse();
    assertThat(f.getIsInterface()).isTrue();
    assertThat(f.getSecurityZone()).isEqualTo(securityZoneFilter);
    assertThat(f.getServiceNowId()).isEqualTo(serviceNowIdFilter);
    assertThat(f.getZoneTypes()).hasSameElementsAs(Set.of(ztf));
    assertThat(f.getParentName()).isEqualTo(parentNameFilter);
    assertThat(f.getChildName()).isEqualTo(childNameFilter);
  }

  @Test
  void filterById_match() {
    ComponentFilter f = new ComponentFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("comp-");
    f.setId(idFilter);
    assertThat(f.matches(comp("comp-api"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ComponentFilter f = new ComponentFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(comp("comp-api"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ComponentFilter f = new ComponentFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
