package com.amadeus.aud.libraries.engine.data.repositories.core;

import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AudDataRepository
    extends BaseRepository<
        ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>>> {

  public AudDataRepository() {
    super(new ConcurrentHashMap<>());
  }
}
