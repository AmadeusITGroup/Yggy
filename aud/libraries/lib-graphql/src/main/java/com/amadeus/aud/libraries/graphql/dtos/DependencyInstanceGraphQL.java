package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("DependencyInstanceDTO")
public record DependencyInstanceGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String dependencyName,
    String sourceComponentInstanceName,
    String targetComponentInstanceName,
    String viaComponentInstanceName,
    String viaServiceName,
    boolean isSourceAsComponent,
    boolean isSourceLimitedToLegacyOnly,
    boolean isSourceLimitedToCloudOnly,
    boolean isSourceLimitedToLandingZone,
    boolean isSourceLimitedToTarget,
    boolean isSourceLimitedToContinent,
    boolean isSourceLimitedToMacroPhase,
    boolean isSourceLimitedToSelector,
    boolean isTargetAsComponent,
    boolean isTargetLimitedToLegacyOnly,
    boolean isTargetLimitedToCloudOnly,
    boolean isTargetLimitedToSourceNamespace,
    boolean isTargetLimitedToSourcePaas,
    boolean isTargetLimitedToSourceSecurityZone,
    boolean isTargetLimitedToSourceApplicationPhase,
    boolean isTargetLimitedToSourceLandingZone,
    boolean isTargetLimitedToSourceTarget,
    boolean isTargetLimitedToSourceContinent,
    boolean isTargetLimitedToSourceMacroPhase,
    Set<ServiceGraphQL> services) {

  public static @Nullable DependencyInstanceGraphQL from(
      @Nullable DependencyInstance dependencyInstance) {
    if (dependencyInstance == null) {
      return null;
    }
    Set<ServiceGraphQL> services = new HashSet<>();
    if (dependencyInstance.getServices() != null) {
      for (Service service : dependencyInstance.getServices()) {
        services.add(ServiceGraphQL.from(service));
      }
    }
    return new DependencyInstanceGraphQL(
        dependencyInstance.id(),
        ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri(),
        dependencyInstance.getName(),
        ScopeGraphQL.from(dependencyInstance.getScope()),
        dependencyInstance.getDependencyName(),
        dependencyInstance.getSourceComponentInstanceName(),
        dependencyInstance.getTargetComponentInstanceName(),
        dependencyInstance.getViaComponentInstanceName(),
        dependencyInstance.getViaServiceName(),
        dependencyInstance.isSourceAsComponent(),
        dependencyInstance.isSourceLimitedToLegacyOnly(),
        dependencyInstance.isSourceLimitedToCloudOnly(),
        dependencyInstance.isSourceLimitedToLandingZone(),
        dependencyInstance.isSourceLimitedToTarget(),
        dependencyInstance.isSourceLimitedToContinent(),
        dependencyInstance.isSourceLimitedToMacroPhase(),
        dependencyInstance.isSourceLimitedToSelector(),
        dependencyInstance.isTargetAsComponent(),
        dependencyInstance.isTargetLimitedToLegacyOnly(),
        dependencyInstance.isTargetLimitedToCloudOnly(),
        dependencyInstance.isTargetLimitedToSourceNamespace(),
        dependencyInstance.isTargetLimitedToSourcePaas(),
        dependencyInstance.isTargetLimitedToSourceSecurityZone(),
        dependencyInstance.isTargetLimitedToSourceApplicationPhase(),
        dependencyInstance.isTargetLimitedToSourceLandingZone(),
        dependencyInstance.isTargetLimitedToSourceTarget(),
        dependencyInstance.isTargetLimitedToSourceContinent(),
        dependencyInstance.isTargetLimitedToSourceMacroPhase(),
        services);
  }
}
