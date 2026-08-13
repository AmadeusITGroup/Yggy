package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("IPAddressInstanceDTO")
public record IPAddressInstanceGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String host,
    String advertisedTo,
    String componentInstance) {
  public static @Nullable IPAddressInstanceGraphQL from(@Nullable IPAddressInstance ip) {
    if (ip == null) {
      return null;
    }
    return new IPAddressInstanceGraphQL(
        ip.id(),
        ObjectTypeListNamesEnum.IPADDRESSINSTANCES.getUri(),
        ip.getName(),
        ScopeGraphQL.from(ip.getScope()),
        ip.getHost(),
        ip.getAdvertisedTo() != null ? ip.getAdvertisedTo().getValue() : null,
        ip.getComponentInstanceName());
  }
}
