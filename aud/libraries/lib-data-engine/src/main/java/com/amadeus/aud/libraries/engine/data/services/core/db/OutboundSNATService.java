package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.OutboundSNATsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNAT;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNATSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class OutboundSNATService extends BaseService<OutboundSNAT, V1OutboundSNAT> {

  @Override
  protected OutboundSNATsRepository getRepository(Version version) {
    OutboundSNATsRepository outboundSNATsRepository = new OutboundSNATsRepository();
    version.setOutboundSNATsRepository(outboundSNATsRepository);
    return outboundSNATsRepository;
  }

  @LogExecutionTime
  // getMacrophase(), getContinent(), getZonetype() cannot return null today,
  // but we won't know tomorrow since it is

  public void init(Version version) {
    MacroPhasesRepository macroPhasesRepository = version.getMacroPhasesRepository();
    if (macroPhasesRepository == null) {
      Log.errorf("MacroPhase Repository not found for version %s", version);
      return;
    }

    ContinentsRepository continentsRepository = version.getContinentsRepository();
    if (continentsRepository == null) {
      Log.errorf("Continent Repository not found for version %s", version);
      return;
    }

    ZoneTypesRepository zoneTypesRepository = version.getZoneTypesRepository();
    if (zoneTypesRepository == null) {
      Log.errorf("ZoneType Repository not found for version %s", version);
      return;
    }

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("Component Repository not found for version %s", version);
      return;
    }

    ComponentInstancesRepository componentInstancesRepository =
        version.getComponentInstancesRepository();
    if (componentInstancesRepository == null) {
      Log.errorf("ComponentInstance Repository not found for version %s", version);
      return;
    }

    init(
        version,
        OutboundSNAT.class,
        V1OutboundSNAT.class,
        (OutboundSNAT outboundSNAT, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addOutboundSNAT(outboundSNAT);

          V1OutboundSNATSpec spec = outboundSNAT.getData().getSpec();
          V1Metadata metadata = outboundSNAT.getData().getMetadata();

          String macroPhaseId = spec.getMacrophase();
          if (macroPhaseId != null && !macroPhaseId.isBlank()) {
            MacroPhase macroPhase = macroPhasesRepository.get(macroPhaseId);
            if (macroPhase == null) {
              logModelWarning(
                  "OutboundSNAT %s references a non-existing MacroPhase %s",
                  metadata.getName(), macroPhaseId);
            } else {
              outboundSNAT.setMacroPhase(macroPhase);
            }
          }

          String continentId = spec.getContinent();
          if (continentId != null && !continentId.isBlank()) {
            Continent continent = continentsRepository.get(continentId);
            if (continent == null) {
              logModelWarning(
                  "OutboundSNAT %s references a non-existing Continent %s",
                  metadata.getName(), continentId);
            } else {
              outboundSNAT.setContinent(continent);
            }
          }

          String zoneTypeId = spec.getZonetype();
          if (zoneTypeId != null && !zoneTypeId.isBlank()) {
            ZoneType zoneType = zoneTypesRepository.get(zoneTypeId);
            if (zoneType == null) {
              logModelWarning(
                  "OutboundSNAT %s references a non-existing ZoneType %s",
                  metadata.getName(), zoneTypeId);
            } else {
              outboundSNAT.setZoneType(zoneType);
            }
          }

          List<String> componentsIds = spec.getComponents();
          if (componentsIds != null && !componentsIds.isEmpty()) {
            for (String componentId : componentsIds) {
              Component component = componentsRepository.get(componentId);
              if (component == null) {
                logModelWarning(
                    "OutboundSNAT %s references a non-existing Component %s",
                    metadata.getName(), componentId);
              } else {
                outboundSNAT.addComponent(component);
              }
            }
          }

          List<String> componentInstancesIds = spec.getComponentInstances();
          if (componentInstancesIds != null && !componentInstancesIds.isEmpty()) {
            for (String componentInstanceId : componentInstancesIds) {
              ComponentInstance componentInstance =
                  componentInstancesRepository.get(componentInstanceId);
              if (componentInstance == null) {
                logModelWarning(
                    "OutboundSNAT %s references a non-existing ComponentInstance %s",
                    metadata.getName(), componentInstanceId);
              } else {
                outboundSNAT.addComponentInstance(componentInstance);
              }
            }
          }
        });
  }
}
