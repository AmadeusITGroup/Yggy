package com.amadeus.aud.services.engine.services.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import io.quarkus.cache.CacheManager;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphCoreServiceTest {
  private static final String VERSION = "1.0.0";

  private GraphEngineService graphEngineService;
  private GraphCoreService graphCoreService;

  @BeforeEach
  void setUp() {
    graphEngineService = mock(GraphEngineService.class);
    graphCoreService =
        new GraphCoreService(
            mock(AudVersionsService.class),
            graphEngineService,
            mock(VersionCacheProducer.class),
            mock(CacheManager.class),
            Optional.empty());
  }

  @Test
  void malformedVersionIsRejected() {
    assertThatThrownBy(() -> graphCoreService.getVersion("missing"))
        .isInstanceOf(InvalidVersionException.class)
        .hasMessage("Invalid version format: missing");
  }

  @Test
  void uninitializedVersionIsNotLoaded() {
    when(graphEngineService.isReady(VERSION)).thenReturn(false);

    GraphVersion graphVersion = graphCoreService.getVersion(VERSION);

    assertThat(graphVersion).isNull();
    verify(graphEngineService, never()).init(anyString());
  }

  @Test
  void initializedVersionIsReturned() {
    GraphStatistics statistics = new GraphStatistics(Map.of(), Map.of(), 2, 1);
    when(graphEngineService.isReady(VERSION)).thenReturn(true);
    when(graphEngineService.getGraphStatistics(VERSION))
        .thenReturn(Uni.createFrom().item(statistics));

    GraphVersion graphVersion = graphCoreService.getVersion(VERSION);

    assertThat(graphVersion).isNotNull();
    assertThat(graphVersion.getVersion()).isEqualTo(VERSION);
    assertThat(graphVersion.toStatistics()).isEqualTo(statistics);
  }
}
