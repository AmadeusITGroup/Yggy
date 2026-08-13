package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import jakarta.annotation.Nullable;

public class ScopesRepository extends ConcurrentRepository<Scope> {
  private final Scope rootScope = new Scope();
  private boolean autoCreateScope = false;

  public ScopesRepository() {
    super();
    rootScope.setName("Root Scope");
  }

  public void autoCreateScope() {
    this.autoCreateScope = true;
  }

  @Override
  public @Nullable Scope get(@Nullable String id) {
    if (id == null) {
      return null;
    }
    if (autoCreateScope) {
      return getData()
          .computeIfAbsent(
              id,
              (String key) -> {
                Scope scope = new Scope();
                scope.setScope(rootScope);
                scope.setUniqueIdFrom(key);
                scope.setName(key);
                scope.setData(new V1Scope());
                return scope;
              });
    }
    return getData().get(id);
  }
}
