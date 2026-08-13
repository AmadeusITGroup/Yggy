package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneFunctionalType;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class LandingZoneFunctionalTypeService
    extends BaseService<LandingZoneFunctionalType, V1LandingzoneFunctionalType> {

  @Override
  protected LandingZoneFunctionalTypesRepository getRepository(Version version) {
    LandingZoneFunctionalTypesRepository landingZoneFunctionalTypesRepository =
        new LandingZoneFunctionalTypesRepository();
    version.setLandingZoneFunctionalTypesRepository(landingZoneFunctionalTypesRepository);
    return landingZoneFunctionalTypesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ScopesRepository scopesRepository = version.getScopesRepository();
    if (scopesRepository == null) {
      Log.errorf("Scope Repository not found for version %s", version);
      return;
    }

    init(
        version,
        LandingZoneFunctionalType.class,
        V1LandingzoneFunctionalType.class,
        (LandingZoneFunctionalType landingZoneFunctionalType, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addLandingZoneFunctionalType(landingZoneFunctionalType);
        });

    LandingZoneFunctionalTypesRepository landingZoneFunctionalTypesRepository =
        version.getLandingZoneFunctionalTypesRepository();
    if (landingZoneFunctionalTypesRepository == null) {
      Log.errorf("LandingZoneFunctionalTypes Repository not found for version %s", version);
      return;
    }

    Scope globalDefinitionScope = scopesRepository.get("GlobalDefinitions");
    if (globalDefinitionScope == null) {
      throw new RuntimeException("GlobalDefinitions scope not found");
    }

    LandingZoneFunctionalType cloud =
        new LandingZoneFunctionalType(
            globalDefinitionScope, "Cloud", "Automatically generated", true);
    landingZoneFunctionalTypesRepository.addObject(cloud);
    globalDefinitionScope.addLandingZoneFunctionalType(cloud);

    LandingZoneFunctionalType legacy =
        new LandingZoneFunctionalType(
            globalDefinitionScope, "Legacy", "Automatically generated", true);
    landingZoneFunctionalTypesRepository.addObject(legacy);
    globalDefinitionScope.addLandingZoneFunctionalType(legacy);
  }
}
