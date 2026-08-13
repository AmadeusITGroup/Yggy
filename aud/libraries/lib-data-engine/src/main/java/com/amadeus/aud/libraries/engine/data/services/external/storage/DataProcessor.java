package com.amadeus.aud.libraries.engine.data.services.external.storage;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;

/**
 * Interface for processors that handle data loading. To be extended by specific implementations for
 * different data types or sources. For ex. StorageAccountDataProcessor for Azure Storage Account
 * data processing.
 *
 * @param <R> The type of repository or result produced by the processor
 */
public interface DataProcessor<R> {

  /**
   * Initialize objects from a specific version or the latest version.
   *
   * @param version The version to load, or null for the latest
   * @return A Uni emitting the repository with the loaded data
   */
  default Uni<R> initObjectsWithFallback(String version) {
    return initObjects(version, true)
        .onFailure()
        .recoverWithUni(
            (Throwable throwable) -> {
              Log.warnf(
                  "Could not load as JSON for version %s, falling back to YAML. Error: %s",
                  version, throwable.getMessage());
              return initObjects(version, false)
                  .onFailure()
                  .invoke(
                      (Throwable fallbackThrowable) ->
                          Log.errorf(
                              "Could not load as YAML for version %s. Error: %s",
                              version, fallbackThrowable.getMessage()));
            });
  }

  /**
   * Initialize objects from a specific version or the latest version.
   *
   * @param version The version to load, or null for the latest
   * @return A Uni emitting the repository with the loaded data
   */
  default Uni<R> initObjects(String version, boolean jsonPreferred) {
    return null;
  }
}
