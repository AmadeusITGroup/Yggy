package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(SubnetInstanceFilter.NAME)
public class SubnetInstanceFilter {
  public static final String NAME = "SubnetInstanceFilter";
  public static final String PROVIDER = "subnetInstances";
  private StringFilter id;
  private StringFilter name;
  private StringFilter scope;
  private StringFilter landingZoneName;
  private StringFilter vlan;
  private Boolean isShared;
  private Boolean isGroup;
  private Boolean isNetworkPrivate;
  private StringFilter cidr;
  private StringFilter subnetTypeName;
  private StringFilter zoneInstanceName;
  private StringFilter parentName;
  private StringFilter deprecatedStatus;
  private StringFilter deprecatedMessage;
  private StringFilter childName;
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

  public void setLandingZoneName(StringFilter landingZoneName) {
    this.landingZoneName = landingZoneName;
  }

  public void setVlan(StringFilter vlan) {
    this.vlan = vlan;
  }

  public void setIsShared(Boolean isShared) {
    this.isShared = isShared;
  }

  public void setIsGroup(Boolean isGroup) {
    this.isGroup = isGroup;
  }

  public void setIsNetworkPrivate(Boolean isNetworkPrivate) {
    this.isNetworkPrivate = isNetworkPrivate;
  }

  public void setCidr(StringFilter cidr) {
    this.cidr = cidr;
  }

  public void setSubnetTypeName(StringFilter subnetTypeName) {
    this.subnetTypeName = subnetTypeName;
  }

  public void setZoneInstanceName(StringFilter zoneInstanceName) {
    this.zoneInstanceName = zoneInstanceName;
  }

  public void setParentName(StringFilter parentName) {
    this.parentName = parentName;
  }

  public void setDeprecatedStatus(StringFilter deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public void setDeprecatedMessage(StringFilter deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
  }

  public void setChildName(StringFilter childName) {
    this.childName = childName;
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

  public StringFilter getLandingZoneName() {
    return landingZoneName;
  }

  public StringFilter getVlan() {
    return vlan;
  }

  public Boolean getIsShared() {
    return isShared;
  }

  public Boolean getIsGroup() {
    return isGroup;
  }

  public Boolean getIsNetworkPrivate() {
    return isNetworkPrivate;
  }

  public StringFilter getCidr() {
    return cidr;
  }

  public StringFilter getSubnetTypeName() {
    return subnetTypeName;
  }

  public StringFilter getZoneInstanceName() {
    return zoneInstanceName;
  }

  public StringFilter getParentName() {
    return parentName;
  }

  public StringFilter getDeprecatedStatus() {
    return deprecatedStatus;
  }

  public StringFilter getDeprecatedMessage() {
    return deprecatedMessage;
  }

  public StringFilter getChildName() {
    return childName;
  }

  public StringFilter getComponentInstanceName() {
    return componentInstanceName;
  }

  public boolean matches(@Nullable SubnetInstance subnetInstance) {
    return subnetInstance != null
        && (id == null || id.matches(subnetInstance.id()))
        && (name == null || name.matches(subnetInstance.getName()))
        && (scope == null || scope.matches(subnetInstance.getScopeName()))
        && (landingZoneName == null || landingZoneName.matches(subnetInstance.getLandingZoneName()))
        && (vlan == null || vlan.matches(subnetInstance.getVlan()))
        && FilterUtils.matchesBoolean(isShared, subnetInstance.isShared())
        && FilterUtils.matchesBoolean(isGroup, subnetInstance.isGroup())
        && FilterUtils.matchesBoolean(isNetworkPrivate, subnetInstance.isNetworkPrivate())
        && (cidr == null || cidr.matches(subnetInstance.getCidr()))
        && (subnetTypeName == null || subnetTypeName.matches(subnetInstance.getSubnetTypeName()))
        && (zoneInstanceName == null
            || zoneInstanceName.matches(subnetInstance.getZoneInstanceName()))
        && (parentName == null || parentName.matches(subnetInstance.getParentName()))
        && (deprecatedStatus == null
            || deprecatedStatus.matches(
                subnetInstance.getDeprecatedStatus() != null
                    ? subnetInstance.getDeprecatedStatus().name()
                    : null))
        && (deprecatedMessage == null
            || deprecatedMessage.matches(subnetInstance.getDeprecatedMessage()))
        && FilterUtils.anyStringFilterMatches(
            childName, subnetInstance.getChildren(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentInstanceName, subnetInstance.getComponentInstances(), AbstractBase::getName);
  }
}
