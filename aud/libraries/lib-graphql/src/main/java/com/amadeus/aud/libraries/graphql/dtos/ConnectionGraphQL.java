package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Connection;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("ConnectionDTO")
public record ConnectionGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String firewallLevel,
    String securityControl,
    String connectionType,
    ZoneTypeGraphQL sourceZoneType,
    ZoneTypeGraphQL destinationZoneType) {
  public static @Nullable ConnectionGraphQL from(@Nullable Connection c) {
    if (c == null) {
      return null;
    }
    return new ConnectionGraphQL(
        c.id(),
        ObjectTypeListNamesEnum.CONNECTIONS.getUri(),
        c.getName(),
        ScopeGraphQL.from(c.getScope()),
        c.getFirewallLevel() != null ? c.getFirewallLevel().getValue() : null,
        c.getSecurityControl() != null ? c.getSecurityControl().getValue() : null,
        c.getConnectionType() != null ? c.getConnectionType().getValue() : null,
        ZoneTypeGraphQL.from(c.getSourceZoneType()),
        ZoneTypeGraphQL.from(c.getDestinationZoneType()));
  }
}
