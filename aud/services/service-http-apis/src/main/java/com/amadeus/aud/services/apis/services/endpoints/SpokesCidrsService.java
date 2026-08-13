package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.SpokeCIDRs;
import com.amadeus.aud.libraries.yaml.models.SpokesCIDRsResponse;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class SpokesCidrsService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public SpokesCidrsService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getSpokesCidrs(String dataVersion, String target, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getSpokesCidrs(version, target, uriInfo));
  }

  public Uni<Response> getSpokesCidrs(Version version, String target, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getSpokesCidrsResponse(version, target, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getSpokesCidrsResponse(Version version, String targetName, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    ConcurrentRepository<Target> targetsRepository = version.getTargetsRepository();
    checkRepository(targetsRepository, versionNumber, "Targets");

    Target target = targetsRepository.get(targetName);
    if (target == null) {
      throw new NotFoundException("Target not found: " + targetName);
    }

    List<SpokeCIDRs> resultCidrs = new ArrayList<>();

    for (LandingZone landingZone : target.getLandingZones()) {
      if ("Default Azure Spoke".equals(landingZone.getLandingZoneTypeName())) {
        SpokeCIDRs spokeCIDRs = new SpokeCIDRs();
        spokeCIDRs.setCidrs(landingZone.getCidrs());
        spokeCIDRs.setLandingzone(landingZone.getName());
        resultCidrs.add(spokeCIDRs);
      }
    }

    SpokesCIDRsResponse spokesCIDRsResponse = new SpokesCIDRsResponse();
    spokesCIDRsResponse.setSpokesCidrs(resultCidrs);
    spokesCIDRsResponse.setUrl(UrlUtils.getUrl(uriInfo));
    spokesCIDRsResponse.setVersion(versionNumber);
    spokesCIDRsResponse.setApiVersion(audApiConfig.version());
    spokesCIDRsResponse.setUrlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber));

    return getOkResponse(spokesCIDRsResponse);
  }
}
