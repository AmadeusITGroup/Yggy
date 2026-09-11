package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectServiceTest {
  private AudService audService;
  private ModelsService modelsService;
  private AudApiConfig audApiConfig;
  private UriInfo uriInfo;
  private ObjectMapper objectMapper;
  private ObjectService service;
  private Version version;

  @BeforeEach
  void setUp() throws Exception {
    audService = mock(AudService.class);
    modelsService = mock(ModelsService.class);
    audApiConfig = mock(AudApiConfig.class);
    uriInfo = mock(UriInfo.class);
    objectMapper = EndpointTestObjectMapper.get();
    service = new ObjectService(audService, modelsService, objectMapper, audApiConfig);
    version = new Version();
    version.setVersionNumber("1.2.3");

    when(audService.getVersion("1.2.3")).thenReturn(Uni.createFrom().item(version));
  }

  @Test
  void stringVersionWrapperReturnsMatchingObjectAndMetadata() throws Exception {
    ObjectNode other = objectMapper.createObjectNode().put("name", "other");
    ObjectNode matching = objectMapper.createObjectNode().put("name", "wanted").put("value", 42);
    when(modelsService.generateResponse(
            version, "component", List.of(), uriInfo, Integer.MAX_VALUE))
        .thenReturn(List.of(other, matching));
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("https://aud.example/object"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("https://aud.example/object"));

    Response response = service.get("1.2.3", "component", "wanted", uriInfo).await().indefinitely();
    JsonNode body = objectMapper.readTree((byte[]) response.getEntity());

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(body.get("apiVersion").asText()).isEqualTo("v1");
    assertThat(body.get("version").asText()).isEqualTo("1.2.3");
    assertThat(body.get("url").asText()).isEqualTo("https://aud.example/object");
    assertThat(body.get("urlVersion").asText())
        .isEqualTo("https://aud.example/version/1.2.3/object");
    assertThat(body.get("component")).isEqualTo(matching);
    verify(audService).getVersion("1.2.3");
  }

  @Test
  void responseWithoutMatchingNameFailsWithNotFound() throws Exception {
    ObjectNode value = objectMapper.createObjectNode().put("name", "other");
    when(modelsService.generateResponse(
            version, "component", List.of(), uriInfo, Integer.MAX_VALUE))
        .thenReturn(List.of(value));

    assertThatThrownBy(
            () -> service.get("1.2.3", "component", "wanted", uriInfo).await().indefinitely())
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Name query parameter doesn't match any object");
  }

  @Test
  void responseWithAbsentNameFieldFailsWithNotFound() throws Exception {
    ObjectNode value = objectMapper.createObjectNode().put("value", 42);
    when(modelsService.generateResponse(
            version, "component", List.of(), uriInfo, Integer.MAX_VALUE))
        .thenReturn(List.of(value));

    assertThatThrownBy(
            () -> service.get("1.2.3", "component", "wanted", uriInfo).await().indefinitely())
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void emptyResponseFailsWithNotFound() throws Exception {
    when(modelsService.generateResponse(
            version, "component", List.of(), uriInfo, Integer.MAX_VALUE))
        .thenReturn(List.of());

    assertThatThrownBy(
            () -> service.get("1.2.3", "component", "wanted", uriInfo).await().indefinitely())
        .isInstanceOf(NotFoundException.class);
  }
}
