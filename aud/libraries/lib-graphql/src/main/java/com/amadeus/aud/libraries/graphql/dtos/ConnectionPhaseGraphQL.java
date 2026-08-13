package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("ConnectionPhaseDTO")
public record ConnectionPhaseGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String control,
    String sourcePhase,
    String destinationPhase) {
  public static @Nullable ConnectionPhaseGraphQL from(@Nullable ConnectionPhase cp) {
    if (cp == null) {
      return null;
    }
    return new ConnectionPhaseGraphQL(
        cp.id(),
        ObjectTypeListNamesEnum.CONNECTIONPHASES.getUri(),
        cp.getName(),
        ScopeGraphQL.from(cp.getScope()),
        cp.getControl() != null ? cp.getControl().getValue() : null,
        cp.getSourcePhaseName(),
        cp.getDestinationPhaseName());
  }
}
