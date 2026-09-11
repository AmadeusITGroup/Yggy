package com.amadeus.aud.services.apis.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MicroProfileContextLauncherSessionListenerTest {
  @Test
  void initializesContextBeforeTestsRun() {
    assertThat(MicroProfileContextLauncherSessionListener.isInitialized()).isTrue();
  }
}
