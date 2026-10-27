package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.AudDataLoader;
import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountService;
import com.amadeus.aud.libraries.models.parsing.JsonService;
import com.amadeus.aud.libraries.models.parsing.ObjectMapperFactory;
import com.amadeus.aud.libraries.models.parsing.YamlService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service to load the core-data from the Azure Storage Account.
 *
 * <p>It uses the StorageAccountService to get either the latest tag or the specified tag from the
 * configuration property. After that, it is dumping the yaml file to the file system if the
 * dumpYaml flag is set to true. Finally, it imports the yaml file into a list of objects.
 *
 * <p>This service is blocking the Quarkus Startup event until the asynchronous process is
 * completed.
 */
@StorageAccountQualifier
@Singleton
public final class AudDataStorageAccountService
    implements StorageAccountDataProcessor<AudDataRepository>, AudDataLoader {
  private static final String BLOB_NAME_API_YAML = "api.yml.gz";
  public static final String BLOB_NAME_API_JSON = "api.json.gz";
  private static final String BLOB_NAME_RELEASE_YAML = "release.yml";
  public static final String BLOB_NAME_RELEASE_JSON = "release.json.gz";
  private static final String BLOB_NAME_METADATA_YAML = "metadata.yml";
  public static final String BLOB_NAME_METADATA_JSON = "metadata.json.gz";

  private final StorageAccountService storageAccountService;
  private final YamlService yamlService;
  private final JsonService jsonService;
  private final AudDataConfig audDataConfig;

  @Inject
  public AudDataStorageAccountService(
      AudDataConfig audDataConfig,
      AudModelConfig audModelConfig,
      BlobServiceAsyncClient blobServiceAsyncClient) {

    this.storageAccountService = new StorageAccountService(blobServiceAsyncClient);

    this.audDataConfig = audDataConfig;
    this.yamlService = new YamlService();
    this.jsonService = new JsonService();

    YamlValidationConfig yamlValidationConfig = audModelConfig.yamlValidation();
    KindBasedDeserializer kindBasedDeserializer =
        new KindBasedDeserializer(
            new DefaultKindMapper(),
            ObjectMapperFactory.createYamlMapper(),
            yamlValidationConfig.enableWarning(),
            yamlValidationConfig.enableOnTheFlyFixes());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Object.class, kindBasedDeserializer);
    this.yamlService.getMapper().registerModule(module);
  }

  /**
   * Initialize objects from a specific version or the latest version.
   *
   * @param version The version to load, or null for the latest
   * @return A Uni emitting the repository with the loaded data
   */
  @Override
  public Uni<AudDataRepository> initObjects(String version, boolean jsonPreferred) {
    return storageAccountService.initObjects(
        version, audDataConfig.cloudStorage().dataContainerName(), this, jsonPreferred);
  }

  @LogExecutionTime
  public Uni<AudDataRepository> load(
      String version, String containerName, BlobService blobService, boolean jsonPreferred) {
    String blobApiName =
        String.join("/", version, jsonPreferred ? BLOB_NAME_API_JSON : BLOB_NAME_API_YAML);
    String blobReleaseName =
        String.join("/", version, jsonPreferred ? BLOB_NAME_RELEASE_JSON : BLOB_NAME_RELEASE_YAML);
    String blobMetadataName =
        String.join(
            "/", version, jsonPreferred ? BLOB_NAME_METADATA_JSON : BLOB_NAME_METADATA_YAML);

    Uni<BufferedReader> apiBlobUni =
        (jsonPreferred || blobApiName.endsWith(".gz"))
            ? blobService
                .getDecompressedBlobReader(containerName, blobApiName)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            : blobService
                .getBlobUni(containerName, blobApiName, false)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem()
                .transform(content -> new BufferedReader(new StringReader(content)));
    Uni<BufferedReader> releaseBlobUni =
        (jsonPreferred || blobReleaseName.endsWith(".gz"))
            ? blobService
                .getDecompressedBlobReader(containerName, blobReleaseName)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            : blobService
                .getBlobUni(containerName, blobReleaseName, false)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem()
                .transform(content -> new BufferedReader(new StringReader(content)));
    Uni<BufferedReader> metadataBlobUni =
        (jsonPreferred || blobMetadataName.endsWith(".gz"))
            ? blobService
                .getDecompressedBlobReader(containerName, blobMetadataName)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            : blobService
                .getBlobUni(containerName, blobMetadataName, false)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem()
                .transform(content -> new BufferedReader(new StringReader(content)));

    return Uni.combine()
        .all()
        .unis(apiBlobUni, releaseBlobUni, metadataBlobUni)
        .with(
            (BufferedReader apiReader,
                BufferedReader releaseReader,
                BufferedReader metadataReader) ->
                new BufferedReader[] {apiReader, releaseReader, metadataReader})
        .emitOn(Infrastructure.getDefaultExecutor())
        .onItem()
        .transform(
            Unchecked.function(
                (BufferedReader[] readers) -> {
                  try {
                    return processResponses(version, readers, jsonPreferred);
                  } finally {
                    for (BufferedReader reader : readers) {
                      if (reader != null) {
                        try {
                          reader.close();
                        } catch (IOException e) {
                          Log.warn("Failed to close reader", e);
                        }
                      }
                    }
                  }
                }));
  }

  public AudDataRepository processResponses(
      String tagDisplayId, BufferedReader[] readers, boolean jsonPreferred) throws IOException {
    AudDataRepository repository = new AudDataRepository();
    repository.setDataVersion(tagDisplayId);

    ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data =
        new ConcurrentHashMap<>();
    for (BufferedReader reader : readers) {
      processResponse(data, reader, jsonPreferred);
    }

    for (Class<? extends V1Base> clazz : new DefaultKindMapper().mappings().values()) {
      if (ModelElement.class.isAssignableFrom(clazz)) {
        data.computeIfAbsent(clazz, k -> new ArrayList<>());
      }
    }

    repository.setData(data);

    return repository;
  }

  private void processResponse(
      ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data,
      BufferedReader reader,
      boolean jsonPreferred)
      throws IOException {
    if (reader == null) {
      return;
    }

    try {
      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> content;
      if (jsonPreferred) {
        content = jsonService.importFromReader(reader);
      } else {
        content = yamlService.importFromReader(reader);
      }
      data.putAll(content);
    } catch (IOException e) {
      if (jsonPreferred) {
        data.putAll(yamlService.importFromReader(reader));
      } else {
        throw e;
      }
    }
  }

  public Uni<BlockBlobItem> save(String version, String content, String blobName) {
    return storageAccountService.save(
        version, audDataConfig.cloudStorage().dataContainerName(), content, blobName, true);
  }

  /**
   * Save all three core data files (API, release, and metadata) for a specific version. This method
   * saves the files in parallel for better performance.
   *
   * @param version The version to save under
   * @param apiContent The API content (JSON or YAML)
   * @param releaseContent The release metadata content (YAML)
   * @param metadataContent The general metadata content (YAML)
   * @param jsonPreferred Whether to save API content as JSON or YAML
   * @return A Uni emitting an array of saved blob items
   */
  public Uni<BlockBlobItem[]> saveAllFiles(
      String version,
      String apiContent,
      String releaseContent,
      String metadataContent,
      boolean jsonPreferred) {

    String apiFileName = jsonPreferred ? BLOB_NAME_API_JSON : BLOB_NAME_API_YAML;
    String releaseFileName = jsonPreferred ? BLOB_NAME_RELEASE_JSON : BLOB_NAME_RELEASE_YAML;
    String metadataFileName = jsonPreferred ? BLOB_NAME_METADATA_JSON : BLOB_NAME_METADATA_YAML;

    Uni<BlockBlobItem> apiBlobUni = save(version, apiContent, apiFileName);
    Uni<BlockBlobItem> releaseBlobUni = save(version, releaseContent, releaseFileName);
    Uni<BlockBlobItem> metadataBlobUni = save(version, metadataContent, metadataFileName);

    return Uni.combine()
        .all()
        .unis(apiBlobUni, releaseBlobUni, metadataBlobUni)
        .with(
            (BlockBlobItem apiBlob, BlockBlobItem releaseBlob, BlockBlobItem metadataBlob) ->
                new BlockBlobItem[] {apiBlob, releaseBlob, metadataBlob})
        .onFailure()
        .invoke(
            e ->
                Log.errorf(
                    "Failed to save one or more files to Storage Account for version %s. Error: %s",
                    version, e));
  }
}
