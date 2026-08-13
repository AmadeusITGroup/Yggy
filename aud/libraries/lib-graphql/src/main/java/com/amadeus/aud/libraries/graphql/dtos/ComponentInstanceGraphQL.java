package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ComponentInstanceDTO")
public record ComponentInstanceGraphQL(
    String id,
    String type,
    String name,
    ComponentGraphQL component,
    ScopeGraphQL scope,
    ZoneTypeGraphQL zoneType,
    String applicationPhase,
    NamespaceGraphQL namespace,
    String serviceTag,
    String deployComponentInstance,
    String inSubnetInstance,
    String outSubnetInstance,
    ZoneInstanceGraphQL inZoneInstance,
    ZoneInstanceGraphQL outZoneInstance,
    String endpointType,
    String deprecatedStatus,
    String deprecatedMessage,
    String hostingPlatformType,
    String k8sIdentifier,
    boolean isAutomaticallyInstantiated,
    LandingZoneGraphQL landingZone,
    String iaasComponentInstanceName,
    Set<String> automaticTemplateInstances) {

  public static @Nullable ComponentInstanceGraphQL from(
      @Nullable ComponentInstance componentInstance) {
    if (componentInstance == null) {
      return null;
    }
    Set<String> automaticTemplateInstances = new HashSet<>();
    if (componentInstance.getAutomaticTemplateInstances() != null) {
      for (AutomaticTemplateInstance automaticTemplateInstance :
          componentInstance.getAutomaticTemplateInstances()) {
        automaticTemplateInstances.add(automaticTemplateInstance.getName());
      }
    }
    return new ComponentInstanceGraphQL(
        componentInstance.id(),
        ObjectTypeListNamesEnum.COMPONENTINSTANCES.getUri(),
        componentInstance.getName(),
        ComponentGraphQL.from(componentInstance.getComponent()),
        componentInstance.getComponent() != null
            ? ScopeGraphQL.from(componentInstance.getComponent().getScope())
            : null,
        ZoneTypeGraphQL.from(componentInstance.getZoneType()),
        componentInstance.getApplicationPhaseName(),
        NamespaceGraphQL.from(componentInstance.getNamespace()),
        componentInstance.getServiceTagName(),
        componentInstance.getDeployComponentInstanceName(),
        componentInstance.getInSubnetInstanceName(),
        componentInstance.getOutSubnetInstanceName(),
        componentInstance.getInZoneInstance() != null
            ? ZoneInstanceGraphQL.from(componentInstance.getInZoneInstance())
            : null,
        componentInstance.getOutZoneInstance() != null
            ? ZoneInstanceGraphQL.from(componentInstance.getOutZoneInstance())
            : null,
        componentInstance.getEndpointType() != null
            ? componentInstance.getEndpointType().name()
            : null,
        componentInstance.getDeprecatedStatus() != null
            ? componentInstance.getDeprecatedStatus().name()
            : null,
        componentInstance.getDeprecatedMessage(),
        componentInstance.getHostingPlatformType() != null
            ? componentInstance.getHostingPlatformType().name()
            : null,
        componentInstance.getK8sIdentifier(),
        componentInstance.isAutomaticallyInstantiated(),
        LandingZoneGraphQL.from(componentInstance.getLandingZone()),
        componentInstance.getHostingPlatformName(),
        automaticTemplateInstances);
  }
}
