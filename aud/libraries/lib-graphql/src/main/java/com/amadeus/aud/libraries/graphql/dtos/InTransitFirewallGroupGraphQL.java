package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.FirewallGroup;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Type;

@Type("InTransitFirewallGroupDTO")
public record InTransitFirewallGroupGraphQL(String name, String vendor) {

  public static @Nullable InTransitFirewallGroupGraphQL from(
      @Nullable FirewallGroup firewallGroup) {
    if (firewallGroup == null) {
      return null;
    }
    return new InTransitFirewallGroupGraphQL(firewallGroup.getName(), firewallGroup.getVendor());
  }
}
