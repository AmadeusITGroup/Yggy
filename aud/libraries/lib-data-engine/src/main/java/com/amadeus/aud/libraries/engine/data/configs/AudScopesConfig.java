package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.inject.Singleton;

@ConfigMapping(prefix = "aud-scopes")
@Singleton
public interface AudScopesConfig {

  @WithDefault("true")
  boolean enabled();

  @WithDefault("all")
  String scopes();

  ScopesBlobConfig blob();

  @WithDefault("true")
  boolean associateScopeWithRequirements();

  @WithDefault("60")
  int maxLoadDuration();

  interface ScopesBlobConfig {
    @WithDefault("false")
    boolean enabled();

    DumpConfig dump();
  }
}
