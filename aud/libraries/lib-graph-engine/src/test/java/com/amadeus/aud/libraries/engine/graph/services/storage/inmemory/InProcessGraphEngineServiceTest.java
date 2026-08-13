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
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

class InProcessGraphEngineServiceTest {

  @Test
  void queryApisAcceptNullFiltersAndReturnUnfilteredData() {
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
    assertThat(
            sut.getGraphEdgesByTargets("version-1", 0, 10, null, EdgeDirection.ANY)
                .await()
                .indefinitely())
        .hasSize(1);
    assertThat(
            sut.getGraphEdgesByPlatforms("version-1", 0, 10, null, EdgeDirection.ANY)
                .await()
                .indefinitely())
        .hasSize(1);
    assertThat(
            sut.getGraphEdgesByLandingZones("version-1", 0, 10, null, EdgeDirection.ANY)
                .await()
                .indefinitely())
        .hasSize(1);

    assertThat(sut.getGraphNodesByTargets("version-1", 0, 10, null).await().indefinitely())
        .hasSize(2);
    assertThat(sut.getGraphNodesByPlatforms("version-1", 0, 10, null).await().indefinitely())
        .hasSize(2);
    assertThat(sut.getGraphNodesByLandingZones("version-1", 0, 10, null).await().indefinitely())
        .hasSize(2);
  }

  @Test
  void lazilyBuiltVersionIsReusedAcrossSubsequentCalls() {
    // Arrange
    GraphFixture fixture = new GraphFixture();
    DataEngine dataEngine = mock(DataEngine.class);
    AudModelConfig audModelConfig = mock(AudModelConfig.class);
    doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
    doReturn(Uni.createFrom().item(fixture.version)).when(dataEngine).getVersion("version-1");

    InProcessGraphEngineService sut =
        new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

    // Act
    sut.getGraphEdges("version-1", 0, 10, EdgeDirection.ANY).await().indefinitely();
    sut.getGraphStatistics("version-1").await().indefinitely();
    sut.getGraphNodes("version-1", 0, 10).await().indefinitely();

    // Assert
    verify(fixture.graphService, times(1))
        .populateGraphFromDependencies(any(GraphBuilder.class), eq(fixture.version));
    assertThat(sut.getAvailableVersionNumbers()).contains("version-1");
  }

  @Test
  void queryMethodsPropagateVersionLookupFailures() {
    // Arrange
    GraphFixture fixture = new GraphFixture();
    DataEngine dataEngine = mock(DataEngine.class);
    AudModelConfig audModelConfig = mock(AudModelConfig.class);
    RuntimeException boom = new RuntimeException("boom");
    doReturn(Optional.empty()).when(audModelConfig).caffeineCache();
    doReturn(Uni.createFrom().failure(boom)).when(dataEngine).getVersion("bad-version");

    InProcessGraphEngineService sut =
        new InProcessGraphEngineService(fixture.graphService, dataEngine, audModelConfig);

    // Act / Assert
    assertThatThrownBy(
            () -> sut.getGraphEdges("bad-version", 0, 10, EdgeDirection.ANY).await().indefinitely())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("boom");

    assertThatThrownBy(
            () ->
                sut.getGraphNodesByTargets("bad-version", 0, 10, List.of("target"))
                    .await()
                    .indefinitely())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("boom");
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

    doReturn(id).when(componentInstance).id();
    doReturn(instanceName).when(componentInstance).getName();
    doReturn(scopeName).when(componentInstance).getScopeName();
    doReturn(mockScope(scopeName)).when(componentInstance).getScope();
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
