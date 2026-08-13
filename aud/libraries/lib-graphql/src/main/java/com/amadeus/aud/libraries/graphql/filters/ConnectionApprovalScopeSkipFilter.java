package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ConnectionApprovalScopeSkipFilter.NAME)
public class ConnectionApprovalScopeSkipFilter {
  public static final String NAME = "ConnectionApprovalScopeSkipFilter";
  public static final String PROVIDER = "connectionApprovalScopeSkips";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Boolean skipSource;
  private Boolean skipDestination;
  private StringFilter status;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setSkipSource(Boolean skipSource) {
    this.skipSource = skipSource;
  }

  public void setSkipDestination(Boolean skipDestination) {
    this.skipDestination = skipDestination;
  }

  public void setStatus(StringFilter status) {
    this.status = status;
  }

  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public Boolean getSkipSource() {
    return skipSource;
  }

  public Boolean getSkipDestination() {
    return skipDestination;
  }

  public StringFilter getStatus() {
    return status;
  }

  public boolean matches(@Nullable ConnectionApprovalScopeSkip connectionApprovalScopeSkip) {
    return connectionApprovalScopeSkip != null
        && (id == null || id.matches(connectionApprovalScopeSkip.id()))
        && (name == null || name.matches(connectionApprovalScopeSkip.getName()))
        && (scope == null || scope.matches(connectionApprovalScopeSkip.getScope()))
        && FilterUtils.matchesBoolean(skipSource, connectionApprovalScopeSkip.getSkipSource())
        && FilterUtils.matchesBoolean(
            skipDestination, connectionApprovalScopeSkip.getSkipDestination())
        && (status == null || status.matches(connectionApprovalScopeSkip.getStatus()));
  }
}
