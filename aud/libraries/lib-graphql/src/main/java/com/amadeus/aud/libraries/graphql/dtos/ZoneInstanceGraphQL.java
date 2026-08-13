package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ZoneInstanceDTO")
public record ZoneInstanceGraphQL(
    String id,
    String type,
    String name,
    String scopeName,
    String landingZoneName,
    String zoneTypeName,
    String firewallGroupName,
    boolean isNsgNeeded,
    Set<String> subnetInstances,
    Set<String> componentInstances) {
  public static @Nullable ZoneInstanceGraphQL from(@Nullable ZoneInstance zoneInstance) {
    if (zoneInstance == null) {
      return null;
    }
    Set<String> subnetInstances = new HashSet<>();
    if (zoneInstance.getSubnetInstances() != null) {
      for (var subnetInstance : zoneInstance.getSubnetInstances()) {
        subnetInstances.add(subnetInstance.getName());
      }
    }
    Set<String> componentInstances = new HashSet<>();
    if (zoneInstance.getComponentInstances() != null) {
      for (var componentInstance : zoneInstance.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new ZoneInstanceGraphQL(
        zoneInstance.id(),
        ObjectTypeListNamesEnum.ZONEINSTANCES.getUri(),
        zoneInstance.getName(),
        zoneInstance.getScope() != null ? zoneInstance.getScope().getName() : null,
        zoneInstance.getLandingZoneName(),
        zoneInstance.getZoneTypeName(),
        zoneInstance.getFirewallGroupName(),
        zoneInstance.isNsgNeeded(),
        subnetInstances,
        componentInstances);
  }
}
