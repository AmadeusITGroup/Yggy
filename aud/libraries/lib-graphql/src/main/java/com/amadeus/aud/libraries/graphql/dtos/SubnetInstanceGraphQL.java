package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("SubnetInstanceDTO")
public record SubnetInstanceGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String landingZoneName,
    String vlan,
    boolean isShared,
    boolean isGroup,
    boolean isNetworkPrivate,
    String cidr,
    String subnetTypeName,
    String zoneInstanceName,
    String parentName,
    String deprecatedStatus,
    String deprecatedMessage,
    Set<String> children,
    Set<String> componentInstances) {

  public static @Nullable SubnetInstanceGraphQL from(@Nullable SubnetInstance subnetInstance) {
    if (subnetInstance == null) {
      return null;
    }

    Set<String> children = new HashSet<>();
    if (subnetInstance.getChildren() != null) {
      for (SubnetInstance child : subnetInstance.getChildren()) {
        children.add(child.getName());
      }
    }

    Set<String> componentInstances = new HashSet<>();
    if (subnetInstance.getComponentInstances() != null) {
      for (ComponentInstance componentInstance : subnetInstance.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }

    return new SubnetInstanceGraphQL(
        subnetInstance.id(),
        ObjectTypeListNamesEnum.SUBNETINSTANCES.getUri(),
        subnetInstance.getName(),
        ScopeGraphQL.from(subnetInstance.getScope()),
        subnetInstance.getLandingZoneName(),
        subnetInstance.getVlan(),
        subnetInstance.isShared(),
        subnetInstance.isGroup(),
        subnetInstance.isNetworkPrivate(),
        subnetInstance.getCidr(),
        subnetInstance.getSubnetTypeName(),
        subnetInstance.getZoneInstanceName(),
        subnetInstance.getParentName(),
        subnetInstance.getDeprecatedStatus() != null
            ? subnetInstance.getDeprecatedStatus().name()
            : null,
        subnetInstance.getDeprecatedMessage(),
        children,
        componentInstances);
  }
}
