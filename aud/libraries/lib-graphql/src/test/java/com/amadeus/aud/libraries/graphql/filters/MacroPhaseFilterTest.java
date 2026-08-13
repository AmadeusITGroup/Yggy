package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Target;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MacroPhaseFilterTest {

  private MacroPhase mp(String name) {
    MacroPhase mp = mock(MacroPhase.class);
    when(mp.getName()).thenReturn(name);
    when(mp.id()).thenReturn("mp-" + name.hashCode());
    when(mp.getAutomationOrder()).thenReturn(3);

    ApplicationPhase ap = mock(ApplicationPhase.class);
    when(ap.getName()).thenReturn("phase-prod");
    when(mp.getApplicationPhases()).thenReturn(Set.of(ap));

    Target t = mock(Target.class);
    when(t.getName()).thenReturn("target-ams");
    when(mp.getTargets()).thenReturn(Set.of(t));

    ConnectionPhase cp = mock(ConnectionPhase.class);
    when(cp.getName()).thenReturn("cp-main");
    when(mp.getConnectionPhases()).thenReturn(Set.of(cp));

    return mp;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new MacroPhaseFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new MacroPhaseFilter().matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByName_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("phase");
    f.setName(stringFilter);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("deploy");
    f.setName(stringFilter);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }

  @Test
  void filterByAutomationOrder_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    f.setAutomationOrder(3);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByAutomationOrder_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    f.setAutomationOrder(99);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }

  @Test
  void filterByApplicationPhaseName_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter applicationPhaseNameFilter = new StringFilter();
    applicationPhaseNameFilter.setLike("phase-prod");
    f.setApplicationPhaseName(applicationPhaseNameFilter);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByApplicationPhaseName_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter applicationPhaseNameFilter = new StringFilter();
    applicationPhaseNameFilter.setLike("phase-staging");
    f.setApplicationPhaseName(applicationPhaseNameFilter);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }

  @Test
  void filterByTargetName_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("target-ams");
    f.setTargetName(targetNameFilter);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByTargetName_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("target-nyc");
    f.setTargetName(targetNameFilter);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }

  @Test
  void filterByConnectionPhaseName_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter connectionPhaseNameFilter = new StringFilter();
    connectionPhaseNameFilter.setLike("cp-main");
    f.setConnectionPhaseName(connectionPhaseNameFilter);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterByConnectionPhaseName_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter connectionPhaseNameFilter = new StringFilter();
    connectionPhaseNameFilter.setLike("cp-other");
    f.setConnectionPhaseName(connectionPhaseNameFilter);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");

    StringFilter applicationPhaseNameFilter = new StringFilter();
    applicationPhaseNameFilter.setLike("ap");
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("t");
    StringFilter connectionPhaseNameFilter = new StringFilter();
    connectionPhaseNameFilter.setLike("cp");

    MacroPhaseFilter f = new MacroPhaseFilter();
    f.setName(stringFilter);
    f.setAutomationOrder(1);
    f.setApplicationPhaseName(applicationPhaseNameFilter);
    f.setTargetName(targetNameFilter);
    f.setConnectionPhaseName(connectionPhaseNameFilter);
    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getAutomationOrder()).isOne();
    assertThat(f.getApplicationPhaseName()).isEqualTo(applicationPhaseNameFilter);
    assertThat(f.getTargetName()).isEqualTo(targetNameFilter);
    assertThat(f.getConnectionPhaseName()).isEqualTo(connectionPhaseNameFilter);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    MacroPhaseFilter f = new MacroPhaseFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("mp-");
    f.setId(idFilter);
    assertThat(f.matches(mp("phase-1"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    MacroPhaseFilter f = new MacroPhaseFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(mp("phase-1"))).isFalse();
  }
}
