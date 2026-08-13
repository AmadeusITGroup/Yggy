package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("AutomaticTemplateInstanceDTO")
public record AutomaticTemplateInstanceGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    String component,
    String landingZone,
    Set<String> componentInstances,
    List<Set<String>> associatedFunctionalTypes,
    List<Set<String>> excludedFunctionalTypes) {

  public static @Nullable AutomaticTemplateInstanceGraphQL from(
      @Nullable AutomaticTemplateInstance automaticTemplateInstance) {
    if (automaticTemplateInstance == null) {
      return null;
    }
    Set<String> componentInstances = new HashSet<>();
    if (automaticTemplateInstance.getComponentInstances() != null) {
      for (ComponentInstance componentInstance :
          automaticTemplateInstance.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new AutomaticTemplateInstanceGraphQL(
        automaticTemplateInstance.id(),
        ObjectTypeListNamesEnum.AUTOMATICTEMPLATEINSTANCES.getUri(),
        automaticTemplateInstance.getName(),
        ScopeGraphQL.from(automaticTemplateInstance.getScope()),
        automaticTemplateInstance.getComponent() != null
            ? automaticTemplateInstance.getComponent().getName()
            : null,
        automaticTemplateInstance.getLandingZone() != null
            ? automaticTemplateInstance.getLandingZone().getName()
            : null,
        componentInstances,
        automaticTemplateInstance.getAssociatedFunctionalTypes(),
        automaticTemplateInstance.getExcludedFunctionalTypes());
  }
}
