package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlackListedCIDRsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import jakarta.inject.Singleton;

@Singleton
public class BlackListedCIDRService extends BaseService<BlackListedCIDR, V1BlackListedCIDR> {

  @Override
  protected BlackListedCIDRsRepository getRepository(Version version) {
    BlackListedCIDRsRepository blackListedCIDRsRepository = new BlackListedCIDRsRepository();
    version.setBlackListedCIDRsRepository(blackListedCIDRsRepository);
    return blackListedCIDRsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        BlackListedCIDR.class,
        V1BlackListedCIDR.class,
        (BlackListedCIDR blackListedCIDR, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addBlackListedCIDR(blackListedCIDR);
        });
  }
}
