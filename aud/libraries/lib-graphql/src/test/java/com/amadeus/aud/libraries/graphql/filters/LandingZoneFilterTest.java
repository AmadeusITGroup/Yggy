package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Target;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LandingZoneFilterTest {

  private LandingZone lz(String name) {
    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn(name);
    when(lz.id()).thenReturn("lz-" + name.hashCode());
    when(lz.getRegionName()).thenReturn("eu-west-1");
    when(lz.getLandingZoneTypeName()).thenReturn("prod");

    MacroPhase mp = mock(MacroPhase.class);
    when(mp.getName()).thenReturn("mp-alpha");
    when(mp.getAutomationOrder()).thenReturn(1);
    when(mp.getApplicationPhases()).thenReturn(null);
    when(mp.getTargets()).thenReturn(null);
    when(mp.getConnectionPhases()).thenReturn(null);
    when(lz.getMacroPhase()).thenReturn(mp);

    when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(Set.of("standard", "paas"));

    Target t = mock(Target.class);
    when(t.getName()).thenReturn("aws-prod");
    when(t.getProviderName()).thenReturn("AWS");
    when(t.getTargetTypeName()).thenReturn("cloud");
    when(t.getMacroPhase()).thenReturn(null);
    when(t.getContinentName()).thenReturn("eu");
    when(t.getPopPrimaryName()).thenReturn(null);
    when(t.getPopSecondaryName()).thenReturn(null);
    when(t.isCloud()).thenReturn(true);
    when(t.isAmadeus()).thenReturn(false);
    when(t.isAudManaged()).thenReturn(true);
    when(t.isEnableOfficeConnectivity()).thenReturn(false);
    when(t.isEnableJumpServerAccess()).thenReturn(false);
    when(t.isEnableVnetToVnet()).thenReturn(false);
    when(lz.getTarget()).thenReturn(t);

    return lz;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new LandingZoneFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new LandingZoneFilter().matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByName_match() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ams");
    f.setName(stringFilter);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void filterByRegionName_match() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("EU-WEST-1");
    f.setRegionName(regionNameFilter);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByRegionName_noMatch() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("us-east-1");
    f.setRegionName(regionNameFilter);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void filterByLandingZoneTypeName_match() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter landingZoneTypeNameFilter = new StringFilter();
    landingZoneTypeNameFilter.setLike("PROD");
    f.setLandingZoneTypeName(landingZoneTypeNameFilter);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByLandingZoneTypeName_noMatch() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter landingZoneTypeNameFilter = new StringFilter();
    landingZoneTypeNameFilter.setLike("dev");
    f.setLandingZoneTypeName(landingZoneTypeNameFilter);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void filterByMacroPhase_match() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("alpha");
    mpf.setName(stringFilter);

    LandingZoneFilter f = new LandingZoneFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByMacroPhase_noMatch() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("beta");
    mpf.setName(stringFilter);

    LandingZoneFilter f = new LandingZoneFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void filterByFunctionalType_match() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter functionalTypeFilter = new StringFilter();
    functionalTypeFilter.setLike("PAAS");
    f.setFunctionalType(functionalTypeFilter);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByFunctionalType_noMatch() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter functionalTypeFilter = new StringFilter();
    functionalTypeFilter.setLike("saas");
    f.setFunctionalType(functionalTypeFilter);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void filterByFunctionalType_nullRepresentation_noMatch() {
    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-1");
    when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);

    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter functionalTypeFilter = new StringFilter();
    functionalTypeFilter.setLike("standard");
    f.setFunctionalType(functionalTypeFilter);
    assertThat(f.matches(lz)).isFalse();
  }

  @Test
  void filterByTarget_match() {
    TargetFilter tf = new TargetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("aws");
    tf.setName(stringFilter);

    LandingZoneFilter f = new LandingZoneFilter();
    f.setTarget(tf);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterByTarget_noMatch() {
    TargetFilter tf = new TargetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("azure");
    tf.setName(stringFilter);

    LandingZoneFilter f = new LandingZoneFilter();
    f.setTarget(tf);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    TargetFilter tf = new TargetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");

    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("r");
    StringFilter landingZoneTypeNameFilter = new StringFilter();
    landingZoneTypeNameFilter.setLike("t");
    StringFilter functionalTypeFilter = new StringFilter();
    functionalTypeFilter.setLike("ft");

    LandingZoneFilter f = new LandingZoneFilter();
    f.setName(stringFilter);
    f.setRegionName(regionNameFilter);
    f.setLandingZoneTypeName(landingZoneTypeNameFilter);
    f.setMacroPhase(mpf);
    f.setFunctionalType(functionalTypeFilter);
    f.setTarget(tf);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getRegionName()).isEqualTo(regionNameFilter);
    assertThat(f.getLandingZoneTypeName()).isEqualTo(landingZoneTypeNameFilter);
    assertThat(f.getMacroPhase()).isSameAs(mpf);
    assertThat(f.getFunctionalType()).isEqualTo(functionalTypeFilter);
    assertThat(f.getTarget()).isSameAs(tf);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    LandingZoneFilter f = new LandingZoneFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("lz-");
    f.setId(idFilter);
    assertThat(f.matches(lz("lz-ams"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    LandingZoneFilter f = new LandingZoneFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(lz("lz-ams"))).isFalse();
  }
}
