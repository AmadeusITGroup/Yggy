package com.amadeus.aud.libraries.engine.graph.services.storage.inmemory;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.CaffeineCacheConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.StoredEdge;
import com.amadeus.aud.libraries.engine.graph.models.StoredEdgeFilters;
import com.amadeus.aud.libraries.engine.graph.models.StoredNode;
import com.amadeus.aud.libraries.engine.graph.models.StoredNodeFilters;
import com.amadeus.aud.libraries.engine.graph.models.VersionData;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.google.common.collect.ImmutableSet;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * In-process implementation of {@link GraphEngineService}. Builds the graph in-memory using {@link
 * GraphService}, converts it to denormalized {@link StoredEdge}/{@link StoredNode} records, and
 * discards the original {@code Graph} object. Identical records across versions are deduplicated
 * via an {@link InMemoryDeduplicationPool} (Flyweight pattern), so that multiple cached versions
 * share the same object references for unchanged edges — drastically reducing heap usage.
 *
 * <p>Phase 2 will replace this with a remote implementation backed by the connectivity-engine
 * microservice.
 */
@InMemoryGraphEngine
@Singleton
public class InProcessGraphEngineService implements GraphEngineService {

  private final DataEngine dataEngine;
  private final GraphDataMapper dataMapper;

  private final Cache<String, VersionData> store;
  private final InMemoryDeduplicationPool deduplicationPool = new InMemoryDeduplicationPool();

  @Inject
  public InProcessGraphEngineService(
      GraphService graphService, DataEngine dataEngine, AudModelConfig audModelConfig) {
    this.dataEngine = dataEngine;
    this.dataMapper = new GraphDataMapper(graphService, deduplicationPool);

    long maximumSize =
        audModelConfig.caffeineCache().map(CaffeineCacheConfig::maximumGraphCacheSize).orElse(8L);
    this.store =
        Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .removalListener(this::onVersionEvicted)
            .build();
    Log.infof("InProcessGraphEngineService initialized with max versions: %d", maximumSize);
  }

  private void onVersionEvicted(String version, VersionData data, RemovalCause cause) {
    if (data != null) {
      Log.infof(
          "Evicted graph version %s (cause: %s, nodes: %d, edges: %d)",
          version, cause, data.getNodes().size(), data.getEdges().size());
    }
    rebuildDeduplicationPool();
  }

  @Override
  public Uni<Void> init(String versionNumber) {
    return dataEngine
        .getVersion(versionNumber)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .invoke(this::getOrBuildVersionData)
        .replaceWithVoid();
  }

  @Override
  @SuppressWarnings("removal")
  @Deprecated(since = "Connectivity Engine", forRemoval = true)
  public @Nullable Graph getGraph(String versionNumber) {
    // Deprecated — all consumers should use getGraphEdgesBy* or getGraphStatistics.
    Log.warnf("getGraph() called for version %s — this method is deprecated", versionNumber);
    return null;
  }

  @Override
  public boolean isReady(String versionNumber) {
    VersionData data = store.getIfPresent(versionNumber);
    return data != null;
  }

  @Override
  public Set<String> getAvailableVersionNumbers() {
    return ImmutableSet.copyOf(store.asMap().keySet());
  }

  @Override
  public Uni<List<Edge>> getGraphEdges(
      String versionString, int page, int size, EdgeDirection direction) {
    return getFilteredEdges(versionString, page, size, edge -> true);
  }

  @Override
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByTargets(
      String versionString, int page, int size, List<String> targets, EdgeDirection direction) {
    return getFilteredEdges(
        versionString,
        page,
        size,
        edge -> StoredEdgeFilters.matchesTargets(edge, targets, direction));
  }

  @Override
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByPlatforms(
      String versionString, int page, int size, List<String> platforms, EdgeDirection direction) {
    return getFilteredEdges(
        versionString,
        page,
        size,
        edge -> StoredEdgeFilters.matchesPlatforms(edge, platforms, direction));
  }

  @Override
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
      getGraphEdgesByLandingZones(
          String versionString,
          int page,
          int size,
          List<String> landingZones,
          EdgeDirection direction) {
    return getFilteredEdges(
        versionString,
        page,
        size,
        edge -> StoredEdgeFilters.matchesLandingZones(edge, landingZones, direction));
  }

  @Override
  public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByNodeIds(
      String versionString,
      int page,
      int size,
      List<String> sourceNodeIds,
      List<String> destinationNodeIds) {
    ImmutableSet<String> srcSet =
        sourceNodeIds != null ? ImmutableSet.copyOf(sourceNodeIds) : ImmutableSet.of();
    ImmutableSet<String> dstSet =
        destinationNodeIds != null ? ImmutableSet.copyOf(destinationNodeIds) : ImmutableSet.of();
    return getFilteredEdges(
        versionString,
        page,
        size,
        (StoredEdge edge) -> {
          boolean srcMatch = srcSet.isEmpty() || srcSet.contains(edge.sourceNodeId());
          boolean dstMatch = dstSet.isEmpty() || dstSet.contains(edge.destinationNodeId());
          return srcMatch && dstMatch;
        });
  }

  @Override
  public Uni<List<Edge>> getGraphEdgesByScope(
      String versionString, int page, int size, String scope) {
    return getFilteredEdges(
        versionString, page, size, edge -> StoredEdgeFilters.matchesScope(edge, scope));
  }

  @Override
  public Uni<List<Node>> getGraphNodes(String versionString, int page, int size) {
    return resolveVersion(versionString)
        .onItem()
        .transform(
            (Version version) -> {
              VersionData data = getOrBuildVersionData(version);
              return com.amadeus.aud.libraries.engine.graph.models.StoredNodeFilters.filterAndPage(
                  data.getNodes(), page, size, node -> true);
            });
  }

  @Override
  public Uni<List<Node>> getGraphNodesByTargets(
      String versionString, int page, int size, List<String> targets) {
    return getFilteredNodes(
        versionString, page, size, node -> StoredNodeFilters.matchesTargets(node, targets));
  }

  @Override
  public Uni<List<Node>> getGraphNodesByPlatforms(
      String versionString, int page, int size, List<String> platforms) {
    return getFilteredNodes(
        versionString, page, size, node -> StoredNodeFilters.matchesPlatforms(node, platforms));
  }

  @Override
  public Uni<List<Node>> getGraphNodesByLandingZones(
      String versionString, int page, int size, List<String> landingZones) {
    return getFilteredNodes(
        versionString,
        page,
        size,
        node -> StoredNodeFilters.matchesLandingZones(node, landingZones));
  }

  @Override
  public Uni<GraphStatistics> getGraphStatistics(String versionString) {
    return resolveVersion(versionString)
        .onItem()
        .transform(
            (Version version) -> {
              VersionData data = getOrBuildVersionData(version);
              return data.getStatistics();
            });
  }

  // ---- Internal helpers ----

  private Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getFilteredEdges(
      String versionString, int page, int size, Predicate<StoredEdge> filter) {
    return resolveVersion(versionString)
        .onItem()
        .transform(
            (Version version) -> {
              VersionData data = getOrBuildVersionData(version);
              return StoredEdgeFilters.filterAndPage(data.getEdges(), page, size, filter);
            });
  }

  private Uni<List<Node>> getFilteredNodes(
      String versionString, int page, int size, Predicate<StoredNode> filter) {
    return resolveVersion(versionString)
        .onItem()
        .transform(
            (Version version) -> {
              VersionData data = getOrBuildVersionData(version);
              return StoredNodeFilters.filterAndPage(data.getNodes(), page, size, filter);
            });
  }

  private Uni<Version> resolveVersion(String versionString) {
    return dataEngine
        .getVersion(versionString)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private VersionData getOrBuildVersionData(Version version) {
    return store.get(
        version.getVersionNumber(), (String unused) -> dataMapper.buildVersionData(version));
  }

  private void rebuildDeduplicationPool() {
    Collection<VersionData> snapshot = store.asMap().values();
    deduplicationPool.rebuild(
        snapshot.stream().map(VersionData::getEdges).toList(),
        snapshot.stream().map(VersionData::getNodes).toList());
  }
}
