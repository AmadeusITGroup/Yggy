package com.amadeus.aud.libraries.nsg.testsupport;

import org.eclipse.microprofile.context.spi.ContextManagerProvider;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/**
 * Initializes the MicroProfile context manager once, before any test runs.
 *
 * <p>Tests run in parallel and several of them trigger Mutiny context propagation on first use.
 * {@link ContextManagerProvider#instance()} lazily registers the provider without synchronization,
 * so two threads racing on the first call make one of them fail with "ContextManagerProvider
 * already set". Initializing it here, single-threaded, removes the race.
 */
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
