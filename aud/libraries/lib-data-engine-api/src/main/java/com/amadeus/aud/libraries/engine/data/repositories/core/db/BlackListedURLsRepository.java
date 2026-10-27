package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;

public class BlackListedURLsRepository extends ConcurrentRepository<BlackListedURL> {

  public BlackListedURLsRepository() {
    super();
  }
}
