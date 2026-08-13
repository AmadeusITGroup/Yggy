package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("ConnectionApprovalScopeSkipDTO")
public record ConnectionApprovalScopeSkipGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isSkipSource,
    boolean isSkipDestination,
    String status) {
  public static @Nullable ConnectionApprovalScopeSkipGraphQL from(
      @Nullable ConnectionApprovalScopeSkip cass) {
    if (cass == null) {
      return null;
    }
    return new ConnectionApprovalScopeSkipGraphQL(
        cass.id(),
        ObjectTypeListNamesEnum.CONNECTIONAPPROVALSCOPESKIPS.getUri(),
        cass.getName(),
        ScopeGraphQL.from(cass.getScope()),
        cass.getSkipSource(),
        cass.getSkipDestination(),
        cass.getStatus());
  }
}
