package com.amadeus.aud.libraries.engine.data.services.external.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

@ResourceLock("mutiny-infrastructure")
class BlobServiceTest {
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  @TempDir Path temporaryDirectory;

  private BlobServiceAsyncClient serviceClient;
  private BlobContainerAsyncClient containerClient;
  private BlobAsyncClient blobClient;
  private BlobService sut;

  @BeforeEach
  void setUp() {
    serviceClient = mock(BlobServiceAsyncClient.class);
    containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);
    when(serviceClient.getBlobContainerAsyncClient("container")).thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient("path/blob")).thenReturn(blobClient);
    sut = new BlobService(serviceClient);
  }

  @Nested
  class GetBlob {
    @Test
    void plainBlobIsDecodedAndTrimmed() {
      when(blobClient.downloadContent()).thenReturn(Mono.just(BinaryData.fromString("  value \n")));

      String content = sut.getBlobUni("container", "path/blob", false).await().atMost(TIMEOUT);

      assertThat(content).isEqualTo("value");
      verify(serviceClient).getBlobContainerAsyncClient("container");
      verify(containerClient).getBlobAsyncClient("path/blob");
    }

    @Test
    void compressedBlobIsDecompressedWithoutTrimming() throws IOException {
      when(blobClient.downloadContent())
          .thenReturn(Mono.just(BinaryData.fromBytes(gzip(" value \n"))));

      String content = sut.getBlobUni("container", "path/blob", true).await().atMost(TIMEOUT);

      assertThat(content).isEqualTo(" value \n");
    }

    @Test
    void downloadFailureIsPropagated() {
      when(blobClient.downloadContent())
          .thenReturn(Mono.error(new IllegalStateException("blob not found")));

      assertThatThrownBy(
              () -> sut.getBlobUni("container", "path/blob", false).await().atMost(TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("blob not found");
    }
  }

  @Nested
  class SaveBlob {
    @Test
    void plainContentIsUploadedWithOverwrite() {
      BlockBlobItem savedItem = blockBlobItem("plain");
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<BinaryData> dataCaptor = ArgumentCaptor.forClass(BinaryData.class);

      BlockBlobItem result =
          sut.saveBlobUni("container", "path/blob", "content", false).await().atMost(TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(blobClient).upload(dataCaptor.capture(), eq(true));
      assertThat(dataCaptor.getValue().toString()).isEqualTo("content");
    }

    @Test
    void compressedContentIsGzippedBeforeUpload() throws IOException {
      BlockBlobItem savedItem = blockBlobItem("gzip");
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<BinaryData> dataCaptor = ArgumentCaptor.forClass(BinaryData.class);

      BlockBlobItem result =
          sut.saveBlobUni("container", "path/blob", "compressed content", true)
              .await()
              .atMost(TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(blobClient).upload(dataCaptor.capture(), eq(true));
      assertThat(gunzip(dataCaptor.getValue().toBytes())).isEqualTo("compressed content");
    }

    @Test
    void uploadFailureIsPropagated() {
      when(blobClient.upload(any(BinaryData.class), eq(true)))
          .thenReturn(Mono.error(new IllegalArgumentException("upload failed")));

      assertThatThrownBy(
              () ->
                  sut.saveBlobUni("container", "path/blob", "content", false)
                      .await()
                      .atMost(TIMEOUT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("upload failed");
    }
  }

  @Nested
  class SaveFile {
    @Test
    void plainFileUsesDirectUpload() throws IOException {
      Path file = temporaryDirectory.resolve("data.yml");
      Files.writeString(file, "plain file");
      BlockBlobItem savedItem = blockBlobItem("file");
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<BinaryData> dataCaptor = ArgumentCaptor.forClass(BinaryData.class);

      BlockBlobItem result =
          sut.saveAndOverrideBlobFromFile("container", "path/blob", file, "application/yaml", false)
              .await()
              .atMost(TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(blobClient).upload(dataCaptor.capture(), eq(true));
      assertThat(dataCaptor.getValue().toString()).isEqualTo("plain file");
    }

    @Test
    void gzipFileSetsEncodingAndOptionalContentType() throws IOException {
      Path file = temporaryDirectory.resolve("data.json");
      Files.writeString(file, "file to compress");
      BlockBlobItem savedItem = blockBlobItem("file-gzip");
      Response<BlockBlobItem> response =
          new SimpleResponse<>(
              new HttpRequest(HttpMethod.PUT, "https://example.invalid/blob"),
              201,
              new HttpHeaders(),
              savedItem);
      when(blobClient.uploadWithResponse(any(BlobParallelUploadOptions.class)))
          .thenReturn(Mono.just(response));
      ArgumentCaptor<BlobParallelUploadOptions> optionsCaptor =
          ArgumentCaptor.forClass(BlobParallelUploadOptions.class);

      BlockBlobItem withContentType =
          sut.saveAndOverrideBlobFromFile("container", "path/blob", file, "application/json", true)
              .await()
              .atMost(TIMEOUT);
      BlockBlobItem withoutContentType =
          sut.saveAndOverrideBlobFromFile("container", "path/blob", file, null, true)
              .await()
              .atMost(TIMEOUT);

      assertThat(withContentType).isSameAs(savedItem);
      assertThat(withoutContentType).isSameAs(savedItem);
      verify(blobClient, org.mockito.Mockito.times(2)).uploadWithResponse(optionsCaptor.capture());
      List<BlobParallelUploadOptions> options = optionsCaptor.getAllValues();
      assertThat(options.get(0).getHeaders().getContentEncoding()).isEqualTo("gzip");
      assertThat(options.get(0).getHeaders().getContentType()).isEqualTo("application/json");
      assertThat(options.get(1).getHeaders().getContentEncoding()).isEqualTo("gzip");
      assertThat(options.get(1).getHeaders().getContentType()).isNull();
      BinaryData uploadedData = BinaryData.fromFlux(options.get(0).getDataFlux()).block(TIMEOUT);
      assertThat(uploadedData).isNotNull();
      assertThat(gunzip(uploadedData.toBytes())).isEqualTo("file to compress");
    }

    @Test
    void missingGzipSourceFileIsPropagated() {
      Path missingFile = temporaryDirectory.resolve("missing.yml");

      assertThatThrownBy(
              () ->
                  sut.saveAndOverrideBlobFromFile(
                          "container", "path/blob", missingFile, "application/yaml", true)
                      .await()
                      .atMost(TIMEOUT))
          .hasRootCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }
  }

  @Nested
  class GetDecompressedReader {
    @Test
    void validGzipReturnsReadableContent() throws IOException {
      when(blobClient.downloadContent())
          .thenReturn(Mono.just(BinaryData.fromBytes(gzip("reader content"))));

      BufferedReader reader =
          sut.getDecompressedBlobReader("container", "path/blob").await().atMost(TIMEOUT);

      try (reader) {
        assertThat(reader.readLine()).isEqualTo("reader content");
      }
    }

    @Test
    void invalidGzipFailsWithDescriptiveCause() {
      when(blobClient.downloadContent())
          .thenReturn(Mono.just(BinaryData.fromString("not compressed")));

      assertThatThrownBy(
              () -> sut.getDecompressedBlobReader("container", "path/blob").await().atMost(TIMEOUT))
          .hasRootCauseInstanceOf(IOException.class)
          .hasMessageContaining("Failed to create decompressed reader");
    }

    @Test
    void failedDownloadIsPropagated() {
      when(blobClient.downloadContent())
          .thenReturn(Mono.error(new IllegalStateException("reader not found")));

      assertThatThrownBy(
              () -> sut.getDecompressedBlobReader("container", "path/blob").await().atMost(TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("reader not found");
    }
  }

  private static BlockBlobItem blockBlobItem(String etag) {
    return new BlockBlobItem(
        etag, OffsetDateTime.parse("2026-01-01T00:00:00Z"), null, Boolean.TRUE, null, null, null);
  }

  private static byte[] gzip(String content) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
      gzipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
    }
    return outputStream.toByteArray();
  }

  private static String gunzip(byte[] content) throws IOException {
    try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(content))) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
