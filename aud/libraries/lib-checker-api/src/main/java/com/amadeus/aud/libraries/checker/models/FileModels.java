package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.models.parsing.YamlService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.amadeus.aud.libraries.yaml.models.utils.ModelElementMixin;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.logging.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.yaml.snakeyaml.LoaderOptions;

public final class FileModels {

  private FileModels() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void populatedDomainsData(
      final Path path, Map<Class<? extends ModelElement>, List<ModelElement>> data) {
    ObjectMapper mapper = new ObjectMapper();
    YAMLFactory yamlFactory = new YAMLFactory();
    setupMapper(mapper);
    try (Stream<Path> paths = Files.walk(path, 10)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
          .forEach((Path p) -> importFile(yamlFactory, p, mapper, data));
    } catch (IOException e) {
      Log.error("should not happen, because file syntax already valid", e);
    }
  }

  private static void setupMapper(ObjectMapper mapper) {
    YamlValidationConfig yamlValidationConfig =
        new YamlValidationConfig() {
          @Override
          public boolean enableOnTheFlyFixes() {
            return true;
          }

          @Override
          public boolean enableWarning() {
            return false;
          }
        };
    KindBasedDeserializer kindBasedDeserializer =
        new KindBasedDeserializer(
            new DefaultKindMapper(),
            buildObjectMapper(),
            yamlValidationConfig.enableWarning(),
            yamlValidationConfig.enableOnTheFlyFixes());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Object.class, kindBasedDeserializer);
    mapper.registerModule(module);
    mapper.addMixIn(ModelElement.class, ModelElementMixin.class);
  }

  private static ObjectMapper buildObjectMapper() {
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
    YAMLFactory yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build();
    ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
    configureMapper(objectMapper);
    return objectMapper;
  }

  private static void configureMapper(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.addMixIn(ModelElement.class, ModelElementMixin.class);
    // IMPORTANT: leave it as is
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
  }

  private static void importFile(
      YAMLFactory yamlFactory,
      Path filePath,
      ObjectMapper mapper,
      Map<Class<? extends ModelElement>, List<ModelElement>> data) {
    try {
      Log.debugf("Reading file %s", filePath);
      Map<Class<? extends V1Base>, List<V1Base>> currentData = new HashMap<>();
      YamlService.importFromFile(yamlFactory, mapper, filePath, currentData);

      Log.debugf("Read %s types", currentData.size());
      for (Map.Entry<Class<? extends V1Base>, List<V1Base>> entry : currentData.entrySet()) {
        Log.debugf("Importing %s", entry.getKey());
        List<ModelElement> newElements = new ArrayList<>(entry.getValue());
        data.merge(
            entry.getKey(),
            newElements,
            (List<ModelElement> oldList, List<ModelElement> newList) -> {
              oldList.addAll(newList);
              return oldList;
            });
      }
    } catch (IOException e) {
      Log.error("should not happen, because file syntax already valid", e);
    }
  }
}
