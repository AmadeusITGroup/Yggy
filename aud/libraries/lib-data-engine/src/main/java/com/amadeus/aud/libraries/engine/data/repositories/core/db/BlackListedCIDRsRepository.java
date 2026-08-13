package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;

public class BlackListedCIDRsRepository extends ConcurrentRepository<BlackListedCIDR> {

  public BlackListedCIDRsRepository() {
    super();
  }
}
