package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.Service;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ApplicationIdDTO")
public record ApplicationIdGraphQL(
    String id, String type, String name, ScopeGraphQL scope, Set<ServiceGraphQL> services) {
  public static @Nullable ApplicationIdGraphQL from(@Nullable ApplicationId applicationId) {
    if (applicationId == null) {
      return null;
    }
    Set<ServiceGraphQL> services = new HashSet<>();
    if (applicationId.getServices() != null) {
      for (Service service : applicationId.getServices()) {
        services.add(ServiceGraphQL.from(service));
      }
    }
    return new ApplicationIdGraphQL(
        applicationId.id(),
        ObjectTypeListNamesEnum.APPLICATIONIDS.getUri(),
        applicationId.getName(),
        ScopeGraphQL.from(applicationId.getScope()),
        services);
  }
}
