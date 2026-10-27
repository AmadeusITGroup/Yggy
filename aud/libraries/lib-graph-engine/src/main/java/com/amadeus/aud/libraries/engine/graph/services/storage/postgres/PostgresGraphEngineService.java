package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphDiff;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@PostgresGraphEngine
@ApplicationScoped
public class PostgresGraphEngineService implements GraphEngineService {

  private final PostgresGraphRepository graphPanacheRepository;
  private final PostgresNodeRepository nodePanacheRepository;
  private final PostgresEdgeRepository edgePanacheRepository;
  private final DataEngine dataEngine;
  private final GraphService graphService;

  @Inject
  public PostgresGraphEngineService(
      PostgresGraphRepository graphPanacheRepository,
      PostgresNodeRepository nodePanacheRepository,
      PostgresEdgeRepository edgePanacheRepository,
      DataEngine dataEngine,
      GraphService graphService) {
    this.graphPanacheRepository = graphPanacheRepository;
    this.nodePanacheRepository = nodePanacheRepository;
    this.edgePanacheRepository = edgePanacheRepository;
    this.dataEngine = dataEngine;
    this.graphService = graphService;
  }

  @Override
  @Transactional
  public Uni<Void> init(String versionNumber) {
    Instant startTime = Instant.now();
    return resolveVersion(versionNumber)
        .onItem()
        .transform(
            (Version version) -> {
              GraphBuilder builder = new GraphBuilder(version.getVersionNumber());
              graphService.populateGraphFromDependencies(builder, version);
              return builder.build();
            })
        .onItem()
        .invoke(this::saveGraph)
        .onItem()
        .invoke(
            ignored ->
                Log.debugf(
                    "Graph Initialization is done: version=%s, Time=%s",
                    versionNumber, Duration.between(startTime, Instant.now())))
        .replaceWithVoid();
  }

  @Override
  @SuppressWarnings("removal")
  @Transactional
  @Deprecated(since = "Connectivity Engine", forRemoval = true)
  public @Nullable Graph getGraph(String versionNumber) {
    Log.warnf("getGraph() called for version %s — this method is deprecated", versionNumber);
    return null;
  }

  @Override
  @Transactional
  public boolean isReady(String versionNumber) {
    Instant startTime = Instant.now();
    boolean present = graphPanacheRepository.findByVersion(versionNumber).isPresent();
    if (!present) {
      Log.debugf(
          "Version is not available: version=%s, Time=%s",
          versionNumber, Duration.between(startTime, Instant.now()));
    } else {
      Log.debugf(
          "Version is available: version=%s, Time=%s",
          versionNumber, Duration.between(startTime, Instant.now()));
    }
    return present;
  }

  @Override
  @Transactional
  public Set<String> getAvailableVersionNumbers() {
    Instant startTime = Instant.now();
    Set<String> versions = graphPanacheRepository.findAllVersions();

    Log.debugf(
        "PanacheEntity repository loading all versions: Count=%d, Time=%s",
        versions.size(), Duration.between(startTime, Instant.now()));

    return versions;
  }

  public Optional<GraphEntity> getLatestGraph(String versionString) {
    return "latest".equalsIgnoreCase(versionString)
        ? graphPanacheRepository.findLatestVersion()
        : graphPanacheRepository.findByVersion(versionString);
  }

  // ==================== READ OPERATIONS (version-range queries) ====================

  @Override
  @Transactional
  public Uni<List<Node>> getGraphNodes(String versionString, int page, int size) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }

    int versionRank = graphEntity.get().getVersionRank();
    Instant startTime = Instant.now();
    List<Node> nodes = nodePanacheRepository.findByVersionRank(versionRank, page, size);

    Log.debugf(
        "PanacheEntity repository loading: Count=%d, Time=%s",
        nodes.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(nodes);
  }

  @Override
  @Transactional
  public Uni<List<Node>> getGraphNodesByTargets(
      String versionString, int page, int size, List<String> targets) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }

    int versionRank = graphEntity.get().getVersionRank();
    Instant startTime = Instant.now();
    List<Node> nodes =
        nodePanacheRepository.findByVersionRankWithTargets(versionRank, page, size, targets);

    Log.debugf(
        "PanacheEntity repository loading: Count=%d, Time=%s",
        nodes.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(nodes);
  }

  @Override
  @Transactional
  public Uni<List<Node>> getGraphNodesByPlatforms(
      String versionString, int page, int size, List<String> platforms) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }

    int versionRank = graphEntity.get().getVersionRank();
    Instant startTime = Instant.now();
    List<Node> nodes =
        nodePanacheRepository.findByVersionRankWithPlatforms(versionRank, page, size, platforms);

    Log.debugf(
        "PanacheEntity repository loading: Count=%d, Time=%s",
        nodes.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(nodes);
  }

  @Override
  @Transactional
  public Uni<List<Node>> getGraphNodesByLandingZones(
      String versionString, int page, int size, List<String> landingZones) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }

    int versionRank = graphEntity.get().getVersionRank();
    Instant startTime = Instant.now();
    List<Node> nodes =
        nodePanacheRepository.findByVersionRankWithLandingZones(
            versionRank, page, size, landingZones);

    Log.debugf(
        "PanacheEntity repository loading: Count=%d, Time=%s",
        nodes.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(nodes);
  }

  @Override
  @Transactional
  public Uni<GraphStatistics> getGraphStatistics(String versionString) {
    return resolveVersion(versionString)
        .onItem()
        .transform(
            (Version version) ->
                graphPanacheRepository
                    .findStatisticsByVersion(version.getVersionNumber())
                    .orElse(null));
  }

  @Override
  @Transactional
  public Uni<List<Edge>> getGraphEdges(
      String versionString, int page, int size, EdgeDirection direction) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    List<Edge> edges = edgePanacheRepository.findByVersionRank(versionRank, page, size);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Override
  @Transactional
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByTargets(
      String versionString, int page, int size, List<String> targets, EdgeDirection direction) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    List<Edge> edges =
        edgePanacheRepository.findByVersionRankWithTargets(
            versionRank, page, size, targets, direction);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Override
  @Transactional
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByPlatforms(
      String versionString, int page, int size, List<String> platforms, EdgeDirection direction) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    List<Edge> edges =
        edgePanacheRepository.findByVersionRankWithPlatforms(
            versionRank, page, size, platforms, direction);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Override
  @Transactional
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
      getGraphEdgesByLandingZones(
          String versionString,
          int page,
          int size,
          List<String> landingZones,
          EdgeDirection direction) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    List<Edge> edges =
        edgePanacheRepository.findByVersionRankWithLandingZones(
            versionRank, page, size, landingZones, direction);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Override
  @Transactional
  public Uni<List<Edge>> getGraphEdgesByNodeIds(
      String versionString,
      int page,
      int size,
      List<String> sourceNodeIds,
      List<String> destinationNodeIds) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    int sourceCount = sourceNodeIds == null ? 0 : sourceNodeIds.size();
    int destinationCount = destinationNodeIds == null ? 0 : destinationNodeIds.size();
    Log.debugf(
        "Fetching edges for version rank %d with source node IDs filter (count=%d) and destination node IDs filter (count=%d)",
        versionRank, sourceCount, destinationCount);

    List<Edge> edges =
        edgePanacheRepository.findByVersionRankWithNodeIds(
            versionRank, sourceNodeIds, destinationNodeIds, page, size);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Override
  @Transactional
  public Uni<List<Edge>> getGraphEdgesByScope(
      String versionString, int page, int size, String scope) {
    Optional<GraphEntity> graphEntity = getLatestGraph(versionString);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionString);
      return Uni.createFrom().item(List.of());
    }
    int versionRank = graphEntity.get().getVersionRank();

    Instant startTime = Instant.now();
    Log.debugf("Fetching edges for version rank %d with scope filter %s", versionRank, scope);

    List<Edge> edges =
        edgePanacheRepository.findByVersionRankWithScope(versionRank, page, size, scope);

    Log.debugf(
        "PanacheEntity with edges repository loading: Count=%d, Time=%s",
        edges.size(), Duration.between(startTime, Instant.now()));

    return Uni.createFrom().item(edges);
  }

  @Transactional
  public long countNodes(GraphVersion graphVersion) {
    int versionRank = graphVersion.getVersionRank();
    Instant startTime = Instant.now();
    long count = nodePanacheRepository.countByVersionRank(versionRank);

    Log.debugf(
        "PanacheEntity repository counting nodes: Count=%d, Time=%s",
        count, Duration.between(startTime, Instant.now()));

    return count;
  }

  @Transactional
  public long countEdges(GraphVersion graphVersion) {
    int versionRank = graphVersion.getVersionRank();
    Instant startTime = Instant.now();
    long count = edgePanacheRepository.countByVersionRank(versionRank);

    Log.debugf(
        "PanacheEntity repository counting edges: Count=%d, Time=%s",
        count, Duration.between(startTime, Instant.now()));

    return count;
  }

  // ==================== WRITE OPERATIONS (version-range) ====================

  @Transactional(rollbackOn = Exception.class)
  public void saveGraph(@Nullable Graph graph) {
    if (graph == null) {
      throw new IllegalArgumentException("Graph cannot be null");
    }

    String versionNumber = graph.getVersionNumber();

    Map<String, ComponentInstance> nodesMap = graph.getNodes();
    if (nodesMap == null || nodesMap.isEmpty()) {
      Log.warnf("No nodes to save for graph %s", versionNumber);
      return;
    }
    Map<String, com.amadeus.aud.libraries.engine.graph.models.Edge> edgesMap = graph.getEdges();
    if (edgesMap == null || edgesMap.isEmpty()) {
      Log.warnf("No edges to save for graph %s", versionNumber);
      return;
    }

    Log.infof(
        "Starting graph save for version %s with %d nodes and %d edges",
        versionNumber, nodesMap.size(), edgesMap.size());

    try {
      Instant startTime = Instant.now();

      // Check if there is a previous version to compute incremental diff
      Optional<GraphEntity> previousGraphOpt = graphPanacheRepository.findByMaxVersionRank();

      if (previousGraphOpt.isPresent()) {
        GraphEntity previousGraph = previousGraphOpt.get();
        int previousRank = previousGraph.getVersionRank();
        Log.infof("Previous version found (rank=%d), computing incremental diff", previousRank);

        Set<String> previousNodeIds = nodePanacheRepository.findActiveNodeIds(previousRank);
        Set<String> previousEdgeKeys = edgePanacheRepository.findActiveEdgeKeys(previousRank);

        GraphDiff diff = GraphDiff.compute(previousNodeIds, previousEdgeKeys, graph);

        Log.infof(
            "Diff computed: +%d/-%d nodes, +%d/-%d edges",
            diff.addedNodes().size(),
            diff.removedNodeIds().size(),
            diff.addedEdges().size(),
            diff.removedEdgeIds().size());

        executeIncrementalSave(versionNumber, graph, diff);
      } else {
        executeFullSave(versionNumber, graph, nodesMap, edgesMap);
      }

      Log.infof(
          "Graph saved to database for version %s in %s",
          versionNumber, Duration.between(startTime, Instant.now()));

    } catch (GraphSaveException e) {
      Log.errorf(e, "Failed to save graph for version %s. Transaction rolled back.", versionNumber);
      throw e;
    } catch (PersistenceException e) {
      Log.errorf(e, "Failed to save graph for version %s. Transaction rolled back.", versionNumber);
      throw new GraphSaveException("Graph save failed for version " + versionNumber, e);
    }
  }

  /** Full snapshot save — used for the first version. */
  private void executeFullSave(
      String versionNumber,
      Graph graph,
      Map<String, ComponentInstance> nodesMap,
      Map<String, com.amadeus.aud.libraries.engine.graph.models.Edge> edgesMap) {
    int newRank = graphPanacheRepository.computeNextVersionRank();
    GraphEntity graphEntity = getOrCreateGraphEntity(versionNumber, newRank);
    Version version = resolveVersion(versionNumber).await().indefinitely();
    graphEntity.computeAndApplyStatistics(graph, version);
    Log.debugf(
        "Graph entity with ID: %s, rank: %d for version %s",
        (Object) graphEntity.getId(), newRank, versionNumber);

    Log.infof(
        "Full snapshot: Processing %d nodes and %d edges with PostgreSQL COPY",
        nodesMap.size(), edgesMap.size());

    Map<ComponentInstance, Long> vertexIdMap =
        nodePanacheRepository.batchInsertNodesCopy(newRank, nodesMap.values());

    if (vertexIdMap == null || vertexIdMap.isEmpty()) {
      throw new GraphSaveException(
          "Failed to insert nodes for graph version "
              + versionNumber
              + ": no node ID mappings were produced");
    }

    // Table is guaranteed empty here (first version ever) — dropping FK/index maintenance during
    // the bulk COPY and rebuilding them afterwards is far cheaper than per-row maintenance.
    edgePanacheRepository.dropIndexesAndConstraintsForBulkLoad();
    try {
      edgePanacheRepository.batchInsertEdgesCopyBinary(newRank, edgesMap.values(), vertexIdMap);
    } finally {
      edgePanacheRepository.recreateIndexesAndConstraintsAfterBulkLoad();
    }

    verifyGraphData(graphEntity, nodesMap.size(), edgesMap.size());
  }

  /** Incremental save — only persist the diff. */
  private void executeIncrementalSave(String versionNumber, Graph graph, GraphDiff diff) {
    int newRank = graphPanacheRepository.computeNextVersionRank();
    GraphEntity graphEntity = getOrCreateGraphEntity(versionNumber, newRank);
    Version version = resolveVersion(versionNumber).await().indefinitely();
    graphEntity.computeAndApplyStatistics(graph, version);
    Log.debugf(
        "Graph entity with ID: %s, rank: %d for version %s",
        (Object) graphEntity.getId(), newRank, versionNumber);

    // 1. Close version ranges for removed nodes
    if (!diff.removedNodeIds().isEmpty()) {
      Log.infof("Closing version ranges for %d removed nodes", diff.removedNodeIds().size());
      // Close edges referencing removed nodes first (by their edge keys)
      nodePanacheRepository.closeNodeVersionRanges(diff.removedNodeIds(), newRank);
    }

    // 2. Close version ranges for removed edges
    if (!diff.removedEdgeIds().isEmpty()) {
      Log.infof("Closing version ranges for %d removed edges", diff.removedEdgeIds().size());
      edgePanacheRepository.closeEdgeVersionRanges(diff.removedEdgeIds(), newRank);
    }

    // 3. Insert new nodes
    Map<ComponentInstance, Long> newNodeIdMap = Map.of();
    if (!diff.addedNodes().isEmpty()) {
      Log.infof("Inserting %d new nodes via COPY", diff.addedNodes().size());
      newNodeIdMap =
          nodePanacheRepository.batchInsertNodesCopy(newRank, diff.addedNodes().values());
    }

    // 4. Insert new edges — resolve node IDs for new edges
    if (!diff.addedEdges().isEmpty()) {
      Log.infof("Inserting %d new edges via COPY BINARY", diff.addedEdges().size());

      // Build complete node ID map: existing active nodes + newly inserted nodes
      Map<ComponentInstance, Long> allNodeIdMap = new HashMap<>();

      // Load existing active node IDs (from previous version — still active at newRank)
      Map<String, Long> existingNodeIdMap = nodePanacheRepository.loadActiveNodeIdMap(newRank);

      // Map ComponentInstance to Long using the edge's source/destination references
      for (com.amadeus.aud.libraries.engine.graph.models.Edge edge : diff.addedEdges().values()) {
        mapNodeId(allNodeIdMap, edge.getSource(), existingNodeIdMap, newNodeIdMap);
        mapNodeId(allNodeIdMap, edge.getDestination(), existingNodeIdMap, newNodeIdMap);
      }

      edgePanacheRepository.batchInsertEdgesCopyBinary(
          newRank, diff.addedEdges().values(), allNodeIdMap);
    }

    // 5. Verify total active counts match the full graph
    verifyGraphData(graphEntity, graph.getNodes().size(), graph.getEdges().size());
  }

  /** Maps a ComponentInstance to its database ID, checking new inserts first then existing. */
  private static void mapNodeId(
      Map<ComponentInstance, Long> target,
      ComponentInstance node,
      Map<String, Long> existingNodeIdMap,
      Map<ComponentInstance, Long> newNodeIdMap) {
    if (target.containsKey(node)) {
      return;
    }

    Long id = newNodeIdMap.get(node);
    if (id == null) {
      id = existingNodeIdMap.get(node.id());
    }
    if (id != null) {
      target.put(node, id);
    }
  }

  // ==================== DELETE OPERATIONS ====================

  @Transactional(rollbackOn = Exception.class)
  public com.amadeus.aud.libraries.engine.graph.models.dto.Graph deleteGraph(
      GraphVersion graphVersion) {
    GraphEntity graphEntity = asEntity(graphVersion);
    String versionId = graphEntity.getVersion();
    int versionRank = graphEntity.getVersionRank();

    long nodeCount = countNodes(graphVersion);
    long edgeCount = countEdges(graphVersion);
    LocalDateTime deletedAt = LocalDateTime.now();

    com.amadeus.aud.libraries.engine.graph.models.dto.Graph deletedGraph =
        new com.amadeus.aud.libraries.engine.graph.models.dto.Graph(
            graphEntity.getCreatedAt(), deletedAt, graphEntity.getVersion(), nodeCount, edgeCount);

    // Clean up: delete nodes/edges added at this rank, reopen those closed at this rank
    cleanupGraphData(graphEntity);
    graphPanacheRepository.delete(graphEntity);

    Log.infof(
        "Deleted graph version: %s rank: %d (nodes: %d, edges: %d)",
        versionId, versionRank, nodeCount, edgeCount);

    return deletedGraph;
  }

  /**
   * Clean up version-range data for the given graph entity. Deletes nodes/edges added at this rank
   * and reopens nodes/edges closed at this rank.
   */
  @Transactional
  public void cleanupGraphData(GraphEntity graphEntity) {
    try {
      int rank = graphEntity.getVersionRank();
      Log.debugf("Cleaning up data for graph version %s (rank %d)", graphEntity.getVersion(), rank);

      // Delete edges/nodes that were created at this rank
      edgePanacheRepository.clearEdgesForVersionRank(rank);
      nodePanacheRepository.clearNodesForVersionRank(rank);

      // Reopen edges/nodes that were closed at this rank
      edgePanacheRepository.reopenEdgesClosedAtRank(rank);
      nodePanacheRepository.reopenNodesClosedAtRank(rank);

      Log.debugf("Cleaned up data for version %s", graphEntity.getVersion());

    } catch (RuntimeException e) {
      Log.errorf(e, "Failed to cleanup graph data for version %s", graphEntity.getVersion());
      throw new GraphSaveException("Failed to cleanup graph data", e);
    }
  }

  // ==================== HELPER METHODS ====================

  @Transactional
  public GraphEntity createGraphEntity(String versionNumber, int versionRank) {
    GraphEntity graphEntity = new GraphEntity();
    graphEntity.setVersion(versionNumber);
    graphEntity.setVersionRank(versionRank);
    graphPanacheRepository.persist(graphEntity);
    graphPanacheRepository.flush();
    return graphEntity;
  }

  @Transactional
  public GraphEntity getOrCreateGraphEntity(String versionNumber, int versionRank) {
    Optional<GraphEntity> existing = graphPanacheRepository.findByVersion(versionNumber);
    if (existing.isPresent()) {
      Log.infof(
          "Graph version %s already exists (ID: %d), replacing data",
          versionNumber, existing.get().getId());
      cleanupGraphData(existing.get());
      existing.get().setVersionRank(versionRank);
      return existing.get();
    }
    return createGraphEntity(versionNumber, versionRank);
  }

  @Transactional
  public @Nullable GraphEntity loadGraph(String versionNumber) {
    Instant startTime = Instant.now();
    Optional<GraphEntity> graphEntity = graphPanacheRepository.findByVersion(versionNumber);
    if (graphEntity.isEmpty()) {
      Log.warnf("Graph not found in database for version %s.", versionNumber);
    } else {
      Log.infof(
          "Graph found in database for version %s in %s.",
          versionNumber, Duration.between(startTime, Instant.now()));
    }

    return graphEntity.orElse(null);
  }

  /** Verify that the graph was saved correctly by checking active counts at the version rank. */
  private void verifyGraphData(GraphEntity graphEntity, int expectedNodes, int expectedEdges) {
    try {
      int versionRank = graphEntity.getVersionRank();
      long actualNodes = nodePanacheRepository.countByVersionRank(versionRank);

      if (actualNodes != expectedNodes) {
        throw new GraphSaveException(
            String.format(
                "Node count mismatch for version %s (rank %d). Expected: %d, Actual: %d",
                graphEntity.getVersion(), versionRank, expectedNodes, actualNodes));
      }

      long actualEdges = edgePanacheRepository.countByVersionRank(versionRank);
      if (actualEdges != expectedEdges) {
        throw new GraphSaveException(
            String.format(
                "Edge count mismatch for version %s (rank %d). Expected: %d, Actual: %d",
                graphEntity.getVersion(), versionRank, expectedEdges, actualEdges));
      }

      Log.debugf(
          "Graph save verification successful for version %s: %d nodes, %d edges",
          graphEntity.getVersion(), actualNodes, actualEdges);

    } catch (GraphSaveException e) {
      throw e;
    } catch (RuntimeException e) {
      Log.errorf(e, "Graph save verification failed for version %s", graphEntity.getVersion());
      throw new GraphSaveException("Graph save verification failed", e);
    }
  }

  public long countGraphs() {
    return graphPanacheRepository.count();
  }

  private static GraphEntity asEntity(GraphVersion graphVersion) {
    return (GraphEntity) graphVersion;
  }

  private Uni<Version> resolveVersion(String versionString) {
    return dataEngine
        .getVersion(versionString)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }
}
