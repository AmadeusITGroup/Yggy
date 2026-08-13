package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("LandingZoneTypeDTO")
public record LandingZoneTypeGraphQL(
    String id, String type, String name, ScopeGraphQL scope, Set<LandingZoneGraphQL> landingZones) {
  public static @Nullable LandingZoneTypeGraphQL from(@Nullable LandingZoneType landingZoneType) {
    if (landingZoneType == null) {
      return null;
    }
    Set<LandingZoneGraphQL> landingZones = new HashSet<>();
    if (landingZoneType.getLandingZones() != null) {
      for (var landingZone : landingZoneType.getLandingZones()) {
        landingZones.add(LandingZoneGraphQL.from(landingZone));
      }
    }
    return new LandingZoneTypeGraphQL(
        landingZoneType.id(),
        ObjectTypeListNamesEnum.LANDINGZONETYPES.getUri(),
        landingZoneType.getName(),
        ScopeGraphQL.from(landingZoneType.getScope()),
        landingZones);
  }
}
