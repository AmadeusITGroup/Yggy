package com.amadeus.aud.libraries.engine.data.configs;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "yaml-validation")
public interface YamlValidationConfig {

  /**
   * Enabling this property will allow performing on-the-fly fixes to some known warnings during the
   * validation of the YAML model submitted by the users.
   *
   * <p>Be aware that has some performance penalties and should be disabled once warnings are
   * resolved.
   *
   * @return true if the YAML fix for warnings is enabled, false otherwise
   */
  // TODO: Ask the user to fix the following warning, implement a validation process to ensure that
  // such issues
  //   do not end up in the yaml files used by AUD-API.
  @WithDefault("false")
  boolean enableOnTheFlyFixes();

  /**
   * This variable allows enabling or disabling the warning messages during the validation of the
   * YAML model.
   *
   * @return true if the warning messages should be enabled, false otherwise
   */
  @WithDefault("false")
  boolean enableWarning();
}
