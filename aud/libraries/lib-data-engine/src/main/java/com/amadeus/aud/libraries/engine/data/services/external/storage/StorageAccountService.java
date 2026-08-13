package com.amadeus.aud.libraries.engine.data.services.external.storage;

import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;

/**
 * Service for accessing and processing data from Azure Blob Storage.
 *
 * <p>Provides common functionality for retrieving and processing data from Azure Blob Storage,
 * including version management, blob retrieval, and basic data processing.
 */
public class StorageAccountService {
  private final BlobServiceAsyncClient blobServiceAsyncClient;
  private final BlobService blobService;
  private final DataProcessingService dataProcessingService;

  public StorageAccountService(BlobServiceAsyncClient blobServiceAsyncClient) {
    this.blobServiceAsyncClient = blobServiceAsyncClient;
    this.blobService = new BlobService(blobServiceAsyncClient);
    this.dataProcessingService = new DataProcessingService();
  }

  /**
   * Initialize objects from a specific version or the latest version.
   *
   * @param version The version to load, or null for the latest
   * @param containerName The container name to load from
   * @param dataProcessor The processor to handle the loaded data
   * @return A Uni emitting the repository with the loaded data
   */
  public <R> Uni<R> initObjects(
      @Nullable String version,
      String containerName,
      StorageAccountDataProcessor<R> dataProcessor,
      boolean jsonPreferred) {
    if (version == null || "latest".equals(version)) {
      return loadLatest(containerName, dataProcessor, jsonPreferred);
    }
    return load(version, containerName, dataProcessor, jsonPreferred);
  }

  /**
   * Load the latest version of data.
   *
   * @param containerName The container name to load from
   * @param dataProcessor The processor to handle the loaded data
   * @return A Uni emitting the repository with the loaded data
   */
  @LogExecutionTime
  public <R> Uni<R> loadLatest(
      String containerName, StorageAccountDataProcessor<R> dataProcessor, boolean jsonPreferred) {
    BlobContainerAsyncClient blobContainerAsyncClient =
        blobServiceAsyncClient.getBlobContainerAsyncClient(containerName);

    return Uni.createFrom()
        .converter(
            UniReactorConverters.fromMono(),
            blobContainerAsyncClient
                .listBlobs()
                .map(blobItem -> blobItem.getName().split("/")[0])
                .collectList()
                .mapNotNull(VersionUtils::findLatestVersion))
        .onItem()
        .transformToUni(
            (String version) ->
                load(version, containerName, dataProcessor, jsonPreferred)
                    .onItem()
                    .invoke(blob -> Log.debugf("Latest version is %s", version)));
  }

  /**
   * Load data from a specific version.
   *
   * @param version The version to load
   * @param containerName The container name to load from
   * @param dataProcessor The processor to handle the loaded data
   * @return A Uni emitting the repository with the loaded data
   */
  @LogExecutionTime
  public <R> Uni<R> load(
      String version,
      String containerName,
      StorageAccountDataProcessor<R> dataProcessor,
      boolean jsonPreferred) {
    return dataProcessor.load(version, containerName, blobService, jsonPreferred);
  }

  @LogExecutionTime
  public Uni<BlockBlobItem> save(
      String version, String containerName, String content, String blobName, boolean compressGzip) {
    String fullBlobName = String.join("/", version, blobName);
    return blobService
        .saveBlobUni(containerName, fullBlobName, content, compressGzip)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor())
        .onFailure()
        .invoke(
            e ->
                Log.errorf(
                    "Failed to save object (%s) to Storage Account for version %s. Error: %s",
                    fullBlobName, version, e));
  }

  /**
   * Calculate hash values for a set of responses.
   *
   * @param repository The repository to store the hash values in
   * @param responses The responses to hash
   */
  public void calculateHashes(BaseRepository<?> repository, String[] responses) {
    dataProcessingService.calculateHashes(repository, responses);
  }

  /**
   * Get the blob service instance.
   *
   * @return The blob service instance
   */
  public BlobService getBlobService() {
    return blobService;
  }

  /**
   * Get the data processing service instance.
   *
   * @return The data processing service instance
   */
  public DataProcessingService getDataProcessingService() {
    return dataProcessingService;
  }
}
