package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(ApplicationIdFilter.NAME)
public class ApplicationIdFilter {
  public static final String NAME = "ApplicationIdFilter";
  public static final String PROVIDER = "applicationIds";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private List<ServiceFilter> services;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setServices(List<ServiceFilter> services) {
    this.services = services;
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

  public List<ServiceFilter> getServices() {
    return services;
  }

  public boolean matches(@Nullable ApplicationId applicationId) {
    return applicationId != null
        && (id == null || id.matches(applicationId.id()))
        && (name == null || name.matches(applicationId.getName()))
        && (scope == null || scope.matches(applicationId.getScope()))
        && FilterUtils.allFilterMatch(
            services, applicationId.getServices(), ServiceFilter::matches);
  }
}
