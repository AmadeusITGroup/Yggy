package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import reactor.core.publisher.Mono;

@ResourceLock("mutiny-infrastructure")
class AudVersionsStorageAccountServiceTest {
  private BlobContainerAsyncClient containerClient;
  private BlobAsyncClient blobClient;
  private AudVersionsStorageAccountService sut;

  @BeforeEach
  void setUp() {
    BlobServiceAsyncClient serviceClient = mock(BlobServiceAsyncClient.class);
    containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);
    when(serviceClient.getBlobContainerAsyncClient("version-container"))
        .thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient("latest")).thenReturn(blobClient);
    when(containerClient.getBlobAsyncClient("versions_history.jsonl")).thenReturn(blobClient);
    sut =
        new AudVersionsStorageAccountService(
            ExternalServiceTestFixture.dataConfig(Path.of(".")), serviceClient);
  }

  @Nested
  class CheckNewVersion {
    @Test
    void nullCurrentVersionReturnsLatest() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString("2.0.0")));

      String version = sut.checkNewVersion(null).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("2.0.0");
    }

    @Test
    void olderCurrentVersionReturnsLatest() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString("2.0.0")));

      String version =
          sut.checkNewVersion("1.9.9").await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("2.0.0");
    }

    @Test
    void equalOrNewerCurrentVersionProducesNoUpdate() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString("2.0.0")));

      String equal =
          sut.checkNewVersion("2.0.0").await().atMost(ExternalServiceTestFixture.TIMEOUT);
      String newer =
          sut.checkNewVersion("2.1.0").await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(equal).isNull();
      assertThat(newer).isNull();
    }

    @Test
    void storageFailureIsPropagated() {
      when(blobClient.downloadContent())
          .thenReturn(Mono.error(new IOException("version unavailable")));

      assertThatThrownBy(
              () -> sut.checkNewVersion("1.0.0").await().atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("version unavailable");
    }
  }

  @Nested
  class GetAndLoadLatest {
    @Test
    void getLatestVersionUsesConfiguredContainerAndTrimsContent() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString(" 3.0.0\n")));

      String version = sut.getLatestVersion().await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("3.0.0");
      verify(containerClient).getBlobAsyncClient("latest");
    }

    @Test
    void loadUsesSuppliedBlobService() {
      BlobService blobService = mock(BlobService.class);
      when(blobService.getBlobUni("versions", "latest", false))
          .thenReturn(Uni.createFrom().item("4.0.0"));

      String version =
          sut.load("ignored", "versions", blobService, true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(version).isEqualTo("4.0.0");
      verify(blobService).getBlobUni("versions", "latest", false);
    }

    @Test
    void processResponsesTrimsFirstValueAndHandlesEmptyOrNullValues() {
      String value = sut.processResponses("ignored", new String[] {" 5.0.0\n", "6.0.0"}, true);
      String empty = sut.processResponses("ignored", new String[0], false);
      String nullValue = sut.processResponses("ignored", new String[] {null}, false);

      assertThat(value).isEqualTo("5.0.0");
      assertThat(empty).isNull();
      assertThat(nullValue).isNull();
    }
  }

  @Nested
  class ListAllVersions {
    @Test
    void validLinesAreReturnedWhileBlankAndMalformedLinesAreIgnored() {
      String content =
          """
          {"version":"2.0.0","timestamp":"2026-04-08T08:43:32Z","links":5}

          not-json
          {"version":"1.2.3","timestamp":"2026-04-07T08:43:32Z"}
          """;
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString(content)));

      List<SingleVersion> versions =
          sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(versions).hasSize(2);
      assertThat(versions.get(0).version()).isEqualTo("2.0.0");
      assertThat(versions.get(0).timestamp())
          .isEqualTo(OffsetDateTime.parse("2026-04-08T08:43:32Z"));
      assertThat(versions.get(0).links()).isEqualTo(5);
      assertThat(versions.get(1).version()).isEqualTo("1.2.3");
      assertThat(versions.get(1).links()).isZero();
      verify(containerClient).getBlobAsyncClient("versions_history.jsonl");
    }

    @Test
    void emptyHistoryProducesEmptyList() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString("\n  \n")));

      List<SingleVersion> versions =
          sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(versions).isEmpty();
    }

    @Test
    void historyDownloadFailureIsPropagated() {
      when(blobClient.downloadContent())
          .thenReturn(Mono.error(new IOException("history unavailable")));

      assertThatThrownBy(
              () -> sut.listAllVersions().await().atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("history unavailable");
    }
  }
}
