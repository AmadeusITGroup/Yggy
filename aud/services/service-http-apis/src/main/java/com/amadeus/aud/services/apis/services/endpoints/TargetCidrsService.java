package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.SpokeCIDRs;
import com.amadeus.aud.libraries.yaml.models.TargetCIDRsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TargetCidrsService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public TargetCidrsService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getTargetCidrs(String version, String targetName, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getTargetCidrs(ver, targetName, uriInfo));
  }

  @CacheResult(cacheName = "getTargetCidrs", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getTargetCidrs(Version version, String targetName, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getResponse(version, targetName, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getResponse(Version version, String targetName, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    ConcurrentRepository<Target> targets = version.getTargetsRepository();
    checkRepository(targets, versionNumber, "Targets");

    Target target = targets.get(targetName);
    if (target == null) {
      throw new NotFoundException("Target not found: " + targetName);
    }

    List<String> targetCidrs = target.getCidrs() != null ? target.getCidrs() : new ArrayList<>();
    List<SpokeCIDRs> landingzoneCidrs = getLandingzoneCidrs(target);

    TargetCIDRsResponse response = new TargetCIDRsResponse();
    response.setUrl(UrlUtils.getUrl(uriInfo));
    response.setUrlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber));
    response.setVersion(versionNumber);
    response.setApiVersion(audApiConfig.version());
    response.setTargetCidrs(targetCidrs);
    response.setLandingzoneCidrs(landingzoneCidrs);

    return getOkResponse(response);
  }

  /**
   * Get landing zone CIDRs for the target
   *
   * @param target The target object
   * @return List of SpokeCIDRs objects
   */
  private List<SpokeCIDRs> getLandingzoneCidrs(Target target) {
    List<SpokeCIDRs> spokeCidrsList = new ArrayList<>();

    if (target.getLandingZones() == null) {
      return spokeCidrsList;
    }

    for (LandingZone lz : target.getLandingZones()) {
      String lzName = lz.getName();
      List<String> cidrsList = lz.getCidrs();

      if (cidrsList != null && !cidrsList.isEmpty()) {
        LandingZoneType lzType = lz.getLandingZoneType();
        String lzTypeName = lzType != null ? lzType.getName() : null;

        spokeCidrsList.add(
            new SpokeCIDRs().landingzone(lzName).landingzonetype(lzTypeName).cidrs(cidrsList));
      }
    }

    return spokeCidrsList;
  }
}
