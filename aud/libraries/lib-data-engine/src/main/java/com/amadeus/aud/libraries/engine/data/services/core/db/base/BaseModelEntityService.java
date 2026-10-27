package com.amadeus.aud.libraries.engine.data.services.core.db.base;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;

public abstract class BaseModelEntityService<T extends AbstractModelEntity<N>, N>
    extends BaseLogService {

  protected abstract ConcurrentRepository<T> getRepository(Version version);

  @LogExecutionTime
  public abstract void init(Version version);

  protected abstract void init(Version version, Class<T> toClazz, Class<N> fromClazz);
}
