package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@ConfigMapping(prefix = "aud-model")
public interface AudModelConfig {

  @WithDefault("false")
  boolean enableWarnings();

  @WithDefault("false")
  boolean keepDuplicates();

  YamlValidationConfig yamlValidation();

  String defaultScopesBitbucketProjectKey();

  @WithDefault("https://test.example.com/")
  String defaultScopesBitbucketUrlPrefix();

  Optional<CaffeineCacheConfig> caffeineCache();
}
