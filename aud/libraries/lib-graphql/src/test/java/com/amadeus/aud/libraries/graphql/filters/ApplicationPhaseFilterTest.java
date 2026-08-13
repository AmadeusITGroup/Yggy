package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApplicationPhaseFilterTest {

  private ApplicationPhase ap(String name) {
    ApplicationPhase ap = mock(ApplicationPhase.class);
    when(ap.id()).thenReturn("ap-" + name.hashCode());
    when(ap.getName()).thenReturn(name);
    when(ap.isCustomerFacing()).thenReturn(true);

    MacroPhase mp = mock(MacroPhase.class);
    when(mp.getName()).thenReturn("mp-prod");
    when(mp.getAutomationOrder()).thenReturn(1);
    when(mp.getApplicationPhases()).thenReturn(null);
    when(mp.getTargets()).thenReturn(null);
    when(mp.getConnectionPhases()).thenReturn(null);
    when(ap.getMacroPhase()).thenReturn(mp);

    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn("ci-frontend");
    when(ap.getComponentInstances()).thenReturn(Set.of(ci));

    return ap;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ApplicationPhaseFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ApplicationPhaseFilter().matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    f.setName(stringFilter);
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dev");
    f.setName(stringFilter);
    assertThat(f.matches(ap("ap-prod"))).isFalse();
  }

  @Test
  void filterByIsCustomerFacing_match() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setIsCustomerFacing(true);
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterByIsCustomerFacing_noMatch() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setIsCustomerFacing(false);
    assertThat(f.matches(ap("ap-prod"))).isFalse();
  }

  @Test
  void filterByMacroPhase_match() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    mpf.setName(stringFilter);

    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterByMacroPhase_noMatch() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("staging");
    mpf.setName(stringFilter);

    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setMacroPhase(mpf);
    assertThat(f.matches(ap("ap-prod"))).isFalse();
  }

  @Test
  void filterByComponentInstances_match() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter componentInstanceFilter = new StringFilter();
    componentInstanceFilter.setLike("ci-frontend");
    f.setComponentInstances(List.of(componentInstanceFilter));
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterByComponentInstances_noMatch() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter componentInstanceFilter = new StringFilter();
    componentInstanceFilter.setLike("ci-missing");
    f.setComponentInstances(List.of(componentInstanceFilter));
    assertThat(f.matches(ap("ap-prod"))).isFalse();
  }

  @Test
  void filterByComponentInstances_nullOnEntity_noMatch() {
    ApplicationPhase ap = mock(ApplicationPhase.class);
    when(ap.getName()).thenReturn("ap-1");
    when(ap.getComponentInstances()).thenReturn(null);

    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter componentInstanceFilter = new StringFilter();
    componentInstanceFilter.setLike("ci-frontend");
    f.setComponentInstances(List.of(componentInstanceFilter));
    assertThat(f.matches(ap)).isFalse();
  }

  @Test
  void filterByComponentInstances_empty_matchesAll() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setComponentInstances(List.of());
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void getters_returnSetValues() {
    MacroPhaseFilter mpf = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter componentInstanceFilter = new StringFilter();
    componentInstanceFilter.setLike("ci-1");

    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setName(stringFilter);
    f.setIsCustomerFacing(true);
    f.setMacroPhase(mpf);
    f.setComponentInstances(List.of(componentInstanceFilter));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getIsCustomerFacing()).isTrue();
    assertThat(f.getMacroPhase()).isSameAs(mpf);
    assertThat(f.getComponentInstances()).containsExactly(componentInstanceFilter);
  }

  @Test
  void filterById_match() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("ap-");
    f.setId(idFilter);
    assertThat(f.matches(ap("ap-prod"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(ap("ap-prod"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ApplicationPhaseFilter f = new ApplicationPhaseFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}
