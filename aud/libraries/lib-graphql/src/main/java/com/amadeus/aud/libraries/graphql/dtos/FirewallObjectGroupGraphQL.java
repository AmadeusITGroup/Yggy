package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Type;

@Type("FirewallObjectGroupDTO")
public record FirewallObjectGroupGraphQL(
    String name,
    String uid,
    String type,
    String zoneType,
    List<EndpointGraphQL> endpoints,
    String owner,
    Map<String, String> labels) {

  @SuppressWarnings("NullAway")
  public static @Nullable FirewallObjectGroupGraphQL from(@Nullable ObjectGroup objectGroup) {
    if (objectGroup == null) {
      return null;
    }
    List<EndpointGraphQL> endpoints = new ArrayList<>();
    if (objectGroup.getEndpoints() != null) {
      for (Endpoint endpoint : objectGroup.getEndpoints()) {
        endpoints.add(EndpointGraphQL.from(endpoint));
      }
    }
    return new FirewallObjectGroupGraphQL(
        objectGroup.getName(),
        objectGroup.getUid(),
        objectGroup.getType() != null ? objectGroup.getType().getValue() : null,
        objectGroup.getZoneType(),
        endpoints,
        objectGroup.getOwner(),
        objectGroup.getLabels());
  }
}
