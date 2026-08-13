package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the diff between two consecutive graph versions. Used by the version-range storage
 * backend to persist only the ~1% that changed instead of the full graph.
 *
 * @param addedNodes nodes present in the new version but absent in the previous one
 * @param removedNodeIds {@code uniqueId}s of nodes present in the previous version but absent in
 *     the new one
 * @param addedEdges edges present in the new version but absent in the previous one (or whose
 *     content changed — same id, different {@code equals})
 * @param removedEdgeIds edge ids present in the previous version but absent in the new one (or
 *     whose content changed)
 */
public record GraphDiff(
    Map<String, ComponentInstance> addedNodes,
    Set<String> removedNodeIds,
    Map<String, Edge> addedEdges,
    Set<String> removedEdgeIds) {

  /**
   * Computes the diff between two graph versions.
   *
   * <p>Identity is determined by map key ({@code ComponentInstance.getUniqueId()} for nodes, {@code
   * Edge.getId()} for edges). Content equality uses the standard {@code equals()} method — if an
   * edge has the same id but different content (e.g. changed {@code governanceActive}), it is
   * treated as a remove + add.
   *
   * @param previousNodeIds set of node {@code uniqueId}s active in the previous version
   * @param previousEdgeIds set of edge ids active in the previous version
   * @param current the new graph to compare against
   * @return a diff containing only the changes
   */
  public static GraphDiff compute(
      Set<String> previousNodeIds, Set<String> previousEdgeIds, Graph current) {

    Map<String, ComponentInstance> currentNodes = current.getNodes();
    Map<String, Edge> currentEdges = current.getEdges();

    // Nodes: added = in current but not in previous
    Map<String, ComponentInstance> addedNodes = new HashMap<>();
    for (var entry : currentNodes.entrySet()) {
      if (!previousNodeIds.contains(entry.getKey())) {
        addedNodes.put(entry.getKey(), entry.getValue());
      }
    }

    // Nodes: removed = in previous but not in current
    Set<String> removedNodeIds = new HashSet<>();
    for (String prevId : previousNodeIds) {
      if (!currentNodes.containsKey(prevId)) {
        removedNodeIds.add(prevId);
      }
    }

    // Edges: added = in current but not in previous
    Map<String, Edge> addedEdges = new HashMap<>();
    for (var entry : currentEdges.entrySet()) {
      if (!previousEdgeIds.contains(entry.getKey())) {
        addedEdges.put(entry.getKey(), entry.getValue());
      }
    }

    // Edges: removed = in previous but not in current
    Set<String> removedEdgeIds = new HashSet<>();
    for (String prevId : previousEdgeIds) {
      if (!currentEdges.containsKey(prevId)) {
        removedEdgeIds.add(prevId);
      }
    }

    return new GraphDiff(
        Map.copyOf(addedNodes),
        Set.copyOf(removedNodeIds),
        Map.copyOf(addedEdges),
        Set.copyOf(removedEdgeIds));
  }

  /** Returns {@code true} if there are no changes between the two versions. */
  public boolean isEmpty() {
    return addedNodes.isEmpty()
        && removedNodeIds.isEmpty()
        && addedEdges.isEmpty()
        && removedEdgeIds.isEmpty();
  }
}
