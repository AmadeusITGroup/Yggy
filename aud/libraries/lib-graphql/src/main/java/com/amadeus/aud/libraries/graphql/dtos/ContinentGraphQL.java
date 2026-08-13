package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Continent;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Type;

@Type("ContinentDTO")
public record ContinentGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    List<String> regions,
    List<String> targets) {
  public static @Nullable ContinentGraphQL from(@Nullable Continent c) {
    if (c == null) {
      return null;
    }
    return new ContinentGraphQL(
        c.id(),
        ObjectTypeListNamesEnum.CONTINENTS.getUri(),
        c.getName(),
        ScopeGraphQL.from(c.getScope()),
        c.getRegionNames(),
        c.getTargetNames());
  }
}
