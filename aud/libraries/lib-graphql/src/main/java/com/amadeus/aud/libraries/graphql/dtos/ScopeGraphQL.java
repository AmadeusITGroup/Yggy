package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Scope;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ScopeDTO")
public record ScopeGraphQL(
    String id,
    String type,
    String name,
    String serviceNowId,
    String repositoryVersion,
    boolean isDeleted,
    Set<String> roles,
    Set<String> targets,
    Set<String> landingZones,
    Set<String> components,
    Set<String> componentInstances,
    Set<ZoneInstanceGraphQL> zoneInstances,
    Set<String> subnetInstances) {

  public static @Nullable ScopeGraphQL from(@Nullable Scope scope) {
    if (scope == null) {
      return null;
    }
    Set<String> targets = new HashSet<>();
    if (scope.getTargets() != null) {
      for (var target : scope.getTargets()) {
        targets.add(target.getName());
      }
    }
    Set<String> landingZones = new HashSet<>();
    if (scope.getLandingZones() != null) {
      for (var landingZone : scope.getLandingZones()) {
        landingZones.add(landingZone.getName());
      }
    }
    Set<String> components = new HashSet<>();
    if (scope.getComponents() != null) {
      for (var component : scope.getComponents()) {
        components.add(component.getName());
      }
    }
    Set<String> componentInstances = new HashSet<>();
    if (scope.getComponentInstances() != null) {
      for (var componentInstance : scope.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    Set<ZoneInstanceGraphQL> zoneInstances = new HashSet<>();
    if (scope.getZoneInstances() != null) {
      for (var zoneInstance : scope.getZoneInstances()) {
        zoneInstances.add(ZoneInstanceGraphQL.from(zoneInstance));
      }
    }
    Set<String> subnetInstances = new HashSet<>();
    if (scope.getSubnetInstances() != null) {
      for (var subnetInstance : scope.getSubnetInstances()) {
        subnetInstances.add(subnetInstance.getName());
      }
    }
    return new ScopeGraphQL(
        scope.id(),
        ObjectTypeListNamesEnum.SCOPES.getUri(),
        scope.getName(),
        scope.getServiceNowId(),
        scope.getRepositoryVersion(),
        scope.isDeleted(),
        scope.getRolesJsonRepresentation(),
        targets,
        landingZones,
        components,
        componentInstances,
        zoneInstances,
        subnetInstances);
  }
}
