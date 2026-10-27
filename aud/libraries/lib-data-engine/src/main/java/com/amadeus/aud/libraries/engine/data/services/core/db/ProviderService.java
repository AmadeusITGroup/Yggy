package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Provider;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class ProviderService extends BaseService<Provider, V1Provider> {

  @Override
  protected ProvidersRepository getRepository(Version version) {
    ProvidersRepository providersRepository = new ProvidersRepository();
    version.setProvidersRepository(providersRepository);
    return providersRepository;
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
        Provider.class,
        V1Provider.class,
        (Provider provider, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addProvider(provider);
          landingZoneFunctionalTypesRepository.addObjectToGovernance(scopesRepository, provider);
        });
  }
}
