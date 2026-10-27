package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.CloudProtectedTargetsResponse;
import com.amadeus.aud.services.apis.models.core.cloudprotectedtargets.TargetEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class CloudProtectedTargetsService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public CloudProtectedTargetsService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getCloudProtectedTargets(String dataVersion, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getCloudProtectedTargets(version, uriInfo));
  }

  public Uni<Response> getCloudProtectedTargets(Version version, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getCloudProtectedTargetsResponse(version, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getCloudProtectedTargetsResponse(Version version, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    TargetsRepository targetsRepository = version.getTargetsRepository();
    checkRepository(targetsRepository, version.getVersionNumber(), "Targets");

    List<Object> targetEntryList = new ArrayList<>();

    for (Target target : targetsRepository.getData().values()) {
      Set<FirewallGroup> firewallGroupSet = target.getFirewallGroups();
      if (target.isCloud()
          && !Constants.STR_AMADEUS.equals(target.getProvider().getName())
          && firewallGroupSet != null) {
        Set<Map<String, String>> firewallGroupMap = new HashSet<>();
        for (FirewallGroup firewallGroup : firewallGroupSet) {
          firewallGroupMap.add(
              Map.of("name", firewallGroup.getName(), "type", firewallGroup.getType()));
        }
        targetEntryList.add(new TargetEntry(target.getName(), firewallGroupMap, target.getCidrs()));
      }
    }

    return getOkResponse(
        new CloudProtectedTargetsResponse()
            .cloudProtectedTargets(targetEntryList)
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .version(versionNumber)
            .apiVersion(audApiConfig.version()));
  }
}
