package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Target;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(TargetFilter.NAME)
public class TargetFilter {
  public static final String NAME = "TargetFilter";
  public static final String PROVIDER = "targets";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter providerName;
  private StringFilter targetTypeName;
  private MacroPhaseFilter macroPhase;
  private StringFilter continentName;
  private StringFilter popPrimaryName;
  private StringFilter popSecondaryName;
  private Boolean isCloud;
  private Boolean isAmadeus;
  private Boolean isAudManaged;
  private Boolean isEnableOfficeConnectivity;
  private Boolean isEnableJumpServerAccess;
  private Boolean isEnableVnetToVnet;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setProviderName(StringFilter providerName) {
    this.providerName = providerName;
  }

  public void setTargetTypeName(StringFilter targetTypeName) {
    this.targetTypeName = targetTypeName;
  }

  public void setMacroPhase(MacroPhaseFilter macroPhase) {
    this.macroPhase = macroPhase;
  }

  public void setContinentName(StringFilter continentName) {
    this.continentName = continentName;
  }

  public void setPopPrimaryName(StringFilter popPrimaryName) {
    this.popPrimaryName = popPrimaryName;
  }

  public void setPopSecondaryName(StringFilter popSecondaryName) {
    this.popSecondaryName = popSecondaryName;
  }

  public void setIsCloud(Boolean isCloud) {
    this.isCloud = isCloud;
  }

  public void setIsAmadeus(Boolean isAmadeus) {
    this.isAmadeus = isAmadeus;
  }

  public void setIsAudManaged(Boolean isAudManaged) {
    this.isAudManaged = isAudManaged;
  }

  public void setIsEnableOfficeConnectivity(Boolean isEnableOfficeConnectivity) {
    this.isEnableOfficeConnectivity = isEnableOfficeConnectivity;
  }

  public void setIsEnableJumpServerAccess(Boolean isEnableJumpServerAccess) {
    this.isEnableJumpServerAccess = isEnableJumpServerAccess;
  }

  public void setIsEnableVnetToVnet(Boolean isEnableVnetToVnet) {
    this.isEnableVnetToVnet = isEnableVnetToVnet;
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

  public StringFilter getProviderName() {
    return providerName;
  }

  public StringFilter getTargetTypeName() {
    return targetTypeName;
  }

  public MacroPhaseFilter getMacroPhase() {
    return macroPhase;
  }

  public StringFilter getContinentName() {
    return continentName;
  }

  public StringFilter getPopPrimaryName() {
    return popPrimaryName;
  }

  public StringFilter getPopSecondaryName() {
    return popSecondaryName;
  }

  public Boolean getIsCloud() {
    return isCloud;
  }

  public Boolean getIsAmadeus() {
    return isAmadeus;
  }

  public Boolean getIsAudManaged() {
    return isAudManaged;
  }

  public Boolean getIsEnableOfficeConnectivity() {
    return isEnableOfficeConnectivity;
  }

  public Boolean getIsEnableJumpServerAccess() {
    return isEnableJumpServerAccess;
  }

  public Boolean getIsEnableVnetToVnet() {
    return isEnableVnetToVnet;
  }

  public boolean matches(@Nullable Target target) {
    return target != null
        && (id == null || id.matches(target.id()))
        && (name == null || name.matches(target.getName()))
        && (scope == null || scope.matches(target.getScope()))
        && (providerName == null || providerName.matches(target.getProviderName()))
        && (targetTypeName == null || targetTypeName.matches(target.getTargetTypeName()))
        && (macroPhase == null || macroPhase.matches(target.getMacroPhase()))
        && (continentName == null || continentName.matches(target.getContinentName()))
        && (popPrimaryName == null || popPrimaryName.matches(target.getPopPrimaryName()))
        && (popSecondaryName == null || popSecondaryName.matches(target.getPopSecondaryName()))
        && FilterUtils.matchesBoolean(isCloud, target.isCloud())
        && FilterUtils.matchesBoolean(isAmadeus, target.isAmadeus())
        && FilterUtils.matchesBoolean(isAudManaged, target.isAudManaged())
        && FilterUtils.matchesBoolean(
            isEnableOfficeConnectivity, target.isEnableOfficeConnectivity())
        && FilterUtils.matchesBoolean(isEnableJumpServerAccess, target.isEnableJumpServerAccess())
        && FilterUtils.matchesBoolean(isEnableVnetToVnet, target.isEnableVnetToVnet());
  }
}
