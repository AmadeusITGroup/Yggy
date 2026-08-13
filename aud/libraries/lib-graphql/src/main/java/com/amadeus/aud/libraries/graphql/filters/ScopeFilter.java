package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ScopeFilter.NAME)
public class ScopeFilter {
  public static final String NAME = "ScopeFilter";
  public static final String PROVIDER = "scopes";
  private StringFilter id;
  private StringFilter name;
  private StringFilter serviceNowId;
  private StringFilter nanoId;
  private StringFilter assigneeGroup;
  private StringFilter repositoryVersion;
  private Boolean isDeleted;
  private StringFilter owner;
  private StringFilter role;
  private StringFilter targetName;
  private StringFilter landingZoneName;
  private StringFilter componentName;
  private StringFilter componentInstanceName;
  private StringFilter zoneInstanceName;
  private StringFilter subnetInstanceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setServiceNowId(StringFilter serviceNowId) {
    this.serviceNowId = serviceNowId;
  }

  public void setNanoId(StringFilter nanoId) {
    this.nanoId = nanoId;
  }

  public void setAssigneeGroup(StringFilter assigneeGroup) {
    this.assigneeGroup = assigneeGroup;
  }

  public void setRepositoryVersion(StringFilter repositoryVersion) {
    this.repositoryVersion = repositoryVersion;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public void setOwner(StringFilter owner) {
    this.owner = owner;
  }

  public void setRole(StringFilter role) {
    this.role = role;
  }

  public void setTargetName(StringFilter targetName) {
    this.targetName = targetName;
  }

  public void setLandingZoneName(StringFilter landingZoneName) {
    this.landingZoneName = landingZoneName;
  }

  public void setComponentName(StringFilter componentName) {
    this.componentName = componentName;
  }

  public void setComponentInstanceName(StringFilter componentInstanceName) {
    this.componentInstanceName = componentInstanceName;
  }

  public void setZoneInstanceName(StringFilter zoneInstanceName) {
    this.zoneInstanceName = zoneInstanceName;
  }

  public void setSubnetInstanceName(StringFilter subnetInstanceName) {
    this.subnetInstanceName = subnetInstanceName;
  }

  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public StringFilter getServiceNowId() {
    return serviceNowId;
  }

  public StringFilter getNanoId() {
    return nanoId;
  }

  public StringFilter getAssigneeGroup() {
    return assigneeGroup;
  }

  public StringFilter getRepositoryVersion() {
    return repositoryVersion;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public StringFilter getOwner() {
    return owner;
  }

  public StringFilter getRole() {
    return role;
  }

  public StringFilter getTargetName() {
    return targetName;
  }

  public StringFilter getLandingZoneName() {
    return landingZoneName;
  }

  public StringFilter getComponentName() {
    return componentName;
  }

  public StringFilter getComponentInstanceName() {
    return componentInstanceName;
  }

  public StringFilter getZoneInstanceName() {
    return zoneInstanceName;
  }

  public StringFilter getSubnetInstanceName() {
    return subnetInstanceName;
  }

  public boolean matches(@Nullable Scope scope) {
    return scope != null
        && (id == null || id.matches(scope.id()))
        && (name == null || name.matches(scope.getName()))
        && (serviceNowId == null || serviceNowId.matches(scope.getServiceNowId()))
        && (nanoId == null || nanoId.matches(scope.getNanoId()))
        && (assigneeGroup == null || assigneeGroup.matches(scope.getAssigneeGroup()))
        && (repositoryVersion == null || repositoryVersion.matches(scope.getRepositoryVersion()))
        && FilterUtils.matchesBoolean(isDeleted, scope.isDeleted())
        && FilterUtils.anyStringFilterMatches(owner, scope.getOwners(), o -> o)
        && FilterUtils.anyStringFilterMatches(role, scope.getRolesJsonRepresentation(), r -> r)
        && FilterUtils.anyStringFilterMatches(targetName, scope.getTargets(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            landingZoneName, scope.getLandingZones(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentName, scope.getComponents(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentInstanceName, scope.getComponentInstances(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            zoneInstanceName, scope.getZoneInstances(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            subnetInstanceName, scope.getSubnetInstances(), AbstractBase::getName);
  }
}
