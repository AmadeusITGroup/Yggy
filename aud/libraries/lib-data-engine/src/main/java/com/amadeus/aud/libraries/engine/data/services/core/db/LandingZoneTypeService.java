package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneType;
import jakarta.inject.Singleton;

@Singleton
public class LandingZoneTypeService extends BaseService<LandingZoneType, V1LandingzoneType> {

  @Override
  protected LandingZoneTypesRepository getRepository(Version version) {
    LandingZoneTypesRepository landingZoneTypesRepository = new LandingZoneTypesRepository();
    version.setLandingZoneTypesRepository(landingZoneTypesRepository);
    return landingZoneTypesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        LandingZoneType.class,
        V1LandingzoneType.class,
        (LandingZoneType landingZoneType, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addLandingZoneType(landingZoneType);
        });
  }
}
