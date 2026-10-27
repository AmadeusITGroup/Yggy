package com.amadeus.aud.libraries.v2.objects.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Display;
import com.amadeus.aud.libraries.backend.models.v2.JsonPathLink;
import com.amadeus.aud.libraries.backend.models.v2.JsonPathPatternLink;
import com.amadeus.aud.libraries.backend.models.v2.Link;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriLink;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectByIdServiceTest {

  private final ObjectByIdService service =
      new ObjectByIdService(new ObjectUtils(new EmptyLinksHandler()));

  @Test
  @DisplayName("should throw NotFoundException when object id not found")
  void getObjectByIdResponseWithInvalidId() {
    TestVersion version = new TestVersion();
    version.addRepository(ObjectTypeListNamesEnum.SERVICES, new ConcurrentRepository<BomElement>());

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(
                        version, ObjectTypeListNamesEnum.SERVICES, "invalid-id", false)
                    .await()
                    .indefinitely());
    assertThat(thrown).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("should return response when object id is found")
  void getObjectByIdResponseWithValidId() {
    TestVersion version = new TestVersion();
    TestEntity element = identified(new TestEntity(), "test-id");
    version.add(ObjectTypeListNamesEnum.SERVICES, element);

    Response<Object> response =
        service
            .getObjectByIdResponse(version, ObjectTypeListNamesEnum.SERVICES, "test-id", false)
            .await()
            .indefinitely();

    assertThat(response).isNotNull();
  }

  @Test
  void getObjectByIdResponseAcceptsStringObjectTypeAndRawAbstractBaseData() {
    TestVersion version = new TestVersion();
    Component component = identified(new Component(), "component-id");
    V1Component rawData = new V1Component();
    component.setData(rawData);
    version.add(ObjectTypeListNamesEnum.COMPONENTS, component);
    Response<Object> response =
        service
            .getObjectByIdResponse(version, new String("components"), "component-id", true)
            .await()
            .indefinitely();

    assertThat(response.data()).isSameAs(rawData);
    assertThat(response.metadata().links()).isNull();
  }

  @Test
  void getObjectByIdResponseReturnsNullRawDataForNonAbstractElement() {
    TestVersion version = new TestVersion();
    BomElement element = () -> "element-id";
    version.add(ObjectTypeListNamesEnum.SERVICES, element);

    Response<Object> response =
        service
            .getObjectByIdResponse(version, ObjectTypeListNamesEnum.SERVICES, "element-id", true)
            .await()
            .indefinitely();

    assertThat(response.data()).isNull();
  }

  @Test
  void getObjectByIdResponseRejectsUnknownObjectType() {
    TestVersion version = new TestVersion();
    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(version, "unknown", "id", false)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Unknown object type: unknown");
  }

  @Test
  void getObjectByIdResponseThrowsWhenRepositoryIsMissing() {
    TestVersion version = new TestVersion();

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(version, ObjectTypeListNamesEnum.SERVICES, "id", false)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(NotFoundException.class)
        .hasMessage("No data for object type: services");
  }

  @Test
  void getObjectByIdResponseThrowsWhenRepositoryDataIsNull() {
    TestVersion version = new TestVersion();
    ConcurrentRepository<BomElement> repository = new ConcurrentRepository<>();
    repository.setData(null);
    version.addRepository(ObjectTypeListNamesEnum.SERVICES, repository);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(version, ObjectTypeListNamesEnum.SERVICES, "id", false)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(NotFoundException.class)
        .hasMessage("No object found with id: id");
  }

  @Test
  void getObjectByIdResponseRejectsDuplicateLogicalIds() {
    TestVersion version = new TestVersion();
    TestEntity first = identified(new TestEntity(), "duplicate");
    TestEntity second = identified(new TestEntity(), "duplicate");
    ConcurrentRepository<BomElement> repository = new ConcurrentRepository<>();
    repository.put("first", first);
    repository.put("second", second);
    version.addRepository(ObjectTypeListNamesEnum.SERVICES, repository);
    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(
                        version, ObjectTypeListNamesEnum.SERVICES, "duplicate", true)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Multiple objects found with id: duplicate");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("objectLinkCases")
  void getObjectByIdResponseBuildsTypeSpecificMetadataLinks(LinkCase linkCase) {
    Response<Object> response = getResponse(linkCase.objectType, linkCase.object);

    assertThat(response.data()).isSameAs(linkCase.object);
    assertThat(describeLinks(response)).containsExactlyElementsOf(linkCase.expectedLinks);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("optionalRelationCases")
  void getObjectByIdResponseOmitsUnavailableOptionalRelationLinks(LinkCase linkCase) {
    Response<Object> response = getResponse(linkCase.objectType, linkCase.object);

    assertThat(describeLinks(response)).containsExactlyElementsOf(linkCase.expectedLinks);
  }

  @Test
  void getObjectByIdResponseAddsScopeAndDefaultHandlerLinks() {
    ApplicationId applicationId = identified(new ApplicationId(), "application-id");
    Scope scope = identified(new Scope(), "scope-id");
    applicationId.setScope(scope);
    UriLink defaultLink = new UriLink("History", "/history/application-id", Display.BUTTON);
    AtomicReference<BomElement> linkedObject = new AtomicReference<>();
    ObjectByIdService serviceWithDefaultLink =
        new ObjectByIdService(
            new ObjectUtils(
                (BomElement object) -> {
                  linkedObject.set(object);
                  return List.of(defaultLink);
                }));

    Response<Object> response =
        getResponse(serviceWithDefaultLink, ObjectTypeListNamesEnum.APPLICATIONIDS, applicationId);

    assertThat(linkedObject).hasValue(applicationId);
    assertThat(describeLinks(response))
        .containsExactly(
            jsonPath("$.scope", data("scopes", "scope-id"), Display.HREF),
            uri("References", data("scopes", "scope-id") + "/references", Display.TAB),
            jsonPath("$.name", data("applicationids", "application-id"), Display.HREF),
            uri("History", "/history/application-id", Display.BUTTON));
  }

  @Test
  void getObjectByIdResponseOmitsCertificationNameLinkAndAcceptsEmptyDefaultLinks() {
    TestEntity certification = identified(new TestEntity(), "certification-id");

    Response<Object> response = getResponse(ObjectTypeListNamesEnum.CERTIFICATIONS, certification);

    assertThat(response.metadata().links()).isEmpty();
  }

  private Response<Object> getResponse(ObjectTypeListNamesEnum objectType, BomElement object) {
    return getResponse(service, objectType, object);
  }

  private Response<Object> getResponse(
      ObjectByIdService objectByIdService, ObjectTypeListNamesEnum objectType, BomElement object) {
    TestVersion version = new TestVersion();
    version.add(objectType, object);

    return objectByIdService
        .getObjectByIdResponse(version, objectType, object.id(), false)
        .await()
        .indefinitely();
  }

  private static Stream<LinkCase> objectLinkCases() {
    return Stream.of(
        applicationPhaseCase(),
        automaticTemplateInstanceCase(),
        componentCase(),
        componentInstanceWithRelationsCase(),
        connectionPhaseCase(),
        continentCase(),
        dependencyCase(),
        dependencyInstanceWithRelationsCase(),
        firewallGroupWithTargetCase(),
        firewallVendorWithGroupsCase(),
        ipAddressInstanceCase(),
        landingZoneCase(),
        landingZoneFunctionalTypeCase(),
        landingZoneTypeCase(),
        macroPhaseCase(),
        namespaceCase(),
        outboundSnatWithComponentInstancesCase(),
        providerCase(),
        regionCase(),
        scopeCase(),
        serviceCase(),
        subnetInstanceCase(),
        targetCase(),
        zoneInstanceCase(),
        zoneTypeCase(),
        noSpecificLinksCase(
            ObjectTypeListNamesEnum.APPLICATIONIDS,
            identified(new ApplicationId(), "application-id"),
            "application-id"),
        noSpecificLinksCase(
            ObjectTypeListNamesEnum.BLOCKEDPORTS,
            identified(new BlockedPort(), "blocked-port"),
            "blocked-port"),
        noSpecificLinksCase(ObjectTypeListNamesEnum.ROLES, identified(new Role(), "role"), "role"),
        noSpecificLinksCase(
            ObjectTypeListNamesEnum.SERVICETAGS,
            identified(new ServiceTag(), "service-tag"),
            "service-tag"),
        noSpecificLinksCase(
            ObjectTypeListNamesEnum.SUBNETTYPES,
            identified(new SubnetType(), "subnet-type"),
            "subnet-type"),
        noSpecificLinksCase(
            ObjectTypeListNamesEnum.TARGETTYPES,
            identified(new TargetType(), "target-type"),
            "target-type"));
  }

  private static Stream<LinkCase> optionalRelationCases() {
    return Stream.of(
        componentInstanceWithoutOptionalRelationsCase(false),
        componentInstanceWithoutOptionalRelationsCase(true),
        dependencyInstanceWithoutOptionalRelationsCase(),
        firewallGroupWithoutTargetCase(),
        firewallVendorWithoutGroupsCase(),
        outboundSnatWithoutComponentInstancesCase(false),
        outboundSnatWithoutComponentInstancesCase(true));
  }

  private static LinkCase applicationPhaseCase() {
    ApplicationPhase applicationPhase = identified(new ApplicationPhase(), "application-phase");
    applicationPhase.setMacroPhase(identified(new MacroPhase(), "macro-phase"));

    return linkCase(
        ObjectTypeListNamesEnum.APPLICATIONPHASES,
        applicationPhase,
        "application-phase",
        jsonPath("$.macro_phase", data("macrophases", "macro-phase"), Display.HREF));
  }

  private static LinkCase automaticTemplateInstanceCase() {
    AutomaticTemplateInstance instance =
        identified(new AutomaticTemplateInstance(), "automatic-template");
    instance.setLandingZone(identified(new LandingZone(), "landing-zone"));
    instance.setComponent(identified(new Component(), "component"));

    return linkCase(
        ObjectTypeListNamesEnum.AUTOMATICTEMPLATEINSTANCES,
        instance,
        "automatic-template",
        jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF),
        jsonPath("$.component", data("components", "component"), Display.HREF));
  }

  private static LinkCase componentCase() {
    Component component = identified(new Component(), "component");

    return linkCase(
        ObjectTypeListNamesEnum.COMPONENTS,
        component,
        "component",
        pattern("$.parents[*]", data("components", "{value}"), Display.HREF),
        pattern(
            "$.access_control.controlled_components[*]",
            data("components", "{value}"),
            Display.HREF),
        pattern(
            "$.access_control.allow[*].applied_to_scopes[*]",
            data("scopes", "{value}"),
            Display.HREF),
        pattern(
            "$.access_control.deny[*].applied_to_scopes[*]",
            data("scopes", "{value}"),
            Display.HREF),
        pattern("$.security_zones[*]", data("zonetypes", "{value}"), Display.HREF),
        uri("Connectivity Table", data("components", "component") + "/connectivity", Display.TAB),
        uri(
            "Instance Repartition",
            data("components", "component") + "/instance-repartition",
            Display.BUTTON));
  }

  private static LinkCase componentInstanceWithRelationsCase() {
    ComponentInstance instance = componentInstanceWithRequiredRelations();
    instance.setNamespace(identified(new Namespace(), "namespace"));
    instance.setInSubnetInstance(identified(new SubnetInstance(), "in-subnet"));
    instance.setOutSubnetInstance(identified(new SubnetInstance(), "out-subnet"));
    instance.setInZoneInstance(identified(new ZoneInstance(), "in-zone"));
    instance.setOutZoneInstance(identified(new ZoneInstance(), "out-zone"));
    instance.setDeployComponentInstance(identified(new ComponentInstance(), "hosting-platform"));
    instance.getData().getSpec().setEndpoints(List.of(new Object()));
    instance.setAutomaticTemplateInstances(
        Set.of(identified(new AutomaticTemplateInstance(), "automatic-template")));
    instance.setZoneType(identified(new ZoneType(), "zone-type"));

    return linkCase(
        ObjectTypeListNamesEnum.COMPONENTINSTANCES,
        instance,
        "component-instance",
        componentInstanceRequiredLinks(
            jsonPath("$.namespace", data("namespaces", "namespace"), Display.HREF),
            jsonPath("$.in_subnet_instance", data("subnetinstances", "in-subnet"), Display.HREF),
            jsonPath("$.out_subnet_instance", data("subnetinstances", "out-subnet"), Display.HREF),
            jsonPath("$.in_zone_instance", data("zoneinstances", "in-zone"), Display.HREF),
            jsonPath("$.out_zone_instance", data("zoneinstances", "out-zone"), Display.HREF),
            jsonPath(
                "$.hosting_platform", data("componentinstances", "hosting-platform"), Display.HREF),
            pattern(
                "$.endpoints[*].implementationzone_instance",
                data("zoneinstances", "landing-zone***{value}"),
                Display.HREF),
            pattern(
                "$.automatic_template_instances[*].name",
                data("automatictemplateinstances", "{value}"),
                Display.HREF),
            jsonPath("$.zone_type", data("zonetypes", "zone-type"), Display.HREF)));
  }

  private static LinkCase componentInstanceWithoutOptionalRelationsCase(boolean emptyCollections) {
    ComponentInstance instance = componentInstanceWithRequiredRelations();
    if (emptyCollections) {
      instance.getData().getSpec().setEndpoints(List.of());
      instance.setAutomaticTemplateInstances(Set.of());
    }

    return linkCase(
        ObjectTypeListNamesEnum.COMPONENTINSTANCES,
        instance,
        "component-instance",
        componentInstanceRequiredLinks());
  }

  private static ComponentInstance componentInstanceWithRequiredRelations() {
    V1ComponentSpec componentSpec = new V1ComponentSpec();
    componentSpec.setIsContainer(true);
    V1Component componentData = new V1Component();
    componentData.setSpec(componentSpec);
    Component component = identified(new Component(), "component");
    component.setData(componentData);
    V1ComponentInstance componentInstanceData = new V1ComponentInstance();
    componentInstanceData.setSpec(new V1ComponentInstanceSpec());
    ComponentInstance instance = identified(new ComponentInstance(), "component-instance");
    instance.setData(componentInstanceData);
    instance.setComponent(component);
    instance.setApplicationPhase(identified(new ApplicationPhase(), "application-phase"));
    instance.setLandingZone(identified(new LandingZone(), "landing-zone"));
    return instance;
  }

  private static String[] componentInstanceRequiredLinks(String... optionalLinks) {
    List<String> links =
        new ArrayList<>(
            List.of(
                jsonPath("$.component", data("components", "component"), Display.HREF),
                jsonPath(
                    "$.application_phase",
                    data("applicationphases", "application-phase"),
                    Display.HREF),
                jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF)));
    links.addAll(List.of(optionalLinks));
    links.add(
        uri(
            "Connectivity Table",
            data("componentinstances", "component-instance") + "/connectivity",
            Display.TAB));
    return links.toArray(String[]::new);
  }

  private static LinkCase connectionPhaseCase() {
    ConnectionPhase connectionPhase = identified(new ConnectionPhase(), "connection-phase");
    connectionPhase.setSourcePhase(identified(new MacroPhase(), "source-phase"));
    connectionPhase.setDestinationPhase(identified(new MacroPhase(), "destination-phase"));

    return linkCase(
        ObjectTypeListNamesEnum.CONNECTIONPHASES,
        connectionPhase,
        "connection-phase",
        jsonPath("$.source_phase", data("macrophases", "source-phase"), Display.HREF),
        jsonPath("$.destination_phase", data("macrophases", "destination-phase"), Display.HREF));
  }

  private static LinkCase continentCase() {
    Continent continent = identified(new Continent(), "continent");

    return linkCase(
        ObjectTypeListNamesEnum.CONTINENTS,
        continent,
        "continent",
        pattern("$.targets[*]", data("targets", "{value}"), Display.HREF),
        pattern("$.regions[*]", data("regions", "{value}"), Display.HREF));
  }

  private static LinkCase dependencyCase() {
    Dependency dependency = identified(new Dependency(), "dependency");
    dependency.setSourceComponent(identified(new Component(), "source-component"));
    dependency.setDestinationComponent(identified(new Component(), "target-component"));

    return linkCase(
        ObjectTypeListNamesEnum.DEPENDENCIES,
        dependency,
        "dependency",
        jsonPath("$.source_component", data("components", "source-component"), Display.HREF),
        jsonPath("$.target_component", data("components", "target-component"), Display.HREF),
        uri(
            "Connectivity Table",
            data("dependencies", "dependency") + "/connectivity",
            Display.TAB));
  }

  private static LinkCase dependencyInstanceWithRelationsCase() {
    DependencyInstance instance = dependencyInstanceWithRequiredRelations();
    Service viaService = service("via-service");
    instance.setViaComponentInstance(identified(new ComponentInstance(), "via-component"));
    instance.setViaService(viaService);
    instance.setComponentInstance(identified(new ComponentInstance(), "source-instance"));
    instance.setTargetComponentInstance(identified(new ComponentInstance(), "target-instance"));

    return linkCase(
        ObjectTypeListNamesEnum.DEPENDENCYINSTANCES,
        instance,
        "dependency-instance",
        dependencyInstanceRequiredLinks(
            jsonPath(
                "$.via_component_instance",
                data("componentinstances", "via-component"),
                Display.HREF),
            jsonPath("$.via_service", data("services", viaService.id()), Display.HREF),
            jsonPath(
                "$.source_component_instance",
                data("componentinstances", "source-instance"),
                Display.HREF),
            jsonPath(
                "$.target_component_instance",
                data("componentinstances", "target-instance"),
                Display.HREF)));
  }

  private static LinkCase dependencyInstanceWithoutOptionalRelationsCase() {
    DependencyInstance instance = dependencyInstanceWithRequiredRelations();

    return linkCase(
        ObjectTypeListNamesEnum.DEPENDENCYINSTANCES,
        instance,
        "dependency-instance",
        dependencyInstanceRequiredLinks());
  }

  private static DependencyInstance dependencyInstanceWithRequiredRelations() {
    DependencyInstance instance = identified(new DependencyInstance(), "dependency-instance");
    instance.setDependency(identified(new Dependency(), "dependency"));
    instance.setLandingZone(identified(new LandingZone(), "landing-zone"));
    return instance;
  }

  private static String[] dependencyInstanceRequiredLinks(String... optionalLinks) {
    List<String> links =
        new ArrayList<>(
            List.of(
                jsonPath("$.dependency", data("dependencies", "dependency"), Display.HREF),
                jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF)));
    links.addAll(List.of(optionalLinks));
    links.add(pattern("$.target_services[*]", data("services", "{value}"), Display.HREF));
    links.add(
        uri(
            "Connectivity Table",
            data("dependencyinstances", "dependency-instance") + "/connectivity",
            Display.TAB));
    return links.toArray(String[]::new);
  }

  private static LinkCase firewallGroupWithTargetCase() {
    FirewallGroup firewallGroup = firewallGroupWithVendor();
    firewallGroup.setTarget(identified(new Target(), "target"));

    return linkCase(
        ObjectTypeListNamesEnum.FIREWALLGROUPS,
        firewallGroup,
        "firewall-group",
        jsonPath("$.firewall_vendor", data("firewallvendors", "firewall-vendor"), Display.HREF),
        jsonPath("$.target", data("targets", "target"), Display.HREF));
  }

  private static LinkCase firewallGroupWithoutTargetCase() {
    FirewallGroup firewallGroup = firewallGroupWithVendor();

    return linkCase(
        ObjectTypeListNamesEnum.FIREWALLGROUPS,
        firewallGroup,
        "firewall-group",
        jsonPath("$.firewall_vendor", data("firewallvendors", "firewall-vendor"), Display.HREF));
  }

  private static FirewallGroup firewallGroupWithVendor() {
    FirewallGroup firewallGroup = identified(new FirewallGroup(), "firewall-group");
    firewallGroup.setFirewallVendor(identified(new FirewallVendor(), "firewall-vendor"));
    return firewallGroup;
  }

  private static LinkCase firewallVendorWithGroupsCase() {
    FirewallVendor firewallVendor = identified(new FirewallVendor(), "firewall-vendor");
    firewallVendor.setFirewallGroups(Set.of(identified(new FirewallGroup(), "firewall-group")));

    return linkCase(
        ObjectTypeListNamesEnum.FIREWALLVENDORS,
        firewallVendor,
        "firewall-vendor",
        pattern("$.firewall_groups[*]", data("firewallgroups", "{value}"), Display.HREF));
  }

  private static LinkCase firewallVendorWithoutGroupsCase() {
    FirewallVendor firewallVendor = identified(new FirewallVendor(), "firewall-vendor");

    return linkCase(ObjectTypeListNamesEnum.FIREWALLVENDORS, firewallVendor, "firewall-vendor");
  }

  private static LinkCase ipAddressInstanceCase() {
    IPAddressInstance instance = identified(new IPAddressInstance(), "ip-address");
    instance.setLandingZone(identified(new LandingZone(), "landing-zone"));
    instance.setComponentInstance(identified(new ComponentInstance(), "component-instance"));
    instance.setUniqueIdFrom("ip-address");

    return linkCase(
        ObjectTypeListNamesEnum.IPADDRESSINSTANCES,
        instance,
        "ip-address",
        jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF),
        jsonPath(
            "$.component_instance",
            data("componentinstances", "component-instance"),
            Display.HREF));
  }

  private static LinkCase landingZoneCase() {
    LandingZone landingZone = identified(new LandingZone(), "landing-zone");
    landingZone.setTarget(identified(new Target(), "target"));
    landingZone.setRegion(identified(new Region(), "region"));
    landingZone.setLandingZoneType(identified(new LandingZoneType(), "landing-zone-type"));
    landingZone.setMacroPhase(identified(new MacroPhase(), "macro-phase"));

    return linkCase(
        ObjectTypeListNamesEnum.LANDINGZONES,
        landingZone,
        "landing-zone",
        jsonPath("$.target", data("targets", "target"), Display.HREF),
        jsonPath("$.region", data("regions", "region"), Display.HREF),
        jsonPath(
            "$.landing_zone_type", data("landingzonetypes", "landing-zone-type"), Display.HREF),
        jsonPath("$.macro_phase", data("macrophases", "macro-phase"), Display.HREF),
        pattern(
            "$.functional_types[*]", data("landingzonefunctionaltypes", "{value}"), Display.HREF),
        uri(
            "Connectivity Table",
            data("landingzones", "landing-zone") + "/connectivity",
            Display.TAB));
  }

  private static LinkCase landingZoneFunctionalTypeCase() {
    LandingZoneFunctionalType type = identified(new LandingZoneFunctionalType(), "functional-type");

    return linkCase(
        ObjectTypeListNamesEnum.LANDINGZONEFUNCTIONALTYPES,
        type,
        "functional-type",
        pattern("$.landing_zones[*]", data("landingzones", "{value}"), Display.HREF));
  }

  private static LinkCase landingZoneTypeCase() {
    LandingZoneType type = identified(new LandingZoneType(), "landing-zone-type");

    return linkCase(
        ObjectTypeListNamesEnum.LANDINGZONETYPES,
        type,
        "landing-zone-type",
        pattern("$.landing_zones[*]", data("landingzones", "{value}"), Display.HREF));
  }

  private static LinkCase macroPhaseCase() {
    MacroPhase macroPhase = identified(new MacroPhase(), "macro-phase");

    return linkCase(
        ObjectTypeListNamesEnum.MACROPHASES,
        macroPhase,
        "macro-phase",
        pattern("$.application_phases[*]", data("applicationphases", "{value}"), Display.HREF),
        pattern("$.targets[*]", data("targets", "{value}"), Display.HREF),
        uri(
            "Connectivity Table",
            data("macrophases", "macro-phase") + "/connectivity",
            Display.TAB));
  }

  private static LinkCase namespaceCase() {
    Namespace namespace = identified(new Namespace(), "namespace");

    return linkCase(
        ObjectTypeListNamesEnum.NAMESPACES,
        namespace,
        "namespace",
        pattern("$.authorized_scopes[*]", data("scopes", "{value}"), Display.HREF),
        uri("Connectivity Table", data("namespaces", "namespace") + "/connectivity", Display.TAB));
  }

  private static LinkCase outboundSnatWithComponentInstancesCase() {
    OutboundSNAT outboundSnat = outboundSnatWithRequiredRelations();
    outboundSnat.setComponentInstances(
        Set.of(identified(new ComponentInstance(), "component-instance")));

    return linkCase(
        ObjectTypeListNamesEnum.OUTBOUNDSNATS,
        outboundSnat,
        "outbound-snat",
        outboundSnatRequiredLinks(
            pattern(
                "$.component_instances[*]", data("componentinstances", "{value}"), Display.HREF)));
  }

  private static LinkCase outboundSnatWithoutComponentInstancesCase(boolean emptyCollection) {
    OutboundSNAT outboundSnat = outboundSnatWithRequiredRelations();
    if (emptyCollection) {
      outboundSnat.setComponentInstances(Set.of());
    }

    return linkCase(
        ObjectTypeListNamesEnum.OUTBOUNDSNATS,
        outboundSnat,
        "outbound-snat",
        outboundSnatRequiredLinks());
  }

  private static OutboundSNAT outboundSnatWithRequiredRelations() {
    OutboundSNAT outboundSnat = identified(new OutboundSNAT(), "outbound-snat");
    outboundSnat.setMacroPhase(identified(new MacroPhase(), "macro-phase"));
    outboundSnat.setContinent(identified(new Continent(), "continent"));
    outboundSnat.setZoneType(identified(new ZoneType(), "zone-type"));
    return outboundSnat;
  }

  private static String[] outboundSnatRequiredLinks(String... optionalLinks) {
    List<String> links =
        new ArrayList<>(
            List.of(
                jsonPath("$.macro_phase", data("macrophases", "macro-phase"), Display.HREF),
                jsonPath("$.continent", data("continents", "continent"), Display.HREF),
                jsonPath("$.zone_type", data("zonetypes", "zone-type"), Display.HREF)));
    links.addAll(List.of(optionalLinks));
    return links.toArray(String[]::new);
  }

  private static LinkCase providerCase() {
    Provider provider = identified(new Provider(), "provider");

    return linkCase(
        ObjectTypeListNamesEnum.PROVIDERS,
        provider,
        "provider",
        pattern("$.regions[*]", data("regions", "{value}"), Display.HREF),
        pattern("$.targets[*]", data("targets", "{value}"), Display.HREF));
  }

  private static LinkCase regionCase() {
    Region region = identified(new Region(), "region");
    region.setProvider(identified(new Provider(), "provider"));
    region.setContinent(identified(new Continent(), "continent"));

    return linkCase(
        ObjectTypeListNamesEnum.REGIONS,
        region,
        "region",
        jsonPath("$.provider", data("providers", "provider"), Display.HREF),
        jsonPath("$.continent", data("continents", "continent"), Display.HREF),
        pattern("$.landing_zones[*]", data("landingzones", "{value}"), Display.HREF),
        uri("Connectivity Table", data("regions", "region") + "/connectivity", Display.TAB));
  }

  private static LinkCase scopeCase() {
    Scope scope = identified(new Scope(), "scope");

    return linkCase(
        ObjectTypeListNamesEnum.SCOPES,
        scope,
        "scope",
        pattern("$.roles[*]", data("roles", "{value}"), Display.HREF),
        uri("Statistics", "/v2/versions/latest/data/scopes/statistics", Display.TAB),
        uri("Release Notes", data("scopes", "scope") + "/release_notes", Display.TAB),
        uri("Deployment", data("scopes", "scope") + "/deployment", Display.TAB),
        uri("PR Reports", data("scopes", "scope") + "/prs", Display.TAB),
        uri("Connectivity Table", data("scopes", "scope") + "/connectivity", Display.TAB));
  }

  private static LinkCase serviceCase() {
    Service service = service("service");

    return linkCase(
        ObjectTypeListNamesEnum.SERVICES,
        service,
        service.id(),
        jsonPath("$.component", data("components", "component"), Display.HREF),
        uri("Connectivity Table", data("services", service.id()) + "/connectivity", Display.TAB));
  }

  private static LinkCase subnetInstanceCase() {
    SubnetInstance subnet = identified(new SubnetInstance(), "subnet");
    subnet.setLandingZone(identified(new LandingZone(), "landing-zone"));
    subnet.setZoneInstance(identified(new ZoneInstance(), "zone-instance"));
    subnet.setSubnetType(identified(new SubnetType(), "subnet-type"));

    return linkCase(
        ObjectTypeListNamesEnum.SUBNETINSTANCES,
        subnet,
        "subnet",
        jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF),
        jsonPath("$.zone_instance", data("zoneinstances", "zone-instance"), Display.HREF),
        jsonPath("$.subnet_type", data("subnettypes", "subnet-type"), Display.HREF));
  }

  private static LinkCase targetCase() {
    Target target = identified(new Target(), "target");
    target.setTargetType(identified(new TargetType(), "target-type"));
    target.setProvider(identified(new Provider(), "provider"));
    target.setMacroPhase(identified(new MacroPhase(), "macro-phase"));
    target.setContinent(identified(new Continent(), "continent"));

    return linkCase(
        ObjectTypeListNamesEnum.TARGETS,
        target,
        "target",
        jsonPath("$.target_type", data("targettypes", "target-type"), Display.HREF),
        jsonPath("$.provider", data("providers", "provider"), Display.HREF),
        jsonPath("$.macro_phase", data("macrophases", "macro-phase"), Display.HREF),
        jsonPath("$.continent", data("continents", "continent"), Display.HREF),
        uri("Connectivity Table", data("targets", "target") + "/connectivity", Display.TAB));
  }

  private static LinkCase zoneInstanceCase() {
    ZoneInstance zoneInstance = identified(new ZoneInstance(), "zone-instance");
    zoneInstance.setZoneType(identified(new ZoneType(), "zone-type"));
    zoneInstance.setLandingZone(identified(new LandingZone(), "landing-zone"));

    return linkCase(
        ObjectTypeListNamesEnum.ZONEINSTANCES,
        zoneInstance,
        "zone-instance",
        jsonPath("$.zone_type", data("zonetypes", "zone-type"), Display.HREF),
        jsonPath("$.landing_zone", data("landingzones", "landing-zone"), Display.HREF));
  }

  private static LinkCase zoneTypeCase() {
    ZoneType zoneType = identified(new ZoneType(), "zone-type");

    return linkCase(
        ObjectTypeListNamesEnum.ZONETYPES,
        zoneType,
        "zone-type",
        uri("Connectivity Table", data("zonetypes", "zone-type") + "/connectivity", Display.TAB));
  }

  private static LinkCase noSpecificLinksCase(
      ObjectTypeListNamesEnum objectType, BomElement object, String id) {
    return linkCase(objectType, object, id);
  }

  private static LinkCase linkCase(
      ObjectTypeListNamesEnum objectType, BomElement object, String id, String... specificLinks) {
    List<String> expectedLinks = new ArrayList<>();
    expectedLinks.add(jsonPath("$.name", data(objectType.getUri(), id), Display.HREF));
    expectedLinks.addAll(List.of(specificLinks));
    return new LinkCase(objectType, object, expectedLinks);
  }

  private static <T extends AbstractModelEntity<?>> T identified(T object, String id) {
    object.setUniqueIdFrom(id);
    return object;
  }

  private static Service service(String name) {
    Component component = identified(new Component(), "component");
    component.setName("component");
    Service service = new Service();
    service.setName(name);
    service.setComponent(component);
    return service;
  }

  private static List<String> describeLinks(Response<Object> response) {
    List<String> descriptions = new ArrayList<>();
    for (Link link : response.metadata().links()) {
      descriptions.add(describe(link));
    }
    return descriptions;
  }

  private static String describe(Link link) {
    return switch (link) {
      case JsonPathLink jsonPathLink ->
          jsonPath(jsonPathLink.jsonPath(), jsonPathLink.uri(), jsonPathLink.display());
      case JsonPathPatternLink patternLink ->
          pattern(patternLink.path(), patternLink.replacement(), patternLink.display());
      case UriLink uriLink -> uri(uriLink.title(), uriLink.uri(), uriLink.display());
    };
  }

  private static String data(String objectType, String id) {
    return "/v2/versions/latest/data/" + objectType + "/" + id;
  }

  private static String jsonPath(String path, String target, Display display) {
    return "jsonpath " + path + " -> " + target + " [" + display + "]";
  }

  private static String pattern(String path, String target, Display display) {
    return "jsonpathpattern " + path + " -> " + target + " [" + display + "]";
  }

  private static String uri(String title, String target, Display display) {
    return "uri " + title + " -> " + target + " [" + display + "]";
  }

  private static final class TestEntity extends AbstractBase<V1Component> {}

  private static final class TestVersion extends Version {
    private final Map<String, ConcurrentRepository<? extends BomElement>> repositories =
        new HashMap<>();

    private <T extends BomElement> void add(ObjectTypeListNamesEnum objectType, T object) {
      ConcurrentRepository<T> repository = new ConcurrentRepository<>();
      repository.put("repository-entry", object);
      addRepository(objectType, repository);
    }

    private void addRepository(
        ObjectTypeListNamesEnum objectType, ConcurrentRepository<? extends BomElement> repository) {
      repositories.put(objectType.getRepositoryKey(), repository);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BomElement> ConcurrentRepository<T> getRepository(String objectType) {
      return (ConcurrentRepository<T>) repositories.get(objectType);
    }
  }

  private static final class LinkCase {
    private final ObjectTypeListNamesEnum objectType;
    private final BomElement object;
    private final List<String> expectedLinks;

    private LinkCase(
        ObjectTypeListNamesEnum objectType, BomElement object, List<String> expectedLinks) {
      this.objectType = objectType;
      this.object = object;
      this.expectedLinks = expectedLinks;
    }

    @Override
    public String toString() {
      return objectType.name();
    }
  }
}
