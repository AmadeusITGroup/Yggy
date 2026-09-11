package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.SCOPE_NAME;
import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.VERSION_NUMBER;
import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.componentInstance;
import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.minimalSpec;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServiceTagsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class ComponentInstanceServiceTest {
  @Test
  void initStopsAtEveryMissingPrerequisiteRepository() {
    ComponentInstanceService sut = new ComponentInstanceServiceTestFixture().service();
    List<Consumer<Version>> prerequisiteInstallers = prerequisiteInstallers();

    for (int missingIndex = 0; missingIndex < prerequisiteInstallers.size(); missingIndex++) {
      Version version = new Version();
      version.setVersionNumber(VERSION_NUMBER);
      for (int installedIndex = 0; installedIndex < missingIndex; installedIndex++) {
        prerequisiteInstallers.get(installedIndex).accept(version);
      }

      assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

      assertThat(version.getComponentInstancesRepository())
          .as("repository missing at prerequisite index %s", missingIndex)
          .isNull();
    }

    Version versionWithoutScopes = new Version();
    versionWithoutScopes.setVersionNumber(VERSION_NUMBER);
    prerequisiteInstallers.forEach(installer -> installer.accept(versionWithoutScopes));

    assertThatCode(() -> sut.init(versionWithoutScopes)).doesNotThrowAnyException();

    assertThat(versionWithoutScopes.getComponentInstancesRepository()).isNull();
  }

  @Test
  void initWithEmptyDataCreatesEmptyRepositories() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    assertThat(fixture.version.getAutomaticTemplateInstancesRepository().getData()).isEmpty();
    assertThat(fixture.version.getComponentInstancesRepository().getData()).isEmpty();
  }

  @Test
  void initLinksNominalReferencesAndPreservesMissingReferenceBehavior() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component workload =
        fixture.addComponent("workload", false, false, false, Map.of("tier", "backend"));
    Component hosting = fixture.addComponent("hosting", true, false);
    LandingZone landingZone =
        fixture.addLandingZone("landing-a", "Cloud", "target-a", "Europe", "RUN");
    ZoneType zoneType = fixture.addZoneType("direct-zone");
    ZoneInstance inZone = fixture.addZone(landingZone, "zone-in", zoneType);
    ZoneInstance outZone = fixture.addZone(landingZone, "zone-out", zoneType);
    SubnetInstance inSubnet = fixture.addSubnet(landingZone, "subnet-in", inZone);
    SubnetInstance outSubnet = fixture.addSubnet(landingZone, "subnet-out", outZone);
    Namespace namespace = fixture.addNamespace("namespace-a");
    ApplicationPhase applicationPhase = fixture.addApplicationPhase("phase-a");
    ServiceTag serviceTag = fixture.addServiceTag("service-tag-a");
    V1ComponentInstance hostingData =
        componentInstance(
            "hosting-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec(hosting.getName()).applicationPhase("phase-a"));
    V1ComponentInstance linkedData =
        componentInstance(
            "linked-instance",
            SCOPE_NAME,
            landingZone.getName(),
            new V1ComponentInstanceSpec()
                .component(workload.getName())
                .applicationPhase(applicationPhase.getName())
                .inSubnetInstance(inSubnet.getName())
                .outSubnetInstance(outSubnet.getName())
                .inZoneInstance(inZone.getName())
                .outZoneInstance(outZone.getName())
                .namespace(namespace.getName())
                .servicetag(serviceTag.getName())
                .zoneType(zoneType.getName())
                .hostingComponent("hosting-instance"));
    V1ComponentInstance missingData =
        componentInstance(
            "missing-references",
            SCOPE_NAME,
            "missing-landing",
            new V1ComponentInstanceSpec()
                .component("missing-component")
                .applicationPhase("missing-phase")
                .inSubnetInstance("missing-in-subnet")
                .outSubnetInstance("missing-out-subnet")
                .inZoneInstance("missing-in-zone")
                .outZoneInstance("missing-out-zone")
                .namespace("generated-namespace")
                .servicetag("missing-service-tag")
                .zoneType("missing-zone-type")
                .hostingComponent("missing-host"));
    V1ComponentInstance blankData =
        componentInstance(
            "blank-references",
            SCOPE_NAME,
            "",
            minimalSpec("").namespace("").servicetag("").zoneType("").hostingComponent(" "));
    V1ComponentInstance missingScopeData =
        componentInstance(
            "missing-scope",
            "unknown-scope",
            landingZone.getName(),
            new V1ComponentInstanceSpec()
                .component(workload.getName())
                .applicationPhase(applicationPhase.getName())
                .namespace(namespace.getName()));
    V1ComponentInstance nullData =
        componentInstance("null-references", SCOPE_NAME, null, new V1ComponentInstanceSpec());
    fixture.addComponentInstanceData(hostingData);
    fixture.addComponentInstanceData(linkedData);
    fixture.addComponentInstanceData(missingData);
    fixture.addComponentInstanceData(blankData);
    fixture.addComponentInstanceData(missingScopeData);
    fixture.addComponentInstanceData(nullData);
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    ComponentInstance linked =
        fixture.version.getComponentInstancesRepository().get("linked-instance");
    ComponentInstance host =
        fixture.version.getComponentInstancesRepository().get("hosting-instance");
    ComponentInstance missing =
        fixture.version.getComponentInstancesRepository().get("missing-references");
    ComponentInstance blank =
        fixture.version.getComponentInstancesRepository().get("blank-references");
    ComponentInstance missingScope =
        fixture.version.getComponentInstancesRepository().get("missing-scope");
    ComponentInstance nullReferences =
        fixture.version.getComponentInstancesRepository().get("null-references");
    Namespace generatedNamespace = fixture.namespacesRepository.get("generated-namespace");
    assertThat(fixture.version.getComponentInstancesRepository().getData()).hasSize(6);
    assertThat(linked.getScope()).isSameAs(fixture.scope);
    assertThat(linked.getComponent()).isSameAs(workload);
    assertThat(linked.getLandingZone()).isSameAs(landingZone);
    assertThat(linked.getInSubnetInstance()).isSameAs(inSubnet);
    assertThat(linked.getOutSubnetInstance()).isSameAs(outSubnet);
    assertThat(linked.getInZoneInstance()).isSameAs(inZone);
    assertThat(linked.getOutZoneInstance()).isSameAs(outZone);
    assertThat(linked.getNamespace()).isSameAs(namespace);
    assertThat(linked.getApplicationPhase()).isSameAs(applicationPhase);
    assertThat(linked.getServiceTag()).isSameAs(serviceTag);
    assertThat(linked.getZoneType()).isSameAs(zoneType);
    assertThat(linked.getDeployComponentInstance()).isSameAs(host);
    assertThat(linked.getTagsAsMap()).containsEntry("tier", "backend");
    assertThat(fixture.scope.getComponentInstances()).contains(linked);
    assertThat(workload.getComponentInstances()).contains(linked);
    assertThat(landingZone.getComponentInstances()).contains(linked);
    assertThat(inSubnet.getComponentInstances()).contains(linked);
    assertThat(outSubnet.getComponentInstances()).contains(linked);
    assertThat(inZone.getComponentInstances()).contains(linked);
    assertThat(outZone.getComponentInstances()).contains(linked);
    assertThat(namespace.getComponentInstances()).contains(linked);
    assertThat(applicationPhase.getComponentInstances()).contains(linked);
    assertThat(serviceTag.getComponentInstances()).contains(linked);
    assertThat(zoneType.getComponentInstances()).contains(linked);
    assertThat(host.getHostedComponentInstances()).contains(linked);
    assertThat(missing.getComponent()).isNull();
    assertThat(missing.getLandingZone()).isNull();
    assertThat(missing.getInSubnetInstance()).isNull();
    assertThat(missing.getOutSubnetInstance()).isNull();
    assertThat(missing.getInZoneInstance()).isNull();
    assertThat(missing.getOutZoneInstance()).isNull();
    assertThat(missing.getApplicationPhase()).isNull();
    assertThat(missing.getServiceTag()).isNull();
    assertThat(missing.getZoneType()).isNull();
    assertThat(missing.getDeployComponentInstance()).isNull();
    assertThat(missing.getNamespace()).isSameAs(generatedNamespace);
    assertThat(generatedNamespace.isAutomaticallyGenerated()).isTrue();
    assertThat(generatedNamespace.getScope()).isSameAs(fixture.scope);
    assertThat(generatedNamespace.getComment()).contains("missing-references");
    assertThat(generatedNamespace.getComponentInstances()).contains(missing);
    assertThat(blank.getComponent()).isNull();
    assertThat(blank.getLandingZone()).isNull();
    assertThat(blank.getNamespace()).isNull();
    assertThat(missingScope.getScope()).isNull();
    assertThat(missingScope.getComponent()).isNull();
    assertThat(missingScope.getLandingZone()).isNull();
    assertThat(nullReferences.getScope()).isSameAs(fixture.scope);
    assertThat(nullReferences.getComponent()).isNull();
    assertThat(nullReferences.getLandingZone()).isNull();
    assertThat(nullReferences.getInSubnetInstance()).isNull();
    assertThat(nullReferences.getOutSubnetInstance()).isNull();
    assertThat(nullReferences.getInZoneInstance()).isNull();
    assertThat(nullReferences.getOutZoneInstance()).isNull();
    assertThat(nullReferences.getNamespace()).isNull();
    assertThat(nullReferences.getApplicationPhase()).isNull();
    assertThat(nullReferences.getServiceTag()).isNull();
    assertThat(nullReferences.getZoneType()).isNull();
  }

  @Test
  void initKeepsFirstDuplicateAndConflictingComponentInstance() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component firstComponent = fixture.addComponent("first-component", false, false);
    fixture.addComponent("conflicting-component", false, false);
    LandingZone landingZone =
        fixture.addLandingZone("landing-a", "Cloud", "target-a", "Europe", "RUN");
    V1ComponentInstance first =
        componentInstance(
            "duplicate-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("first-component"));
    V1ComponentInstance duplicate =
        componentInstance(
            "duplicate-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("first-component"));
    V1ComponentInstance conflicting =
        componentInstance(
            "duplicate-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("conflicting-component"));
    fixture.addComponentInstanceData(first);
    fixture.addComponentInstanceData(duplicate);
    fixture.addComponentInstanceData(conflicting);
    ComponentInstanceService sut = fixture.service();

    assertThatCode(() -> sut.init(fixture.version)).doesNotThrowAnyException();

    assertThat(fixture.version.getComponentInstancesRepository().getData()).hasSize(1);
    assertThat(
            fixture.version.getComponentInstancesRepository().get("duplicate-instance").getData())
        .isSameAs(first);
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("duplicate-instance")
                .getComponent())
        .isSameAs(firstComponent);
  }

  @Test
  void initHandlesEndpointTypesDirectionsAndMalformedEntries() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    LandingZone landingZone =
        fixture.addLandingZone("landing-a", "Cloud", "target-a", "Europe", "RUN");
    ZoneType zoneType = fixture.addZoneType("paas-zone-type");
    ZoneInstance zoneInstance = fixture.addZone(landingZone, "paas-zone", zoneType);
    ZoneInstance zoneWithoutSubnets =
        fixture.addZone(landingZone, "zone-without-subnets", zoneType);
    zoneWithoutSubnets.setSubnetInstances(null);
    SubnetInstance firstSubnet = fixture.addSubnet(landingZone, "first-subnet", zoneInstance);
    SubnetInstance secondSubnet = fixture.addSubnet(landingZone, "second-subnet", zoneInstance);
    List<Object> endpoints =
        List.of(
            "not-a-map",
            endpoint("implementationzone_instance", "paas-zone", "IN"),
            endpoint("implementationzone_instance", "paas-zone", "D_INOUT"),
            endpoint("implementationzone_instance", "paas-zone", "NONE"),
            endpoint("implementationzone_instance", "zone-without-subnets", "OUT"),
            endpoint("implementationzone_instance", "missing-zone", "OUT"),
            Map.of(
                "implementationzone_instance",
                "",
                "subnet_instance",
                "first-subnet",
                "direction",
                "IN"),
            endpoint("subnet_instance", "first-subnet", "IN"),
            endpoint("subnet_instance", "second-subnet", "D_OUT"),
            endpoint("subnet_instance", "first-subnet", "NONE"),
            endpoint("subnet_instance", "", "IN"),
            endpoint("subnet_instance", "missing-subnet", "OUT"),
            Map.of("direction", "IN"));
    V1ComponentInstance zonedData =
        componentInstance(
            "zoned-endpoints",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(endpoints));
    V1ComponentInstance multiSubnetData =
        componentInstance(
            "multi-subnet-endpoints",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.MULTI_SUBNET_PAA_S)
                .endpoints(List.of(endpoint("subnet_instance", "first-subnet", "D_INOUT"))));
    V1ComponentInstance nullEndpointsData =
        componentInstance(
            "null-endpoints",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(null));
    V1ComponentInstance emptyEndpointsData =
        componentInstance(
            "empty-endpoints",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.MULTI_SUBNET_PAA_S)
                .endpoints(List.of()));
    V1ComponentInstance unrelatedTypeData =
        componentInstance(
            "cidr-endpoints",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST)
                .endpoints(List.of("ignored")));
    fixture.addComponentInstanceData(zonedData);
    fixture.addComponentInstanceData(multiSubnetData);
    fixture.addComponentInstanceData(nullEndpointsData);
    fixture.addComponentInstanceData(emptyEndpointsData);
    fixture.addComponentInstanceData(unrelatedTypeData);
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    ComponentInstance zoned =
        fixture.version.getComponentInstancesRepository().get("zoned-endpoints");
    ComponentInstance multiSubnet =
        fixture.version.getComponentInstancesRepository().get("multi-subnet-endpoints");
    ComponentInstance unrelated =
        fixture.version.getComponentInstancesRepository().get("cidr-endpoints");
    assertThat(zoned.getZoneTypes()).containsExactly(zoneType);
    assertThat(zoned.getPaasZoneInSubnetInstances())
        .containsExactlyInAnyOrder(firstSubnet, secondSubnet);
    assertThat(zoned.getPaasZoneOutSubnetInstances())
        .containsExactlyInAnyOrder(firstSubnet, secondSubnet);
    assertThat(multiSubnet.getPaasZoneOutSubnetInstances())
        .containsExactlyInAnyOrder(firstSubnet, secondSubnet);
    assertThat(multiSubnet.getPaasZoneInSubnetInstances()).isNull();
    assertThat(zoneInstance.getComponentInstances()).contains(zoned);
    assertThat(unrelated.getPaasZoneInSubnetInstances()).isNull();
    assertThat(unrelated.getPaasZoneOutSubnetInstances()).isNull();
  }

  @Test
  void initPropagatesMalformedEndpointDirection() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    LandingZone landingZone =
        fixture.addLandingZone("landing-a", "Cloud", "target-a", "Europe", "RUN");
    ZoneType zoneType = fixture.addZoneType("paas-zone-type");
    fixture.addZone(landingZone, "paas-zone", zoneType);
    V1ComponentInstance malformed =
        componentInstance(
            "malformed-direction",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(
                    List.of(endpoint("implementationzone_instance", "paas-zone", "SIDEWAYS"))));
    fixture.addComponentInstanceData(malformed);
    ComponentInstanceService sut = fixture.service();

    assertThatThrownBy(() -> sut.init(fixture.version))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SIDEWAYS");

    assertThat(fixture.version.getComponentInstancesRepository()).isNull();
  }

  @Test
  void initPropagatesMissingComponentInstanceSpec() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    V1ComponentInstance malformed =
        componentInstance("missing-spec", SCOPE_NAME, "landing-a", null);
    fixture.addComponentInstanceData(malformed);
    ComponentInstanceService sut = fixture.service();

    assertThatThrownBy(() -> sut.init(fixture.version)).isInstanceOf(NullPointerException.class);

    assertThat(fixture.version.getAutomaticTemplateInstancesRepository()).isNull();
    assertThat(fixture.version.getComponentInstancesRepository()).isNull();
  }

  @Test
  void automaticTemplateDetectionHandlesNullBlankAndBothAssociations() {
    V1ComponentInstance none = componentInstance("none", SCOPE_NAME, "", minimalSpec(""));
    V1ComponentInstance blank =
        componentInstance(
            "blank",
            SCOPE_NAME,
            "",
            minimalSpec("").associatedImplementationZone(" ").associatedPlatformComponent("\t"));
    V1ComponentInstance implementation =
        componentInstance(
            "implementation",
            SCOPE_NAME,
            "",
            minimalSpec("").associatedImplementationZone("implementation-zone"));
    V1ComponentInstance platform =
        componentInstance(
            "platform",
            SCOPE_NAME,
            "",
            minimalSpec("").associatedPlatformComponent("platform-component"));
    V1ComponentInstance both =
        componentInstance(
            "both",
            SCOPE_NAME,
            "",
            minimalSpec("")
                .associatedImplementationZone("implementation-zone")
                .associatedPlatformComponent("platform-component"));

    boolean noneDetected = ComponentInstanceService.isAutomaticTemplateInstance(none);
    boolean blankDetected = ComponentInstanceService.isAutomaticTemplateInstance(blank);
    boolean implementationDetected =
        ComponentInstanceService.isAutomaticTemplateInstance(implementation);
    boolean platformDetected = ComponentInstanceService.isAutomaticTemplateInstance(platform);
    boolean bothDetected = ComponentInstanceService.isAutomaticTemplateInstance(both);

    assertThat(noneDetected).isFalse();
    assertThat(blankDetected).isFalse();
    assertThat(implementationDetected).isTrue();
    assertThat(platformDetected).isTrue();
    assertThat(bothDetected).isTrue();
  }

  @Test
  void initInternalsRunsLogicAndInitializesHostingThenWorkloadRelationships() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component hosting = fixture.addComponent("hosting", true, false);
    Component parent = fixture.addComponent("parent", false, false);
    Component workload = fixture.addComponent("workload", false, false);
    workload.addParent(parent);
    LandingZone landingZone =
        fixture.addLandingZone("landing-a", "Cloud", "target-a", "Europe", "RUN");
    V1ComponentInstance hostingData =
        componentInstance(
            "hosting-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec(hosting.getName()).sourcesSelectors(List.of(Map.of("role", "host"))));
    V1ComponentInstance workloadData =
        componentInstance(
            "workload-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec(workload.getName()).sourcesSelectors(List.of(Map.of("role", "worker"))));
    V1ComponentInstance missingComponentData =
        componentInstance(
            "missing-component-instance",
            SCOPE_NAME,
            landingZone.getName(),
            minimalSpec("missing").sourcesSelectors(List.of(Map.of("role", "orphan"))));
    fixture.addComponentInstanceData(hostingData);
    fixture.addComponentInstanceData(workloadData);
    fixture.addComponentInstanceData(missingComponentData);
    AtomicInteger logicInvocationCount = new AtomicInteger();
    AtomicReference<Version> logicVersion = new AtomicReference<>();
    ComponentInstanceLogic componentInstanceLogic =
        (Version version) -> {
          logicInvocationCount.incrementAndGet();
          logicVersion.set(version);
        };
    ComponentInstanceService sut = fixture.service(componentInstanceLogic);
    sut.init(fixture.version);

    sut.initInternals(fixture.version);

    ComponentInstance hostingInstance =
        fixture.version.getComponentInstancesRepository().get("hosting-instance");
    ComponentInstance workloadInstance =
        fixture.version.getComponentInstancesRepository().get("workload-instance");
    ComponentInstance missingComponentInstance =
        fixture.version.getComponentInstancesRepository().get("missing-component-instance");
    assertThat(logicInvocationCount).hasValue(1);
    assertThat(logicVersion).hasValue(fixture.version);
    assertThat(hostingInstance.getTagsAsMap())
        .containsEntry("aud/securityzone", "unknown")
        .containsEntry("aud/provider", "Cloud")
        .containsEntry("aud/target", "target-a")
        .containsEntry("aud/landingzone", "landing-a")
        .containsEntry("aud/macrophase", "RUN");
    assertThat(workloadInstance.getTagsAsMap())
        .containsEntry("aud/securityzone", "unknown")
        .containsEntry("aud/provider", "Cloud");
    assertThat(hostingInstance.getSourcesSelectors()).containsExactly(Map.of("role", "host"));
    assertThat(workloadInstance.getSourcesSelectors()).containsExactly(Map.of("role", "worker"));
    assertThat(missingComponentInstance.getSourcesSelectors())
        .containsExactly(Map.of("role", "orphan"));
    assertThat(workloadInstance.isChildOfComponent("parent")).isTrue();
    assertThat(hostingInstance.isChildOfComponent("hosting")).isTrue();
    assertThat(missingComponentInstance.getTagsAsMap()).isNull();
  }

  @Test
  void initInternalsStopsAfterInvokingLogicWhenRepositoriesAreMissing() {
    List<Version> logicVersions = new ArrayList<>();
    ComponentInstanceLogic componentInstanceLogic = logicVersions::add;
    ComponentInstanceService sut =
        new ComponentInstanceServiceTestFixture().service(componentInstanceLogic);
    Version missingInstances = new Version();
    Version missingComponents = new Version();
    missingComponents.setComponentInstancesRepository(new ComponentInstancesRepository());

    assertThatCode(
            () -> {
              sut.initInternals(missingInstances);
              sut.initInternals(missingComponents);
            })
        .doesNotThrowAnyException();

    assertThat(logicVersions).containsExactly(missingInstances, missingComponents);
  }

  private static Map<String, String> endpoint(
      String referenceKey, String reference, String direction) {
    return Map.of(referenceKey, reference, "direction", direction);
  }

  private static List<Consumer<Version>> prerequisiteInstallers() {
    return List.of(
        (Version version) -> {
          AudDataRepository dataRepository = new AudDataRepository();
          dataRepository.getData().put(V1ComponentInstance.class, List.of());
          version.setAudDataRepository(dataRepository);
        },
        (Version version) -> version.setSubnetInstancesRepository(new SubnetInstancesRepository()),
        (Version version) -> version.setZoneInstancesRepository(new ZoneInstancesRepository()),
        (Version version) -> version.setNamespacesRepository(new NamespacesRepository()),
        (Version version) -> version.setComponentsRepository(new ComponentsRepository()),
        (Version version) -> version.setLandingZonesRepository(new LandingZonesRepository()),
        (Version version) ->
            version.setApplicationPhasesRepository(new ApplicationPhasesRepository()),
        (Version version) -> version.setServiceTagsRepository(new ServiceTagsRepository()),
        (Version version) -> version.setZoneTypesRepository(new ZoneTypesRepository()));
  }
}
