package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("mutiny-infrastructure")
class AudVersionsLocalFileServiceTest {
  private LocalFileService localFileService;
  private AudVersionsLocalFileService sut;

  @BeforeEach
  void setUp() {
    localFileService = mock(LocalFileService.class);
    sut = new AudVersionsLocalFileService(localFileService);
  }

  @Nested
  class CheckNewVersion {
    @Test
    void nullCurrentVersionReturnsLatestVersion() {
      when(localFileService.findLatestVersion()).thenReturn(Uni.createFrom().item("2.0.0"));

      String version = sut.checkNewVersion(null).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("2.0.0");
    }

    @Test
    void olderCurrentVersionReturnsLatestVersion() {
      when(localFileService.findLatestVersion()).thenReturn(Uni.createFrom().item("2.0.0"));

      String version =
          sut.checkNewVersion("1.9.9").await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("2.0.0");
    }

    @Test
    void equalOrNewerCurrentVersionProducesNoUpdate() {
      when(localFileService.findLatestVersion()).thenReturn(Uni.createFrom().item("2.0.0"));

      String equal =
          sut.checkNewVersion("2.0.0").await().atMost(ExternalServiceTestFixture.TIMEOUT);
      String newer =
          sut.checkNewVersion("2.1.0").await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(equal).isNull();
      assertThat(newer).isNull();
    }

    @Test
    void lookupFailureIsPropagated() {
      when(localFileService.findLatestVersion())
          .thenReturn(Uni.createFrom().failure(new IOException("version lookup failed")));

      assertThatThrownBy(
              () -> sut.checkNewVersion("1.0.0").await().atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("version lookup failed");
    }
  }

  @Nested
  class ListVersions {
    @Test
    void localVersionsAreAdaptedToPublicVersionResults() throws IOException {
      when(localFileService.findAllVersions()).thenReturn(List.of("2.0.0", "1.2.3"));

      List<SingleVersion> versions =
          sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(versions).extracting(SingleVersion::version).containsExactly("2.0.0", "1.2.3");
      assertThat(versions)
          .allSatisfy(
              (SingleVersion version) -> {
                assertThat(version.timestamp()).isNull();
                assertThat(version.links()).isZero();
              });
    }

    @Test
    void emptyLocalVersionsProduceEmptyResult() throws IOException {
      when(localFileService.findAllVersions()).thenReturn(List.of());

      List<SingleVersion> versions =
          sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(versions).isEmpty();
    }

    @Test
    void listingFailureIsPropagated() throws IOException {
      when(localFileService.findAllVersions()).thenThrow(new IOException("listing failed"));

      assertThatThrownBy(
              () -> sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(RuntimeException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("listing failed");
    }
  }

  @Test
  void getLatestVersionDelegatesToLocalFileService() {
    when(localFileService.findLatestVersion()).thenReturn(Uni.createFrom().item("3.0.0"));

    String version = sut.getLatestVersion().await().atMost(ExternalServiceTestFixture.TIMEOUT);

    assertThat(version).isEqualTo("3.0.0");
    verify(localFileService).findLatestVersion();
  }
}
