package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.backend.services.v2.UriFromObjectType;
import com.amadeus.aud.libraries.backend.services.v2.UriProvider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class UrisService {
  UriProvider uriProvider;

  @Inject
  public UrisService(UriFromObjectType uriFromObjectType) {
    this.uriProvider = uriFromObjectType;
  }

  public Uni<Response> getUris() {
    return uriProvider.getUris().map(urisResponse -> Response.ok(urisResponse).build());
  }
}
