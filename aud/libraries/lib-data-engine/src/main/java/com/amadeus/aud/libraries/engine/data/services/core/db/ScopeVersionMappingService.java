package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopeVersionMappingsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import jakarta.inject.Singleton;

@Singleton
public class ScopeVersionMappingService
    extends BaseService<ScopeVersionMapping, V1ScopeVersionMapping> {

  @Override
  protected ScopeVersionMappingsRepository getRepository(Version version) {
    ScopeVersionMappingsRepository scopeVersionMappingsRepository =
        new ScopeVersionMappingsRepository();
    version.setScopeVersionMappingsRepository(scopeVersionMappingsRepository);
    return scopeVersionMappingsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(version, ScopeVersionMapping.class, V1ScopeVersionMapping.class, this::callback);
  }

  private void callback(ScopeVersionMapping scopeVersionMapping, Scope scope) {
    if (scope == null) {
      logModelWarning(
          "Scope (%s) not found for ScopeVersionMapping: %s",
          scopeVersionMapping.getScope(), scopeVersionMapping);
    } else {
      scope.addScopeVersionMapping(scopeVersionMapping);
    }
  }
}
