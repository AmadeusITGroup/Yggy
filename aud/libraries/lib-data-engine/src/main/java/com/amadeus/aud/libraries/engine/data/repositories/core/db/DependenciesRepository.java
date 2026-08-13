package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Dependency;

public class DependenciesRepository extends ConcurrentRepository<Dependency> {
  public DependenciesRepository() {
    super();
  }
}
