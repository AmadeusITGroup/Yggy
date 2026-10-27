package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.LocalFileQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileDataProcessor;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.models.parsing.JsonService;
import com.amadeus.aud.libraries.models.parsing.ObjectMapperFactory;
import com.amadeus.aud.libraries.models.parsing.YamlService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@LocalFileQualifier
@ApplicationScoped
public class AudDataLocalFileService implements LocalFileDataProcessor<AudDataRepository> {

  private static final String FILE_NAME_API_YAML = "api.yml";
  public static final String FILE_NAME_API_JSON = "api.json";
  private static final String FILE_NAME_RELEASE_YAML = "release.yml";
  public static final String FILE_NAME_RELEASE_JSON = "release.json";
  private static final String FILE_NAME_METADATA_YAML = "metadata.yml";
  public static final String FILE_NAME_METADATA_JSON = "metadata.json";

  private final LocalFileService localFileService;
  private final YamlService yamlService;
  private final JsonService jsonService;

  @Inject
  public AudDataLocalFileService(LocalFileService localFileService, AudModelConfig audModelConfig) {
    this.localFileService = localFileService;
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
    return localFileService.initObjects(
        version,
        jsonPreferred
            ? new String[] {FILE_NAME_API_JSON, FILE_NAME_RELEASE_JSON, FILE_NAME_METADATA_JSON}
            : new String[] {FILE_NAME_API_YAML, FILE_NAME_RELEASE_YAML, FILE_NAME_METADATA_YAML},
        this,
        jsonPreferred);
  }

  public AudDataRepository processResponses(
      String tagDisplayId, String[] contents, boolean jsonPreferred) throws IOException {
    AudDataRepository repository = new AudDataRepository();
    repository.setDataVersion(tagDisplayId);

    ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data =
        new ConcurrentHashMap<>();
    for (String content : contents) {
      processResponse(data, content, jsonPreferred);
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
      String content,
      boolean jsonPreferred)
      throws IOException {
    if (content == null) {
      return;
    }

    ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> parsedContent;
    try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
      if (jsonPreferred) {
        parsedContent = jsonService.importFromReader(reader);
      } else {
        parsedContent = yamlService.importFromReader(reader);
      }
    } catch (IOException e) {
      if (jsonPreferred) {
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
          parsedContent = yamlService.importFromReader(reader);
        }
      } else {
        throw e;
      }
    }
    data.putAll(parsedContent);
  }
}
