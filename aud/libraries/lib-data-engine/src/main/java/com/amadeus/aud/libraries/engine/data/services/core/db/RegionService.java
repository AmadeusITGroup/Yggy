package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Region;
import com.amadeus.aud.libraries.yaml.models.V1RegionSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class RegionService extends BaseService<Region, V1Region> {

  @Override
  protected RegionsRepository getRepository(Version version) {
    RegionsRepository regionsRepository = new RegionsRepository();
    version.setRegionsRepository(regionsRepository);
    return regionsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ProvidersRepository providersRepository = version.getProvidersRepository();
    if (providersRepository == null) {
      Log.errorf("Provider Repository not found for version %s", version);
      return;
    }

    ContinentsRepository continentsRepository = version.getContinentsRepository();
    if (continentsRepository == null) {
      Log.errorf("Continent Repository not found for version %s", version);
      return;
    }

    init(
        version,
        Region.class,
        V1Region.class,
        (Region region, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addRegion(region);
          linkProviderAndContinent(region, providersRepository, continentsRepository);
        });
  }

  private void linkProviderAndContinent(
      Region region,
      ProvidersRepository providersRepository,
      ContinentsRepository continentsRepository) {

    V1RegionSpec spec = region.getData().getSpec();

    String providerName = spec.getProvider();
    if (providerName != null && !providerName.isEmpty()) {
      Provider provider = providersRepository.get(providerName);
      if (provider == null) {
        logModelWarning(
            "Region %s references a non-existing Provider %s", region.id(), providerName);
      } else {
        provider.addRegion(region);
        region.setProvider(provider);
      }
    }

    String continentName = spec.getContinent();
    if (continentName != null && !continentName.isEmpty()) {
      Continent continent = continentsRepository.get(continentName);
      if (continent == null) {
        logModelWarning(
            "Region %s references a non-existing Continent %s", region.id(), spec.getContinent());
      } else {
        continent.addRegion(region);
        region.setContinent(continent);
      }
    }
  }
}
