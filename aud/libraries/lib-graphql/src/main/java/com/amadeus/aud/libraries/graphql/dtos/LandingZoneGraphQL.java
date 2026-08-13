package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("LandingZoneDTO")
public record LandingZoneGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    TargetGraphQL target,
    String regionName,
    String landingZoneTypeName,
    MacroPhaseGraphQL macroPhase,
    List<String> cidrs,
    Set<String> functionalTypes,
    Set<ZoneInstanceGraphQL> zoneInstances,
    Set<String> subnetInstances,
    Set<String> componentInstances) {

  public static @Nullable LandingZoneGraphQL from(@Nullable LandingZone landingZone) {
    if (landingZone == null) {
      return null;
    }
    Set<ZoneInstanceGraphQL> zoneInstances = new HashSet<>();
    if (landingZone.getZoneInstances() != null) {
      for (var zoneInstance : landingZone.getZoneInstances()) {
        zoneInstances.add(ZoneInstanceGraphQL.from(zoneInstance));
      }
    }
    Set<String> subnetInstances = new HashSet<>();
    if (landingZone.getSubnetInstances() != null) {
      for (var subnetInstance : landingZone.getSubnetInstances()) {
        subnetInstances.add(subnetInstance.getName());
      }
    }
    Set<String> componentInstances = new HashSet<>();
    if (landingZone.getComponentInstances() != null) {
      for (var componentInstance : landingZone.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new LandingZoneGraphQL(
        landingZone.id(),
        ObjectTypeListNamesEnum.LANDINGZONES.getUri(),
        landingZone.getName(),
        ScopeGraphQL.from(landingZone.getScope()),
        TargetGraphQL.from(landingZone.getTarget()),
        landingZone.getRegionName(),
        landingZone.getLandingZoneTypeName(),
        MacroPhaseGraphQL.from(landingZone.getMacroPhase()),
        landingZone.getCidrs(),
        landingZone.getFunctionalTypeJsonRepresentation(),
        zoneInstances,
        subnetInstances,
        componentInstances);
  }
}
