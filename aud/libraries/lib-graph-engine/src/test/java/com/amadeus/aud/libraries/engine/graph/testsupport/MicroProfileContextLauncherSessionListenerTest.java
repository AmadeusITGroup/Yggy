package com.amadeus.aud.libraries.engine.graph.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MicroProfileContextLauncherSessionListenerTest {
  @Test
  void initializesContextBeforeTestsRun() {
    assertThat(MicroProfileContextLauncherSessionListener.isInitialized()).isTrue();
  }
}
