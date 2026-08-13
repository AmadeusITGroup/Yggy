package com.amadeus.aud.libraries.engine.graph.services.storage.remote;

import com.amadeus.aud.libraries.engine.graph.clients.EdgesByNodeIdsRequest;
import com.amadeus.aud.libraries.engine.graph.clients.GraphEngineClient;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@RemoteGraphEngine
@Singleton
public class GraphEngineRemoteService implements GraphEngineService {
  private static final Duration MAX_WAIT = Duration.ofMinutes(10);
  private static final String GET_GRAPH_EDGES = "get graph edges";
  private static final String GET_GRAPH_NODES = "get graph nodes";

  /** Maximum page size used when retrieving all edges for a scope (target, platform, etc.). */
  public static final int MAX_EDGE_PAGE_SIZE = 4_000_000;

  record RetryConfig(int maxRetries, Duration initialBackoff, Duration maxBackoff) {
    static final RetryConfig DEFAULT =
        new RetryConfig(3, Duration.ofMillis(500), Duration.ofSeconds(3));
    static final RetryConfig NO_RETRY =
        new RetryConfig(0, Duration.ofMillis(0), Duration.ofSeconds(0));
  }

  private final GraphEngineClient graphEngineClient;
  private final RetryConfig retryConfig;

  @Inject
  public GraphEngineRemoteService(@RestClient GraphEngineClient graphEngineClient) {
    this(graphEngineClient, RetryConfig.DEFAULT);
  }

  GraphEngineRemoteService(GraphEngineClient graphEngineClient, RetryConfig retryConfig) {
    this.graphEngineClient = graphEngineClient;
    this.retryConfig = retryConfig;
  }

  @Override
  public Uni<Void> init(String version) {
    return withStrategy("init graph", graphEngineClient.init(version))
        .onItem()
        .invoke(GraphEngineRemoteService::validateInitResponse)
        .replaceWithVoid();
  }

  private static void validateInitResponse(Response response) {
    try {
      int status = response.getStatus();
      if (status == Response.Status.OK.getStatusCode()
          || status == Response.Status.ACCEPTED.getStatusCode()) {
        return;
      }
      throw new IllegalStateException(
          "Unexpected graph init status from connectivity engine: " + status);
    } finally {
      response.close();
    }
  }

  @Override
  @SuppressWarnings("removal")
  @Deprecated(since = "Connectivity Engine", forRemoval = true)
  public @Nullable com.amadeus.aud.libraries.engine.graph.models.Graph getGraph(
      String versionNumber) {
    return null;
  }

  @Override
  public boolean isReady(String versionNumber) {
    try {
      Response response =
          withStrategy("get graph init status", graphEngineClient.getGraphInitStatus(versionNumber))
              .await()
              .indefinitely();
      try {
        int status = response.getStatus();
        if (status == Response.Status.OK.getStatusCode()) {
          return true;
        }
        if (status == Response.Status.ACCEPTED.getStatusCode()
            || status == Response.Status.NOT_FOUND.getStatusCode()) {
          return false;
        }
        Log.warnf(
            "Unexpected graph init status %s for version %s; treating as not ready",
            status, versionNumber);
        return false;
      } finally {
        response.close();
      }
    } catch (Exception e) {
      Log.debugf(e, "Graph version %s is not ready yet", versionNumber);
      return false;
    }
  }

  @Override
  public Set<String> getAvailableVersionNumbers() {
    try {
      List<String> versions = getAvailableVersions().await().indefinitely();
      return versions == null ? Set.of() : new LinkedHashSet<>(versions);
    } catch (Exception e) {
      Log.warnf(e, "Failed to fetch cached graph versions from connectivity engine");
      return Set.of();
    }
  }

  public Uni<List<Node>> getGraphNodesByLandingZones(
      String versionString, int page, int size, List<String> landingZones) {
    return withStrategy(
        GET_GRAPH_NODES,
        graphEngineClient.getGraphNodes(versionString, page, size, landingZones, null, null));
  }

  public Uni<List<Node>> getGraphNodesByPlatforms(
      String versionString, int page, int size, List<String> platforms) {
    return withStrategy(
        GET_GRAPH_NODES,
        graphEngineClient.getGraphNodes(versionString, page, size, null, platforms, null));
  }

  public Uni<List<Node>> getGraphNodesByTargets(
      String versionString, int page, int size, List<String> targets) {
    return withStrategy(
        GET_GRAPH_NODES,
        graphEngineClient.getGraphNodes(versionString, page, size, null, null, targets));
  }

  public Uni<List<Node>> getGraphNodes(String versionString, int page, int size) {
    return withStrategy(
        GET_GRAPH_NODES,
        graphEngineClient.getGraphNodes(versionString, page, size, null, null, null));
  }

  public Uni<List<Edge>> getGraphEdgesByLandingZones(
      String versionString,
      int page,
      int size,
      List<String> landingZones,
      EdgeDirection direction) {
    return withStrategy(
        GET_GRAPH_EDGES,
        graphEngineClient.getGraphEdges(
            versionString, page, size, landingZones, null, null, direction));
  }

  public Uni<List<Edge>> getGraphEdgesByPlatforms(
      String versionString, int page, int size, List<String> platforms, EdgeDirection direction) {
    return withStrategy(
        GET_GRAPH_EDGES,
        graphEngineClient.getGraphEdges(
            versionString, page, size, null, platforms, null, direction));
  }

  public Uni<List<Edge>> getGraphEdgesByTargets(
      String versionString, int page, int size, List<String> targets, EdgeDirection direction) {
    return withStrategy(
        GET_GRAPH_EDGES,
        graphEngineClient.getGraphEdges(versionString, page, size, null, null, targets, direction));
  }

  public Uni<List<Edge>> getGraphEdges(
      String versionString, int page, int size, EdgeDirection direction) {
    return withStrategy(
        GET_GRAPH_EDGES,
        graphEngineClient.getGraphEdges(versionString, page, size, null, null, null, direction));
  }

  public Uni<List<Edge>> getGraphEdgesByNodeIds(
      String versionString,
      int page,
      int size,
      List<String> sourceNodeIds,
      List<String> destinationNodeIds) {
    EdgesByNodeIdsRequest request = new EdgesByNodeIdsRequest(sourceNodeIds, destinationNodeIds);
    return withStrategy(
        "get graph edges by node ids",
        graphEngineClient.getGraphEdgesByNodeIds(versionString, page, size, request));
  }

  @Override
  public Uni<List<Edge>> getGraphEdgesByScope(
      String versionString, int page, int size, String scope) {
    return withStrategy(
        "get graph edges by scope",
        graphEngineClient.getGraphEdgesByScope(versionString, page, size, scope));
  }

  public Uni<GraphStatistics> getGraphStatistics(String versionString) {
    return withStrategy(
        "get graph statistics", graphEngineClient.getGraphStatistics(versionString));
  }

  public Uni<List<String>> getAvailableVersions() {
    return withStrategy("all graph versions", graphEngineClient.getAvailableVersions());
  }

  private <T> Uni<T> withStrategy(CharSequence title, Uni<T> uni) {
    var base =
        uni.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            .emitOn(Infrastructure.getDefaultWorkerPool());
    var withRetry =
        retryConfig.maxRetries() > 0
            ? base.onFailure()
                .retry()
                .withBackOff(retryConfig.initialBackoff(), retryConfig.maxBackoff())
                .atMost(retryConfig.maxRetries())
            : base;
    return withRetry
        .ifNoItem()
        .after(MAX_WAIT)
        .failWith(
            new TimeoutException("Timed out waiting for " + title + " for more than " + MAX_WAIT));
  }
}
