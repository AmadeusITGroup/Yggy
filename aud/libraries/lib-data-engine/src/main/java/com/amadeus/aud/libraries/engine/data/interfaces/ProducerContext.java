package com.amadeus.aud.libraries.engine.data.interfaces;

import java.time.Duration;

public interface ProducerContext {

  default Duration maxWait() {
    return Duration.ofSeconds(60);
  }
}
