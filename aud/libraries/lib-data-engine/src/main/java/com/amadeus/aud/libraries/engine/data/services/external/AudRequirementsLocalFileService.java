package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.JsonService;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.ObjectMapperFactory;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.YamlService;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.LocalFileQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.DataProcessingService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileDataProcessor;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;

@LocalFileQualifier
@ApplicationScoped
public final class AudRequirementsLocalFileService
    implements LocalFileDataProcessor<AudRequirementsRepository> {

  public static final String FILE_NAME_YAML = "requirements.yml";
  public static final String FILE_NAME_JSON = "requirements.json";

  private final LocalFileService localFileService;
  private final DataProcessingService dataProcessingService = new DataProcessingService();
  private final ObjectMapper yamlObjectMapper;
  private final ObjectMapper jsonObjectMapper;
  private final YamlService yamlService;
  private final JsonService jsonService;

  @Inject
  public AudRequirementsLocalFileService(
      LocalFileService localFileService, AudModelConfig audModelConfig) {
    this.localFileService = localFileService;
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

  @Override
  public Uni<AudRequirementsRepository> initObjects(String version, boolean jsonPreferred) {
    return localFileService.initObjects(
        version,
        jsonPreferred ? new String[] {FILE_NAME_JSON} : new String[] {FILE_NAME_YAML},
        this,
        jsonPreferred);
  }

  @Override
  public AudRequirementsRepository processResponses(
      String tagDisplayId, String[] responses, boolean jsonPreferred) throws IOException {
    AudRequirementsRepository repository = new AudRequirementsRepository();
    repository.setDataVersion(tagDisplayId);

    dataProcessingService.calculateHashes(repository, responses);
    if (responses.length > 0 && responses[0] != null && !responses[0].isEmpty()) {
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
