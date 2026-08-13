package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.graphql.Type;

@Type("BlackListedCIDRDTO")
public record BlackListedCIDRGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String cidr,
    List<String> transportLayer,
    List<String> ports) {

  public static @Nullable BlackListedCIDRGraphQL from(@Nullable BlackListedCIDR cidr) {
    if (cidr == null) {
      return null;
    }
    // Map each TransportLayerEnum to its wire value (e.g. "Tcp", "Udp", "Icmp")
    List<String> transportLayerValues = new ArrayList<>();
    if (cidr.getTransportLayer() != null) {
      for (V1BlackListedCIDRSpec.TransportLayerEnum t : cidr.getTransportLayer()) {
        transportLayerValues.add(t.getValue());
      }
    }

    return new BlackListedCIDRGraphQL(
        cidr.id(),
        ObjectTypeListNamesEnum.BLACKLISTEDCIDRS.getUri(),
        cidr.getName(),
        ScopeGraphQL.from(cidr.getScope()),
        cidr.getCidr(),
        transportLayerValues,
        cidr.getPorts() == null ? List.of() : cidr.getPorts());
  }
}
