package com.amadeus.aud.libraries.engine.graph.services.storage.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.graph.clients.EdgesByNodeIdsRequest;
import com.amadeus.aud.libraries.engine.graph.clients.GraphEngineClient;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Every method under test dispatches through {@code Infrastructure.getDefaultWorkerPool()}, which
 * is shared across the whole JVM, and {@code isReady} deliberately reports {@code false} for any
 * failure. Run serially: under concurrent execution a saturated pool surfaces here as an
 * unexplained {@code false} rather than as a timeout. Same reason the service tests in
 * service-http-apis are annotated this way.
 */
@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class GraphEngineRemoteServiceTest {

  @Mock GraphEngineClient graphEngineClient;

  @Test
  void initReturnsVoidWhenRemoteRespondsAccepted() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.init("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.ACCEPTED).build()));

    // Act + Assert
    assertThatCode(() -> sut.init("1.0.0").await().indefinitely()).doesNotThrowAnyException();
  }

  @Test
  void initReturnsVoidWhenRemoteRespondsOk() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.init("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.OK).build()));

    // Act + Assert
    assertThatCode(() -> sut.init("1.0.0").await().indefinitely()).doesNotThrowAnyException();
  }

  @Test
  void initFailsWhenRemoteRespondsWithUnexpectedStatus() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.init("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));

    // Act + Assert
    assertThatThrownBy(() -> sut.init("1.0.0").await().indefinitely())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unexpected graph init status");
  }

  @Test
  void isReadyReturnsTrueWhenStatisticsAreAvailable() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.getGraphInitStatus("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.OK).build()));

    // Act
    boolean ready = sut.isReady("1.0.0");

    // Assert
    assertThat(ready).isTrue();
  }

  @Test
  void isReadyReturnsFalseWhenStatisticsAreNull() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.getGraphInitStatus("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.ACCEPTED).build()));

    // Act
    boolean ready = sut.isReady("1.0.0");

    // Assert
    assertThat(ready).isFalse();
  }

  @Test
  void isReadyReturnsFalseWhenRemoteCallFails() {
    // Arrange
    GraphEngineRemoteService sut =
        new GraphEngineRemoteService(
            graphEngineClient, GraphEngineRemoteService.RetryConfig.NO_RETRY);
    when(graphEngineClient.getGraphInitStatus("1.0.0"))
        .thenReturn(Uni.createFrom().failure(new RuntimeException("boom")));

    // Act
    boolean ready = sut.isReady("1.0.0");

    // Assert
    assertThat(ready).isFalse();
  }

  @Test
  void isReadyReturnsFalseWhenInitStatusEndpointReturnsNotFound() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.getGraphInitStatus("1.0.0"))
        .thenReturn(Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build()));

    // Act
    boolean ready = sut.isReady("1.0.0");

    // Assert
    assertThat(ready).isFalse();
  }

  @Test
  void getCachedVersionNumbersReturnsAllRemoteVersions() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.getAvailableVersions())
        .thenReturn(Uni.createFrom().item(List.of("1.0.3", "1.0.2")));

    // Act
    Set<String> versions = sut.getAvailableVersionNumbers();

    // Assert
    assertThat(versions).containsExactly("1.0.3", "1.0.2");
  }

  @Test
  void getCachedVersionNumbersReturnsEmptySetWhenRemoteReturnsNull() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    when(graphEngineClient.getAvailableVersions()).thenReturn(Uni.createFrom().nullItem());

    // Act
    Set<String> versions = sut.getAvailableVersionNumbers();

    // Assert
    assertThat(versions).isEmpty();
  }

  @Test
  void getCachedVersionNumbersReturnsEmptySetWhenRemoteCallFails() {
    // Arrange
    GraphEngineRemoteService sut =
        new GraphEngineRemoteService(
            graphEngineClient, GraphEngineRemoteService.RetryConfig.NO_RETRY);
    when(graphEngineClient.getAvailableVersions())
        .thenReturn(Uni.createFrom().failure(new RuntimeException("boom")));

    // Act
    Set<String> versions = sut.getAvailableVersionNumbers();

    // Assert
    assertThat(versions).isEmpty();
  }

  @Test
  void delegatesNodeQueriesToRemoteClientWithExpectedFilters() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    List<Node> nodes = List.of(new Node("ci-1", "lz-a", "platform-a", "target-a"));
    when(graphEngineClient.getGraphNodes("1.0.0", 0, 50, null, null, null))
        .thenReturn(Uni.createFrom().item(nodes));
    when(graphEngineClient.getGraphNodes("1.0.0", 0, 50, List.of("lz-a"), null, null))
        .thenReturn(Uni.createFrom().item(nodes));
    when(graphEngineClient.getGraphNodes("1.0.0", 0, 50, null, List.of("platform-a"), null))
        .thenReturn(Uni.createFrom().item(nodes));
    when(graphEngineClient.getGraphNodes("1.0.0", 0, 50, null, null, List.of("target-a")))
        .thenReturn(Uni.createFrom().item(nodes));

    // Act / Assert
    assertThat(sut.getGraphNodes("1.0.0", 0, 50).await().indefinitely()).isEqualTo(nodes);
    assertThat(
            sut.getGraphNodesByLandingZones("1.0.0", 0, 50, List.of("lz-a")).await().indefinitely())
        .isEqualTo(nodes);
    assertThat(
            sut.getGraphNodesByPlatforms("1.0.0", 0, 50, List.of("platform-a"))
                .await()
                .indefinitely())
        .isEqualTo(nodes);
    assertThat(
            sut.getGraphNodesByTargets("1.0.0", 0, 50, List.of("target-a")).await().indefinitely())
        .isEqualTo(nodes);
  }

  @Test
  void delegatesEdgeQueriesAndStatisticsToRemoteClient() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);
    List<Edge> edges = List.of(new Edge("edge-1", "dep-1", "svc-1", true, "src", "dst"));
    GraphStatistics statistics =
        new GraphStatistics(java.util.Map.of("scope", 1L), java.util.Map.of(), 1L, 1L);

    when(graphEngineClient.getGraphEdges("1.0.0", 0, 100, null, null, null, EdgeDirection.ANY))
        .thenReturn(Uni.createFrom().item(edges));
    when(graphEngineClient.getGraphEdges(
            "1.0.0", 0, 100, List.of("lz-a"), null, null, EdgeDirection.OUTBOUND))
        .thenReturn(Uni.createFrom().item(edges));
    when(graphEngineClient.getGraphEdges(
            "1.0.0", 0, 100, null, List.of("platform-a"), null, EdgeDirection.INBOUND))
        .thenReturn(Uni.createFrom().item(edges));
    when(graphEngineClient.getGraphEdges(
            "1.0.0", 0, 100, null, null, List.of("target-a"), EdgeDirection.ANY))
        .thenReturn(Uni.createFrom().item(edges));
    when(graphEngineClient.getGraphEdgesByNodeIds(
            eq("1.0.0"),
            eq(0),
            eq(100),
            org.mockito.ArgumentMatchers.argThat(
                req ->
                    req != null
                        && List.of("src").equals(req.getSourceNodeIds())
                        && List.of("dst").equals(req.getDestinationNodeIds()))))
        .thenReturn(Uni.createFrom().item(edges));
    when(graphEngineClient.getGraphStatistics("1.0.0"))
        .thenReturn(Uni.createFrom().item(statistics));

    // Act / Assert
    assertThat(sut.getGraphEdges("1.0.0", 0, 100, EdgeDirection.ANY).await().indefinitely())
        .isEqualTo(edges);
    assertThat(
            sut.getGraphEdgesByLandingZones(
                    "1.0.0", 0, 100, List.of("lz-a"), EdgeDirection.OUTBOUND)
                .await()
                .indefinitely())
        .isEqualTo(edges);
    assertThat(
            sut.getGraphEdgesByPlatforms(
                    "1.0.0", 0, 100, List.of("platform-a"), EdgeDirection.INBOUND)
                .await()
                .indefinitely())
        .isEqualTo(edges);
    assertThat(
            sut.getGraphEdgesByTargets("1.0.0", 0, 100, List.of("target-a"), EdgeDirection.ANY)
                .await()
                .indefinitely())
        .isEqualTo(edges);
    assertThat(
            sut.getGraphEdgesByNodeIds("1.0.0", 0, 100, List.of("src"), List.of("dst"))
                .await()
                .indefinitely())
        .isEqualTo(edges);
    assertThat(sut.getGraphStatistics("1.0.0").await().indefinitely()).isEqualTo(statistics);
  }

  @Test
  @SuppressWarnings("removal")
  void returnsDefaultValuesForDeprecatedAndScopeMethods() {
    // Arrange
    GraphEngineRemoteService sut = new GraphEngineRemoteService(graphEngineClient);

    // Act / Assert
    assertThat(sut.getGraph("1.0.0")).isNull();
  }

  @Test
  void edgesByNodeIdsRequestConstructorsAndGettersWork() {
    // Arrange
    List<String> sourceIds = List.of("src1", "src2");
    List<String> destinationIds = List.of("dst1", "dst2");

    // Act - Constructor with parameters
    EdgesByNodeIdsRequest request1 = new EdgesByNodeIdsRequest(sourceIds, destinationIds);

    // Assert
    assertThat(request1.getSourceNodeIds()).isEqualTo(sourceIds);
    assertThat(request1.getDestinationNodeIds()).isEqualTo(destinationIds);

    // Act - Default constructor and setters
    EdgesByNodeIdsRequest request2 = new EdgesByNodeIdsRequest();
    request2.setSourceNodeIds(sourceIds);
    request2.setDestinationNodeIds(destinationIds);

    // Assert
    assertThat(request2.getSourceNodeIds()).isEqualTo(sourceIds);
    assertThat(request2.getDestinationNodeIds()).isEqualTo(destinationIds);
  }
}
