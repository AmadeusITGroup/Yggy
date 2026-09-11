package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import io.smallrye.mutiny.Uni;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PostgresGraphEngineServiceTest {

  private PostgresGraphRepository graphRepository;
  private PostgresNodeRepository nodeRepository;
  private PostgresEdgeRepository edgeRepository;
  private DataEngine dataEngine;
  private GraphService graphService;
  private PostgresGraphEngineService sut;

  @BeforeEach
  void setUp() {
    graphRepository = mock(PostgresGraphRepository.class);
    nodeRepository = mock(PostgresNodeRepository.class);
    edgeRepository = mock(PostgresEdgeRepository.class);
    dataEngine = mock(DataEngine.class);
    graphService = mock(GraphService.class);
    sut =
        new PostgresGraphEngineService(
            graphRepository, nodeRepository, edgeRepository, dataEngine, graphService);
  }

  @Test
  void readApisReturnEmptyWhenVersionDoesNotExist() {
    doReturn(Optional.empty()).when(graphRepository).findByVersion("1.0.0");

    assertThat(sut.getGraphNodes("1.0.0", 0, 10).await().indefinitely()).isEmpty();
    assertThat(sut.getGraphEdges("1.0.0", 0, 10, EdgeDirection.ANY).await().indefinitely())
        .isEmpty();
    assertThat(
            sut.getGraphEdgesByNodeIds("1.0.0", 0, 10, List.of("src"), List.of("dst"))
                .await()
                .indefinitely())
        .isEmpty();

    verify(nodeRepository, never()).findByVersionRank(anyInt(), anyInt(), anyInt());
    verify(edgeRepository, never()).findByVersionRank(anyInt(), anyInt(), anyInt());
  }

  @Test
  void readApisDelegateToRepositoriesWhenVersionExists() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("1.0.0");
    graph.setVersionRank(3);
    doReturn(Optional.of(graph)).when(graphRepository).findByVersion("1.0.0");

    List<Node> nodes = List.of(new Node("src", "lz", "platform", "target"));
    doReturn(nodes).when(nodeRepository).findByVersionRank(3, 1, 5);
    doReturn(nodes).when(nodeRepository).findByVersionRankWithTargets(3, 0, 10, List.of("target"));
    doReturn(nodes)
        .when(nodeRepository)
        .findByVersionRankWithPlatforms(3, 0, 10, List.of("platform"));
    doReturn(nodes).when(nodeRepository).findByVersionRankWithLandingZones(3, 0, 10, List.of("lz"));

    List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> edges =
        List.of(
            new com.amadeus.aud.libraries.engine.graph.models.dto.Edge(
                "e", "d", "s", true, "src", "dst"));
    doReturn(edges).when(edgeRepository).findByVersionRank(3, 0, 10);
    doReturn(edges)
        .when(edgeRepository)
        .findByVersionRankWithTargets(3, 0, 10, List.of("target"), EdgeDirection.OUTBOUND);
    doReturn(edges)
        .when(edgeRepository)
        .findByVersionRankWithPlatforms(3, 0, 10, List.of("platform"), EdgeDirection.INBOUND);
    doReturn(edges)
        .when(edgeRepository)
        .findByVersionRankWithLandingZones(3, 0, 10, List.of("lz"), EdgeDirection.ANY);
    doReturn(edges)
        .when(edgeRepository)
        .findByVersionRankWithNodeIds(3, List.of("src"), List.of("dst"), 0, 10);

    assertThat(sut.getGraphNodes("1.0.0", 1, 5).await().indefinitely()).isEqualTo(nodes);
    assertThat(sut.getGraphNodesByTargets("1.0.0", 0, 10, List.of("target")).await().indefinitely())
        .isEqualTo(nodes);
    assertThat(
            sut.getGraphNodesByPlatforms("1.0.0", 0, 10, List.of("platform"))
                .await()
                .indefinitely())
        .isEqualTo(nodes);
    assertThat(
            sut.getGraphNodesByLandingZones("1.0.0", 0, 10, List.of("lz")).await().indefinitely())
        .isEqualTo(nodes);

    assertThat(sut.getGraphEdges("1.0.0", 0, 10, EdgeDirection.ANY).await().indefinitely())
        .containsExactlyElementsOf(edges);
    assertThat(
            sut.getGraphEdgesByTargets("1.0.0", 0, 10, List.of("target"), EdgeDirection.OUTBOUND)
                .await()
                .indefinitely())
        .containsExactlyElementsOf(edges);
    assertThat(
            sut.getGraphEdgesByPlatforms("1.0.0", 0, 10, List.of("platform"), EdgeDirection.INBOUND)
                .await()
                .indefinitely())
        .containsExactlyElementsOf(edges);
    assertThat(
            sut.getGraphEdgesByLandingZones("1.0.0", 0, 10, List.of("lz"), EdgeDirection.ANY)
                .await()
                .indefinitely())
        .containsExactlyElementsOf(edges);
    assertThat(
            sut.getGraphEdgesByNodeIds("1.0.0", 0, 10, List.of("src"), List.of("dst"))
                .await()
                .indefinitely())
        .isEqualTo(edges);
  }

  @Test
  void helperApisExposeRepositoryData() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("1.1.1");
    graph.setVersionRank(4);
    graph.setCreatedAt(LocalDateTime.now());

    doReturn(Optional.of(graph)).when(graphRepository).findByVersion("1.1.1");
    doReturn(Optional.of(graph)).when(graphRepository).findLatestVersion();
    doReturn(new LinkedHashSet<>(List.of("1.0.0", "1.1.1")))
        .when(graphRepository)
        .findAllVersions();
    doReturn(12L).when(nodeRepository).countByVersionRank(4);
    doReturn(7L).when(edgeRepository).countByVersionRank(4);
    doReturn(2L).when(graphRepository).count();

    assertThat(sut.getLatestGraph("latest")).hasValue(graph);
    assertThat(sut.getLatestGraph("1.1.1")).hasValue(graph);
    assertThat(sut.getAvailableVersionNumbers()).containsExactly("1.0.0", "1.1.1");
    assertThat(sut.countNodes(graph)).isEqualTo(12L);
    assertThat(sut.countEdges(graph)).isEqualTo(7L);
    assertThat(sut.countGraphs()).isEqualTo(2L);
    assertThat(sut.loadGraph("1.1.1")).isSameAs(graph);
  }

  @Test
  void statusAndStatisticsApisExposeRepositoryData() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("1.0.0");

    GraphStatistics statistics =
        new GraphStatistics(Map.of("scope-a", 3L), Map.of("scope-a", 2L), 7L, 9L);
    Version version = mock(Version.class);

    doReturn(Optional.empty())
        .doReturn(Optional.of(graph))
        .when(graphRepository)
        .findByVersion("1.0.0");
    doReturn("1.0.0").when(version).getVersionNumber();
    doReturn(Uni.createFrom().item(version)).when(dataEngine).getVersion("latest");
    doReturn(Optional.of(statistics)).when(graphRepository).findStatisticsByVersion("1.0.0");

    assertThat(sut.isReady("1.0.0")).isFalse();
    assertThat(sut.isReady("1.0.0")).isTrue();
    assertThat(sut.getGraphStatistics("latest").await().indefinitely()).isEqualTo(statistics);

    verify(graphRepository).findStatisticsByVersion("1.0.0");
  }

  @Test
  void createAndGetOrCreateGraphEntityFollowExpectedFlow() {
    doNothing().when(graphRepository).persist(any(GraphEntity.class));
    doNothing().when(graphRepository).flush();
    doReturn(Optional.empty()).when(graphRepository).findByVersion("2.0.0");

    GraphEntity created = sut.createGraphEntity("2.0.0", 10);
    assertThat(created.getVersion()).isEqualTo("2.0.0");
    assertThat(created.getVersionRank()).isEqualTo(10);

    GraphEntity fromGetOrCreate = sut.getOrCreateGraphEntity("2.0.0", 11);
    assertThat(fromGetOrCreate.getVersion()).isEqualTo("2.0.0");
    assertThat(fromGetOrCreate.getVersionRank()).isEqualTo(11);

    GraphEntity existing = new GraphEntity();
    existing.setVersion("2.0.0");
    existing.setVersionRank(1);
    doReturn(Optional.of(existing)).when(graphRepository).findByVersion("2.0.0");

    PostgresGraphEngineService spy = spy(sut);
    doNothing().when(spy).cleanupGraphData(existing);

    GraphEntity reused = spy.getOrCreateGraphEntity("2.0.0", 99);
    assertThat(reused).isSameAs(existing);
    assertThat(reused.getVersionRank()).isEqualTo(99);
    verify(spy).cleanupGraphData(existing);
  }

  @Test
  void cleanupGraphDataUpdatesRepositoriesAndWrapsFailures() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("3.0.0");
    graph.setVersionRank(12);

    sut.cleanupGraphData(graph);
    verify(edgeRepository).clearEdgesForVersionRank(12);
    verify(nodeRepository).clearNodesForVersionRank(12);
    verify(edgeRepository).reopenEdgesClosedAtRank(12);
    verify(nodeRepository).reopenNodesClosedAtRank(12);

    doReturn(Optional.of(graph)).when(graphRepository).findByVersion("3.0.0");
  }

  @Test
  void deleteGraphReturnsDeletedMetadata() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("4.0.0");
    graph.setVersionRank(8);
    graph.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));

    doReturn(5L).when(nodeRepository).countByVersionRank(8);
    doReturn(6L).when(edgeRepository).countByVersionRank(8);

    com.amadeus.aud.libraries.engine.graph.models.dto.Graph deleted = sut.deleteGraph(graph);

    assertThat(deleted.version()).isEqualTo("4.0.0");
    assertThat(deleted.verticesCount()).isEqualTo(5L);
    assertThat(deleted.edgesCount()).isEqualTo(6L);
    verify(graphRepository).delete(graph);
  }

  @Test
  void saveGraphValidatesInputAndSupportsFullAndIncrementalFlows() {
    assertThatThrownBy(() -> sut.saveGraph(null)).isInstanceOf(IllegalArgumentException.class);

    Graph emptyGraph = new GraphBuilder("5.0.0").build();
    sut.saveGraph(emptyGraph);

    Graph graph = buildGraph("5.0.1");
    Version version = mock(Version.class);
    doReturn(null).when(version).getDependencyInstancesRepository();
    doReturn(Uni.createFrom().item(version)).when(dataEngine).getVersion("5.0.1");

    doReturn(Optional.empty()).when(graphRepository).findByMaxVersionRank();
    doReturn(1).when(graphRepository).computeNextVersionRank();
    doReturn(Optional.empty()).when(graphRepository).findByVersion("5.0.1");

    doNothing().when(graphRepository).persist(any(GraphEntity.class));
    doNothing().when(graphRepository).flush();

    Map<ComponentInstance, Long> idMap =
        Map.of(graph.getNodes().get("src"), 1L, graph.getNodes().get("dst"), 2L);
    doReturn(idMap).when(nodeRepository).batchInsertNodesCopy(eq(1), anyCollection());
    doReturn(2L).when(nodeRepository).countByVersionRank(1);
    doReturn(1L).when(edgeRepository).countByVersionRank(1);

    sut.saveGraph(graph);

    GraphEntity previous = new GraphEntity();
    previous.setVersion("5.0.0");
    previous.setVersionRank(1);
    doReturn(Optional.of(previous)).when(graphRepository).findByMaxVersionRank();
    doReturn(2).when(graphRepository).computeNextVersionRank();
    doReturn(Optional.empty()).when(graphRepository).findByVersion("5.0.1");
    doReturn(Set.of()).when(nodeRepository).findActiveNodeIds(1);
    doReturn(Set.of()).when(edgeRepository).findActiveEdgeKeys(1);
    doReturn(idMap).when(nodeRepository).batchInsertNodesCopy(eq(2), anyCollection());
    doReturn(Map.of()).when(nodeRepository).loadActiveNodeIdMap(2);
    doReturn(2L).when(nodeRepository).countByVersionRank(2);
    doReturn(1L).when(edgeRepository).countByVersionRank(2);

    sut.saveGraph(graph);

    verify(edgeRepository)
        .batchInsertEdgesCopyBinary(
            eq(1),
            anyCollection(),
            org.mockito.ArgumentMatchers.<Map<ComponentInstance, Long>>any());
    verify(edgeRepository)
        .batchInsertEdgesCopyBinary(
            eq(2),
            anyCollection(),
            org.mockito.ArgumentMatchers.<Map<ComponentInstance, Long>>any());
  }

  @Test
  void cleanupGraphDataWrapsRuntimeExceptionsAsGraphSaveException() {
    GraphEntity graph = new GraphEntity();
    graph.setVersion("6.0.0");
    graph.setVersionRank(20);

    doThrow(new RuntimeException("fail")).when(edgeRepository).clearEdgesForVersionRank(20);

    assertThatThrownBy(() -> sut.cleanupGraphData(graph)).isInstanceOf(GraphSaveException.class);
  }

  private static Graph buildGraph(String version) {
    ComponentInstance src = mockComponent("src", "source", "scope-a");
    ComponentInstance dst = mockComponent("dst", "destination", "scope-b");
    DependencyInstance dep = mockDependency("dep-1", "dep-name", "dep-scope");
    Service svc = mockService("svc-1");

    GraphBuilder builder = new GraphBuilder(version);
    builder.putNode(src.id(), src);
    builder.putNode(dst.id(), dst);
    builder.putEdge("edge-1", new Edge(src, dst, dep, svc, true));
    return builder.build();
  }

  private static ComponentInstance mockComponent(String id, String name, String scopeName) {
    ComponentInstance component = mock(ComponentInstance.class);
    Scope scope = mock(Scope.class);
    doReturn(id).when(component).id();
    doReturn(name).when(component).getName();
    doReturn(scopeName).when(component).getScopeName();
    doReturn(scope).when(component).getScope();
    doReturn(scopeName).when(scope).getName();
    return component;
  }

  private static DependencyInstance mockDependency(String id, String name, String scopeName) {
    DependencyInstance dependency = mock(DependencyInstance.class);
    Scope scope = mock(Scope.class);
    doReturn(id).when(dependency).id();
    doReturn(name).when(dependency).getName();
    doReturn(scopeName).when(dependency).getScopeName();
    doReturn(scope).when(dependency).getScope();
    doReturn(scopeName).when(scope).getName();
    return dependency;
  }

  private static Service mockService(String id) {
    Service service = mock(Service.class);
    doReturn(id).when(service).id();
    return service;
  }
}
