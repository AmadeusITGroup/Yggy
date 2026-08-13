package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ZoneInstanceFilter.NAME)
public class ZoneInstanceFilter {
  public static final String NAME = "ZoneInstanceFilter";
  public static final String PROVIDER = "zoneInstances";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter landingZoneName;
  private StringFilter zoneTypeName;
  private StringFilter firewallGroupName;
  private Boolean isNsgNeeded;
  private StringFilter subnetInstanceName;
  private StringFilter componentInstanceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setLandingZoneName(StringFilter landingZoneName) {
    this.landingZoneName = landingZoneName;
  }

  public void setZoneTypeName(StringFilter zoneTypeName) {
    this.zoneTypeName = zoneTypeName;
  }

  public void setFirewallGroupName(StringFilter firewallGroupName) {
    this.firewallGroupName = firewallGroupName;
  }

  public void setIsNsgNeeded(Boolean isNsgNeeded) {
    this.isNsgNeeded = isNsgNeeded;
  }

  public void setSubnetInstanceName(StringFilter subnetInstanceName) {
    this.subnetInstanceName = subnetInstanceName;
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

  public ScopeFilter getScope() {
    return scope;
  }

  public StringFilter getLandingZoneName() {
    return landingZoneName;
  }

  public StringFilter getZoneTypeName() {
    return zoneTypeName;
  }

  public StringFilter getFirewallGroupName() {
    return firewallGroupName;
  }

  public Boolean getIsNsgNeeded() {
    return isNsgNeeded;
  }

  public StringFilter getSubnetInstanceName() {
    return subnetInstanceName;
  }

  public StringFilter getComponentInstanceName() {
    return componentInstanceName;
  }

  public boolean matches(@Nullable ZoneInstance zoneInstance) {
    return zoneInstance != null
        && (id == null || id.matches(zoneInstance.id()))
        && (name == null || name.matches(zoneInstance.getName()))
        && (scope == null || scope.matches(zoneInstance.getScope()))
        && (landingZoneName == null || landingZoneName.matches(zoneInstance.getLandingZoneName()))
        && (zoneTypeName == null || zoneTypeName.matches(zoneInstance.getZoneTypeName()))
        && (firewallGroupName == null
            || firewallGroupName.matches(zoneInstance.getFirewallGroupName()))
        && FilterUtils.matchesBoolean(isNsgNeeded, zoneInstance.isNsgNeeded())
        && FilterUtils.anyStringFilterMatches(
            subnetInstanceName, zoneInstance.getSubnetInstances(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentInstanceName, zoneInstance.getComponentInstances(), AbstractBase::getName);
  }
}
