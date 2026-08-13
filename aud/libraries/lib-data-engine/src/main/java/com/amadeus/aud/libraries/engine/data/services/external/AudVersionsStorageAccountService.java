package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.utils.ModelElementMixin;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.annotations.VisibleForTesting;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Service to load the latest version of requirements from Azure Storage Account.
 *
 * <p>This service retrieves the latest version of requirements YAML file from a specified container
 * and blob name, processes it, and stores it in an AudRequirementsRepository.
 */
@StorageAccountQualifier
@Singleton
public final class AudVersionsStorageAccountService implements AudVersionsService {

  private static final String BLOB_NAME = "latest";
  private static final String BLOB_NAME_ALL_VERSIONS = "versions_history.jsonl";
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private final StorageAccountService storageAccountService;
  private final AudDataConfig audDataConfig;

  static {
    OBJECT_MAPPER.addMixIn(ModelElement.class, ModelElementMixin.class);
  }

  @Inject
  public AudVersionsStorageAccountService(
      AudDataConfig audDataConfig, BlobServiceAsyncClient blobServiceAsyncClient) {

    this.audDataConfig = audDataConfig;
    this.storageAccountService = new StorageAccountService(blobServiceAsyncClient);
  }

  @Override
  @LogExecutionTime
  public Uni<String> checkNewVersion(@Nullable String lastVersionNumber) {
    return getLatestVersion()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            (String version) -> {
              if (lastVersionNumber == null
                  || VersionUtils.toLong(version) > VersionUtils.toLong(lastVersionNumber)) {
                Log.infof("New version found: %s", version);
                return Uni.createFrom().item(version);
              } else {
                Log.infof("No new version found. Latest is %s", version);
                return Uni.createFrom().nullItem();
              }
            })
        .onFailure()
        .invoke(
            e ->
                Log.error(
                    "Failed to get latest version from the Azure Storage Account. Error:", e));
  }

  public Uni<String> getLatestVersion() {
    return storageAccountService
        .getBlobService()
        .getBlobUni(audDataConfig.cloudStorage().versionContainerName(), BLOB_NAME, false)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor())
        .onFailure()
        .invoke(
            e ->
                Log.error("Failed to get object (%s) from Storage Accounts. Error:", BLOB_NAME, e));
  }

  public Uni<String> load(
      String version, String containerName, BlobService blobService, boolean jsonPreferred) {
    return blobService.getBlobUni(containerName, BLOB_NAME, false);
  }

  public @Nullable String processResponses(
      String tagDisplayId, String[] responses, boolean jsonPreferred) {
    if (responses.length > 0 && responses[0] != null) {
      return responses[0].trim();
    }
    return null;
  }

  public Uni<List<SingleVersion>> listAllVersions() {
    return storageAccountService
        .getBlobService()
        .getBlobUni(
            audDataConfig.cloudStorage().versionContainerName(), BLOB_NAME_ALL_VERSIONS, false)
        .onItem()
        .transform(
            content ->
                Arrays.stream(content.split("\n"))
                    .filter(line -> !line.isBlank())
                    .map(AudVersionsStorageAccountService::parseVersionLine)
                    .filter(Objects::nonNull)
                    .toList());
  }

  @VisibleForTesting
  protected static SingleVersion parseVersionLine(String line) {
    try {
      return OBJECT_MAPPER.readValue(line, SingleVersion.class);
    } catch (JsonProcessingException e) {
      Log.warnf(e, "Failed to parse version line: %s", line);
      return null;
    }
  }
}
