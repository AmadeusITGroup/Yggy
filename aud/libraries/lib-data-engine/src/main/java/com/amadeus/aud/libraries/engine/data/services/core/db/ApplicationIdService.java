package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationIdsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import jakarta.inject.Singleton;

@Singleton
public class ApplicationIdService extends BaseService<ApplicationId, V1ApplicationID> {

  @Override
  protected ApplicationIdsRepository getRepository(Version version) {
    ApplicationIdsRepository applicationIdsRepository = new ApplicationIdsRepository();
    version.setApplicationIdsRepository(applicationIdsRepository);
    return applicationIdsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        ApplicationId.class,
        V1ApplicationID.class,
        (ApplicationId applicationId, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addApplicationId(applicationId);
        });
  }
}
