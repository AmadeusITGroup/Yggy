package com.amadeus.aud.libraries.engine.data.services.external.storage;

import com.amadeus.aud.libraries.engine.data.utils.FileUtils;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import reactor.core.publisher.Mono;

/** Service for accessing and retrieving data from Azure Blob Storage. */
public class BlobService {
  private static final int CHUNK_SIZE = 16 * 1024;
  private final BlobServiceAsyncClient blobServiceAsyncClient;

  public BlobService(BlobServiceAsyncClient blobServiceAsyncClient) {
    this.blobServiceAsyncClient = blobServiceAsyncClient;
  }

  /**
   * Retrieve a blob from Azure Blob Storage.
   *
   * @param containerName The container name
   * @param blobName The blob name
   * @return A Uni emitting the blob content as a string
   */
  public Uni<String> getBlobUni(String containerName, String blobName, boolean decodeGzipped) {
    Log.debugf("BlobService::getBlobUni : container=%s; blob=%s", containerName, blobName);
    BlobAsyncClient blobAsyncClient =
        blobServiceAsyncClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

    return Uni.createFrom()
        .converter(UniReactorConverters.fromMono(), blobAsyncClient.downloadContent())
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transform(
            Unchecked.function(
                (BinaryData binaryData) -> {
                  String response;
                  if (decodeGzipped) {
                    response = FileUtils.decodeGzipped(binaryData);
                  } else {
                    response = binaryData.toString().trim();
                  }
                  return response;
                }));
  }

  public Uni<BlockBlobItem> saveBlobUni(
      String containerName, String blobName, String content, boolean compressGzip) {
    BlobAsyncClient blobAsyncClient =
        blobServiceAsyncClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

    return Uni.createFrom()
        .item(content)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            Unchecked.function(
                (String s) -> {
                  BinaryData data;
                  if (compressGzip) {
                    data = FileUtils.encodeGzip(s);
                  } else {
                    data = BinaryData.fromString(s);
                  }
                  Mono<BlockBlobItem> upload = blobAsyncClient.upload(data, true);
                  return Uni.createFrom()
                      .converter(UniReactorConverters.fromMono(), upload)
                      .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                      .emitOn(Infrastructure.getDefaultWorkerPool());
                }));
  }

  /**
   * Upload a local file as a blob, overwriting any existing blob with the same name.
   *
   * <p>When {@code gzip} is true, the file is gzip-compressed on the fly while reading (streaming,
   * only the compressed output is buffered in memory) and the blob is stored with {@code
   * Content-Encoding: gzip} together with the provided {@code contentType}, so consumers can stream
   * the bytes back to HTTP clients without re-compressing.
   *
   * @param containerName container name
   * @param blobName blob name
   * @param path local file to upload
   * @param contentType content type to set on the blob HTTP headers when gzipping; ignored (and may
   *     be {@code null}) when {@code gzip} is false
   * @param gzip whether to gzip the file before upload
   */
  public Uni<BlockBlobItem> saveAndOverrideBlobFromFile(
      String containerName,
      String blobName,
      Path path,
      @Nullable String contentType,
      boolean gzip) {
    BlobAsyncClient blobAsyncClient =
        blobServiceAsyncClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);

    if (!gzip) {
      return Uni.createFrom()
          .converter(
              UniReactorConverters.fromMono(),
              blobAsyncClient.upload(BinaryData.fromFile(path), true))
          .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
          .emitOn(Infrastructure.getDefaultWorkerPool());
    }

    return Uni.createFrom()
        .item(Unchecked.supplier(() -> gzipFile(path)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            (BinaryData data) -> {
              BlobHttpHeaders headers = new BlobHttpHeaders().setContentEncoding("gzip");
              if (contentType != null) {
                headers.setContentType(contentType);
              }
              BlobParallelUploadOptions opts =
                  new BlobParallelUploadOptions(data).setHeaders(headers);
              Mono<BlockBlobItem> upload =
                  blobAsyncClient.uploadWithResponse(opts).map(Response::getValue);
              return Uni.createFrom()
                  .converter(UniReactorConverters.fromMono(), upload)
                  .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                  .emitOn(Infrastructure.getDefaultWorkerPool());
            });
  }

  /**
   * Stream-compress a file with gzip and return the compressed bytes as {@link BinaryData}.
   *
   * <p>Source bytes are read in 8 KiB chunks (via {@link InputStream#transferTo}); only the
   * compressed output is held in memory.
   */
  private static BinaryData gzipFile(Path path) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (InputStream in = Files.newInputStream(path);
        GZIPOutputStream gz = new GZIPOutputStream(outputStream)) {
      in.transferTo(gz);
    }
    return BinaryData.fromBytes(outputStream.toByteArray());
  }

  /**
   * Returns a Uni emitting a BufferedReader for the decompressed content of a gzipped blob. The
   * caller is responsible for closing the BufferedReader.
   */
  public Uni<BufferedReader> getDecompressedBlobReader(String containerName, String blobName) {
    BlobAsyncClient blobAsyncClient =
        blobServiceAsyncClient
            .getBlobContainerAsyncClient(containerName)
            .getBlobAsyncClient(blobName);
    return Uni.createFrom()
        .converter(UniReactorConverters.fromMono(), blobAsyncClient.downloadContent())
        .onItem()
        .transform(
            Unchecked.function(
                (BinaryData binaryData) -> {
                  try {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(binaryData.toStream());
                    return new BufferedReader(
                        new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8), CHUNK_SIZE);
                  } catch (IOException e) {
                    throw new RuntimeException("Failed to create decompressed reader", e);
                  }
                }));
  }
}
