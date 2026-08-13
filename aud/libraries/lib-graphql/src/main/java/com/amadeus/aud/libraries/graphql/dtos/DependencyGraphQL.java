package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("DependencyDTO")
public record DependencyGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String sourceComponentName,
    String destinationComponentName,
    Set<String> dependencyInstances) {
  public static @Nullable DependencyGraphQL from(@Nullable Dependency dependency) {
    if (dependency == null) {
      return null;
    }
    Set<String> dependencyInstances = new HashSet<>();
    if (dependency.getDependencyInstances() != null) {
      for (DependencyInstance dependencyInstance : dependency.getDependencyInstances()) {
        dependencyInstances.add(dependencyInstance.getName());
      }
    }
    return new DependencyGraphQL(
        dependency.id(),
        ObjectTypeListNamesEnum.DEPENDENCIES.getUri(),
        dependency.getName(),
        ScopeGraphQL.from(dependency.getScope()),
        dependency.getSourceComponentName(),
        dependency.getDestinationComponentName(),
        dependencyInstances);
  }
}
