package com.amadeus.aud.libraries.engine.graph.services.storage.inmemory;

import com.amadeus.aud.libraries.engine.graph.models.StoredEdge;
import com.amadeus.aud.libraries.engine.graph.models.StoredNode;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Flyweight pool that deduplicates identical {@link StoredEdge}, {@link StoredNode}, and {@link
 * String} instances across graph versions.
 *
 * <p>Between consecutive graph versions, the vast majority of edges and nodes are identical.
 * Without deduplication, each version materializes its own copy of every object, causing memory
 * usage to scale linearly with the number of retained versions. This pool ensures that structurally
 * equal objects share a single canonical instance in the JVM heap.
 *
 * <p>Thread-safety is guaranteed by the underlying {@link ConcurrentHashMap}s.
 */
public final class InMemoryDeduplicationPool {

  private volatile ConcurrentHashMap<StoredEdge, StoredEdge> edgePool = new ConcurrentHashMap<>();
  private volatile ConcurrentHashMap<StoredNode, StoredNode> nodePool = new ConcurrentHashMap<>();
  private volatile ConcurrentHashMap<String, String> stringPool = new ConcurrentHashMap<>();

  /**
   * Return the canonical instance for the given edge. If an equal edge is already in the pool, the
   * existing instance is returned (saving heap). Otherwise, the provided instance becomes
   * canonical.
   */
  StoredEdge internEdge(StoredEdge edge) {
    return edgePool.computeIfAbsent(edge, k -> k);
  }

  /** Return the canonical instance for the given node. */
  StoredNode internNode(StoredNode node) {
    return nodePool.computeIfAbsent(node, k -> k);
  }

  /**
   * Return the canonical instance for the given string. This deduplicates the many repeated landing
   * zone, platform, and target names that appear across thousands of edges/nodes.
   *
   * <p>Unlike {@link String#intern()}, this pool lives on the regular heap and is garbage-collected
   * when the pool itself is discarded or rebuilt.
   */
  @Nullable
  String internString(@Nullable String s) {
    if (s == null) {
      return null;
    }
    return stringPool.computeIfAbsent(s, k -> k);
  }

  /**
   * Rebuild all three pools from the edges and nodes still referenced by remaining graph versions.
   * Called after a version is evicted so that references to removed data can be garbage-collected.
   */
  synchronized void rebuild(
      Collection<? extends Collection<StoredEdge>> remainingEdges,
      Collection<? extends Collection<StoredNode>> remainingNodes) {

    ConcurrentHashMap<StoredEdge, StoredEdge> newEdgePool = new ConcurrentHashMap<>();
    ConcurrentHashMap<StoredNode, StoredNode> newNodePool = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, String> newStringPool = new ConcurrentHashMap<>();

    for (Collection<StoredEdge> edges : remainingEdges) {
      for (StoredEdge e : edges) {
        newEdgePool.putIfAbsent(e, e);
        internEdgeStrings(e, newStringPool);
      }
    }

    for (Collection<StoredNode> nodes : remainingNodes) {
      for (StoredNode n : nodes) {
        newNodePool.putIfAbsent(n, n);
        internNodeStrings(n, newStringPool);
      }
    }

    long oldEdges = edgePool.size();
    long oldNodes = nodePool.size();
    long oldStrings = stringPool.size();

    edgePool = newEdgePool;
    nodePool = newNodePool;
    stringPool = newStringPool;

    Log.infof(
        "Deduplication pool rebuilt — edges: %d→%d, nodes: %d→%d, strings: %d→%d",
        oldEdges,
        newEdgePool.size(),
        oldNodes,
        newNodePool.size(),
        oldStrings,
        newStringPool.size());
  }

  void logStats() {
    Log.infof(
        "Deduplication pool stats — edges: %d, nodes: %d, strings: %d",
        edgePool.size(), nodePool.size(), stringPool.size());
  }

  private static void internEdgeStrings(StoredEdge e, ConcurrentHashMap<String, String> pool) {
    putIfNotNull(pool, e.name());
    putIfNotNull(pool, e.dependencyInstanceId());
    putIfNotNull(pool, e.serviceId());
    putIfNotNull(pool, e.sourceNodeId());
    putIfNotNull(pool, e.destinationNodeId());
    putIfNotNull(pool, e.sourceNodeScope());
    putIfNotNull(pool, e.destinationNodeScope());
    putIfNotNull(pool, e.sourceNodeLandingZone());
    putIfNotNull(pool, e.sourceNodePlatform());
    putIfNotNull(pool, e.sourceNodeTarget());
    putIfNotNull(pool, e.destinationNodeLandingZone());
    putIfNotNull(pool, e.destinationNodePlatform());
    putIfNotNull(pool, e.destinationNodeTarget());
  }

  private static void internNodeStrings(StoredNode n, ConcurrentHashMap<String, String> pool) {
    putIfNotNull(pool, n.componentInstanceId());
    putIfNotNull(pool, n.landingZoneName());
    putIfNotNull(pool, n.platformName());
    putIfNotNull(pool, n.targetName());
  }

  private static void putIfNotNull(ConcurrentHashMap<String, String> pool, @Nullable String value) {
    if (value != null) {
      pool.putIfAbsent(value, value);
    }
  }
}
