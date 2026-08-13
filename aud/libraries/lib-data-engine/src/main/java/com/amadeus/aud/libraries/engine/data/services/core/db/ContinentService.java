package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Continent;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class ContinentService extends BaseService<Continent, V1Continent> {

  @Override
  protected ContinentsRepository getRepository(Version version) {
    ContinentsRepository continentsRepository = new ContinentsRepository();
    version.setContinentsRepository(continentsRepository);
    return continentsRepository;
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
        Continent.class,
        V1Continent.class,
        (Continent continent, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addContinent(continent);
          landingZoneFunctionalTypesRepository.addObjectToGovernance(scopesRepository, continent);
        });
  }
}
