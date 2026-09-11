package com.amadeus.aud.services.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.graph.clients.EdgesByNodeIdsRequest;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Graph;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.services.engine.services.resources.GraphResourceService;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphResourceTest {
  private static final String VERSION = "1.0.0";

  private GraphResourceService graphService;
  private GraphResource graphResource;

  @BeforeEach
  void setUp() {
    graphService = mock(GraphResourceService.class);
    graphResource = new GraphResource();
    graphResource.graphService = graphService;
  }

  @Test
  void initGraphDelegatesToService() {
    Response expected = Response.accepted().build();
    when(graphService.initGraphAsync(VERSION)).thenReturn(expected);

    Response actual = graphResource.initGraph(VERSION);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphInitStatusDelegatesToService() {
    Response expected = Response.ok().build();
    when(graphService.getInitStatus(VERSION)).thenReturn(expected);

    Response actual = graphResource.getGraphInitStatus(VERSION);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphDelegatesToService() {
    Graph expected = new Graph(LocalDateTime.MIN, null, VERSION, 2, 1);
    when(graphService.getGraph(VERSION)).thenReturn(expected);

    Graph actual = graphResource.getGraph(VERSION);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphNodesDelegatesToService() {
    List<String> landingZones = List.of("landing-zone");
    List<String> platforms = List.of("platform");
    List<String> targets = List.of("target");
    List<Node> expected = List.of();
    when(graphService.getGraphNodes(VERSION, landingZones, platforms, targets, 2, 50))
        .thenReturn(expected);

    List<Node> actual =
        graphResource.getGraphNodes(VERSION, 2, 50, landingZones, platforms, targets);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphEdgesDelegatesToService() {
    List<String> landingZones = List.of("landing-zone");
    List<String> platforms = List.of("platform");
    List<String> targets = List.of("target");
    List<Edge> expected = List.of();
    when(graphService.getGraphEdges(
            VERSION, landingZones, platforms, targets, EdgeDirection.OUTBOUND, 2, 50))
        .thenReturn(expected);

    List<Edge> actual =
        graphResource.getGraphEdges(
            VERSION, 2, 50, landingZones, platforms, targets, EdgeDirection.OUTBOUND);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphEdgesByScopeDelegatesToService() {
    List<Edge> expected = List.of();
    when(graphService.getGraphEdgesByScope(VERSION, "scope", 2, 50)).thenReturn(expected);

    List<Edge> actual = graphResource.getGraphEdgesByScope(VERSION, 2, 50, "scope");

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphEdgesByNodeIdsDelegatesToService() {
    List<String> sourceNodeIds = List.of("source");
    List<String> destinationNodeIds = List.of("destination");
    EdgesByNodeIdsRequest request = new EdgesByNodeIdsRequest(sourceNodeIds, destinationNodeIds);
    List<Edge> expected = List.of();
    when(graphService.getGraphEdgesByNodeIds(VERSION, sourceNodeIds, destinationNodeIds, 2, 50))
        .thenReturn(expected);

    List<Edge> actual = graphResource.getGraphEdgesByNodeIds(VERSION, 2, 50, request);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphEdgesByNodeIdsAcceptsNullRequest() {
    List<Edge> expected = List.of();
    when(graphService.getGraphEdgesByNodeIds(VERSION, null, null, 0, 100)).thenReturn(expected);

    List<Edge> actual = graphResource.getGraphEdgesByNodeIds(VERSION, 0, 100, null);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getGraphStatisticsDelegatesToService() {
    GraphStatistics expected = new GraphStatistics(Map.of(), Map.of(), 2, 1);
    when(graphService.getGraphStatistics(VERSION)).thenReturn(expected);

    GraphStatistics actual = graphResource.getGraphStatistics(VERSION);

    assertThat(actual).isSameAs(expected);
  }

  @Test
  void getAvailableVersionsDelegatesToService() {
    List<String> expected = List.of(VERSION);
    when(graphService.getAllVersions()).thenReturn(expected);

    List<String> actual = graphResource.getAvailableVersions();

    assertThat(actual).isSameAs(expected);
  }
}
