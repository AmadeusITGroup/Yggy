package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ZoneTypeFilter.NAME)
public class ZoneTypeFilter {
  public static final String NAME = "ZoneTypeFilter";
  public static final String PROVIDER = "zoneTypes";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Boolean isTechnical;
  private Boolean isReal;
  private Boolean isPaas;
  private StringFilter componentName;
  private StringFilter connectionName;
  private StringFilter zoneInstanceName;
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

  public void setIsTechnical(Boolean isTechnical) {
    this.isTechnical = isTechnical;
  }

  public void setIsReal(Boolean isReal) {
    this.isReal = isReal;
  }

  public void setIsPaas(Boolean isPaas) {
    this.isPaas = isPaas;
  }

  public void setComponentName(StringFilter componentName) {
    this.componentName = componentName;
  }

  public void setConnectionName(StringFilter connectionName) {
    this.connectionName = connectionName;
  }

  public void setZoneInstanceName(StringFilter zoneInstanceName) {
    this.zoneInstanceName = zoneInstanceName;
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

  public Boolean getIsTechnical() {
    return isTechnical;
  }

  public Boolean getIsReal() {
    return isReal;
  }

  public Boolean getIsPaas() {
    return isPaas;
  }

  public StringFilter getComponentName() {
    return componentName;
  }

  public StringFilter getConnectionName() {
    return connectionName;
  }

  public StringFilter getZoneInstanceName() {
    return zoneInstanceName;
  }

  public StringFilter getComponentInstanceName() {
    return componentInstanceName;
  }

  public boolean matches(@Nullable ZoneType zoneType) {
    return zoneType != null
        && (id == null || id.matches(zoneType.id()))
        && (name == null || name.matches(zoneType.getName()))
        && (scope == null || scope.matches(zoneType.getScope()))
        && FilterUtils.matchesBoolean(isTechnical, zoneType.isTechnical())
        && FilterUtils.matchesBoolean(isReal, zoneType.isReal())
        && FilterUtils.matchesBoolean(isPaas, zoneType.isPaas())
        && FilterUtils.stringFilterMatches(
            componentName, zoneType.getComponent(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            connectionName, zoneType.getConnections(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            zoneInstanceName, zoneType.getZoneInstances(), AbstractBase::getName)
        && FilterUtils.anyStringFilterMatches(
            componentInstanceName, zoneType.getComponentInstances(), AbstractBase::getName);
  }
}
