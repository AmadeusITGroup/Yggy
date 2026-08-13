package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DependencyFilterTest {

  private Dependency dep(String name) {
    Dependency d = mock(Dependency.class);
    when(d.getName()).thenReturn(name);
    when(d.id()).thenReturn("dep-" + name.hashCode());
    when(d.getSourceComponentName()).thenReturn("src-comp");
    when(d.getDestinationComponentName()).thenReturn("dst-comp");

    DependencyInstance di = mock(DependencyInstance.class);
    when(di.getName()).thenReturn("dep-inst-alpha");
    when(d.getDependencyInstances()).thenReturn(Set.of(di));

    return d;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new DependencyFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new DependencyFilter().matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterByName_match() {
    DependencyFilter f = new DependencyFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dep");
    f.setName(stringFilter);
    assertThat(f.matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    DependencyFilter f = new DependencyFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(dep("dep-1"))).isFalse();
  }

  @Test
  void filterBySourceComponentName_match() {
    DependencyFilter f = new DependencyFilter();
    StringFilter sourceComponentNameFilter = new StringFilter();
    sourceComponentNameFilter.setLike("SRC-COMP");
    f.setSourceComponentName(sourceComponentNameFilter);
    assertThat(f.matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterBySourceComponentName_noMatch() {
    DependencyFilter f = new DependencyFilter();
    StringFilter sourceComponentNameFilter = new StringFilter();
    sourceComponentNameFilter.setLike("other-comp");
    f.setSourceComponentName(sourceComponentNameFilter);
    assertThat(f.matches(dep("dep-1"))).isFalse();
  }

  @Test
  void filterBySourceComponentName_nullOnEntity_noMatch() {
    Dependency d = mock(Dependency.class);
    when(d.getName()).thenReturn("dep-1");
    when(d.getSourceComponentName()).thenReturn(null);

    DependencyFilter f = new DependencyFilter();
    StringFilter sourceComponentNameFilter = new StringFilter();
    sourceComponentNameFilter.setLike("src-comp");
    f.setSourceComponentName(sourceComponentNameFilter);
    assertThat(f.matches(d)).isFalse();
  }

  @Test
  void filterByDestinationComponentName_match() {
    DependencyFilter f = new DependencyFilter();
    StringFilter destinationComponentNameFilter = new StringFilter();
    destinationComponentNameFilter.setLike("DST-COMP");
    f.setDestinationComponentName(destinationComponentNameFilter);
    assertThat(f.matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterByDestinationComponentName_noMatch() {
    DependencyFilter f = new DependencyFilter();
    StringFilter destinationComponentNameFilter = new StringFilter();
    destinationComponentNameFilter.setLike("other-comp");
    f.setDestinationComponentName(destinationComponentNameFilter);
    assertThat(f.matches(dep("dep-1"))).isFalse();
  }

  @Test
  void filterByDestinationComponentName_nullOnEntity_noMatch() {
    Dependency d = mock(Dependency.class);
    when(d.getName()).thenReturn("dep-1");
    when(d.getDestinationComponentName()).thenReturn(null);

    DependencyFilter f = new DependencyFilter();
    StringFilter destinationComponentNameFilter = new StringFilter();
    destinationComponentNameFilter.setLike("dst-comp");
    f.setDestinationComponentName(destinationComponentNameFilter);
    assertThat(f.matches(d)).isFalse();
  }

  @Test
  void filterByDependencyInstanceName_match() {
    DependencyFilter f = new DependencyFilter();
    StringFilter dependencyInstanceNameFilter = new StringFilter();
    dependencyInstanceNameFilter.setLike("DEP-INST-ALPHA");
    f.setDependencyInstanceName(dependencyInstanceNameFilter);
    assertThat(f.matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterByDependencyInstanceName_noMatch() {
    DependencyFilter f = new DependencyFilter();
    StringFilter dependencyInstanceNameFilter = new StringFilter();
    dependencyInstanceNameFilter.setLike("missing-inst");
    f.setDependencyInstanceName(dependencyInstanceNameFilter);
    assertThat(f.matches(dep("dep-1"))).isFalse();
  }

  @Test
  void filterByDependencyInstanceName_nullInstances_noMatch() {
    Dependency d = mock(Dependency.class);
    when(d.getName()).thenReturn("dep-1");
    when(d.getDependencyInstances()).thenReturn(null);

    DependencyFilter f = new DependencyFilter();
    StringFilter dependencyInstanceNameFilter = new StringFilter();
    dependencyInstanceNameFilter.setLike("dep-inst-alpha");
    f.setDependencyInstanceName(dependencyInstanceNameFilter);
    assertThat(f.matches(d)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter sourceComponentNameFilter = new StringFilter();
    sourceComponentNameFilter.setLike("src");
    StringFilter destinationComponentNameFilter = new StringFilter();
    destinationComponentNameFilter.setLike("dst");
    StringFilter dependencyInstanceNameFilter = new StringFilter();
    dependencyInstanceNameFilter.setLike("di");

    DependencyFilter f = new DependencyFilter();
    f.setName(stringFilter);
    f.setSourceComponentName(sourceComponentNameFilter);
    f.setDestinationComponentName(destinationComponentNameFilter);
    f.setDependencyInstanceName(dependencyInstanceNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getSourceComponentName()).isEqualTo(sourceComponentNameFilter);
    assertThat(f.getDestinationComponentName()).isEqualTo(destinationComponentNameFilter);
    assertThat(f.getDependencyInstanceName()).isEqualTo(dependencyInstanceNameFilter);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    DependencyFilter f = new DependencyFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    DependencyFilter f = new DependencyFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("dep-");
    f.setId(idFilter);
    assertThat(f.matches(dep("dep-1"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    DependencyFilter f = new DependencyFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(dep("dep-1"))).isFalse();
  }
}
