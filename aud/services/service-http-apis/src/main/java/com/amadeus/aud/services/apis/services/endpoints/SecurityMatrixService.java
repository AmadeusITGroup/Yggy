package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.yaml.models.SecurityMatrixResponse;
import com.amadeus.aud.services.apis.models.core.matrix.SecurityMatrixContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Singleton
public class SecurityMatrixService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public SecurityMatrixService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getSecurityMatrices(String dataVersion, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getSecurityMatrices(version, uriInfo));
  }

  public Uni<Response> getSecurityMatrices(Version version, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getSecurityMatricesResponse(version, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getSecurityMatricesResponse(Version version, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    ZoneTypesRepository zoneTypesRepository = version.getZoneTypesRepository();
    checkRepository(zoneTypesRepository, version.getVersionNumber(), "Zone Types");

    SecurityMatrixContainer securityMatrices =
        SecurityMatrixContainer.builder().withZoneTypes(zoneTypesRepository.getData()).build();

    return getOkResponse(
        new SecurityMatrixResponse()
            .matrices(securityMatrices.toJson())
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .version(versionNumber)
            .apiVersion(audApiConfig.version()));
  }
}
