package com.amadeus.aud.libraries.engine.graph.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphServiceContractsTest {

  @Nested
  class GraphServiceTests {

    @Test
    void returnsOnlyLinksMatchingTheRequestedScope() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1", true);
      ComponentInstance source = mockComponentInstance("src", "source", "scope-a");
      ComponentInstance destination = mockComponentInstance("dst", "destination", "scope-b");
      Edge matchingEdge = newEdge(source, destination, "scope-c");
      builder.putNode(source.id(), source);
      builder.putNode(destination.id(), destination);
      builder.putEdge(matchingEdge.getId(), matchingEdge);
      Graph graph = builder.build();

      // Act
      List<Link> result = GraphService.linksLimitedToScope(graph, "scope-b");

      // Assert
      assertThat(result).containsExactly(matchingEdge.getLink());
      assertThat(GraphService.linksLimitedToScope(graph, "missing")).isEmpty();
    }

    @Test
    void ignoresEdgesWithoutLinksAndMatchesSourceOrDependencyScopes() {
      // Arrange - inline all mocks to avoid helper method overhead
      GraphBuilder builder = new GraphBuilder("v1", true);

      ComponentInstance source = mock(ComponentInstance.class);
      Scope sourceScope = mock(Scope.class);
      doReturn("src").when(source).id();
      doReturn("source").when(source).getName();
      doReturn(sourceScope).when(source).getScope();
      doReturn("scope-a").when(sourceScope).getName();

      ComponentInstance destination = mock(ComponentInstance.class);
      Scope destScope = mock(Scope.class);
      doReturn("dst").when(destination).id();
      doReturn("destination").when(destination).getName();
      doReturn(destScope).when(destination).getScope();
      doReturn("scope-b").when(destScope).getName();

      // Create edges with batched mock setup
      DependencyInstance srcScopeDep = mock(DependencyInstance.class);
      Scope srcScopeDepScope = mock(Scope.class);
      Service service1 = mock(Service.class);
      doReturn("dep-id-scope-c").when(srcScopeDep).id();
      doReturn("dependency-name").when(srcScopeDep).getName();
      doReturn(srcScopeDepScope).when(srcScopeDep).getScope();
      doReturn("scope-c").when(srcScopeDepScope).getName();
      doReturn("service-id").when(service1).id();
      doReturn("service-name").when(service1).getName();
      doReturn("443").when(service1).getPorts();
      doReturn(V1ServiceSpec.ProtocolEnum.TCP).when(service1).getProtocol();
      Edge sourceScopeEdge = new Edge(source, destination, srcScopeDep, service1, true, true);

      DependencyInstance depScopeDep = mock(DependencyInstance.class);
      Scope depScopeDepScope = mock(Scope.class);
      Service service2 = mock(Service.class);
      doReturn("dep-id-scope-z").when(depScopeDep).id();
      doReturn("dependency-name").when(depScopeDep).getName();
      doReturn(depScopeDepScope).when(depScopeDep).getScope();
      doReturn("scope-z").when(depScopeDepScope).getName();
      doReturn("service-id").when(service2).id();
      doReturn("service-name").when(service2).getName();
      doReturn("443").when(service2).getPorts();
      doReturn(V1ServiceSpec.ProtocolEnum.TCP).when(service2).getProtocol();
      Edge dependencyScopeEdge = new Edge(source, destination, depScopeDep, service2, true, true);

      DependencyInstance nolinkDep = mock(DependencyInstance.class);
      Scope nolinkScope = mock(Scope.class);
      Service nolinkService = mock(Service.class);
      doReturn("dep-id-scope-a").when(nolinkDep).id();
      doReturn("dependency-name").when(nolinkDep).getName();
      doReturn(nolinkScope).when(nolinkDep).getScope();
      doReturn("scope-a").when(nolinkScope).getName();
      doReturn("service-id").when(nolinkService).id();
      doReturn("service-name").when(nolinkService).getName();
      doReturn("443").when(nolinkService).getPorts();
      doReturn(V1ServiceSpec.ProtocolEnum.TCP).when(nolinkService).getProtocol();
      Edge edgeWithoutLink = new Edge(source, destination, nolinkDep, nolinkService, true, false);

      builder.putNode(source.id(), source);
      builder.putNode(destination.id(), destination);
      builder.putEdge("1", sourceScopeEdge);
      builder.putEdge("2", dependencyScopeEdge);
      builder.putEdge("3", edgeWithoutLink);
      Graph graph = builder.build();

      // Act / Assert
      assertThat(GraphService.linksLimitedToScope(graph, "scope-a"))
          .contains(sourceScopeEdge.getLink());
      assertThat(GraphService.linksLimitedToScope(graph, "scope-z"))
          .contains(dependencyScopeEdge.getLink());
    }
  }

  @Nested
  class GraphEngineServiceDefaultMethodTests {

    @Test
    void fetchesAllEdgesThroughDefaultConvenienceMethods() {
      // Arrange
      com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge =
          new com.amadeus.aud.libraries.engine.graph.models.dto.Edge(
              "edge-name", "dep-id", "service-id", true, "src", "dst");
      Node dtoNode = new Node("node-id", "lz-name", "platform-name", "target-name");
      GraphEngineService sut = new FakeGraphEngineService(dtoEdge, dtoNode);

      // Act
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> targets =
          sut.getAllEdgesByTargets("v1", List.of("target"), EdgeDirection.ANY);
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> platforms =
          sut.getAllEdgesByPlatforms("v1", List.of("platform"), EdgeDirection.ANY);
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> landingZones =
          sut.getAllEdgesByLandingZones("v1", List.of("lz"), EdgeDirection.ANY);
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> nodeIds =
          sut.getAllEdgesByNodeIds("v1", List.of("src"), List.of("dst"));

      // Assert
      assertThat(targets).containsExactly(dtoEdge);
      assertThat(platforms).containsExactly(dtoEdge);
      assertThat(landingZones).containsExactly(dtoEdge);
      assertThat(nodeIds).containsExactly(dtoEdge);
    }

    @Test
    void delegatesNetworkComplianceStrategyThroughTestMethod() {
      // Arrange
      ComponentInstance source = mock(ComponentInstance.class);
      ComponentInstance destination = mock(ComponentInstance.class);

      // Act
      boolean result = NetworkComplianceStrategy.NONE.test(source, destination);

      // Assert
      assertThat(result).isFalse();
      assertThat(NetworkComplianceStrategy.NONE.isNetworkComplianceRequired(source, destination))
          .isFalse();
    }

    @Test
    void fetchesMultiplePagesWhenDefaultMethodsReceiveFullBatches() {
      // Arrange
      com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge =
          new com.amadeus.aud.libraries.engine.graph.models.dto.Edge(
              "edge-name", "dep-id", "service-id", true, "src", "dst");
      GraphEngineService sut = new MultiPageFakeGraphEngineService(dtoEdge);

      // Act
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> targets =
          sut.getAllEdgesByTargets("v1", List.of("target"), EdgeDirection.ANY);

      // Assert
      assertThat(targets).hasSize(GraphEngineService.MAX_EDGE_PAGE_SIZE + 1);
    }
  }

  private static final class FakeGraphEngineService implements GraphEngineService {
    private final com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge;
    private final Node dtoNode;

    private FakeGraphEngineService(
        com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge, Node dtoNode) {
      this.dtoEdge = dtoEdge;
      this.dtoNode = dtoNode;
    }

    @Override
    public Uni<Void> init(String versionNumber) {
      return Uni.createFrom().voidItem();
    }

    @Override
    @SuppressWarnings("removal")
    public @Nullable Graph getGraph(String versionNumber) {
      return null;
    }

    @Override
    public boolean isReady(String versionNumber) {
      return true;
    }

    @Override
    public Set<String> getAvailableVersionNumbers() {
      return Set.of("v1");
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByTargets(
        String versionString, int page, int size, List<String> targets, EdgeDirection direction) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdgesByPlatforms(
            String versionString,
            int page,
            int size,
            List<String> platforms,
            EdgeDirection direction) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdgesByLandingZones(
            String versionString,
            int page,
            int size,
            List<String> landingZones,
            EdgeDirection direction) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByNodeIds(
        String versionString,
        int page,
        int size,
        List<String> sourceNodeIds,
        List<String> destinationNodeIds) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByScope(
        String versionString, int page, int size, String scope) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<Node>> getGraphNodes(String versionNumber, int page, int size) {
      return Uni.createFrom().item(List.of(dtoNode));
    }

    @Override
    public Uni<List<Node>> getGraphNodesByTargets(
        String versionNumber, int page, int size, List<String> targets) {
      return Uni.createFrom().item(List.of(dtoNode));
    }

    @Override
    public Uni<List<Node>> getGraphNodesByPlatforms(
        String versionNumber, int page, int size, List<String> platforms) {
      return Uni.createFrom().item(List.of(dtoNode));
    }

    @Override
    public Uni<List<Node>> getGraphNodesByLandingZones(
        String versionNumber, int page, int size, List<String> landingZones) {
      return Uni.createFrom().item(List.of(dtoNode));
    }

    @Override
    public Uni<GraphStatistics> getGraphStatistics(String versionString) {
      return Uni.createFrom()
          .item(new GraphStatistics(java.util.Map.of(), java.util.Map.of(), 0L, 0L));
    }

    @Override
    public @Nullable Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdges(String versionNumber, int page, int size, EdgeDirection direction) {
      return null;
    }
  }

  private static ComponentInstance mockComponentInstance(String id, String name, String scopeName) {
    ComponentInstance componentInstance = mock(ComponentInstance.class);
    Scope scope = mock(Scope.class);
    doReturn(id).when(componentInstance).id();
    doReturn(name).when(componentInstance).getName();
    doReturn(scope).when(componentInstance).getScope();
    doReturn(scopeName).when(scope).getName();
    return componentInstance;
  }

  private static Edge newEdge(
      ComponentInstance source, ComponentInstance destination, String dependencyScopeName) {
    return new Edge(
        source,
        destination,
        mockDependencyInstance(dependencyScopeName),
        mockService(),
        true,
        true);
  }

  private static DependencyInstance mockDependencyInstance(String dependencyScopeName) {
    DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    Scope dependencyScope = mock(Scope.class);
    doReturn("dep-id-" + dependencyScopeName).when(dependencyInstance).id();
    doReturn("dependency-name").when(dependencyInstance).getName();
    doReturn(dependencyScope).when(dependencyInstance).getScope();
    doReturn(dependencyScopeName).when(dependencyScope).getName();
    return dependencyInstance;
  }

  private static Service mockService() {
    Service service = mock(Service.class);
    doReturn("service-id").when(service).id();
    doReturn("service-name").when(service).getName();
    doReturn("443").when(service).getPorts();
    doReturn(V1ServiceSpec.ProtocolEnum.TCP).when(service).getProtocol();
    return service;
  }

  private static final class MultiPageFakeGraphEngineService implements GraphEngineService {
    private final com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge;

    private MultiPageFakeGraphEngineService(
        com.amadeus.aud.libraries.engine.graph.models.dto.Edge dtoEdge) {
      this.dtoEdge = dtoEdge;
    }

    @Override
    public Uni<Void> init(String versionNumber) {
      return Uni.createFrom().voidItem();
    }

    @Override
    @SuppressWarnings("removal")
    public @Nullable Graph getGraph(String versionNumber) {
      return null;
    }

    @Override
    public boolean isReady(String versionNumber) {
      return true;
    }

    @Override
    public Set<String> getAvailableVersionNumbers() {
      return Set.of("v1");
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByTargets(
        String versionString, int page, int size, List<String> targets, EdgeDirection direction) {
      if (page == 0) {
        return Uni.createFrom().item(java.util.Collections.nCopies(size, dtoEdge));
      }
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdgesByPlatforms(
            String versionString,
            int page,
            int size,
            List<String> platforms,
            EdgeDirection direction) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdgesByLandingZones(
            String versionString,
            int page,
            int size,
            List<String> landingZones,
            EdgeDirection direction) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByNodeIds(
        String versionString,
        int page,
        int size,
        List<String> sourceNodeIds,
        List<String> destinationNodeIds) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>> getGraphEdgesByScope(
        String versionString, int page, int size, String scope) {
      return Uni.createFrom().item(List.of(dtoEdge));
    }

    @Override
    public @Nullable Uni<List<Node>> getGraphNodes(String versionNumber, int page, int size) {
      return null;
    }

    @Override
    public @Nullable Uni<List<Node>> getGraphNodesByTargets(
        String versionNumber, int page, int size, List<String> targets) {
      return null;
    }

    @Override
    public @Nullable Uni<List<Node>> getGraphNodesByPlatforms(
        String versionNumber, int page, int size, List<String> platforms) {
      return null;
    }

    @Override
    public @Nullable Uni<List<Node>> getGraphNodesByLandingZones(
        String versionNumber, int page, int size, List<String> landingZones) {
      return null;
    }

    @Override
    public Uni<GraphStatistics> getGraphStatistics(String versionString) {
      return Uni.createFrom()
          .item(new GraphStatistics(java.util.Map.of(), java.util.Map.of(), 0L, 0L));
    }

    @Override
    public @Nullable Uni<List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge>>
        getGraphEdges(String versionNumber, int page, int size, EdgeDirection direction) {
      return null;
    }
  }
}
