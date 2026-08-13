package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.graphql.Type;

@Type("BlackListedURLDTO")
public record BlackListedURLGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String fqdn,
    List<String> transportLayer,
    List<String> ports) {

  public static @Nullable BlackListedURLGraphQL from(@Nullable BlackListedURL url) {
    if (url == null) {
      return null;
    }
    // Map each TransportLayerEnum to its wire value (e.g. "Tcp", "Udp", "Icmp")
    List<String> transportLayerValues = new ArrayList<>();
    if (url.getTransportLayer() != null) {
      for (V1BlackListedURLSpec.TransportLayerEnum t : url.getTransportLayer()) {
        transportLayerValues.add(t.getValue());
      }
    }

    return new BlackListedURLGraphQL(
        url.id(),
        ObjectTypeListNamesEnum.BLACKLISTEDURLS.getUri(),
        url.getName(),
        ScopeGraphQL.from(url.getScope()),
        url.getFqdn(),
        transportLayerValues,
        url.getPorts() == null ? List.of() : url.getPorts());
  }
}
