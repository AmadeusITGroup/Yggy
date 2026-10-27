package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.amadeus.aud.libraries.backend.services.v2.UriFromObjectType;
import com.amadeus.aud.libraries.backend.services.v2.UriProvider;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class UrisService {
  UriProvider uriProvider;

  @Inject
  public UrisService(UriFromObjectType uriFromObjectType) {
    this.uriProvider = uriFromObjectType;
  }

  public Uni<Response<List<UriItem>>> getUris() {
    return uriProvider.getUris();
  }
}
