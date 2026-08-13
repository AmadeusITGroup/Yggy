package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("FirewallGroupDTO")
public record FirewallGroupGraphQL(
    String id,
    String name,
    ScopeGraphQL scope,
    String externalIps,
    String firewallVendor,
    String target,
    Set<String> zoneInstances) {
  public static @Nullable FirewallGroupGraphQL from(@Nullable FirewallGroup firewallGroup) {
    if (firewallGroup == null) {
      return null;
    }
    Set<String> zoneInstances = new HashSet<>();
    if (firewallGroup.getZoneInstances() != null) {
      for (ZoneInstance zoneInstance : firewallGroup.getZoneInstances()) {
        zoneInstances.add(zoneInstance.getName());
      }
    }
    return new FirewallGroupGraphQL(
        firewallGroup.id(),
        firewallGroup.getName(),
        ScopeGraphQL.from(firewallGroup.getScope()),
        firewallGroup.getExternalIps(),
        firewallGroup.getFirewallVendorName(),
        firewallGroup.getTargetName(),
        zoneInstances);
  }
}
