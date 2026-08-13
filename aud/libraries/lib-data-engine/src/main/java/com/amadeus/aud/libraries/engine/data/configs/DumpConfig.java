package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.WithDefault;
import java.util.Optional;

public interface DumpConfig {

  @WithDefault("false")
  boolean enabled();

  @WithDefault(".")
  Optional<String> basePath();
}
