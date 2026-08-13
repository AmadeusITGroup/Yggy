package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("ConnectionApprovalComponentSkipDTO")
public record ConnectionApprovalComponentSkipGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isSkipSource,
    boolean isSkipDestination,
    String status) {
  public static @Nullable ConnectionApprovalComponentSkipGraphQL from(
      @Nullable ConnectionApprovalComponentSkip cacs) {
    if (cacs == null) {
      return null;
    }
    return new ConnectionApprovalComponentSkipGraphQL(
        cacs.id(),
        ObjectTypeListNamesEnum.CONNECTIONAPPROVALCOMPONENTSKIPS.getUri(),
        cacs.getName(),
        ScopeGraphQL.from(cacs.getScope()),
        cacs.getSkipSource(),
        cacs.getSkipDestination(),
        cacs.getStatus());
  }
}
