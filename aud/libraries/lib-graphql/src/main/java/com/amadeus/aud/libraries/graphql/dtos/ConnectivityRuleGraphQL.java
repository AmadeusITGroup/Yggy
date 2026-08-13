package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.ConnectivityRule;
import com.amadeus.aud.libraries.yaml.models.FirewallGroup;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Type;

@Type("ConnectivityRuleDTO")
public record ConnectivityRuleGraphQL(
    String name,
    String uid,
    FirewallObjectGroupGraphQL srcObjectGroup,
    FirewallObjectGroupGraphQL dstObjectGroup,
    List<String> ports,
    String protocol,
    String owner,
    String scope,
    List<InTransitFirewallGroupGraphQL> inTransitFirewallGroups,
    Map<String, String> labels) {

  @SuppressWarnings("NullAway")
  public static @Nullable ConnectivityRuleGraphQL from(
      @Nullable ConnectivityRule connectivityRule) {
    if (connectivityRule == null) {
      return null;
    }
    List<InTransitFirewallGroupGraphQL> inTransitFirewallGroups = new ArrayList<>();
    List<FirewallGroup> transitGroups = connectivityRule.getInTransitFirewallGroups();
    if (transitGroups != null) {
      for (var firewallGroup : transitGroups) {
        inTransitFirewallGroups.add(InTransitFirewallGroupGraphQL.from(firewallGroup));
      }
    }
    return new ConnectivityRuleGraphQL(
        connectivityRule.getName(),
        connectivityRule.getUid(),
        FirewallObjectGroupGraphQL.from(connectivityRule.getSrcObjectGroup()),
        FirewallObjectGroupGraphQL.from(connectivityRule.getDstObjectGroup()),
        connectivityRule.getPorts(),
        connectivityRule.getProtocol().getValue(),
        connectivityRule.getOwner(),
        connectivityRule.getScope(),
        inTransitFirewallGroups,
        connectivityRule.getLabels());
  }
}
