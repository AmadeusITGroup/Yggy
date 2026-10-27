package com.amadeus.aud.services.engine.services.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.quarkus.cache.CacheManager;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphCoreServiceTest {
  private static final String VERSION = "1.0.0";

  private GraphEngineService graphEngineService;
  private GraphCoreService graphCoreService;
  private InMemorySpanExporter spanExporter;

  @BeforeEach
  void setUp() {
    graphEngineService = mock(GraphEngineService.class);
    spanExporter = InMemorySpanExporter.create();
    Tracer tracer =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                    .build())
            .build()
            .getTracer(GraphCoreServiceTest.class.getName());
    graphCoreService =
        new GraphCoreService(
            mock(AudVersionsService.class),
            graphEngineService,
            mock(VersionCacheProducer.class),
            mock(CacheManager.class),
            Optional.empty(),
            tracer);
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

  @Test
  void queuedInitializationJobsEachGetTheirOwnTrace() {
    when(graphEngineService.init(anyString())).thenReturn(Uni.createFrom().voidItem());
    when(graphEngineService.isReady(anyString())).thenReturn(false);
    graphCoreService.startInitializationWorker();

    graphCoreService.requestVersionInitialization("1.0.0");
    graphCoreService.requestVersionInitialization("2.0.0");

    List<SpanData> spans = awaitFinishedSpans(2);

    assertThat(spans.get(0).getTraceId()).isNotEqualTo(spans.get(1).getTraceId());
    assertThat(spans).allMatch(span -> !span.getParentSpanContext().isValid());
  }

  // The worker processes the queue asynchronously, so poll instead of asserting immediately.
  private List<SpanData> awaitFinishedSpans(int expectedCount) {
    Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
    while (spanExporter.getFinishedSpanItems().size() < expectedCount
        && Instant.now().isBefore(deadline)) {
      Thread.onSpinWait();
    }
    List<SpanData> spans = spanExporter.getFinishedSpanItems();
    assertThat(spans).hasSize(expectedCount);
    return spans;
  }
}
