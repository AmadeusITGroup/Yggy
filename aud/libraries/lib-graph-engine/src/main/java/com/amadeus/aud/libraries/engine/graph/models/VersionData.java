package com.amadeus.aud.libraries.engine.graph.models;

import java.util.List;

/** Per-version storage container holding denormalized graph data and pre-computed statistics. */
public final class VersionData {
  private final List<StoredEdge> edges;
  private final List<StoredNode> nodes;
  private final GraphStatistics statistics;

  public VersionData(List<StoredEdge> edges, List<StoredNode> nodes, GraphStatistics statistics) {
    this.edges = edges;
    this.nodes = nodes;
    this.statistics = statistics;
  }

  public List<StoredEdge> getEdges() {
    return edges;
  }

  public List<StoredNode> getNodes() {
    return nodes;
  }

  public GraphStatistics getStatistics() {
    return statistics;
  }
}
