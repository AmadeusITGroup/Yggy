package com.amadeus.aud.libraries.nsg.services;

import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.configs.CidrRangeConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.models.core.utils.StringUtils;
import com.amadeus.aud.libraries.nsg.models.NetworkRule;
import com.amadeus.aud.libraries.nsg.models.Protocol;
import com.amadeus.aud.libraries.nsg.models.SecurityGroupMap;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Singleton
public class NsgService extends BaseService {

  private final GraphEngineService graphEngineService;
  private final AudApiConfig audApiConfig;

  @Inject
  public NsgService(GraphEngineService graphEngineService, AudApiConfig audApiConfig) {
    this.graphEngineService = graphEngineService;
    this.audApiConfig = audApiConfig;
  }

  private Service getAllService(Version version) {
    ServicesRepository servicesRepository = version.getServicesRepository();
    checkRepository(servicesRepository, version.getVersionNumber(), "Services");
    return servicesRepository.getAllService();
  }

  public void addPaasInternalConnectivity(
      Version version, LandingZone landingZone, Map<String, SecurityGroupMap> nsgMap) {
    Set<ComponentInstance> componentInstances = landingZone.getComponentInstances();
    if (componentInstances == null) {
      return;
    }

    Service allService = getAllService(version);
    if (allService == null) {
      throw new IllegalStateException(
          "ALL service not found for version " + version.getVersionNumber());
    }

    for (ComponentInstance componentInstance : componentInstances) {
      boolean isHosting = componentInstance.getComponent().isHosting();
      if (!isHosting) {
        continue;
      }
      V1ComponentInstanceSpec.EndpointTypeEnum endpointType = componentInstance.getEndpointType();
      if (endpointType != V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
        continue;
      }

      Set<SubnetInstance> paasZoneOutSubnetInstances =
          componentInstance.getPaasZoneOutSubnetInstances();
      if (paasZoneOutSubnetInstances == null) {
        continue;
      }

      Set<ZoneInstance> zoneInstances = new HashSet<>();
      for (SubnetInstance subnetInstance : paasZoneOutSubnetInstances) {
        zoneInstances.add(subnetInstance.getZoneInstance());
      }

      for (SubnetInstance subnetInstance : paasZoneOutSubnetInstances) {
        for (ZoneInstance zoneInstance : zoneInstances) {
          if (!Objects.equals(zoneInstance.getName(), subnetInstance.getZoneInstanceName())) {
            String zoneNsgRuleName =
                String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());
            nsgMap
                .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
                .addInbound(
                    createSubnetPort(
                        zoneNsgRuleName,
                        List.of(subnetInstance.getCidr()),
                        zoneInstance,
                        null,
                        allService,
                        null));
          }
        }
      }
    }
  }

  public void addTopLevelConnectivity(
      @Nullable DependencyInstancesRepository dependencyInstancesRepository,
      Map<String, SecurityGroupMap> nsgMap) {
    if (dependencyInstancesRepository == null || !dependencyInstancesRepository.isReady()) {
      throw new IllegalStateException("DependencyInstances repository is not available");
    }

    for (DependencyInstance dependencyInstance : dependencyInstancesRepository.getData().values()) {
      // Process TopLevel to TopLevel generic source and generic target instance.
      // They are used to cover static global security rules like ping/traceroute.
      // They are not part of the computed graph, need to be added manually
      Dependency dependency = dependencyInstance.getDependency();
      if (Objects.equals(dependency.getSourceComponentName(), Constants.TOP_LEVEL_COMPONENT_NAME)
          && Objects.equals(
              dependency.getDestinationComponentName(), Constants.TOP_LEVEL_COMPONENT_NAME)
          && dependencyInstance.isSourceAsComponent()
          && dependencyInstance.isTargetAsComponent()) {
        for (Service service : dependencyInstance.getServices()) {
          for (Map.Entry<String, SecurityGroupMap> entry : nsgMap.entrySet()) {
            String key = entry.getKey();
            SecurityGroupMap value = entry.getValue();

            RangeSet<Integer> ports;
            V1ServiceSpec.ProtocolEnum protocol = service.getProtocol();
            if (protocol == V1ServiceSpec.ProtocolEnum.ICMP) {
              ports = TreeRangeSet.create(PortUtils.ALL_RANGE_SET);
            } else {
              ports = service.getPortsRange();
            }
            String dependencyInstanceName = dependencyInstance.getName();
            String protocolString = protocol.toString();
            String zoneNsgRuleName =
                String.join(
                    "_",
                    key,
                    dependencyInstanceName,
                    protocolString,
                    String.valueOf(Objects.hash(key, dependencyInstanceName, protocolString)));
            value.addInbound(
                new NetworkRule(
                    StringUtils.normalize(zoneNsgRuleName),
                    TreeRangeSet.create(IPv4Utils.ALL_RANGE_SET),
                    TreeRangeSet.create(IPv4Utils.ALL_RANGE_SET),
                    ports,
                    protocolString));
          }
        }
      }
    }
  }

  public void addPaasToPaasConnectivity(
      Version version,
      Set<SubnetInstance> filteredSubnetInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    Service allService = getAllService(version);
    if (allService == null) {
      throw new IllegalStateException(
          "ALL service not found for version " + version.getVersionNumber());
    }

    for (SubnetInstance subnetInstance : filteredSubnetInstances) {
      for (SubnetInstance subnetInstance1 : filteredSubnetInstances) {
        if (!subnetInstance1.isGroup()
            && !Objects.equals(subnetInstance, subnetInstance1)
            && subnetInstance.getZoneInstance().getZoneType().isPaas()
            && subnetInstance1.getZoneInstance().getZoneType().isPaas()) {
          ZoneInstance zoneInstance = subnetInstance.getZoneInstance();
          String zoneNsgRuleName =
              String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());

          nsgMap
              .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
              .addInbound(
                  createSubnetPort(
                      zoneNsgRuleName, subnetInstance1, zoneInstance, null, allService, null));
        }
      }
    }
  }

  /// Group all the destinations CIDRS not in the ZI to allows the genric rules to be more generic
  /// for all CIDRs
  ///
  /// This function analyze all the inbound rules of a given NSG and identify all the destination
  /// cidrs not part of the zone instance then for each rule having a destination cidr not in the ZI
  /// we replace it by all the destination cidrs. Only needed due to Maximum limit reach on Azure
  ///
  /// @param zoneInstancesRepository the repository with all the ZoneInstance objects
  /// @param landingZone the landing zone we are currently processing
  /// @param nsgMap the current Map of the NSG
  public void optimizeDestinationCidr(
      @Nullable ZoneInstancesRepository zoneInstancesRepository,
      LandingZone landingZone,
      @Nullable Map<String, SecurityGroupMap> nsgMap) {
    if (zoneInstancesRepository == null || !zoneInstancesRepository.isReady()) {
      throw new IllegalStateException("ZoneInstances repository is not available");
    }
    if (nsgMap == null) {
      throw new IllegalStateException("nsgMap must not be null");
    }

    for (Map.Entry<String, SecurityGroupMap> entry : nsgMap.entrySet()) {
      String ziName = entry.getKey();
      SecurityGroupMap nsgRules = entry.getValue();

      String realZiName = ziName.substring(ziName.indexOf('-') + 1);
      String ziUniqueId = BaseInstance.generateUniqueId(landingZone.getName(), realZiName);
      ZoneInstance zi = zoneInstancesRepository.get(ziUniqueId);

      if (zi == null) {
        throw new IllegalStateException(
            "Zone instance " + ziUniqueId + " not found in processed landing zone");
      }

      RangeSet<BigInteger> ziDestinationCIDRs = zi.getInternalDstCidrRangeSetForNSG();
      RangeSet<BigInteger> otherDestination = TreeRangeSet.create();

      for (NetworkRule inbound : nsgRules.getInbound()) {
        RangeSet<BigInteger> inboundDestinationCIDRSs = inbound.getDstCidrRangeSet();
        if ((PortUtils.ALL_RANGE_SET.equals(inbound.getPortsRangeSet()))
            && (Protocol.STAR.equals(inbound.getProtocol()))
            && (!inboundDestinationCIDRSs.equals(ziDestinationCIDRs))
            && (!IPv4Utils.ALL_RANGE_SET.equals(inboundDestinationCIDRSs))) {
          otherDestination.addAll(inbound.getDstCidrRangeSet());
        }
      }

      for (NetworkRule inbound : nsgRules.getInbound()) {
        RangeSet<BigInteger> inboundDestinationCIDRSs = inbound.getDstCidrRangeSet();
        if ((PortUtils.ALL_RANGE_SET.equals(inbound.getPortsRangeSet()))
            && (Protocol.STAR.equals(inbound.getProtocol()))
            && (!inboundDestinationCIDRSs.equals(ziDestinationCIDRs))
            && (!IPv4Utils.ALL_RANGE_SET.equals(inboundDestinationCIDRSs))) {
          inbound.setDstCidrRangeSet(otherDestination);
        }
      }
    }
  }

  public void removeNotNeededConnectivity(
      @Nullable Set<ZoneInstance> landingZoneZoneInstances, Map<String, SecurityGroupMap> nsgMap) {
    if (landingZoneZoneInstances == null) {
      return;
    }
    for (ZoneInstance zoneInstance : landingZoneZoneInstances) {
      if (zoneInstance.getZoneType().isTechnical() && !zoneInstance.isNsgNeeded()) {
        nsgMap.remove(String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName()));
      }
    }
  }

  public void addPaasSpecificConnectivity(
      Version version,
      @Nullable SubnetInstancesRepository subnetInstancesRepository,
      @Nullable ZoneInstancesRepository zoneInstancesRepository,
      @Nullable Set<ZoneInstance> landingZoneZoneInstances,
      LandingZone landingZone,
      Map<String, SecurityGroupMap> nsgMap) {
    if (landingZoneZoneInstances == null) {
      return;
    }

    if (zoneInstancesRepository == null || !zoneInstancesRepository.isReady()) {
      throw new IllegalStateException(
          "ZoneInstances repository is not available for version " + version.getVersionNumber());
    }

    if (subnetInstancesRepository == null || !subnetInstancesRepository.isReady()) {
      throw new IllegalStateException(
          "SubnetInstances repository is not available for version " + version.getVersionNumber());
    }

    Service allService = getAllService(version);
    if (allService == null) {
      throw new IllegalStateException(
          "ALL service not found for version " + version.getVersionNumber());
    }

    Set<ComponentInstance> componentInstances = landingZone.getComponentInstances();
    if (componentInstances == null) {
      return;
    }

    Map<String, SubnetInstance> lzSubnetsByName = new HashMap<>();
    for (SubnetInstance si :
        Optional.ofNullable(landingZone.getSubnetInstances()).orElseGet(Set::of)) {
      lzSubnetsByName.put(si.getName(), si);
    }

    for (ComponentInstance ci : componentInstances) {
      if (!ci.getComponent().isHosting()) {
        continue;
      }

      if (ci.getEndpointType() != V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
        SubnetInstance siOut = ci.getOutSubnetInstance();
        ZoneInstance ziOut = (siOut != null) ? siOut.getZoneInstance() : null;
        if (ziOut != null && Objects.equals(ziOut.getZoneTypeName(), "PaaS")) {
          addOverlayConnectivity(allService, ci, siOut, nsgMap);
        }
      } else {
        processZonedPaasComponent(
            ci,
            allService,
            lzSubnetsByName,
            zoneInstancesRepository,
            subnetInstancesRepository,
            nsgMap);
      }
    }
  }

  private void processZonedPaasComponent(
      ComponentInstance ci,
      Service allService,
      Map<String, SubnetInstance> lzSubnetsByName,
      ZoneInstancesRepository ziRepo,
      SubnetInstancesRepository siRepo,
      Map<String, SecurityGroupMap> nsgMap) {

    for (Endpoint endpt :
        Optional.ofNullable(ci.getInternalEndpoints(Direction.OUT)).orElse(Set.of())) {
      SubnetInstance siOut = lzSubnetsByName.get(endpt.getDescription());

      if (siOut == null || !Objects.equals(siOut.getZoneInstance().getZoneTypeName(), "PaaS")) {
        continue;
      }

      addOverlayConnectivity(allService, ci, siOut, nsgMap);
      copyNsgRulesFromSourceZones(ci, siOut, ziRepo, siRepo, nsgMap);
    }
  }

  private void addOverlayConnectivity(
      Service allService,
      ComponentInstance componentInstance,
      SubnetInstance subnetInstanceOut,
      Map<String, SecurityGroupMap> nsgMap) {
    List<String> hostingOverlaySubnets = componentInstance.getHostingOverlaySubnets();
    if (hostingOverlaySubnets == null || hostingOverlaySubnets.isEmpty()) {
      return;
    }

    try {
      RangeSet<BigInteger> dstCidrs = IPv4Utils.toRangeSet(hostingOverlaySubnets);
      ZoneInstance zi = subnetInstanceOut.getZoneInstance();
      if (zi == null) {
        return;
      }
      String zoneNsgRuleName = String.join("-", zi.getZoneTypeName(), zi.getName());

      Set<SubnetInstance> subnetInstances =
          Optional.ofNullable(zi.getSubnetInstances()).orElseGet(Set::of);

      for (SubnetInstance subnetInstance : subnetInstances) {
        Range<BigInteger> cidrRange = subnetInstance.getCidrRange();
        if (dstCidrs != null && cidrRange != null) {
          dstCidrs.add(cidrRange);
        }
      }

      for (SubnetInstance subnetInstance : subnetInstances) {
        nsgMap
            .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
            .addInbound(
                createSubnetPort(
                    zoneNsgRuleName,
                    List.of(subnetInstance.getCidr()),
                    zi,
                    dstCidrs,
                    allService,
                    null));
      }

      for (String overlay : hostingOverlaySubnets) {
        nsgMap
            .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
            .addInbound(
                createSubnetPort(
                    zoneNsgRuleName, List.of(overlay), zi, dstCidrs, allService, null));
      }
    } catch (RuntimeException e) {
      Log.errorf(
          "Error while processing overlay connectivity for %s: %s",
          componentInstance.getName(), e.getMessage());
    }
  }

  private void copyNsgRulesFromSourceZones(
      ComponentInstance ci,
      SubnetInstance siTarget,
      ZoneInstancesRepository ziRepo,
      SubnetInstancesRepository siRepo,
      Map<String, SecurityGroupMap> nsgMap) {

    ZoneInstance ziTarget = siTarget.getZoneInstance();
    if (ziTarget == null) {
      return;
    }

    for (Object endpoint : ci.getEndpoints()) {
      if (!(endpoint instanceof Map<?, ?> endpointMap)) {
        continue;
      }

      String directionStr = (String) endpointMap.get("direction");
      if (directionStr == null || Direction.valueOf(directionStr) != Direction.IN) {
        continue;
      }

      ZoneInstance ziSource = resolveSourceZoneInstance(ci, endpointMap, ziRepo, siRepo);
      if (ziSource == null || Objects.equals(ziSource.getName(), ziTarget.getName())) {
        continue;
      }

      String sourceZoneNsgName = String.join("-", ziSource.getZoneTypeName(), ziSource.getName());
      String targetZoneNsgName = String.join("-", ziTarget.getZoneTypeName(), ziTarget.getName());

      SecurityGroupMap sourceSgm = nsgMap.get(sourceZoneNsgName);
      if (sourceSgm == null) {
        continue;
      }

      SecurityGroupMap targetSgm =
          nsgMap.computeIfAbsent(targetZoneNsgName, _ -> new SecurityGroupMap());

      if (targetSgm.getOutbound() == null) {
        targetSgm.setOutbound(sourceSgm.getOutbound());
      }

      for (NetworkRule sourceRule : sourceSgm.getInbound()) {
        RangeSet<BigInteger> cidrRange = sourceRule.getCidrRangeSet();
        TreeRangeSet<BigInteger> cloneCidrRange = null;
        if (cidrRange != null) {
          cloneCidrRange = TreeRangeSet.create(cidrRange);
        }

        RangeSet<BigInteger> dstCidrRange = sourceRule.getDstCidrRangeSet();
        TreeRangeSet<BigInteger> cloneDstCidrRange = null;
        if (dstCidrRange != null) {
          cloneDstCidrRange = TreeRangeSet.create(dstCidrRange);
        }

        NetworkRule copiedRule =
            new NetworkRule(
                sourceRule.getName(),
                cloneCidrRange,
                cloneDstCidrRange,
                TreeRangeSet.create(PortUtils.ALL_RANGE_SET),
                Protocol.STAR,
                sourceRule.getServiceTag());
        targetSgm.addInbound(copiedRule);
      }
    }
  }

  private @Nullable ZoneInstance resolveSourceZoneInstance(
      ComponentInstance ci,
      Map<?, ?> endpointMap,
      ZoneInstancesRepository ziRepo,
      SubnetInstancesRepository siRepo) {

    String lzName = ci.getLandingZoneName();
    String implZiName = (String) endpointMap.get("implementationzone_instance");
    String subnetName = (String) endpointMap.get("subnet_instance");

    if (implZiName != null && !implZiName.isBlank()) {
      return ziRepo.get(BaseInstance.generateUniqueId(lzName, implZiName));
    }

    if (subnetName != null && !subnetName.isBlank()) {
      SubnetInstance si = siRepo.get(BaseInstance.generateUniqueId(lzName, subnetName));
      return (si != null) ? si.getZoneInstance() : null;
    }

    return null;
  }

  public void addSameZoneConnectivity(
      Version version,
      @Nullable Set<ZoneInstance> landingZoneZoneInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    if (landingZoneZoneInstances == null) {
      return;
    }

    Service allService = getAllService(version);
    if (allService == null) {
      throw new IllegalStateException(
          "ALL service not found for version " + version.getVersionNumber());
    }

    for (ZoneInstance zoneInstance : landingZoneZoneInstances) {
      Set<Endpoint> endpoints = new HashSet<>();
      Set<SubnetInstance> subnetInstances = zoneInstance.getSubnetInstances();
      if (subnetInstances == null) {
        continue;
      }
      for (SubnetInstance subnetInstance : subnetInstances) {
        endpoints.addAll(subnetInstance.getInternalEndpoints());
      }

      Set<Range<BigInteger>> filteredCidrs = filterCidrsEndpoints(endpoints);

      if (!filteredCidrs.isEmpty()) {
        String zoneNsgRuleName =
            String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());

        nsgMap
            .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
            .addInbound(
                createSubnetPort(
                    zoneNsgRuleName, filteredCidrs, zoneInstance, null, allService, null));
      }
    }
  }

  public void addBasicConnectivity(
      Version version,
      LandingZone landingZone,
      Set<SubnetInstance> filteredSubnetInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    DependencyInstancesRepository dependencyInstancesRepository =
        version.getDependencyInstancesRepository();
    if (dependencyInstancesRepository == null) {
      Log.warn("No DependencyInstancesRepository found for version " + version.getVersionNumber());
      throw new IllegalCallerException(
          "Repository not ready yet for version " + version.getVersionNumber());
    }
    ComponentInstancesRepository componentInstancesRepository =
        version.getComponentInstancesRepository();
    if (componentInstancesRepository == null) {
      Log.warn("No ComponentInstancesRepository found for version " + version.getVersionNumber());
      throw new IllegalCallerException(
          "Repository not ready yet for version " + version.getVersionNumber());
    }
    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null) {
      Log.warn("No ServicesRepository found for version " + version.getVersionNumber());
      throw new IllegalCallerException(
          "Repository not ready yet for version " + version.getVersionNumber());
    }

    List<Edge> edges =
        graphEngineService.getAllEdgesByLandingZones(
            version.getVersionNumber(), List.of(landingZone.id()), EdgeDirection.INBOUND);

    for (Edge edge : edges) {
      processEdgeForNsg(
          edge,
          landingZone,
          filteredSubnetInstances,
          nsgMap,
          dependencyInstancesRepository,
          componentInstancesRepository,
          servicesRepository);
    }
  }

  /** Processes a single edge for NSG rule generation. */
  private void processEdgeForNsg(
      Edge edge,
      LandingZone landingZone,
      Set<SubnetInstance> filteredSubnetInstances,
      Map<String, SecurityGroupMap> nsgMap,
      DependencyInstancesRepository dependencyInstancesRepository,
      ComponentInstancesRepository componentInstancesRepository,
      ServicesRepository servicesRepository) {
    DependencyInstance dependencyInstance =
        dependencyInstancesRepository.get(edge.getDependencyInstanceId());
    ComponentInstance sourceComponentInstance =
        componentInstancesRepository.get(edge.getSourceNodeId());
    ComponentInstance destinationComponentInstance =
        componentInstancesRepository.get(edge.getTargetNodeId());
    Service service = servicesRepository.get(edge.getServiceId());

    if (dependencyInstance == null) {
      throw new IllegalStateException("No DependencyInstance found for edge " + edge.getName());
    }
    if (sourceComponentInstance == null) {
      throw new IllegalStateException(
          "No Source ComponentInstance found for edge " + edge.getName());
    }
    if (destinationComponentInstance == null) {
      throw new IllegalStateException(
          "No Destination ComponentInstance found for edge " + edge.getName());
    }
    if (service == null) {
      throw new IllegalStateException("No Service found for edge " + edge.getName());
    }

    LandingZone sourceLandingZone = sourceComponentInstance.getLandingZone();
    LandingZone destinationLandingZone = destinationComponentInstance.getLandingZone();

    boolean isInbound = Objects.equals(destinationLandingZone, landingZone);
    boolean isOutbound = Objects.equals(sourceLandingZone, landingZone);

    if (!isInbound && !isOutbound) {
      return;
    }

    if (isInbound) {
      processInboundEdge(
          sourceComponentInstance,
          destinationComponentInstance,
          dependencyInstance,
          service,
          filteredSubnetInstances,
          nsgMap);
    }
  }

  /** Processes inbound edge endpoints for NSG rule generation. */
  private void processInboundEdge(
      ComponentInstance sourceComponentInstance,
      ComponentInstance destinationComponentInstance,
      DependencyInstance dependencyInstance,
      Service service,
      Set<SubnetInstance> filteredSubnetInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    boolean useWideSubnets = dependencyInstance.isUseWideSubnet();
    ServiceTag sourceServiceTag = sourceComponentInstance.getServiceTag();
    ServiceTag destinationServiceTag = destinationComponentInstance.getServiceTag();

    Set<Endpoint> endpoints =
        Optional.ofNullable(destinationComponentInstance.getInternalEndpoints(Direction.IN))
            .orElse(Set.of());
    for (Endpoint endpoint : endpoints) {
      SubnetInstance matchedSubnetInstance = findMatchingSubnet(endpoint, filteredSubnetInstances);
      if (matchedSubnetInstance == null) {
        continue;
      }
      ZoneInstance zoneInstance = matchedSubnetInstance.getZoneInstance();

      if (isSubnetRuleToCreate(sourceComponentInstance, destinationComponentInstance)) {
        addSubnetConnectivityRule(
            sourceComponentInstance,
            destinationComponentInstance,
            dependencyInstance,
            service,
            zoneInstance,
            useWideSubnets,
            nsgMap);
      }

      String zoneNsgRuleName =
          String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());
      if (sourceServiceTag != null && destinationServiceTag == null) {
        addServiceTagRule(
            sourceComponentInstance,
            service,
            sourceServiceTag,
            zoneInstance,
            zoneNsgRuleName,
            nsgMap);
      }
    }
  }

  private static SubnetInstance findMatchingSubnet(
      Endpoint endpoint, Set<SubnetInstance> filteredSubnetInstances) {
    for (SubnetInstance subnetInstance : filteredSubnetInstances) {
      if (Objects.equals(endpoint.getValue(), subnetInstance.getCidr())) {
        return subnetInstance;
      }
    }
    return null;
  }

  /** Adds subnet-based inbound NSG rules, including via-component rules when applicable. */
  private void addSubnetConnectivityRule(
      ComponentInstance sourceComponentInstance,
      ComponentInstance destinationComponentInstance,
      DependencyInstance dependencyInstance,
      Service service,
      ZoneInstance zoneInstance,
      boolean useWideSubnets,
      Map<String, SecurityGroupMap> nsgMap) {
    String zoneNsgRuleName =
        String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());
    Target sourceLandingZoneTarget = sourceComponentInstance.getLandingZone().getTarget();
    if (Objects.equals(
            sourceLandingZoneTarget.getProviderName(), Constants.MICROSOFT_AZURE_PROVIDER)
        && useWideSubnets) {
      for (Endpoint endp : sourceLandingZoneTarget.getInternalEndpoints(true)) {
        nsgMap
            .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
            .addInbound(
                createSubnetPort(
                    zoneNsgRuleName,
                    List.of(endp.getCidrRange()),
                    zoneInstance,
                    null,
                    service,
                    null));
      }
    } else {
      nsgMap
          .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
          .addInbound(
              createSubnetPort(
                  zoneNsgRuleName, null, zoneInstance, null, service, sourceComponentInstance));
    }

    addViaComponentRule(
        sourceComponentInstance, destinationComponentInstance, dependencyInstance, nsgMap);
  }

  /** Adds via-component inbound NSG rule if a via component instance is defined. */
  private void addViaComponentRule(
      ComponentInstance sourceComponentInstance,
      ComponentInstance destinationComponentInstance,
      DependencyInstance dependencyInstance,
      Map<String, SecurityGroupMap> nsgMap) {
    ComponentInstance viaComponentInstance = dependencyInstance.getViaComponentInstance();
    if (viaComponentInstance == null) {
      return;
    }
    Service viaService = dependencyInstance.getViaService();
    ZoneInstance viaZoneInstance = viaComponentInstance.getInZoneInstance();
    if (viaZoneInstance == null) {
      viaZoneInstance = viaComponentInstance.getInSubnetInstance().getZoneInstance();
    }
    ZoneInstance dstZoneInstance = destinationComponentInstance.getInZoneInstance();
    if (dstZoneInstance == null) {
      dstZoneInstance = destinationComponentInstance.getInSubnetInstance().getZoneInstance();
    }
    String viaZoneNsgRuleName =
        String.join("-", viaZoneInstance.getZoneTypeName(), viaZoneInstance.getName());
    nsgMap
        .computeIfAbsent(viaZoneNsgRuleName, _ -> new SecurityGroupMap())
        .addInbound(
            createSubnetPort(
                viaZoneNsgRuleName,
                null,
                viaZoneInstance,
                dstZoneInstance.getInternalDstCidrRangeSetForNSG(),
                viaService,
                sourceComponentInstance));
  }

  /** Adds a service-tag based inbound NSG rule. */
  private void addServiceTagRule(
      ComponentInstance sourceComponentInstance,
      Service service,
      ServiceTag sourceServiceTag,
      ZoneInstance zoneInstance,
      String zoneNsgRuleName,
      Map<String, SecurityGroupMap> nsgMap) {
    RangeSet<BigInteger> dstCidrRange = null;
    if (sourceServiceTag.getAnyNeededInNsg()) {
      dstCidrRange = TreeRangeSet.create();
      dstCidrRange.add(IPv4Utils.toRange(IPv4Utils.ALL_WILDCARD));
    }

    nsgMap
        .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
        .addInbound(
            createSubnetPortFromServiceTag(
                zoneNsgRuleName, zoneInstance, dstCidrRange, service, sourceComponentInstance));
  }

  public void addEastWestConnectivity(
      Version version,
      Target landingZoneTarget,
      @Nullable Set<ZoneInstance> landingZoneZoneInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    if (!landingZoneTarget.isEnableVnetToVnet() || landingZoneZoneInstances == null) {
      return;
    }

    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null || !servicesRepository.isReady()) {
      throw new IllegalStateException(
          "Services repository is not available for version " + version.getVersionNumber());
    }

    Set<Endpoint> endpoints = landingZoneTarget.getInternalEndpoints(true);
    if (endpoints.isEmpty()) {
      endpoints = landingZoneTarget.getInternalEndpoints();
    }

    addConnectivity(
        landingZoneZoneInstances,
        endpoints,
        servicesRepository.getTcpService(),
        servicesRepository.getUdpService(),
        nsgMap);
  }

  private void addConnectivity(
      Set<ZoneInstance> landingZoneZoneInstances,
      Set<Endpoint> endpoints,
      Service tcpService,
      Service udpService,
      Map<String, SecurityGroupMap> nsgMap) {
    Set<Range<BigInteger>> filteredCidrs = filterCidrsEndpoints(endpoints);

    for (ZoneInstance zoneInstance : landingZoneZoneInstances) {
      RangeSet<BigInteger> destinationCidrs = TreeRangeSet.create();
      Set<SubnetInstance> subnetInstances = zoneInstance.getSubnetInstances();
      if (subnetInstances != null) {
        for (SubnetInstance subnetInstance : subnetInstances) {
          if (!subnetInstance.isGroup()
              && !Objects.equals(subnetInstance.getSubnetTypeName(), Constants.GLOBAL)) {
            Range<BigInteger> cidrRange = subnetInstance.getCidrRange();
            if (cidrRange != null) {
              destinationCidrs.add(cidrRange);
            }
          }
        }
      }

      String zoneNsgRuleName =
          String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());
      SecurityGroupMap securityGroupMap =
          nsgMap.computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap());
      securityGroupMap.addInbound(
          createSubnetPort(
              zoneNsgRuleName, filteredCidrs, zoneInstance, destinationCidrs, tcpService, null));
      securityGroupMap.addInbound(
          createSubnetPort(
              zoneNsgRuleName, filteredCidrs, zoneInstance, destinationCidrs, udpService, null));
    }
  }

  private static Set<Range<BigInteger>> filterCidrsEndpoints(Set<Endpoint> endpoints) {
    return (Set<Range<BigInteger>>) filterCidrsEndpoints(endpoints, new HashSet<>());
  }

  private static Collection<Range<BigInteger>> filterCidrsEndpoints(
      Set<Endpoint> endpoints, Collection<Range<BigInteger>> filteredCidrsSet) {
    for (Endpoint endp : endpoints) {
      if (endp.getType() == Endpoint.TypeEnum.CIDR) {
        filteredCidrsSet.add(endp.getCidrRange());
      }
    }
    return filteredCidrsSet;
  }

  public void addJumpServerConnectivity(
      Version version,
      @Nullable ComponentsRepository componentsRepository,
      Target landingZoneTarget,
      @Nullable Set<ZoneInstance> landingZoneZoneInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    if (componentsRepository == null || !componentsRepository.isReady()) {
      throw new IllegalStateException(
          "Components repository is not available for version " + version.getVersionNumber());
    }
    if (!landingZoneTarget.isEnableJumpServerAccess() || landingZoneZoneInstances == null) {
      return;
    }

    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null || !servicesRepository.isReady()) {
      throw new IllegalStateException(
          "Services repository is not available for version " + version.getVersionNumber());
    }

    Map<String, Set<Endpoint>> endpointsMap = new HashMap<>();
    Component jumpServer = componentsRepository.get("JumpServer");
    if (jumpServer == null) {
      jumpServer = componentsRepository.get("JumpServer Linux");
    }
    if (jumpServer != null) {
      Set<ComponentInstance> jumpServerComponentInstances = jumpServer.getAllComponentInstances();
      if (jumpServerComponentInstances != null) {
        for (ComponentInstance jumpServerInstance : jumpServerComponentInstances) {
          Set<Endpoint> endpoints = jumpServerInstance.getInternalEndpoints(Direction.OUT);
          if (endpoints != null) {
            endpointsMap
                .computeIfAbsent(
                    jumpServerInstance.getLandingZone().getTargetName(), _ -> new HashSet<>())
                .addAll(endpoints);
          }
        }
      }
    }

    Service tcpService = servicesRepository.getTcpService();
    Service udpService = servicesRepository.getUdpService();
    for (Set<Endpoint> endpoints : endpointsMap.values()) {
      addConnectivity(landingZoneZoneInstances, endpoints, tcpService, udpService, nsgMap);
    }
  }

  public void addOfficeConnectivity(
      Version version,
      @Nullable LandingZonesRepository landingZonesRepository,
      Target landingZoneTarget,
      @Nullable Set<ZoneInstance> landingZoneZoneInstances,
      Map<String, SecurityGroupMap> nsgMap) {
    if (landingZonesRepository == null || !landingZonesRepository.isReady()) {
      throw new IllegalStateException(
          "LandingZones repository is not available for version " + version.getVersionNumber());
    }
    if (!landingZoneTarget.isEnableOfficeConnectivity() || landingZoneZoneInstances == null) {
      return;
    }
    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null || !servicesRepository.isReady()) {
      throw new IllegalStateException(
          "Services repository is not available for version " + version.getVersionNumber());
    }

    LandingZone officesLandingZone = landingZonesRepository.get("Offices");
    if (officesLandingZone != null) {
      addConnectivity(
          landingZoneZoneInstances,
          officesLandingZone.getInternalEndpoints(),
          servicesRepository.getTcpService(),
          servicesRepository.getUdpService(),
          nsgMap);
    }
  }

  public void addTechnicalAccess(
      Version version,
      String landingZoneMacroPhaseName,
      Set<SubnetInstance> filteredSubnetInstances,
      Set<LandingZone> landingZones,
      Map<String, SecurityGroupMap> nsgMap) {
    boolean correctMacroPhase =
        !Objects.equals(landingZoneMacroPhaseName, "INT")
            && !Objects.equals(landingZoneMacroPhaseName, "RND")
            && !Objects.equals(landingZoneMacroPhaseName, "DES")
            && !Objects.equals(landingZoneMacroPhaseName, "TST");
    Service allService = getAllService(version);
    if (allService == null) {
      throw new IllegalStateException(
          "ALL service not found for version " + version.getVersionNumber());
    }

    for (SubnetInstance subnetInstance : filteredSubnetInstances) {
      for (LandingZone lz : landingZones) {
        Set<SubnetInstance> subnetInstances = lz.getSubnetInstances();
        if (subnetInstances == null) {
          continue;
        }
        for (SubnetInstance sb : subnetInstances) {
          ZoneType zoneType = sb.getZoneInstance().getZoneType();
          if (!sb.isGroup()
              && (zoneType.isTechnical() || (zoneType.isPaas() && correctMacroPhase))) {
            ZoneInstance zoneInstance = subnetInstance.getZoneInstance();
            String zoneNsgRuleName =
                String.join("-", zoneInstance.getZoneTypeName(), zoneInstance.getName());

            nsgMap
                .computeIfAbsent(zoneNsgRuleName, _ -> new SecurityGroupMap())
                .addInbound(
                    createSubnetPort(zoneNsgRuleName, sb, zoneInstance, null, allService, null));
          }
        }
      }
    }
  }

  private boolean isSubnetRuleToCreate(
      ComponentInstance sourceComponentInstance, ComponentInstance destinationComponentInstance) {
    Target sourceLandingZoneTarget = sourceComponentInstance.getLandingZone().getTarget();
    if (Objects.equals(
            sourceLandingZoneTarget, destinationComponentInstance.getLandingZone().getTarget())
        && sourceLandingZoneTarget.isEnableVnetToVnet()) {
      return false;
    }

    if (sourceComponentInstance.getComponent().isContainer()
        && destinationComponentInstance.getComponent().isContainer()
        && Objects.equals(
            sourceComponentInstance.getDeployComponentInstance(),
            destinationComponentInstance.getDeployComponentInstance())) {
      return false;
    }

    return sourceComponentInstance.getServiceTag() == null
        && destinationComponentInstance.getServiceTag() == null;
  }

  private NetworkRule createSubnetPort(
      String zoneNsgRuleName,
      Object subnetToAdd,
      ZoneInstance zoneInstance,
      RangeSet<BigInteger> destinationCidrs,
      Service service,
      ComponentInstance componentInstance) {
    if ((subnetToAdd == null && componentInstance == null) || zoneInstance == null) {
      return null;
    }

    List<Range<BigInteger>> cidrs = new ArrayList<>();
    if (subnetToAdd instanceof SubnetInstance subnetInstance) {
      filterCidrsEndpoints(subnetInstance.getInternalEndpoints(), cidrs);
    } else if (subnetToAdd instanceof Collection<?> subnetToAddCollection) {
      for (Object cidr : subnetToAddCollection) {
        if (cidr instanceof String cidrString) {
          cidrs.add(IPv4Utils.toRange(cidrString));
        } else if (cidr instanceof Range<?> rCidr) {
          @SuppressWarnings("unchecked")
          Range<BigInteger> bigIntRange = (Range<BigInteger>) rCidr;
          cidrs.add(bigIntRange);
        }
      }
    } else if (componentInstance != null) {
      filterCidrsEndpoints(componentInstance.getInternalEndpoints(Direction.OUT), cidrs);
    }

    Map<String, CidrRangeConfig> cidrRangeConfigMap = audApiConfig.bundleRanges();
    if (cidrRangeConfigMap != null && !cidrRangeConfigMap.isEmpty()) {
      Set<String> configCidrMatched = new HashSet<>();
      cidrs.removeIf(
          (Range<BigInteger> cidrRange) -> {
            for (Map.Entry<String, CidrRangeConfig> entry : cidrRangeConfigMap.entrySet()) {
              String key = entry.getKey();
              Range<BigInteger> cidrRangeConfig = entry.getValue().cidr();
              if (cidrRangeConfig.encloses(cidrRange)) {
                configCidrMatched.add(key);
                return true;
              }
            }
            return false;
          });

      for (String configCidr : configCidrMatched) {
        cidrs.add(cidrRangeConfigMap.get(configCidr).cidr());
      }
    }

    V1ServiceSpec.ProtocolEnum protocol = service.getProtocol();

    RangeSet<Integer> portsRangeSet = service.getPortsRange();

    String protocolString;
    if (protocol == V1ServiceSpec.ProtocolEnum.STAR) {
      protocolString = "ANY";
    } else {
      protocolString = protocol.toString();
    }

    destinationCidrs = getDestinationCidrs(zoneInstance, destinationCidrs);

    String groupName =
        String.join(
            "_",
            "Clients",
            zoneNsgRuleName,
            protocolString,
            String.valueOf(portsRangeSet.hashCode()));

    return new NetworkRule(
        groupName,
        TreeRangeSet.create(cidrs),
        destinationCidrs,
        portsRangeSet,
        protocol.toString());
  }

  private static RangeSet<BigInteger> getDestinationCidrs(
      ZoneInstance zoneInstance, RangeSet<BigInteger> destinationCidrs) {
    if (destinationCidrs != null) {
      return destinationCidrs;
    }

    return zoneInstance.getInternalDstCidrRangeSetForNSG();
  }

  private NetworkRule createSubnetPortFromServiceTag(
      String zoneNsgRuleName,
      ZoneInstance zoneInstance,
      RangeSet<BigInteger> destinationCidrs,
      Service service,
      ComponentInstance componentInstance) {

    if (componentInstance == null) {
      return null;
    }

    destinationCidrs = getDestinationCidrs(zoneInstance, destinationCidrs);

    String serviceTagName = componentInstance.getServiceTagName();
    String groupName = String.join("_", "STagClients", zoneNsgRuleName, serviceTagName);
    // TODO: Think at how to ensure that at string building time
    groupName = StringUtils.normalize(groupName);

    V1ServiceSpec.ProtocolEnum protocol = service.getProtocol();

    RangeSet<Integer> portsRangeSet = service.getPortsRange();

    return new NetworkRule(
        groupName, serviceTagName, destinationCidrs, portsRangeSet, protocol.toString());
  }

  //  private static List<String> getPorts(
  //      Service service, V1ServiceSpec.ProtocolEnum protocol) {
  //    List<String> ports = new ArrayList<>();
  //    if (protocol != V1ServiceSpec.ProtocolEnum.ICMP) {
  //      String collapsedPorts = service.getCollapsedPorts();
  //      if (collapsedPorts != null && !collapsedPorts.isBlank()) {
  //        ports.addAll(Arrays.asList(collapsedPorts.split(",")));
  //      }
  //    } else {
  //      ports.addAll(PortUtils.ALL_LIST);
  //    }
  //    return ports;
  //  }
}
