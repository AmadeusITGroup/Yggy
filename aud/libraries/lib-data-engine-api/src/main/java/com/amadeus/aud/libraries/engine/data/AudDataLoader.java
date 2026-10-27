package com.amadeus.aud.libraries.engine.data;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import io.smallrye.mutiny.Uni;

/**
 * Loads the AUD data of one version.
 *
 * <p>Lets modules that only depend on {@code lib-data-engine-api} load data; the implementation is
 * provided at runtime by {@code lib-data-engine}.
 */
public interface AudDataLoader {

  /**
   * Loads the AUD data of a version.
   *
   * @param version the version to load, e.g. {@code "latest"}
   * @param jsonPreferred load the JSON export when available, YAML otherwise
   * @return a Uni emitting the repository with the loaded data
   */
  Uni<AudDataRepository> initObjects(String version, boolean jsonPreferred);
}
