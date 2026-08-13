package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(MacroPhaseFilter.NAME)
public class MacroPhaseFilter {
  public static final String NAME = "MacroPhaseFilter";
  public static final String PROVIDER = "macroPhases";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Integer automationOrder;
  private StringFilter applicationPhaseName;
  private StringFilter targetName;
  private StringFilter connectionPhaseName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setAutomationOrder(Integer automationOrder) {
    this.automationOrder = automationOrder;
  }

  public void setApplicationPhaseName(StringFilter applicationPhaseName) {
    this.applicationPhaseName = applicationPhaseName;
  }

  public void setTargetName(StringFilter targetName) {
    this.targetName = targetName;
  }

  public void setConnectionPhaseName(StringFilter connectionPhaseName) {
    this.connectionPhaseName = connectionPhaseName;
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

  public Integer getAutomationOrder() {
    return automationOrder;
  }

  public StringFilter getApplicationPhaseName() {
    return applicationPhaseName;
  }

  public StringFilter getTargetName() {
    return targetName;
  }

  public StringFilter getConnectionPhaseName() {
    return connectionPhaseName;
  }

  public boolean matches(@Nullable MacroPhase macroPhase) {
    return macroPhase != null
        && (id == null || id.matches(macroPhase.id()))
        && (name == null || name.matches(macroPhase.getName()))
        && (scope == null || scope.matches(macroPhase.getScope()))
        && (automationOrder == null || automationOrder.equals(macroPhase.getAutomationOrder()))
        && FilterUtils.anyStringFilterMatches(
            applicationPhaseName, macroPhase.getApplicationPhases(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            targetName, macroPhase.getTargets(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            connectionPhaseName, macroPhase.getConnectionPhases(), AbstractBase::getName);
  }
}
