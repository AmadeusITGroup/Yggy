package com.amadeus.aud.libraries.models.parsing;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.utils.ModelElementMixin;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * Factory for creating and configuring ObjectMapper instances. Centralizes configuration to ensure
 * consistency across services.
 */
public final class ObjectMapperFactory {

  private ObjectMapperFactory() {}

  /**
   * Create a JSON ObjectMapper with standard configuration.
   *
   * @return Configured JSON ObjectMapper
   */
  public static ObjectMapper createJsonMapper() {
    ObjectMapper mapper = new ObjectMapper();
    configureMapper(mapper);
    return mapper;
  }

  /**
   * Create a YAML ObjectMapper with standard configuration and security settings.
   *
   * @return Configured YAML ObjectMapper
   */
  public static ObjectMapper createYamlMapper() {
    LoaderOptions loaderOptions = new LoaderOptions();
    // Security settings to prevent YAML bombs
    loaderOptions.setCodePointLimit(200_000_000); // 200MB limit instead of MAX_VALUE
    loaderOptions.setMaxAliasesForCollections(50);
    loaderOptions.setNestingDepthLimit(50);

    YAMLFactory yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build();

    ObjectMapper mapper = new ObjectMapper(yamlFactory);
    configureMapper(mapper);
    return mapper;
  }

  /**
   * Apply common configuration to an ObjectMapper.
   *
   * @param objectMapper The mapper to configure
   */
  private static void configureMapper(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper
        .coercionConfigDefaults()
        .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
    objectMapper.addMixIn(ModelElement.class, ModelElementMixin.class);

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }
}
