package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.SecurityMatrixResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
@DisplayName("SecurityMatrixService Tests")
class SecurityMatrixServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private SecurityMatrixService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service = new SecurityMatrixService(audService, audApiConfig);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(uriInfo.getAbsolutePath())
        .thenReturn(new URI("http://localhost:8080/api/security-matrices"));
    when(uriInfo.getRequestUri())
        .thenReturn(new URI("http://localhost:8080/api/security-matrices"));
  }

  @Test
  @DisplayName("should return security matrices response with zone types")
  void getSecurityMatricesWithValidVersion() throws Exception {
    ZoneType internal = mock(ZoneType.class);
    when(internal.getName()).thenReturn("Internal");

    ZoneType external = mock(ZoneType.class);
    when(external.getName()).thenReturn("External");

    ConcurrentHashMap<String, ZoneType> zoneTypesData = new ConcurrentHashMap<>();
    zoneTypesData.put("Internal", internal);
    zoneTypesData.put("External", external);

    ZoneTypesRepository zoneTypesRepository = mock(ZoneTypesRepository.class);
    when(zoneTypesRepository.getData()).thenReturn(zoneTypesData);
    when(zoneTypesRepository.isReady()).thenReturn(true);
    when(version.getZoneTypesRepository()).thenReturn(zoneTypesRepository);

    Response response = service.getSecurityMatrices(version, uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityMatrixResponse body = objectMapper.readValue(bodyBytes, SecurityMatrixResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getMatrices()).isNotNull();

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> matrices =
        (Map<String, Map<String, String>>) body.getMatrices();

    assertThat(matrices.keySet()).containsExactlyInAnyOrder("Internal", "External");
    assertThat(matrices.get("Internal").keySet()).contains("Internal", "External");
    assertThat(matrices.get("External").keySet()).contains("Internal", "External");
  }

  @Test
  @DisplayName("should retrieve version from audService when using string version")
  void getSecurityMatricesWithStringVersion() throws Exception {
    ZoneType internal = mock(ZoneType.class);
    when(internal.getName()).thenReturn("Internal");

    ConcurrentHashMap<String, ZoneType> zoneTypesData = new ConcurrentHashMap<>();
    zoneTypesData.put("Internal", internal);

    ZoneTypesRepository zoneTypesRepository = mock(ZoneTypesRepository.class);
    when(zoneTypesRepository.getData()).thenReturn(zoneTypesData);
    when(zoneTypesRepository.isReady()).thenReturn(true);
    when(version.getZoneTypesRepository()).thenReturn(zoneTypesRepository);

    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

    Response response = service.getSecurityMatrices("1.0.0", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityMatrixResponse body = objectMapper.readValue(bodyBytes, SecurityMatrixResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getMatrices()).isNotNull();
  }
}
