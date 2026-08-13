package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ZoneTypeDTO")
public record ZoneTypeGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isTechnical,
    boolean isReal,
    boolean isPaas,
    String componentName,
    Set<String> connections,
    Set<ZoneInstanceGraphQL> zoneInstances,
    Set<String> componentInstances) {
  public static @Nullable ZoneTypeGraphQL from(@Nullable ZoneType zoneType) {
    if (zoneType == null) {
      return null;
    }
    Set<String> connections = new HashSet<>();
    if (zoneType.getConnections() != null) {
      for (var connection : zoneType.getConnections()) {
        connections.add(connection.getName());
      }
    }
    Set<ZoneInstanceGraphQL> zoneInstances = new HashSet<>();
    if (zoneType.getZoneInstances() != null) {
      for (var zoneInstance : zoneType.getZoneInstances()) {
        zoneInstances.add(ZoneInstanceGraphQL.from(zoneInstance));
      }
    }
    Set<String> componentInstances = new HashSet<>();
    if (zoneType.getComponentInstances() != null) {
      for (var componentInstance : zoneType.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new ZoneTypeGraphQL(
        zoneType.id(),
        ObjectTypeListNamesEnum.ZONETYPES.getUri(),
        zoneType.getName(),
        ScopeGraphQL.from(zoneType.getScope()),
        zoneType.isTechnical(),
        zoneType.isReal(),
        zoneType.isPaas(),
        zoneType.getComponent() != null ? zoneType.getComponent().getName() : null,
        connections,
        zoneInstances,
        componentInstances);
  }
}
