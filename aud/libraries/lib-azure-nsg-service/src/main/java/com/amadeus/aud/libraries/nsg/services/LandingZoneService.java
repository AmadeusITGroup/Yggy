package com.amadeus.aud.libraries.nsg.services;

import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnetInstances;
import static com.amadeus.aud.libraries.nsg.services.LandingZoneUtils.getSubnets;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.Constants;
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
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import com.amadeus.aud.libraries.nsg.models.Network;
import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import com.amadeus.aud.libraries.nsg.models.SecurityGroupMap;
import com.amadeus.aud.libraries.nsg.models.Subnet;
import com.amadeus.aud.libraries.nsg.models.ZoneSecurityGroups;
import com.amadeus.aud.libraries.nsg.ports.LandingZoneNetworkSecurityPort;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class LandingZoneService extends BaseService implements LandingZoneNetworkSecurityPort {
  private final NsgService nsgService;
  private final AudApiConfig audApiConfig;
  private final AudService audService;
  private final Tracer tracer;

  @Inject
  public LandingZoneService(
      NsgService nsgService, AudApiConfig audApiConfig, AudService audService, Tracer tracer) {
    this.nsgService = nsgService;
    this.audApiConfig = audApiConfig;
    this.audService = audService;
    this.tracer = tracer;
  }

  @Override
  public Uni<NetworkSecurity> getNetworkSecurity(String version, String landingZoneId) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getNetworkSecurity(ver, landingZoneId));
  }

  public Uni<NetworkSecurity> getNetworkSecurity(Version version, String landingZoneId) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    checkRepository(landingZonesRepository, version.getVersionNumber(), "LandingZones");

    LandingZone landingZone = landingZonesRepository.get(landingZoneId);
    if (landingZone == null) {
      throw new LandingZoneNotFound(landingZoneId);
    }

    Target landingZoneTarget = landingZone.getTarget();
    String providerName = landingZoneTarget.getProviderName();
    if (!Objects.equals(providerName, Constants.MICROSOFT_AZURE_PROVIDER)) {
      throw new IllegalStateException("Landing zone is not in Azure, NSGs can not be processed.");
    }

    return Uni.createFrom()
        .item(() -> buildNetworkSecurity(version, landingZone, landingZoneTarget))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  @CacheResult(cacheName = "getNetworkSecurity", keyGenerator = VersionCacheKeyGenerator.class)
  public NetworkSecurity buildNetworkSecurity(
      Version version, LandingZone landingZone, Target landingZoneTarget) {
    Span span =
        tracer
            .spanBuilder("nsg.build-network-security")
            .setAttribute("aud.version", version.getVersionNumber())
            .setAttribute("aud.landing_zone", landingZone.getName())
            .startSpan();
    io.opentelemetry.context.Scope scope = span.makeCurrent();
    try {
      return buildNetworkSecurityInternal(version, landingZone, landingZoneTarget);
    } catch (RuntimeException e) {
      span.recordException(e);
      span.setStatus(StatusCode.ERROR);
      throw e;
    } finally {
      scope.close();
      span.end();
    }
  }

  private NetworkSecurity buildNetworkSecurityInternal(
      Version version, LandingZone landingZone, Target landingZoneTarget) {
    String versionNumber = version.getVersionNumber();

    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();

    DependencyInstancesRepository dependencyInstancesRepository =
        version.getDependencyInstancesRepository();
    checkRepository(dependencyInstancesRepository, versionNumber, "Dependency Instances");

    ZoneInstancesRepository zoneInstancesRepository = version.getZoneInstancesRepository();
    checkRepository(zoneInstancesRepository, versionNumber, "Zone Instances");

    SubnetInstancesRepository subnetInstancesRepository = version.getSubnetInstancesRepository();
    checkRepository(subnetInstancesRepository, versionNumber, "Subnet Instances");

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    checkRepository(componentsRepository, versionNumber, "Components Repository");

    Set<SubnetInstance> filteredSubnetInstances = getSubnetInstances(landingZone);

    // Subnets
    Map<String, Subnet> subnetMap = getSubnets(filteredSubnetInstances);

    // NSGs
    Map<String, SecurityGroupMap> nsgMap = new HashMap<>();
    // Base Access
    nsgService.addBasicConnectivity(version, landingZone, filteredSubnetInstances, nsgMap);
    // Technical Access
    nsgService.addTechnicalAccess(
        version,
        landingZone.getMacroPhaseName(),
        filteredSubnetInstances,
        landingZoneTarget.getLandingZones(),
        nsgMap);

    Set<ZoneInstance> landingZoneZoneInstances = landingZone.getZoneInstances();

    // Enable all ports from office networks on all subnets
    nsgService.addOfficeConnectivity(
        version, landingZonesRepository, landingZoneTarget, landingZoneZoneInstances, nsgMap);

    // Enable all ports from jumpserver networks on all subnets
    nsgService.addJumpServerConnectivity(
        version, componentsRepository, landingZoneTarget, landingZoneZoneInstances, nsgMap);

    // Enable all ports from East/West on all subnets
    nsgService.addEastWestConnectivity(
        version, landingZoneTarget, landingZoneZoneInstances, nsgMap);

    // Always add as the vnet to vnet block the restricted ports
    nsgService.addSameZoneConnectivity(version, landingZoneZoneInstances, nsgMap);

    // Ensure all internal paas subnets can communicate whatever the IZ
    nsgService.addPaasInternalConnectivity(version, landingZone, nsgMap);

    // Add PaaS specific Zone NSG
    nsgService.addPaasSpecificConnectivity(
        version,
        subnetInstancesRepository,
        zoneInstancesRepository,
        landingZoneZoneInstances,
        landingZone,
        nsgMap);

    // Remove not needed NSG
    // TODO: Look if we can avoid creating them at all
    nsgService.removeNotNeededConnectivity(landingZoneZoneInstances, nsgMap);

    // Add PaaS to PaaS NSG
    nsgService.addPaasToPaasConnectivity(version, filteredSubnetInstances, nsgMap);

    // Add Top Level Connectivity
    nsgService.addTopLevelConnectivity(dependencyInstancesRepository, nsgMap);

    // Adjust the destination cidrs fro better optimization
    // Added as a separate step to allow this to be optimal/adjusted with the plugin concept will
    // allow it
    nsgService.optimizeDestinationCidr(zoneInstancesRepository, landingZone, nsgMap);

    // Optimize Inbound Network Rules
    for (SecurityGroupMap securityGroupMap : nsgMap.values()) {
      securityGroupMap.optimizeInboundNetworkRules();
      // We normalize the rules only after the optimization since they could be reduces.
      // This is important for performances.
      securityGroupMap.normalizeNetworkRuleNames();
    }

    Map<String, String> tagsAsMap = landingZone.getTagsAsMap();
    Map<String, Network> landingZoneNetwork =
        Map.of(
            "landing_zone",
            new Network()
                .cidrs(landingZone.getCidrs())
                .tags(tagsAsMap)
                .providerSpecific(landingZone.getProviderSpecificAsMap()));

    return new NetworkSecurity(
        null,
        null,
        versionNumber,
        audApiConfig.version(),
        landingZoneNetwork,
        subnetMap,
        new ZoneSecurityGroups(nsgMap, null, tagsAsMap));
  }
}
