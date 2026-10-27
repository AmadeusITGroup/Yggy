package com.amadeus.aud.libraries.engine.graph.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import org.junit.jupiter.api.Test;

class NetworkComplianceStrategyTest {

  @Test
  void delegatesNetworkComplianceStrategyThroughTestMethod() {
    // Arrange
    ComponentInstance source = mock(ComponentInstance.class);
    ComponentInstance destination = mock(ComponentInstance.class);

    // Act
    boolean result = NetworkComplianceStrategy.NONE.test(source, destination);

    // Assert
    assertThat(result).isFalse();
    assertThat(NetworkComplianceStrategy.NONE.isNetworkComplianceRequired(source, destination))
        .isFalse();
  }
}
