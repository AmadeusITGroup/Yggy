package com.amadeus.aud.libraries.engine.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphDiff;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.engine.graph.services.storage.SimpleGraphVersion;
import com.amadeus.aud.libraries.engine.graph.services.storage.postgres.GraphEntity;
import com.amadeus.aud.libraries.engine.graph.utils.StringUtils;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphCoverageSupportTest {

  @Nested
  class StringUtilsTests {

    @Test
    void escapesCsvValuesWhenSpecialCharactersArePresent() {
      assertThat(StringUtils.escapeCsvValue(null)).isEmpty();
      assertThat(StringUtils.escapeCsvValue("plain")).isEqualTo("plain");
      assertThat(StringUtils.escapeCsvValue("a,b")).isEqualTo("\"a,b\"");
      assertThat(StringUtils.escapeCsvValue("a\"b")).isEqualTo("\"a\"\"b\"");
      assertThat(StringUtils.escapeCsvValue("a\nb")).isEqualTo("\"a\nb\"");
    }

    @Test
    void escapesTextValuesForPostgresTextProtocol() {
      assertThat(StringUtils.escapeTextValue(null)).isEqualTo("\\N");
      assertThat(StringUtils.escapeTextValue("plain")).isEqualTo("plain");
      assertThat(StringUtils.escapeTextValue("a\tb\nc\\d\re")).isEqualTo("a\\tb\\nc\\\\d\\re");
    }
  }

  @Nested
  class GraphDiffTests {

    @Test
    void computesAddedAndRemovedNodesAndEdges() {
      ComponentInstance src = mockComponent("src", "source", "scope-a");
      ComponentInstance dst = mockComponent("dst", "destination", "scope-b");
      DependencyInstance dep = mockDependency("dep-1", "dep-name", "dep-scope");
      Service svc = mockService("svc-1");

      GraphBuilder builder = new GraphBuilder("v1");
      builder.putNode(src.id(), src);
      builder.putNode(dst.id(), dst);
      Edge edge = new Edge(src, dst, dep, svc, true);
      builder.putEdge(edge.getId(), edge);

      GraphDiff diff =
          GraphDiff.compute(Set.of("src", "gone"), Set.of("old-edge"), builder.build());

      assertThat(diff.addedNodes()).containsOnlyKeys("dst");
      assertThat(diff.removedNodeIds()).containsExactly("gone");
      assertThat(diff.addedEdges()).containsOnlyKeys(edge.getId());
      assertThat(diff.removedEdgeIds()).containsExactly("old-edge");
      assertThat(diff.isEmpty()).isFalse();
    }

    @Test
    void isEmptyWhenPreviousAndCurrentSetsMatch() {
      ComponentInstance src = mockComponent("src", "source", "scope-a");
      ComponentInstance dst = mockComponent("dst", "destination", "scope-b");
      DependencyInstance dep = mockDependency("dep-1", "dep-name", "dep-scope");
      Service svc = mockService("svc-1");

      GraphBuilder builder = new GraphBuilder("v1");
      builder.putNode(src.id(), src);
      builder.putNode(dst.id(), dst);
      Edge edge = new Edge(src, dst, dep, svc, true);
      builder.putEdge(edge.getId(), edge);

      GraphDiff diff =
          GraphDiff.compute(Set.of("src", "dst"), Set.of(edge.getId()), builder.build());

      assertThat(diff.isEmpty()).isTrue();
    }
  }

  @Nested
  class GraphEntityAndStorageModelTests {

    @Test
    void graphEntityExposesImmutableStatisticsMapsAndLifecycleTimestamps() {
      GraphEntity entity = new GraphEntity();
      entity.prePersist();

      assertThat(entity.getCreatedAt()).isNotNull();

      entity.setVersion("1.2.3");
      entity.setVersionRank(7);
      entity.setStatisticsGlobalLinksCount(3);
      entity.setStatisticsGlobalNodesCount(2);

      Map<String, Long> scopeLinks = Map.of("scope-a", 3L);
      Map<String, Long> scopeNodes = Map.of("scope-a", 2L);
      entity.setStatisticsScopeNbLink(scopeLinks);
      entity.setStatisticsScopeNbNode(scopeNodes);

      GraphStatistics stats = entity.toStatistics();
      assertThat(stats.scopeNbLink()).containsEntry("scope-a", 3L);
      assertThat(stats.scopeNbNode()).containsEntry("scope-a", 2L);

      entity.preUpdate();
      assertThat(entity.getStatisticsScopeNbLink()).isEqualTo(scopeLinks);
      assertThat(entity.getStatisticsScopeNbNode()).isEqualTo(scopeNodes);
    }

    @Test
    void graphEntityComputesAndAppliesStatisticsFromGraph() {
      ComponentInstance src = mockComponent("src", "source", "scope-a");
      ComponentInstance dst = mockComponent("dst", "destination", "scope-b");
      DependencyInstance dep = mockDependency("dep-1", "dep-name", "dep-scope");
      Service svc = mockService("svc-1");

      GraphBuilder builder = new GraphBuilder("v2");
      builder.putNode(src.id(), src);
      builder.putNode(dst.id(), dst);
      builder.putEdge("e1", new Edge(src, dst, dep, svc, true));

      Version version = mock(Version.class);
      doReturn(null).when(version).getDependencyInstancesRepository();

      GraphEntity entity = new GraphEntity();
      entity.computeAndApplyStatistics(builder.build(), version);

      assertThat(entity.getStatisticsGlobalLinksCount()).isEqualTo(1);
      assertThat(entity.getStatisticsGlobalNodesCount()).isEqualTo(2);
      assertThat(entity.getStatisticsScopeNbNode())
          .containsEntry("scope-a", 1L)
          .containsEntry("scope-b", 1L);
      assertThat(entity.getStatisticsScopeNbLink()).containsEntry("unknown", 1L);
    }

    @Test
    void simpleGraphVersionAndExceptionConstructorsWorkAsExpected() {
      LocalDateTime now = LocalDateTime.now();
      GraphStatistics stats = new GraphStatistics(Map.of("s", 1L), Map.of("s", 2L), 2L, 1L);

      SimpleGraphVersion withDefaultRank = new SimpleGraphVersion("1.0.0", now, stats);
      SimpleGraphVersion withRank = new SimpleGraphVersion("1.0.1", now, stats, 9);

      assertThat(withDefaultRank.getVersionRank()).isZero();
      assertThat(withRank.getVersion()).isEqualTo("1.0.1");
      assertThat(withRank.getCreatedAt()).isEqualTo(now);
      assertThat(withRank.toStatistics()).isEqualTo(stats);

      RuntimeException cause = new RuntimeException("boom");
      GraphSaveException byMessage = new GraphSaveException("m");
      GraphSaveException byMessageCause = new GraphSaveException("m2", cause);
      GraphSaveException byCause = new GraphSaveException(cause);

      assertThat(byMessage).hasMessage("m");
      assertThat(byMessageCause).hasMessage("m2").hasCause(cause);
      assertThat(byCause).hasCause(cause);
    }
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
