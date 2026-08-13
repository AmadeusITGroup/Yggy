package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Service;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ComponentDTO")
public record ComponentGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String deprecatedStatus,
    String deprecatedMessage,
    boolean isPublic,
    boolean isPublicInheritance,
    boolean isPublicDependency,
    boolean isPrivate,
    boolean isHosting,
    boolean isHosted,
    boolean isTechnical,
    boolean isInterface,
    boolean isContainer,
    Set<String> securityZones,
    Set<String> serviceNowIds,
    Set<String> parents,
    Set<String> children,
    Set<ZoneTypeGraphQL> zoneTypes,
    Set<ServiceGraphQL> services) {

  public static @Nullable ComponentGraphQL from(@Nullable Component component) {
    if (component == null) {
      return null;
    }
    Set<String> children = new HashSet<>();
    if (component.getChildren() != null) {
      for (Component child : component.getChildren()) {
        children.add(child.getName());
      }
    }
    Set<ZoneTypeGraphQL> zoneTypes = new HashSet<>();
    if (component.getZoneTypes() != null) {
      for (com.amadeus.aud.libraries.models.core.db.ZoneType zoneType : component.getZoneTypes()) {
        zoneTypes.add(ZoneTypeGraphQL.from(zoneType));
      }
    }
    Set<ServiceGraphQL> services = new HashSet<>();
    if (component.getServices() != null) {
      for (Service service : component.getServices()) {
        services.add(ServiceGraphQL.from(service));
      }
    }
    return new ComponentGraphQL(
        component.id(),
        ObjectTypeListNamesEnum.COMPONENTS.getUri(),
        component.getName(),
        ScopeGraphQL.from(component.getScope()),
        component.getDeprecatedStatus() != null ? component.getDeprecatedStatus().name() : null,
        component.getDeprecatedMessage(),
        component.isPublic(),
        component.isPublicInheritance(),
        component.isPublicDependency(),
        component.isPrivate(),
        component.isHosting(),
        component.isHosted(),
        component.isTechnical(),
        component.isInterface(),
        component.isContainer(),
        component.getSecurityZones(),
        component.getServiceNowIds(),
        component.getParentsNames(),
        children,
        zoneTypes,
        services);
  }
}
