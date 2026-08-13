package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.yaml.models.V1Target;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class TargetService extends BaseService<Target, V1Target> {
  @Override
  protected TargetsRepository getRepository(Version version) {
    TargetsRepository targetsRepository = new TargetsRepository();
    version.setTargetsRepository(targetsRepository);
    return targetsRepository;
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

    ProvidersRepository providersRepository = version.getProvidersRepository();
    if (providersRepository == null) {
      Log.errorf("Provider Repository not found for version %s", version);
      return;
    }

    TargetTypesRepository targetTypesRepository = version.getTargetTypesRepository();
    if (targetTypesRepository == null) {
      Log.errorf("TargetType Repository not found for version %s", version);
      return;
    }

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

    ConcurrentRepository<Target> repository =
        init(
            version,
            Target.class,
            V1Target.class,
            (Target target, Scope scope) -> {
              if (scope == null) {
                return;
              }
              scope.addTarget(target);
              linkReferences(
                  target,
                  providersRepository,
                  targetTypesRepository,
                  macroPhasesRepository,
                  continentsRepository);
              landingZoneFunctionalTypesRepository.addObjectToGovernance(scopesRepository, target);
            });

    initPops(repository);
  }

  private void linkReferences(
      Target target,
      ProvidersRepository providersRepository,
      TargetTypesRepository targetTypesRepository,
      MacroPhasesRepository macroPhasesRepository,
      ContinentsRepository continentsRepository) {

    linkProvider(target, providersRepository);
    linkTargetType(target, targetTypesRepository);
    linkMacroPhase(target, macroPhasesRepository);
    linkContinent(target, continentsRepository);
  }

  private void linkProvider(Target target, ProvidersRepository providersRepository) {
    String providerName = target.getData().getSpec().getProvider();
    if (providerName == null || providerName.isEmpty()) {
      return;
    }

    Provider provider = providersRepository.get(providerName);
    if (provider == null) {
      logModelWarning("Target %s references a non-existing Provider %s", target.id(), providerName);
    } else {
      target.setProvider(provider);
      provider.addTarget(target);
    }
  }

  private void linkTargetType(Target target, TargetTypesRepository targetTypesRepository) {
    String targetTypeName = target.getData().getSpec().getTargettype();
    if (targetTypeName == null || targetTypeName.isEmpty()) {
      return;
    }

    TargetType targetType = targetTypesRepository.get(targetTypeName);
    if (targetType == null) {
      logModelWarning(
          "Target %s references a non-existing TargetType %s", target.id(), targetTypeName);
    } else {
      target.setTargetType(targetType);
      targetType.addTarget(target);
    }
  }

  private void linkMacroPhase(Target target, MacroPhasesRepository macroPhasesRepository) {
    String macroPhaseName = target.getData().getSpec().getMacroPhase();
    if (macroPhaseName == null || macroPhaseName.isEmpty()) {
      return;
    }

    MacroPhase macroPhase = macroPhasesRepository.get(macroPhaseName);
    if (macroPhase == null) {
      logModelWarning(
          "Target %s references a non-existing MacroPhase %s", target.id(), macroPhaseName);
    } else {
      target.setMacroPhase(macroPhase);
      macroPhase.addTarget(target);
    }
  }

  private void linkContinent(Target target, ContinentsRepository continentsRepository) {
    String continentName = target.getData().getSpec().getContinent();
    if (continentName == null || continentName.isEmpty()) {
      return;
    }

    Continent continent = continentsRepository.get(continentName);
    if (continent == null) {
      logModelWarning(
          "Target %s references a non-existing Continent %s", target.id(), continentName);
    } else {
      target.setContinent(continent);
      continent.addTarget(target);
    }
  }

  private void initPops(ConcurrentRepository<Target> targetsRepository) {
    ConcurrentMap<String, Target> targetsMap = targetsRepository.getData();
    targetsMap.forEach(
        (String id, Target target) -> {
          String popPrimaryName = target.getData().getSpec().getPopPrimary();
          if (popPrimaryName != null) {
            Target popPrimary = targetsRepository.get(popPrimaryName);
            if (popPrimary == null) {
              logModelWarning("PopPrimary %s not found for target %s", popPrimaryName, target.id());
            } else {
              target.setPopPrimary(popPrimary);
            }
          }

          String popSecondaryName = target.getData().getSpec().getPopSecondary();
          if (popSecondaryName != null) {
            Target popSecondary = targetsRepository.get(popSecondaryName);
            if (popSecondary == null) {
              logModelWarning(
                  "PopSecondary %s not found for target %s", popSecondaryName, target.id());
            } else {
              target.setPopSecondary(popSecondary);
            }
          }
        });
  }

  public void initInternals(Version version) {
    TargetsRepository targetsRepository = version.getTargetsRepository();
    if (targetsRepository == null) {
      logModelWarning("Target Repository not found for version %s", version);
      return;
    }

    ConcurrentMap<String, Target> data = targetsRepository.getData();
    if (data == null) {
      logModelWarning("No Target data for version %s", version);
      return;
    }

    for (Target target : data.values()) {
      target.getInternalEndpoints();
      target.getInternalEndpoints(true);
    }
  }
}
