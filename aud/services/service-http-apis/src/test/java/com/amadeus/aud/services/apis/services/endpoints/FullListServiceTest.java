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
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstanceSpec;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.TargetTypeMixin;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.ZoneInstanceMixin;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FullListService Tests")
@Execution(ExecutionMode.SAME_THREAD)
class FullListServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private ObjectMapper objectMapper;
  private FullListService service;

  @BeforeEach
  void setUp() {
    objectMapper = EndpointTestObjectMapper.get().copy();
    objectMapper.addMixIn(TargetType.class, TargetTypeMixin.class);
    objectMapper.addMixIn(ZoneInstance.class, ZoneInstanceMixin.class);
    service = new TestableFullListService(audService, audApiConfig, objectMapper);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractResponseBody(Response response) throws Exception {
    byte[] bodyBytes = (byte[]) response.getEntity();
    return objectMapper.readValue(bodyBytes, Map.class);
  }

  private JsonNode extractJsonBody(Response response) throws Exception {
    return objectMapper.readTree((byte[]) response.getEntity());
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

  private void setupCommonMocks() throws Exception {
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/fulllist"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/fulllist"));
  }

  private void stubRepository(String objectType, BomElement... elements) {
    ConcurrentMap<String, BomElement> data = new ConcurrentHashMap<>();
    for (int index = 0; index < elements.length; index++) {
      data.put(Integer.toString(index), elements[index]);
    }
    stubRepositoryData(objectType, data);
  }

  private void stubRepositoryData(
      String objectType, ConcurrentMap<String, BomElement> repositoryData) {
    ConcurrentRepository<BomElement> repository = mock();
    when(repository.getData()).thenReturn(repositoryData);
    when(version.getRepository(objectType)).thenReturn(repository);
  }

  @Nested
  @DisplayName("Repository Access Tests - Java Format")
  class RepositoryAccessTests {

    @Test
    @DisplayName("should return full list of services from repository")
    void shouldReturnFullListOfServices() throws Exception {
      setupCommonMocks();

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

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body).containsEntry("api_version", "v1").containsEntry("version", "1.0.0");

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).hasSize(3);

      @SuppressWarnings({"unchecked"})
      List<String> serviceNames =
          values.stream()
              .map(item -> (String) ((Map<String, Object>) item.get("metadata")).get("name"))
              .toList();
      assertThat(serviceNames).contains("service-prod", "service-dev", "service-test");
    }

    @Test
    @DisplayName("should throw NotFoundException when repository type does not exist")
    void shouldThrowNotFoundExceptionWhenRepositoryTypeNotExists() {
      when(version.getRepository("unknown-type")).thenReturn(null);

      try {
        service.getFullListJavaFormat(version, "unknown-type", uriInfo).await().indefinitely();
        assertThat(true).as("Should have thrown NotFoundException").isFalse();
      } catch (NotFoundException e) {
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("should handle empty repository gracefully")
    void shouldHandleEmptyRepository() throws Exception {
      setupCommonMocks();

      ConcurrentHashMap<String, Service> emptyRepository = new ConcurrentHashMap<>();

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) emptyRepository;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).isEmpty();
    }

    @Test
    @DisplayName("should handle null repository data gracefully")
    void shouldHandleNullRepositoryData() throws Exception {
      setupCommonMocks();

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      when(servicesRepository.getData()).thenReturn(null);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).isEmpty();
    }
  }

  @Nested
  @DisplayName("Response Structure Tests - Java Format")
  class ResponseStructureTests {

    @Test
    @DisplayName("should include metadata in response (version, api_version, urls)")
    void shouldIncludeMetadata() throws Exception {
      setupCommonMocks();

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

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body).containsEntry("api_version", "v1").containsEntry("version", "1.0.0");
      assertThat(body.get("url")).isNotNull();
      assertThat(body.get("url_version")).isNotNull();
    }
  }

  @Nested
  @DisplayName("Object Type Handling Tests - Java Format")
  class ObjectTypeHandlingTests {

    @Test
    @DisplayName("should handle different object types case-insensitively")
    void shouldHandleObjectTypesCaseInsensitive() throws Exception {
      setupCommonMocks();

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
          service.getFullListJavaFormat(version, "Service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).hasSize(1);
    }

    @Test
    @DisplayName("should handle multiple items of same type")
    void shouldHandleMultipleItems() throws Exception {
      setupCommonMocks();

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

      Response response =
          service.getFullListJavaFormat(version, "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).hasSize(10);
    }
  }

  @Nested
  @DisplayName("String Version Parameter Tests - Java Format")
  class StringVersionParameterTests {

    @Test
    @DisplayName("should handle String version parameter and resolve via AudService")
    void shouldHandleStringVersionParameter() throws Exception {
      setupCommonMocks();

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

      Response response =
          service.getFullListJavaFormat("1.0.0", "service", uriInfo).await().indefinitely();
      Map<String, Object> body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Java Format Tests")
  class JavaFormatTests {

    @Test
    @DisplayName("should return response in Java format")
    void shouldReturnJavaFormat() throws Exception {
      setupCommonMocks();

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
      assertThat(body).containsEntry("api_version", "v1");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> values = (List<Map<String, Object>>) body.get("values");
      assertThat(values).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Python Format Tests - Without Query Parameters")
  class PythonFormatNoParamsTests {

    @Test
    @DisplayName("should return full list without query parameters")
    void shouldReturnFullListWithoutQueryParams() throws Exception {
      setupCommonMocks();

      Service service1 = createService("service-prod");
      Service service2 = createService("service-dev");

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-prod", service1);
      repositoryData.put("service-dev", service2);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, null)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getEntity()).isNotNull();
    }

    @Test
    @DisplayName("should handle empty repository in Python format")
    void shouldHandleEmptyRepositoryPythonFormat() throws Exception {
      setupCommonMocks();

      ConcurrentHashMap<String, Service> emptyRepository = new ConcurrentHashMap<>();

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) emptyRepository;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, null)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should throw NotFoundException for non-existent repository in Python format")
    void shouldThrowNotFoundForInvalidTypeInPythonFormat() {
      when(version.getRepository("unknown")).thenReturn(null);

      try {
        service
            .getFullListPythonFormatFiltered(version, "unknown", uriInfo, null)
            .await()
            .indefinitely();
        assertThat(true).as("Should have thrown NotFoundException").isFalse();
      } catch (NotFoundException e) {
        assertThat(e).isNotNull();
      }
    }
  }

  @Nested
  @DisplayName("Python Format Tests - With Query Parameters")
  class PythonFormatWithQueryParamsTests {

    @Test
    @DisplayName("should return full list with query parameters (no filters)")
    void shouldReturnFullListWithEmptyQueryParams() throws Exception {
      setupCommonMocks();

      Service service1 = createService("service-prod");
      Service service2 = createService("service-dev");

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-prod", service1);
      repositoryData.put("service-dev", service2);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should apply SNOW pagination filters")
    void shouldApplySnowPaginationFilters() throws Exception {
      setupCommonMocks();

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      for (int i = 1; i <= 5; i++) {
        Service svc = createService("service-" + i);
        repositoryData.put("service-" + i, svc);
      }

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("SNOWStartFilter", "1");
      queryParams.putSingle("SNOWNumberFilter", "2");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getEntity()).isNotNull();
    }

    @Test
    @DisplayName("should handle field filters with query parameters")
    void shouldApplyFieldFilters() throws Exception {
      setupCommonMocks();

      Service service1 = createService("service-prod");
      Service service2 = createService("service-dev");

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-prod", service1);
      repositoryData.put("service-dev", service2);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("name", "service-prod");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should combine SNOW pagination with field filters")
    void shouldCombineSnowPaginationWithFieldFilters() throws Exception {
      setupCommonMocks();

      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      for (int i = 1; i <= 5; i++) {
        Service svc = createService("service-" + i);
        repositoryData.put("service-" + i, svc);
      }

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("SNOWStartFilter", "0");
      queryParams.putSingle("SNOWNumberFilter", "3");
      queryParams.putSingle("name", "service-1");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("should handle null query parameters")
    void shouldHandleNullQueryParameters() throws Exception {
      setupCommonMocks();

      Service service1 = createService("service-test");
      ConcurrentHashMap<String, Service> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("service-test", service1);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> servicesRepository = mock(ConcurrentRepository.class);
      @SuppressWarnings("unchecked")
      ConcurrentMap<String, BomElement> castedData =
          (ConcurrentMap<String, BomElement>) (Object) repositoryData;
      when(servicesRepository.getData()).thenReturn(castedData);
      when(version.getRepository("service")).thenReturn(servicesRepository);

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, null)
              .await()
              .indefinitely();

      assertThat(response.getStatus()).isEqualTo(200);
    }
  }

  @Nested
  @DisplayName("Public Python Format Overload Tests")
  class PublicPythonFormatOverloadTests {

    @Test
    @DisplayName("should resolve versions through both public Python overloads")
    void shouldResolveVersionsThroughBothPublicPythonOverloads() throws Exception {
      setupCommonMocks();
      stubRepository("service", createService("service-one"));
      when(audService.getVersion("current")).thenReturn(Uni.createFrom().item(version));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("name", "SERVICE-ONE");

      Response unfilteredResponse =
          service.getFullListPythonFormat("current", "service", uriInfo).await().indefinitely();
      Response filteredResponse =
          service
              .getFullListPythonFormat("current", "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(unfilteredResponse).path("service")).hasSize(1);
      assertThat(extractJsonBody(filteredResponse).path("service")).hasSize(1);
    }

    @Test
    @DisplayName("should transform null repository data to an empty typed array")
    void shouldTransformNullRepositoryDataToEmptyTypedArray() throws Exception {
      setupCommonMocks();
      stubRepositoryData("service", null);

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, null)
              .await()
              .indefinitely();
      JsonNode body = extractJsonBody(response);

      assertThat(body.has("values")).isFalse();
      assertThat(body.path("service")).isEmpty();
    }
  }

  @Nested
  @DisplayName("Python Filter Validation Tests")
  class PythonFilterValidationTests {

    @Test
    @DisplayName("should reject a filter field that does not exist")
    void shouldRejectUnknownFilterField() {
      stubRepository("service", createService("service-one"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("missing", "value");

      assertThatThrownBy(
              () ->
                  service
                      .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
                      .await()
                      .indefinitely())
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Invalid filter field");
    }

    @Test
    @DisplayName("should allow filters against an empty repository")
    void shouldAllowFiltersAgainstEmptyRepository() throws Exception {
      setupCommonMocks();
      stubRepository("service");
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("missing", "value");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(response).path("service")).isEmpty();
    }

    @Test
    @DisplayName("should match field values case-insensitively against any accepted value")
    void shouldMatchAnyAcceptedValueCaseInsensitively() throws Exception {
      setupCommonMocks();
      stubRepository("service", createService("service-one"), createService("service-two"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.add("name", "missing");
      queryParams.add("name", "SERVICE-TWO");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();
      JsonNode values = extractJsonBody(response).path("service");

      assertThat(values).hasSize(1);
      assertThat(values.get(0).path("name").asText()).isEqualTo("service-two");
    }

    @Test
    @DisplayName("should extract snake-case, complex, enum, and boolean fields")
    void shouldExtractSupportedFilterFieldValues() throws Exception {
      setupCommonMocks();
      Service matchingService = createService("matching-service");
      matchingService.setServiceNowId("snow-42");
      matchingService.setProtocol(V1ServiceSpec.ProtocolEnum.TCP);
      matchingService.setApplicationId(createApplicationId("application-one"));
      stubRepository("service", matchingService);

      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("service_now_id", "SNOW-42");
      queryParams.putSingle("protocol", "tcp");
      queryParams.putSingle("applicationId", "APPLICATION-ONE");
      queryParams.putSingle("private", "false");
      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(response).path("service")).hasSize(1);
    }

    @Test
    @DisplayName("should reject null and non-extractable field values")
    void shouldRejectNullAndNonExtractableFieldValues() throws Exception {
      setupCommonMocks();
      Service serviceWithNullValue = createService("null-value");
      stubRepository("service", serviceWithNullValue);
      MultivaluedMap<String, String> nullValueParams = new MultivaluedHashMap<>();
      nullValueParams.putSingle("serviceNowId", "anything");

      Response nullValueResponse =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, nullValueParams)
              .await()
              .indefinitely();

      TestBomElement nonExtractable = new TestBomElement("complex", new Object());
      stubRepository("fixture", nonExtractable);
      MultivaluedMap<String, String> complexValueParams = new MultivaluedHashMap<>();
      complexValueParams.putSingle("value", "anything");
      Response complexValueResponse =
          service
              .getFullListPythonFormatFiltered(version, "fixture", uriInfo, complexValueParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(nullValueResponse).path("service")).isEmpty();
      assertThat(extractJsonBody(complexValueResponse).path("fixture")).isEmpty();
    }

    @Test
    @DisplayName("should ignore empty filters and the legacy SNOW filter")
    void shouldIgnoreEmptyAndLegacySnowFilters() throws Exception {
      setupCommonMocks();
      stubRepository("service", createService("service-one"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.put("nullFilter", null);
      queryParams.put("emptyFilter", Collections.emptyList());
      queryParams.putSingle("SNOWFilter", "ignored");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(response).path("service")).hasSize(1);
    }
  }

  @Nested
  @DisplayName("SNOW Pagination Validation Tests")
  class SnowPaginationValidationTests {

    @Test
    @DisplayName("should require both pagination values before applying a page")
    void shouldRequireBothPaginationValues() throws Exception {
      setupCommonMocks();
      stubRepository(
          "service",
          createService("service-one"),
          createService("service-two"),
          createService("service-three"));
      MultivaluedMap<String, String> startOnly = new MultivaluedHashMap<>();
      startOnly.putSingle("SNOWStartFilter", "1");
      MultivaluedMap<String, String> numberOnly = new MultivaluedHashMap<>();
      numberOnly.putSingle("SNOWNumberFilter", "1");

      Response startOnlyResponse =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, startOnly)
              .await()
              .indefinitely();
      Response numberOnlyResponse =
          service
              .getFullListPythonFormatFiltered(version, "service", uriInfo, numberOnly)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(startOnlyResponse).path("service")).hasSize(3);
      assertThat(extractJsonBody(numberOnlyResponse).path("service")).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({"SNOWStartFilter,not-a-number", "SNOWNumberFilter,1.5"})
    @DisplayName("should reject non-integer pagination values")
    void shouldRejectNonIntegerPaginationValues(String parameter, String value) {
      stubRepository("service", createService("service-one"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle(parameter, value);

      assertThatThrownBy(
              () ->
                  service
                      .getFullListPythonFormatFiltered(version, "service", uriInfo, queryParams)
                      .await()
                      .indefinitely())
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining(parameter + " must be an integer");
    }

    @Test
    @DisplayName("should ignore SNOW pagination for unsupported object types")
    void shouldIgnoreSnowPaginationForUnsupportedObjectType() throws Exception {
      setupCommonMocks();
      stubRepository("applicationId", createApplicationId("application-one"));
      MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
      queryParams.putSingle("SNOWStartFilter", "invalid");
      queryParams.putSingle("SNOWNumberFilter", "invalid");

      Response response =
          service
              .getFullListPythonFormatFiltered(version, "APPLICATION_ID", uriInfo, queryParams)
              .await()
              .indefinitely();

      assertThat(extractJsonBody(response).path("applicationid")).hasSize(1);
    }
  }

  @Nested
  @DisplayName("Child Enrichment Tests")
  class ChildEnrichmentTests {

    @Test
    @DisplayName("should enrich target types with null, empty, and populated target arrays")
    void shouldEnrichTargetTypesWithChildArrays() throws Exception {
      setupCommonMocks();
      TargetType nullTargets = createTargetType("null-targets");
      TargetType emptyTargets = createTargetType("empty-targets");
      emptyTargets.setTargets(Collections.emptySet());
      TargetType populatedTargets = createTargetType("populated-targets");
      Target target = new Target();
      target.setName("target-one");
      populatedTargets.setTargets(Set.of(target));
      stubRepository("targetType", nullTargets, emptyTargets, populatedTargets);

      Response response =
          service
              .getFullListPythonFormatFiltered(
                  version, "target_type", uriInfo, new MultivaluedHashMap<>())
              .await()
              .indefinitely();
      JsonNode values = extractJsonBody(response).path("targettype");
      List<Integer> childCounts = new ArrayList<>();
      values.forEach(item -> childCounts.add(item.path("targets").size()));

      assertThat(childCounts).containsExactlyInAnyOrder(0, 0, 1);
      assertThat(values.toString()).contains("target-one");
    }

    @Test
    @DisplayName("should enrich zone instances with null, empty, and populated subnet arrays")
    void shouldEnrichZoneInstancesWithChildArrays() throws Exception {
      setupCommonMocks();
      ZoneInstance nullSubnets = createZoneInstance("null-subnets");
      ZoneInstance emptySubnets = createZoneInstance("empty-subnets");
      emptySubnets.setSubnetInstances(Collections.emptySet());
      ZoneInstance populatedSubnets = createZoneInstance("populated-subnets");
      SubnetInstance subnet = new SubnetInstance();
      subnet.setName("subnet-one");
      populatedSubnets.setSubnetInstances(Set.of(subnet));
      stubRepository("zoneInstance", nullSubnets, emptySubnets, populatedSubnets);

      Response response =
          service
              .getFullListPythonFormatFiltered(
                  version, "zone_instance", uriInfo, new MultivaluedHashMap<>())
              .await()
              .indefinitely();
      JsonNode values = extractJsonBody(response).path("zone_instance");
      List<Integer> childCounts = new ArrayList<>();
      values.forEach(item -> childCounts.add(item.path("subnet_instances").size()));

      assertThat(childCounts).containsExactlyInAnyOrder(0, 0, 1);
      assertThat(values.toString()).contains("subnet-one");
    }
  }

  private ApplicationId createApplicationId(String name) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    V1ApplicationID model = new V1ApplicationID();
    model.setMetadata(metadata);
    return new ApplicationId(model);
  }

  private TargetType createTargetType(String name) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    V1TargetType model = new V1TargetType();
    model.setMetadata(metadata);
    return new TargetType(model);
  }

  private ZoneInstance createZoneInstance(String name) {
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName(name);
    metadata.setLandingzone("landing-zone");
    V1ZoneInstanceSpec spec = new V1ZoneInstanceSpec();
    spec.setZoneType("zone-type");
    V1ZoneInstance model = new V1ZoneInstance();
    model.setMetadata(metadata);
    model.setSpec(spec);
    return new ZoneInstance(model);
  }

  private static final class TestableFullListService extends FullListService {
    private TestableFullListService(
        AudService audService, AudApiConfig audApiConfig, ObjectMapper objectMapper) {
      super(audService, audApiConfig);
      setObjectMapper(objectMapper);
      injectObjectMapper(objectMapper);
    }
  }

  private static final class TestBomElement implements BomElement {
    private final String id;
    private final Object value;

    private TestBomElement(String id, Object value) {
      this.id = id;
      this.value = value;
    }

    @Override
    public String id() {
      return id;
    }

    public Object getValue() {
      return value;
    }
  }
}
