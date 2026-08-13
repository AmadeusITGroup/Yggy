package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(ApplicationPhaseFilter.NAME)
public class ApplicationPhaseFilter {
  public static final String NAME = "ApplicationPhaseFilter";
  public static final String PROVIDER = "applicationPhases";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Boolean isCustomerFacing;
  private MacroPhaseFilter macroPhase;
  private List<StringFilter> componentInstances;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setIsCustomerFacing(Boolean isCustomerFacing) {
    this.isCustomerFacing = isCustomerFacing;
  }

  public void setMacroPhase(MacroPhaseFilter macroPhase) {
    this.macroPhase = macroPhase;
  }

  public void setComponentInstances(List<StringFilter> componentInstances) {
    this.componentInstances = componentInstances;
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

  public Boolean getIsCustomerFacing() {
    return isCustomerFacing;
  }

  public MacroPhaseFilter getMacroPhase() {
    return macroPhase;
  }

  public List<StringFilter> getComponentInstances() {
    return componentInstances;
  }

  public boolean matches(@Nullable ApplicationPhase applicationPhase) {
    return applicationPhase != null
        && (id == null || id.matches(applicationPhase.id()))
        && (name == null || name.matches(applicationPhase.getName()))
        && (scope == null || scope.matches(applicationPhase.getScope()))
        && FilterUtils.matchesBoolean(isCustomerFacing, applicationPhase.isCustomerFacing())
        && (macroPhase == null || macroPhase.matches(applicationPhase.getMacroPhase()))
        && (componentInstances == null
            || componentInstances.isEmpty()
            || (applicationPhase.getComponentInstances() != null
                && componentInstances.stream()
                    .allMatch(
                        ci ->
                            FilterUtils.anyStringFilterMatches(
                                ci,
                                applicationPhase.getComponentInstances(),
                                AbstractBase::getName))));
  }
}
