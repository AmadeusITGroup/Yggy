package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import jakarta.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mutable builder for constructing a {@link Graph}. Supports concurrent writes via {@link
 * ConcurrentHashMap}.
 */
public class GraphBuilder {
  private final String versionNumber;
  private final boolean storeFullLink;
  private final ConcurrentHashMap<String, ComponentInstance> nodes = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Edge> edges = new ConcurrentHashMap<>();

  public GraphBuilder(String versionNumber) {
    this(versionNumber, false);
  }

  public GraphBuilder(String versionNumber, boolean storeFullLink) {
    this.versionNumber = versionNumber;
    this.storeFullLink = storeFullLink;
  }

  public boolean isStoreFullLink() {
    return storeFullLink;
  }

  public void putNode(String id, ComponentInstance vertex) {
    nodes.put(id, vertex);
  }

  public void putEdge(String id, Edge edge) {
    edges.put(id, edge);
  }

  public boolean containsNode(String id) {
    return nodes.containsKey(id);
  }

  public @Nullable Edge getEdge(String id) {
    return edges.get(id);
  }

  public int nodesSize() {
    return nodes.size();
  }

  public int edgesSize() {
    return edges.size();
  }

  public String getVersionNumber() {
    return versionNumber;
  }

  /** Builds an immutable {@link Graph} from the current state. */
  public Graph build() {
    return Graph.create(versionNumber, nodes, edges);
  }
}
