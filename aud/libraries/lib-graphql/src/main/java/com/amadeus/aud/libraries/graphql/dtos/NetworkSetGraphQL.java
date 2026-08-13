package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.NetworkSet;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Type;

@Type("NetworkSetDTO")
public record NetworkSetGraphQL(
    String name, List<EndpointGraphQL> endpoints, Map<String, String> labels) {

  public static @Nullable NetworkSetGraphQL from(@Nullable NetworkSet networkSet) {
    if (networkSet == null) {
      return null;
    }
    List<EndpointGraphQL> endpoints = new ArrayList<>();
    if (networkSet.getEndpoints() != null) {
      for (Endpoint endpoint : networkSet.getEndpoints()) {
        endpoints.add(EndpointGraphQL.from(endpoint));
      }
    }
    return new NetworkSetGraphQL(networkSet.getName(), endpoints, networkSet.getLabels());
  }
}
