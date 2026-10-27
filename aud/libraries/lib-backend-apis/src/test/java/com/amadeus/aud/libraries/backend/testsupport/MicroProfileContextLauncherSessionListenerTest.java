package com.amadeus.aud.libraries.backend.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MicroProfileContextLauncherSessionListenerTest {
  @Test
  void initializesContextBeforeTestsRun() {
    assertThat(MicroProfileContextLauncherSessionListener.isInitialized()).isTrue();
  }
}
