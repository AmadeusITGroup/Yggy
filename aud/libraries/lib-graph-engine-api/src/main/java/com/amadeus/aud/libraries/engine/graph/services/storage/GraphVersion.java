package com.amadeus.aud.libraries.engine.graph.services.storage;

import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import java.time.LocalDateTime;

/**
 * Backend-agnostic representation of a stored graph version.
 *
 * <p>All two storage backends (PostgreSQL, In-Memory) return implementations of this interface
 * through {@link GraphEngineService}. Consumers of the storage layer should depend only on this
 * interface, never on backend-specific entities.
 *
 * <p>Graphs are <strong>immutable after construction</strong>: once saved, their nodes and edges
 * never change. This invariant enables aggressive caching and denormalization strategies in each
 * backend.
 *
 * @see GraphEngineService
 * @see SimpleGraphVersion
 */
public interface GraphVersion {

  /** The semantic version string of this graph (e.g. {@code "1.2.3"}). */
  String getVersion();

  /** The timestamp when this graph version was first persisted. */
  LocalDateTime getCreatedAt();

  /** Pre-computed statistics for this graph (node/edge counts per scope). */
  GraphStatistics toStatistics();

  /**
   * Sequential rank used by version-range storage. Monotonically increasing integer independent of
   * semver, enabling simple range comparisons for nodes and edges.
   */
  int getVersionRank();
}
