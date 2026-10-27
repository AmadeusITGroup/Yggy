package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhase;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class MacroPhaseService extends BaseService<MacroPhase, V1MacroPhase> {
  @Override
  protected MacroPhasesRepository getRepository(Version version) {
    MacroPhasesRepository macroPhasesRepository = new MacroPhasesRepository();
    version.setMacroPhasesRepository(macroPhasesRepository);
    return macroPhasesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ScopesRepository scopesRepository = version.getScopesRepository();
    if (scopesRepository == null) {
      Log.errorf("Scope Repository not found for version %s", version);
      return;
    }

    LandingZoneFunctionalTypesRepository landingZoneFunctionalTypesRepository =
        version.getLandingZoneFunctionalTypesRepository();
    if (landingZoneFunctionalTypesRepository == null) {
      Log.errorf("LandingZoneFunctionalTypes Repository not found for version %s", version);
      return;
    }

    init(
        version,
        MacroPhase.class,
        V1MacroPhase.class,
        (MacroPhase macroPhase, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addMacroPhase(macroPhase);

          landingZoneFunctionalTypesRepository.addObjectToGovernance(scopesRepository, macroPhase);
        });
  }
}
