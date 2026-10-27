package com.amadeus.aud.libraries.engine.data;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.services.VersionService;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DefaultDataEngine implements DataEngine {
  private final VersionService versionService;

  @Inject
  public DefaultDataEngine(VersionService versionService) {
    this.versionService = versionService;
  }

  private Uni<Version> init(String versionNumber) {
    return versionService.getVersion(versionNumber);
  }

  public Uni<String> checkForNewVersion() {
    return versionService.checkForNewVersion();
  }

  public Uni<Version> getCurrentVersion() {
    return versionService.getCurrentVersion();
  }

  public Uni<Version> getVersion(String versionId) {
    Uni<Version> version;
    if (Constants.LATEST.equalsIgnoreCase(versionId)) {
      version = getCurrentVersion();
    } else {
      if (!VersionUtils.isSemver(versionId)) {
        throw new InvalidVersionException(versionId);
      }
      version = init(versionId);
    }
    return version;
  }
}
