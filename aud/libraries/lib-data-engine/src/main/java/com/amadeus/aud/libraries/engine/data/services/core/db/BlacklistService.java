package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlacklistsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import jakarta.inject.Singleton;

@Singleton
public class BlacklistService extends BaseService<Blacklist, V1Blacklist> {

  @Override
  protected BlacklistsRepository getRepository(Version version) {
    BlacklistsRepository blacklistsRepository = new BlacklistsRepository();
    version.setBlacklistsRepository(blacklistsRepository);
    return blacklistsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        Blacklist.class,
        V1Blacklist.class,
        (Blacklist blacklist, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addBlacklist(blacklist);
        });
  }
}
