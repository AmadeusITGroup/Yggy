package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.Endpoint;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("EndpointDTO")
public record EndpointGraphQL(String type, String value, String description) {

  public static @Nullable EndpointGraphQL from(@Nullable Endpoint endpoint) {
    if (endpoint == null) {
      return null;
    }
    return new EndpointGraphQL(endpoint.getType(), endpoint.getValue(), endpoint.getDescription());
  }
}
