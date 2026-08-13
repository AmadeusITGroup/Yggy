package com.amadeus.aud.libraries.engine.data.services.external.storage;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import java.util.List;

public interface AudVersionsService {

  /**
   * Check for a new version of the data.
   *
   * @return A Uni emitting the new version string if available, or null if no new version is found
   */
  default Uni<String> checkNewVersion() {
    return checkNewVersion(null);
  }

  /**
   * Check for a new version of the data.
   *
   * @return A Uni emitting the new version string if available, or null if no new version is found
   * @param lastVersionNumber The last known version number, or null to check for the latest version
   */
  Uni<String> checkNewVersion(@Nullable String lastVersionNumber);

  Uni<List<SingleVersion>> listAllVersions();

  Uni<String> getLatestVersion();
}
