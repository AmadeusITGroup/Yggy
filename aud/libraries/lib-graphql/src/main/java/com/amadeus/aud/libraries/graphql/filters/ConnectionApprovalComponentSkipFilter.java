package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ConnectionApprovalComponentSkipFilter.NAME)
public class ConnectionApprovalComponentSkipFilter {
  public static final String NAME = "ConnectionApprovalComponentSkipFilter";
  public static final String PROVIDER = "connectionApprovalComponentSkips";
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

  public ScopeFilter getScope() {
    return scope;
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

  public boolean matches(
      @Nullable ConnectionApprovalComponentSkip connectionApprovalComponentSkip) {
    return connectionApprovalComponentSkip != null
        && (id == null || id.matches(connectionApprovalComponentSkip.id()))
        && (name == null || name.matches(connectionApprovalComponentSkip.getName()))
        && (scope == null || scope.matches(connectionApprovalComponentSkip.getScope()))
        && FilterUtils.matchesBoolean(skipSource, connectionApprovalComponentSkip.getSkipSource())
        && FilterUtils.matchesBoolean(
            skipDestination, connectionApprovalComponentSkip.getSkipDestination())
        && (status == null || status.matches(connectionApprovalComponentSkip.getStatus()));
  }
}
