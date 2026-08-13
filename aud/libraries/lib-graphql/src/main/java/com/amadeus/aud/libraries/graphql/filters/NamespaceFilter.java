package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(NamespaceFilter.NAME)
public class NamespaceFilter {
  public static final String NAME = "NamespaceFilter";
  public static final String PROVIDER = "namespaces";
  private StringFilter id;
  private StringFilter name;
  private StringFilter scope;
  private Boolean isShared;
  private Boolean isAutomaticallyGenerated;
  private StringFilter authorizedScopeName;
  private StringFilter componentInstanceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(StringFilter scope) {
    this.scope = scope;
  }

  public void setIsShared(Boolean isShared) {
    this.isShared = isShared;
  }

  public void setIsAutomaticallyGenerated(Boolean isAutomaticallyGenerated) {
    this.isAutomaticallyGenerated = isAutomaticallyGenerated;
  }

  public void setAuthorizedScopeName(StringFilter authorizedScopeName) {
    this.authorizedScopeName = authorizedScopeName;
  }

  public void setComponentInstanceName(StringFilter componentInstanceName) {
    this.componentInstanceName = componentInstanceName;
  }

  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public StringFilter getScope() {
    return scope;
  }

  public Boolean getIsShared() {
    return isShared;
  }

  public Boolean getIsAutomaticallyGenerated() {
    return isAutomaticallyGenerated;
  }

  public StringFilter getAuthorizedScopeName() {
    return authorizedScopeName;
  }

  public StringFilter getComponentInstanceName() {
    return componentInstanceName;
  }

  public boolean matches(@Nullable Namespace namespace) {
    return namespace != null
        && (id == null || id.matches(namespace.id()))
        && (name == null || name.matches(namespace.getName()))
        && (scope == null || scope.matches(namespace.getScopeName()))
        && FilterUtils.matchesBoolean(isShared, namespace.isShared())
        && FilterUtils.matchesBoolean(
            isAutomaticallyGenerated, namespace.isAutomaticallyGenerated())
        && FilterUtils.anyStringFilterMatches(
            authorizedScopeName, namespace.getAuthorizedScopes(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentInstanceName, namespace.getComponentInstances(), AbstractBase::getName);
  }
}
