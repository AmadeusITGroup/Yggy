package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.microprofile.graphql.Type;

@Type("MacroPhaseDTO")
public record MacroPhaseGraphQL(
    String id,
    String type,
    String name,
    ScopeGraphQL scope,
    Integer automationOrder,
    Set<String> applicationPhases,
    Set<String> targets,
    Set<String> connectionPhases) {
  public static @Nullable MacroPhaseGraphQL from(@Nullable MacroPhase macroPhase) {
    if (macroPhase == null) {
      return null;
    }
    Set<String> applicationPhases = new HashSet<>();
    if (macroPhase.getApplicationPhases() != null) {
      for (var applicationPhase : macroPhase.getApplicationPhases()) {
        applicationPhases.add(applicationPhase.getName());
      }
    }
    Set<String> targets = new HashSet<>();
    if (macroPhase.getTargets() != null) {
      for (var target : macroPhase.getTargets()) {
        targets.add(target.getName());
      }
    }
    Set<String> connectionPhases = new HashSet<>();
    if (macroPhase.getConnectionPhases() != null) {
      for (var connectionPhase : macroPhase.getConnectionPhases()) {
        connectionPhases.add(connectionPhase.getName());
      }
    }
    return new MacroPhaseGraphQL(
        macroPhase.id(),
        ObjectTypeListNamesEnum.MACROPHASES.getUri(),
        macroPhase.getName(),
        ScopeGraphQL.from(macroPhase.getScope()),
        macroPhase.getAutomationOrder(),
        applicationPhases,
        targets,
        connectionPhases);
  }
}
