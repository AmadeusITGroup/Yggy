package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ConnectionPhaseFilter.NAME)
public class ConnectionPhaseFilter {
  public static final String NAME = "ConnectionPhaseFilter";
  public static final String PROVIDER = "connectionPhases";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter control;
  private StringFilter sourcePhase;
  private StringFilter destinationPhase;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
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

  public StringFilter getControl() {
    return control;
  }

  public StringFilter getSourcePhase() {
    return sourcePhase;
  }

  public StringFilter getDestinationPhase() {
    return destinationPhase;
  }

  public void setControl(StringFilter control) {
    this.control = control;
  }

  public void setSourcePhase(StringFilter sourcePhase) {
    this.sourcePhase = sourcePhase;
  }

  public void setDestinationPhase(StringFilter destinationPhase) {
    this.destinationPhase = destinationPhase;
  }

  public boolean matches(@Nullable ConnectionPhase connectionPhase) {
    return connectionPhase != null
        && (id == null || id.matches(connectionPhase.id()))
        && (name == null || name.matches(connectionPhase.getName()))
        && (scope == null || scope.matches(connectionPhase.getScope()))
        && (control == null
            || (connectionPhase.getControl() != null
                && control.matches(connectionPhase.getControl().getValue())))
        && (sourcePhase == null || sourcePhase.matches(connectionPhase.getSourcePhaseName()))
        && (destinationPhase == null
            || destinationPhase.matches(connectionPhase.getDestinationPhaseName()));
  }
}
