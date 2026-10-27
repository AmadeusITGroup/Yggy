package com.amadeus.aud.libraries.engine.graph.services.storage;

import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Abstraction over the graph engine. Implementations (in-memory, PostgreSQL, remote REST client)
 * live in {@code lib-graph-engine}; consumers only depend on this interface.
 */
public interface GraphEngineService {

  /** Maximum page size used when retrieving all edges for a scope (target, platform, etc.). */
  int MAX_EDGE_PAGE_SIZE = 4_000_000;

  /** Build (or refresh) the graph for the given version. */
  Uni<Void> init(String versionNumber);

  /** Return the cached graph for the given version, or {@code null} if not yet built. */
  @Deprecated(since = "Connectivity Engine", forRemoval = true)
  @Nullable
  Graph getGraph(String versionNumber);

  /** Returns {@code true} when a non-empty graph is cached for the version. */
  boolean isReady(String versionNumber);

  /** Version numbers for which a graph is currently cached. */
  Set<String> getAvailableVersionNumbers();

  Uni<List<Edge>> getGraphEdgesByTargets(
      String versionString, int page, int size, List<String> targets, EdgeDirection direction);

  Uni<List<Edge>> getGraphEdgesByPlatforms(
      String versionString, int page, int size, List<String> platforms, EdgeDirection direction);

  Uni<List<Edge>> getGraphEdgesByLandingZones(
      String versionString, int page, int size, List<String> landingZones, EdgeDirection direction);

  Uni<List<Edge>> getGraphEdgesByNodeIds(
      String versionString,
      int page,
      int size,
      List<String> sourceNodeIds,
      List<String> destinationNodeIds);

  Uni<List<Edge>> getGraphEdgesByScope(String versionString, int page, int size, String scope);

  Uni<List<Node>> getGraphNodes(String versionNumber, int page, int size);

  Uni<List<Node>> getGraphNodesByTargets(
      String versionNumber, int page, int size, List<String> targets);

  Uni<List<Node>> getGraphNodesByPlatforms(
      String versionNumber, int page, int size, List<String> platforms);

  Uni<List<Node>> getGraphNodesByLandingZones(
      String versionNumber, int page, int size, List<String> landingZones);

  Uni<GraphStatistics> getGraphStatistics(String versionString);

  Uni<List<Edge>> getGraphEdges(String versionNumber, int page, int size, EdgeDirection direction);

  /**
   * Fetch all edges matching the given targets by paginating automatically. Returns all matching
   * edges regardless of {@link #MAX_EDGE_PAGE_SIZE}.
   */
  default List<Edge> getAllEdgesByTargets(
      String versionString, List<String> targets, EdgeDirection direction) {
    return fetchAllEdges(
        (page, size) -> getGraphEdgesByTargets(versionString, page, size, targets, direction));
  }

  /** Fetch all edges matching the given platforms by paginating automatically. */
  default List<Edge> getAllEdgesByPlatforms(
      String versionString, List<String> platforms, EdgeDirection direction) {
    return fetchAllEdges(
        (page, size) -> getGraphEdgesByPlatforms(versionString, page, size, platforms, direction));
  }

  /** Fetch all edges matching the given landing zones by paginating automatically. */
  default List<Edge> getAllEdgesByLandingZones(
      String versionString, List<String> landingZones, EdgeDirection direction) {
    return fetchAllEdges(
        (page, size) ->
            getGraphEdgesByLandingZones(versionString, page, size, landingZones, direction));
  }

  /** Fetch all edges matching the given node IDs by paginating automatically. */
  default List<Edge> getAllEdgesByNodeIds(
      String versionString, List<String> sourceNodeIds, List<String> destinationNodeIds) {
    return fetchAllEdges(
        (page, size) ->
            getGraphEdgesByNodeIds(versionString, page, size, sourceNodeIds, destinationNodeIds));
  }

  /** Fetch all edges matching the scope by paginating automatically. */
  default List<Edge> getAllEdgesByScope(String versionString, String scope) {
    return fetchAllEdges((page, size) -> getGraphEdgesByScope(versionString, page, size, scope));
  }

  @FunctionalInterface
  interface PagedEdgeFetcher {
    Uni<List<Edge>> fetch(int page, int size);
  }

  private static List<Edge> fetchAllEdges(PagedEdgeFetcher fetcher) {
    List<Edge> allEdges = new ArrayList<>();
    int page = 0;
    while (true) {
      List<Edge> batch = fetcher.fetch(page, MAX_EDGE_PAGE_SIZE).await().indefinitely();
      allEdges.addAll(batch);
      if (batch.size() < MAX_EDGE_PAGE_SIZE) {
        break;
      }
      page++;
    }
    return allEdges;
  }
}
