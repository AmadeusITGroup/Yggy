package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/// Service responsible for managing version-related operations. Provides functionality to retrieve
/// current version information, list available versions, and trigger loading of specific versions.
@ApplicationScoped
public class VersionService {
  private final AudVersionsService audVersionsService;

  @Inject
  public VersionService(AudVersionsService audVersionsService) {
    this.audVersionsService = audVersionsService;
  }

  public Uni<Response<List<SingleVersion>>> getAllAvailableVersionsResponse() {
    return audVersionsService
        .listAllVersions()
        .onItem()
        .transform(
            versions -> new com.amadeus.aud.libraries.backend.models.v2.Response<>(versions, null));
  }
}
