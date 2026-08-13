package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Namespace;
import com.amadeus.aud.libraries.yaml.models.V1NamespaceSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class NamespaceService extends BaseService<Namespace, V1Namespace> {

  @Override
  protected NamespacesRepository getRepository(Version version) {
    NamespacesRepository namespacesRepository = new NamespacesRepository();
    version.setNamespacesRepository(namespacesRepository);
    return namespacesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ScopesRepository scopesRepository = version.getScopesRepository();
    if (scopesRepository == null) {
      Log.errorf("Scope Repository not found for version %s", version);
      return;
    }

    init(
        version,
        Namespace.class,
        V1Namespace.class,
        (Namespace namespace, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addNamespace(namespace);

          V1NamespaceSpec spec = namespace.getData().getSpec();
          if (spec != null && spec.getAuthorizedScopes() != null) {
            for (String authorizedScopeName : spec.getAuthorizedScopes()) {
              Scope authorizedScope = scopesRepository.get(authorizedScopeName);
              namespace.addAuthorizedScope(authorizedScope);
            }
          }
        });
  }
}
