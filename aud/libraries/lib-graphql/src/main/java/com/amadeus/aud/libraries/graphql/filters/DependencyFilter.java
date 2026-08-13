package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(DependencyFilter.NAME)
public class DependencyFilter {
  public static final String NAME = "DependencyFilter";
  public static final String PROVIDER = "dependencies";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter sourceComponentName;
  private StringFilter destinationComponentName;
  private StringFilter dependencyInstanceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setSourceComponentName(StringFilter sourceComponentName) {
    this.sourceComponentName = sourceComponentName;
  }

  public void setDestinationComponentName(StringFilter destinationComponentName) {
    this.destinationComponentName = destinationComponentName;
  }

  public void setDependencyInstanceName(StringFilter dependencyInstanceName) {
    this.dependencyInstanceName = dependencyInstanceName;
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

  public StringFilter getSourceComponentName() {
    return sourceComponentName;
  }

  public StringFilter getDestinationComponentName() {
    return destinationComponentName;
  }

  public StringFilter getDependencyInstanceName() {
    return dependencyInstanceName;
  }

  public boolean matches(@Nullable Dependency dependency) {
    return dependency != null
        && (id == null || id.matches(dependency.id()))
        && (name == null || name.matches(dependency.getName()))
        && (scope == null || scope.matches(dependency.getScope()))
        && (sourceComponentName == null
            || sourceComponentName.matches(dependency.getSourceComponentName()))
        && (destinationComponentName == null
            || destinationComponentName.matches(dependency.getDestinationComponentName()))
        && FilterUtils.anyStringFilterMatches(
            dependencyInstanceName, dependency.getDependencyInstances(), AbstractBase::getName);
  }
}
