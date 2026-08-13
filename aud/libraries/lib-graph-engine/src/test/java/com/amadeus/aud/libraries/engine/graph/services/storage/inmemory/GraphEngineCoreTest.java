package com.amadeus.aud.libraries.engine.graph.services.storage.inmemory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.CaffeineCacheConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.StoredEdge;
import com.amadeus.aud.libraries.engine.graph.models.StoredNode;
import com.amadeus.aud.libraries.engine.graph.models.VersionData;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.Target;
import io.smallrye.mutiny.Uni;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.invocation.InvocationOnMock;

@Execution(ExecutionMode.SAME_THREAD)
class GraphEngineCoreTest {

  @Nested
  class GraphDataMapperTests {

    @Test
    void buildsVersionDataWithStoredGraphStatsAndScopeIndex() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      GraphDataMapper sut =
          new GraphDataMapper(fixture.graphService, new InMemoryDeduplicationPool());

      // Act
      VersionData result = sut.buildVersionData(fixture.version);

      // Assert
      assertThat(result.getNodes())
          .containsExactlyInAnyOrder(
              new StoredNode("src-id", "source-lz", "source-platform", "source-target"),
              new StoredNode(
                  "dst-id", "destination-lz", "destination-platform", "destination-target"));

      assertThat(result.getEdges())
          .containsExactly(
              new StoredEdge(
                  fixture.edge.getName(),
                  "dependency-id",
                  "service-id",
                  true,
                  "src-id",
                  "dst-id",
                  "source-scope",
                  "destination-scope",
                  "source-lz",
                  "source-platform",
                  "source-target",
                  "destination-lz",
                  "destination-platform",
                  "destination-target"));

      GraphStatistics statistics = result.getStatistics();
      assertThat(statistics.scopeNbLink()).containsEntry("dependency-scope", 1L);
      assertThat(statistics.scopeNbNode())
          .containsEntry("source-scope", 1L)
          .containsEntry("destination-scope", 1L);
      assertThat(statistics.globalNodesCount()).isEqualTo(2L);
      assertThat(statistics.globalLinksCount()).isOne();
    }

    @Test
    void buildsVersionDataWhenDependencyRepositoryIsMissingAndNodesHavePartialMetadata() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      doReturn(null).when(fixture.version).getDependencyInstancesRepository();
      doReturn(null).when(fixture.source).getLandingZone();
      doReturn(null).when(fixture.source).getDeployComponentInstance();
      doReturn(null).when(fixture.destination).getDeployComponentInstance();
      GraphDataMapper sut =
          new GraphDataMapper(fixture.graphService, new InMemoryDeduplicationPool());

      // Act
      VersionData result = sut.buildVersionData(fixture.version);

      // Assert
      assertThat(result.getNodes())
          .contains(new StoredNode("src-id", null, null, null))
          .contains(new StoredNode("dst-id", "destination-lz", null, "destination-target"));
      assertThat(result.getEdges())
          .containsExactly(
              new StoredEdge(
                  fixture.edge.getName(),
                  "dependency-id",
                  "service-id",
                  true,
                  "src-id",
                  "dst-id",
                  "source-scope",
                  "destination-scope",
                  null,
                  null,
                  null,
                  "destination-lz",
                  null,
                  "destination-target"));
    }

    @Test
    void skipsScopeIndexWhenEdgeReferencesUnknownNode() {
      // Arrange - minimal inline mocks instead of heavy GraphFixture
      Version version = mock(Version.class);
      GraphService graphService = mock(GraphService.class);
      doReturn("v1").when(version).getVersionNumber();

      ComponentInstance source = mock(ComponentInstance.class);
      ComponentInstance destination = mock(ComponentInstance.class);
      Scope sourceScope = mock(Scope.class);
      DependencyInstance dependencyInstance = mock(DependencyInstance.class);
      Service service = mock(Service.class);
      Edge edge = mock(Edge.class);
      doReturn("src-id").when(source).id();
      doReturn("dst-id").when(destination).id();
      doReturn(sourceScope).when(source).getScope();
      doReturn("source-scope").when(sourceScope).getName();
      doReturn("edge-id").when(edge).getId();
      doReturn("edge-name").when(edge).getName();
      doReturn(source).when(edge).getSource();
      doReturn(destination).when(edge).getDestination();
      doReturn(dependencyInstance).when(edge).getDependencyInstance();
      doReturn(service).when(edge).getService();
      doReturn("dependency-id").when(dependencyInstance).id();
      doReturn("service-id").when(service).id();

      doAnswer(
              (InvocationOnMock invocation) -> {
                GraphBuilder builder = invocation.getArgument(0);
                builder.putNode(source.id(), source);
                builder.putEdge(edge.getId(), edge);
                return null;
              })
          .when(graphService)
          .populateGraphFromDependencies(any(GraphBuilder.class), eq(version));

      GraphDataMapper sut = new GraphDataMapper(graphService, new InMemoryDeduplicationPool());

      // Act
      VersionData result = sut.buildVersionData(version);

      // Assert
      assertThat(result.getEdges()).hasSize(1);
    }

    @Test
    void computesUnknownScopeStatisticsWhenDependencyLookupIsMissing() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      doReturn(null).when(fixture.dependencyInstancesRepository).get("dependency-id");
      GraphDataMapper sut =
          new GraphDataMapper(fixture.graphService, new InMemoryDeduplicationPool());

      // Act
      VersionData result = sut.buildVersionData(fixture.version);

      // Assert
      assertThat(result.getStatistics().scopeNbLink()).containsEntry("unknown", 1L);
      assertThat(result.getStatistics().scopeNbNode())
          .containsEntry("source-scope", 1L)
          .containsEntry("destination-scope", 1L);
    }

    @Test
    void computesStatisticsWithUnknownDependencyScopeAndMissingDestinationNode() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      doReturn(null).when(fixture.version).getDependencyInstancesRepository();
      GraphBuilder builder = new GraphBuilder("version-1", true);
      builder.putNode(fixture.source.id(), fixture.source);
      builder.putEdge(fixture.edge.getId(), fixture.edge);

      // Act
      GraphStatistics result = GraphStatisticsComputer.compute(builder.build(), fixture.version);

      // Assert
      assertThat(result.scopeNbLink()).containsEntry("unknown", 1L);
      assertThat(result.scopeNbNode()).containsEntry("source-scope", 1L);
      assertThat(result.scopeNbNode()).doesNotContainKey("destination-scope");
    }
  }

  @Nested
  class InMemoryDeduplicationPoolTests {

    @Test
    void internsAndRebuildsCanonicalInstances() {
      // Arrange
      InMemoryDeduplicationPool sut = new InMemoryDeduplicationPool();
      StoredNode nodeA = new StoredNode("ci-1", "lz-1", "platform-1", "target-1");
      StoredNode nodeB = new StoredNode("ci-1", "lz-1", "platform-1", "target-1");
      StoredEdge edgeA =
          new StoredEdge(
              "edge-name",
              "dep-id",
              "svc-id",
              true,
              "src-id",
              "dst-id",
              "scope-a",
              "scope-b",
              "lz-1",
              "platform-1",
              "target-1",
              "lz-2",
              "platform-2",
              "target-2");
      StoredEdge edgeB =
          new StoredEdge(
              "edge-name",
              "dep-id",
              "svc-id",
              true,
              "src-id",
              "dst-id",
              "scope-a",
              "scope-b",
              "lz-1",
              "platform-1",
              "target-1",
              "lz-2",
              "platform-2",
              "target-2");
      String first = new String("shared");
      String second = new String("shared");

      // Act
      StoredNode canonicalNode = sut.internNode(nodeA);
      sut.internEdge(edgeA);
      String canonicalString = sut.internString(first);
      sut.rebuild(List.of(List.of(edgeB)), List.of(List.of(nodeB)));

      // Assert
      assertThat(canonicalNode).isEqualTo(nodeB);
      assertThat(sut.internNode(nodeB)).isSameAs(nodeB);
      assertThat(sut.internEdge(edgeB)).isSameAs(edgeB);
      assertThat(sut.internString(second)).isEqualTo(canonicalString);
      assertThat(sut.internString(null)).isNull();
    }
  }

  @Nested
  class InProcessGraphEngineServiceTests {

    @Test
    void cachesVersionDataAndServesFilteredQueries() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      CaffeineCacheConfig caffeineCacheConfig = mock(CaffeineCacheConfig.class);
      doReturn(Optional.of(caffeineCacheConfig)).when(audModelConfig).caffeineCache();
      doReturn(4L).when(caffeineCacheConfig).maximumGraphCacheSize();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act
      sut.init("version-1").await().indefinitely();

      // Assert
      assertThat(sut.isReady("version-1")).isTrue();
      assertThat(sut.getAvailableVersionNumbers()).containsExactly("version-1");
      assertThat(sut.getGraphStatistics("version-1").await().indefinitely().globalLinksCount())
          .isOne();
      assertThat(
              sut.getGraphEdgesByTargets(
                      "version-1", 0, 10, List.of("source-target"), EdgeDirection.OUTBOUND)
                  .await()
                  .indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphEdgesByPlatforms(
                      "version-1", 0, 10, List.of("destination-platform"), EdgeDirection.INBOUND)
                  .await()
                  .indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphEdgesByLandingZones(
                      "version-1", 0, 10, List.of("source-lz"), EdgeDirection.OUTBOUND)
                  .await()
                  .indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphEdgesByNodeIds("version-1", 0, 10, List.of("src-id"), List.of("dst-id"))
                  .await()
                  .indefinitely())
          .hasSize(1);
    }

    @Test
    void returnsEmptyResultsWhenVersionIsNotCached() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act / Assert
      assertThat(sut.isReady("missing")).isFalse();
      assertThat(sut.getAvailableVersionNumbers()).isEmpty();
    }

    @Test
    void supportsEmptyNodeIdFiltersAndPagedMisses() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);
      sut.init("version-1").await().indefinitely();

      // Act / Assert
      assertThat(sut.getGraphEdgesByNodeIds("version-1", 0, 10, null, null).await().indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphEdgesByTargets(
                      "version-1", 1, 10, List.of("source-target"), EdgeDirection.OUTBOUND)
                  .await()
                  .indefinitely())
          .isEmpty();
      assertThat(sut.getGraphStatistics("version-1").await().indefinitely().scopeNbLink())
          .containsEntry("dependency-scope", 1L);
    }

    @Test
    @SuppressWarnings("removal")
    void reportsNotReadyForCachedEmptyGraphsAndExposesDeprecatedGraphMethod() {
      // Arrange
      GraphService emptyGraphService = mock(GraphService.class);
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      Version version = mock(Version.class);
      doReturn("version-empty").when(version).getVersionNumber();
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(version)).when(dataEngine).getVersion("version-empty");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(emptyGraphService, dataEngine, audModelConfig);

      // Act
      sut.init("version-empty").await().indefinitely();

      // Assert
      assertThat(sut.isReady("version-empty")).isFalse();
      assertThat(sut.getGraph("version-empty")).isNull();
      assertThat(sut.getGraphStatistics("version-empty").await().indefinitely())
          .isEqualTo(new GraphStatistics(Map.of(), Map.of(), 0L, 0L));
    }

    @Test
    void filtersNodeIdsIndependentlyForSourceAndDestinationSets() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);
      sut.init("version-1").await().indefinitely();

      // Act / Assert - batch all assertions to minimize method calls
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> sourceOnly =
          sut.getGraphEdgesByNodeIds("version-1", 0, 10, List.of("src-id"), List.of())
              .await()
              .indefinitely();
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> destOnly =
          sut.getGraphEdgesByNodeIds("version-1", 0, 10, List.of(), List.of("dst-id"))
              .await()
              .indefinitely();
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> missing =
          sut.getGraphEdgesByNodeIds("version-1", 0, 10, List.of("missing"), List.of("dst-id"))
              .await()
              .indefinitely();

      assertThat(sourceOnly).hasSize(1);
      assertThat(destOnly).hasSize(1);
      assertThat(missing).isEmpty();
    }

    @Test
    void servesNodesWithAndWithoutNodeFilters() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);
      sut.init("version-1").await().indefinitely();

      // Act / Assert
      assertThat(sut.getGraphNodes("version-1", 0, 10).await().indefinitely()).hasSize(2);
      assertThat(
              sut.getGraphNodesByTargets("version-1", 0, 10, List.of("source-target"))
                  .await()
                  .indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphNodesByPlatforms("version-1", 0, 10, List.of("destination-platform"))
                  .await()
                  .indefinitely())
          .hasSize(1);
      assertThat(
              sut.getGraphNodesByLandingZones("version-1", 0, 10, List.of("missing-lz"))
                  .await()
                  .indefinitely())
          .isEmpty();
      assertThat(sut.getGraphEdges("version-1", 0, 10, EdgeDirection.ANY).await().indefinitely())
          .hasSize(1);
    }

    @Test
    void initializesAndServesMultipleVersionsWithConfiguredCacheSize() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      Version secondVersion = mock(Version.class);
      doReturn("version-2").when(secondVersion).getVersionNumber();
      doReturn(fixture.dependencyInstancesRepository)
          .when(secondVersion)
          .getDependencyInstancesRepository();
      doAnswer(
              (InvocationOnMock invocation) -> {
                GraphBuilder builder = invocation.getArgument(0);
                builder.putNode(fixture.source.id(), fixture.source);
                builder.putNode(fixture.destination.id(), fixture.destination);
                builder.putEdge(fixture.edge.getId(), fixture.edge);
                return null;
              })
          .when(fixture.graphService)
          .populateGraphFromDependencies(any(GraphBuilder.class), eq(secondVersion));

      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      CaffeineCacheConfig caffeineCacheConfig = mock(CaffeineCacheConfig.class);
      doReturn(Optional.of(caffeineCacheConfig)).when(audModelConfig).caffeineCache();
      doReturn(1L).when(caffeineCacheConfig).maximumGraphCacheSize();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      doReturn(Uni.createFrom().item(secondVersion)).when(dataEngine).getVersion("version-2");

      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act
      sut.init("version-1").await().indefinitely();
      sut.init("version-2").await().indefinitely();

      // Assert
      assertThat(sut.getAvailableVersionNumbers()).contains("version-2");
      assertThat(sut.isReady("version-2")).isTrue();
      assertThat(sut.getGraphEdges("version-2", 0, 10, EdgeDirection.ANY).await().indefinitely())
          .hasSize(1);
    }

    @Test
    void lazilyBuildsAndCachesVersionDataWhenQueryingWithoutInit() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act
      assertThat(sut.isReady("version-1")).isFalse();
      assertThat(sut.getGraphNodes("version-1", 0, 10).await().indefinitely()).hasSize(2);

      // Assert
      assertThat(sut.getAvailableVersionNumbers()).contains("version-1");
      assertThat(sut.isReady("version-1")).isTrue();
      verify(fixture.graphService, times(1))
          .populateGraphFromDependencies(any(GraphBuilder.class), eq(fixture.version));
    }

    @Test
    void repeatedInitForSameVersionBuildsGraphOnlyOnce() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act
      sut.init("version-1").await().indefinitely();
      sut.init("version-1").await().indefinitely();
      sut.getGraphEdges("version-1", 0, 10, EdgeDirection.OUTBOUND).await().indefinitely();

      // Assert
      verify(fixture.graphService, times(1))
          .populateGraphFromDependencies(any(GraphBuilder.class), eq(fixture.version));
    }

    @Test
    void propagatesVersionLookupFailuresFromInitAndQueries() {
      // Arrange
      GraphFixture fixture = new GraphFixture();
      DataEngine dataEngine = mock(DataEngine.class);
      AudModelConfig audModelConfig = mock(AudModelConfig.class);
      RuntimeException boom = new RuntimeException("boom");
      doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
      doReturn(Uni.createFrom().failure(boom)).when(dataEngine).getVersion("missing-version");
      InProcessGraphEngineService sut =
          new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

      // Act / Assert
      assertThatThrownBy(() -> sut.init("missing-version").await().indefinitely())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("boom");
      assertThatThrownBy(() -> sut.getGraphStatistics("missing-version").await().indefinitely())
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("boom");
    }
  }

  private static final class GraphFixture {
    private final Version version = mock(Version.class);
    private final GraphService graphService = mock(GraphService.class);
    private final DependencyInstancesRepository dependencyInstancesRepository =
        mock(DependencyInstancesRepository.class);
    private final DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    private final Service service = mock(Service.class);
    private final ComponentInstance source =
        mockComponentInstance(
            "src-id",
            "source-component-instance",
            "source-component-name",
            "source-platform",
            "source-scope",
            "source-lz",
            "source-target");
    private final ComponentInstance destination =
        mockComponentInstance(
            "dst-id",
            "destination-component-instance",
            "destination-component-name",
            "destination-platform",
            "destination-scope",
            "destination-lz",
            "destination-target");
    private final Edge edge;

    private GraphFixture() {
      doReturn("version-1").when(version).getVersionNumber();
      doReturn(dependencyInstancesRepository).when(version).getDependencyInstancesRepository();

      Scope dependencyScope = mockScope("dependency-scope");
      doReturn("dependency-id").when(dependencyInstance).id();
      doReturn("dependency-name").when(dependencyInstance).getName();
      doReturn("dependency-scope").when(dependencyInstance).getScopeName();
      doReturn(dependencyScope).when(dependencyInstance).getScope();

      ConcurrentMap<String, DependencyInstance> dependencyData = new ConcurrentHashMap<>();
      dependencyData.put("dependency-id", dependencyInstance);
      doReturn(dependencyData).when(dependencyInstancesRepository).getData();
      doReturn(dependencyInstance).when(dependencyInstancesRepository).get("dependency-id");

      doReturn("service-id").when(service).id();

      edge = new Edge(source, destination, dependencyInstance, service, true, false);

      doAnswer(
              (InvocationOnMock invocation) -> {
                GraphBuilder builder = invocation.getArgument(0);
                builder.putNode(source.id(), source);
                builder.putNode(destination.id(), destination);
                builder.putEdge(edge.getId(), edge);
                return null;
              })
          .when(graphService)
          .populateGraphFromDependencies(any(GraphBuilder.class), eq(version));
    }
  }

  private static ComponentInstance mockComponentInstance(
      String id,
      String instanceName,
      String componentName,
      String deployName,
      String scopeName,
      String landingZoneName,
      String targetName) {
    ComponentInstance componentInstance = mock(ComponentInstance.class);
    ComponentInstance deployComponentInstance = mock(ComponentInstance.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target target = mock(Target.class);
    Scope scope = mockScope(scopeName);

    // Batch all doReturn calls together to minimize method call overhead
    doReturn(id).when(componentInstance).id();
    doReturn(instanceName).when(componentInstance).getName();
    doReturn(scopeName).when(componentInstance).getScopeName();
    doReturn(scope).when(componentInstance).getScope();
    doReturn(landingZone).when(componentInstance).getLandingZone();
    doReturn(deployComponentInstance).when(componentInstance).getDeployComponentInstance();
    doReturn(componentName).when(deployComponentInstance).getName();
    doReturn(deployName).when(deployComponentInstance).getName();
    doReturn(landingZoneName).when(landingZone).getName();
    doReturn(targetName).when(landingZone).getTargetName();
    doReturn(target).when(landingZone).getTarget();

    doReturn(targetName).when(target).getName();

    return componentInstance;
  }

  private static Scope mockScope(String name) {
    Scope scope = mock(Scope.class);
    doReturn(name).when(scope).getName();
    return scope;
  }
}
