package com.amadeus.aud.libraries.engine.data.services.core.db.base;

import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

public class BaseLogService {

  @Inject AudModelConfig audModelConfig;

  protected void logModelWarning(String format, Object... params) {
    if (audModelConfig.enableWarnings()) {
      Log.warnf(format, params);
    }
  }

  protected void logModelDebug(String format, Object... params) {
    if (audModelConfig.enableWarnings()) {
      Log.debugf(format, params);
    }
  }

  protected AudModelConfig getAudModelConfig() {
    return audModelConfig;
  }
}
