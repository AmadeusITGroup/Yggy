package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.GraphResponse;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphServiceTest {
  private GraphEngineService graphEngineService;
  private AudService audService;
  private AudApiConfig audApiConfig;
  private UriInfo uriInfo;
  private GraphService service;

  @BeforeEach
  void setUp() throws Exception {
    graphEngineService = mock(GraphEngineService.class);
    audService = mock(AudService.class);
    audApiConfig = mock(AudApiConfig.class);
    uriInfo = mock(UriInfo.class);
    service = new GraphService(graphEngineService, audService, audApiConfig);

    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("https://aud.example/graph"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("https://aud.example/graph"));
  }

  @Test
  void explicitVersionTransformsEdgesIntoOrderedUniqueNodesAndNames() {
    Version version = version("1.2.3");
    Edge first = new Edge("edge-a", "dependency-a", "service-a", true, "node-a", "node-b");
    Edge second = new Edge("edge-b", "dependency-b", "service-b", false, "node-b", "node-c");
    when(audService.getVersion("1.2.3")).thenReturn(Uni.createFrom().item(version));
    when(graphEngineService.getAllEdgesByNodeIds("1.2.3", List.of(), List.of()))
        .thenReturn(List.of(first, second));

    Response response = service.getGraph("1.2.3", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity())
        .isInstanceOfSatisfying(
            GraphResponse.class,
            (GraphResponse body) -> {
              assertThat(body.getVersion()).isEqualTo("1.2.3");
              assertThat(body.getApiVersion()).isEqualTo("v1");
              assertThat(body.getUrl()).isEqualTo("https://aud.example/graph");
              assertThat(body.getUrlVersion()).isEqualTo("https://aud.example/version/1.2.3/graph");
              assertThat(body.getNodesIds()).containsExactly("node-a", "node-b", "node-c");
              assertThat(body.getEdgesIds()).containsExactly("edge-a", "edge-b");
            });
    verify(graphEngineService).getAllEdgesByNodeIds("1.2.3", List.of(), List.of());
  }

  @Test
  void noVersionOverloadUsesCurrentVersionAndSupportsEmptyGraph() {
    Version version = version("2.0.0");
    when(audService.getCurrentVersion()).thenReturn(Uni.createFrom().item(version));
    when(graphEngineService.getAllEdgesByNodeIds("2.0.0", List.of(), List.of()))
        .thenReturn(List.of());

    Response response = service.getGraph(uriInfo).await().indefinitely();

    assertThat(response.getEntity())
        .isInstanceOfSatisfying(
            GraphResponse.class,
            (GraphResponse body) -> {
              assertThat(body.getNodesIds()).isEmpty();
              assertThat(body.getEdgesIds()).isEmpty();
            });
    verify(audService).getCurrentVersion();
  }

  @Test
  void missingExplicitVersionFailsWithNotFound() {
    when(audService.getVersion("missing")).thenReturn(Uni.createFrom().nullItem());

    assertThatThrownBy(() -> service.getGraph("missing", uriInfo).await().indefinitely())
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Version not found: missing");
  }

  @Test
  void missingCurrentVersionFailsWithInternalServerError() {
    when(audService.getCurrentVersion()).thenReturn(Uni.createFrom().nullItem());

    assertThatThrownBy(() -> service.getGraph(uriInfo).await().indefinitely())
        .isInstanceOf(InternalServerErrorException.class)
        .hasMessage("No latest version found.");
  }

  private static Version version(String number) {
    Version version = new Version();
    version.setVersionNumber(number);
    return version;
  }
}
