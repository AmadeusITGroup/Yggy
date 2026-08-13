package com.amadeus.aud.libraries.engine.data;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.services.VersionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class DataEngine {
  private final VersionService versionService;

  @Inject
  public DataEngine(VersionService versionService) {
    this.versionService = versionService;
  }

  public Uni<Version> init(String versionNumber) {
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
      version = init(versionId);
    }
    return version;
  }
}
