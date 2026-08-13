package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import org.junit.jupiter.api.Test;

class ComponentInstanceFilterTest {

  // ─── helpers ────────────────────────────────────────────────────────────────

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private ComponentInstance ci(String name) {
    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn(name);
    when(ci.id()).thenReturn("ci-" + name.hashCode());

    // component + scope
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

    // zone type
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

    // application phase
    when(ci.getApplicationPhaseName()).thenReturn("phase-prod");

    // namespace
    Namespace ns = mock(Namespace.class);
    when(ns.getName()).thenReturn("ns-default");
    when(ns.isShared()).thenReturn(false);
    when(ns.isAutomaticallyGenerated()).thenReturn(false);
    when(ns.getAuthorizedScopes()).thenReturn(null);
    when(ns.getComponentInstances()).thenReturn(null);
    when(ci.getNamespace()).thenReturn(ns);

    // simple string fields
    when(ci.getServiceTagName()).thenReturn("tag-svc");
    when(ci.getDeployComponentInstanceName()).thenReturn("deploy-ci");
    when(ci.getInSubnetInstanceName()).thenReturn("subnet-in");
    when(ci.getOutSubnetInstanceName()).thenReturn("subnet-out");

    // zone instances in/out
    ZoneInstance inZi = mock(ZoneInstance.class);
    when(inZi.getName()).thenReturn("zi-in");
    when(inZi.getLandingZoneName()).thenReturn("lz-ams");
    when(inZi.getZoneTypeName()).thenReturn("dmz");
    when(inZi.getFirewallGroupName()).thenReturn(null);
    when(inZi.isNsgNeeded()).thenReturn(false);
    when(inZi.getSubnetInstances()).thenReturn(null);
    when(inZi.getComponentInstances()).thenReturn(null);
    when(ci.getInZoneInstance()).thenReturn(inZi);

    ZoneInstance outZi = mock(ZoneInstance.class);
    when(outZi.getName()).thenReturn("zi-out");
    when(outZi.getLandingZoneName()).thenReturn("lz-ams");
    when(outZi.getZoneTypeName()).thenReturn("dmz");
    when(outZi.getFirewallGroupName()).thenReturn(null);
    when(outZi.isNsgNeeded()).thenReturn(false);
    when(outZi.getSubnetInstances()).thenReturn(null);
    when(outZi.getComponentInstances()).thenReturn(null);
    when(ci.getOutZoneInstance()).thenReturn(outZi);

    // enums
    when(ci.getEndpointType()).thenReturn(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST);
    when(ci.getHostingPlatformType())
        .thenReturn(V1ComponentInstanceSpec.HostingComponentTypeEnum.AKS);

    // other fields
    when(ci.getK8sIdentifier()).thenReturn("k8s-id-1");
    when(ci.isAutomaticallyInstantiated()).thenReturn(true);
    when(ci.getHostingPlatformName()).thenReturn("iaas-ci");
    when(ci.getAutomaticTemplateInstances()).thenReturn(null);

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
    when(ci.getLandingZone()).thenReturn(lz);

    return ci;
  }

  // ─── null / empty ────────────────────────────────────────────────────────────

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ComponentInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ComponentInstanceFilter().matches(ci("ci-app"))).isTrue();
  }

  // ─── name ────────────────────────────────────────────────────────────────────

  @Test
  void filterByName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("app");
    f.setName(stringFilter);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── scope ───────────────────────────────────────────────────────────────────

  @Test
  void filterByScope_match() {
    ScopeFilter sf = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    sf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setScope(sf);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByScope_noMatch() {
    ScopeFilter sf = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dev");
    sf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setScope(sf);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  @Test
  void filterByScope_nullComponent_noMatch() {
    ScopeFilter sf = new ScopeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    sf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setScope(sf);
    ComponentInstance ci = ci("ci-app");
    when(ci.getComponent()).thenReturn(null);
    assertThat(f.matches(ci)).isFalse();
  }

  // ─── zone type ───────────────────────────────────────────────────────────────

  @Test
  void filterByZoneType_match() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dmz");
    ztf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setZoneType(ztf);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByZoneType_noMatch() {
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("internet");
    ztf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setZoneType(ztf);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── applicationPhaseName ───────────────────────────────────────────────────

  @Test
  void filterByApplicationPhaseName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setApplicationPhaseName(sf("phase-prod"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByApplicationPhaseName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setApplicationPhaseName(sf("phase-dev"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  @Test
  void filterByApplicationPhaseName_null_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setApplicationPhaseName(sf("phase-prod"));
    ComponentInstance ci = ci("ci-app");
    when(ci.getApplicationPhaseName()).thenReturn(null);
    assertThat(f.matches(ci)).isFalse();
  }

  // ─── namespace ───────────────────────────────────────────────────────────────

  @Test
  void filterByNamespace_match() {
    NamespaceFilter nf = new NamespaceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ns-default");
    nf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setNamespace(nf);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByNamespace_noMatch() {
    NamespaceFilter nf = new NamespaceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ns-other");
    nf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setNamespace(nf);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── serviceTagName ──────────────────────────────────────────────────────────

  @Test
  void filterByServiceTagName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setServiceTagName(sf("tag-svc"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByServiceTagName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setServiceTagName(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── deployComponentInstanceName ────────────────────────────────────────────

  @Test
  void filterByDeployComponentInstanceName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setDeployComponentInstanceName(sf("deploy-ci"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByDeployComponentInstanceName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setDeployComponentInstanceName(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── inSubnetInstanceName ────────────────────────────────────────────────────

  @Test
  void filterByInSubnetInstanceName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setInSubnetInstanceName(sf("subnet-in"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByInSubnetInstanceName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setInSubnetInstanceName(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── outSubnetInstanceName ───────────────────────────────────────────────────

  @Test
  void filterByOutSubnetInstanceName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setOutSubnetInstanceName(sf("subnet-out"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByOutSubnetInstanceName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setOutSubnetInstanceName(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── inZoneInstance ──────────────────────────────────────────────────────────

  @Test
  void filterByInZoneInstance_match() {
    ZoneInstanceFilter zif = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("zi-in");
    zif.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setInZoneInstance(zif);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByInZoneInstance_noMatch() {
    ZoneInstanceFilter zif = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    zif.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setInZoneInstance(zif);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── outZoneInstance ─────────────────────────────────────────────────────────

  @Test
  void filterByOutZoneInstance_match() {
    ZoneInstanceFilter zof = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("zi-out");
    zof.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setOutZoneInstance(zof);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByOutZoneInstance_noMatch() {
    ZoneInstanceFilter zof = new ZoneInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    zof.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setOutZoneInstance(zof);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── endpointType ────────────────────────────────────────────────────────────

  @Test
  void filterByEndpointType_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setEndpointType(sf(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST.name()));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByEndpointType_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setEndpointType(sf(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S.name()));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  @Test
  void filterByEndpointType_nullEnum_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setEndpointType(sf(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST.name()));
    ComponentInstance ci = ci("ci-app");
    when(ci.getEndpointType()).thenReturn(null);
    assertThat(f.matches(ci)).isFalse();
  }

  // ─── hostingPlatformType ─────────────────────────────────────────────────────

  @Test
  void filterByHostingPlatformType_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setHostingPlatformType(sf(V1ComponentInstanceSpec.HostingComponentTypeEnum.AKS.name()));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByHostingPlatformType_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setHostingPlatformType(
        sf(V1ComponentInstanceSpec.HostingComponentTypeEnum.OPEN_SHIFT.name()));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  @Test
  void filterByHostingPlatformType_nullEnum_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setHostingPlatformType(sf(V1ComponentInstanceSpec.HostingComponentTypeEnum.AKS.name()));
    ComponentInstance ci = ci("ci-app");
    when(ci.getHostingPlatformType()).thenReturn(null);
    assertThat(f.matches(ci)).isFalse();
  }

  // ─── k8sIdentifier ───────────────────────────────────────────────────────────

  @Test
  void filterByK8sIdentifier_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setK8sIdentifier(sf("k8s-id-1"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByK8sIdentifier_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setK8sIdentifier(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── isAutomaticallyInstantiated ─────────────────────────────────────────────

  @Test
  void filterByIsAutomaticallyInstantiated_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setIsAutomaticallyInstantiated(true);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByIsAutomaticallyInstantiated_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setIsAutomaticallyInstantiated(false);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── landingZone ─────────────────────────────────────────────────────────────

  @Test
  void filterByLandingZone_match() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lz-ams");
    lzf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setLandingZone(lzf);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByLandingZone_noMatch() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lz-par");
    lzf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setLandingZone(lzf);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── iaasComponentInstanceName ───────────────────────────────────────────────

  @Test
  void filterByIaasComponentInstanceName_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setIaasComponentInstanceName(sf("iaas-ci"));
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByIaasComponentInstanceName_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setIaasComponentInstanceName(sf("other"));
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── component (nested) ──────────────────────────────────────────────────────

  @Test
  void filterByComponent_match() {
    ComponentFilter cf = new ComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("comp-api");
    cf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setComponent(cf);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterByComponent_noMatch() {
    ComponentFilter cf = new ComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    cf.setName(stringFilter);
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setComponent(cf);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }

  // ─── getters ─────────────────────────────────────────────────────────────────

  @Test
  void getters_returnSetValues() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    ScopeFilter sf = new ScopeFilter();
    ZoneTypeFilter ztf = new ZoneTypeFilter();
    NamespaceFilter nf = new NamespaceFilter();
    ZoneInstanceFilter inZif = new ZoneInstanceFilter();
    ZoneInstanceFilter outZif = new ZoneInstanceFilter();
    LandingZoneFilter lzf = new LandingZoneFilter();
    ComponentFilter cf = new ComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ci");

    f.setName(stringFilter);
    f.setScope(sf);
    f.setZoneType(ztf);
    f.setApplicationPhaseName(sf("phase"));
    f.setNamespace(nf);
    f.setServiceTagName(sf("tag"));
    f.setDeployComponentInstanceName(sf("deploy"));
    f.setInSubnetInstanceName(sf("in-sn"));
    f.setOutSubnetInstanceName(sf("out-sn"));
    f.setInZoneInstance(inZif);
    f.setOutZoneInstance(outZif);
    f.setEndpointType(sf("ET"));
    f.setDeprecatedStatus(sf("DS"));
    f.setHostingPlatformType(sf("HPT"));
    f.setK8sIdentifier(sf("k8s"));
    f.setIsAutomaticallyInstantiated(true);
    f.setLandingZone(lzf);
    f.setIaasComponentInstanceName(sf("iaas"));
    f.setComponent(cf);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getScope()).isSameAs(sf);
    assertThat(f.getZoneType()).isSameAs(ztf);
    assertThat(f.getApplicationPhaseName()).isNotNull();
    assertThat(f.getNamespace()).isSameAs(nf);
    assertThat(f.getServiceTagName()).isNotNull();
    assertThat(f.getDeployComponentInstanceName()).isNotNull();
    assertThat(f.getInSubnetInstanceName()).isNotNull();
    assertThat(f.getOutSubnetInstanceName()).isNotNull();
    assertThat(f.getInZoneInstance()).isSameAs(inZif);
    assertThat(f.getOutZoneInstance()).isSameAs(outZif);
    assertThat(f.getEndpointType()).isNotNull();
    assertThat(f.getDeprecatedStatus()).isNotNull();
    assertThat(f.getHostingPlatformType()).isNotNull();
    assertThat(f.getK8sIdentifier()).isNotNull();
    assertThat(f.getIsAutomaticallyInstantiated()).isTrue();
    assertThat(f.getLandingZone()).isSameAs(lzf);
    assertThat(f.getIaasComponentInstanceName()).isNotNull();
    assertThat(f.getComponent()).isSameAs(cf);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ComponentInstanceFilter f = new ComponentInstanceFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("ci-");
    f.setId(idFilter);
    assertThat(f.matches(ci("ci-app"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ComponentInstanceFilter f = new ComponentInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(ci("ci-app"))).isFalse();
  }
}
