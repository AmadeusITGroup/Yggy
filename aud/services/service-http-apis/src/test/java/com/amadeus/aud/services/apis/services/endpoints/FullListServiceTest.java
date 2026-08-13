package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FullListService Tests")
@Execution(ExecutionMode.SAME_THREAD)
@Disabled
class FullListServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private FullListService service;

  @BeforeEach
  void setUp() throws Exception {
    service = new FullListService(audService, audApiConfig);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/fulllist"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/fulllist"));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractResponseBody(Response response) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    byte[] bodyBytes = (byte[]) response.getEntity();
    return objectMapper.readValue(bodyBytes, Map.class);
  }

  private Service createService(String name) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);

    V1ServiceSpec spec = new V1ServiceSpec();

    V1Service v1Service = new V1Service();
    v1Service.setMetadata(metadata);
    v1Service.setSpec(spec);

    return new Service(v1Service);
  }

  @Nested
  @DisplayName("Repository Access Tests")
  class RepositoryAccessTests {

    @Test
    @DisplayName("should return full list of services from repository")
    void shouldReturnFullListOfServices() throws Exception {
      Service service1 = createService("service-prod");
      Service service2 = createService("service-dev");
      Service service3 = createService("service-test");

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-prod", service1);
      repositoryData.put("service-dev", service2);
      repositoryData.put("service-test", service3);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.get("apiVersion")).isEqualTo("v1");
      assertThat(body.get("version")).isEqualTo("1.0.0");

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).hasSize(3);

      @SuppressWarnings({"unchecked"})
      List<String> serviceNames =
          data.stream()
              .map(item -> (String) ((Map<String, Object>) item.get("metadata")).get("name"))
              .toList();
      assertThat(serviceNames).contains("service-prod", "service-dev", "service-test");
    }

    @Test
    @DisplayName("should throw NotFoundException when repository type does not exist")
    void shouldThrowNotFoundExceptionWhenRepositoryTypeNotExists() {
      when(version.getRepository("unknown-type")).thenReturn(null);

      try {
        service.getFullList(version, "unknown-type", uriInfo).await().indefinitely();
        assertThat(true).as("Should have thrown NotFoundException").isFalse();
      } catch (NotFoundException e) {
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("should handle empty repository gracefully")
    void shouldHandleEmptyRepository() throws Exception {
      ConcurrentHashMap<String, Service> emptyRepository = new ConcurrentHashMap<>();

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) emptyRepository;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).isEmpty();
    }

    @Test
    @DisplayName("should handle null repository data gracefully")
    void shouldHandleNullRepositoryData() throws Exception {
      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      when(servicesRepository.getData()).thenReturn(null);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).isEmpty();
    }
  }

  @Nested
  @DisplayName("Response Structure Tests")
  class ResponseStructureTests {

    @Test
    @DisplayName("should include metadata in response (version, apiVersion, urls)")
    void shouldIncludeMetadata() throws Exception {
      Service svc = createService("test-service");
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("test-service", svc);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.get("apiVersion")).isEqualTo("v1");
      assertThat(body.get("version")).isEqualTo("1.0.0");
      assertThat(body.get("url")).isNotNull();
      assertThat(body.get("urlVersion")).isNotNull();
    }
  }

  @Nested
  @DisplayName("Object Type Handling Tests")
  class ObjectTypeHandlingTests {

    @Test
    @DisplayName("should handle different object types case-insensitively")
    void shouldHandleObjectTypesCaseInsensitive() throws Exception {
      Service svc = createService("service-one");
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-one", svc);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "Service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).hasSize(1);
    }

    @Test
    @DisplayName("should handle multiple items of same type")
    void shouldHandleMultipleItems() throws Exception {
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      for (int i = 1; i <= 10; i++) {
        repositoryData.put("service-" + i, createService("service-" + i));
      }

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response = service.getFullList(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).hasSize(10);
    }
  }

  @Nested
  @DisplayName("String Version Parameter Tests")
  class StringVersionParameterTests {

    @Test
    @DisplayName("should handle String version parameter and resolve via AudService")
    void shouldHandleStringVersionParameter() throws Exception {
      Service svc = createService("service-prod");
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-prod", svc);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);
      when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

      Response response = service.getFullList("1.0.0", "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Java Format Tests")
  class JavaFormatTests {

    @Test
    @DisplayName("should return response in Java format")
    void shouldReturnJavaFormat() throws Exception {
      Service svc = createService("service-one");
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-one", svc);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.get("apiVersion")).isEqualTo("v1");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
      assertThat(data).hasSize(1);
    }
  }
}
