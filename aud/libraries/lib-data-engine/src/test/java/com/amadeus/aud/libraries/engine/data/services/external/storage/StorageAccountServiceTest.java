package com.amadeus.aud.libraries.engine.data.services.external.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlockBlobItem;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

@ResourceLock("mutiny-infrastructure")
class StorageAccountServiceTest {
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  private BlobServiceAsyncClient serviceClient;
  private BlobContainerAsyncClient containerClient;
  private BlobAsyncClient blobClient;
  private StorageAccountService sut;

  @BeforeEach
  void setUp() {
    serviceClient = mock(BlobServiceAsyncClient.class);
    containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);
    when(serviceClient.getBlobContainerAsyncClient("data")).thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient(any(String.class))).thenReturn(blobClient);
    sut = new StorageAccountService(serviceClient);
  }

  @Nested
  class InitObjects {
    @Test
    void explicitVersionDelegatesDirectlyToProcessor() {
      AtomicReference<BlobService> receivedBlobService = new AtomicReference<>();
      StorageAccountDataProcessor<String> processor =
          (String version, String container, BlobService blobService, boolean jsonPreferred) -> {
            receivedBlobService.set(blobService);
            return Uni.createFrom().item(version + ":" + container + ":" + jsonPreferred);
          };

      String result = sut.initObjects("1.2.3", "data", processor, true).await().atMost(TIMEOUT);

      assertThat(result).isEqualTo("1.2.3:data:true");
      assertThat(receivedBlobService).hasValue(sut.getBlobService());
      verify(serviceClient, never()).getBlobContainerAsyncClient("data");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "latest")
    void latestAliasesSelectHighestListedVersion(String requestedVersion) {
      when(containerClient.listBlobs())
          .thenReturn(
              pagedFlux(
                  List.of(
                      new BlobItem().setName("1.9.9/api.yml"),
                      new BlobItem().setName("2.0.0/api.yml"),
                      new BlobItem().setName("invalid/api.yml"),
                      new BlobItem().setName("2.0.0/metadata.yml"))));
      StorageAccountDataProcessor<String> processor =
          (String version, String container, BlobService blobService, boolean jsonPreferred) ->
              Uni.createFrom().item(version + ":" + jsonPreferred);

      String result =
          sut.initObjects(requestedVersion, "data", processor, false).await().atMost(TIMEOUT);

      assertThat(result).isEqualTo("2.0.0:false");
      verify(containerClient).listBlobs();
    }

    @Test
    void emptyListingDelegatesWithNullVersion() {
      when(containerClient.listBlobs()).thenReturn(pagedFlux(List.of()));
      AtomicReference<Boolean> invoked = new AtomicReference<>(false);
      StorageAccountDataProcessor<String> processor =
          (String version, String container, BlobService blobService, boolean jsonPreferred) -> {
            invoked.set(true);
            return Uni.createFrom().item(version);
          };

      String result = sut.loadLatest("data", processor, false).await().atMost(TIMEOUT);

      assertThat(result).isNull();
      assertThat(invoked).hasValue(true);
    }

    @Test
    void listingFailureIsPropagated() {
      when(containerClient.listBlobs())
          .thenReturn(failedPagedFlux(new IllegalStateException("listing failed")));
      StorageAccountDataProcessor<String> processor =
          (String version, String container, BlobService blobService, boolean jsonPreferred) ->
              Uni.createFrom().item("unused");

      assertThatThrownBy(() -> sut.loadLatest("data", processor, false).await().atMost(TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("listing failed");
    }

    @Test
    void processorFailureIsPropagated() {
      StorageAccountDataProcessor<String> processor =
          (String version, String container, BlobService blobService, boolean jsonPreferred) ->
              Uni.createFrom().failure(new IllegalArgumentException("processing failed"));

      assertThatThrownBy(() -> sut.load("1.0.0", "data", processor, false).await().atMost(TIMEOUT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("processing failed");
    }
  }

  @Nested
  class Save {
    @Test
    void versionAndBlobNameAreCombinedAndCompressed() {
      BlockBlobItem savedItem = blockBlobItem();
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

      BlockBlobItem result =
          sut.save("1.2.3", "data", "content", "api.json.gz", true).await().atMost(TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(containerClient).getBlobAsyncClient(nameCaptor.capture());
      assertThat(nameCaptor.getValue()).isEqualTo("1.2.3/api.json.gz");
    }

    @Test
    void uploadFailureIsPropagated() {
      when(blobClient.upload(any(BinaryData.class), eq(true)))
          .thenReturn(Mono.error(new IllegalStateException("save failed")));

      assertThatThrownBy(
              () -> sut.save("1.2.3", "data", "content", "api.yml", false).await().atMost(TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("save failed");
    }
  }

  @Test
  void processingHelpersAreExposedAndCalculateCombinedHashes() {
    BaseRepository<String> repository = new BaseRepository<>("data");

    sut.calculateHashes(repository, new String[] {"first", null, "second"});

    assertThat(repository.getDataMD5Hash()).hasSize(32);
    assertThat(repository.getDataSHA256Hash()).hasSize(64);
    assertThat(sut.getBlobService()).isNotNull();
    assertThat(sut.getDataProcessingService()).isNotNull();
  }

  private static PagedFlux<BlobItem> pagedFlux(List<BlobItem> items) {
    HttpRequest request = new HttpRequest(HttpMethod.GET, "https://example.invalid/container");
    PagedResponse<BlobItem> response =
        new PagedResponseBase<Void, BlobItem>(request, 200, new HttpHeaders(), items, null, null);
    return new PagedFlux<>(() -> Mono.just(response));
  }

  private static PagedFlux<BlobItem> failedPagedFlux(Throwable failure) {
    return new PagedFlux<>(() -> Mono.error(failure));
  }

  private static BlockBlobItem blockBlobItem() {
    return new BlockBlobItem(
        "etag", OffsetDateTime.parse("2026-01-01T00:00:00Z"), null, Boolean.TRUE, null, null, null);
  }
}
