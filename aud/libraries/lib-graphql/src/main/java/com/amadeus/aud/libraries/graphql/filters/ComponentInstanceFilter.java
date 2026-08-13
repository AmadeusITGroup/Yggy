package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ComponentInstanceFilter.NAME)
public class ComponentInstanceFilter {
  public static final String NAME = "ComponentInstanceFilter";
  public static final String PROVIDER = "componentInstances";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private ZoneTypeFilter zoneType;
  private StringFilter applicationPhaseName;
  private NamespaceFilter namespace;
  private StringFilter serviceTagName;
  private StringFilter deployComponentInstanceName;
  private StringFilter inSubnetInstanceName;
  private StringFilter outSubnetInstanceName;
  private ZoneInstanceFilter inZoneInstance;
  private ZoneInstanceFilter outZoneInstance;
  private StringFilter endpointType;
  private StringFilter deprecatedStatus;
  private StringFilter hostingPlatformType;
  private StringFilter k8sIdentifier;
  private Boolean isAutomaticallyInstantiated;
  private LandingZoneFilter landingZone;
  private StringFilter iaasComponentInstanceName;
  private ComponentFilter component;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setZoneType(ZoneTypeFilter zoneType) {
    this.zoneType = zoneType;
  }

  public void setApplicationPhaseName(StringFilter applicationPhaseName) {
    this.applicationPhaseName = applicationPhaseName;
  }

  public void setNamespace(NamespaceFilter namespace) {
    this.namespace = namespace;
  }

  public void setServiceTagName(StringFilter serviceTagName) {
    this.serviceTagName = serviceTagName;
  }

  public void setDeployComponentInstanceName(StringFilter deployComponentInstanceName) {
    this.deployComponentInstanceName = deployComponentInstanceName;
  }

  public void setInSubnetInstanceName(StringFilter inSubnetInstanceName) {
    this.inSubnetInstanceName = inSubnetInstanceName;
  }

  public void setOutSubnetInstanceName(StringFilter outSubnetInstanceName) {
    this.outSubnetInstanceName = outSubnetInstanceName;
  }

  public void setInZoneInstance(ZoneInstanceFilter inZoneInstance) {
    this.inZoneInstance = inZoneInstance;
  }

  public void setOutZoneInstance(ZoneInstanceFilter outZoneInstance) {
    this.outZoneInstance = outZoneInstance;
  }

  public void setEndpointType(StringFilter endpointType) {
    this.endpointType = endpointType;
  }

  public void setDeprecatedStatus(StringFilter deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public void setHostingPlatformType(StringFilter hostingPlatformType) {
    this.hostingPlatformType = hostingPlatformType;
  }

  public void setK8sIdentifier(StringFilter k8sIdentifier) {
    this.k8sIdentifier = k8sIdentifier;
  }

  public void setIsAutomaticallyInstantiated(Boolean isAutomaticallyInstantiated) {
    this.isAutomaticallyInstantiated = isAutomaticallyInstantiated;
  }

  public void setLandingZone(LandingZoneFilter landingZone) {
    this.landingZone = landingZone;
  }

  public void setIaasComponentInstanceName(StringFilter iaasComponentInstanceName) {
    this.iaasComponentInstanceName = iaasComponentInstanceName;
  }

  public void setComponent(ComponentFilter component) {
    this.component = component;
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

  public ZoneTypeFilter getZoneType() {
    return zoneType;
  }

  public StringFilter getApplicationPhaseName() {
    return applicationPhaseName;
  }

  public NamespaceFilter getNamespace() {
    return namespace;
  }

  public StringFilter getServiceTagName() {
    return serviceTagName;
  }

  public StringFilter getDeployComponentInstanceName() {
    return deployComponentInstanceName;
  }

  public StringFilter getInSubnetInstanceName() {
    return inSubnetInstanceName;
  }

  public StringFilter getOutSubnetInstanceName() {
    return outSubnetInstanceName;
  }

  public ZoneInstanceFilter getInZoneInstance() {
    return inZoneInstance;
  }

  public ZoneInstanceFilter getOutZoneInstance() {
    return outZoneInstance;
  }

  public StringFilter getEndpointType() {
    return endpointType;
  }

  public StringFilter getDeprecatedStatus() {
    return deprecatedStatus;
  }

  public StringFilter getHostingPlatformType() {
    return hostingPlatformType;
  }

  public StringFilter getK8sIdentifier() {
    return k8sIdentifier;
  }

  public Boolean getIsAutomaticallyInstantiated() {
    return isAutomaticallyInstantiated;
  }

  public LandingZoneFilter getLandingZone() {
    return landingZone;
  }

  public StringFilter getIaasComponentInstanceName() {
    return iaasComponentInstanceName;
  }

  public ComponentFilter getComponent() {
    return component;
  }

  public boolean matches(@Nullable ComponentInstance componentInstance) {
    return componentInstance != null
        && (id == null || id.matches(componentInstance.id()))
        && (name == null || name.matches(componentInstance.getName()))
        && (scope == null
            || scope.matches(
                componentInstance.getComponent() != null
                    ? componentInstance.getComponent().getScope()
                    : null))
        && (zoneType == null || zoneType.matches(componentInstance.getZoneType()))
        && (applicationPhaseName == null
            || applicationPhaseName.matches(componentInstance.getApplicationPhaseName()))
        && (namespace == null || namespace.matches(componentInstance.getNamespace()))
        && (serviceTagName == null || serviceTagName.matches(componentInstance.getServiceTagName()))
        && (deployComponentInstanceName == null
            || deployComponentInstanceName.matches(
                componentInstance.getDeployComponentInstanceName()))
        && (inSubnetInstanceName == null
            || inSubnetInstanceName.matches(componentInstance.getInSubnetInstanceName()))
        && (outSubnetInstanceName == null
            || outSubnetInstanceName.matches(componentInstance.getOutSubnetInstanceName()))
        && (inZoneInstance == null || inZoneInstance.matches(componentInstance.getInZoneInstance()))
        && (outZoneInstance == null
            || outZoneInstance.matches(componentInstance.getOutZoneInstance()))
        && (endpointType == null
            || endpointType.matches(
                componentInstance.getEndpointType() != null
                    ? componentInstance.getEndpointType().name()
                    : null))
        && (deprecatedStatus == null
            || deprecatedStatus.matches(
                componentInstance.getDeprecatedStatus() != null
                    ? componentInstance.getDeprecatedStatus().name()
                    : null))
        && (hostingPlatformType == null
            || hostingPlatformType.matches(
                componentInstance.getHostingPlatformType() != null
                    ? componentInstance.getHostingPlatformType().name()
                    : null))
        && (k8sIdentifier == null || k8sIdentifier.matches(componentInstance.getK8sIdentifier()))
        && FilterUtils.matchesBoolean(
            isAutomaticallyInstantiated, componentInstance.isAutomaticallyInstantiated())
        && (landingZone == null || landingZone.matches(componentInstance.getLandingZone()))
        && (iaasComponentInstanceName == null
            || iaasComponentInstanceName.matches(componentInstance.getHostingPlatformName()))
        && (component == null || component.matches(componentInstance.getComponent()));
  }
}
