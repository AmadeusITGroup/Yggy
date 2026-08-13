package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("NamespaceDTO")
public record NamespaceGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isShared,
    boolean isAutomaticallyGenerated,
    Set<String> authorizedScopes,
    Set<String> componentInstances) {
  public static @Nullable NamespaceGraphQL from(@Nullable Namespace namespace) {
    if (namespace == null) {
      return null;
    }
    Set<String> authorizedScopes = new HashSet<>();
    if (namespace.getAuthorizedScopes() != null) {
      for (var authorizedScope : namespace.getAuthorizedScopes()) {
        authorizedScopes.add(authorizedScope.getName());
      }
    }
    Set<String> componentInstances = new HashSet<>();
    if (namespace.getComponentInstances() != null) {
      for (var componentInstance : namespace.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new NamespaceGraphQL(
        namespace.id(),
        ObjectTypeListNamesEnum.NAMESPACES.getUri(),
        namespace.getName(),
        ScopeGraphQL.from(namespace.getScope()),
        namespace.isShared(),
        namespace.isAutomaticallyGenerated(),
        authorizedScopes,
        componentInstances);
  }
}
