package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Map;

@ConfigMapping(prefix = "aud-api")
public interface AudApiConfig {

  @WithDefault("local")
  String platform();

  @WithDefault("local")
  String version();

  Map<String, CidrRangeConfig> bundleRanges();
}
