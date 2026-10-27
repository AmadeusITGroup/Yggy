package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServiceTagsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.yaml.models.V1Servicetag;
import jakarta.inject.Singleton;

@Singleton
public class ServiceTagService extends BaseService<ServiceTag, V1Servicetag> {

  @Override
  protected ServiceTagsRepository getRepository(Version version) {
    ServiceTagsRepository serviceTagsRepository = new ServiceTagsRepository();
    version.setServiceTagsRepository(serviceTagsRepository);
    return serviceTagsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        ServiceTag.class,
        V1Servicetag.class,
        (ServiceTag serviceTag, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addServiceTag(serviceTag);
        });
  }
}
