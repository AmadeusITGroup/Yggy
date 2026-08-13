package com.amadeus.aud.libraries.engine.graph.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.time.Duration;

@ConfigMapping(prefix = "aud.graph")
public interface GraphEngineConfig {

  Init init();

  interface Init {

    @WithDefault("10M")
    Duration readyTimeout();

    @WithDefault("1S")
    Duration readyPollInterval();
  }
}
