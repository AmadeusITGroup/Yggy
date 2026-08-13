package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DependencyInstanceFilterTest {

  private DependencyInstance di(String name) {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.id()).thenReturn("di-" + name.hashCode());
    when(di.getName()).thenReturn(name);
    when(di.getDependencyName()).thenReturn("dep-alpha");
    when(di.getSourceComponentInstanceName()).thenReturn("ci-src");
    when(di.getTargetComponentInstanceName()).thenReturn("ci-tgt");
    when(di.getViaComponentInstanceName()).thenReturn("ci-via");
    when(di.getViaServiceName()).thenReturn("svc-via");
    when(di.isSourceAsComponent()).thenReturn(true);
    when(di.isSourceLimitedToLegacyOnly()).thenReturn(false);
    when(di.isSourceLimitedToCloudOnly()).thenReturn(false);
    when(di.isSourceLimitedToLandingZone()).thenReturn(false);
    when(di.isSourceLimitedToTarget()).thenReturn(false);
    when(di.isSourceLimitedToContinent()).thenReturn(false);
    when(di.isSourceLimitedToMacroPhase()).thenReturn(false);
    when(di.isSourceLimitedToSelector()).thenReturn(false);
    when(di.isTargetAsComponent()).thenReturn(false);
    when(di.isTargetLimitedToLegacyOnly()).thenReturn(false);
    when(di.isTargetLimitedToCloudOnly()).thenReturn(false);
    when(di.isTargetLimitedToSourceNamespace()).thenReturn(false);
    when(di.isTargetLimitedToSourcePaas()).thenReturn(false);
    when(di.isTargetLimitedToSourceSecurityZone()).thenReturn(false);
    when(di.isTargetLimitedToSourceApplicationPhase()).thenReturn(false);
    when(di.isTargetLimitedToSourceLandingZone()).thenReturn(false);
    when(di.isTargetLimitedToSourceTarget()).thenReturn(false);
    when(di.isTargetLimitedToSourceContinent()).thenReturn(false);
    when(di.isTargetLimitedToSourceMacroPhase()).thenReturn(false);
    when(di.getServiceNames()).thenReturn(Set.of("svc-http"));
    return di;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new DependencyInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new DependencyInstanceFilter().matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("di");
    f.setName(stringFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByDependencyName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter dependencyNameFilter = new StringFilter();
    dependencyNameFilter.setLike("DEP-ALPHA");
    f.setDependencyName(dependencyNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByDependencyName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter dependencyNameFilter = new StringFilter();
    dependencyNameFilter.setLike("other");
    f.setDependencyName(dependencyNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByDependencyName_nullOnEntity_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getDependencyName()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter dependencyNameFilter = new StringFilter();
    dependencyNameFilter.setLike("dep-alpha");
    f.setDependencyName(dependencyNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void filterBySourceComponentInstanceName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter sourceComponentInstanceNameFilter = new StringFilter();
    sourceComponentInstanceNameFilter.setLike("CI-SRC");
    f.setSourceComponentInstanceName(sourceComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterBySourceComponentInstanceName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter sourceComponentInstanceNameFilter = new StringFilter();
    sourceComponentInstanceNameFilter.setLike("other");
    f.setSourceComponentInstanceName(sourceComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterBySourceComponentInstanceName_nullOnEntity_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getSourceComponentInstanceName()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter sourceComponentInstanceNameFilter = new StringFilter();
    sourceComponentInstanceNameFilter.setLike("ci-src");
    f.setSourceComponentInstanceName(sourceComponentInstanceNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void filterByTargetComponentInstanceName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter targetComponentInstanceNameFilter = new StringFilter();
    targetComponentInstanceNameFilter.setLike("CI-TGT");
    f.setTargetComponentInstanceName(targetComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByTargetComponentInstanceName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter targetComponentInstanceNameFilter = new StringFilter();
    targetComponentInstanceNameFilter.setLike("other");
    f.setTargetComponentInstanceName(targetComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByTargetComponentInstanceName_nullOnEntity_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getTargetComponentInstanceName()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter targetComponentInstanceNameFilter = new StringFilter();
    targetComponentInstanceNameFilter.setLike("ci-tgt");
    f.setTargetComponentInstanceName(targetComponentInstanceNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void filterByViaComponentInstanceName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaComponentInstanceNameFilter = new StringFilter();
    viaComponentInstanceNameFilter.setLike("CI-VIA");
    f.setViaComponentInstanceName(viaComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByViaComponentInstanceName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaComponentInstanceNameFilter = new StringFilter();
    viaComponentInstanceNameFilter.setLike("other");
    f.setViaComponentInstanceName(viaComponentInstanceNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByViaComponentInstanceName_nullOnEntity_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getViaComponentInstanceName()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaComponentInstanceNameFilter = new StringFilter();
    viaComponentInstanceNameFilter.setLike("ci-via");
    f.setViaComponentInstanceName(viaComponentInstanceNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void filterByViaServiceName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaServiceNameFilter = new StringFilter();
    viaServiceNameFilter.setLike("SVC-VIA");
    f.setViaServiceName(viaServiceNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByViaServiceName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaServiceNameFilter = new StringFilter();
    viaServiceNameFilter.setLike("other");
    f.setViaServiceName(viaServiceNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByViaServiceName_nullOnEntity_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getViaServiceName()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter viaServiceNameFilter = new StringFilter();
    viaServiceNameFilter.setLike("svc-via");
    f.setViaServiceName(viaServiceNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void filterByIsSourceAsComponent_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceAsComponent(true);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByIsSourceAsComponent_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceAsComponent(false);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToLegacyOnly_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToLegacyOnly(false);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByIsSourceLimitedToLegacyOnly_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToLegacyOnly(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToCloudOnly_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToCloudOnly(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToLandingZone_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToLandingZone(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToTarget_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToTarget(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToContinent_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToContinent(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToMacroPhase_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToMacroPhase(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsSourceLimitedToSelector_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsSourceLimitedToSelector(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetAsComponent_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetAsComponent(false);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByIsTargetAsComponent_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetAsComponent(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToLegacyOnly_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToLegacyOnly(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToCloudOnly_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToCloudOnly(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceNamespace_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceNamespace(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourcePaas_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourcePaas(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceSecurityZone_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceSecurityZone(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceApplicationPhase_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceApplicationPhase(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceLandingZone_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceLandingZone(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceTarget_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceTarget(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceContinent_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceContinent(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByIsTargetLimitedToSourceMacroPhase_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setIsTargetLimitedToSourceMacroPhase(true);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByServiceName_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter serviceNameFilter = new StringFilter();
    serviceNameFilter.setLike("SVC-HTTP");
    f.setServiceName(serviceNameFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterByServiceName_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter serviceNameFilter = new StringFilter();
    serviceNameFilter.setLike("svc-missing");
    f.setServiceName(serviceNameFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }

  @Test
  void filterByServiceName_nullServiceNames_noMatch() {
    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("di-1");
    when(di.getServiceNames()).thenReturn(null);

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter serviceNameFilter = new StringFilter();
    serviceNameFilter.setLike("svc-http");
    f.setServiceName(serviceNameFilter);
    assertThat(f.matches(di)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter dependencyNameFilter = new StringFilter();
    dependencyNameFilter.setLike("d");
    StringFilter sourceComponentInstanceNameFilter = new StringFilter();
    sourceComponentInstanceNameFilter.setLike("src");
    StringFilter targetComponentInstanceNameFilter = new StringFilter();
    targetComponentInstanceNameFilter.setLike("tgt");
    StringFilter viaComponentInstanceNameFilter = new StringFilter();
    viaComponentInstanceNameFilter.setLike("via");
    StringFilter viaServiceNameFilter = new StringFilter();
    viaServiceNameFilter.setLike("vs");
    StringFilter serviceNameFilter = new StringFilter();
    serviceNameFilter.setLike("svc");

    f.setName(stringFilter);
    f.setDependencyName(dependencyNameFilter);
    f.setSourceComponentInstanceName(sourceComponentInstanceNameFilter);
    f.setTargetComponentInstanceName(targetComponentInstanceNameFilter);
    f.setViaComponentInstanceName(viaComponentInstanceNameFilter);
    f.setViaServiceName(viaServiceNameFilter);
    f.setIsSourceAsComponent(true);
    f.setIsSourceLimitedToLegacyOnly(false);
    f.setIsSourceLimitedToCloudOnly(true);
    f.setIsSourceLimitedToLandingZone(false);
    f.setIsSourceLimitedToTarget(true);
    f.setIsSourceLimitedToContinent(false);
    f.setIsSourceLimitedToMacroPhase(true);
    f.setIsSourceLimitedToSelector(false);
    f.setIsTargetAsComponent(true);
    f.setIsTargetLimitedToLegacyOnly(false);
    f.setIsTargetLimitedToCloudOnly(true);
    f.setIsTargetLimitedToSourceNamespace(false);
    f.setIsTargetLimitedToSourcePaas(true);
    f.setIsTargetLimitedToSourceSecurityZone(false);
    f.setIsTargetLimitedToSourceApplicationPhase(true);
    f.setIsTargetLimitedToSourceLandingZone(false);
    f.setIsTargetLimitedToSourceTarget(true);
    f.setIsTargetLimitedToSourceContinent(false);
    f.setIsTargetLimitedToSourceMacroPhase(true);
    f.setServiceName(serviceNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getDependencyName()).isEqualTo(dependencyNameFilter);
    assertThat(f.getSourceComponentInstanceName()).isEqualTo(sourceComponentInstanceNameFilter);
    assertThat(f.getTargetComponentInstanceName()).isEqualTo(targetComponentInstanceNameFilter);
    assertThat(f.getViaComponentInstanceName()).isEqualTo(viaComponentInstanceNameFilter);
    assertThat(f.getViaServiceName()).isEqualTo(viaServiceNameFilter);
    assertThat(f.getIsSourceAsComponent()).isTrue();
    assertThat(f.getIsSourceLimitedToLegacyOnly()).isFalse();
    assertThat(f.getIsSourceLimitedToCloudOnly()).isTrue();
    assertThat(f.getIsSourceLimitedToLandingZone()).isFalse();
    assertThat(f.getIsSourceLimitedToTarget()).isTrue();
    assertThat(f.getIsSourceLimitedToContinent()).isFalse();
    assertThat(f.getIsSourceLimitedToMacroPhase()).isTrue();
    assertThat(f.getIsSourceLimitedToSelector()).isFalse();
    assertThat(f.getIsTargetAsComponent()).isTrue();
    assertThat(f.getIsTargetLimitedToLegacyOnly()).isFalse();
    assertThat(f.getIsTargetLimitedToCloudOnly()).isTrue();
    assertThat(f.getIsTargetLimitedToSourceNamespace()).isFalse();
    assertThat(f.getIsTargetLimitedToSourcePaas()).isTrue();
    assertThat(f.getIsTargetLimitedToSourceSecurityZone()).isFalse();
    assertThat(f.getIsTargetLimitedToSourceApplicationPhase()).isTrue();
    assertThat(f.getIsTargetLimitedToSourceLandingZone()).isFalse();
    assertThat(f.getIsTargetLimitedToSourceTarget()).isTrue();
    assertThat(f.getIsTargetLimitedToSourceContinent()).isFalse();
    assertThat(f.getIsTargetLimitedToSourceMacroPhase()).isTrue();
    assertThat(f.getServiceName()).isEqualTo(serviceNameFilter);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    DependencyInstanceFilter f = new DependencyInstanceFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("di-");
    f.setId(idFilter);
    assertThat(f.matches(di("di-1"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    DependencyInstanceFilter f = new DependencyInstanceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(di("di-1"))).isFalse();
  }
}
