package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Target;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("TargetDTO")
public record TargetGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String providerName,
    String targetTypeName,
    MacroPhaseGraphQL macroPhase,
    String continentName,
    String popPrimaryName,
    String popSecondaryName,
    List<String> cidrs,
    boolean isCloud,
    boolean isAmadeus,
    boolean isAudManaged,
    boolean isEnableOfficeConnectivity,
    boolean isEnableJumpServerAccess,
    boolean isEnableVnetToVnet,
    Set<String> landingZones,
    Set<String> firewallGroups) {

  public static @Nullable TargetGraphQL from(@Nullable Target target) {
    if (target == null) {
      return null;
    }
    Set<String> landingZones = new HashSet<>();
    if (target.getLandingZones() != null) {
      for (var landingZone : target.getLandingZones()) {
        landingZones.add(landingZone.getName());
      }
    }
    Set<String> firewallGroups = new HashSet<>();
    if (target.getFirewallGroups() != null) {
      for (var firewallGroup : target.getFirewallGroups()) {
        firewallGroups.add(firewallGroup.getName());
      }
    }
    return new TargetGraphQL(
        target.id(),
        ObjectTypeListNamesEnum.TARGETS.getUri(),
        target.getName(),
        ScopeGraphQL.from(target.getScope()),
        target.getProviderName(),
        target.getTargetTypeName(),
        MacroPhaseGraphQL.from(target.getMacroPhase()),
        target.getContinentName(),
        target.getPopPrimaryName(),
        target.getPopSecondaryName(),
        target.getCidrs(),
        target.isCloud(),
        target.isAmadeus(),
        target.isAudManaged(),
        target.isEnableOfficeConnectivity(),
        target.isEnableJumpServerAccess(),
        target.isEnableVnetToVnet(),
        landingZones,
        firewallGroups);
  }
}
