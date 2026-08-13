package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmptyComponentInstanceLogic implements ComponentInstanceLogic {
  @Override
  public void accept(Version version) {
    // do nothing
  }
}
