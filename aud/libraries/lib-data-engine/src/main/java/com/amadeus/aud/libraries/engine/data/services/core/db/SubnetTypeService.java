package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.yaml.models.V1SubnetType;
import jakarta.inject.Singleton;

@Singleton
public class SubnetTypeService extends BaseService<SubnetType, V1SubnetType> {
  @Override
  protected SubnetTypesRepository getRepository(Version version) {
    SubnetTypesRepository subnetTypesRepository = new SubnetTypesRepository();
    version.setSubnetTypesRepository(subnetTypesRepository);
    return subnetTypesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        SubnetType.class,
        V1SubnetType.class,
        (SubnetType subnetType, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addSubnetType(subnetType);
        });
  }
}
