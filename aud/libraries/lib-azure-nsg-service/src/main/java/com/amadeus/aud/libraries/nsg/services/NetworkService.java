package com.amadeus.aud.libraries.nsg.services;

import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnetInstances;
import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnets;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
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
import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import com.amadeus.aud.libraries.nsg.models.NetworkConfig;
import com.amadeus.aud.libraries.nsg.ports.NetworkConfigPort;
import com.amadeus.aud.libraries.yaml.models.Network;
import com.amadeus.aud.libraries.yaml.models.Subnet;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class NetworkService extends BaseService implements NetworkConfigPort {
  private final AudApiConfig audApiConfig;
  private final AudService audService;

  @Inject
  public NetworkService(AudApiConfig audApiConfig, AudService audService) {
    this.audApiConfig = audApiConfig;
    this.audService = audService;
  }

  @Override
  public Uni<NetworkConfig> getNetworkConfig(String version, String networkName) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getNetworkConfig(ver, networkName));
  }

  @CacheResult(cacheName = "getNetworkConfig", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<NetworkConfig> getNetworkConfig(Version version, String networkName) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    checkRepository(landingZonesRepository, version.getVersionNumber(), "LandingZones");

    LandingZone landingZone = landingZonesRepository.get(networkName);
    if (landingZone == null) {
      throw new LandingZoneNotFound(networkName);
    }

    return Uni.createFrom()
        .item(() -> getNetworkConfigResponse(version, landingZone))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private NetworkConfig getNetworkConfigResponse(Version version, LandingZone landingZone) {
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

    return new NetworkConfig(
        null,
        null,
        versionNumber,
        audApiConfig.version(),
        new Network()
            .cidrs(landingZone.getCidrs())
            .tags(landingZone.getTagsAsMap())
            .providerSpecific(
                landingZone.getMergedProviderSpecific() != null
                    ? landingZone.getMergedProviderSpecific()
                    : landingZone.getProviderSpecificAsMap()),
        subnetMap);
  }
}
