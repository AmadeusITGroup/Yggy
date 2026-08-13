package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Connection;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ConnectionFilter.NAME)
public class ConnectionFilter {
  public static final String NAME = "ConnectionFilter";
  public static final String PROVIDER = "connections";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter firewallLevel;
  private StringFilter securityControl;
  private StringFilter connectionType;
  private ZoneTypeFilter sourceZoneType;
  private ZoneTypeFilter destinationZoneType;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setFirewallLevel(StringFilter firewallLevel) {
    this.firewallLevel = firewallLevel;
  }

  public void setSecurityControl(StringFilter securityControl) {
    this.securityControl = securityControl;
  }

  public void setConnectionType(StringFilter connectionType) {
    this.connectionType = connectionType;
  }

  public void setSourceZoneType(ZoneTypeFilter sourceZoneType) {
    this.sourceZoneType = sourceZoneType;
  }

  public void setDestinationZoneType(ZoneTypeFilter destinationZoneType) {
    this.destinationZoneType = destinationZoneType;
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

  public StringFilter getFirewallLevel() {
    return firewallLevel;
  }

  public StringFilter getSecurityControl() {
    return securityControl;
  }

  public StringFilter getConnectionType() {
    return connectionType;
  }

  public ZoneTypeFilter getSourceZoneType() {
    return sourceZoneType;
  }

  public ZoneTypeFilter getDestinationZoneType() {
    return destinationZoneType;
  }

  public boolean matches(@Nullable Connection connection) {
    return connection != null
        && (id == null || id.matches(connection.id()))
        && (name == null || name.matches(connection.getName()))
        && (scope == null || scope.matches(connection.getScope()))
        && (firewallLevel == null
            || (connection.getFirewallLevel() != null
                && firewallLevel.matches(connection.getFirewallLevel().getValue())))
        && (securityControl == null
            || (connection.getSecurityControl() != null
                && securityControl.matches(connection.getSecurityControl().getValue())))
        && (connectionType == null
            || (connection.getConnectionType() != null
                && connectionType.matches(connection.getConnectionType().getValue())))
        && (sourceZoneType == null || sourceZoneType.matches(connection.getSourceZoneType()))
        && (destinationZoneType == null
            || destinationZoneType.matches(connection.getDestinationZoneType()));
  }
}
