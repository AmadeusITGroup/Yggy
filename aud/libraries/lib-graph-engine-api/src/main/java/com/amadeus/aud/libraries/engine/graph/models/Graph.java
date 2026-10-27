package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import java.util.Map;

/**
 * An immutable connectivity graph containing nodes ({@link ComponentInstance}) and {@link Edge}s.
 *
 * <p>Instances are created exclusively via {@link GraphBuilder}. Once built, the graph cannot be
 * modified — all maps are unmodifiable copies. This guarantees that pre-computed statistics and
 * cached query results remain consistent for the lifetime of the graph.
 *
 * @see GraphBuilder
 */
public final class Graph {

  private final String versionNumber;
  private final ImmutableMap<String, ComponentInstance> nodes;
  private final ImmutableMap<String, Edge> edges;

  /** Use {@link GraphBuilder#build()} to create instances. */
  private Graph(
      String versionNumber, Map<String, ComponentInstance> nodes, Map<String, Edge> edges) {
    this.versionNumber = versionNumber;
    this.nodes = ImmutableMap.copyOf(nodes);
    this.edges = ImmutableMap.copyOf(edges);
  }

  /** Factory used by {@link GraphBuilder}. */
  static Graph create(
      String versionNumber, Map<String, ComponentInstance> nodes, Map<String, Edge> edges) {
    return new Graph(versionNumber, nodes, edges);
  }

  public String getVersionNumber() {
    return versionNumber;
  }

  public ImmutableMap<String, ComponentInstance> getNodes() {
    return nodes;
  }

  public ImmutableMap<String, Edge> getEdges() {
    return edges;
  }

  public @Nullable Edge getEdge(String id) {
    return edges.get(id);
  }

  public boolean containsNode(String id) {
    return nodes.containsKey(id);
  }
}
