package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Target;
import org.junit.jupiter.api.Test;

class TargetFilterTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  private Target target(String name) {
    Target t = mock(Target.class);
    when(t.id()).thenReturn("target-" + name.hashCode());
    when(t.getName()).thenReturn(name);
    when(t.getProviderName()).thenReturn("AWS");
    when(t.getTargetTypeName()).thenReturn("cloud");
    when(t.getContinentName()).thenReturn("eu");
    when(t.getPopPrimaryName()).thenReturn("pop-a");
    when(t.getPopSecondaryName()).thenReturn("pop-b");
    when(t.isCloud()).thenReturn(true);
    when(t.isAmadeus()).thenReturn(false);
    when(t.isAudManaged()).thenReturn(true);
    when(t.isEnableOfficeConnectivity()).thenReturn(false);
    when(t.isEnableJumpServerAccess()).thenReturn(false);
    when(t.isEnableVnetToVnet()).thenReturn(true);

    MacroPhase mp = mock(MacroPhase.class);
    when(mp.getName()).thenReturn("phase-prod");
    when(mp.getAutomationOrder()).thenReturn(2);
    when(mp.getApplicationPhases()).thenReturn(null);
    when(mp.getTargets()).thenReturn(null);
    when(mp.getConnectionPhases()).thenReturn(null);
    when(t.getMacroPhase()).thenReturn(mp);

    return t;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new TargetFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new TargetFilter().matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByName_match() {
    TargetFilter f = new TargetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("aws");
    f.setName(stringFilter);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    TargetFilter f = new TargetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("azure");
    f.setName(stringFilter);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByProviderName_match() {
    TargetFilter f = new TargetFilter();
    f.setProviderName(sf("AWS"));
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByProviderName_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setProviderName(sf("Azure"));
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByTargetTypeName_match() {
    TargetFilter f = new TargetFilter();
    f.setTargetTypeName(sf("CLOUD"));
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByTargetTypeName_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setTargetTypeName(sf("on-prem"));
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByMacroPhase_match() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    mpf.setName(stringFilter);

    TargetFilter f = new TargetFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByMacroPhase_noMatch() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dev");
    mpf.setName(stringFilter);

    TargetFilter f = new TargetFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByContinentName_match() {
    TargetFilter f = new TargetFilter();
    f.setContinentName(sf("EU"));
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByContinentName_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setContinentName(sf("US"));
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByPopPrimaryName_match() {
    TargetFilter f = new TargetFilter();
    f.setPopPrimaryName(sf("POP-A"));
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByPopPrimaryName_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setPopPrimaryName(sf("pop-z"));
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByPopSecondaryName_match() {
    TargetFilter f = new TargetFilter();
    f.setPopSecondaryName(sf("POP-B"));
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByPopSecondaryName_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setPopSecondaryName(sf("pop-z"));
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsCloud_match() {
    TargetFilter f = new TargetFilter();
    f.setIsCloud(true);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByIsCloud_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsCloud(false);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsAmadeus_match() {
    TargetFilter f = new TargetFilter();
    f.setIsAmadeus(false);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByIsAmadeus_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsAmadeus(true);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsAudManaged_match() {
    TargetFilter f = new TargetFilter();
    f.setIsAudManaged(true);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByIsAudManaged_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsAudManaged(false);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsEnableOfficeConnectivity_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsEnableOfficeConnectivity(true);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsEnableJumpServerAccess_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsEnableJumpServerAccess(true);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void filterByIsEnableVnetToVnet_match() {
    TargetFilter f = new TargetFilter();
    f.setIsEnableVnetToVnet(true);
    assertThat(f.matches(target("aws-prod"))).isTrue();
  }

  @Test
  void filterByIsEnableVnetToVnet_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setIsEnableVnetToVnet(false);
    assertThat(f.matches(target("aws-prod"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");

    TargetFilter f = new TargetFilter();
    f.setName(stringFilter);
    f.setProviderName(sf("p"));
    f.setTargetTypeName(sf("t"));
    f.setMacroPhase(mpf);
    f.setContinentName(sf("c"));
    f.setPopPrimaryName(sf("pp"));
    f.setPopSecondaryName(sf("ps"));
    f.setIsCloud(true);
    f.setIsAmadeus(false);
    f.setIsAudManaged(true);
    f.setIsEnableOfficeConnectivity(false);
    f.setIsEnableJumpServerAccess(true);
    f.setIsEnableVnetToVnet(false);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getProviderName()).isNotNull();
    assertThat(f.getTargetTypeName()).isNotNull();
    assertThat(f.getMacroPhase()).isSameAs(mpf);
    assertThat(f.getContinentName()).isNotNull();
    assertThat(f.getPopPrimaryName()).isNotNull();
    assertThat(f.getPopSecondaryName()).isNotNull();
    assertThat(f.getIsCloud()).isTrue();
    assertThat(f.getIsAmadeus()).isFalse();
    assertThat(f.getIsAudManaged()).isTrue();
    assertThat(f.getIsEnableOfficeConnectivity()).isFalse();
    assertThat(f.getIsEnableJumpServerAccess()).isTrue();
    assertThat(f.getIsEnableVnetToVnet()).isFalse();
  }

  @Test
  void filterById_match() {
    TargetFilter f = new TargetFilter();
    f.setId(sf("target-"));
    assertThat(f.matches(target("t-prod"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    TargetFilter f = new TargetFilter();
    f.setId(sf("xyz"));
    assertThat(f.matches(target("t-prod"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    TargetFilter f = new TargetFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
