package com.amadeus.aud.libraries.engine.data.clients;

import com.amadeus.aud.libraries.engine.data.fallbackhandlers.UniStatisticsFallback;
import com.amadeus.aud.libraries.yaml.models.Statistics;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "aud-remote")
@OidcClientFilter(value = "aud-governance")
@Singleton
@Path("/")
public interface AudRemoteClient {

  @GET
  @Path("/version/latest/statistics")
  @Produces(MediaType.APPLICATION_JSON)
  @Fallback(UniStatisticsFallback.class)
  Uni<Statistics> statistics();
}
