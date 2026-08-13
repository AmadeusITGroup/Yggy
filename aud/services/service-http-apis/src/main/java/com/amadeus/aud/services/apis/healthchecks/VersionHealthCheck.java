package com.amadeus.aud.services.apis.healthchecks;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.quarkus.logging.Log;
import io.smallrye.health.api.HealthGroup;
import io.smallrye.mutiny.TimeoutException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Map;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
@HealthGroup("version")
public class VersionHealthCheck implements HealthCheck {
  private final DataEngine dataEngine;

  @Inject
  public VersionHealthCheck(DataEngine dataEngine) {
    this.dataEngine = dataEngine;
  }

  @Override
  public HealthCheckResponse call() {
    HealthCheckResponseBuilder dataReady = HealthCheckResponse.named("Version Readiness");

    try {
      Version version = dataEngine.getCurrentVersion().await().atMost(Duration.ofMillis(50));

      if (version != null) {
        dataReady.up().withData("version", version.getVersionNumber());
        for (Map.Entry<String, Long> entry : version.getRepositorySizes().entrySet()) {
          dataReady.withData(entry.getKey(), entry.getValue());
        }
        return dataReady.build();
      }
      return getDownResponse(dataReady);
    } catch (TimeoutException e) {
      return getDownResponse(dataReady);
    } catch (Exception e) {
      Log.errorf(e, "Exception in calling VersionHealthCheck");
      return getDownResponse(dataReady);
    }
  }

  private static HealthCheckResponse getDownResponse(HealthCheckResponseBuilder dataReady) {
    return dataReady.down().withData("reason", "No version initialized").build();
  }
}
