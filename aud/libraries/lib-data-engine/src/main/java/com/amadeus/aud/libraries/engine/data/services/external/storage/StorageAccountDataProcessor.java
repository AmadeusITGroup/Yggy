package com.amadeus.aud.libraries.engine.data.services.external.storage;

import com.azure.storage.blob.models.BlockBlobItem;
import io.smallrye.mutiny.Uni;

public interface StorageAccountDataProcessor<R> extends DataProcessor<R> {

  /**
   * Load data from a specific version.
   *
   * @param version The version to load
   * @param containerName The container name to load from
   * @param blobService The blob service to use for retrieving data
   * @return A Uni emitting the repository with the loaded data
   */
  Uni<R> load(String version, String containerName, BlobService blobService, boolean jsonPreferred);

  /**
   * Save data to a specific version. Seems to be unused so far.
   *
   * @param version The version to save
   * @param containerName The container name to save to
   * @param blobService The blob service to use for saving data
   * @param content The content to save
   * @param name The name of the blob to save
   * @return A Uni emitting the result of the save operation
   */
  default Uni<BlockBlobItem> save(
      String version, String containerName, BlobService blobService, String content, String name) {
    return Uni.createFrom().failure(new RuntimeException("No proper implementation for save()"));
  }
}
