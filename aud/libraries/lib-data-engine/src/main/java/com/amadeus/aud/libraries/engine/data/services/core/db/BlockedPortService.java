package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlockedPortsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import jakarta.inject.Singleton;

@Singleton
public class BlockedPortService extends BaseService<BlockedPort, V1BlockedPort> {

  @Override
  protected BlockedPortsRepository getRepository(Version version) {
    BlockedPortsRepository blockedPortsRepository = new BlockedPortsRepository();
    version.setBlockedPortsRepository(blockedPortsRepository);
    return blockedPortsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        BlockedPort.class,
        V1BlockedPort.class,
        (BlockedPort blockedPort, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addBlockedPort(blockedPort);
        });
  }
}
