package com.amadeus.aud.libraries.engine.data;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.smallrye.mutiny.Uni;

public interface DataEngine {
  Uni<Version> getVersion(String versionId); // "latest" or semver; throws InvalidVersionException

  Uni<Version> getCurrentVersion();

  Uni<String> checkForNewVersion();
}
