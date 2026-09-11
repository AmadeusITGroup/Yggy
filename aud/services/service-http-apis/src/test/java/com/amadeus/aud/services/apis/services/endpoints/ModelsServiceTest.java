package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModelsService Tests")
class ModelsServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;

  private ModelsService service;
  private Version version;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = EndpointTestObjectMapper.get();
    service = new ModelsService(audService, audApiConfig, objectMapper);
    version = mock(Version.class);
  }

  private void stubResponseMetadata() throws Exception {
    when(audApiConfig.version()).thenReturn("v1");
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/models"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/models"));
    MultivaluedMap<String, String> emptyParams = new MultivaluedHashMap<>();
    when(uriInfo.getQueryParameters()).thenReturn(emptyParams);
  }

  private GenericResponse<?> extractResponseBody(Response response) throws Exception {
    byte[] bodyBytes = (byte[]) response.getEntity();
    return objectMapper.readValue(bodyBytes, GenericResponse.class);
  }

  @Nested
  @DisplayName("Repository Data Retrieval Tests")
  class RepositoryDataTests {

    @Test
    @DisplayName("should return all models from repository")
    void shouldReturnAllModels() throws Exception {
      stubResponseMetadata();

      ConcurrentHashMap<String, BomElement> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("app-1", createApplicationId("app-1", "Application 1"));
      repositoryData.put("app-2", createApplicationId("app-2", "Application 2"));

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(repositoryData);
      when(version.getRepository("application")).thenReturn(repository);

      Response response =
          service.getResponse(version, "application", Collections.emptyList(), uriInfo, 100);
      GenericResponse<?> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getApiVersion()).isEqualTo("v1");
      assertThat(body.getVersion()).isEqualTo("1.0.0");
      assertThat((List<?>) body.getValues()).hasSize(2);
    }

    @Test
    @DisplayName("should return empty list when repository is empty")
    void shouldReturnEmptyList() throws Exception {
      stubResponseMetadata();

      ConcurrentHashMap<String, BomElement> repositoryData = new ConcurrentHashMap<>();

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(repositoryData);
      when(version.getRepository("application")).thenReturn(repository);

      Response response =
          service.getResponse(version, "application", Collections.emptyList(), uriInfo, 100);
      GenericResponse<?> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat((List<?>) body.getValues()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("should throw NotFoundException for unknown object type")
    void shouldThrowForUnknownType() {
      when(version.getRepository("unknown")).thenReturn(null);
      when(version.getRepositoryNames()).thenReturn(Collections.emptySet());

      assertThatThrownBy(
              () -> service.getResponse(version, "unknown", Collections.emptyList(), uriInfo, 100))
          .isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("should throw BadRequestException for non-positive limit")
    void shouldThrowForNonPositiveLimit(int limit) {
      assertThatThrownBy(
              () ->
                  service.getResponse(
                      version, "application", Collections.emptyList(), uriInfo, limit))
          .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException for a null version")
    void shouldThrowForNullVersion() {
      assertThatThrownBy(
              () ->
                  service.generateResponse(
                      null, "application", Collections.emptyList(), uriInfo, 10))
          .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    @DisplayName("should describe populated and empty repositories for an unknown type")
    void shouldDescribeKnownTypesForUnknownType() {
      ConcurrentRepository<BomElement> populatedRepository =
          repositoryWith(createApplicationId("app-1", "Application 1"));
      ConcurrentRepository<BomElement> emptyRepository = repositoryWith();
      when(version.getRepository("unknown")).thenReturn(null);
      when(version.getRepositoryNames()).thenReturn(Set.of("populated", "empty"));
      when(version.getRepository("populated")).thenReturn(populatedRepository);
      when(version.getRepository("empty")).thenReturn(emptyRepository);

      assertThatThrownBy(
              () ->
                  service.generateResponse(
                      version, "unknown", Collections.emptyList(), uriInfo, 10))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("Unknown object type")
          .hasMessageContaining("\"type\":\"empty\"")
          .hasMessageContaining("\"type\":\"populated\"")
          .hasMessageContaining("\"fields\":[");
    }
  }

  @Nested
  @DisplayName("Limit Parameter Tests")
  class LimitTests {

    @Test
    @DisplayName("should respect limit parameter")
    void shouldRespectLimit() throws Exception {
      stubResponseMetadata();

      ConcurrentHashMap<String, BomElement> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("app-1", createApplicationId("app-1", "App 1"));
      repositoryData.put("app-2", createApplicationId("app-2", "App 2"));
      repositoryData.put("app-3", createApplicationId("app-3", "App 3"));

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(repositoryData);
      when(version.getRepository("application")).thenReturn(repository);

      Response response =
          service.getResponse(version, "application", Collections.emptyList(), uriInfo, 2);
      GenericResponse<?> body = extractResponseBody(response);

      assertThat((List<?>) body.getValues()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("Query and Selection Tests")
  class QueryAndSelectionTests {

    @Test
    @DisplayName("should match wildcard and alternative where values while ignoring malformed keys")
    void shouldMatchWildcardAndAlternativeWhereValues() {
      stubRepository(
          "application",
          createApplicationId("app-1", "Alpha Application"),
          createApplicationId("app-2", "Beta Application"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.add("where[name]", "No Match");
      queryParams.add("where[name]", "Alpha*");
      queryParams.add("where[name", "Beta*");
      queryParams.add("other]", "Beta*");
      when(uriInfo.getQueryParameters()).thenReturn(queryParams);

      List<JsonNode> values =
          service.generateResponse(version, "application", Collections.emptyList(), uriInfo, 10);

      assertThat(values)
          .singleElement()
          .satisfies(node -> assertThat(node.get("name").asText()).isEqualTo("Alpha Application"));
    }

    @Test
    @DisplayName("should require every where condition and reject missing fields")
    void shouldRequireEveryWhereConditionAndRejectMissingFields() {
      stubRepository("application", createApplicationId("app-1", "Alpha Application"));
      MultivaluedMap<String, String> matchingParams = new MultivaluedHashMap<>();
      matchingParams.putSingle("where[name]", "Alpha*");
      matchingParams.putSingle("where[type]", "applicationid");
      when(uriInfo.getQueryParameters()).thenReturn(matchingParams);

      List<JsonNode> matchingValues =
          service.generateResponse(version, "application", Collections.emptyList(), uriInfo, 10);

      MultivaluedMap<String, String> missingFieldParams = new MultivaluedHashMap<>();
      missingFieldParams.putSingle("where[missing]", "*");
      when(uriInfo.getQueryParameters()).thenReturn(missingFieldParams);
      List<JsonNode> missingFieldValues =
          service.generateResponse(version, "application", Collections.emptyList(), uriInfo, 10);

      assertThat(matchingValues).hasSize(1);
      assertThat(missingFieldValues).isEmpty();
    }

    @Test
    @DisplayName("should omit unselected and missing fields")
    void shouldReturnOnlySelectedExistingFields() {
      stubRepository("application", createApplicationId("app-1", "Alpha Application"));
      when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

      List<JsonNode> values =
          service.generateResponse(version, "application", List.of("name", "missing"), uriInfo, 10);

      assertThat(values)
          .singleElement()
          .satisfies(
              (JsonNode node) -> {
                assertThat(node.properties()).hasSize(1);
                assertThat(node.get("name").asText()).isEqualTo("Alpha Application");
                assertThat(node.has("missing")).isFalse();
              });
    }

    @Test
    @DisplayName("should reject where patterns that do not match")
    void shouldRejectNonMatchingWherePattern() {
      stubRepository("application", createApplicationId("app-1", "Alpha Application"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("where[name]", "Beta*");
      when(uriInfo.getQueryParameters()).thenReturn(queryParams);

      List<JsonNode> values =
          service.generateResponse(version, "application", Collections.emptyList(), uriInfo, 10);

      assertThat(values).isEmpty();
    }
  }

  @Test
  @DisplayName("should resolve the version and return JSON metadata through get")
  void shouldResolveVersionAndReturnJsonMetadataThroughGet() throws Exception {
    stubResponseMetadata();
    stubRepository("application", createApplicationId("app-1", "Application 1"));
    when(audService.getVersion("current"))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(version));

    Response response =
        service
            .get("application", "current", uriInfo, Collections.emptyList(), 10)
            .await()
            .indefinitely();
    GenericResponse<?> body = extractResponseBody(response);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getUrl()).isEqualTo("http://localhost:8080/api/models");
    assertThat(body.getUrlVersion()).contains("1.0.0");
    assertThat((List<?>) body.getValues()).hasSize(1);
  }

  private void stubRepository(String objectType, BomElement... elements) {
    ConcurrentRepository<BomElement> repository = repositoryWith(elements);
    when(version.getRepository(objectType)).thenReturn(repository);
  }

  private ConcurrentRepository<BomElement> repositoryWith(BomElement... elements) {
    ConcurrentHashMap<String, BomElement> data = new ConcurrentHashMap<>();
    for (int index = 0; index < elements.length; index++) {
      data.put(Integer.toString(index), elements[index]);
    }
    ConcurrentRepository<BomElement> repository = mock();
    when(repository.getData()).thenReturn(data);
    return repository;
  }

  private ApplicationId createApplicationId(String id, String name) {
    ApplicationId appId = new ApplicationId();
    V1Metadata metadatav1 = new V1Metadata();
    metadatav1.setName(name);
    V1ApplicationID metadata = new V1ApplicationID();
    metadata.setApiVersion("v1");
    metadata.setKind("ApplicationID");
    metadata.setMetadata(metadatav1);

    appId.setData(metadata);

    return appId;
  }
}
