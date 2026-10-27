package com.amadeus.aud.libraries.nsg.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.nsg.models.NetworkRule;
import com.amadeus.aud.libraries.nsg.models.SecurityGroupMap;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.google.common.collect.TreeRangeSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NsgServiceTest {

  private GraphEngineService graphEngineService;
  private AudApiConfig audApiConfig;
  private NsgService nsgService;

  @BeforeEach
  void setUp() {
    graphEngineService = mock(GraphEngineService.class);
    audApiConfig = mock(AudApiConfig.class);
    nsgService = new NsgService(graphEngineService, audApiConfig);
  }

  @Test
  void addPaasSpecificConnectivity_ShouldBeDeterministicWithMultipleOutEndpoints() {
    Version version = mock(Version.class);
    doReturn("test-v1").when(version).getVersionNumber();

    ServicesRepository servicesRepo = mock(ServicesRepository.class);
    doReturn(true).when(servicesRepo).isReady();
    Service allService = mock(Service.class);
    doReturn(ProtocolEnum.TCP).when(allService).getProtocol();
    doReturn(TreeRangeSet.create(PortUtils.ALL_RANGE_SET)).when(allService).getPortsRange();

    doReturn(allService).when(servicesRepo).getAllService();
    doReturn(servicesRepo).when(version).getServicesRepository();

    SubnetInstancesRepository subnetRepo = mock(SubnetInstancesRepository.class);
    doReturn(true).when(subnetRepo).isReady();
    ZoneInstancesRepository zoneRepo = mock(ZoneInstancesRepository.class);
    doReturn(true).when(zoneRepo).isReady();

    LandingZone landingZone = mock(LandingZone.class);

    ZoneInstance paasZone = mock(ZoneInstance.class);
    doReturn("PaaS").when(paasZone).getZoneTypeName();
    doReturn("paas-zone").when(paasZone).getName();
    ZoneType paasType = mock(ZoneType.class);
    doReturn(true).when(paasType).isPaas();
    doReturn(paasType).when(paasZone).getZoneType();

    ComponentInstance ci = mock(ComponentInstance.class);
    Component comp = mock(Component.class);
    doReturn(comp).when(ci).getComponent();
    doReturn(true).when(comp).isHosting();
    doReturn(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S).when(ci).getEndpointType();
    doReturn(List.of("10.244.0.0/16")).when(ci).getHostingOverlaySubnets();
    doReturn(List.of()).when(ci).getEndpoints();

    Endpoint outValid = new Endpoint(Endpoint.TypeEnum.CIDR, "10.0.1.0/24", "subnet-valid");
    Endpoint outGhost = new Endpoint(Endpoint.TypeEnum.CIDR, "10.0.2.0/24", "subnet-ghost");

    SubnetInstance sValid = mock(SubnetInstance.class);
    doReturn("subnet-valid").when(sValid).getName();
    doReturn(paasZone).when(sValid).getZoneInstance();
    doReturn("paas-zone").when(sValid).getZoneInstanceName();
    doReturn("10.0.1.0/24").when(sValid).getCidr();

    doReturn(Set.of(sValid)).when(landingZone).getSubnetInstances();
    doReturn(Set.of(ci)).when(landingZone).getComponentInstances();

    Set<Endpoint> order1 = new LinkedHashSet<>(List.of(outValid, outGhost));
    doReturn(order1).when(ci).getInternalEndpoints(Direction.OUT);
    Map<String, SecurityGroupMap> nsgMap1 = new HashMap<>();
    nsgService.addPaasSpecificConnectivity(
        version, subnetRepo, zoneRepo, Set.of(paasZone), landingZone, nsgMap1);

    Set<Endpoint> order2 = new LinkedHashSet<>(List.of(outGhost, outValid));
    doReturn(order2).when(ci).getInternalEndpoints(Direction.OUT);
    Map<String, SecurityGroupMap> nsgMap2 = new HashMap<>();
    nsgService.addPaasSpecificConnectivity(
        version, subnetRepo, zoneRepo, Set.of(paasZone), landingZone, nsgMap2);

    assertThat(nsgMap1).containsKey("PaaS-paas-zone");
    assertThat(nsgMap1.get("PaaS-paas-zone").getInbound()).isNotEmpty();
    assertThat(nsgMap1)
        .as("NSG generation does not depend on list order")
        .containsExactlyInAnyOrderEntriesOf(nsgMap2);
  }

  @Test
  void addPaasSpecificConnectivity_StandardComponent_ShouldAddOverlayInPaasZone() {
    Version version = setupVersionMock();
    LandingZone lz = mock(LandingZone.class);

    ZoneInstance paasZone = createZoneMock("PaaS", "my-paas");

    ComponentInstance ci = createComponentMock(null, true);
    doReturn(List.of("192.168.1.0/24")).when(ci).getHostingOverlaySubnets();

    SubnetInstance siOut = mock(SubnetInstance.class);
    doReturn(paasZone).when(siOut).getZoneInstance();
    doReturn(siOut).when(ci).getOutSubnetInstance();

    doReturn(Set.of(ci)).when(lz).getComponentInstances();
    doReturn(Set.of(siOut)).when(lz).getSubnetInstances();

    Map<String, SecurityGroupMap> nsgMap = new HashMap<>();

    nsgService.addPaasSpecificConnectivity(
        version,
        version.getSubnetInstancesRepository(),
        version.getZoneInstancesRepository(),
        Set.of(paasZone),
        lz,
        nsgMap);

    assertThat(nsgMap).containsKey("PaaS-my-paas");
    assertThat(nsgMap.get("PaaS-my-paas").getInbound()).isNotEmpty();
  }

  @Test
  void addPaasSpecificConnectivity_NonPaasZone_ShouldIgnore() {
    Version version = setupVersionMock();
    LandingZone lz = mock(LandingZone.class);

    ZoneInstance techZone = createZoneMock("Technical", "my-tech");

    ComponentInstance ci = createComponentMock(null, true);
    SubnetInstance siOut = mock(SubnetInstance.class);
    doReturn(techZone).when(siOut).getZoneInstance();
    doReturn(siOut).when(ci).getOutSubnetInstance();

    doReturn(Set.of(ci)).when(lz).getComponentInstances();
    doReturn(Set.of(siOut)).when(lz).getSubnetInstances();

    Map<String, SecurityGroupMap> nsgMap = new HashMap<>();

    nsgService.addPaasSpecificConnectivity(
        version,
        version.getSubnetInstancesRepository(),
        version.getZoneInstancesRepository(),
        Set.of(techZone),
        lz,
        nsgMap);

    assertThat(nsgMap).isEmpty();
  }

  @Test
  void addPaasSpecificConnectivity_ZonedPaas_ShouldCopyRules() {
    Version version = setupVersionMock();
    LandingZone lz = mock(LandingZone.class);
    ZoneInstancesRepository ziRepo = version.getZoneInstancesRepository();

    ZoneInstance ziSource = createZoneMock("PaaS", "source-zone");
    SecurityGroupMap sourceSgm = new SecurityGroupMap();
    sourceSgm.addInbound(
        new NetworkRule(
            "source-rule",
            TreeRangeSet.create(),
            TreeRangeSet.create(),
            TreeRangeSet.create(),
            "Tcp"));

    ZoneInstance ziTarget = createZoneMock("PaaS", "target-zone");

    ComponentInstance ci =
        createComponentMock(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S, true);
    doReturn("lz1").when(ci).getLandingZoneName();

    Map<String, Object> endptIn = new HashMap<>();
    endptIn.put("direction", "IN");
    endptIn.put("implementationzone_instance", "source-zone");
    doReturn(List.of(endptIn)).when(ci).getEndpoints();

    Endpoint outEndpt = new Endpoint(Endpoint.TypeEnum.CIDR, "10.0.0.1/32", "target-subnet");
    doReturn(Set.of(outEndpt)).when(ci).getInternalEndpoints(Direction.OUT);

    SubnetInstance siTarget = mock(SubnetInstance.class);
    doReturn("target-subnet").when(siTarget).getName();
    doReturn(ziTarget).when(siTarget).getZoneInstance();

    doReturn(Set.of(ci)).when(lz).getComponentInstances();
    doReturn(Set.of(siTarget)).when(lz).getSubnetInstances();

    doReturn(ziSource)
        .when(ziRepo)
        .get(
            com.amadeus.aud.libraries.models.core.db.base.BaseInstance.generateUniqueId(
                "lz1", "source-zone"));

    Map<String, SecurityGroupMap> nsgMap = new HashMap<>();
    nsgMap.put("PaaS-source-zone", sourceSgm);

    nsgService.addPaasSpecificConnectivity(
        version, version.getSubnetInstancesRepository(), ziRepo, Set.of(ziTarget), lz, nsgMap);

    assertThat(nsgMap).containsKey("PaaS-target-zone");
    SecurityGroupMap targetSgm = nsgMap.get("PaaS-target-zone");
    assertThat(targetSgm.getInbound()).extracting(NetworkRule::getName).contains("source-rule");
  }

  private Version setupVersionMock() {
    Version v = mock(Version.class);
    doReturn("test-v1").when(v).getVersionNumber();

    ServicesRepository sr = mock(ServicesRepository.class);
    doReturn(true).when(sr).isReady();
    Service all = mock(Service.class);
    doReturn(ProtocolEnum.TCP).when(all).getProtocol();
    doReturn(TreeRangeSet.create(PortUtils.ALL_RANGE_SET)).when(all).getPortsRange();
    doReturn(all).when(sr).getAllService();
    doReturn(sr).when(v).getServicesRepository();

    SubnetInstancesRepository sir = mock(SubnetInstancesRepository.class);
    doReturn(true).when(sir).isReady();
    doReturn(sir).when(v).getSubnetInstancesRepository();

    ZoneInstancesRepository zir = mock(ZoneInstancesRepository.class);
    doReturn(true).when(zir).isReady();
    doReturn(zir).when(v).getZoneInstancesRepository();

    return v;
  }

  private ZoneInstance createZoneMock(String type, String name) {
    ZoneInstance zi = mock(ZoneInstance.class);
    doReturn(type).when(zi).getZoneTypeName();
    doReturn(name).when(zi).getName();
    ZoneType zt = mock(ZoneType.class);
    doReturn(Objects.equals(type, "PaaS")).when(zt).isPaas();
    doReturn(zt).when(zi).getZoneType();
    return zi;
  }

  private ComponentInstance createComponentMock(
      V1ComponentInstanceSpec.EndpointTypeEnum type, boolean hosting) {
    ComponentInstance ci = mock(ComponentInstance.class);
    Component c = mock(Component.class);
    doReturn(c).when(ci).getComponent();
    doReturn(hosting).when(c).isHosting();
    doReturn(type).when(ci).getEndpointType();
    return ci;
  }

  @Test
  void addBasicConnectivity_throwsIllegalCallerException_whenDependencyInstancesRepositoryIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();
    doReturn(null).when(version).getDependencyInstancesRepository();

    assertThatThrownBy(
            () ->
                nsgService.addBasicConnectivity(
                    version, mock(LandingZone.class), Set.of(), new HashMap<>()))
        .isInstanceOf(IllegalCallerException.class)
        .hasMessageContaining("Repository not ready yet");
  }

  @Test
  void addBasicConnectivity_throwsIllegalCallerException_whenComponentInstancesRepositoryIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();
    doReturn(mock(DependencyInstancesRepository.class))
        .when(version)
        .getDependencyInstancesRepository();
    doReturn(null).when(version).getComponentInstancesRepository();

    assertThatThrownBy(
            () ->
                nsgService.addBasicConnectivity(
                    version, mock(LandingZone.class), Set.of(), new HashMap<>()))
        .isInstanceOf(IllegalCallerException.class)
        .hasMessageContaining("Repository not ready yet");
  }

  @Test
  void addBasicConnectivity_throwsIllegalCallerException_whenServicesRepositoryIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();
    doReturn(mock(DependencyInstancesRepository.class))
        .when(version)
        .getDependencyInstancesRepository();
    doReturn(mock(ComponentInstancesRepository.class))
        .when(version)
        .getComponentInstancesRepository();
    doReturn(null).when(version).getServicesRepository();

    assertThatThrownBy(
            () ->
                nsgService.addBasicConnectivity(
                    version, mock(LandingZone.class), Set.of(), new HashMap<>()))
        .isInstanceOf(IllegalCallerException.class)
        .hasMessageContaining("Repository not ready yet");
  }

  @Test
  void
      addBasicConnectivity_whenViaAndDestinationAreHostedComponents_resolvesZoneFromDeployComponentInstance() {
    // Arrange
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    DependencyInstancesRepository depRepo = mock(DependencyInstancesRepository.class);
    doReturn(true).when(depRepo).isReady();
    doReturn(depRepo).when(version).getDependencyInstancesRepository();

    ComponentInstancesRepository compRepo = mock(ComponentInstancesRepository.class);
    doReturn(true).when(compRepo).isReady();
    doReturn(compRepo).when(version).getComponentInstancesRepository();

    ServicesRepository svcRepo = mock(ServicesRepository.class);
    doReturn(true).when(svcRepo).isReady();
    doReturn(svcRepo).when(version).getServicesRepository();

    LandingZone destinationLandingZone = mock(LandingZone.class);
    doReturn("lz-dest-id").when(destinationLandingZone).id();
    Target destTarget = mock(Target.class);
    doReturn("Azure").when(destTarget).getProviderName();
    doReturn(false).when(destTarget).isEnableVnetToVnet();
    doReturn(destTarget).when(destinationLandingZone).getTarget();

    LandingZone sourceLandingZone = mock(LandingZone.class);
    doReturn("lz-src-id").when(sourceLandingZone).id();
    Target srcTarget = mock(Target.class);
    doReturn("Azure").when(srcTarget).getProviderName();
    doReturn(false).when(srcTarget).isEnableVnetToVnet();
    doReturn(srcTarget).when(sourceLandingZone).getTarget();

    ComponentInstance sourceCi = mock(ComponentInstance.class);
    Component sourceComp = mock(Component.class);
    doReturn(false).when(sourceComp).isContainer();
    doReturn(sourceComp).when(sourceCi).getComponent();
    doReturn(sourceLandingZone).when(sourceCi).getLandingZone();
    doReturn(null).when(sourceCi).getServiceTag();
    Endpoint srcEndpoint = new Endpoint(Endpoint.TypeEnum.CIDR, "10.0.0.1/32", "src-ep");
    doReturn(Set.of(srcEndpoint)).when(sourceCi).getInternalEndpoints(Direction.OUT);

    ZoneInstance destHostZone = createZoneMock("Technical", "dest-host-zone");
    doReturn(TreeRangeSet.create()).when(destHostZone).getInternalDstCidrRangeSetForNSG();

    ComponentInstance destHostCi = mock(ComponentInstance.class);
    doReturn(destHostZone).when(destHostCi).getInZoneInstance();

    ComponentInstance destCi = mock(ComponentInstance.class);
    Component destComp = mock(Component.class);
    doReturn(false).when(destComp).isContainer();
    doReturn(destComp).when(destCi).getComponent();
    doReturn(destinationLandingZone).when(destCi).getLandingZone();
    doReturn(null).when(destCi).getServiceTag();
    doReturn(null).when(destCi).getInZoneInstance();
    doReturn(null).when(destCi).getInSubnetInstance();
    doReturn(destHostCi).when(destCi).getDeployComponentInstance();
    Endpoint destInEndpoint = new Endpoint(Endpoint.TypeEnum.CIDR, "10.1.0.0/24", "dest-ep");
    doReturn(Set.of(destInEndpoint)).when(destCi).getInternalEndpoints(Direction.IN);

    ZoneInstance viaHostZone = createZoneMock("Technical", "via-host-zone");
    doReturn(TreeRangeSet.create()).when(viaHostZone).getInternalDstCidrRangeSetForNSG();

    ComponentInstance viaHostCi = mock(ComponentInstance.class);
    doReturn(viaHostZone).when(viaHostCi).getInZoneInstance();

    ComponentInstance viaCi = mock(ComponentInstance.class);
    doReturn(null).when(viaCi).getInZoneInstance();
    doReturn(null).when(viaCi).getInSubnetInstance();
    doReturn(viaHostCi).when(viaCi).getDeployComponentInstance();

    Service mainService = mock(Service.class);
    doReturn(ProtocolEnum.TCP).when(mainService).getProtocol();
    doReturn(TreeRangeSet.create(PortUtils.ALL_RANGE_SET)).when(mainService).getPortsRange();

    Service viaService = mock(Service.class);
    doReturn(ProtocolEnum.TCP).when(viaService).getProtocol();
    doReturn(TreeRangeSet.create(PortUtils.ALL_RANGE_SET)).when(viaService).getPortsRange();

    DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    doReturn(false).when(dependencyInstance).isUseWideSubnet();
    doReturn(viaCi).when(dependencyInstance).getViaComponentInstance();
    doReturn(viaService).when(dependencyInstance).getViaService();

    ZoneInstance matchedZone = createZoneMock("Technical", "matched-zone");
    doReturn(TreeRangeSet.create()).when(matchedZone).getInternalDstCidrRangeSetForNSG();
    SubnetInstance matchedSubnet = mock(SubnetInstance.class);
    doReturn("10.1.0.0/24").when(matchedSubnet).getCidr();
    doReturn(matchedZone).when(matchedSubnet).getZoneInstance();

    Edge edge = new Edge("edge-test", "dep-1", "svc-main", true, "ci-src", "ci-dest");

    doReturn(List.of(edge))
        .when(graphEngineService)
        .getAllEdgesByLandingZones("1.0.0", List.of("lz-dest-id"), EdgeDirection.INBOUND);

    doReturn(dependencyInstance).when(depRepo).get("dep-1");
    doReturn(sourceCi).when(compRepo).get("ci-src");
    doReturn(destCi).when(compRepo).get("ci-dest");
    doReturn(mainService).when(svcRepo).get("svc-main");

    Map<String, SecurityGroupMap> nsgMap = new HashMap<>();

    // Act
    nsgService.addBasicConnectivity(version, destinationLandingZone, Set.of(matchedSubnet), nsgMap);

    // Assert
    assertThat(nsgMap).containsKey("Technical-matched-zone");
    assertThat(nsgMap).containsKey("Technical-via-host-zone");
    SecurityGroupMap viaNsg = nsgMap.get("Technical-via-host-zone");
    assertThat(viaNsg.getInbound()).isNotEmpty();
  }
}
