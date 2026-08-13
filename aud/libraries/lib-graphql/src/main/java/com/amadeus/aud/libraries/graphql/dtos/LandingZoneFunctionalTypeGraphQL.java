package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("LandingZoneFunctionalTypeDTO")
public record LandingZoneFunctionalTypeGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isGenerated,
    Set<LandingZoneGraphQL> landingZones) {
  public static @Nullable LandingZoneFunctionalTypeGraphQL from(
      @Nullable LandingZoneFunctionalType landingZoneFunctionalType) {
    if (landingZoneFunctionalType == null) {
      return null;
    }
    Set<LandingZoneGraphQL> landingZones = new HashSet<>();
    if (landingZoneFunctionalType.getLandingZones() != null) {
      for (LandingZone landingZone : landingZoneFunctionalType.getLandingZones()) {
        landingZones.add(LandingZoneGraphQL.from(landingZone));
      }
    }
    return new LandingZoneFunctionalTypeGraphQL(
        landingZoneFunctionalType.id(),
        ObjectTypeListNamesEnum.LANDINGZONEFUNCTIONALTYPES.getUri(),
        landingZoneFunctionalType.getName(),
        ScopeGraphQL.from(landingZoneFunctionalType.getScope()),
        landingZoneFunctionalType.isGenerated(),
        landingZones);
  }
}
