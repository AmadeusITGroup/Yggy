package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
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
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Singleton
public class ComponentInstanceService extends BaseService<ComponentInstance, V1ComponentInstance> {
  private static final String GENERATED_COMMENT_PREFIX =
      "Automatically generated as association with";
  private final AutomaticTemplateInstanceService automaticTemplateInstanceService;
  private final ComponentInstanceLogic componentInstanceLogic;

  @Inject
  public ComponentInstanceService(
      AutomaticTemplateInstanceService automaticTemplateInstanceService,
      ComponentInstanceLogic componentInstanceLogic) {
    this.automaticTemplateInstanceService = automaticTemplateInstanceService;
    this.componentInstanceLogic = componentInstanceLogic;
  }

  @Override
  protected ComponentInstancesRepository getRepository(Version version) {
    ComponentInstancesRepository componentInstancesRepository = new ComponentInstancesRepository();
    version.setComponentInstancesRepository(componentInstancesRepository);
    return componentInstancesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    AudDataRepository dataRepository = version.getAudDataRepository();
    if (dataRepository == null) {
      Log.errorf("No data Repository found for version %s", version);
      return;
    }

    SubnetInstancesRepository subnetInstancesRepository = version.getSubnetInstancesRepository();
    if (subnetInstancesRepository == null) {
      Log.errorf("No SubnetInstance Repository found for version %s", version);
      return;
    }

    ZoneInstancesRepository zoneInstancesRepository = version.getZoneInstancesRepository();
    if (zoneInstancesRepository == null) {
      Log.errorf("No ZoneInstance Repository found for version %s", version);
      return;
    }

    NamespacesRepository namespacesRepository = version.getNamespacesRepository();
    if (namespacesRepository == null) {
      Log.errorf("No Namespace Repository found for version %s", version);
      return;
    }

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("No Component Repository found for version %s", version);
      return;
    }

    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("No LandingZone Repository found for version %s", version);
      return;
    }

    ApplicationPhasesRepository applicationPhasesRepository =
        version.getApplicationPhasesRepository();
    if (applicationPhasesRepository == null) {
      Log.errorf("No ApplicationPhase Repository found for version %s", version);
      return;
    }

    ServiceTagsRepository serviceTagsRepository = version.getServiceTagsRepository();
    if (serviceTagsRepository == null) {
      Log.errorf("No ServiceTag Repository found for version %s", version);
      return;
    }

    ZoneTypesRepository zoneTypesRepository = version.getZoneTypesRepository();
    if (zoneTypesRepository == null) {
      Log.errorf("No ZoneType Repository found for version %s", version);
      return;
    }

    List<V1Base> componentInstances = new ArrayList<>();
    List<ModelElement> automaticTemplateInstances = new ArrayList<>();

    for (ModelElement object : dataRepository.getData().get(V1ComponentInstance.class)) {
      V1ComponentInstance componentInstance = (V1ComponentInstance) object;
      if (isAutomaticTemplateInstance(componentInstance)) {
        automaticTemplateInstances.add(object);
      } else {
        componentInstances.add(componentInstance);
      }
    }

    List<AutomaticTemplateInstance> associatedImplementationZones = new ArrayList<>();
    List<AutomaticTemplateInstance> associatedPlatformComponents = new ArrayList<>();
    List<ComponentInstance> hostingComponents = new ArrayList<>();

    automaticTemplateInstanceService.initAutomaticTemplateInstances(
        version,
        automaticTemplateInstances,
        ((AutomaticTemplateInstance automaticTemplateInstance) -> {
          V1ComponentInstanceSpec spec = automaticTemplateInstance.getData().getSpec();
          String associatedPlatformComponent = spec.getAssociatedPlatformComponent();
          String associatedImplementationZone = spec.getAssociatedImplementationZone();
          if (associatedImplementationZone != null && !associatedImplementationZone.isBlank()) {
            associatedImplementationZones.add(automaticTemplateInstance);
          }
          if (associatedPlatformComponent != null && !associatedPlatformComponent.isBlank()) {
            associatedPlatformComponents.add(automaticTemplateInstance);
          }
        }));

    String versionNumber = version.getVersionNumber();

    ConcurrentRepository<ComponentInstance> componentInstancesRepository =
        init(
            version,
            ComponentInstance.class,
            V1ComponentInstance.class,
            componentInstances,
            (ComponentInstance componentInstance, Scope scope) -> {
              if (scope == null) {
                return;
              }

              scope.addComponentInstance(componentInstance);

              V1ComponentInstance data = componentInstance.getData();

              V1ComponentInstanceSpec spec = data.getSpec();
              V1MetadataInstance metadata = data.getMetadata();

              String inSubnetInstance = spec.getInSubnetInstance();
              if (inSubnetInstance != null && !inSubnetInstance.isEmpty()) {
                String subnetInstanceId =
                    SubnetInstance.generateUniqueId(metadata.getLandingzone(), inSubnetInstance);
                SubnetInstance subnetInstance = subnetInstancesRepository.get(subnetInstanceId);
                if (subnetInstance == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing SubnetInstance %s",
                      componentInstance.id(), subnetInstanceId);
                } else {
                  componentInstance.setInSubnetInstance(subnetInstance);
                  subnetInstance.addComponentInstance(componentInstance);
                }
              }

              String outSubnetInstance = spec.getOutSubnetInstance();
              if (outSubnetInstance != null && !outSubnetInstance.isEmpty()) {
                String subnetInstanceId =
                    String.join(
                        SubnetInstance.ID_SEPARATOR, metadata.getLandingzone(), outSubnetInstance);
                SubnetInstance subnetInstance = subnetInstancesRepository.get(subnetInstanceId);
                if (subnetInstance == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing SubnetInstance %s",
                      componentInstance.id(), subnetInstanceId);
                } else {
                  componentInstance.setOutSubnetInstance(subnetInstance);
                  subnetInstance.addComponentInstance(componentInstance);
                }
              }

              String inZoneInstance = spec.getInZoneInstance();
              if (inZoneInstance != null && !inZoneInstance.isEmpty()) {
                String zoneInstanceId =
                    String.join(
                        SubnetInstance.ID_SEPARATOR, metadata.getLandingzone(), inZoneInstance);
                ZoneInstance zoneInstance = zoneInstancesRepository.get(zoneInstanceId);
                if (zoneInstance == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing ZoneInstance %s",
                      componentInstance.id(), zoneInstanceId);
                } else {
                  componentInstance.setInZoneInstance(zoneInstance);
                  zoneInstance.addComponentInstance(componentInstance);
                }
              }

              String outZoneInstance = spec.getOutZoneInstance();
              if (outZoneInstance != null && !outZoneInstance.isEmpty()) {
                String zoneInstanceId =
                    String.join(
                        SubnetInstance.ID_SEPARATOR, metadata.getLandingzone(), outZoneInstance);
                ZoneInstance zoneInstance = zoneInstancesRepository.get(zoneInstanceId);
                if (zoneInstance == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing ZoneInstance %s",
                      componentInstance.id(), zoneInstanceId);
                } else {
                  componentInstance.setOutZoneInstance(zoneInstance);
                  zoneInstance.addComponentInstance(componentInstance);
                }
              }

              String namespaceId = spec.getNamespace();
              if (namespaceId != null && !namespaceId.isEmpty()) {
                Namespace namespace = namespacesRepository.get(namespaceId);
                if (namespace == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing Namespace %s",
                      componentInstance.id(), namespaceId);
                  // TODO: REMOVE it once the users fix the missing Namespace from the source of
                  // data
                  namespace =
                      createGeneratedNamespace(
                          namespaceId,
                          componentInstance.id(),
                          versionNumber,
                          scope,
                          namespacesRepository);
                }
                //                else {
                componentInstance.setNamespace(namespace);
                namespace.addComponentInstance(componentInstance);
                //                }
              }

              String componentId = spec.getComponent();
              if (componentId != null && !componentId.isEmpty()) {
                Component component = componentsRepository.get(componentId);
                if (component == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing Component %s",
                      componentInstance.id(), componentId);
                } else {
                  componentInstance.setComponent(component);
                  component.addComponentInstance(componentInstance);

                  componentInstance.inheritTagsFromComponent();
                }
              }

              String landingZoneId = metadata.getLandingzone();
              if (landingZoneId != null && !landingZoneId.isEmpty()) {
                LandingZone landingZone = landingZonesRepository.get(landingZoneId);
                if (landingZone == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing LandingZone %s",
                      componentInstance.id(), landingZoneId);
                } else {
                  componentInstance.setLandingZone(landingZone);
                  landingZone.addComponentInstance(componentInstance);
                }
              }

              String applicationPhaseId = spec.getApplicationPhase();
              if (applicationPhaseId != null && !applicationPhaseId.isEmpty()) {
                ApplicationPhase applicationPhase =
                    applicationPhasesRepository.get(applicationPhaseId);
                if (applicationPhase == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing ApplicationPhase %s",
                      componentInstance.id(), applicationPhaseId);
                } else {
                  componentInstance.setApplicationPhase(applicationPhase);
                  applicationPhase.addComponentInstance(componentInstance);
                }
              }

              String serviceTagId = spec.getServicetag();
              if (serviceTagId != null && !serviceTagId.isEmpty()) {
                ServiceTag serviceTag = serviceTagsRepository.get(serviceTagId);
                if (serviceTag == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing ServiceTag %s",
                      componentInstance.id(), serviceTagId);
                } else {
                  componentInstance.setServiceTag(serviceTag);
                  serviceTag.addComponentInstance(componentInstance);
                }
              }

              String zoneTypeId = spec.getZoneType();
              if (zoneTypeId != null && !zoneTypeId.isEmpty()) {
                ZoneType zoneType = zoneTypesRepository.get(zoneTypeId);
                if (zoneType == null) {
                  logModelWarning(
                      "ComponentInstance %s references a non-existing ZoneType %s",
                      componentInstance.id(), zoneTypeId);
                } else {
                  componentInstance.setZoneType(zoneType);
                  zoneType.addComponentInstance(componentInstance);
                }
              }

              V1ComponentInstanceSpec.EndpointTypeEnum endpointType = spec.getEndpointType();
              if (endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S
                  || endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.MULTI_SUBNET_PAA_S) {
                List<Object> endpoints = spec.getEndpoints();
                if (endpoints != null) {
                  for (Object endpointObject : endpoints) {
                    if (!(endpointObject instanceof Map)) {
                      Log.errorf(
                          "ComponentInstance %s endpoints not in the correct format",
                          componentInstance.id());
                      continue;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, String> endpoint = (Map<String, String>) endpointObject;

                    String implementationZoneInstanceName =
                        endpoint.get("implementationzone_instance");
                    String subnetInstanceName = endpoint.get("subnet_instance");
                    Direction direction = Direction.valueOf(endpoint.get("direction"));
                    boolean isDirectionOut = direction.isOutDirection();
                    boolean isDirectionIn = direction == Direction.IN;
                    if (implementationZoneInstanceName != null
                        && !implementationZoneInstanceName.isEmpty()) {
                      String zoneInstanceId =
                          ZoneInstance.generateUniqueId(
                              landingZoneId, implementationZoneInstanceName);
                      ZoneInstance zoneInstance = zoneInstancesRepository.get(zoneInstanceId);
                      if (zoneInstance == null) {
                        logModelWarning(
                            "ComponentInstance %s references a non-existing ZoneInstance %s",
                            componentInstance.id(), implementationZoneInstanceName);
                      } else {
                        zoneInstance.addComponentInstance(componentInstance);
                        componentInstance.addZoneType(zoneInstance.getZoneType());

                        Set<SubnetInstance> subnetInstances = zoneInstance.getSubnetInstances();
                        if (subnetInstances != null) {
                          if (isDirectionOut) {
                            componentInstance.addPaasZoneOutSubnetInstances(subnetInstances);
                          } else if (isDirectionIn) {
                            componentInstance.addPaasZoneInSubnetInstances(subnetInstances);
                          }
                        }
                      }
                    } else if (subnetInstanceName != null && !subnetInstanceName.isEmpty()) {
                      String subnetInstanceId =
                          SubnetInstance.generateUniqueId(landingZoneId, subnetInstanceName);
                      SubnetInstance subnetInstance =
                          subnetInstancesRepository.get(subnetInstanceId);
                      if (subnetInstance == null) {
                        logModelWarning(
                            "ComponentInstance %s references a non-existing SubnetInstance %s",
                            componentInstance.id(), subnetInstanceName);
                      } else {
                        subnetInstance.addComponentInstance(componentInstance);
                        componentInstance.addZoneType(
                            subnetInstance.getZoneInstance().getZoneType());

                        if (isDirectionOut) {
                          componentInstance.addPaasZoneOutSubnetInstances(
                              subnetInstance.getZoneInstance().getSubnetInstances());
                        } else if (isDirectionIn) {
                          componentInstance.addPaasZoneInSubnetInstances(
                              subnetInstance.getZoneInstance().getSubnetInstances());
                        }
                      }
                    } else {
                      Log.errorf(
                          "ComponentInstance %s does not reference to ZoneInstance or SubnetInstance",
                          componentInstance.id());
                    }
                  }
                }
              }

              String hostingComponent = spec.getHostingComponent();
              if (hostingComponent != null && !hostingComponent.isBlank()) {
                hostingComponents.add(componentInstance);
              }
            });

    initAssociatedImplementationZones(
        applicationPhasesRepository,
        subnetInstancesRepository,
        zoneInstancesRepository,
        componentInstancesRepository,
        associatedImplementationZones,
        versionNumber);
    initAssociatedPlatformComponents(
        applicationPhasesRepository,
        subnetInstancesRepository,
        zoneInstancesRepository,
        componentsRepository,
        zoneTypesRepository,
        namespacesRepository,
        componentInstancesRepository,
        associatedPlatformComponents,
        versionNumber);
    initAssociatedHostingComponents(componentInstancesRepository, hostingComponents);
  }

  public static boolean isAutomaticTemplateInstance(V1ComponentInstance componentInstance) {
    String associatedPlatformComponent =
        componentInstance.getSpec().getAssociatedPlatformComponent();
    String associatedImplementationZone =
        componentInstance.getSpec().getAssociatedImplementationZone();

    return (associatedPlatformComponent != null && !associatedPlatformComponent.isBlank())
        || (associatedImplementationZone != null && !associatedImplementationZone.isBlank());
  }

  public void initInternals(Version version) {
    componentInstanceLogic.accept(version);
    ComponentInstancesRepository componentInstancesRepository =
        version.getComponentInstancesRepository();
    if (componentInstancesRepository == null) {
      Log.errorf("No ComponentInstance Repository found for version %s", version);
      return;
    }
    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("No Component Repository found for version %s", version);
      return;
    }

    Collection<ComponentInstance> values = componentInstancesRepository.getData().values();

    for (ComponentInstance componentInstance : values) {
      Component component = componentInstance.getComponent();
      if (component != null && component.isHosting()) {
        componentInstance.initExtraTags();
      }
    }

    for (ComponentInstance componentInstance : values) {
      Component component = componentInstance.getComponent();
      if (component != null && !component.isHosting()) {
        componentInstance.initExtraTags();
      }
    }

    // IMPORTANT: DO not remove this.
    // Otherwise, lazy initialization kicks in which can cause problems in a multithreaded
    // environment
    for (ComponentInstance componentInstance : values) {
      componentInstance.initSourceSelectors();
      componentInstance.initAncestorNames();
    }
  }

  // getName(), getComponent(), getApplicationPhase() cannot return null today,
  // but we won't know tomorrow since it is an autogenerated class

  private void initAssociatedImplementationZones(
      ConcurrentRepository<ApplicationPhase> applicationPhasesRepository,
      ConcurrentRepository<SubnetInstance> subnetInstancesRepository,
      ConcurrentRepository<ZoneInstance> zoneInstancesRepository,
      ConcurrentRepository<ComponentInstance> componentInstancesRepository,
      List<AutomaticTemplateInstance> associatedImplementationZones,
      String version) {
    long versionLong = VersionUtils.toLong(version);
    for (AutomaticTemplateInstance associatedImplementationZone : associatedImplementationZones) {
      V1ComponentInstance data = associatedImplementationZone.getData();

      V1ComponentInstanceSpec spec = data.getSpec();
      V1MetadataInstance metadata = data.getMetadata();

      String componentInstanceName = metadata.getName();

      Component component = associatedImplementationZone.getComponent();
      if (component == null) {
        continue;
      }

      String inSubnetInstanceId =
          SubnetInstance.generateUniqueId(
              associatedImplementationZone.getLandingZoneName(), spec.getInSubnetInstance());
      SubnetInstance inSubnetInstance = subnetInstancesRepository.get(inSubnetInstanceId);
      if (inSubnetInstance == null) {
        logModelWarning(
            "ComponentInstance %s references a non-existing SubnetInstance %s",
            componentInstanceName, inSubnetInstanceId);
        continue;
      }
      String outSubnetInstanceName = spec.getOutSubnetInstance();
      if (!Objects.equals(outSubnetInstanceName, inSubnetInstance.getName())) {
        continue;
      }

      V1ComponentSpec componentSpec = component.getData().getSpec();
      if (Boolean.TRUE.equals(componentSpec.getIsContainer())) {
        continue;
      }

      List<Set<String>> associatedFunctionalTypes =
          associatedImplementationZone.getAssociatedFunctionalTypes();
      if (associatedFunctionalTypes == null || associatedFunctionalTypes.isEmpty()) {
        continue;
      }

      String componentInstanceAssociatedImplementationZone = spec.getAssociatedImplementationZone();
      for (ZoneInstance zoneInstance : zoneInstancesRepository.getData().values()) {
        if (!Objects.equals(
            zoneInstance.getName(), componentInstanceAssociatedImplementationZone)) {
          continue;
        }

        String subnetInstanceId2 =
            SubnetInstance.generateUniqueId(
                zoneInstance.getLandingZoneName(), spec.getInSubnetInstance());
        SubnetInstance subnetInstance2 = subnetInstancesRepository.get(subnetInstanceId2);
        if (subnetInstance2 == null) {
          continue;
        }

        if (!Objects.equals(
            inSubnetInstance.getZoneInstance().getZoneType().getName(),
            zoneInstance.getZoneType().getName())) {
          continue;
        }

        LandingZone landingZone = zoneInstance.getLandingZone();
        boolean associatedFound =
            associatedImplementationZone.matchesLandingZoneFunctionalTypes(
                landingZone.getLandingZoneFunctionalTypes());
        if (!associatedFound) {
          continue;
        }

        String generatedComponentName =
            String.join(" - ", componentInstanceName, landingZone.getName());

        ComponentInstance generatedComponentInstance =
            componentInstancesRepository.get(generatedComponentName);
        if (generatedComponentInstance != null) {
          generatedComponentInstance.addAutomaticTemplateInstance(associatedImplementationZone);
          continue;
        }

        createGeneratedComponentInstance(
            applicationPhasesRepository,
            subnetInstancesRepository,
            zoneInstancesRepository,
            componentInstancesRepository,
            generatedComponentName,
            String.join(
                " ",
                GENERATED_COMMENT_PREFIX,
                landingZone.getName(),
                "instances due to functional types"),
            component,
            associatedImplementationZone,
            landingZone,
            null,
            null,
            null,
            versionLong);
      }
    }
  }

  // getName()cannot return null today,
  // but we won't know tomorrow since it is an autogenerated class

  private void initAssociatedHostingComponents(
      ConcurrentRepository<ComponentInstance> componentInstancesRepository,
      List<ComponentInstance> hostingComponents) {
    for (ComponentInstance hostingComponent : hostingComponents) {
      V1ComponentInstance data = hostingComponent.getData();
      V1ComponentInstanceSpec spec = data.getSpec();
      V1MetadataInstance metadata = data.getMetadata();

      String hostId = spec.getHostingComponent();
      if (hostId != null && !hostId.isEmpty()) {
        ComponentInstance host = componentInstancesRepository.get(hostId);
        if (host == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing ComponentInstance %s",
              metadata.getName(), hostId);
        } else {
          hostingComponent.setDeployComponentInstance(host);
          host.addHostedComponentInstance(hostingComponent);
        }
      }
    }
  }

  // getName(), getComponent() cannot return null today,
  // but we won't know tomorrow since it is an autogenerated class

  private void initAssociatedPlatformComponents(
      ConcurrentRepository<ApplicationPhase> applicationPhasesRepository,
      ConcurrentRepository<SubnetInstance> subnetInstancesRepository,
      ConcurrentRepository<ZoneInstance> zoneInstancesRepository,
      ConcurrentRepository<Component> componentsRepository,
      ConcurrentRepository<ZoneType> zoneTypesRepository,
      ConcurrentRepository<Namespace> namespacesRepository,
      ConcurrentRepository<ComponentInstance> componentInstancesRepository,
      List<AutomaticTemplateInstance> associatedPlatformComponents,
      String version) {
    long versionLong = VersionUtils.toLong(version);
    for (AutomaticTemplateInstance associatedPlatformComponent : associatedPlatformComponents) {
      V1ComponentInstance data = associatedPlatformComponent.getData();
      V1ComponentInstanceSpec spec = data.getSpec();
      V1MetadataInstance metadata = data.getMetadata();

      String componentInstanceName = metadata.getName();

      Component component = associatedPlatformComponent.getComponent();
      if (component == null) {
        continue;
      }

      LandingZone landingZone = associatedPlatformComponent.getLandingZone();
      if (landingZone == null) {
        continue;
      }

      Component platformComponent = null;
      String platformComponentName = spec.getAssociatedPlatformComponent();
      if (platformComponentName != null && !platformComponentName.isBlank()) {
        platformComponent = componentsRepository.get(platformComponentName);
        if (platformComponent == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing Component %s",
              componentInstanceName, platformComponentName);
          continue;
        }
      }

      ZoneType zoneType = null;
      String zoneTypeId = spec.getZoneType();
      if (zoneTypeId != null && !zoneTypeId.isBlank()) {
        zoneType = zoneTypesRepository.get(zoneTypeId);
        if (zoneType == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing ZoneType %s",
              componentInstanceName, zoneTypeId);
          continue;
        }
      }

      Namespace namespace = null;
      String namespaceId = spec.getNamespace();
      if (namespaceId != null && !namespaceId.isBlank()) {
        namespace = namespacesRepository.get(namespaceId);
        if (namespace == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing Namespace %s",
              componentInstanceName, namespaceId);
          // TODO: REMOVE it once the users fix the missing Namespace from the aud-data
          namespace =
              createGeneratedNamespace(
                  namespaceId,
                  componentInstanceName,
                  version,
                  associatedPlatformComponent.getScope(),
                  namespacesRepository);
        }
      }

      if (Boolean.FALSE.equals(component.getData().getSpec().getIsContainer())
          || (platformComponent == null
              || Boolean.FALSE.equals(platformComponent.getData().getSpec().getIsHosting()))) {
        logModelWarning(
            "Discarding %s as it does not have the correct hosting", componentInstanceName);
        continue;
      }

      for (ComponentInstance componentInstance : platformComponent.getAllComponentInstances()) {
        String generatedComponentName =
            String.join(" - ", componentInstanceName, componentInstance.getName());
        String generatedBaseComment =
            String.join(" ", GENERATED_COMMENT_PREFIX, platformComponent.getName());

        ComponentInstance generatedComponentInstance =
            componentInstancesRepository.get(generatedComponentName);
        if (generatedComponentInstance != null) {
          generatedComponentInstance.addAutomaticTemplateInstance(associatedPlatformComponent);
          continue;
        }

        if (zoneType == null
            || (zoneType != null && !componentInstance.isValidZoneType(zoneType, Direction.IN))) {
          String zoneTypeName = zoneType != null ? zoneType.getName() : "null";
          logModelWarning(
              "Discarding %s as it does not have the correct ZoneType %s",
              generatedComponentName, zoneTypeName);
          continue;
        }

        LandingZone instanceLandingZone = componentInstance.getLandingZone();

        List<Set<String>> associatedFunctionalTypes =
            associatedPlatformComponent.getAssociatedFunctionalTypes();
        if (associatedFunctionalTypes != null && !associatedFunctionalTypes.isEmpty()) {
          boolean associatedFound =
              associatedPlatformComponent.matchesLandingZoneFunctionalTypes(
                  componentInstance.getLandingZone().getLandingZoneFunctionalTypes());

          if (associatedFound) {
            createGeneratedComponentInstance(
                applicationPhasesRepository,
                subnetInstancesRepository,
                zoneInstancesRepository,
                componentInstancesRepository,
                generatedComponentName,
                String.join(" ", generatedBaseComment, "instances due to functional types"),
                component,
                associatedPlatformComponent,
                instanceLandingZone,
                zoneType,
                namespace,
                componentInstance,
                versionLong);
          }
        } else {
          boolean isProviderAmadeus =
              "Amadeus".equals(instanceLandingZone.getTarget().getProvider().getName());

          if ((Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToLegacyOnly())
                  && isProviderAmadeus)
              || (Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToCloudOnly())
                  && !isProviderAmadeus)
              || (Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToLegacyOnly())
                  && Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToCloudOnly()))) {

            if (Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToLandingzone())
                && Objects.equals(instanceLandingZone, landingZone)) {
              createGeneratedComponentInstance(
                  applicationPhasesRepository,
                  subnetInstancesRepository,
                  zoneInstancesRepository,
                  componentInstancesRepository,
                  generatedComponentName,
                  String.join(" ", generatedBaseComment, "instances limited to landing zone"),
                  component,
                  associatedPlatformComponent,
                  instanceLandingZone,
                  zoneType,
                  namespace,
                  componentInstance,
                  versionLong);
            } else if (Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToTarget())
                && (landingZone != null
                    && Objects.equals(instanceLandingZone.getTarget(), landingZone.getTarget()))) {

              createGeneratedComponentInstance(
                  applicationPhasesRepository,
                  subnetInstancesRepository,
                  zoneInstancesRepository,
                  componentInstancesRepository,
                  generatedComponentName,
                  String.join(" ", generatedBaseComment, "instances limited to target"),
                  component,
                  associatedPlatformComponent,
                  instanceLandingZone,
                  zoneType,
                  namespace,
                  componentInstance,
                  versionLong);
            } else if (Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToContinent())
                && (landingZone != null
                    && Objects.equals(
                        instanceLandingZone.getRegion().getContinent(),
                        landingZone.getRegion().getContinent()))) {

              createGeneratedComponentInstance(
                  applicationPhasesRepository,
                  subnetInstancesRepository,
                  zoneInstancesRepository,
                  componentInstancesRepository,
                  generatedComponentName,
                  String.join(" ", generatedBaseComment, "instances limited to continent"),
                  component,
                  associatedPlatformComponent,
                  instanceLandingZone,
                  zoneType,
                  namespace,
                  componentInstance,
                  versionLong);
            } else if (Boolean.TRUE.equals(spec.getIsAssociatedPlatformLimitedToMacrophase())
                && (landingZone != null
                    && Objects.equals(
                        instanceLandingZone.getMacroPhase(), landingZone.getMacroPhase()))) {

              createGeneratedComponentInstance(
                  applicationPhasesRepository,
                  subnetInstancesRepository,
                  zoneInstancesRepository,
                  componentInstancesRepository,
                  generatedComponentName,
                  String.join(" ", generatedBaseComment, "instances limited to macrophase"),
                  component,
                  associatedPlatformComponent,
                  instanceLandingZone,
                  zoneType,
                  namespace,
                  componentInstance,
                  versionLong);
            } else if (Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToMacrophase())
                && Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToLandingzone())
                && Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToTarget())
                && Boolean.FALSE.equals(spec.getIsAssociatedPlatformLimitedToContinent())) {

              createGeneratedComponentInstance(
                  applicationPhasesRepository,
                  subnetInstancesRepository,
                  zoneInstancesRepository,
                  componentInstancesRepository,
                  generatedComponentName,
                  String.join(" ", generatedBaseComment, "instances"),
                  component,
                  associatedPlatformComponent,
                  instanceLandingZone,
                  zoneType,
                  namespace,
                  componentInstance,
                  versionLong);
            }
          }
        }
      }
    }
  }

  private void createGeneratedComponentInstance(
      ConcurrentRepository<ApplicationPhase> applicationPhasesRepository,
      ConcurrentRepository<SubnetInstance> subnetInstancesRepository,
      ConcurrentRepository<ZoneInstance> zoneInstancesRepository,
      ConcurrentRepository<ComponentInstance> componentInstancesRepository,
      String generatedComponentName,
      String comment,
      Component component,
      AutomaticTemplateInstance automaticTemplateInstance,
      LandingZone landingZone,
      ZoneType zoneType,
      Namespace namespace,
      ComponentInstance componentInstance,
      long version) {
    V1ComponentInstance v1ComponentInstance = automaticTemplateInstance.getData();
    V1ComponentInstanceSpec spec = v1ComponentInstance.getSpec();

    ComponentInstance generatedComponentInstance = new ComponentInstance(v1ComponentInstance);
    generatedComponentInstance.setUniqueIdFrom(generatedComponentName);
    generatedComponentInstance.setName(generatedComponentName);
    generatedComponentInstance.setAutomaticallyInstantiated(true);
    generatedComponentInstance.setComment(comment);
    generatedComponentInstance.setVersion(version);

    Scope scope = automaticTemplateInstance.getScope();
    generatedComponentInstance.setScope(scope);
    scope.addComponentInstance(generatedComponentInstance);

    generatedComponentInstance.setLandingZone(landingZone);
    landingZone.addComponentInstance(generatedComponentInstance);

    generatedComponentInstance.setComponent(component);
    component.addComponentInstance(generatedComponentInstance);
    generatedComponentInstance.inheritTagsFromComponent();

    String applicationPhaseId = landingZone.getMacroPhase().getName();
    if (applicationPhaseId != null && !applicationPhaseId.isEmpty()) {
      ApplicationPhase applicationPhase = applicationPhasesRepository.get(applicationPhaseId);
      if (applicationPhase == null) {
        logModelWarning(
            "ComponentInstance %s references a non-existing ApplicationPhase %s",
            generatedComponentName, applicationPhaseId);
      } else {
        generatedComponentInstance.setApplicationPhase(applicationPhase);
        applicationPhase.addComponentInstance(generatedComponentInstance);
      }
    }

    if (zoneType != null) {
      generatedComponentInstance.setZoneType(zoneType);
    }

    generatedComponentInstance.addAutomaticTemplateInstance(automaticTemplateInstance);
    automaticTemplateInstance.addComponentInstance(generatedComponentInstance);

    if (namespace != null) {
      generatedComponentInstance.setNamespace(namespace);
      namespace.addComponentInstance(generatedComponentInstance);
    }

    String inSubnetInstanceString = spec.getInSubnetInstance();
    if (inSubnetInstanceString != null && !inSubnetInstanceString.isEmpty()) {
      String inSubnetInstanceId =
          SubnetInstance.generateUniqueId(landingZone.getName(), inSubnetInstanceString);
      SubnetInstance inSubnetInstance = subnetInstancesRepository.get(inSubnetInstanceId);
      if (inSubnetInstance == null) {
        logModelWarning(
            "ComponentInstance %s references a non-existing SubnetInstance %s",
            generatedComponentName, inSubnetInstanceId);
      } else {
        generatedComponentInstance.setInSubnetInstance(inSubnetInstance);
        inSubnetInstance.addComponentInstance(generatedComponentInstance);
      }
    }

    String outSubnetInstanceString = spec.getOutSubnetInstance();
    if (outSubnetInstanceString != null && !outSubnetInstanceString.isEmpty()) {
      String outSubnetInstanceId =
          SubnetInstance.generateUniqueId(landingZone.getName(), outSubnetInstanceString);
      SubnetInstance outSubnetInstance = subnetInstancesRepository.get(outSubnetInstanceId);
      if (outSubnetInstance == null) {
        logModelWarning(
            "ComponentInstance %s references a non-existing SubnetInstance %s",
            generatedComponentName, outSubnetInstanceId);
      } else {
        generatedComponentInstance.setOutSubnetInstance(outSubnetInstance);
        outSubnetInstance.addComponentInstance(generatedComponentInstance);
      }
    }

    String landingZoneName = landingZone.getName();
    V1ComponentInstanceSpec.EndpointTypeEnum endpointType = spec.getEndpointType();
    if (endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
      List<Object> endpoints = spec.getEndpoints();
      if (endpoints != null) {
        for (Object endpointObject : endpoints) {
          if (!(endpointObject instanceof Map)) {
            Log.errorf(
                "ComponentInstance %s endpoints not in the correct format", generatedComponentName);
            continue;
          }

          @SuppressWarnings("unchecked")
          Map<String, String> endpoint = (Map<String, String>) endpointObject;

          Direction direction = Direction.valueOf(endpoint.get("direction"));
          boolean isDirectionOut = direction.isOutDirection();
          boolean isDirectionIn = direction == Direction.IN;

          String implementationZoneInstanceName = endpoint.get("implementationzone_instance");
          String subnetInstanceName = endpoint.get("subnet_instance");
          if (implementationZoneInstanceName != null && !implementationZoneInstanceName.isEmpty()) {
            String zoneInstanceId =
                ZoneInstance.generateUniqueId(landingZoneName, implementationZoneInstanceName);
            ZoneInstance zoneInstance = zoneInstancesRepository.get(zoneInstanceId);
            if (zoneInstance == null) {
              logModelWarning(
                  "ComponentInstance %s references a non-existing ZoneInstance %s",
                  generatedComponentName, implementationZoneInstanceName);
            } else {
              if (isDirectionOut) {
                generatedComponentInstance.addPaasZoneOutSubnetInstances(
                    zoneInstance.getSubnetInstances());
              } else if (isDirectionIn) {
                generatedComponentInstance.addPaasZoneInSubnetInstances(
                    zoneInstance.getSubnetInstances());
              }
            }
          } else if (subnetInstanceName != null && !subnetInstanceName.isEmpty()) {
            String subnetInstanceId =
                SubnetInstance.generateUniqueId(landingZoneName, subnetInstanceName);
            SubnetInstance subnetInstance = subnetInstancesRepository.get(subnetInstanceId);
            if (subnetInstance == null) {
              logModelWarning(
                  "ComponentInstance %s references a non-existing SubnetInstance %s",
                  generatedComponentName, subnetInstanceName);
            } else {
              if (isDirectionOut) {
                generatedComponentInstance.addPaasZoneOutSubnetInstances(
                    subnetInstance.getZoneInstance().getSubnetInstances());
              } else if (isDirectionIn) {
                generatedComponentInstance.addPaasZoneInSubnetInstances(
                    subnetInstance.getZoneInstance().getSubnetInstances());
              }
            }
          } else {
            Log.errorf(
                "ComponentInstance %s does not reference to ZoneInstance or SubnetInstance",
                generatedComponentName);
          }
        }
      }
    }

    if (componentInstance != null) {
      String inZoneInstanceString = spec.getInZoneInstance();
      if (inZoneInstanceString != null && !inZoneInstanceString.isEmpty()) {
        String inZoneInstanceId =
            ZoneInstance.generateUniqueId(landingZone.getName(), inZoneInstanceString);
        ZoneInstance inZoneInstance = zoneInstancesRepository.get(inZoneInstanceId);
        if (inZoneInstance == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing ZoneInstance %s",
              generatedComponentName, inZoneInstanceId);
        } else {
          generatedComponentInstance.setInZoneInstance(inZoneInstance);
          inZoneInstance.addComponentInstance(generatedComponentInstance);
        }
      }

      String outZoneInstanceString = spec.getOutZoneInstance();
      if (outZoneInstanceString != null && !outZoneInstanceString.isEmpty()) {
        String outZoneInstanceId =
            ZoneInstance.generateUniqueId(landingZone.getName(), outZoneInstanceString);
        ZoneInstance outZoneInstance = zoneInstancesRepository.get(outZoneInstanceId);
        if (outZoneInstance == null) {
          logModelWarning(
              "ComponentInstance %s references a non-existing ZoneInstance %s",
              generatedComponentName, outZoneInstanceId);
        } else {
          generatedComponentInstance.setOutZoneInstance(outZoneInstance);
          outZoneInstance.addComponentInstance(generatedComponentInstance);
        }
      }

      generatedComponentInstance.setDeployComponentInstance(componentInstance);
      componentInstance.addHostedComponentInstance(generatedComponentInstance);
    }

    componentInstancesRepository.put(generatedComponentInstance.id(), generatedComponentInstance);
  }

  private static Namespace createGeneratedNamespace(
      String namespaceName,
      String componentInstanceName,
      String version,
      Scope scope,
      ConcurrentRepository<Namespace> namespacesRepository) {
    Namespace namespace = new Namespace();
    namespace.setUniqueIdFrom(namespaceName);
    namespace.setName(namespaceName);
    namespace.setComment(String.join(" ", GENERATED_COMMENT_PREFIX, componentInstanceName));
    namespace.setVersion(version);
    namespace.setAutomaticallyGenerated(true);
    namespace.setScope(scope);

    namespacesRepository.put(namespace.id(), namespace);

    return namespace;
  }
}
