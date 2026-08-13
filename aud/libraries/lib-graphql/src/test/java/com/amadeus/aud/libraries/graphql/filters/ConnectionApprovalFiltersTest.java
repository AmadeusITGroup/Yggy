package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import org.junit.jupiter.api.Test;

class ConnectionApprovalFiltersTest {

  private StringFilter sf(String value) {
    StringFilter sf = new StringFilter();
    sf.setLike(value);
    return sf;
  }

  // ---------- ConnectionApprovalComponentSkipFilter ----------

  private ConnectionApprovalComponentSkip cacs() {
    ConnectionApprovalComponentSkip cacs = mock(ConnectionApprovalComponentSkip.class);
    when(cacs.getName()).thenReturn("skip-comp-1");
    when(cacs.id()).thenReturn("cacs-" + "skip-comp-1".hashCode());
    when(cacs.getSkipSource()).thenReturn(true);
    when(cacs.getSkipDestination()).thenReturn(false);
    when(cacs.getStatus()).thenReturn("active");
    return cacs;
  }

  @Test
  void componentSkip_matchesNull_returnsFalse() {
    assertThat(new ConnectionApprovalComponentSkipFilter().matches(null)).isFalse();
  }

  @Test
  void componentSkip_emptyFilter_matchesAll() {
    assertThat(new ConnectionApprovalComponentSkipFilter().matches(cacs())).isTrue();
  }

  @Test
  void componentSkip_filterBySkipSource_match() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    f.setSkipSource(true);
    assertThat(f.matches(cacs())).isTrue();
  }

  @Test
  void componentSkip_filterBySkipSource_noMatch() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    f.setSkipSource(false);
    assertThat(f.matches(cacs())).isFalse();
  }

  @Test
  void componentSkip_filterBySkipDestination_noMatch() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    f.setSkipDestination(true);
    assertThat(f.matches(cacs())).isFalse();
  }

  @Test
  void componentSkip_filterByStatus_match() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    StringFilter statusFilter = new StringFilter();
    statusFilter.setLike("active");
    f.setStatus(statusFilter);
    assertThat(f.matches(cacs())).isTrue();
  }

  @Test
  void componentSkip_filterByStatus_noMatch() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    StringFilter statusFilter = new StringFilter();
    statusFilter.setLike("inactive");
    f.setStatus(statusFilter);
    assertThat(f.matches(cacs())).isFalse();
  }

  // ---------- ConnectionApprovalScopeSkipFilter ----------

  private ConnectionApprovalScopeSkip cass() {
    ConnectionApprovalScopeSkip cass = mock(ConnectionApprovalScopeSkip.class);
    when(cass.getName()).thenReturn("skip-scope-1");
    when(cass.id()).thenReturn("cass-" + "skip-scope-1".hashCode());
    when(cass.getSkipSource()).thenReturn(false);
    when(cass.getSkipDestination()).thenReturn(true);
    when(cass.getStatus()).thenReturn("active");
    return cass;
  }

  @Test
  void scopeSkip_matchesNull_returnsFalse() {
    assertThat(new ConnectionApprovalScopeSkipFilter().matches(null)).isFalse();
  }

  @Test
  void scopeSkip_emptyFilter_matchesAll() {
    assertThat(new ConnectionApprovalScopeSkipFilter().matches(cass())).isTrue();
  }

  @Test
  void scopeSkip_filterByName_match() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("scope");
    f.setName(stringFilter);
    assertThat(f.matches(cass())).isTrue();
  }

  @Test
  void scopeSkip_filterBySkipDestination_match() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    f.setSkipDestination(true);
    assertThat(f.matches(cass())).isTrue();
  }

  @Test
  void scopeSkip_filterBySkipDestination_noMatch() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    f.setSkipDestination(false);
    assertThat(f.matches(cass())).isFalse();
  }

  @Test
  void scopeSkip_filterByStatus_noMatch() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    StringFilter statusFilter = new StringFilter();
    statusFilter.setLike("inactive");
    f.setStatus(statusFilter);
    assertThat(f.matches(cass())).isFalse();
  }

  @Test
  void componentSkip_filterById_match() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("cacs-");
    f.setId(idFilter);
    assertThat(f.matches(cacs())).isTrue();
  }

  @Test
  void componentSkip_filterById_noMatch() {
    ConnectionApprovalComponentSkipFilter f = new ConnectionApprovalComponentSkipFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(cacs())).isFalse();
  }

  @Test
  void scopeSkip_filterById_match() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("cass-");
    f.setId(idFilter);
    assertThat(f.matches(cass())).isTrue();
  }

  @Test
  void scopeSkip_filterById_noMatch() {
    ConnectionApprovalScopeSkipFilter f = new ConnectionApprovalScopeSkipFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(cass())).isFalse();
  }
}
