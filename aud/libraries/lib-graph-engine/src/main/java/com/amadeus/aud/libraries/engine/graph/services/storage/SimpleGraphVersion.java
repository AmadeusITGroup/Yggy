package com.amadeus.aud.libraries.engine.graph.services.storage;

import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import java.time.LocalDateTime;

/**
 * Lightweight, backend-agnostic implementation of {@link GraphVersion}.
 *
 * <p>Used by the In-Memory backends that do not need a JPA entity. The record is immutable by
 * design, matching the graph-immutability invariant.
 */
public record SimpleGraphVersion(
    String version, LocalDateTime createdAt, GraphStatistics statistics, int versionRank)
    implements GraphVersion {

  /** Backwards-compatible constructor for backends that do not use version ranks. */
  public SimpleGraphVersion(String version, LocalDateTime createdAt, GraphStatistics statistics) {
    this(version, createdAt, statistics, 0);
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public GraphStatistics toStatistics() {
    return statistics;
  }

  @Override
  public int getVersionRank() {
    return versionRank;
  }
}
