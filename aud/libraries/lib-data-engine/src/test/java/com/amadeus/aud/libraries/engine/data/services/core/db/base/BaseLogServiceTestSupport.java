package com.amadeus.aud.libraries.engine.data.services.core.db.base;

import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.CaffeineCacheConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import java.util.Optional;

public final class BaseLogServiceTestSupport {
  private static final YamlValidationConfig YAML_VALIDATION_CONFIG =
      new YamlValidationConfig() {
        @Override
        public boolean enableOnTheFlyFixes() {
          return false;
        }

        @Override
        public boolean enableWarning() {
          return false;
        }
      };

  private static final AudModelConfig MODEL_CONFIG =
      new AudModelConfig() {
        @Override
        public boolean enableWarnings() {
          return false;
        }

        @Override
        public boolean keepDuplicates() {
          return false;
        }

        @Override
        public YamlValidationConfig yamlValidation() {
          return YAML_VALIDATION_CONFIG;
        }

        @Override
        public String defaultScopesBitbucketProjectKey() {
          return "TEST";
        }

        @Override
        public String defaultScopesBitbucketUrlPrefix() {
          return "https://test.example.com/";
        }

        @Override
        public Optional<CaffeineCacheConfig> caffeineCache() {
          return Optional.empty();
        }
      };

  private BaseLogServiceTestSupport() {}

  public static AudModelConfig modelConfig() {
    return MODEL_CONFIG;
  }

  public static <T extends BaseLogService> T configure(T service) {
    service.audModelConfig = MODEL_CONFIG;
    return service;
  }
}
