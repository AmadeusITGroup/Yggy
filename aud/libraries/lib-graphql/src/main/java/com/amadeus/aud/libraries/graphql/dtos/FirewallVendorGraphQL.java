package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("FirewallVendorDTO")
public record FirewallVendorGraphQL(
    String id, String name, ScopeGraphQL scope, Set<FirewallGroupGraphQL> firewallGroups) {
  public static @Nullable FirewallVendorGraphQL from(@Nullable FirewallVendor firewallVendor) {
    if (firewallVendor == null) {
      return null;
    }
    Set<FirewallGroupGraphQL> firewallGroups = new HashSet<>();
    if (firewallVendor.getFirewallGroups() != null) {
      for (FirewallGroup firewallGroup : firewallVendor.getFirewallGroups()) {
        firewallGroups.add(FirewallGroupGraphQL.from(firewallGroup));
      }
    }
    return new FirewallVendorGraphQL(
        firewallVendor.id(),
        firewallVendor.getName(),
        ScopeGraphQL.from(firewallVendor.getScope()),
        firewallGroups);
  }
}
