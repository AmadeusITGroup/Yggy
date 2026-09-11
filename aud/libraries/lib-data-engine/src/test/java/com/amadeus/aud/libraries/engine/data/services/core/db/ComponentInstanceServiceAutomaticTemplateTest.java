package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.SCOPE_NAME;
import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.componentInstance;
import static com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceServiceTestFixture.minimalSpec;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class ComponentInstanceServiceAutomaticTemplateTest {
  @Test
  void implementationZoneTemplateGeneratesOnlyMatchingLandingZonesAndLinksEndpoints() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component templateComponent = fixture.addComponent("implementation-workload", false, false);
    Component containerComponent = fixture.addComponent("container-workload", false, true);
    ZoneType matchingZoneType = fixture.addZoneType("matching-zone-type");
    ZoneType otherZoneType = fixture.addZoneType("other-zone-type");
    LandingZone source =
        fixture.addLandingZone("source-lz", "Cloud", "source-target", "Europe", "RUN");
    LandingZone matched =
        fixture.addLandingZone("matched-lz", "Cloud", "matched-target", "Europe", "RUN");
    LandingZone missingPhase =
        fixture.addLandingZone(
            "missing-phase-lz", "Cloud", "missing-phase-target", "Europe", "MISSING");
    LandingZone wrongFunctionalType =
        fixture.addLandingZone(
            "wrong-functional-lz", "Cloud", "wrong-functional-target", "Europe", "RUN");
    LandingZone wrongZoneType =
        fixture.addLandingZone("wrong-zone-type-lz", "Cloud", "wrong-zone-target", "Europe", "RUN");
    LandingZone missingSubnet =
        fixture.addLandingZone(
            "missing-subnet-lz", "Cloud", "missing-subnet-target", "Europe", "RUN");
    fixture.addFunctionalType(matched, "compute");
    fixture.addFunctionalType(missingPhase, "compute");
    fixture.addFunctionalType(wrongZoneType, "compute");
    fixture.addFunctionalType(missingSubnet, "compute");
    ZoneInstance sourceZone = fixture.addZone(source, "source-zone", matchingZoneType);
    fixture.addSubnet(source, "shared-subnet", sourceZone);
    ZoneInstance matchedAssociationZone =
        fixture.addZone(matched, "implementation-zone", matchingZoneType);
    SubnetInstance matchedSharedSubnet =
        fixture.addSubnet(matched, "shared-subnet", matchedAssociationZone);
    ZoneInstance endpointZone = fixture.addZone(matched, "endpoint-zone", matchingZoneType);
    SubnetInstance firstEndpointSubnet =
        fixture.addSubnet(matched, "endpoint-subnet", endpointZone);
    SubnetInstance secondEndpointSubnet =
        fixture.addSubnet(matched, "endpoint-subnet-two", endpointZone);
    fixture.addZoneAlias("duplicate-matched-zone", matchedAssociationZone);
    ZoneInstance missingPhaseAssociationZone =
        fixture.addZone(missingPhase, "implementation-zone", matchingZoneType);
    fixture.addSubnet(missingPhase, "shared-subnet", missingPhaseAssociationZone);
    ZoneInstance wrongFunctionalAssociationZone =
        fixture.addZone(wrongFunctionalType, "implementation-zone", matchingZoneType);
    fixture.addSubnet(wrongFunctionalType, "shared-subnet", wrongFunctionalAssociationZone);
    ZoneInstance wrongTypeAssociationZone =
        fixture.addZone(wrongZoneType, "implementation-zone", otherZoneType);
    fixture.addSubnet(wrongZoneType, "shared-subnet", wrongTypeAssociationZone);
    fixture.addZone(missingSubnet, "implementation-zone", matchingZoneType);
    ApplicationPhase applicationPhase = fixture.addApplicationPhase("RUN");
    List<Object> generatedEndpoints =
        List.of(
            "invalid-entry",
            endpoint("implementationzone_instance", "endpoint-zone", "IN"),
            endpoint("implementationzone_instance", "endpoint-zone", "D_INOUT"),
            endpoint("implementationzone_instance", "endpoint-zone", "NONE"),
            endpoint("implementationzone_instance", "missing-zone", "OUT"),
            Map.of(
                "implementationzone_instance",
                "",
                "subnet_instance",
                "endpoint-subnet",
                "direction",
                "IN"),
            endpoint("subnet_instance", "endpoint-subnet", "IN"),
            endpoint("subnet_instance", "endpoint-subnet-two", "D_OUT"),
            endpoint("subnet_instance", "endpoint-subnet", "NONE"),
            endpoint("subnet_instance", "", "IN"),
            endpoint("subnet_instance", "missing-subnet", "OUT"),
            Map.of("direction", "IN"));
    V1ComponentInstance generatingTemplate =
        componentInstance(
            "implementation-template",
            SCOPE_NAME,
            source.getName(),
            new V1ComponentInstanceSpec()
                .component(templateComponent.getName())
                .applicationPhase("")
                .inSubnetInstance("shared-subnet")
                .outSubnetInstance("shared-subnet")
                .associatedImplementationZone("implementation-zone")
                .associatedFunctionalTypes(List.of("compute"))
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(generatedEndpoints));
    fixture.addComponentInstanceData(generatingTemplate);
    fixture.addComponentInstanceData(
        implementationTemplate(
            "missing-component-template", "missing-component", source, "shared-subnet"));
    fixture.addComponentInstanceData(
        implementationTemplate(
            "missing-source-subnet-template",
            templateComponent.getName(),
            source,
            "absent-subnet"));
    V1ComponentInstance mismatchedOutSubnet =
        implementationTemplate(
            "mismatched-out-template", templateComponent.getName(), source, "shared-subnet");
    mismatchedOutSubnet.getSpec().setOutSubnetInstance("different-subnet");
    fixture.addComponentInstanceData(mismatchedOutSubnet);
    fixture.addComponentInstanceData(
        implementationTemplate(
            "container-template", containerComponent.getName(), source, "shared-subnet"));
    V1ComponentInstance noFunctionalTypes =
        implementationTemplate(
            "no-functional-types-template", templateComponent.getName(), source, "shared-subnet");
    noFunctionalTypes.getSpec().setAssociatedFunctionalTypes(null);
    fixture.addComponentInstanceData(noFunctionalTypes);
    V1ComponentInstance emptyFunctionalTypes =
        implementationTemplate(
            "empty-functional-types-template",
            templateComponent.getName(),
            source,
            "shared-subnet");
    emptyFunctionalTypes.getSpec().setAssociatedFunctionalTypes(List.of());
    emptyFunctionalTypes.getSpec().setAssociatedPlatformComponent(" ");
    fixture.addComponentInstanceData(emptyFunctionalTypes);
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    ComponentInstance generated =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("implementation-template - matched-lz");
    ComponentInstance generatedWithoutPhase =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("implementation-template - missing-phase-lz");
    AutomaticTemplateInstance automaticTemplate =
        fixture.version.getAutomaticTemplateInstancesRepository().get("implementation-template");
    assertThat(fixture.version.getAutomaticTemplateInstancesRepository().getData()).hasSize(7);
    assertThat(fixture.version.getComponentInstancesRepository().getData()).hasSize(2);
    assertThat(generated.isAutomaticallyInstantiated()).isTrue();
    assertThat(generated.getName()).isEqualTo("implementation-template - matched-lz");
    assertThat(generated.getComment()).contains("functional types");
    assertThat(generated.getReadableVersion())
        .isEqualTo(ComponentInstanceServiceTestFixture.VERSION_NUMBER);
    assertThat(generated.getScope()).isSameAs(fixture.scope);
    assertThat(generated.getLandingZone()).isSameAs(matched);
    assertThat(generated.getComponent()).isSameAs(templateComponent);
    assertThat(generated.getApplicationPhase()).isSameAs(applicationPhase);
    assertThat(generated.getInSubnetInstance()).isSameAs(matchedSharedSubnet);
    assertThat(generated.getOutSubnetInstance()).isSameAs(matchedSharedSubnet);
    assertThat(generated.getDeployComponentInstance()).isNull();
    assertThat(generated.getZoneType()).isNull();
    assertThat(generated.getNamespace()).isNull();
    assertThat(generated.getAutomaticTemplateInstances()).containsExactly(automaticTemplate);
    assertThat(automaticTemplate.getComponentInstances())
        .containsExactlyInAnyOrder(generated, generatedWithoutPhase);
    assertThat(generated.getPaasZoneInSubnetInstances())
        .containsExactlyInAnyOrder(firstEndpointSubnet, secondEndpointSubnet);
    assertThat(generated.getPaasZoneOutSubnetInstances())
        .containsExactlyInAnyOrder(firstEndpointSubnet, secondEndpointSubnet);
    assertThat(applicationPhase.getComponentInstances()).contains(generated);
    assertThat(matched.getComponentInstances()).contains(generated);
    assertThat(templateComponent.getComponentInstances()).contains(generated);
    assertThat(generatedWithoutPhase.getApplicationPhase()).isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("implementation-template - wrong-functional-lz"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("implementation-template - wrong-zone-type-lz"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("implementation-template - missing-subnet-lz"))
        .isNull();
  }

  @Test
  void platformTemplateCreatesGeneratedInstancesWithAllSupportedRelationships() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component container = fixture.addComponent("container-workload", false, true);
    Component platform = fixture.addComponent("hosting-platform", true, false);
    ZoneType zoneType = fixture.addZoneType("platform-zone-type");
    LandingZone source =
        fixture.addLandingZone("source-lz", "Amadeus", "source-target", "Europe", "RUN");
    LandingZone deployed =
        fixture.addLandingZone("deployed-lz", "Cloud", "deployed-target", "Europe", "RUN");
    LandingZone missingPhase =
        fixture.addLandingZone("missing-phase-lz", "Cloud", "phase-target", "Europe", "MISSING");
    LandingZone emptyPhase =
        fixture.addLandingZone("empty-phase-lz", "Cloud", "empty-target", "Europe", "");
    LandingZone nullPhase =
        fixture.addLandingZone("null-phase-lz", "Cloud", "null-target", "Europe", "NULL");
    nullPhase.getMacroPhase().setName(null);
    ZoneInstance deployedZone = fixture.addZone(deployed, "common-zone", zoneType);
    SubnetInstance deployedSubnet = fixture.addSubnet(deployed, "common-subnet", deployedZone);
    ZoneInstance missingPhaseZone = fixture.addZone(missingPhase, "common-zone", zoneType);
    fixture.addSubnet(missingPhase, "common-subnet", missingPhaseZone);
    ZoneInstance emptyPhaseZone = fixture.addZone(emptyPhase, "common-zone", zoneType);
    fixture.addSubnet(emptyPhase, "common-subnet", emptyPhaseZone);
    ZoneInstance nullPhaseZone = fixture.addZone(nullPhase, "common-zone", zoneType);
    fixture.addSubnet(nullPhase, "common-subnet", nullPhaseZone);
    Namespace generatedNamespace;
    ApplicationPhase applicationPhase = fixture.addApplicationPhase("RUN");
    fixture.addComponentInstanceData(
        platformInstance("platform-deployed", platform, deployed, "common-subnet"));
    fixture.addComponentInstanceData(
        platformInstance("platform-missing-phase", platform, missingPhase, "common-subnet"));
    fixture.addComponentInstanceData(
        platformInstance("platform-empty-phase", platform, emptyPhase, "common-subnet"));
    fixture.addComponentInstanceData(
        platformInstance("platform-null-phase", platform, nullPhase, "common-subnet"));
    V1ComponentInstance linkedTemplate =
        componentInstance(
            "linked-platform-template",
            SCOPE_NAME,
            source.getName(),
            new V1ComponentInstanceSpec()
                .component(container.getName())
                .applicationPhase("")
                .associatedPlatformComponent(platform.getName())
                .zoneType(zoneType.getName())
                .namespace("generated-namespace")
                .inSubnetInstance("common-subnet")
                .outSubnetInstance("common-subnet")
                .inZoneInstance("common-zone")
                .outZoneInstance("common-zone")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(
                    List.of(
                        endpoint("implementationzone_instance", "common-zone", "IN"),
                        endpoint("implementationzone_instance", "common-zone", "D_INOUT"),
                        endpoint("subnet_instance", "common-subnet", "IN"),
                        endpoint("subnet_instance", "common-subnet", "D_OUT"))));
    V1ComponentInstance missingLinksTemplate =
        componentInstance(
            "missing-links-template",
            SCOPE_NAME,
            source.getName(),
            new V1ComponentInstanceSpec()
                .component(container.getName())
                .applicationPhase("")
                .associatedPlatformComponent(platform.getName())
                .zoneType(zoneType.getName())
                .inSubnetInstance("missing-in-subnet")
                .outSubnetInstance("missing-out-subnet")
                .inZoneInstance("missing-in-zone")
                .outZoneInstance("missing-out-zone")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(
                    List.of(
                        "invalid-entry",
                        endpoint("implementationzone_instance", "missing-zone", "OUT"),
                        endpoint("subnet_instance", "missing-subnet", "IN"),
                        Map.of("direction", "IN"))));
    V1ComponentInstance emptyLinksTemplate =
        componentInstance(
            "empty-links-template",
            SCOPE_NAME,
            source.getName(),
            new V1ComponentInstanceSpec()
                .component(container.getName())
                .applicationPhase("")
                .associatedPlatformComponent(platform.getName())
                .zoneType(zoneType.getName())
                .namespace(" ")
                .inSubnetInstance("")
                .outSubnetInstance("")
                .inZoneInstance("")
                .outZoneInstance("")
                .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
                .endpoints(null));
    fixture.addComponentInstanceData(linkedTemplate);
    fixture.addComponentInstanceData(missingLinksTemplate);
    fixture.addComponentInstanceData(emptyLinksTemplate);
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    ComponentInstance platformInstance =
        fixture.version.getComponentInstancesRepository().get("platform-deployed");
    ComponentInstance generated =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("linked-platform-template - platform-deployed");
    ComponentInstance generatedWithoutPhase =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("linked-platform-template - platform-missing-phase");
    ComponentInstance generatedWithMissingLinks =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("missing-links-template - platform-deployed");
    AutomaticTemplateInstance automaticTemplate =
        fixture.version.getAutomaticTemplateInstancesRepository().get("linked-platform-template");
    generatedNamespace = fixture.namespacesRepository.get("generated-namespace");
    assertThat(fixture.version.getComponentInstancesRepository().getData()).hasSize(16);
    assertThat(generated.isAutomaticallyInstantiated()).isTrue();
    assertThat(generated.getScope()).isSameAs(fixture.scope);
    assertThat(generated.getLandingZone()).isSameAs(deployed);
    assertThat(generated.getComponent()).isSameAs(container);
    assertThat(generated.getZoneType()).isSameAs(zoneType);
    assertThat(generated.getNamespace()).isSameAs(generatedNamespace);
    assertThat(generatedNamespace.isAutomaticallyGenerated()).isTrue();
    assertThat(generatedNamespace.getComponentInstances()).contains(generated);
    assertThat(generated.getApplicationPhase()).isSameAs(applicationPhase);
    assertThat(generated.getInSubnetInstance()).isSameAs(deployedSubnet);
    assertThat(generated.getOutSubnetInstance()).isSameAs(deployedSubnet);
    assertThat(generated.getInZoneInstance()).isSameAs(deployedZone);
    assertThat(generated.getOutZoneInstance()).isSameAs(deployedZone);
    assertThat(generated.getDeployComponentInstance()).isSameAs(platformInstance);
    assertThat(platformInstance.getHostedComponentInstances()).contains(generated);
    assertThat(generated.getAutomaticTemplateInstances()).contains(automaticTemplate);
    assertThat(automaticTemplate.getComponentInstances()).contains(generated);
    assertThat(generated.getPaasZoneInSubnetInstances()).containsExactly(deployedSubnet);
    assertThat(generated.getPaasZoneOutSubnetInstances()).containsExactly(deployedSubnet);
    assertThat(generatedWithoutPhase.getApplicationPhase()).isNull();
    assertThat(generatedWithMissingLinks.getNamespace()).isNull();
    assertThat(generatedWithMissingLinks.getInSubnetInstance()).isNull();
    assertThat(generatedWithMissingLinks.getOutSubnetInstance()).isNull();
    assertThat(generatedWithMissingLinks.getInZoneInstance()).isNull();
    assertThat(generatedWithMissingLinks.getOutZoneInstance()).isNull();
    assertThat(generatedWithMissingLinks.getPaasZoneInSubnetInstances()).isNull();
    assertThat(generatedWithMissingLinks.getPaasZoneOutSubnetInstances()).isNull();
  }

  @Test
  void platformTemplateLimitModesSelectOnlyMatchingPlatformInstances() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component container = fixture.addComponent("container-workload", false, true);
    Component platform = fixture.addComponent("hosting-platform", true, false);
    ZoneType zoneType = fixture.addZoneType("platform-zone-type");
    LandingZone source =
        fixture.addLandingZone("source-lz", "Amadeus", "shared-target", "Europe", "RUN");
    LandingZone sameTarget =
        fixture.addLandingZone("same-target-lz", "Amadeus", "shared-target", "Europe", "RUN");
    LandingZone sameContinent =
        fixture.addLandingZone("same-continent-lz", "Cloud", "continent-target", "Europe", "BUILD");
    LandingZone sameMacroPhase =
        fixture.addLandingZone("same-macro-lz", "Cloud", "macro-target", "America", "RUN");
    LandingZone unrelated =
        fixture.addLandingZone("unrelated-lz", "Cloud", "unrelated-target", "America", "BUILD");
    fixture.addFunctionalType(sameContinent, "compute");
    fixture.addApplicationPhase("RUN");
    fixture.addApplicationPhase("BUILD");
    fixture.addComponentInstanceData(platformInstance("platform-source", platform, source, ""));
    fixture.addComponentInstanceData(
        platformInstance("platform-same-target", platform, sameTarget, ""));
    fixture.addComponentInstanceData(
        platformInstance("platform-same-continent", platform, sameContinent, ""));
    fixture.addComponentInstanceData(
        platformInstance("platform-same-macro", platform, sameMacroPhase, ""));
    fixture.addComponentInstanceData(
        platformInstance("platform-unrelated", platform, unrelated, ""));
    V1ComponentInstance functionalTemplate =
        platformTemplate("functional-template", container, platform, zoneType, source);
    functionalTemplate.getSpec().setAssociatedFunctionalTypes(List.of("compute"));
    V1ComponentInstance landingTemplate =
        platformTemplate("landing-template", container, platform, zoneType, source);
    landingTemplate.getSpec().setIsAssociatedPlatformLimitedToLegacyOnly(true);
    landingTemplate.getSpec().setIsAssociatedPlatformLimitedToLandingzone(true);
    V1ComponentInstance targetTemplate =
        platformTemplate("target-template", container, platform, zoneType, source);
    targetTemplate.getSpec().setIsAssociatedPlatformLimitedToTarget(true);
    V1ComponentInstance continentTemplate =
        platformTemplate("continent-template", container, platform, zoneType, source);
    continentTemplate.getSpec().setIsAssociatedPlatformLimitedToContinent(true);
    V1ComponentInstance macroTemplate =
        platformTemplate("macro-template", container, platform, zoneType, source);
    macroTemplate.getSpec().setIsAssociatedPlatformLimitedToMacrophase(true);
    V1ComponentInstance unrestrictedTemplate =
        platformTemplate("unrestricted-template", container, platform, zoneType, source);
    V1ComponentInstance cloudTemplate =
        platformTemplate("cloud-template", container, platform, zoneType, source);
    cloudTemplate.getSpec().setIsAssociatedPlatformLimitedToCloudOnly(true);
    fixture.addComponentInstanceData(functionalTemplate);
    fixture.addComponentInstanceData(landingTemplate);
    fixture.addComponentInstanceData(targetTemplate);
    fixture.addComponentInstanceData(continentTemplate);
    fixture.addComponentInstanceData(macroTemplate);
    fixture.addComponentInstanceData(unrestrictedTemplate);
    fixture.addComponentInstanceData(cloudTemplate);
    ComponentInstanceService sut = fixture.service();

    sut.init(fixture.version);

    assertGenerated(fixture, "functional-template", "platform-same-continent", true);
    assertGenerated(fixture, "functional-template", "platform-source", false);
    assertGenerated(fixture, "landing-template", "platform-source", true);
    assertGenerated(fixture, "landing-template", "platform-same-target", false);
    assertGenerated(fixture, "target-template", "platform-source", true);
    assertGenerated(fixture, "target-template", "platform-same-target", true);
    assertGenerated(fixture, "target-template", "platform-same-continent", false);
    assertGenerated(fixture, "continent-template", "platform-same-continent", true);
    assertGenerated(fixture, "continent-template", "platform-same-macro", false);
    assertGenerated(fixture, "macro-template", "platform-same-macro", true);
    assertGenerated(fixture, "macro-template", "platform-unrelated", false);
    assertGenerated(fixture, "unrestricted-template", "platform-unrelated", true);
    assertGenerated(fixture, "cloud-template", "platform-source", false);
    assertGenerated(fixture, "cloud-template", "platform-same-continent", true);
    assertGenerated(fixture, "cloud-template", "platform-unrelated", true);
  }

  @Test
  void invalidPlatformTemplatesAreDiscardedAndDuplicateGenerationIsMerged() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component container = fixture.addComponent("container-workload", false, true);
    Component nonContainer = fixture.addComponent("non-container-workload", false, false);
    Component platform = fixture.addComponent("hosting-platform", true, false);
    Component nonHostingPlatform = fixture.addComponent("non-hosting-platform", false, false);
    ZoneType hostZoneType = fixture.addZoneType("host-zone-type");
    ZoneType requestedZoneType = fixture.addZoneType("requested-zone-type");
    Namespace existingNamespace = fixture.addNamespace("existing-namespace");
    LandingZone source =
        fixture.addLandingZone("source-lz", "Amadeus", "source-target", "Europe", "RUN");
    LandingZone deployed =
        fixture.addLandingZone("deployed-lz", "Cloud", "deployed-target", "Europe", "RUN");
    ZoneInstance hostZone = fixture.addZone(deployed, "host-zone", hostZoneType);
    fixture.addSubnet(deployed, "host-subnet", hostZone);
    V1ComponentInstance platformInstance =
        platformInstance("platform-instance", platform, deployed, "host-subnet");
    platformInstance
        .getSpec()
        .endpointType(V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S)
        .endpoints(List.of(endpoint("implementationzone_instance", "host-zone", "IN")));
    fixture.addComponentInstanceData(platformInstance);
    fixture.addComponentInstanceData(
        platformInstance("non-hosting-instance", nonHostingPlatform, deployed, "host-subnet"));
    V1ComponentInstance missingComponent =
        platformTemplate("missing-component", container, platform, requestedZoneType, source);
    missingComponent.getSpec().setComponent("absent-component");
    V1ComponentInstance missingLanding =
        platformTemplate("missing-landing", container, platform, requestedZoneType, source);
    missingLanding.getSpec().setAssociatedImplementationZone("also-associated");
    missingLanding.getMetadata().setLandingzone("absent-landing");
    V1ComponentInstance missingPlatform =
        platformTemplate("missing-platform", container, platform, requestedZoneType, source);
    missingPlatform.getSpec().setAssociatedPlatformComponent("absent-platform");
    V1ComponentInstance missingZoneType =
        platformTemplate("missing-zone-type", container, platform, requestedZoneType, source);
    missingZoneType.getSpec().setZoneType("absent-zone-type");
    V1ComponentInstance nonContainerTemplate =
        platformTemplate("non-container", nonContainer, platform, requestedZoneType, source);
    nonContainerTemplate.getSpec().setNamespace("created-before-discard");
    V1ComponentInstance nonHostingPlatformTemplate =
        platformTemplate(
            "non-hosting-platform", container, nonHostingPlatform, requestedZoneType, source);
    V1ComponentInstance noZoneType =
        platformTemplate("no-zone-type", container, platform, requestedZoneType, source);
    noZoneType.getSpec().setZoneType(null);
    V1ComponentInstance blankOptionals =
        platformTemplate("blank-optionals", container, platform, requestedZoneType, source);
    blankOptionals.getSpec().setZoneType(" ");
    blankOptionals.getSpec().setNamespace(" ");
    blankOptionals.getSpec().setAssociatedImplementationZone(" ");
    V1ComponentInstance invalidHostZone =
        platformTemplate("invalid-host-zone", container, platform, requestedZoneType, source);
    fixture.addComponentInstanceData(missingComponent);
    fixture.addComponentInstanceData(missingLanding);
    fixture.addComponentInstanceData(missingPlatform);
    fixture.addComponentInstanceData(missingZoneType);
    fixture.addComponentInstanceData(nonContainerTemplate);
    fixture.addComponentInstanceData(nonHostingPlatformTemplate);
    fixture.addComponentInstanceData(noZoneType);
    fixture.addComponentInstanceData(blankOptionals);
    fixture.addComponentInstanceData(invalidHostZone);
    V1ComponentInstance duplicateOne =
        componentInstance(
            "duplicate-template",
            SCOPE_NAME,
            source.getName(),
            platformTemplateSpec(container, platform, hostZoneType));
    duplicateOne.getMetadata().setLabels(Map.of("variant", "one"));
    duplicateOne.getSpec().setNamespace(existingNamespace.getName());
    V1ComponentInstance duplicateTwo =
        componentInstance(
            "duplicate-template",
            SCOPE_NAME,
            source.getName(),
            platformTemplateSpec(container, platform, hostZoneType));
    duplicateTwo.getMetadata().setLabels(Map.of("variant", "two"));
    duplicateTwo.getSpec().setNamespace(existingNamespace.getName());
    fixture.addComponentInstanceData(duplicateOne);
    fixture.addComponentInstanceData(duplicateTwo);
    ComponentInstanceService sut = fixture.service();

    assertThatCode(() -> sut.init(fixture.version)).doesNotThrowAnyException();

    ComponentInstance duplicateGenerated =
        fixture
            .version
            .getComponentInstancesRepository()
            .get("duplicate-template - platform-instance");
    assertThat(fixture.namespacesRepository.get("created-before-discard")).isNotNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("non-container - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("non-hosting-platform - non-hosting-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("no-zone-type - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("blank-optionals - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("invalid-host-zone - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("missing-zone-type - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("missing-platform - platform-instance"))
        .isNull();
    assertThat(duplicateGenerated).isNotNull();
    assertThat(duplicateGenerated.getNamespace()).isSameAs(existingNamespace);
    assertThat(duplicateGenerated.getAutomaticTemplateInstances()).hasSize(2);
  }

  @Test
  void directAutomaticCollaboratorCanExposeNullableAutomaticTemplateState() {
    ComponentInstanceServiceTestFixture fixture = new ComponentInstanceServiceTestFixture();
    Component implementationComponent =
        fixture.addComponent("implementation-workload", false, false);
    Component container = fixture.addComponent("container-workload", false, true);
    Component platform = fixture.addComponent("hosting-platform", true, false);
    ZoneType zoneType = fixture.addZoneType("zone-type");
    LandingZone source =
        fixture.addLandingZone("source-lz", "Amadeus", "source-target", "Europe", "RUN");
    LandingZone deployed =
        fixture.addLandingZone("deployed-lz", "Cloud", "deployed-target", "Europe", "RUN");
    ZoneInstance sourceZone = fixture.addZone(source, "source-zone", zoneType);
    fixture.addSubnet(source, "shared-subnet", sourceZone);
    fixture.addComponentInstanceData(platformInstance("platform-instance", platform, deployed, ""));
    V1ComponentInstance implementationData =
        implementationTemplate(
            "nullable-functional-implementation",
            implementationComponent.getName(),
            source,
            "shared-subnet");
    AutomaticTemplateInstance implementationTemplate =
        automaticTemplate(implementationData, implementationComponent, source, fixture);
    V1ComponentInstance nullPlatformData =
        platformTemplate("null-platform", container, platform, zoneType, source);
    AutomaticTemplateInstance nullPlatformTemplate =
        automaticTemplate(nullPlatformData, container, source, fixture);
    V1ComponentInstance blankPlatformData =
        platformTemplate("blank-platform", container, platform, zoneType, source);
    AutomaticTemplateInstance blankPlatformTemplate =
        automaticTemplate(blankPlatformData, container, source, fixture);
    V1ComponentInstance nullFunctionalData =
        platformTemplate("null-functional-platform", container, platform, zoneType, source);
    AutomaticTemplateInstance nullFunctionalTemplate =
        automaticTemplate(nullFunctionalData, container, source, fixture);
    AutomaticTemplateInstanceService automaticTemplateInstanceService =
        new CallbackAutomaticTemplateInstanceService(
            (Consumer<AutomaticTemplateInstance> callback) -> {
              callback.accept(implementationTemplate);
              implementationTemplate.setAssociatedFunctionalTypes(null);
              callback.accept(nullPlatformTemplate);
              nullPlatformData.getSpec().setAssociatedPlatformComponent(null);
              callback.accept(blankPlatformTemplate);
              blankPlatformData.getSpec().setAssociatedPlatformComponent(" ");
              callback.accept(nullFunctionalTemplate);
              nullFunctionalTemplate.setAssociatedFunctionalTypes(null);
            });
    ComponentInstanceService sut =
        configure(
            new ComponentInstanceService(
                automaticTemplateInstanceService, new EmptyComponentInstanceLogic()));

    sut.init(fixture.version);

    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("nullable-functional-implementation - deployed-lz"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("null-platform - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("blank-platform - platform-instance"))
        .isNull();
    assertThat(
            fixture
                .version
                .getComponentInstancesRepository()
                .get("null-functional-platform - platform-instance"))
        .isNotNull();
  }

  private static final class CallbackAutomaticTemplateInstanceService
      extends AutomaticTemplateInstanceService {
    private final Consumer<Consumer<AutomaticTemplateInstance>> callbackDriver;

    private CallbackAutomaticTemplateInstanceService(
        Consumer<Consumer<AutomaticTemplateInstance>> callbackDriver) {
      this.callbackDriver = callbackDriver;
    }

    @Override
    public void initAutomaticTemplateInstances(
        Version version,
        List<ModelElement> automaticTemplateInstances,
        @Nullable Consumer<AutomaticTemplateInstance> callback) {
      if (callback != null) {
        callbackDriver.accept(callback);
      }
    }
  }

  private static V1ComponentInstance implementationTemplate(
      String name, String componentName, LandingZone source, String inSubnet) {
    return componentInstance(
        name,
        SCOPE_NAME,
        source.getName(),
        new V1ComponentInstanceSpec()
            .component(componentName)
            .applicationPhase("")
            .inSubnetInstance(inSubnet)
            .outSubnetInstance(inSubnet)
            .associatedImplementationZone("implementation-zone")
            .associatedFunctionalTypes(List.of("compute")));
  }

  private static V1ComponentInstance platformInstance(
      String name, Component platform, LandingZone landingZone, String subnetName) {
    return componentInstance(
        name,
        SCOPE_NAME,
        landingZone.getName(),
        minimalSpec(platform.getName()).inSubnetInstance(subnetName).outSubnetInstance(subnetName));
  }

  private static V1ComponentInstance platformTemplate(
      String name, Component container, Component platform, ZoneType zoneType, LandingZone source) {
    return componentInstance(
        name, SCOPE_NAME, source.getName(), platformTemplateSpec(container, platform, zoneType));
  }

  private static V1ComponentInstanceSpec platformTemplateSpec(
      Component container, Component platform, ZoneType zoneType) {
    return new V1ComponentInstanceSpec()
        .component(container.getName())
        .applicationPhase("")
        .associatedPlatformComponent(platform.getName())
        .zoneType(zoneType.getName());
  }

  private static AutomaticTemplateInstance automaticTemplate(
      V1ComponentInstance data,
      Component component,
      LandingZone landingZone,
      ComponentInstanceServiceTestFixture fixture) {
    AutomaticTemplateInstance automaticTemplateInstance = new AutomaticTemplateInstance(data);
    automaticTemplateInstance.setComponent(component);
    automaticTemplateInstance.setLandingZone(landingZone);
    automaticTemplateInstance.setScope(fixture.scope);
    return automaticTemplateInstance;
  }

  private static void assertGenerated(
      ComponentInstanceServiceTestFixture fixture,
      String templateName,
      String platformInstanceName,
      boolean expected) {
    ComponentInstance generated =
        fixture
            .version
            .getComponentInstancesRepository()
            .get(String.join(" - ", templateName, platformInstanceName));
    assertThat(generated != null).isEqualTo(expected);
  }

  private static Map<String, String> endpoint(
      String referenceKey, String reference, String direction) {
    return Map.of(referenceKey, reference, "direction", direction);
  }
}
