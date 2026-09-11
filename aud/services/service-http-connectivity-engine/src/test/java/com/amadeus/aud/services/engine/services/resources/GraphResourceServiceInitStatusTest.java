package com.amadeus.aud.services.engine.services.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.services.engine.resources.models.GraphInitStatusResponse;
import com.amadeus.aud.services.engine.resources.models.GraphLoadStatus;
import com.amadeus.aud.services.engine.services.core.GraphCoreService;
import com.amadeus.aud.services.engine.services.core.InitRequestStatus;
import com.amadeus.aud.services.engine.services.core.InitStatus;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphResourceServiceInitStatusTest {

  private GraphCoreService graphCoreService;
  private GraphResourceService graphResourceService;

  @BeforeEach
  void setUp() {
    graphCoreService = mock(GraphCoreService.class);
    GraphEngineService graphEngineService = mock(GraphEngineService.class);
    DataEngine dataEngine = mock(DataEngine.class);
    graphResourceService =
        new GraphResourceService(graphCoreService, graphEngineService, dataEngine);
  }

  @Test
  void initGraphAsyncReturnsAcceptedWhenQueued() {
    when(graphCoreService.requestVersionInitialization("latest"))
        .thenReturn(InitRequestStatus.QUEUED);

    Response response = graphResourceService.initGraphAsync("latest");

    assertThat(response.getStatus()).isEqualTo(202);
    GraphInitStatusResponse payload = (GraphInitStatusResponse) response.getEntity();
    assertThat(payload.getStatus()).isEqualTo(GraphLoadStatus.QUEUED);
  }

  @Test
  void initGraphAsyncReturnsOkWhenAlreadyReady() {
    when(graphCoreService.requestVersionInitialization("latest"))
        .thenReturn(InitRequestStatus.ALREADY_READY);

    Response response = graphResourceService.initGraphAsync("latest");

    assertThat(response.getStatus()).isEqualTo(200);
    GraphInitStatusResponse payload = (GraphInitStatusResponse) response.getEntity();
    assertThat(payload.getStatus()).isEqualTo(GraphLoadStatus.READY);
  }

  @Test
  void initGraphAsyncThrowsWhenVersionIsInvalid() {
    when(graphCoreService.requestVersionInitialization(""))
        .thenReturn(InitRequestStatus.INVALID_VERSION);

    assertThatThrownBy(() -> graphResourceService.initGraphAsync(""))
        .asInstanceOf(throwable(BadRequestException.class));
  }

  @Test
  void getInitStatusReturnsAcceptedWhenInProgress() {
    when(graphCoreService.getVersionInitializationStatus("latest"))
        .thenReturn(InitStatus.IN_PROGRESS);

    Response response = graphResourceService.getInitStatus("latest");

    assertThat(response.getStatus()).isEqualTo(202);
    GraphInitStatusResponse payload = (GraphInitStatusResponse) response.getEntity();
    assertThat(payload.getStatus()).isEqualTo(GraphLoadStatus.IN_PROGRESS);
  }

  @Test
  void getInitStatusReturnsNotFoundWhenNotReady() {
    when(graphCoreService.getVersionInitializationStatus("latest"))
        .thenReturn(InitStatus.NOT_READY);

    Response response = graphResourceService.getInitStatus("latest");

    assertThat(response.getStatus()).isEqualTo(404);
    GraphInitStatusResponse payload = (GraphInitStatusResponse) response.getEntity();
    assertThat(payload.getStatus()).isEqualTo(GraphLoadStatus.NOT_READY);
  }
}
