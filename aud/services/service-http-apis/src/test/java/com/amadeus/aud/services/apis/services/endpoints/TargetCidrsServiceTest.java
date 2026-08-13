package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.SpokeCIDRs;
import com.amadeus.aud.libraries.yaml.models.TargetCIDRsResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class TargetCidrsServiceTest {

  private static final String TEST_URI = "http://localhost:8080/api/target/test-target/cidrs";
  private static final String TEST_TARGET_NAME = "test-target";
  private static final String TEST_VERSION = "1.0.0";
  private static final String API_VERSION = "v1";

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private Version version;
  @Mock private UriInfo uriInfo;

  private TargetCidrsService service;

  @BeforeEach
  void setUp() throws Exception {
    service = spy(new TargetCidrsService(audService, audApiConfig));

    // Use lenient() for stubs that might not be used in all tests
    lenient().when(version.getVersionNumber()).thenReturn(TEST_VERSION);
    lenient().when(audApiConfig.version()).thenReturn(API_VERSION);
    lenient().doNothing().when(service).checkRepository(any(), anyString(), anyString());

    // Mock getOkResponse to return a proper Response with the entity
    // Use doAnswer().when() pattern to avoid calling the method during setup
    lenient()
        .doAnswer(
            (InvocationOnMock invocation) -> {
              Object entity = invocation.getArgument(0);
              return Response.status(Response.Status.OK).entity(entity).build();
            })
        .when(service)
        .getOkResponse(any());

    URI mockUri = new URI(TEST_URI);
    lenient().when(uriInfo.getAbsolutePath()).thenReturn(mockUri);
    lenient().when(uriInfo.getRequestUri()).thenReturn(mockUri);
  }

  @Nested
  @DisplayName("getTargetCidrs with Version")
  class GetTargetCidrsWithVersion {

    @Test
    @DisplayName("should return target CIDRs with landing zone CIDRs")
    void shouldReturnTargetCidrsWithLandingZoneCidrs() {
      // Given
      Target target =
          createMockTarget(Arrays.asList("10.0.0.0/16", "10.1.0.0/16"), createMockLandingZones());

      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Uni<Response> responseUni = service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo);
      Response response = responseUni.await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getEntity()).isInstanceOf(TargetCIDRsResponse.class);

      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getTargetCidrs())
          .containsExactlyInAnyOrder("10.0.0.0/16", "10.1.0.0/16");
      assertThat(responseEntity.getLandingzoneCidrs()).hasSize(2);
      assertThat(responseEntity.getVersion()).isEqualTo(TEST_VERSION);
      assertThat(responseEntity.getApiVersion()).isEqualTo(API_VERSION);
      assertThat(responseEntity.getUrl()).isEqualTo(TEST_URI);
    }

    @Test
    @DisplayName("should handle target with no CIDRs")
    void shouldHandleTargetWithNoCidrs() {
      // Given
      Target target = createMockTarget(null, Collections.emptySet());
      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getTargetCidrs()).isEmpty();
      assertThat(responseEntity.getLandingzoneCidrs()).isEmpty();
    }

    @Test
    @DisplayName("should handle target with empty CIDRs list")
    void shouldHandleTargetWithEmptyCidrsList() {
      // Given
      Target target = createMockTarget(Collections.emptyList(), Collections.emptySet());
      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getTargetCidrs()).isEmpty();
      assertThat(responseEntity.getLandingzoneCidrs()).isEmpty();
    }

    @Test
    @DisplayName("should throw NotFoundException when target not found")
    void shouldThrowNotFoundExceptionWhenTargetNotFound() {
      // Given
      TargetsRepository mockRepo = mockTargetRepository(null);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When/Then
      // The exception gets wrapped in CompletionException by Unchecked.supplier()
      Throwable exception =
          assertThatThrownBy(
                  () ->
                      service
                          .getTargetCidrs(version, TEST_TARGET_NAME, uriInfo)
                          .await()
                          .indefinitely())
              .isInstanceOf(Exception.class)
              .actual();

      // Verify the cause is NotFoundException
      assertThat(exception)
          .satisfiesAnyOf(
              ex -> assertThat(ex).isInstanceOf(NotFoundException.class),
              ex -> assertThat(ex.getCause()).isInstanceOf(NotFoundException.class));
    }

    @Test
    @DisplayName("should handle landing zones with null CIDRs")
    void shouldHandleLandingZonesWithNullCidrs() {
      // Given
      LandingZone lzWithNullCidrs = createMockLandingZone("lz-null", "Legacy", null);
      Target target = createMockTarget(List.of("10.0.0.0/16"), Set.of(lzWithNullCidrs));

      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getLandingzoneCidrs()).isEmpty();
    }

    @Test
    @DisplayName("should handle landing zones with empty CIDRs list")
    void shouldHandleLandingZonesWithEmptyCidrsList() {
      // Given
      LandingZone lzWithEmptyCidrs =
          createMockLandingZone("lz-empty", "Legacy", Collections.emptyList());
      Target target = createMockTarget(List.of("10.0.0.0/16"), Set.of(lzWithEmptyCidrs));

      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getLandingzoneCidrs()).isEmpty();
    }

    @Test
    @DisplayName("should handle landing zones with null landing zone type")
    void shouldHandleLandingZonesWithNullType() {
      // Given
      LandingZone lz = createMockLandingZone("lz-no-type", null, List.of("192.168.0.0/24"));
      Target target = createMockTarget(List.of("10.0.0.0/16"), Set.of(lz));

      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getLandingzoneCidrs()).hasSize(1);
      SpokeCIDRs spokeCidrs = responseEntity.getLandingzoneCidrs().getFirst();
      assertThat(spokeCidrs.getLandingzone()).isEqualTo("lz-no-type");
      assertThat(spokeCidrs.getLandingzonetype()).isNull();
      assertThat(spokeCidrs.getCidrs()).containsExactly("192.168.0.0/24");
    }

    @Test
    @DisplayName("should return versioned URL correctly")
    void shouldReturnVersionedUrlCorrectly() throws Exception {
      // Given
      Target target = createMockTarget(List.of("10.0.0.0/16"), Collections.emptySet());
      TargetsRepository mockRepo = mockTargetRepository(target);
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      URI baseUri = new URI("http://localhost:8080/api/target/test-target/cidrs");
      when(uriInfo.getRequestUri()).thenReturn(baseUri);
      when(uriInfo.getAbsolutePath()).thenReturn(baseUri);

      // When
      Response response =
          service.getTargetCidrs(version, TEST_TARGET_NAME, uriInfo).await().indefinitely();

      // Then
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getUrlVersion())
          .contains("/version/" + TEST_VERSION)
          .contains("/target/test-target/cidrs");
    }
  }

  @Nested
  @DisplayName("Async getTargetCidrs")
  class AsyncGetTargetCidrs {

    @Test
    @DisplayName("should call audService.getVersion with correct version")
    void shouldCallGetVersionWithCorrectVersion() {
      // Given
      String versionString = "2.0.0";
      Target target = createMockTarget(List.of("10.0.0.0/16"), Collections.emptySet());
      TargetsRepository mockRepo = mockTargetRepository(target);

      when(audService.getVersion(versionString)).thenReturn(Uni.createFrom().item(version));
      when(version.getTargetsRepository()).thenReturn(mockRepo);

      // When
      Uni<Response> result = service.getTargetCidrs(versionString, TEST_TARGET_NAME, uriInfo);
      Response response = result.await().indefinitely();

      // Then
      assertThat(response.getStatus()).isEqualTo(200);
      TargetCIDRsResponse responseEntity = (TargetCIDRsResponse) response.getEntity();
      assertThat(responseEntity.getVersion()).isEqualTo(TEST_VERSION);
    }
  }

  // Helper methods
  private TargetsRepository mockTargetRepository(Target target) {
    TargetsRepository repo = mock(TargetsRepository.class);
    when(repo.get(TEST_TARGET_NAME)).thenReturn(target);
    return repo;
  }

  private Target createMockTarget(List<String> cidrs, Set<LandingZone> landingZones) {
    Target target = mock(Target.class);
    when(target.getCidrs()).thenReturn(cidrs);
    when(target.getLandingZones()).thenReturn(landingZones);
    return target;
  }

  private Set<LandingZone> createMockLandingZones() {
    Set<LandingZone> landingZones = new HashSet<>();
    landingZones.add(createMockLandingZone("lz-1", "Legacy", List.of("192.168.1.0/24")));
    landingZones.add(
        createMockLandingZone("lz-2", "Cloud", Arrays.asList("192.168.2.0/24", "192.168.3.0/24")));
    return landingZones;
  }

  private LandingZone createMockLandingZone(String name, String typeName, List<String> cidrs) {
    LandingZone lz = mock(LandingZone.class);
    lenient().when(lz.getName()).thenReturn(name);
    lenient().when(lz.getCidrs()).thenReturn(cidrs);

    if (typeName != null) {
      LandingZoneType lzType = mock(LandingZoneType.class);
      lenient().when(lzType.getName()).thenReturn(typeName);
      lenient().when(lz.getLandingZoneType()).thenReturn(lzType);
    } else {
      lenient().when(lz.getLandingZoneType()).thenReturn(null);
    }

    return lz;
  }
}
