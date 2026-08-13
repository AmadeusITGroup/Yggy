package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhase;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhaseSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class ApplicationPhaseService extends BaseService<ApplicationPhase, V1ApplicationPhase> {

  @Override
  protected ApplicationPhasesRepository getRepository(Version version) {
    ApplicationPhasesRepository applicationPhasesRepository = new ApplicationPhasesRepository();
    version.setApplicationPhasesRepository(applicationPhasesRepository);
    return applicationPhasesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    MacroPhasesRepository macroPhasesRepository = version.getMacroPhasesRepository();
    if (macroPhasesRepository == null) {
      Log.errorf("MacroPhase Repository not found for version %s", version);
      return;
    }

    init(
        version,
        ApplicationPhase.class,
        V1ApplicationPhase.class,
        (ApplicationPhase applicationPhase, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addApplicationPhase(applicationPhase);

          V1ApplicationPhaseSpec spec = applicationPhase.getData().getSpec();

          String macroPhaseName = spec.getMacroPhase();
          if (macroPhaseName != null && !macroPhaseName.isEmpty()) {
            MacroPhase macroPhase = macroPhasesRepository.get(macroPhaseName);
            if (macroPhase == null) {
              logModelWarning(
                  "ApplicationPhase %s references a non-existing MacroPhase %s",
                  applicationPhase.id(), macroPhaseName);
            } else {
              applicationPhase.setMacroPhase(macroPhase);
              macroPhase.addApplicationPhase(applicationPhase);
            }
          }
        });
  }
}
