package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NamespaceFilterTest {

  private Namespace ns(String name) {
    Namespace n = mock(Namespace.class);
    when(n.getName()).thenReturn(name);
    when(n.id()).thenReturn("ns-" + name.hashCode());
    when(n.isShared()).thenReturn(true);
    when(n.isAutomaticallyGenerated()).thenReturn(false);

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("scope-prod");
    when(n.getAuthorizedScopes()).thenReturn(Set.of(scope));

    ComponentInstance ci = mock(ComponentInstance.class);
    when(ci.getName()).thenReturn("ci-app");
    when(n.getComponentInstances()).thenReturn(Set.of(ci));

    return n;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new NamespaceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new NamespaceFilter().matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByName_match() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("default");
    f.setName(stringFilter);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }

  @Test
  void filterByIsShared_match() {
    NamespaceFilter f = new NamespaceFilter();
    f.setIsShared(true);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByIsShared_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    f.setIsShared(false);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }

  @Test
  void filterByIsAutomaticallyGenerated_match() {
    NamespaceFilter f = new NamespaceFilter();
    f.setIsAutomaticallyGenerated(false);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByIsAutomaticallyGenerated_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    f.setIsAutomaticallyGenerated(true);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }

  @Test
  void filterByAuthorizedScopeName_match() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter authorizedScopeNameFilter = new StringFilter();
    authorizedScopeNameFilter.setLike("SCOPE-PROD");
    f.setAuthorizedScopeName(authorizedScopeNameFilter);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByAuthorizedScopeName_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter authorizedScopeNameFilter = new StringFilter();
    authorizedScopeNameFilter.setLike("scope-dev");
    f.setAuthorizedScopeName(authorizedScopeNameFilter);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }

  @Test
  void filterByAuthorizedScopeName_nullScopes_noMatch() {
    Namespace n = mock(Namespace.class);
    when(n.getName()).thenReturn("ns-1");
    when(n.getAuthorizedScopes()).thenReturn(null);

    NamespaceFilter f = new NamespaceFilter();
    StringFilter authorizedScopeNameFilter = new StringFilter();
    authorizedScopeNameFilter.setLike("scope-prod");
    f.setAuthorizedScopeName(authorizedScopeNameFilter);
    assertThat(f.matches(n)).isFalse();
  }

  @Test
  void filterByComponentInstanceName_match() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("CI-APP");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterByComponentInstanceName_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-missing");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }

  @Test
  void filterByComponentInstanceName_nullInstances_noMatch() {
    Namespace n = mock(Namespace.class);
    when(n.getName()).thenReturn("ns-1");
    when(n.getComponentInstances()).thenReturn(null);

    NamespaceFilter f = new NamespaceFilter();
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci-app");
    f.setComponentInstanceName(componentInstanceNameFilter);
    assertThat(f.matches(n)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    StringFilter authorizedScopeNameFilter = new StringFilter();
    authorizedScopeNameFilter.setLike("s");
    StringFilter componentInstanceNameFilter = new StringFilter();
    componentInstanceNameFilter.setLike("ci");

    NamespaceFilter f = new NamespaceFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    f.setIsShared(true);
    f.setIsAutomaticallyGenerated(false);
    f.setAuthorizedScopeName(authorizedScopeNameFilter);
    f.setComponentInstanceName(componentInstanceNameFilter);

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getIsShared()).isTrue();
    assertThat(f.getIsAutomaticallyGenerated()).isFalse();
    assertThat(f.getAuthorizedScopeName()).isEqualTo(authorizedScopeNameFilter);
    assertThat(f.getComponentInstanceName()).isEqualTo(componentInstanceNameFilter);
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    NamespaceFilter f = new NamespaceFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void filterById_match() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("ns-");
    f.setId(idFilter);
    assertThat(f.matches(ns("ns-default"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    NamespaceFilter f = new NamespaceFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(ns("ns-default"))).isFalse();
  }
}
