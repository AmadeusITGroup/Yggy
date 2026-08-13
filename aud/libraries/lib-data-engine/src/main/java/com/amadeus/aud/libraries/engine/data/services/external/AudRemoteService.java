package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.clients.AudRemoteClient;
import com.amadeus.aud.libraries.yaml.models.Statistics;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service to interact with the Aud Governance API.
 *
 * <p>This service provides methods to retrieve various governance-related data such as scope
 * certifications, security areas, security area mappings, component placements, connection
 * approvals, service approvals, and scopes.
 */
@Singleton
public class AudRemoteService {
  private static final Duration MAX_WAIT = Duration.ofSeconds(60);
  private static final int MAX_RETRIES = 3;
  private final AudRemoteClient restClient;

  @Inject
  public AudRemoteService(@RestClient AudRemoteClient restClient) {
    this.restClient = restClient;
  }

  public Uni<Statistics> statistics() {
    return withStrategy("Statistics", restClient.statistics());
  }

  private static <T> Uni<T> withStrategy(CharSequence title, Uni<T> uni) {
    return uni.runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onFailure()
        .retry()
        .withBackOff(Duration.ofMillis(500), Duration.ofSeconds(3))
        .atMost(MAX_RETRIES)
        .ifNoItem()
        .after(MAX_WAIT)
        .failWith(
            new TimeoutException("Timed out waiting for " + title + " for more than " + MAX_WAIT));
  }
}
