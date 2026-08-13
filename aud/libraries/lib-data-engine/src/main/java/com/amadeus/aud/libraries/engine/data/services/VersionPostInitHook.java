package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;

public interface VersionPostInitHook {
  void run(Version version);
}
