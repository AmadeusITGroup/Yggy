package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhase;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class ConnectionPhaseService extends BaseService<ConnectionPhase, V1ConnectionPhase> {

  @Override
  protected ConnectionPhasesRepository getRepository(Version version) {
    ConnectionPhasesRepository connectionPhasesRepository = new ConnectionPhasesRepository();
    version.setConnectionPhasesRepository(connectionPhasesRepository);
    return connectionPhasesRepository;
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
        ConnectionPhase.class,
        V1ConnectionPhase.class,
        (ConnectionPhase connectionPhase, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addConnectionPhase(connectionPhase);
          linkMacroPhases(connectionPhase, macroPhasesRepository);
        });
  }

  private void linkMacroPhases(
      ConnectionPhase connectionPhase, MacroPhasesRepository macroPhasesRepository) {

    String sourcePhaseName = connectionPhase.getData().getSpec().getSourcePhase();
    if (sourcePhaseName != null && !sourcePhaseName.isEmpty()) {
      MacroPhase sourcePhase = macroPhasesRepository.get(sourcePhaseName);
      if (sourcePhase == null) {
        logModelWarning(
            "ConnectionPhase %s references a non-existing source MacroPhase %s",
            connectionPhase.id(), sourcePhaseName);
      } else {
        connectionPhase.setSourcePhase(sourcePhase);
        sourcePhase.addConnectionPhase(connectionPhase);
      }
    }

    String destinationPhaseName = connectionPhase.getData().getSpec().getDestinationPhase();
    if (destinationPhaseName != null && !destinationPhaseName.isEmpty()) {
      MacroPhase destinationPhase = macroPhasesRepository.get(destinationPhaseName);
      if (destinationPhase == null) {
        logModelWarning(
            "ConnectionPhase %s references a non-existing destination MacroPhase %s",
            connectionPhase.id(), destinationPhaseName);
      } else {
        connectionPhase.setDestinationPhase(destinationPhase);
        destinationPhase.addConnectionPhase(connectionPhase);
      }
    }
  }
}
