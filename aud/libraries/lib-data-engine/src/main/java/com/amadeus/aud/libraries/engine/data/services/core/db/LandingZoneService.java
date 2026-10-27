package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
import com.amadeus.aud.libraries.yaml.models.V1LandingZoneSpec;
import com.amadeus.aud.libraries.yaml.models.V1MappingLandingzoneFunctionaltype;
import com.amadeus.aud.libraries.yaml.models.V1MappingLandingzoneFunctionaltypeSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class LandingZoneService extends BaseService<LandingZone, V1LandingZone> {

  @Override
  protected LandingZonesRepository getRepository(Version version) {
    LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
    version.setLandingZonesRepository(landingZonesRepository);
    return landingZonesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    TargetsRepository targetsRepository = version.getTargetsRepository();
    if (targetsRepository == null) {
      Log.errorf("Target Repository not found for version %s", version);
      return;
    }
    LandingZoneFunctionalTypesRepository landingZoneFunctionalTypesRepository =
        version.getLandingZoneFunctionalTypesRepository();
    if (landingZoneFunctionalTypesRepository == null) {
      Log.errorf("LandingZoneFunctionalType Repository not found for version %s", version);
      return;
    }
    LandingZoneTypesRepository landingZoneTypesRepository = version.getLandingZoneTypesRepository();
    if (landingZoneTypesRepository == null) {
      Log.errorf("LandingZoneType Repository not found for version %s", version);
      return;
    }
    MacroPhasesRepository macroPhasesRepository = version.getMacroPhasesRepository();
    if (macroPhasesRepository == null) {
      Log.errorf("MacroPhase Repository not found for version %s", version);
      return;
    }
    RegionsRepository regionsRepository = version.getRegionsRepository();
    if (regionsRepository == null) {
      Log.errorf("Region Repository not found for version %s", version);
      return;
    }
    ScopesRepository scopesRepository = version.getScopesRepository();
    if (scopesRepository == null) {
      Log.errorf("Scope Repository not found for version %s", version);
      return;
    }

    AudDataRepository audDataRepository = version.getAudDataRepository();
    if (audDataRepository == null) {
      Log.errorf("AUD Data Repository not found for version %s", version);
      return;
    }

    ConcurrentRepository<LandingZone> repository =
        init(
            version,
            LandingZone.class,
            V1LandingZone.class,
            (LandingZone landingZone, Scope scope) -> {
              if (scope == null) {
                return;
              }

              scope.addLandingZone(landingZone);

              V1LandingZoneSpec spec = landingZone.getData().getSpec();

              String regionName = spec.getRegion();
              if (!regionName.isEmpty()) {
                Region region = regionsRepository.get(regionName);
                if (region == null) {
                  logModelWarning(
                      "LandingZone %s references a non-existing Region %s",
                      landingZone.id(), regionName);
                } else {
                  landingZone.setRegion(region);
                  region.addLandingZone(landingZone);
                }
              }

              Target target = null;
              String targetName = spec.getTarget();
              if (targetName != null && !targetName.isBlank()) {
                target = targetsRepository.get(targetName);
                if (target == null) {
                  logModelWarning(
                      "LandingZone %s references a non-existing Target %s",
                      landingZone.id(), targetName);
                } else {
                  landingZone.setTarget(target);
                  target.addLandingZone(landingZone);

                  landingZone.setMergedProviderSpecific(
                      LandingZone.deepMergeProviderSpecific(
                          target.getProviderSpecific(), landingZone.getProviderSpecific()));

                  LandingZoneFunctionalType targetLandingZoneFunctionalType =
                      landingZoneFunctionalTypesRepository.get(targetName);
                  if (targetLandingZoneFunctionalType == null) {
                    logModelWarning(
                        "LandingZone Target %s references a non-existing LandingZoneFunctionalType %s",
                        landingZone.id(), targetName);
                  } else {
                    landingZone.addLandingZoneFunctionalType(targetLandingZoneFunctionalType);
                    targetLandingZoneFunctionalType.addLandingZone(landingZone);
                  }

                  String targetMacroPhaseName = target.getMacroPhase().getName();
                  if (targetMacroPhaseName != null && !targetMacroPhaseName.isBlank()) {
                    LandingZoneFunctionalType targetMacroPhaselandingZoneFunctionalType =
                        landingZoneFunctionalTypesRepository.get(targetMacroPhaseName);
                    if (targetMacroPhaselandingZoneFunctionalType == null) {
                      logModelWarning(
                          "LandingZone Target MacroPhase %s references a non-existing LandingZoneFunctionalType %s",
                          landingZone.id(), targetMacroPhaseName);
                    } else {
                      landingZone.addLandingZoneFunctionalType(
                          targetMacroPhaselandingZoneFunctionalType);
                      targetMacroPhaselandingZoneFunctionalType.addLandingZone(landingZone);
                    }
                  }

                  String targetContinentName = target.getContinent().getName();
                  if (targetContinentName != null && !targetContinentName.isBlank()) {
                    LandingZoneFunctionalType targetContinentLandingZoneFunctionalType =
                        landingZoneFunctionalTypesRepository.get(targetContinentName);
                    if (targetContinentLandingZoneFunctionalType == null) {
                      logModelWarning(
                          "LandingZone Target Continent %s references a non-existing LandingZoneFunctionalType %s",
                          landingZone.id(), targetContinentName);
                    } else {
                      landingZone.addLandingZoneFunctionalType(
                          targetContinentLandingZoneFunctionalType);
                      targetContinentLandingZoneFunctionalType.addLandingZone(landingZone);
                    }
                  }

                  String targetProviderName = target.getProvider().getName();
                  if (targetProviderName != null && !targetProviderName.isBlank()) {
                    LandingZoneFunctionalType targetProviderLandingZoneFunctionalType =
                        landingZoneFunctionalTypesRepository.get(targetProviderName);
                    if (targetProviderLandingZoneFunctionalType == null) {
                      logModelWarning(
                          "LandingZone Target Provider %s references a non-existing LandingZoneFunctionalType %s",
                          landingZone.id(), targetProviderName);
                    } else {
                      landingZone.addLandingZoneFunctionalType(
                          targetProviderLandingZoneFunctionalType);
                      targetProviderLandingZoneFunctionalType.addLandingZone(landingZone);
                    }

                    // FIXME: Ugly Hardcoding, think at something better
                    if ("Amadeus".equals(targetProviderName)) {
                      LandingZoneFunctionalType legacyLandingZoneFunctionalType =
                          landingZoneFunctionalTypesRepository.get("Legacy");
                      if (legacyLandingZoneFunctionalType == null) {
                        logModelWarning(
                            "LandingZone Target Provider %s references a non-existing LandingZoneFunctionalType Legacy",
                            landingZone.id());
                      } else {
                        landingZone.addLandingZoneFunctionalType(legacyLandingZoneFunctionalType);
                        legacyLandingZoneFunctionalType.addLandingZone(landingZone);
                      }
                    } else {
                      LandingZoneFunctionalType cloudLandingZoneFunctionalType =
                          landingZoneFunctionalTypesRepository.get("Cloud");
                      if (cloudLandingZoneFunctionalType == null) {
                        logModelWarning(
                            "LandingZone Target Provider %s references a non-existing LandingZoneFunctionalType Cloud",
                            landingZone.id());
                      } else {
                        landingZone.addLandingZoneFunctionalType(cloudLandingZoneFunctionalType);
                        cloudLandingZoneFunctionalType.addLandingZone(landingZone);
                      }
                    }
                  }
                }
              }

              String landingZoneTypeName = spec.getLandingzonetype();
              if (landingZoneTypeName != null && !landingZoneTypeName.isBlank()) {
                LandingZoneType landingZoneType =
                    landingZoneTypesRepository.get(landingZoneTypeName);
                if (landingZoneType == null) {
                  logModelWarning(
                      "LandingZone %s references a non-existing LandingZoneType %s",
                      landingZone.id(), landingZoneTypeName);
                } else {
                  landingZone.setLandingZoneType(landingZoneType);
                  landingZoneType.addLandingZone(landingZone);
                }
              }

              String macroPhaseName = spec.getMacrophase();
              if ((macroPhaseName == null || macroPhaseName.isEmpty()) && target != null) {
                macroPhaseName = target.getMacroPhase().getName();
              }
              if (macroPhaseName != null && !macroPhaseName.isBlank()) {
                MacroPhase macroPhase = macroPhasesRepository.get(macroPhaseName);
                if (macroPhase == null) {
                  logModelWarning(
                      "LandingZone %s references a non-existing MacroPhase %s",
                      landingZone.id(), macroPhaseName);
                } else {
                  landingZone.setMacroPhase(macroPhase);
                }
              }

              List<String> functionalTypes = spec.getFunctionaltypes();
              if (functionalTypes != null) {
                functionalTypes.forEach(
                    (String functionalTypeName) -> {
                      LandingZoneFunctionalType functionalType =
                          landingZoneFunctionalTypesRepository.get(functionalTypeName);
                      if (functionalType == null) {
                        logModelWarning(
                            "LandingZone %s references a non-existing LandingZoneFunctionalType %s",
                            landingZone.id(), functionalTypeName);
                      } else {
                        landingZone.addLandingZoneFunctionalType(functionalType);
                        functionalType.addLandingZone(landingZone);
                      }
                    });
              }
            });

    associateWithLandingZoneFunctionalType(
        repository,
        landingZoneFunctionalTypesRepository,
        scopesRepository,
        audDataRepository.getData().get(V1MappingLandingzoneFunctionaltype.class));
  }

  // getLandingzonetype(), getFunctionaltype() cannot return null today,
  // but we won't know tomorrow since it is an autogenerated class

  private void associateWithLandingZoneFunctionalType(
      ConcurrentRepository<LandingZone> landingZonesRepository,
      ConcurrentRepository<LandingZoneFunctionalType> landingZoneFunctionalTypesRepository,
      ConcurrentRepository<Scope> scopesRepository,
      List<? extends ModelElement> mappings) {
    for (ModelElement o : mappings) {
      V1MappingLandingzoneFunctionaltype data = (V1MappingLandingzoneFunctionaltype) o;

      V1MappingLandingzoneFunctionaltypeSpec spec = data.getSpec();
      V1Metadata metadata = data.getMetadata();

      LandingZone landingZone = null;
      LandingZoneFunctionalType landingZoneFunctionalType = null;

      String landingZoneName = spec.getLandingzone();
      if (landingZoneName != null && !landingZoneName.isBlank()) {
        landingZone = landingZonesRepository.get(landingZoneName);
        if (landingZone == null) {
          logModelWarning(
              "MappingLandingzoneFunctionaltype %s references a non-existing LandingZone %s",
              metadata.getName(), landingZoneName);
        }
      }

      String landingZoneFunctionalTypeName = spec.getFunctionaltype();
      if (landingZoneFunctionalTypeName != null && !landingZoneFunctionalTypeName.isBlank()) {
        landingZoneFunctionalType =
            landingZoneFunctionalTypesRepository.get(landingZoneFunctionalTypeName);
        if (landingZoneFunctionalType == null) {
          logModelWarning(
              "MappingLandingzoneFunctionaltype %s references a non-existing LandingZoneFunctionalType %s",
              metadata.getName(), landingZoneFunctionalTypeName);
        }

        String scopeName = metadata.getScope();
        if (scopeName != null && !scopeName.isBlank()) {
          Scope scope = scopesRepository.get(scopeName);
          if (scope == null) {
            logModelWarning(
                "MappingLandingzoneFunctionaltype %s references a non-existing Scope %s",
                metadata.getName(), scopeName);
          } else {
            scope.addLandingZoneFunctionalType(landingZoneFunctionalType);
          }
        }
      }

      if (landingZone != null && landingZoneFunctionalType != null) {
        landingZone.addLandingZoneFunctionalType(landingZoneFunctionalType);
        landingZoneFunctionalType.addLandingZone(landingZone);
      }
    }
  }

  public void initInternals(Version version) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("Landing Zone Repository not found for version %s", version);
      return;
    }

    ConcurrentMap<String, LandingZone> data = landingZonesRepository.getData();
    if (data == null) {
      Log.errorf("No Landing Zone data for version %s", version);
      return;
    }

    for (LandingZone landingZone : data.values()) {
      landingZone.initInternalEndpoints(false);
      landingZone.initInternalEndpoints(true);
    }
  }
}
