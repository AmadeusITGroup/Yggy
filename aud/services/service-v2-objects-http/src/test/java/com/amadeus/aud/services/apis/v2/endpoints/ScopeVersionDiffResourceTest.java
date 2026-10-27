package com.amadeus.aud.services.apis.v2.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.v2.objects.service.ScopeVersionDiffService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScopeVersionDiffResource Unit Tests")
class ScopeVersionDiffResourceTest {

  @Test
  void getScopeVersionDiffReturnsOkResponse() {
    // Arrange
    AudService audService = mock(AudService.class);
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    ScopeVersionDiffService scopeVersionDiffService = mock(ScopeVersionDiffService.class);
    ScopeVersionDiffResource sut =
        new ScopeVersionDiffResource(audService, audVersionsService, scopeVersionDiffService);

    List<SingleVersion> versions =
        List.of(new SingleVersion("1.0.0", null, 0), new SingleVersion("2.0.0", null, 0));
    when(audVersionsService.listAllVersions()).thenReturn(Uni.createFrom().item(versions));

    Version oldVer = mock(Version.class);
    Version newVer = mock(Version.class);
    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(oldVer));
    when(audService.getVersion("2.0.0")).thenReturn(Uni.createFrom().item(newVer));

    Response<List<ScopeVersionDiffItem>> expectedResponse =
        new Response<>(List.of(), new Metadata().withPagination(new Pagination(0, 1, 10)));
    when(scopeVersionDiffService.getScopeVersionDiffResponse(eq(oldVer), eq(newVer), eq(1), eq(10)))
        .thenReturn(Uni.createFrom().item(expectedResponse));

    // Act
    jakarta.ws.rs.core.Response response =
        sut.getScopeVersionDiff("1.0.0", "2.0.0", 1, 10).await().indefinitely();

    // Assert
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getEntity()).isEqualTo(expectedResponse);
  }

  @Test
  void getScopeVersionDiffFailsWhenOldVersionMissing() {
    // Arrange
    AudService audService = mock(AudService.class);
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    ScopeVersionDiffService scopeVersionDiffService = mock(ScopeVersionDiffService.class);
    ScopeVersionDiffResource sut =
        new ScopeVersionDiffResource(audService, audVersionsService, scopeVersionDiffService);

    List<SingleVersion> versions = List.of(new SingleVersion("1.0.0", null, 0));
    when(audVersionsService.listAllVersions()).thenReturn(Uni.createFrom().item(versions));

    // Act & Assert
    assertThatThrownBy(() -> sut.getScopeVersionDiff(null, "1.0.0", 1, 10).await().indefinitely())
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("oldVersion: a valid version is required");
  }

  @Test
  void getScopeVersionDiffFailsWhenVersionDoesNotExist() {
    // Arrange
    AudService audService = mock(AudService.class);
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    ScopeVersionDiffService scopeVersionDiffService = mock(ScopeVersionDiffService.class);
    ScopeVersionDiffResource sut =
        new ScopeVersionDiffResource(audService, audVersionsService, scopeVersionDiffService);

    List<SingleVersion> versions = List.of(new SingleVersion("1.0.0", null, 0));
    when(audVersionsService.listAllVersions()).thenReturn(Uni.createFrom().item(versions));

    // Act & Assert
    assertThatThrownBy(
            () -> sut.getScopeVersionDiff("9.9.9", "1.0.0", 1, 10).await().indefinitely())
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("oldVersion: a valid version is required");
  }

  @Test
  void getScopeVersionDiffFailsWhenNoVersionsAvailable() {
    // Arrange
    AudService audService = mock(AudService.class);
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    ScopeVersionDiffService scopeVersionDiffService = mock(ScopeVersionDiffService.class);
    ScopeVersionDiffResource sut =
        new ScopeVersionDiffResource(audService, audVersionsService, scopeVersionDiffService);

    when(audVersionsService.listAllVersions()).thenReturn(Uni.createFrom().item(List.of()));

    // Act & Assert
    assertThatThrownBy(
            () -> sut.getScopeVersionDiff("latest", "latest", 1, 10).await().indefinitely())
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessageContaining("No versions available");
  }
}
