package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Blacklist;

public class BlacklistsRepository extends ConcurrentRepository<Blacklist> {

  public BlacklistsRepository() {
    super();
  }
}
