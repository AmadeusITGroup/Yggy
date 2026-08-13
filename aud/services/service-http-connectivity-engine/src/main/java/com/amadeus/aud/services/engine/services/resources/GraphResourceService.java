package com.amadeus.aud.services.engine.services.resources;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Graph;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import com.amadeus.aud.services.engine.resources.models.GraphInitStatusResponse;
import com.amadeus.aud.services.engine.resources.models.GraphLoadStatus;
import com.amadeus.aud.services.engine.services.core.GraphCoreService;
import com.amadeus.aud.services.engine.services.core.InitRequestStatus;
import com.amadeus.aud.services.engine.services.core.InitStatus;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;

@Singleton
public class GraphResourceService {
  private final GraphCoreService graphCoreService;
  private final GraphEngineService graphDbService;
  private final DataEngine dataEngine;

  @Inject
  public GraphResourceService(
      GraphCoreService graphCoreService,
      GraphEngineService graphDbServiceInstance,
      DataEngine dataEngine) {
    this.graphCoreService = graphCoreService;
    this.graphDbService = graphDbServiceInstance;
    this.dataEngine = dataEngine;
  }

  private Graph toGraphResponse(GraphVersion graphVersion) {
    if (graphVersion == null) {
      return null;
    }

    GraphStatistics statistics = graphVersion.toStatistics();
    long nodesCount = statistics == null ? 0 : statistics.globalNodesCount();
    long edgesCount = statistics == null ? 0 : statistics.globalLinksCount();

    return new Graph(
        graphVersion.getCreatedAt(), null, graphVersion.getVersion(), nodesCount, edgesCount);
  }

  public Graph getGraph(String versionId) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionId);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionId);
    }
    return toGraphResponse(graphVersion);
  }

  public Response initGraphAsync(String versionId) {
    InitRequestStatus status = graphCoreService.requestVersionInitialization(versionId);

    return switch (status) {
      case INVALID_VERSION -> throw new BadRequestException("Version cannot be empty");
      case ALREADY_READY ->
          Response.ok(
                  new GraphInitStatusResponse(
                      versionId, GraphLoadStatus.READY, "Graph is already initialized"))
              .build();
      case QUEUED ->
          Response.accepted(
                  new GraphInitStatusResponse(
                      versionId, GraphLoadStatus.QUEUED, "Graph initialization has been queued"))
              .build();
      case ALREADY_IN_PROGRESS ->
          Response.accepted(
                  new GraphInitStatusResponse(
                      versionId,
                      GraphLoadStatus.IN_PROGRESS,
                      "Graph initialization is already in progress"))
              .build();
    };
  }

  public Response getInitStatus(String versionId) {
    InitStatus status = graphCoreService.getVersionInitializationStatus(versionId);

    return switch (status) {
      case INVALID_VERSION -> throw new BadRequestException("Version cannot be empty");
      case READY ->
          Response.ok(new GraphInitStatusResponse(versionId, GraphLoadStatus.READY, null)).build();
      case IN_PROGRESS ->
          Response.status(Response.Status.ACCEPTED)
              .entity(new GraphInitStatusResponse(versionId, GraphLoadStatus.IN_PROGRESS, null))
              .build();
      case NOT_READY ->
          Response.status(Response.Status.NOT_FOUND)
              .entity(
                  new GraphInitStatusResponse(
                      versionId,
                      GraphLoadStatus.NOT_READY,
                      "Graph is not initialized for this version"))
              .build();
    };
  }

  public List<String> getAllVersions() {
    return graphDbService.getAvailableVersionNumbers().stream()
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  public Graph deleteGraph(String versionNumber) {
    throw new NotSupportedException("Deleting graph versions is not supported");
  }

  /** Get edges for a specific graph version with pagination to avoid memory issues */
  public List<Edge> getGraphEdges(
      String versionNumber,
      @Nullable List<String> landingZones,
      @Nullable List<String> platforms,
      @Nullable List<String> targets,
      EdgeDirection direction,
      int page,
      int size) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionNumber);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionNumber);
    }

    if (targets != null && !targets.isEmpty()) {
      return graphDbService
          .getGraphEdgesByTargets(versionNumber, page, size, targets, direction)
          .await()
          .indefinitely();
    }
    if (platforms != null && !platforms.isEmpty()) {
      return graphDbService
          .getGraphEdgesByPlatforms(versionNumber, page, size, platforms, direction)
          .await()
          .indefinitely();
    }
    if (landingZones != null && !landingZones.isEmpty()) {
      return graphDbService
          .getGraphEdgesByLandingZones(versionNumber, page, size, landingZones, direction)
          .await()
          .indefinitely();
    }

    return graphDbService
        .getGraphEdges(versionNumber, page, size, direction)
        .await()
        .indefinitely();
  }

  /**
   * Get edges connecting specific source and destination node IDs. This method is optimized for
   * connectivity queries.
   */
  public List<Edge> getGraphEdgesByNodeIds(
      String versionNumber,
      List<String> sourceNodeIds,
      List<String> destinationNodeIds,
      int page,
      int size) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionNumber);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionNumber);
    }

    return graphDbService
        .getGraphEdgesByNodeIds(versionNumber, page, size, sourceNodeIds, destinationNodeIds)
        .await()
        .indefinitely();
  }

  /** Get edges for a specific scope */
  public List<Edge> getGraphEdgesByScope(
      String versionNumber, @Nullable String scope, int page, int size) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionNumber);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionNumber);
    }
    if (scope == null || scope.isBlank()) {
      throw new BadRequestException("query parameter 'scope' is required");
    }

    Version version = dataEngine.getVersion(versionNumber).await().indefinitely();
    if (version == null
        || version.getScopesRepository() == null
        || !version.getScopesRepository().getData().containsKey(scope)) {
      throw new NotFoundException("Scope not found in version " + versionNumber + ": " + scope);
    }

    return graphDbService
        .getGraphEdgesByScope(versionNumber, page, size, scope)
        .await()
        .indefinitely();
  }

  /** Get nodes for a specific graph version with pagination */
  public List<Node> getGraphNodes(
      String versionNumber,
      @Nullable List<String> landingZones,
      @Nullable List<String> platforms,
      @Nullable List<String> targets,
      int page,
      int size) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionNumber);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionNumber);
    }

    if (targets != null && !targets.isEmpty()) {
      return graphDbService
          .getGraphNodesByTargets(versionNumber, page, size, targets)
          .await()
          .indefinitely();
    }
    if (platforms != null && !platforms.isEmpty()) {
      return graphDbService
          .getGraphNodesByPlatforms(versionNumber, page, size, platforms)
          .await()
          .indefinitely();
    }
    if (landingZones != null && !landingZones.isEmpty()) {
      return graphDbService
          .getGraphNodesByLandingZones(versionNumber, page, size, landingZones)
          .await()
          .indefinitely();
    }
    return graphDbService.getGraphNodes(versionNumber, page, size).await().indefinitely();
  }

  /**
   * Get pre-computed statistics for a graph version. Statistics are computed once at graph
   * initialization time and cached since the graph is immutable.
   */
  public GraphStatistics getGraphStatistics(String versionNumber) {
    GraphVersion graphVersion = graphCoreService.getVersion(versionNumber);
    if (graphVersion == null) {
      throw new NotFoundException("Version not found: " + versionNumber);
    }
    return graphVersion.toStatistics();
  }
}
