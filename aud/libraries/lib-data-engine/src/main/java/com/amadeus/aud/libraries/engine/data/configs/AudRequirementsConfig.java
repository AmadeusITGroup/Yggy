package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@ConfigMapping(prefix = "aud-requirements")
public interface AudRequirementsConfig {

  Optional<DumpConfig> dump();
}
