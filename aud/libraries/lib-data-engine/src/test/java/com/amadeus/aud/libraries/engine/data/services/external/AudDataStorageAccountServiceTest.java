package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import io.smallrye.mutiny.Uni;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

@ResourceLock("mutiny-infrastructure")
class AudDataStorageAccountServiceTest {
  @TempDir Path temporaryDirectory;

  private BlobServiceAsyncClient serviceClient;
  private BlobContainerAsyncClient containerClient;
  private BlobAsyncClient blobClient;
  private AudDataStorageAccountService sut;

  @BeforeEach
  void setUp() {
    serviceClient = mock(BlobServiceAsyncClient.class);
    containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);
    when(serviceClient.getBlobContainerAsyncClient("data-container")).thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    sut =
        new AudDataStorageAccountService(
            ExternalServiceTestFixture.dataConfig(temporaryDirectory),
            ExternalServiceTestFixture.modelConfig(),
            serviceClient);
  }

  @Nested
  class Load {
    @Test
    void yamlLoadUsesCompressedApiAndPlainReleaseAndMetadata() {
      BlobService blobService = mock(BlobService.class);
      CloseTrackingReader apiReader =
          new CloseTrackingReader(ExternalServiceTestFixture.CORE_MAPPING_YAML, false);
      when(blobService.getDecompressedBlobReader("core", "1.2.3/api.yml.gz"))
          .thenReturn(Uni.createFrom().item(apiReader));
      when(blobService.getBlobUni("core", "1.2.3/release.yml", false))
          .thenReturn(Uni.createFrom().item(ExternalServiceTestFixture.CORE_RELEASE_YAML));
      when(blobService.getBlobUni("core", "1.2.3/metadata.yml", false))
          .thenReturn(Uni.createFrom().item(""));

      AudDataRepository repository =
          sut.load("1.2.3", "core", blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(repository.getData().get(V1ScopeReleaseNote.class)).hasSize(1);
      assertThat(apiReader.isClosed()).isTrue();
      verify(blobService).getDecompressedBlobReader("core", "1.2.3/api.yml.gz");
      verify(blobService).getBlobUni("core", "1.2.3/release.yml", false);
      verify(blobService).getBlobUni("core", "1.2.3/metadata.yml", false);
    }

    @Test
    void jsonLoadUsesThreeDecompressedReadersAndClosesThem() {
      BlobService blobService = mock(BlobService.class);
      CloseTrackingReader apiReader =
          new CloseTrackingReader(ExternalServiceTestFixture.CORE_MAPPING_JSON, false);
      CloseTrackingReader releaseReader =
          new CloseTrackingReader(ExternalServiceTestFixture.CORE_RELEASE_JSON, false);
      CloseTrackingReader metadataReader = new CloseTrackingReader("[]", false);
      when(blobService.getDecompressedBlobReader("core", "2.0.0/api.json.gz"))
          .thenReturn(Uni.createFrom().item(apiReader));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/release.json.gz"))
          .thenReturn(Uni.createFrom().item(releaseReader));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/metadata.json.gz"))
          .thenReturn(Uni.createFrom().item(metadataReader));

      AudDataRepository repository =
          sut.load("2.0.0", "core", blobService, true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(repository.getData().get(V1ScopeReleaseNote.class)).hasSize(1);
      assertThat(apiReader.isClosed()).isTrue();
      assertThat(releaseReader.isClosed()).isTrue();
      assertThat(metadataReader.isClosed()).isTrue();
    }

    @Test
    void readerCloseFailureDoesNotDiscardProcessedRepository() {
      BlobService blobService = mock(BlobService.class);
      CloseTrackingReader apiReader =
          new CloseTrackingReader(ExternalServiceTestFixture.CORE_MAPPING_JSON, true);
      when(blobService.getDecompressedBlobReader("core", "2.0.0/api.json.gz"))
          .thenReturn(Uni.createFrom().item(apiReader));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/release.json.gz"))
          .thenReturn(Uni.createFrom().item(new BufferedReader(new StringReader("[]"))));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/metadata.json.gz"))
          .thenReturn(Uni.createFrom().item(new BufferedReader(new StringReader("[]"))));

      AudDataRepository repository =
          sut.load("2.0.0", "core", blobService, true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(apiReader.isClosed()).isTrue();
    }

    @Test
    void blobFailureIsPropagated() {
      BlobService blobService = mock(BlobService.class);
      when(blobService.getDecompressedBlobReader("core", "2.0.0/api.json.gz"))
          .thenReturn(Uni.createFrom().failure(new IOException("api missing")));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/release.json.gz"))
          .thenReturn(Uni.createFrom().item(new BufferedReader(new StringReader("[]"))));
      when(blobService.getDecompressedBlobReader("core", "2.0.0/metadata.json.gz"))
          .thenReturn(Uni.createFrom().item(new BufferedReader(new StringReader("[]"))));

      assertThatThrownBy(
              () ->
                  sut.load("2.0.0", "core", blobService, true)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("api missing");
    }

    @Test
    void initObjectsWithFallbackRetriesYamlAfterJsonFailure() throws IOException {
      BlobAsyncClient jsonApi = failedBlob("json unavailable");
      BlobAsyncClient jsonRelease = failedBlob("json unavailable");
      BlobAsyncClient jsonMetadata = failedBlob("json unavailable");
      BlobAsyncClient yamlApi =
          downloadedBlob(BinaryData.fromBytes(gzip(ExternalServiceTestFixture.CORE_MAPPING_YAML)));
      BlobAsyncClient yamlRelease =
          downloadedBlob(BinaryData.fromString(ExternalServiceTestFixture.CORE_RELEASE_YAML));
      BlobAsyncClient yamlMetadata = downloadedBlob(BinaryData.fromString(""));
      when(containerClient.getBlobAsyncClient("1.2.3/api.json.gz")).thenReturn(jsonApi);
      when(containerClient.getBlobAsyncClient("1.2.3/release.json.gz")).thenReturn(jsonRelease);
      when(containerClient.getBlobAsyncClient("1.2.3/metadata.json.gz")).thenReturn(jsonMetadata);
      when(containerClient.getBlobAsyncClient("1.2.3/api.yml.gz")).thenReturn(yamlApi);
      when(containerClient.getBlobAsyncClient("1.2.3/release.yml")).thenReturn(yamlRelease);
      when(containerClient.getBlobAsyncClient("1.2.3/metadata.yml")).thenReturn(yamlMetadata);

      AudDataRepository repository =
          sut.initObjectsWithFallback("1.2.3").await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(repository.getData().get(V1ScopeReleaseNote.class)).hasSize(1);
    }
  }

  @Nested
  class ProcessResponses {
    @Test
    void nullAndEmptyYamlReadersProduceKnownEmptyGroups() throws IOException {
      BufferedReader emptyReader = new BufferedReader(new StringReader(""));

      AudDataRepository repository =
          sut.processResponses("empty", new BufferedReader[] {null, emptyReader}, false);

      assertThat(repository.getDataVersion()).isEqualTo("empty");
      assertThat(repository.getData()).isNotEmpty();
      assertThat(repository.getData().values())
          .allSatisfy(elements -> assertThat(elements).isEmpty());
      assertThat(repository.getDataMD5Hash()).isNull();
      assertThat(repository.getDataSHA256Hash()).isNull();
    }

    @Test
    void laterFragmentReplacesEarlierGroupOfSameModelClass() throws IOException {
      String secondMapping =
          ExternalServiceTestFixture.CORE_MAPPING_JSON.replace("json-mapping", "second-mapping");
      BufferedReader first =
          new BufferedReader(new StringReader(ExternalServiceTestFixture.CORE_MAPPING_JSON));
      BufferedReader second = new BufferedReader(new StringReader(secondMapping));

      AudDataRepository repository =
          sut.processResponses("2.0.0", new BufferedReader[] {first, second}, true);

      ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data =
          repository.getData();
      assertThat(data.get(V1ScopeVersionMapping.class))
          .singleElement()
          .extracting(ModelElement::getMetadata)
          .extracting(V1Metadata::getName)
          .isEqualTo("second-mapping");
    }

    @Test
    void malformedYamlIsPropagated() {
      BufferedReader malformed = new BufferedReader(new StringReader("---\nkind: ["));

      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new BufferedReader[] {malformed}, false))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("while parsing a flow node");
    }

    @Test
    void invalidJsonAttemptsYamlFallbackAndPropagatesParsingFailure() {
      BufferedReader malformed = new BufferedReader(new StringReader("not-json-or-yaml: ["));

      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new BufferedReader[] {malformed}, true))
          .isInstanceOf(RuntimeException.class)
          .hasRootCauseInstanceOf(IOException.class);
    }
  }

  @Nested
  class Save {
    @Test
    void saveUsesVersionedNameAndGzipCompression() {
      BlockBlobItem savedItem = blockBlobItem("single");
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

      BlockBlobItem result =
          sut.save("3.0.0", "content", "custom.json.gz")
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(containerClient).getBlobAsyncClient(nameCaptor.capture());
      assertThat(nameCaptor.getValue()).isEqualTo("3.0.0/custom.json.gz");
    }

    @Test
    void saveAllFilesUsesJsonAndYamlNames() {
      BlockBlobItem savedItem = blockBlobItem("all");
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

      BlockBlobItem[] jsonResults =
          sut.saveAllFiles("3.0.0", "api", "release", "metadata", true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);
      BlockBlobItem[] yamlResults =
          sut.saveAllFiles("4.0.0", "api", "release", "metadata", false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(jsonResults).containsExactly(savedItem, savedItem, savedItem);
      assertThat(yamlResults).containsExactly(savedItem, savedItem, savedItem);
      verify(containerClient, org.mockito.Mockito.times(6))
          .getBlobAsyncClient(nameCaptor.capture());
      assertThat(nameCaptor.getAllValues())
          .containsExactlyInAnyOrder(
              "3.0.0/api.json.gz",
              "3.0.0/release.json.gz",
              "3.0.0/metadata.json.gz",
              "4.0.0/api.yml.gz",
              "4.0.0/release.yml",
              "4.0.0/metadata.yml");
    }

    @Test
    void saveAllFilesPropagatesUploadFailure() {
      when(blobClient.upload(any(BinaryData.class), eq(true)))
          .thenReturn(Mono.error(new IllegalStateException("storage unavailable")));

      assertThatThrownBy(
              () ->
                  sut.saveAllFiles("3.0.0", "api", "release", "metadata", true)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("storage unavailable");
    }
  }

  private static BlobAsyncClient downloadedBlob(BinaryData content) {
    BlobAsyncClient client = mock(BlobAsyncClient.class);
    when(client.downloadContent()).thenReturn(Mono.just(content));
    return client;
  }

  private static BlobAsyncClient failedBlob(String message) {
    BlobAsyncClient client = mock(BlobAsyncClient.class);
    when(client.downloadContent()).thenReturn(Mono.error(new IOException(message)));
    return client;
  }

  private static byte[] gzip(String content) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
      gzipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    }
    return outputStream.toByteArray();
  }

  private static BlockBlobItem blockBlobItem(String etag) {
    return new BlockBlobItem(
        etag, OffsetDateTime.parse("2026-01-01T00:00:00Z"), null, Boolean.TRUE, null, null, null);
  }

  private static final class CloseTrackingReader extends BufferedReader {
    private final boolean failOnClose;
    private boolean closed;
    private int closeCount;

    private CloseTrackingReader(String content, boolean failOnClose) {
      super(new StringReader(content));
      this.failOnClose = failOnClose;
    }

    @Override
    public void close() throws IOException {
      closed = true;
      closeCount++;
      super.close();
      if (failOnClose && closeCount > 1) {
        throw new IOException("close failed");
      }
    }

    private boolean isClosed() {
      return closed;
    }
  }
}
