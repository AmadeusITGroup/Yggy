package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudRequirementsConfig;
import com.amadeus.aud.libraries.engine.data.configs.DumpConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.JsonService;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.ObjectMapperFactory;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.YamlService;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Service to load the requirements from Azure Storage Account.
 *
 * <p>This service retrieves the requirements YAML file from a specified container and version,
 * processes it, and stores it in an AudRequirementsRepository.
 *
 * <p>It can also dump the YAML file to the file system if the dumpYaml flag is set to true.
 */
@StorageAccountQualifier
@Singleton
public final class AudRequirementsStorageAccountService
    implements StorageAccountDataProcessor<AudRequirementsRepository> {

  public static final String BLOB_NAME_YAML = "requirements.yml";
  public static final String BLOB_NAME_JSON = "requirements.json.gz";

  private final ObjectMapper yamlObjectMapper;
  private final ObjectMapper jsonObjectMapper;
  private final YamlService yamlService;
  private final JsonService jsonService;
  private final StorageAccountService storageAccountService;
  private final AudRequirementsConfig config;
  private final AudDataConfig audDataConfig;

  @Inject
  public AudRequirementsStorageAccountService(
      AudDataConfig audDataConfig,
      AudModelConfig audModelConfig,
      AudRequirementsConfig config,
      BlobServiceAsyncClient blobServiceAsyncClient) {
    this.storageAccountService = new StorageAccountService(blobServiceAsyncClient);
    this.audDataConfig = audDataConfig;
    this.config = config;

    this.yamlService = new YamlService();
    this.jsonService = new JsonService();

    ObjectMapper yamlMapper = yamlService.getMapper();
    YamlValidationConfig yamlValidationConfig = audModelConfig.yamlValidation();
    KindBasedDeserializer kindBasedDeserializer =
        new KindBasedDeserializer(
            new DefaultKindMapper(),
            ObjectMapperFactory.createYamlMapper(),
            yamlValidationConfig.enableWarning(),
            yamlValidationConfig.enableOnTheFlyFixes());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Object.class, kindBasedDeserializer);
    yamlMapper.registerModule(module);
    this.yamlObjectMapper = yamlMapper;

    this.jsonObjectMapper = jsonService.getMapper();
  }

  public Uni<AudRequirementsRepository> initObjects(String version, boolean jsonPreferred) {
    return storageAccountService.initObjects(
        version, audDataConfig.cloudStorage().requirementsContainerName(), this, jsonPreferred);
  }

  public Uni<AudRequirementsRepository> load(
      String version, String containerName, BlobService blobService) {
    return load(version, containerName, blobService, false);
  }

  @LogExecutionTime
  public Uni<AudRequirementsRepository> load(
      String version, String containerName, BlobService blobService, boolean jsonPreferred) {
    DumpConfig dumpConfig =
        this.config
            .dump()
            .orElseGet(
                () ->
                    new DumpConfig() {
                      @Override
                      public boolean enabled() {
                        return false;
                      }

                      @Override
                      public Optional<String> basePath() {
                        return Optional.of("");
                      }
                    });
    boolean dumpBlobEnabled = dumpConfig.enabled();
    String dumpBlobPath = dumpConfig.basePath().orElse(".");

    String blobName = String.join("/", version, jsonPreferred ? BLOB_NAME_JSON : BLOB_NAME_YAML);
    return blobService
        .getBlobUni(containerName, blobName, jsonPreferred)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor())
        .onItem()
        .transform(
            Unchecked.function(
                (String response) -> {
                  if (dumpBlobEnabled) {
                    Path path = Path.of(dumpBlobPath, blobName);
                    if (jsonPreferred) {
                      jsonService.dump(response, path);
                    } else {
                      yamlService.dump(response, path);
                    }
                  }
                  return processResponses(version, new String[] {response}, jsonPreferred);
                }));
  }

  public Uni<BlockBlobItem> save(String version, String content, String blobName) {
    return storageAccountService.save(
        version, audDataConfig.cloudStorage().requirementsContainerName(), content, blobName, true);
  }

  public AudRequirementsRepository processResponses(
      String tagDisplayId, String[] responses, boolean jsonPreferred) throws IOException {
    AudRequirementsRepository repository = new AudRequirementsRepository();
    repository.setDataVersion(tagDisplayId);

    storageAccountService.calculateHashes(repository, responses);

    if (responses.length > 0 && responses[0] != null) {
      repository.setData(importData(responses[0], jsonPreferred));
    }

    return repository;
  }

  /**
   * Import content into the appropriate data structure.
   *
   * @param content The content to import (JSON or YAML)
   * @param jsonPreferred Whether to parse as JSON or YAML
   * @return The imported data
   * @throws IOException If an error occurs during import
   */
  private Requirements importData(String content, boolean jsonPreferred) throws IOException {
    if (jsonPreferred) {
      return jsonObjectMapper.readValue(content, Requirements.class);
    }
    return yamlObjectMapper.readValue(content, Requirements.class);
  }
}
