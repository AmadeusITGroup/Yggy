package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServiceTagsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class ComponentInstanceServiceTestFixture {
  static final String SCOPE_NAME = "application-scope";
  static final String VERSION_NUMBER = "1.2.3";

  final ApplicationPhasesRepository applicationPhasesRepository = new ApplicationPhasesRepository();
  final AudDataRepository dataRepository = new AudDataRepository();
  final ComponentsRepository componentsRepository = new ComponentsRepository();
  final LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
  final NamespacesRepository namespacesRepository = new NamespacesRepository();
  final ScopesRepository scopesRepository = new ScopesRepository();
  final ServiceTagsRepository serviceTagsRepository = new ServiceTagsRepository();
  final SubnetInstancesRepository subnetInstancesRepository = new SubnetInstancesRepository();
  final Version version = new Version();
  final ZoneInstancesRepository zoneInstancesRepository = new ZoneInstancesRepository();
  final ZoneTypesRepository zoneTypesRepository = new ZoneTypesRepository();
  final Scope scope;

  private final List<V1ComponentInstance> componentInstanceData = new ArrayList<>();
  private final Map<String, Continent> continents = new LinkedHashMap<>();
  private final Map<String, MacroPhase> macroPhases = new LinkedHashMap<>();
  private final Map<String, Provider> providers = new LinkedHashMap<>();
  private final Map<String, Target> targets = new LinkedHashMap<>();

  ComponentInstanceServiceTestFixture() {
    version.setVersionNumber(VERSION_NUMBER);
    version.setAudDataRepository(dataRepository);
    version.setApplicationPhasesRepository(applicationPhasesRepository);
    version.setComponentsRepository(componentsRepository);
    version.setLandingZonesRepository(landingZonesRepository);
    version.setNamespacesRepository(namespacesRepository);
    version.setScopesRepository(scopesRepository);
    version.setServiceTagsRepository(serviceTagsRepository);
    version.setSubnetInstancesRepository(subnetInstancesRepository);
    version.setZoneInstancesRepository(zoneInstancesRepository);
    version.setZoneTypesRepository(zoneTypesRepository);

    scope = entity(new Scope(), SCOPE_NAME);
    scopesRepository.put(scope.id(), scope);
    dataRepository.getData().put(V1ComponentInstance.class, componentInstanceData);
  }

  ComponentInstanceService service() {
    return service(new EmptyComponentInstanceLogic());
  }

  ComponentInstanceService service(ComponentInstanceLogic componentInstanceLogic) {
    AutomaticTemplateInstanceService automaticTemplateInstanceService =
        configure(new AutomaticTemplateInstanceService());
    return configure(
        new ComponentInstanceService(automaticTemplateInstanceService, componentInstanceLogic));
  }

  void addComponentInstanceData(V1ComponentInstance componentInstance) {
    componentInstanceData.add(componentInstance);
  }

  Component addComponent(String name, boolean hosting, boolean container) {
    return addComponent(name, hosting, container, false, Map.of());
  }

  Component addComponent(
      String name,
      boolean hosting,
      boolean container,
      boolean technical,
      Map<String, String> labels) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope(SCOPE_NAME);
    metadata.setLabels(labels);
    V1ComponentSpec spec =
        new V1ComponentSpec().isHosting(hosting).isContainer(container).isTechnical(technical);
    V1Component data = new V1Component();
    data.setMetadata(metadata);
    data.setSpec(spec);
    Component component = new Component(data);
    component.setScope(scope);
    componentsRepository.put(component.id(), component);
    scope.addComponent(component);
    return component;
  }

  LandingZone addLandingZone(
      String name,
      String providerName,
      String targetName,
      String continentName,
      String macroPhaseName) {
    Provider provider = providers.computeIfAbsent(providerName, key -> entity(new Provider(), key));
    Continent continent =
        continents.computeIfAbsent(continentName, key -> entity(new Continent(), key));
    MacroPhase macroPhase =
        macroPhases.computeIfAbsent(macroPhaseName, key -> entity(new MacroPhase(), key));
    Target target =
        targets.computeIfAbsent(
            targetName,
            (String key) -> {
              Target createdTarget = entity(new Target(), key);
              createdTarget.setProvider(provider);
              createdTarget.setContinent(continent);
              createdTarget.setMacroPhase(macroPhase);
              return createdTarget;
            });
    Region region = entity(new Region(), name + "-region");
    region.setProvider(provider);
    region.setContinent(continent);
    LandingZone landingZone = entity(new LandingZone(), name);
    landingZone.setScope(scope);
    landingZone.setTarget(target);
    landingZone.setRegion(region);
    landingZone.setMacroPhase(macroPhase);
    landingZone.setLandingZoneFunctionalTypes(new LinkedHashSet<>());
    landingZonesRepository.put(landingZone.id(), landingZone);
    return landingZone;
  }

  void addFunctionalType(LandingZone landingZone, String functionalTypeName) {
    LandingZoneFunctionalType functionalType =
        entity(new LandingZoneFunctionalType(), functionalTypeName);
    landingZone.addLandingZoneFunctionalType(functionalType);
  }

  ZoneType addZoneType(String name) {
    ZoneType existing = zoneTypesRepository.get(name);
    if (existing != null) {
      return existing;
    }
    ZoneType zoneType = entity(new ZoneType(), name);
    zoneTypesRepository.put(zoneType.id(), zoneType);
    return zoneType;
  }

  ZoneInstance addZone(LandingZone landingZone, String name, ZoneType zoneType) {
    ZoneInstance zoneInstance = entity(new ZoneInstance(), name);
    zoneInstance.setUniqueIdFrom(ZoneInstance.generateUniqueId(landingZone.getName(), name));
    zoneInstance.setLandingZone(landingZone);
    zoneInstance.setZoneType(zoneType);
    zoneInstance.setSubnetInstances(new LinkedHashSet<>());
    zoneInstancesRepository.put(zoneInstance.id(), zoneInstance);
    landingZone.addZoneInstance(zoneInstance);
    zoneType.addZoneInstance(zoneInstance);
    return zoneInstance;
  }

  void addZoneAlias(String repositoryKey, ZoneInstance zoneInstance) {
    zoneInstancesRepository.put(repositoryKey, zoneInstance);
  }

  SubnetInstance addSubnet(LandingZone landingZone, String name, ZoneInstance zoneInstance) {
    SubnetInstance subnetInstance = entity(new SubnetInstance(), name);
    subnetInstance.setUniqueIdFrom(SubnetInstance.generateUniqueId(landingZone.getName(), name));
    subnetInstance.setLandingZone(landingZone);
    subnetInstance.setZoneInstance(zoneInstance);
    subnetInstancesRepository.put(subnetInstance.id(), subnetInstance);
    zoneInstance.getSubnetInstances().add(subnetInstance);
    landingZone.addSubnetInstance(subnetInstance);
    return subnetInstance;
  }

  Namespace addNamespace(String name) {
    Namespace namespace = entity(new Namespace(), name);
    namespace.setScope(scope);
    namespacesRepository.put(namespace.id(), namespace);
    return namespace;
  }

  ApplicationPhase addApplicationPhase(String name) {
    ApplicationPhase applicationPhase = entity(new ApplicationPhase(), name);
    applicationPhasesRepository.put(applicationPhase.id(), applicationPhase);
    return applicationPhase;
  }

  ServiceTag addServiceTag(String name) {
    ServiceTag serviceTag = entity(new ServiceTag(), name);
    serviceTagsRepository.put(serviceTag.id(), serviceTag);
    return serviceTag;
  }

  static V1ComponentInstanceSpec minimalSpec(String componentName) {
    return new V1ComponentInstanceSpec()
        .component(componentName)
        .applicationPhase("")
        .inSubnetInstance("")
        .outSubnetInstance("")
        .inZoneInstance("")
        .outZoneInstance("");
  }

  static V1ComponentInstance componentInstance(
      String name, String scopeName, String landingZoneName, V1ComponentInstanceSpec spec) {
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName(name);
    metadata.setScope(scopeName);
    metadata.setLandingzone(landingZoneName);
    V1ComponentInstance componentInstance = new V1ComponentInstance();
    componentInstance.setApiVersion("aud/v1");
    componentInstance.setKind("ComponentInstance");
    componentInstance.setMetadata(metadata);
    componentInstance.setSpec(spec);
    return componentInstance;
  }

  private static <T extends com.amadeus.aud.libraries.models.core.db.base.AbstractBase<?>> T entity(
      T entity, String name) {
    entity.setUniqueIdFrom(name);
    entity.setName(name);
    return entity;
  }
}
