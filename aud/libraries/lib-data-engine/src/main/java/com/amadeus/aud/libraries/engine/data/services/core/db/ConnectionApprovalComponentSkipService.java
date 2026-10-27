package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionApprovalComponentSkipsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalComponentSkip;
import jakarta.inject.Singleton;

@Singleton
public class ConnectionApprovalComponentSkipService
    extends BaseService<ConnectionApprovalComponentSkip, V1ConnectionApprovalComponentSkip> {

  @Override
  protected ConnectionApprovalComponentSkipsRepository getRepository(Version version) {
    ConnectionApprovalComponentSkipsRepository connectionApprovalComponentSkipsRepository =
        new ConnectionApprovalComponentSkipsRepository();
    version.setConnectionApprovalComponentSkipsRepository(
        connectionApprovalComponentSkipsRepository);
    return connectionApprovalComponentSkipsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        ConnectionApprovalComponentSkip.class,
        V1ConnectionApprovalComponentSkip.class,
        (ConnectionApprovalComponentSkip connectionApprovalComponentSkip, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addConnectionApprovalComponentSkip(connectionApprovalComponentSkip);

          // ensure existing rules are still working (wider but not blocking)
          connectionApprovalComponentSkip.setStatus("OK");
        });
  }
}
