package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.yaml.models.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("VersionService Tests")
class VersionServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private VersionCacheProducer versionCacheProducer;
  @Mock private GraphEngineService graphEngineService;
  @Mock private AudVersionsService audVersionsService;
  @Mock private UriInfo uriInfo;

  private VersionService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service =
        new VersionService(audService, audApiConfig, versionCacheProducer, graphEngineService);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);
  }

  private void stubVersionMetadata() {
    when(audApiConfig.version()).thenReturn("v1");
    when(audApiConfig.platform()).thenReturn("yggy");
  }

  @Test
  @DisplayName("should return latest version with all fields populated")
  void getLatestVersionReturnsCompleteVersion() {
    stubVersionMetadata();

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version internalVersion =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(internalVersion.getVersionNumber()).thenReturn("1.0.0");

    AudRequirementsRepository requirementsRepo = mock(AudRequirementsRepository.class);
    when(requirementsRepo.getDataMD5Hash()).thenReturn("abc123hash");
    when(internalVersion.getAudRequirementsRepository()).thenReturn(requirementsRepo);

    when(audService.getCurrentVersion()).thenReturn(Uni.createFrom().item(internalVersion));

    Version result = service.getLatestVersion();

    assertThat(result).isNotNull();
    assertThat(result.getRequirementsVersion()).isEqualTo("1.0.0");
    assertThat(result.getApiVersion()).isEqualTo("v1");
    assertThat(result.getPlatform()).isEqualTo("yggy");
  }

  @Test
  @DisplayName("should return null when no latest version is available")
  void getLatestVersionReturnsNullWhenNoVersion() {
    when(audService.getCurrentVersion())
        .thenReturn(
            Uni.createFrom()
                .item((com.amadeus.aud.libraries.engine.data.models.core.internals.Version) null));

    Version result = service.getLatestVersion();

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("should return Optional with version when available")
  void maybeGetLatestVersionReturnsPresent() {
    stubVersionMetadata();

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version internalVersion =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(internalVersion.getVersionNumber()).thenReturn("2.0.0");
    when(internalVersion.getAudRequirementsRepository()).thenReturn(null);

    when(audService.getCurrentVersion()).thenReturn(Uni.createFrom().item(internalVersion));

    Optional<Version> result = service.maybeGetLatestVersion();

    assertThat(result).isPresent();
    assertThat(result.get().getRequirementsVersion()).isEqualTo("2.0.0");
  }

  @Test
  @DisplayName("should return empty Optional when no latest version is available")
  void maybeGetLatestVersionReturnsEmpty() {
    when(audService.getCurrentVersion())
        .thenReturn(
            Uni.createFrom()
                .item((com.amadeus.aud.libraries.engine.data.models.core.internals.Version) null));

    Optional<Version> result = service.maybeGetLatestVersion();

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should get version by number and return correct response")
  void getVersionByNumberReturnsVersion() throws Exception {
    stubVersionMetadata();

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version internalVersion =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(internalVersion.getVersionNumber()).thenReturn("1.5.0");
    when(internalVersion.getAudRequirementsRepository()).thenReturn(null);
    when(audService.getVersion()).thenReturn(Uni.createFrom().item(internalVersion));
    when(audService.getVersion()).thenReturn(Uni.createFrom().item(internalVersion));

    Response response =
        service
            .getVersion(
                (com.amadeus.aud.services.apis.models.http.requests.VersionLoadRequest) null)
            .await()
            .indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    Version body = (Version) response.getEntity();

    assertThat(body).isNotNull();
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getPlatform()).isEqualTo("yggy");
  }

  @Test
  @DisplayName("should return latest version as Uni")
  void getLatestVersionUniReturnsVersion() {
    stubVersionMetadata();

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version internalVersion =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(internalVersion.getVersionNumber()).thenReturn("3.0.0");
    when(internalVersion.getAudRequirementsRepository()).thenReturn(null);

    when(audService.getCurrentVersion()).thenReturn(Uni.createFrom().item(internalVersion));

    Version result = service.getLatestVersionUni().await().indefinitely();

    assertThat(result).isNotNull();
    assertThat(result.getRequirementsVersion()).isEqualTo("3.0.0");
  }

  @Test
  @DisplayName("should return loaded versions with correct structure")
  void getLoadedVersionsReturnsVersionsResponse() throws Exception {
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/versions"));

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version version1 =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(version1.getVersionNumber()).thenReturn("1.0.0");
    when(version1.getAudRequirementsRepository()).thenReturn(null);

    com.amadeus.aud.libraries.engine.data.models.core.internals.Version version2 =
        mock(com.amadeus.aud.libraries.engine.data.models.core.internals.Version.class);
    when(version2.getVersionNumber()).thenReturn("2.0.0");
    when(version2.getAudRequirementsRepository()).thenReturn(null);

    java.util.concurrent.ConcurrentHashMap<
            String, com.amadeus.aud.libraries.engine.data.models.core.internals.Version>
        versionMap = new java.util.concurrent.ConcurrentHashMap<>();
    versionMap.put("1.0.0", version1);
    versionMap.put("2.0.0", version2);

    @SuppressWarnings("unchecked")
    LoadingCache<String, com.amadeus.aud.libraries.engine.data.models.core.internals.Version>
        versionCache = mock(LoadingCache.class);

    org.mockito.Mockito.doReturn(versionMap).when(versionCache).asMap();
    org.mockito.Mockito.doReturn(version1).when(versionCache).get("1.0.0");
    org.mockito.Mockito.doReturn(version2).when(versionCache).get("2.0.0");

    when(versionCacheProducer.getCache()).thenReturn(versionCache);
    when(graphEngineService.getAvailableVersionNumbers())
        .thenReturn(Set.of("1.0.0", "2.0.0", "3.0.0"));

    Response response = service.getLoadedVersions(uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> body = objectMapper.readValue(bodyBytes, java.util.Map.class);

    assertThat(body).containsEntry("api_version", "v1");
    assertThat(body.get("count")).isNotNull();
    @SuppressWarnings("unchecked")
    java.util.Map<String, Integer> count = (java.util.Map<String, Integer>) body.get("count");
    assertThat(count).containsEntry("model_versions", 2).containsEntry("graph_versions", 3);
  }
}
