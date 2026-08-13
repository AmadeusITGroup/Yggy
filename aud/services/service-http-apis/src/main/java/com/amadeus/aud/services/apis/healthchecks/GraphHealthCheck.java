package com.amadeus.aud.services.apis.healthchecks;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import io.smallrye.health.api.HealthGroup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
@HealthGroup("graph")
public class GraphHealthCheck implements HealthCheck {
  private final GraphEngineService graphEngineService;
  private final AudService audService;

  @Inject
  public GraphHealthCheck(GraphEngineService graphEngineService, AudService audService) {
    this.graphEngineService = graphEngineService;
    this.audService = audService;
  }

  @Override
  public HealthCheckResponse call() {
    HealthCheckResponseBuilder dataReady = HealthCheckResponse.named("Graph Readiness");

    Version currentVersion = audService.getCurrentVersion().await().indefinitely();
    if (currentVersion != null) {
      String versionNumber = currentVersion.getVersionNumber();
      if (graphEngineService.isReady(versionNumber)) {
        GraphStatistics stats =
            graphEngineService.getGraphStatistics(versionNumber).await().indefinitely();
        dataReady.up();
        dataReady.withData("Edge size", stats.globalLinksCount());
      } else {
        dataReady.down();
      }
    } else {
      dataReady.down();
    }

    return dataReady.build();
  }
}
