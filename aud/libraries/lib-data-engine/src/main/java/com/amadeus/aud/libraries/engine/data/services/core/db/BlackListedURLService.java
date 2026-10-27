package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlackListedURLsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURL;
import jakarta.inject.Singleton;

@Singleton
public class BlackListedURLService extends BaseService<BlackListedURL, V1BlackListedURL> {

  @Override
  protected BlackListedURLsRepository getRepository(Version version) {
    BlackListedURLsRepository blackListedURLsRepository = new BlackListedURLsRepository();
    version.setBlackListedURLsRepository(blackListedURLsRepository);
    return blackListedURLsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        BlackListedURL.class,
        V1BlackListedURL.class,
        (BlackListedURL blackListedURL, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addBlackListedURL(blackListedURL);
        });
  }
}
