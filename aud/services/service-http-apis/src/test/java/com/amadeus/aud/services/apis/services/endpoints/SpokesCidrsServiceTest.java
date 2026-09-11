package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.SpokesCIDRsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
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
@DisplayName("SpokesCidrsService Tests")
class SpokesCidrsServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private SpokesCidrsService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service = new SpokesCidrsService(audService, audApiConfig);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(version.getVersionNumber()).thenReturn("1.0.0");
  }

  private void stubResponseMetadata() throws Exception {
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/spokes-cidrs"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/spokes-cidrs"));
  }

  @Test
  @DisplayName("should return spokes CIDRs for Azure Spoke landing zones")
  void getSpokesCidrsWithValidTarget() throws Exception {
    stubResponseMetadata();

    LandingZone azureSpoke = mock(LandingZone.class);
    when(azureSpoke.getName()).thenReturn("azure-spoke-1");
    when(azureSpoke.getLandingZoneTypeName()).thenReturn("Default Azure Spoke");
    when(azureSpoke.getCidrs()).thenReturn(List.of("10.0.0.0/16", "10.1.0.0/16"));

    LandingZone otherType = mock(LandingZone.class);
    when(otherType.getLandingZoneTypeName()).thenReturn("Other Type");

    Target target = mock(Target.class);
    when(target.getLandingZones()).thenReturn(Set.of(azureSpoke, otherType));

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    when(targetsRepository.get("target-1")).thenReturn(target);
    when(targetsRepository.isReady()).thenReturn(true);
    when(version.getTargetsRepository()).thenReturn(targetsRepository);

    Response response = service.getSpokesCidrs(version, "target-1", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SpokesCIDRsResponse body = objectMapper.readValue(bodyBytes, SpokesCIDRsResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getSpokesCidrs()).isNotNull();

    assertThat(body.getSpokesCidrs()).hasSize(1);
    assertThat(body.getSpokesCidrs().get(0).getLandingzone()).isEqualTo("azure-spoke-1");
    assertThat(body.getSpokesCidrs().get(0).getCidrs())
        .containsExactly("10.0.0.0/16", "10.1.0.0/16");
  }

  @Test
  @DisplayName("should throw NotFoundException when target does not exist")
  void getSpokesCidrsWithNonexistentTarget() {
    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    when(targetsRepository.get("nonexistent")).thenReturn(null);
    when(targetsRepository.isReady()).thenReturn(true);
    when(version.getTargetsRepository()).thenReturn(targetsRepository);

    Throwable thrown =
        catchThrowable(
            () -> service.getSpokesCidrs(version, "nonexistent", uriInfo).await().indefinitely());

    assertThat(thrown).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("should retrieve version from audService when using string version")
  void getSpokesCidrsWithStringVersion() throws Exception {
    stubResponseMetadata();

    LandingZone azureSpoke = mock(LandingZone.class);
    when(azureSpoke.getName()).thenReturn("azure-spoke-1");
    when(azureSpoke.getLandingZoneTypeName()).thenReturn("Default Azure Spoke");
    when(azureSpoke.getCidrs()).thenReturn(List.of("10.0.0.0/16"));
    Target target = mock(Target.class);
    when(target.getLandingZones()).thenReturn(Set.of(azureSpoke));

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    when(targetsRepository.get("target-1")).thenReturn(target);
    when(targetsRepository.isReady()).thenReturn(true);
    when(version.getTargetsRepository()).thenReturn(targetsRepository);

    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

    Response response = service.getSpokesCidrs("1.0.0", "target-1", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SpokesCIDRsResponse body = objectMapper.readValue(bodyBytes, SpokesCIDRsResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getSpokesCidrs()).hasSize(1);
  }
}
