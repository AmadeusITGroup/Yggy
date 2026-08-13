package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("ApplicationPhaseDTO")
public record ApplicationPhaseGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    boolean isCustomerFacing,
    MacroPhaseGraphQL macroPhase,
    Set<String> componentInstances) {
  public static @Nullable ApplicationPhaseGraphQL from(
      @Nullable ApplicationPhase applicationPhase) {
    if (applicationPhase == null) {
      return null;
    }
    Set<String> componentInstances = new HashSet<>();
    if (applicationPhase.getComponentInstances() != null) {
      for (AbstractBase<?> componentInstance : applicationPhase.getComponentInstances()) {
        componentInstances.add(componentInstance.getName());
      }
    }
    return new ApplicationPhaseGraphQL(
        applicationPhase.id(),
        ObjectTypeListNamesEnum.APPLICATIONPHASES.getUri(),
        applicationPhase.getName(),
        ScopeGraphQL.from(applicationPhase.getScope()),
        applicationPhase.isCustomerFacing(),
        MacroPhaseGraphQL.from(applicationPhase.getMacroPhase()),
        componentInstances);
  }
}
