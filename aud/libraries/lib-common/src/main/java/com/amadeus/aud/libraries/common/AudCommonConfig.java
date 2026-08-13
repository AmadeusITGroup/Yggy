package com.amadeus.aud.libraries.common;

import io.smallrye.config.ConfigMapping;
import java.util.List;

@ConfigMapping(prefix = "aud-common")
public interface AudCommonConfig {
  List<String> pciPhases();
}
