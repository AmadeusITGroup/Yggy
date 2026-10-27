package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
import jakarta.inject.Singleton;

@Singleton
public class ZoneTypeService extends BaseService<ZoneType, V1ZoneType> {

  @Override
  protected ZoneTypesRepository getRepository(Version version) {
    ZoneTypesRepository zoneTypesRepository = new ZoneTypesRepository();
    version.setZoneTypesRepository(zoneTypesRepository);
    return zoneTypesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        ZoneType.class,
        V1ZoneType.class,
        (ZoneType zoneType, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addZoneType(zoneType);
        });
  }
}
