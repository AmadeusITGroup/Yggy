package com.amadeus.aud.libraries.engine.graph.models;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StoredModelsAndFiltersTest {

  @Nested
  class StoredFiltersTests {

    @Test
    void filtersAndPagesStoredEdges() {
      // Arrange
      StoredEdge first =
          new StoredEdge(
              "edge-1",
              "dep-1",
              "service-1",
              true,
              "src-1",
              "dst-1",
              "scope-a",
              "scope-b",
              "lz-a",
              "platform-a",
              "target-a",
              "lz-b",
              "platform-b",
              "target-b");
      StoredEdge second =
          new StoredEdge(
              "edge-2",
              "dep-2",
              "service-2",
              false,
              "src-2",
              "dst-2",
              "scope-c",
              "scope-d",
              "lz-c",
              "platform-c",
              "target-c",
              "lz-d",
              "platform-d",
              "target-d");

      // Act
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> page =
          StoredEdgeFilters.filterAndPage(List.of(first, second), 0, 1, edge -> true);

      // Assert
      assertThat(page)
          .singleElement()
          .satisfies(
              (com.amadeus.aud.libraries.engine.graph.models.dto.Edge edge) -> {
                assertThat(edge.getName()).isEqualTo("edge-1");
                assertThat(edge.getDependencyInstanceId()).isEqualTo("dep-1");
                assertThat(edge.getServiceId()).isEqualTo("service-1");
                assertThat(edge.getSourceNodeId()).isEqualTo("src-1");
                assertThat(edge.getTargetNodeId()).isEqualTo("dst-1");
              });
      assertThat(
              StoredEdgeFilters.matchesTargets(first, List.of("target-a"), EdgeDirection.OUTBOUND))
          .isTrue();
      assertThat(
              StoredEdgeFilters.matchesTargets(first, List.of("target-b"), EdgeDirection.INBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchesTargets(first, List.of("missing"), EdgeDirection.ANY))
          .isFalse();
      assertThat(
              StoredEdgeFilters.matchesPlatforms(
                  first, List.of("platform-a"), EdgeDirection.OUTBOUND))
          .isTrue();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(first, List.of("lz-b"), EdgeDirection.INBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchDirection(true, false, EdgeDirection.OUTBOUND)).isTrue();
      assertThat(StoredEdgeFilters.matchDirection(false, true, EdgeDirection.INBOUND)).isTrue();
      assertThat(StoredEdgeFilters.matchDirection(false, false, EdgeDirection.ANY)).isFalse();
      assertThat(StoredEdgeFilters.matchesTargets(first, null, EdgeDirection.ANY)).isTrue();
      assertThat(StoredEdgeFilters.matchesPlatforms(first, List.of(), EdgeDirection.ANY)).isTrue();
      assertThat(StoredEdgeFilters.matchesLandingZones(first, null, EdgeDirection.ANY)).isTrue();
      assertThat(StoredEdgeFilters.filterAndPage(List.of(first, second), 1, 1, edge -> true))
          .extracting(com.amadeus.aud.libraries.engine.graph.models.dto.Edge::getName)
          .containsExactly("edge-2");
      assertThat(
              StoredEdgeFilters.matchesPlatforms(first, List.of("missing"), EdgeDirection.OUTBOUND))
          .isFalse();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(
                  first, List.of("missing"), EdgeDirection.INBOUND))
          .isFalse();
      assertThat(StoredEdgeFilters.matchDirection(true, false, EdgeDirection.ANY)).isTrue();
      assertThat(StoredEdgeFilters.matchDirection(false, false, EdgeDirection.OUTBOUND)).isFalse();
      assertThat(StoredEdgeFilters.toDto(first).getTargetNodeId()).isEqualTo("dst-1");
    }

    @Test
    void filtersByScopeWithVariousMatches() {
      // Arrange
      StoredEdge sourceMatch =
          new StoredEdge(
              "edge-source",
              "dep-1",
              "svc-1",
              true,
              "src-1",
              "dst-1",
              "matching-scope",
              "other-scope",
              "lz-1",
              "p-1",
              "t-1",
              "lz-2",
              "p-2",
              "t-2");
      StoredEdge destinationMatch =
          new StoredEdge(
              "edge-dest",
              "dep-2",
              "svc-2",
              false,
              "src-2",
              "dst-2",
              "other-scope",
              "matching-scope",
              "lz-3",
              "p-3",
              "t-3",
              "lz-4",
              "p-4",
              "t-4");
      StoredEdge bothMatch =
          new StoredEdge(
              "edge-both",
              "dep-3",
              "svc-3",
              true,
              "src-3",
              "dst-3",
              "matching-scope",
              "matching-scope",
              "lz-5",
              "p-5",
              "t-5",
              "lz-6",
              "p-6",
              "t-6");
      StoredEdge noMatch =
          new StoredEdge(
              "edge-none",
              "dep-4",
              "svc-4",
              false,
              "src-4",
              "dst-4",
              "other-scope",
              "another-scope",
              "lz-7",
              "p-7",
              "t-7",
              "lz-8",
              "p-8",
              "t-8");

      // Act & Assert
      assertThat(StoredEdgeFilters.matchesScope(sourceMatch, "matching-scope")).isTrue();
      assertThat(StoredEdgeFilters.matchesScope(destinationMatch, "matching-scope")).isTrue();
      assertThat(StoredEdgeFilters.matchesScope(bothMatch, "matching-scope")).isTrue();
      assertThat(StoredEdgeFilters.matchesScope(noMatch, "matching-scope")).isFalse();
      assertThat(StoredEdgeFilters.matchesScope(sourceMatch, "non-existent")).isFalse();
    }

    @Test
    void filtersWithAllDirectionsAndMatchCombinations() {
      // Arrange
      StoredEdge edge =
          new StoredEdge(
              "edge",
              "dep",
              "svc",
              true,
              "src",
              "dst",
              "scope-src",
              "scope-dst",
              "lz-src",
              "p-src",
              "t-src",
              "lz-dst",
              "p-dst",
              "t-dst");

      // Test OUTBOUND (only source matters)
      assertThat(StoredEdgeFilters.matchesTargets(edge, List.of("t-src"), EdgeDirection.OUTBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchesTargets(edge, List.of("t-dst"), EdgeDirection.OUTBOUND))
          .isFalse();
      assertThat(StoredEdgeFilters.matchesPlatforms(edge, List.of("p-src"), EdgeDirection.OUTBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchesPlatforms(edge, List.of("p-dst"), EdgeDirection.OUTBOUND))
          .isFalse();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(
                  edge, List.of("lz-src"), EdgeDirection.OUTBOUND))
          .isTrue();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(
                  edge, List.of("lz-dst"), EdgeDirection.OUTBOUND))
          .isFalse();

      // Test INBOUND (only destination matters)
      assertThat(StoredEdgeFilters.matchesTargets(edge, List.of("t-dst"), EdgeDirection.INBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchesTargets(edge, List.of("t-src"), EdgeDirection.INBOUND))
          .isFalse();
      assertThat(StoredEdgeFilters.matchesPlatforms(edge, List.of("p-dst"), EdgeDirection.INBOUND))
          .isTrue();
      assertThat(StoredEdgeFilters.matchesPlatforms(edge, List.of("p-src"), EdgeDirection.INBOUND))
          .isFalse();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(edge, List.of("lz-dst"), EdgeDirection.INBOUND))
          .isTrue();
      assertThat(
              StoredEdgeFilters.matchesLandingZones(edge, List.of("lz-src"), EdgeDirection.INBOUND))
          .isFalse();
    }

    @Test
    void handlesEmptyListsAndNullValuesInFiltering() {
      // Arrange
      StoredEdge edge =
          new StoredEdge(
              "edge", "dep", "svc", true, "src", "dst", "scope", "scope", null, null, null, null,
              null, null);

      // Act & Assert - filtering with null node values should still work
      assertThat(StoredEdgeFilters.matchesTargets(edge, List.of("any"), EdgeDirection.ANY))
          .isFalse();
      assertThat(StoredEdgeFilters.matchesPlatforms(edge, List.of("any"), EdgeDirection.ANY))
          .isFalse();
      assertThat(StoredEdgeFilters.matchesLandingZones(edge, List.of("any"), EdgeDirection.ANY))
          .isFalse();
    }

    @Test
    void handlesPaginationWithFilters() {
      // Arrange - create edges with specific characteristics
      StoredEdge edge1 =
          new StoredEdge(
              "edge1", "dep1", "svc1", true, "src1", "dst1", "scope1", "scope2", "lz-a", "p-a",
              "t-a", "lz-b", "p-b", "t-b");
      StoredEdge edge2 =
          new StoredEdge(
              "edge2", "dep2", "svc2", false, "src2", "dst2", "scope1", "scope3", "lz-a", "p-a",
              "t-a", "lz-c", "p-c", "t-c");
      StoredEdge edge3 =
          new StoredEdge(
              "edge3", "dep3", "svc3", true, "src3", "dst3", "scope1", "scope4", "lz-a", "p-a",
              "t-a", "lz-d", "p-d", "t-d");

      // Act & Assert - paginate through filtered results
      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> page0 =
          StoredEdgeFilters.filterAndPage(
              List.of(edge1, edge2, edge3), 0, 2, e -> "lz-a".equals(e.sourceNodeLandingZone()));
      assertThat(page0).hasSize(2);

      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> page1 =
          StoredEdgeFilters.filterAndPage(
              List.of(edge1, edge2, edge3), 1, 2, e -> "lz-a".equals(e.sourceNodeLandingZone()));
      assertThat(page1).hasSize(1);

      List<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> page2 =
          StoredEdgeFilters.filterAndPage(
              List.of(edge1, edge2, edge3), 2, 2, e -> "lz-a".equals(e.sourceNodeLandingZone()));
      assertThat(page2).isEmpty();
    }

    @Test
    void filtersAndPagesStoredNodes() {
      // Arrange
      StoredNode first = new StoredNode("ci-1", "lz-a", "platform-a", "target-a");
      StoredNode second = new StoredNode("ci-2", "lz-b", "platform-b", "target-b");

      // Act
      List<Node> page = StoredNodeFilters.filterAndPage(List.of(first, second), 0, 2, node -> true);

      // Assert
      assertThat(page)
          .containsExactly(
              new Node("ci-1", "lz-a", "platform-a", "target-a"),
              new Node("ci-2", "lz-b", "platform-b", "target-b"));
      assertThat(StoredNodeFilters.matchesTargets(first, List.of("target-a"))).isTrue();
      assertThat(StoredNodeFilters.matchesPlatforms(first, List.of("platform-a"))).isTrue();
      assertThat(StoredNodeFilters.matchesLandingZones(first, List.of("lz-a"))).isTrue();
      assertThat(StoredNodeFilters.matchesLandingZones(first, List.of("missing"))).isFalse();
      assertThat(StoredNodeFilters.matchesTargets(first, null)).isTrue();
      assertThat(StoredNodeFilters.matchesPlatforms(first, List.of())).isTrue();
      assertThat(StoredNodeFilters.filterAndPage(List.of(first, second), 1, 1, node -> true))
          .containsExactly(new Node("ci-2", "lz-b", "platform-b", "target-b"));
      assertThat(StoredNodeFilters.matchesTargets(first, List.of("missing"))).isFalse();
      assertThat(StoredNodeFilters.matchesPlatforms(first, List.of("missing"))).isFalse();
      assertThat(StoredNodeFilters.toDto(first))
          .isEqualTo(new Node("ci-1", "lz-a", "platform-a", "target-a"));
      assertThat(
              StoredNodeFilters.filterAndPage(
                  List.of(first, second), 0, 1, node -> "ci-2".equals(node.componentInstanceId())))
          .containsExactly(new Node("ci-2", "lz-b", "platform-b", "target-b"));
      assertThat(StoredNodeFilters.filterAndPage(List.of(first, second), 0, 0, node -> true))
          .isEmpty();
    }
  }

  @Nested
  class StoredRecordTests {

    @Test
    void exposesStoredNodeAndStoredEdgeFields() {
      // Arrange
      StoredNode storedNode = new StoredNode("ci", "lz", "platform", "target");
      StoredEdge storedEdge =
          new StoredEdge(
              "name", "dep", "svc", true, "src", "dst", "scope1", "scope2", "lz1", "p1", "t1",
              "lz2", "p2", "t2");

      // Assert
      assertThat(storedNode.targetName()).isEqualTo("target");
      assertThat(storedEdge.destinationNodeTarget()).isEqualTo("t2");
    }
  }
}
