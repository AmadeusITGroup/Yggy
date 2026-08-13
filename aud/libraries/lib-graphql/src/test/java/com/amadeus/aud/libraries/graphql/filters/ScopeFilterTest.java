package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ScopeFilterTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private Scope scope(String name) {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn(name);
    when(s.id()).thenReturn("scope-" + name.hashCode());
    when(s.getServiceNowId()).thenReturn("RITM0001");
    when(s.getNanoId()).thenReturn("nano-01");
    when(s.getAssigneeGroup()).thenReturn("team-ops");
    when(s.getRepositoryVersion()).thenReturn("main");
    when(s.isDeleted()).thenReturn(false);
    when(s.getOwners()).thenReturn(Set.of("owner-alice"));
    when(s.getRolesJsonRepresentation()).thenReturn(Set.of("role-admin"));

    Target t = mock(Target.class);
    when(t.getName()).thenReturn("tgt-prod");
    when(s.getTargets()).thenReturn(Set.of(t));

    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(s.getLandingZones()).thenReturn(Set.of(lz));

    Component comp = mock(Component.class);
    when(comp.getName()).thenReturn("comp-api");
    when(s.getComponents()).thenReturn(Set.of(comp));

    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn("ci-app");
    when(s.getComponentInstances()).thenReturn(Set.of(ci));

    ZoneInstance zi = mock(ZoneInstance.class);
    when(zi.getName()).thenReturn("zi-dmz");
    when(s.getZoneInstances()).thenReturn(Set.of(zi));

    SubnetInstance si = mock(SubnetInstance.class);
    when(si.getName()).thenReturn("subnet-a");
    when(s.getSubnetInstances()).thenReturn(Set.of(si));

    return s;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ScopeFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ScopeFilter().matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ScopeFilter f = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    f.setName(stringFilter);
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByServiceNowId_match() {
    ScopeFilter f = new ScopeFilter();
    f.setServiceNowId(sf("RITM0001"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByServiceNowId_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setServiceNowId(sf("RITM0002"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByNanoId_match() {
    ScopeFilter f = new ScopeFilter();
    f.setNanoId(sf("NANO-01"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByNanoId_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setNanoId(sf("nano-99"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByAssigneeGroup_match() {
    ScopeFilter f = new ScopeFilter();
    f.setAssigneeGroup(sf("TEAM-OPS"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByAssigneeGroup_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setAssigneeGroup(sf("team-dev"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByRepositoryVersion_match() {
    ScopeFilter f = new ScopeFilter();
    f.setRepositoryVersion(sf("main"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByRepositoryVersion_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setRepositoryVersion(sf("develop"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByIsDeleted_match() {
    ScopeFilter f = new ScopeFilter();
    f.setIsDeleted(false);
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByIsDeleted_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setIsDeleted(true);
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByOwner_match() {
    ScopeFilter f = new ScopeFilter();
    f.setOwner(sf("OWNER-ALICE"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByOwner_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setOwner(sf("owner-bob"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByOwner_nullOwners_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getOwners()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setOwner(sf("owner-alice"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByRole_match() {
    ScopeFilter f = new ScopeFilter();
    f.setRole(sf("ROLE-ADMIN"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByRole_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setRole(sf("role-viewer"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByRole_nullRoles_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getRolesJsonRepresentation()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setRole(sf("role-admin"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByTargetName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setTargetName(sf("TGT-PROD"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByTargetName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setTargetName(sf("tgt-dev"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByTargetName_nullTargets_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getTargets()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setTargetName(sf("tgt-prod"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByLandingZoneName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setLandingZoneName(sf("LZ-AMS"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByLandingZoneName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setLandingZoneName(sf("lz-lon"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByLandingZoneName_nullLandingZones_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getLandingZones()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setLandingZoneName(sf("lz-ams"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByComponentName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setComponentName(sf("COMP-API"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByComponentName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setComponentName(sf("comp-ui"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByComponentName_nullComponents_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getComponents()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setComponentName(sf("comp-api"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByComponentInstanceName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setComponentInstanceName(sf("CI-APP"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByComponentInstanceName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setComponentInstanceName(sf("ci-db"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByComponentInstanceName_nullInstances_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getComponentInstances()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setComponentInstanceName(sf("ci-app"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterByZoneInstanceName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setZoneInstanceName(sf("ZI-DMZ"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterByZoneInstanceName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setZoneInstanceName(sf("zi-lan"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterByZoneInstanceName_nullZoneInstances_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getZoneInstances()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setZoneInstanceName(sf("zi-dmz"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void filterBySubnetInstanceName_match() {
    ScopeFilter f = new ScopeFilter();
    f.setSubnetInstanceName(sf("SUBNET-A"));
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterBySubnetInstanceName_noMatch() {
    ScopeFilter f = new ScopeFilter();
    f.setSubnetInstanceName(sf("subnet-z"));
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }

  @Test
  void filterBySubnetInstanceName_nullSubnets_noMatch() {
    Scope s = mock(Scope.class);
    when(s.getName()).thenReturn("scope-1");
    when(s.getSubnetInstances()).thenReturn(null);

    ScopeFilter f = new ScopeFilter();
    f.setSubnetInstanceName(sf("subnet-a"));
    assertThat(f.matches(s)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    ScopeFilter f = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    f.setServiceNowId(sf("sn"));
    f.setNanoId(sf("ni"));
    f.setAssigneeGroup(sf("ag"));
    f.setRepositoryVersion(sf("v"));
    f.setIsDeleted(true);
    f.setOwner(sf("o"));
    f.setRole(sf("r"));
    f.setTargetName(sf("t"));
    f.setLandingZoneName(sf("lz"));
    f.setComponentName(sf("c"));
    f.setComponentInstanceName(sf("ci"));
    f.setZoneInstanceName(sf("zi"));
    f.setSubnetInstanceName(sf("si"));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getServiceNowId()).isNotNull();
    assertThat(f.getNanoId()).isNotNull();
    assertThat(f.getAssigneeGroup()).isNotNull();
    assertThat(f.getRepositoryVersion()).isNotNull();
    assertThat(f.getIsDeleted()).isTrue();
    assertThat(f.getOwner()).isNotNull();
    assertThat(f.getRole()).isNotNull();
    assertThat(f.getTargetName()).isNotNull();
    assertThat(f.getLandingZoneName()).isNotNull();
    assertThat(f.getComponentName()).isNotNull();
    assertThat(f.getComponentInstanceName()).isNotNull();
    assertThat(f.getZoneInstanceName()).isNotNull();
    assertThat(f.getSubnetInstanceName()).isNotNull();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ScopeFilter f = new ScopeFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    ScopeFilter f = new ScopeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("scope-");
    f.setId(idFilter);
    assertThat(f.matches(scope("scope-prod"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ScopeFilter f = new ScopeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(scope("scope-prod"))).isFalse();
  }
}
