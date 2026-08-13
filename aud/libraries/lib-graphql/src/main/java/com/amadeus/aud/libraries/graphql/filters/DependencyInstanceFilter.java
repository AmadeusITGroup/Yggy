package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(DependencyInstanceFilter.NAME)
public class DependencyInstanceFilter {
  public static final String NAME = "DependencyInstanceFilter";
  public static final String PROVIDER = "dependencyInstances";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter dependencyName;
  private StringFilter sourceComponentInstanceName;
  private StringFilter targetComponentInstanceName;
  private StringFilter viaComponentInstanceName;
  private StringFilter viaServiceName;
  private Boolean isSourceAsComponent;
  private Boolean isSourceLimitedToLegacyOnly;
  private Boolean isSourceLimitedToCloudOnly;
  private Boolean isSourceLimitedToLandingZone;
  private Boolean isSourceLimitedToTarget;
  private Boolean isSourceLimitedToContinent;
  private Boolean isSourceLimitedToMacroPhase;
  private Boolean isSourceLimitedToSelector;
  private Boolean isTargetAsComponent;
  private Boolean isTargetLimitedToLegacyOnly;
  private Boolean isTargetLimitedToCloudOnly;
  private Boolean isTargetLimitedToSourceNamespace;
  private Boolean isTargetLimitedToSourcePaas;
  private Boolean isTargetLimitedToSourceSecurityZone;
  private Boolean isTargetLimitedToSourceApplicationPhase;
  private Boolean isTargetLimitedToSourceLandingZone;
  private Boolean isTargetLimitedToSourceTarget;
  private Boolean isTargetLimitedToSourceContinent;
  private Boolean isTargetLimitedToSourceMacroPhase;
  private StringFilter serviceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setDependencyName(StringFilter dependencyName) {
    this.dependencyName = dependencyName;
  }

  public void setSourceComponentInstanceName(StringFilter sourceComponentInstanceName) {
    this.sourceComponentInstanceName = sourceComponentInstanceName;
  }

  public void setTargetComponentInstanceName(StringFilter targetComponentInstanceName) {
    this.targetComponentInstanceName = targetComponentInstanceName;
  }

  public void setViaComponentInstanceName(StringFilter viaComponentInstanceName) {
    this.viaComponentInstanceName = viaComponentInstanceName;
  }

  public void setViaServiceName(StringFilter viaServiceName) {
    this.viaServiceName = viaServiceName;
  }

  public void setIsSourceAsComponent(Boolean isSourceAsComponent) {
    this.isSourceAsComponent = isSourceAsComponent;
  }

  public void setIsSourceLimitedToLegacyOnly(Boolean isSourceLimitedToLegacyOnly) {
    this.isSourceLimitedToLegacyOnly = isSourceLimitedToLegacyOnly;
  }

  public void setIsSourceLimitedToCloudOnly(Boolean isSourceLimitedToCloudOnly) {
    this.isSourceLimitedToCloudOnly = isSourceLimitedToCloudOnly;
  }

  public void setIsSourceLimitedToLandingZone(Boolean isSourceLimitedToLandingZone) {
    this.isSourceLimitedToLandingZone = isSourceLimitedToLandingZone;
  }

  public void setIsSourceLimitedToTarget(Boolean isSourceLimitedToTarget) {
    this.isSourceLimitedToTarget = isSourceLimitedToTarget;
  }

  public void setIsSourceLimitedToContinent(Boolean isSourceLimitedToContinent) {
    this.isSourceLimitedToContinent = isSourceLimitedToContinent;
  }

  public void setIsSourceLimitedToMacroPhase(Boolean isSourceLimitedToMacroPhase) {
    this.isSourceLimitedToMacroPhase = isSourceLimitedToMacroPhase;
  }

  public void setIsSourceLimitedToSelector(Boolean isSourceLimitedToSelector) {
    this.isSourceLimitedToSelector = isSourceLimitedToSelector;
  }

  public void setIsTargetAsComponent(Boolean isTargetAsComponent) {
    this.isTargetAsComponent = isTargetAsComponent;
  }

  public void setIsTargetLimitedToLegacyOnly(Boolean isTargetLimitedToLegacyOnly) {
    this.isTargetLimitedToLegacyOnly = isTargetLimitedToLegacyOnly;
  }

  public void setIsTargetLimitedToCloudOnly(Boolean isTargetLimitedToCloudOnly) {
    this.isTargetLimitedToCloudOnly = isTargetLimitedToCloudOnly;
  }

  public void setIsTargetLimitedToSourceNamespace(Boolean isTargetLimitedToSourceNamespace) {
    this.isTargetLimitedToSourceNamespace = isTargetLimitedToSourceNamespace;
  }

  public void setIsTargetLimitedToSourcePaas(Boolean isTargetLimitedToSourcePaas) {
    this.isTargetLimitedToSourcePaas = isTargetLimitedToSourcePaas;
  }

  public void setIsTargetLimitedToSourceSecurityZone(Boolean isTargetLimitedToSourceSecurityZone) {
    this.isTargetLimitedToSourceSecurityZone = isTargetLimitedToSourceSecurityZone;
  }

  public void setIsTargetLimitedToSourceApplicationPhase(
      Boolean isTargetLimitedToSourceApplicationPhase) {
    this.isTargetLimitedToSourceApplicationPhase = isTargetLimitedToSourceApplicationPhase;
  }

  public void setIsTargetLimitedToSourceLandingZone(Boolean isTargetLimitedToSourceLandingZone) {
    this.isTargetLimitedToSourceLandingZone = isTargetLimitedToSourceLandingZone;
  }

  public void setIsTargetLimitedToSourceTarget(Boolean isTargetLimitedToSourceTarget) {
    this.isTargetLimitedToSourceTarget = isTargetLimitedToSourceTarget;
  }

  public void setIsTargetLimitedToSourceContinent(Boolean isTargetLimitedToSourceContinent) {
    this.isTargetLimitedToSourceContinent = isTargetLimitedToSourceContinent;
  }

  public void setIsTargetLimitedToSourceMacroPhase(Boolean isTargetLimitedToSourceMacroPhase) {
    this.isTargetLimitedToSourceMacroPhase = isTargetLimitedToSourceMacroPhase;
  }

  public void setServiceName(StringFilter serviceName) {
    this.serviceName = serviceName;
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

  public StringFilter getDependencyName() {
    return dependencyName;
  }

  public StringFilter getSourceComponentInstanceName() {
    return sourceComponentInstanceName;
  }

  public StringFilter getTargetComponentInstanceName() {
    return targetComponentInstanceName;
  }

  public StringFilter getViaComponentInstanceName() {
    return viaComponentInstanceName;
  }

  public StringFilter getViaServiceName() {
    return viaServiceName;
  }

  public Boolean getIsSourceAsComponent() {
    return isSourceAsComponent;
  }

  public Boolean getIsSourceLimitedToLegacyOnly() {
    return isSourceLimitedToLegacyOnly;
  }

  public Boolean getIsSourceLimitedToCloudOnly() {
    return isSourceLimitedToCloudOnly;
  }

  public Boolean getIsSourceLimitedToLandingZone() {
    return isSourceLimitedToLandingZone;
  }

  public Boolean getIsSourceLimitedToTarget() {
    return isSourceLimitedToTarget;
  }

  public Boolean getIsSourceLimitedToContinent() {
    return isSourceLimitedToContinent;
  }

  public Boolean getIsSourceLimitedToMacroPhase() {
    return isSourceLimitedToMacroPhase;
  }

  public Boolean getIsSourceLimitedToSelector() {
    return isSourceLimitedToSelector;
  }

  public Boolean getIsTargetAsComponent() {
    return isTargetAsComponent;
  }

  public Boolean getIsTargetLimitedToLegacyOnly() {
    return isTargetLimitedToLegacyOnly;
  }

  public Boolean getIsTargetLimitedToCloudOnly() {
    return isTargetLimitedToCloudOnly;
  }

  public Boolean getIsTargetLimitedToSourceNamespace() {
    return isTargetLimitedToSourceNamespace;
  }

  public Boolean getIsTargetLimitedToSourcePaas() {
    return isTargetLimitedToSourcePaas;
  }

  public Boolean getIsTargetLimitedToSourceSecurityZone() {
    return isTargetLimitedToSourceSecurityZone;
  }

  public Boolean getIsTargetLimitedToSourceApplicationPhase() {
    return isTargetLimitedToSourceApplicationPhase;
  }

  public Boolean getIsTargetLimitedToSourceLandingZone() {
    return isTargetLimitedToSourceLandingZone;
  }

  public Boolean getIsTargetLimitedToSourceTarget() {
    return isTargetLimitedToSourceTarget;
  }

  public Boolean getIsTargetLimitedToSourceContinent() {
    return isTargetLimitedToSourceContinent;
  }

  public Boolean getIsTargetLimitedToSourceMacroPhase() {
    return isTargetLimitedToSourceMacroPhase;
  }

  public StringFilter getServiceName() {
    return serviceName;
  }

  public boolean matches(@Nullable DependencyInstance dependencyInstance) {
    return dependencyInstance != null
        && (id == null || id.matches(dependencyInstance.id()))
        && (name == null || name.matches(dependencyInstance.getName()))
        && (scope == null || scope.matches(dependencyInstance.getScope()))
        && (dependencyName == null
            || dependencyName.matches(dependencyInstance.getDependencyName()))
        && (sourceComponentInstanceName == null
            || sourceComponentInstanceName.matches(
                dependencyInstance.getSourceComponentInstanceName()))
        && (targetComponentInstanceName == null
            || targetComponentInstanceName.matches(
                dependencyInstance.getTargetComponentInstanceName()))
        && (viaComponentInstanceName == null
            || viaComponentInstanceName.matches(dependencyInstance.getViaComponentInstanceName()))
        && (viaServiceName == null
            || viaServiceName.matches(dependencyInstance.getViaServiceName()))
        && FilterUtils.matchesBoolean(isSourceAsComponent, dependencyInstance.isSourceAsComponent())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToLegacyOnly, dependencyInstance.isSourceLimitedToLegacyOnly())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToCloudOnly, dependencyInstance.isSourceLimitedToCloudOnly())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToLandingZone, dependencyInstance.isSourceLimitedToLandingZone())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToTarget, dependencyInstance.isSourceLimitedToTarget())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToContinent, dependencyInstance.isSourceLimitedToContinent())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToMacroPhase, dependencyInstance.isSourceLimitedToMacroPhase())
        && FilterUtils.matchesBoolean(
            isSourceLimitedToSelector, dependencyInstance.isSourceLimitedToSelector())
        && FilterUtils.matchesBoolean(isTargetAsComponent, dependencyInstance.isTargetAsComponent())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToLegacyOnly, dependencyInstance.isTargetLimitedToLegacyOnly())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToCloudOnly, dependencyInstance.isTargetLimitedToCloudOnly())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceNamespace, dependencyInstance.isTargetLimitedToSourceNamespace())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourcePaas, dependencyInstance.isTargetLimitedToSourcePaas())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceSecurityZone,
            dependencyInstance.isTargetLimitedToSourceSecurityZone())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceApplicationPhase,
            dependencyInstance.isTargetLimitedToSourceApplicationPhase())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceLandingZone,
            dependencyInstance.isTargetLimitedToSourceLandingZone())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceTarget, dependencyInstance.isTargetLimitedToSourceTarget())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceContinent, dependencyInstance.isTargetLimitedToSourceContinent())
        && FilterUtils.matchesBoolean(
            isTargetLimitedToSourceMacroPhase,
            dependencyInstance.isTargetLimitedToSourceMacroPhase())
        && FilterUtils.anyStringFilterMatches(
            serviceName, dependencyInstance.getServiceNames(), s -> s);
  }
}
