package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ModelsService Tests")
class ModelsServiceTest {

  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;

  private ModelsService service;
  private Version version;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    service = new ModelsService(null, audApiConfig, objectMapper);

    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    version = mock(Version.class);
    when(audApiConfig.version()).thenReturn("v1");
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/models"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/models"));
    @SuppressWarnings("unchecked")
    jakarta.ws.rs.core.MultivaluedMap<String, String> emptyParams =
        mock(jakarta.ws.rs.core.MultivaluedMap.class);
    when(emptyParams.entrySet()).thenReturn(Collections.emptySet());
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

    @Test
    @DisplayName("should throw BadRequestException for negative limit")
    void shouldThrowForNegativeLimit() {
      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(new ConcurrentHashMap<>());
      when(version.getRepository("application")).thenReturn(repository);

      assertThatThrownBy(
              () ->
                  service.getResponse(version, "application", Collections.emptyList(), uriInfo, -1))
          .isInstanceOf(BadRequestException.class);
    }
  }

  @Nested
  @DisplayName("Limit Parameter Tests")
  class LimitTests {

    @Test
    @DisplayName("should respect limit parameter")
    void shouldRespectLimit() throws Exception {
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
