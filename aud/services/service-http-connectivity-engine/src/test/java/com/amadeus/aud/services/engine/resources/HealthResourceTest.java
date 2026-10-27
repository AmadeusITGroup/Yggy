package com.amadeus.aud.services.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthResourceTest {

  @Test
  void getHealthReturnsStillAlive() {
    // Arrange
    HealthResource sut = new HealthResource();

    // Act
    String response = sut.getHealth().await().indefinitely();

    // Assert
    assertThat(response).isEqualTo("Still alive");
  }
}
