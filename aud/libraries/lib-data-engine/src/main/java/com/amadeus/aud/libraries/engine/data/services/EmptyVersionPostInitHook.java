package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmptyVersionPostInitHook implements VersionPostInitHook {
  @Override
  public void run(Version version) {}
}
