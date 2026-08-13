package com.amadeus.aud.libraries.nsg.services;

import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnetInstances;
import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnets;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.nsg.models.NetworkConfig;
import com.amadeus.aud.libraries.yaml.models.Network;
import com.amadeus.aud.libraries.yaml.models.Subnet;
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
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class NetworkService extends BaseService {
  private final AudApiConfig audApiConfig;
  private final AudService audService;

  @Inject
  public NetworkService(AudApiConfig audApiConfig, AudService audService) {
    this.audApiConfig = audApiConfig;
    this.audService = audService;
  }

  public Uni<Response> getNetworkConfig(String version, String networkName, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getNetworkConfig(ver, networkName, uriInfo));
  }

  @CacheResult(cacheName = "getNetworkConfig", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getNetworkConfig(Version version, String networkName, UriInfo uriInfo) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    checkRepository(landingZonesRepository, version.getVersionNumber(), "LandingZones");

    LandingZone landingZone = landingZonesRepository.get(networkName);
    if (landingZone == null) {
      throw new NotFoundException();
    }

    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getNetworkConfigResponse(version, landingZone, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getNetworkConfigResponse(
      Version version, LandingZone landingZone, UriInfo uriInfo) throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    DependencyInstancesRepository dependencyInstancesRepository =
        version.getDependencyInstancesRepository();
    checkRepository(dependencyInstancesRepository, versionNumber, "Dependency Instances");

    ZoneInstancesRepository zoneInstancesRepository = version.getZoneInstancesRepository();
    checkRepository(zoneInstancesRepository, versionNumber, "Zone Instances");

    SubnetInstancesRepository subnetInstancesRepository = version.getSubnetInstancesRepository();
    checkRepository(subnetInstancesRepository, versionNumber, "Subnet Instances");

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    checkRepository(componentsRepository, versionNumber, "Components Repository");

    // Subnets
    Set<SubnetInstance> filteredSubnetInstances = getSubnetInstances(landingZone);
    Map<String, Subnet> subnetMap = getSubnets(filteredSubnetInstances);

    return getOkResponse(
        new NetworkConfig(
            UrlUtils.getUrl(uriInfo),
            UrlUtils.getVersionedUrl(uriInfo, versionNumber),
            versionNumber,
            audApiConfig.version(),
            new Network()
                .cidrs(landingZone.getCidrs())
                .tags(landingZone.getTagsAsMap())
                .providerSpecific(
                    landingZone.getMergedProviderSpecific() != null
                        ? landingZone.getMergedProviderSpecific()
                        : landingZone.getProviderSpecificAsMap()),
            subnetMap));
  }
}
