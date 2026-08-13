package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import java.util.Set;
import org.eclipse.microprofile.graphql.Input;

@Input(ComponentFilter.NAME)
public class ComponentFilter {
  public static final String NAME = "ComponentFilter";
  public static final String PROVIDER = "components";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter deprecatedStatus;
  private Boolean isPublic;
  private Boolean isPrivate;
  private Boolean isHosting;
  private Boolean isHosted;
  private Boolean isTechnical;
  private Boolean isContainer;
  private Boolean isInterface;
  private StringFilter securityZone;
  private StringFilter serviceNowId;
  private Set<ZoneTypeFilter> zoneTypes;
  private StringFilter parentName;
  private StringFilter childName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setDeprecatedStatus(StringFilter deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public void setIsPrivate(Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public void setIsHosting(Boolean isHosting) {
    this.isHosting = isHosting;
  }

  public void setIsHosted(Boolean isHosted) {
    this.isHosted = isHosted;
  }

  public void setIsTechnical(Boolean isTechnical) {
    this.isTechnical = isTechnical;
  }

  public void setIsContainer(Boolean isContainer) {
    this.isContainer = isContainer;
  }

  public void setIsInterface(Boolean isInterface) {
    this.isInterface = isInterface;
  }

  public void setSecurityZone(StringFilter securityZone) {
    this.securityZone = securityZone;
  }

  public void setServiceNowId(StringFilter serviceNowId) {
    this.serviceNowId = serviceNowId;
  }

  public void setZoneTypes(Set<ZoneTypeFilter> zoneTypes) {
    this.zoneTypes = zoneTypes;
  }

  public void setParentName(StringFilter parentName) {
    this.parentName = parentName;
  }

  public void setChildName(StringFilter childName) {
    this.childName = childName;
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

  public StringFilter getDeprecatedStatus() {
    return deprecatedStatus;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public Boolean getIsPrivate() {
    return isPrivate;
  }

  public Boolean getIsHosting() {
    return isHosting;
  }

  public Boolean getIsHosted() {
    return isHosted;
  }

  public Boolean getIsTechnical() {
    return isTechnical;
  }

  public Boolean getIsContainer() {
    return isContainer;
  }

  public Boolean getIsInterface() {
    return isInterface;
  }

  public StringFilter getSecurityZone() {
    return securityZone;
  }

  public StringFilter getServiceNowId() {
    return serviceNowId;
  }

  public Set<ZoneTypeFilter> getZoneTypes() {
    return zoneTypes;
  }

  public StringFilter getParentName() {
    return parentName;
  }

  public StringFilter getChildName() {
    return childName;
  }

  public boolean matches(@Nullable Component component) {
    return component != null
        && (id == null || id.matches(component.id()))
        && (name == null || name.matches(component.getName()))
        && (scope == null || scope.matches(component.getScope()))
        && (deprecatedStatus == null
            || deprecatedStatus.matches(
                component.getDeprecatedStatus() != null
                    ? component.getDeprecatedStatus().name()
                    : null))
        && FilterUtils.matchesBoolean(isPublic, component.isPublic())
        && FilterUtils.matchesBoolean(isPrivate, component.isPrivate())
        && FilterUtils.matchesBoolean(isHosting, component.isHosting())
        && FilterUtils.matchesBoolean(isHosted, component.isHosted())
        && FilterUtils.matchesBoolean(isTechnical, component.isTechnical())
        && FilterUtils.matchesBoolean(isContainer, component.isContainer())
        && FilterUtils.matchesBoolean(isInterface, component.isInterface())
        && FilterUtils.anyStringFilterMatches(securityZone, component.getSecurityZones(), s -> s)
        && FilterUtils.anyStringFilterMatches(serviceNowId, component.getServiceNowIds(), s -> s)
        && FilterUtils.allFilterMatch(zoneTypes, component.getZoneTypes(), ZoneTypeFilter::matches)
        && FilterUtils.anyStringFilterMatches(parentName, component.getParentsNames(), s -> s)
        && FilterUtils.anyStringFilterMatches(
            childName, component.getChildren(), AbstractBase::getName);
  }
}
