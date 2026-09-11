package com.amadeus.aud.libraries.engine.graph.services.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphEngineServiceProducerTest {

  @Mock GraphEngineService inMemoryGraphEngine;
  @Mock GraphEngineService postgresGraphEngine;
  @Mock GraphEngineService remoteGraphEngine;
  @Mock Instance<GraphEngineService> inMemoryGraphEngineService;
  @Mock Instance<GraphEngineService> postgresGraphEngineService;
  @Mock Instance<GraphEngineService> remoteGraphEngineService;

  @Test
  void produceGraphEngineServiceReturnsInMemoryWhenTypeIsInMemory() {
    GraphEngineServiceProducer sut = newProducer("inmemory");
    stubAvailable(inMemoryGraphEngineService, inMemoryGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(inMemoryGraphEngine);
    verify(remoteGraphEngineService, never()).get();
  }

  @Test
  void produceGraphEngineServiceReturnsRemoteWhenTypeIsRemote() {
    GraphEngineServiceProducer sut = newProducer("  REMOTE  ");
    stubAvailable(remoteGraphEngineService, remoteGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(remoteGraphEngine);
    verify(remoteGraphEngineService).get();
  }

  @Test
  void produceGraphEngineServiceFallsBackToInMemoryWhenRemoteIsUnsatisfied() {
    GraphEngineServiceProducer sut = newProducer("remote");
    when(remoteGraphEngineService.isUnsatisfied()).thenReturn(true);
    stubAvailable(inMemoryGraphEngineService, inMemoryGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(inMemoryGraphEngine);
    verify(remoteGraphEngineService, never()).get();
  }

  @Test
  void produceGraphEngineServiceFallsBackToInMemoryWhenRemoteIsAmbiguous() {
    GraphEngineServiceProducer sut = newProducer("remote");
    when(remoteGraphEngineService.isUnsatisfied()).thenReturn(false);
    when(remoteGraphEngineService.isAmbiguous()).thenReturn(true);
    stubAvailable(inMemoryGraphEngineService, inMemoryGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(inMemoryGraphEngine);
    verify(remoteGraphEngineService, never()).get();
  }

  @Test
  void produceGraphEngineServiceReturnsPostgresWhenTypeIsPostgres() {
    GraphEngineServiceProducer sut = newProducer(" postgres ");
    stubAvailable(postgresGraphEngineService, postgresGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(postgresGraphEngine);
    verify(remoteGraphEngineService, never()).get();
  }

  @Test
  void produceGraphEngineServiceFallsBackToInMemoryWhenTypeIsUnknown() {
    GraphEngineServiceProducer sut = newProducer("unknown");
    stubAvailable(inMemoryGraphEngineService, inMemoryGraphEngine);

    GraphEngineService actual = sut.produceGraphEngineService();

    assertThat(actual).isSameAs(inMemoryGraphEngine);
    verify(remoteGraphEngineService, never()).get();
  }

  private GraphEngineServiceProducer newProducer(String graphDatabaseType) {
    GraphEngineServiceProducer producer = new GraphEngineServiceProducer();
    producer.inMemoryGraphEngineService = inMemoryGraphEngineService;
    producer.postgresGraphEngineService = postgresGraphEngineService;
    producer.remoteGraphEngineService = remoteGraphEngineService;
    producer.graphDatabaseType = graphDatabaseType;
    return producer;
  }

  private void stubAvailable(
      Instance<GraphEngineService> serviceInstance, GraphEngineService graphEngineService) {
    when(serviceInstance.isUnsatisfied()).thenReturn(false);
    when(serviceInstance.isAmbiguous()).thenReturn(false);
    when(serviceInstance.get()).thenReturn(graphEngineService);
  }
}
