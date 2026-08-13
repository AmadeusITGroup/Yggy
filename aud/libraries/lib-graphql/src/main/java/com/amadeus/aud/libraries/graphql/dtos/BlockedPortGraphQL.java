package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("BlockedPortDTO")
public record BlockedPortGraphQL(
    String id, String type, String name, ScopeGraphQL scope, Integer port, String protocol) {

  public static @Nullable BlockedPortGraphQL from(@Nullable BlockedPort bp) {
    if (bp == null) {
      return null;
    }
    return new BlockedPortGraphQL(
        bp.id(),
        ObjectTypeListNamesEnum.BLOCKEDPORTS.getUri(),
        bp.getName(),
        ScopeGraphQL.from(bp.getScope()),
        bp.getPort(),
        bp.getProtocol() != null ? bp.getProtocol().getValue() : null);
  }
}
