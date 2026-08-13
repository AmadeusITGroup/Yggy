package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AutomaticTemplateInstanceFilterTest {

  // ─── helpers ────────────────────────────────────────────────────────────────

  private AutomaticTemplateInstance ati(String name) {
    AutomaticTemplateInstance ati = mock(AutomaticTemplateInstance.class);
    when(ati.id()).thenReturn("ati-" + name.hashCode());
    when(ati.getName()).thenReturn(name);
    when(ati.getComponentName()).thenReturn("comp-api");

    // landing zone
    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(lz.getTarget()).thenReturn(null);
    when(lz.getRegionName()).thenReturn(null);
    when(lz.getLandingZoneType()).thenReturn(null);
    when(lz.getMacroPhase()).thenReturn(null);
    when(lz.getCidrs()).thenReturn(null);
    when(lz.getLandingZoneFunctionalTypes()).thenReturn(null);
    when(lz.getZoneInstances()).thenReturn(null);
    when(lz.getSubnetInstances()).thenReturn(null);
    when(lz.getComponentInstances()).thenReturn(null);
    when(ati.getLandingZone()).thenReturn(lz);

    // component instances
    ComponentInstance ci = componentInstance("ci-app");
    when(ati.getComponentInstances()).thenReturn(Set.of(ci));

    return ati;
  }

  private ComponentInstance componentInstance(String name) {
    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn(name);

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("scope-prod");
    when(scope.getServiceNowId()).thenReturn(null);
    when(scope.getNanoId()).thenReturn(null);
    when(scope.getAssigneeGroup()).thenReturn(null);
    when(scope.getRepositoryVersion()).thenReturn(null);
    when(scope.isDeleted()).thenReturn(false);
    when(scope.getOwners()).thenReturn(null);
    when(scope.getRolesJsonRepresentation()).thenReturn(null);
    when(scope.getTargets()).thenReturn(null);
    when(scope.getLandingZones()).thenReturn(null);
    when(scope.getComponents()).thenReturn(null);
    when(scope.getComponentInstances()).thenReturn(null);
    when(scope.getZoneInstances()).thenReturn(null);
    when(scope.getSubnetInstances()).thenReturn(null);

    Component component = mock(Component.class);
    when(component.getName()).thenReturn("comp-api");
    when(component.getScope()).thenReturn(scope);
    when(ci.getComponent()).thenReturn(component);

    ZoneType zt = mock(ZoneType.class);
    when(zt.getName()).thenReturn("dmz");
    when(zt.isTechnical()).thenReturn(false);
    when(zt.isReal()).thenReturn(true);
    when(zt.isPaas()).thenReturn(false);
    when(zt.getComponent()).thenReturn(null);
    when(zt.getConnections()).thenReturn(null);
    when(zt.getZoneInstances()).thenReturn(null);
    when(zt.getComponentInstances()).thenReturn(null);
    when(ci.getZoneType()).thenReturn(zt);

    when(ci.getApplicationPhaseName()).thenReturn("phase-prod");

    Namespace ns = mock(Namespace.class);
    when(ns.getName()).thenReturn("ns-default");
    when(ns.isShared()).thenReturn(false);
    when(ns.isAutomaticallyGenerated()).thenReturn(false);
    when(ns.getAuthorizedScopes()).thenReturn(null);
    when(ns.getComponentInstances()).thenReturn(null);
    when(ci.getNamespace()).thenReturn(ns);

    when(ci.getServiceTagName()).thenReturn("tag-svc");
    when(ci.getDeployComponentInstanceName()).thenReturn("deploy-ci");
    when(ci.getInSubnetInstanceName()).thenReturn("subnet-in");
    when(ci.getOutSubnetInstanceName()).thenReturn("subnet-out");
    when(ci.getInZoneInstance()).thenReturn(null);
    when(ci.getOutZoneInstance()).thenReturn(null);
    when(ci.getEndpointType()).thenReturn(null);
    when(ci.getDeprecatedStatus()).thenReturn(null);
    when(ci.getHostingPlatformType()).thenReturn(null);
    when(ci.getK8sIdentifier()).thenReturn("k8s-id");
    when(ci.isAutomaticallyInstantiated()).thenReturn(false);
    when(ci.getHostingPlatformName()).thenReturn(null);
    when(ci.getAutomaticTemplateInstances()).thenReturn(null);

    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(lz.getTarget()).thenReturn(null);
    when(lz.getRegionName()).thenReturn(null);
    when(lz.getLandingZoneType()).thenReturn(null);
    when(lz.getMacroPhase()).thenReturn(null);
    when(lz.getCidrs()).thenReturn(null);
    when(lz.getLandingZoneFunctionalTypes()).thenReturn(null);
    when(lz.getZoneInstances()).thenReturn(null);
    when(lz.getSubnetInstances()).thenReturn(null);
    when(lz.getComponentInstances()).thenReturn(null);
    when(ci.getLandingZone()).thenReturn(lz);

    return ci;
  }

  // ─── null / empty ────────────────────────────────────────────────────────────

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new AutomaticTemplateInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new AutomaticTemplateInstanceFilter().matches(ati("ati-main"))).isTrue();
  }

  // ─── name ────────────────────────────────────────────────────────────────────

  @Test
  void filterByName_match() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("main");
    f.setName(stringFilter);
    assertThat(f.matches(ati("ati-main"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(ati("ati-main"))).isFalse();
  }

  // ─── componentName ───────────────────────────────────────────────────────────

  @Test
  void filterByComponentName_match() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("comp-api");
    f.setComponentName(componentNameFilter);
    assertThat(f.matches(ati("ati-main"))).isTrue();
  }

  @Test
  void filterByComponentName_noMatch() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("other");
    f.setComponentName(componentNameFilter);
    assertThat(f.matches(ati("ati-main"))).isFalse();
  }

  // ─── landingZone (nested) ────────────────────────────────────────────────────

  @Test
  void filterByLandingZone_match() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lz-ams");
    lzf.setName(stringFilter);
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setLandingZone(lzf);
    assertThat(f.matches(ati("ati-main"))).isTrue();
  }

  @Test
  void filterByLandingZone_noMatch() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lz-par");
    lzf.setName(stringFilter);
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setLandingZone(lzf);
    assertThat(f.matches(ati("ati-main"))).isFalse();
  }

  // ─── componentInstances (nested) ─────────────────────────────────────────────

  @Test
  void filterByComponentInstances_match() {
    ComponentInstanceFilter cif = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ci-app");
    cif.setName(stringFilter);
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setComponentInstances(List.of(cif));
    assertThat(f.matches(ati("ati-main"))).isTrue();
  }

  @Test
  void filterByComponentInstances_noMatch() {
    ComponentInstanceFilter cif = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    cif.setName(stringFilter);
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setComponentInstances(List.of(cif));
    assertThat(f.matches(ati("ati-main"))).isFalse();
  }

  @Test
  void filterByComponentInstances_nullComponentInstances_noMatch() {
    ComponentInstanceFilter cif = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ci-app");
    cif.setName(stringFilter);
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setComponentInstances(List.of(cif));
    AutomaticTemplateInstance ati = ati("ati-main");
    when(ati.getComponentInstances()).thenReturn(null);
    assertThat(f.matches(ati)).isFalse();
  }

  // ─── getters ─────────────────────────────────────────────────────────────────

  @Test
  void getters_returnSetValues() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    LandingZoneFilter lzf = new LandingZoneFilter();
    ComponentInstanceFilter cif = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ati");
    StringFilter componentNameFilter = new StringFilter();
    componentNameFilter.setLike("comp");

    f.setName(stringFilter);
    f.setComponentName(componentNameFilter);
    f.setLandingZone(lzf);
    f.setComponentInstances(List.of(cif));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getComponentName()).isEqualTo(componentNameFilter);
    assertThat(f.getLandingZone()).isSameAs(lzf);
    assertThat(f.getComponentInstances()).containsExactly(cif);
  }

  @Test
  void filterById_match() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("ati-");
    f.setId(idFilter);
    assertThat(f.matches(ati("ati-template"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(ati("ati-template"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    AutomaticTemplateInstanceFilter f = new AutomaticTemplateInstanceFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
