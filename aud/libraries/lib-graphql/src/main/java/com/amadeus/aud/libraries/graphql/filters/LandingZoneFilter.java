package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(LandingZoneFilter.NAME)
public class LandingZoneFilter {
  public static final String NAME = "LandingZoneFilter";
  public static final String PROVIDER = "landingZones";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private TargetFilter target;
  private StringFilter regionName;
  private StringFilter landingZoneTypeName;
  private MacroPhaseFilter macroPhase;
  private StringFilter functionalType;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setTarget(TargetFilter target) {
    this.target = target;
  }

  public void setRegionName(StringFilter regionName) {
    this.regionName = regionName;
  }

  public void setLandingZoneTypeName(StringFilter landingZoneTypeName) {
    this.landingZoneTypeName = landingZoneTypeName;
  }

  public void setMacroPhase(MacroPhaseFilter macroPhase) {
    this.macroPhase = macroPhase;
  }

  public void setFunctionalType(StringFilter functionalType) {
    this.functionalType = functionalType;
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

  public TargetFilter getTarget() {
    return target;
  }

  public StringFilter getRegionName() {
    return regionName;
  }

  public StringFilter getLandingZoneTypeName() {
    return landingZoneTypeName;
  }

  public MacroPhaseFilter getMacroPhase() {
    return macroPhase;
  }

  public StringFilter getFunctionalType() {
    return functionalType;
  }

  public boolean matches(@Nullable LandingZone landingZone) {
    return landingZone != null
        && (id == null || id.matches(landingZone.id()))
        && (name == null || name.matches(landingZone.getName()))
        && (scope == null || scope.matches(landingZone.getScope()))
        && (regionName == null || regionName.matches(landingZone.getRegionName()))
        && (landingZoneTypeName == null
            || landingZoneTypeName.matches(landingZone.getLandingZoneTypeName()))
        && (macroPhase == null || macroPhase.matches(landingZone.getMacroPhase()))
        && FilterUtils.anyStringFilterMatches(
            functionalType, landingZone.getFunctionalTypeJsonRepresentation(), ft -> ft)
        && (target == null || target.matches(landingZone.getTarget()));
  }
}
