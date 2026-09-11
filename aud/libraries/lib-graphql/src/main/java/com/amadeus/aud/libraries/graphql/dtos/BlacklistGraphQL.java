package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("BlacklistDTO")
public record BlacklistGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String sourceComponent,
    Set<String> destinationHostFqdns,
    Set<String> destinationHostCidrs,
    Set<String> sourceZones,
    Set<String> sourceMacrophases,
    Set<String> sourceTargets,
    Set<String> destinationApplicationProtocols,
    Set<String> destinationTransportProtocols,
    Set<String> destinationPortRanges) {

  public static @Nullable BlacklistGraphQL from(@Nullable Blacklist blacklist) {
    if (blacklist == null) {
      return null;
    }

    Set<String> sourceZones = new HashSet<>();
    if (blacklist.getSourceZones() != null) {
      sourceZones.addAll(blacklist.getSourceZones());
    }

    Set<String> sourceMacrophases = new HashSet<>();
    if (blacklist.getSourceMacrophases() != null) {
      sourceMacrophases.addAll(blacklist.getSourceMacrophases());
    }

    Set<String> sourceTargets = new HashSet<>();
    if (blacklist.getSourceTargets() != null) {
      sourceTargets.addAll(blacklist.getSourceTargets());
    }

    Set<String> destinationApplicationProtocols = new HashSet<>();
    if (blacklist.getDestinationApplicationProtocols() != null) {
      destinationApplicationProtocols.addAll(blacklist.getDestinationApplicationProtocols());
    }

    Set<String> destinationTransportProtocols = new HashSet<>();
    if (blacklist.getDestinationTransportProtocols() != null) {
      destinationTransportProtocols.addAll(blacklist.getDestinationTransportProtocols());
    }

    Set<String> destinationPortRanges = new HashSet<>();
    if (blacklist.getDestinationPortRanges() != null) {
      destinationPortRanges.addAll(blacklist.getDestinationPortRanges());
    }

    return new BlacklistGraphQL(
        blacklist.id(),
        ObjectTypeListNamesEnum.BLACKLISTS.getUri(),
        blacklist.getName(),
        ScopeGraphQL.from(blacklist.getScope()),
        blacklist.getSourceComponent(),
        toSet(blacklist.getDestinationHostFqdns()),
        toSet(blacklist.getDestinationHostCidrs()),
        sourceZones,
        sourceMacrophases,
        sourceTargets,
        destinationApplicationProtocols,
        destinationTransportProtocols,
        destinationPortRanges);
  }

  private static Set<String> toSet(@Nullable List<String> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptySet();
    }
    return new HashSet<>(values);
  }
}
