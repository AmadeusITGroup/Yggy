package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "aud-data")
public interface AudDataConfig {

  CloudStorageConfig cloudStorage();

  interface CloudStorageConfig {

    @WithDefault("aud-data")
    String dataContainerName();

    @WithDefault("aud-data")
    String requirementsContainerName();

    @WithDefault("aud-version")
    String versionContainerName();

    @WithDefault("pr-diff")
    String prContainerName();
  }

  LocalFileConfig localFile();

  interface LocalFileConfig {

    @WithDefault("/tmp/yggy-data")
    String localDataDirectory();

    @WithDefault("false")
    boolean useLocalData();

    @WithDefault("1200")
    Integer scopesMaxLoadDurationSeconds();
  }
}
