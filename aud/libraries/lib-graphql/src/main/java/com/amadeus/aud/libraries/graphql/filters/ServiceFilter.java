package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.google.common.collect.Range;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ServiceFilter.NAME)
public class ServiceFilter {
  public static final String NAME = "ServiceFilter";
  public static final String PROVIDER = "services";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter componentName;
  private StringFilter applicationIdName;
  private StringFilter serviceNowId;
  private StringFilter protocol;
  private StringFilter approvedProtocol;
  private StringFilter deprecatedStatus;
  private Boolean isPrivate;
  private Boolean isPci;
  private Boolean isPii;
  private Boolean isEncrypted;
  private Boolean isDbSynchro;
  private StringFilter port;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setComponentName(StringFilter componentName) {
    this.componentName = componentName;
  }

  public void setApplicationIdName(StringFilter applicationIdName) {
    this.applicationIdName = applicationIdName;
  }

  public void setServiceNowId(StringFilter serviceNowId) {
    this.serviceNowId = serviceNowId;
  }

  public void setProtocol(StringFilter protocol) {
    this.protocol = protocol;
  }

  public void setApprovedProtocol(StringFilter approvedProtocol) {
    this.approvedProtocol = approvedProtocol;
  }

  public void setDeprecatedStatus(StringFilter deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public void setIsPrivate(Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public void setIsPci(Boolean isPci) {
    this.isPci = isPci;
  }

  public void setIsPii(Boolean isPii) {
    this.isPii = isPii;
  }

  public void setIsEncrypted(Boolean isEncrypted) {
    this.isEncrypted = isEncrypted;
  }

  public void setIsDbSynchro(Boolean isDbSynchro) {
    this.isDbSynchro = isDbSynchro;
  }

  public void setPort(StringFilter port) {
    this.port = port;
  }

  public StringFilter getPort() {
    return this.port;
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

  public StringFilter getComponentName() {
    return componentName;
  }

  public StringFilter getApplicationIdName() {
    return applicationIdName;
  }

  public StringFilter getServiceNowId() {
    return serviceNowId;
  }

  public StringFilter getProtocol() {
    return protocol;
  }

  public StringFilter getApprovedProtocol() {
    return approvedProtocol;
  }

  public StringFilter getDeprecatedStatus() {
    return deprecatedStatus;
  }

  public Boolean getIsPrivate() {
    return isPrivate;
  }

  public Boolean getIsPci() {
    return isPci;
  }

  public Boolean getIsPii() {
    return isPii;
  }

  public Boolean getIsEncrypted() {
    return isEncrypted;
  }

  public Boolean getIsDbSynchro() {
    return isDbSynchro;
  }

  public boolean matches(@Nullable Service service) {
    return service != null
        && (id == null || id.matches(service.id()))
        && (name == null || name.matches(service.getName()))
        && (scope == null || scope.matches(service.getScope()))
        && (componentName == null || componentName.matches(service.getComponentName()))
        && (applicationIdName == null || applicationIdName.matches(service.getApplicationIdName()))
        && (serviceNowId == null || serviceNowId.matches(service.getServiceNowId()))
        && (protocol == null
            || protocol.matches(
                service.getProtocol() != null ? service.getProtocol().getValue() : null))
        && (approvedProtocol == null
            || approvedProtocol.matches(
                service.getApprovedProtocol() != null
                    ? service.getApprovedProtocol().getValue()
                    : null))
        && (deprecatedStatus == null
            || deprecatedStatus.matches(
                service.getDeprecatedStatus() != null
                    ? service.getDeprecatedStatus().getValue()
                    : null))
        && FilterUtils.matchesBoolean(isPrivate, service.isPrivate())
        && FilterUtils.matchesBoolean(isPci, service.isPci())
        && FilterUtils.matchesBoolean(isPii, service.isPii())
        && FilterUtils.matchesBoolean(isEncrypted, service.isEncrypted())
        && FilterUtils.matchesBoolean(isDbSynchro, service.isDbSynchro())
        && matchesPortFilter(service);
  }

  private boolean matchesPortFilter(Service service) {
    if (port == null) {
      return true;
    }

    if (service.getPorts() == null || service.getPortsRange() == null) {
      return false;
    }

    String eq = port.getEq();
    if (eq != null) {
      Range<Integer> requestedPort = PortUtils.toRange(eq.trim());
      return requestedPort != null && service.getPortsRange().encloses(requestedPort);
    }

    String like = port.getLike();
    if (like != null && like.matches("\\d+")) {
      Range<Integer> requestedPort = PortUtils.toRange(like.trim());
      return requestedPort != null && service.getPortsRange().encloses(requestedPort);
    }

    return port.matches(service.getPorts());
  }
}
