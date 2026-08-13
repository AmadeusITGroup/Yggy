package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
import jakarta.inject.Singleton;

@Singleton
public class TargetTypeService extends BaseService<TargetType, V1TargetType> {

  @Override
  protected TargetTypesRepository getRepository(Version version) {
    TargetTypesRepository targetTypesRepository = new TargetTypesRepository();
    version.setTargetTypesRepository(targetTypesRepository);
    return targetTypesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        TargetType.class,
        V1TargetType.class,
        (TargetType targetType, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addTargetType(targetType);
        });
  }
}
