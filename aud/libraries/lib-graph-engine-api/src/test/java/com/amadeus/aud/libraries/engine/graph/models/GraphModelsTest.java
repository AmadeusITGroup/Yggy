package com.amadeus.aud.libraries.engine.graph.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphModelsTest {

  @Nested
  class GraphBuilderAndGraphTests {

    @Test
    void buildsImmutableGraphWithNodesAndEdges() {
      // Arrange - inline mocks to avoid helper method overhead
      GraphBuilder sut = new GraphBuilder("version-42", true);

      ComponentInstance source = mock(ComponentInstance.class);
      Scope sourceScope = mock(Scope.class);
      doReturn("src-id").when(source).id();
      doReturn("src-name").when(source).getName();
      doReturn(sourceScope).when(source).getScope();
      doReturn("scope-a").when(sourceScope).getName();

      ComponentInstance destination = mock(ComponentInstance.class);
      Scope destScope = mock(Scope.class);
      doReturn("dst-id").when(destination).id();
      doReturn("dst-name").when(destination).getName();
      doReturn(destScope).when(destination).getScope();
      doReturn("scope-b").when(destScope).getName();

      DependencyInstance dependencyInstance = mock(DependencyInstance.class);
      Service service = mock(Service.class);
      doReturn("dependency-id").when(dependencyInstance).id();
      doReturn("dependency-name").when(dependencyInstance).getName();
      doReturn("service-id").when(service).id();
      doReturn("service-name").when(service).getName();
      doReturn("8080").when(service).getPorts();
      doReturn(ProtocolEnum.TCP).when(service).getProtocol();

      Edge edge = new Edge(source, destination, dependencyInstance, service, true, true);

      // Act
      sut.putNode(source.id(), source);
      sut.putNode(destination.id(), destination);
      sut.putEdge(edge.getId(), edge);
      Graph graph = sut.build();

      // Assert
      assertThat(sut.isStoreFullLink()).isTrue();
      assertThat(sut.containsNode("src-id")).isTrue();
      assertThat(sut.getEdge(edge.getId())).isSameAs(edge);
      assertThat(sut.nodesSize()).isEqualTo(2);
      assertThat(sut.edgesSize()).isOne();
      assertThat(sut.getVersionNumber()).isEqualTo("version-42");

      assertThat(graph.getVersionNumber()).isEqualTo("version-42");
      assertThat(graph.containsNode("src-id")).isTrue();
      assertThat(graph.getEdge(edge.getId())).isSameAs(edge);
      assertThat(graph.getNodes()).containsEntry("src-id", source);
      assertThat(graph.getEdges()).containsEntry(edge.getId(), edge);
    }
  }

  @Nested
  class EdgeAndLinkTests {

    @Test
    void exposesExpectedIdentifiersNamesAndLinkData() {
      // Arrange - inline all mock setup to avoid helper method overhead
      ComponentInstance source = mock(ComponentInstance.class);
      Scope sourceScope = mock(Scope.class);
      doReturn("src-id").when(source).id();
      doReturn("src-name").when(source).getName();
      doReturn(sourceScope).when(source).getScope();
      doReturn("scope-a").when(sourceScope).getName();

      ComponentInstance destination = mock(ComponentInstance.class);
      Scope destScope = mock(Scope.class);
      doReturn("dst-id").when(destination).id();
      doReturn("dst-name").when(destination).getName();
      doReturn(destScope).when(destination).getScope();
      doReturn("scope-b").when(destScope).getName();

      DependencyInstance dependencyInstance = mock(DependencyInstance.class);
      Service service = mock(Service.class);
      doReturn("dependency-id").when(dependencyInstance).id();
      doReturn("dependency-name").when(dependencyInstance).getName();
      doReturn("service-id").when(service).id();
      doReturn("service-name").when(service).getName();
      doReturn("8080").when(service).getPorts();
      doReturn(ProtocolEnum.TCP).when(service).getProtocol();

      // Act
      Edge edge = new Edge(source, destination, dependencyInstance, service, true, true);
      Link link = edge.getLink();

      // Assert
      assertThat(edge.getId()).isEqualTo("src-id***dst-id***service-id***dependency-id");
      assertThat(edge.getName()).isEqualTo("src-name***dst-name***service-id***dependency-name");
      assertThat(edge.getSource().id()).isEqualTo("src-id");
      assertThat(edge.getDestination().id()).isEqualTo("dst-id");
      assertThat(edge.getDependencyInstance().id()).isEqualTo("dependency-id");
      assertThat(edge.getService().id()).isEqualTo("service-id");
      assertThat(edge.isGovernanceActive()).isTrue();
      assertThat(link).isNotNull();
      assertThat(link.name()).isEqualTo("dependency-name - Tcp - 8080");
      assertThat(link.functionalName())
          .isEqualTo("From CI: src-name\nTo CI: dst-name\nService: service-name");
      Rule rule = new Rule("rule-name", "SNOW-1");
      link.setRule(rule);
      assertThat(link.rule()).isEqualTo(rule);
      assertThat(link).isEqualTo(new Link(source, destination, dependencyInstance, service));
      assertThat(link.toString()).contains("source=").contains("target=");
      assertThat(Edge.getUniqueId(source, destination, service, dependencyInstance))
          .isEqualTo(edge.getId());
      assertThat(Edge.getName(source, destination, service, dependencyInstance))
          .isEqualTo(edge.getName());
      assertThat(edge)
          .isEqualTo(edge)
          .isEqualTo(new Edge(source, destination, dependencyInstance, service, true, true))
          .isNotEqualTo(new Object())
          .isNotEqualTo(null)
          .isNotEqualTo(new Edge(source, destination, dependencyInstance, service, false, true));

      // Inline otherSource instead of calling mockComponentInstance
      ComponentInstance otherSource = mock(ComponentInstance.class);
      Scope otherSourceScope = mock(Scope.class);
      doReturn("other-src").when(otherSource).id();
      doReturn("other-src").when(otherSource).getName();
      doReturn(otherSourceScope).when(otherSource).getScope();
      doReturn("scope-a").when(otherSourceScope).getName();

      assertThat(edge)
          .isNotEqualTo(
              new Edge(otherSource, destination, dependencyInstance, service, true, true));
      assertThat(new Edge(source, destination, dependencyInstance, service, false, false).getLink())
          .isNull();
      Link serviceLessLink = new Link(source, destination, dependencyInstance, null);
      assertThat(serviceLessLink.name()).isEqualTo("dependency-name - null - null");
      assertThat(serviceLessLink).isNotEqualTo(new Object()).isNotEqualTo(null);
      serviceLessLink.setRule(null);
      assertThat(serviceLessLink.rule()).isNull();
      assertThat(serviceLessLink)
          .isNotEqualTo(new Link(source, destination, dependencyInstance, service));
    }
  }

  @Nested
  class ResponseAndRecordTests {

    @Test
    void exposesGraphResponseAndSimpleRecords() {
      // Arrange
      GraphResponse graphResponse =
          new GraphResponse("/url", "v2", "1.0", "api", List.of("n1"), List.of("e1"));
      GraphResponse equalResponse =
          new GraphResponse("/url", "v2", "1.0", "api", List.of("n1"), List.of("e1"));
      GraphStatistics graphStatistics =
          new GraphStatistics(java.util.Map.of("scope", 1L), java.util.Map.of("scope", 2L), 2L, 3L);
      Rule rule = new Rule("rule", "snow");

      // Act
      graphResponse.setNodesIds(List.of("n2"));
      graphResponse.setEdgesIds(List.of("e2"));

      // Assert
      assertThat(graphResponse.getUrl()).isEqualTo("/url");
      assertThat(graphResponse.getUrlVersion()).isEqualTo("v2");
      assertThat(graphResponse.getVersion()).isEqualTo("1.0");
      assertThat(graphResponse.getApiVersion()).isEqualTo("api");
      assertThat(graphResponse.getNodesIds()).containsExactly("n2");
      assertThat(graphResponse.getEdgesIds()).containsExactly("e2");
      assertThat(equalResponse).isEqualTo(equalResponse);
      assertThat(graphResponse).isNotEqualTo(equalResponse).isNotEqualTo(new Object());
      assertThat(equalResponse)
          .isEqualTo(new GraphResponse("/url", "v2", "1.0", "api", List.of("n1"), List.of("e1")))
          .hasSameHashCodeAs(equalResponse);
      assertThat(graphStatistics.scopeNbLink()).containsEntry("scope", 1L);
      assertThat(graphStatistics.scopeNbNode()).containsEntry("scope", 2L);
      assertThat(graphStatistics.globalNodesCount()).isEqualTo(2L);
      assertThat(graphStatistics.globalLinksCount()).isEqualTo(3L);
      assertThat(rule.name()).isEqualTo("rule");
      assertThat(rule.serviceNowId()).isEqualTo("snow");
      assertThat(EdgeDirection.values())
          .containsExactly(EdgeDirection.INBOUND, EdgeDirection.OUTBOUND, EdgeDirection.ANY);
    }
  }
}
