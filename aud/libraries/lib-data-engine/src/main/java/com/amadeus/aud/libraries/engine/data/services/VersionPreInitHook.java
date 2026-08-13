package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.smallrye.mutiny.Uni;

public interface VersionPreInitHook {
  Uni<Void> run(Version version, boolean associateWithRequirement);
}
