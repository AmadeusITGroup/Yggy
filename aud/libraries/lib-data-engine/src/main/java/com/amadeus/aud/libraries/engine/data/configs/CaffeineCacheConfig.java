package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.WithDefault;

public interface CaffeineCacheConfig {

  @WithDefault("8")
  long maximumGraphCacheSize();

  @WithDefault("false")
  boolean enableMetrics();
}
