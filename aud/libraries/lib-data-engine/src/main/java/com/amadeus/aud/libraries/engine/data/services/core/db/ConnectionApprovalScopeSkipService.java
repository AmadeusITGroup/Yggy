package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionApprovalScopeSkipsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkip;
import jakarta.inject.Singleton;

@Singleton
public class ConnectionApprovalScopeSkipService
    extends BaseService<ConnectionApprovalScopeSkip, V1ConnectionApprovalScopeSkip> {

  @Override
  protected ConnectionApprovalScopeSkipsRepository getRepository(Version version) {
    ConnectionApprovalScopeSkipsRepository connectionApprovalScopeSkipsRepository =
        new ConnectionApprovalScopeSkipsRepository();
    version.setConnectionApprovalScopeSkipsRepository(connectionApprovalScopeSkipsRepository);
    return connectionApprovalScopeSkipsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        ConnectionApprovalScopeSkip.class,
        V1ConnectionApprovalScopeSkip.class,
        (ConnectionApprovalScopeSkip connectionApprovalScopeSkip, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addConnectionApprovalScopeSkip(connectionApprovalScopeSkip);

          // ensure existing rules are still working (wider but not blocking)
          connectionApprovalScopeSkip.setStatus("OK");
        });
  }
}
