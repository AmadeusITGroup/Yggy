package com.amadeus.aud.services.apis.testsupport;

import org.eclipse.microprofile.context.spi.ContextManagerProvider;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

public final class MicroProfileContextLauncherSessionListener implements LauncherSessionListener {
  private static final Object INITIALIZATION_LOCK = new Object();
  private static volatile boolean initialized;

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    synchronized (INITIALIZATION_LOCK) {
      if (!initialized) {
        ContextManagerProvider.instance().getContextManager();
        initialized = true;
      }
    }
  }

  static boolean isInitialized() {
    return initialized;
  }
}
