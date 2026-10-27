package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class DependencyInstanceService
    extends BaseService<DependencyInstance, V1DependencyInstance> {

  @Override
  protected DependencyInstancesRepository getRepository(Version version) {
    DependencyInstancesRepository dependencyInstancesRepository =
        new DependencyInstancesRepository();
    version.setDependencyInstancesRepository(dependencyInstancesRepository);
    return dependencyInstancesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("Component Repository not found for version %s", version);
      return;
    }

    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("LandingZone Repository not found for version %s", version);
      return;
    }

    ComponentInstancesRepository componentInstancesRepository =
        version.getComponentInstancesRepository();
    if (componentInstancesRepository == null) {
      Log.errorf("ComponentInstance Repository not found for version %s", version);
      return;
    }

    DependenciesRepository dependenciesRepository = version.getDependenciesRepository();
    if (dependenciesRepository == null) {
      Log.errorf("Dependency Repository not found for version %s", version);
      return;
    }

    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null) {
      Log.errorf("Service Repository not found for version %s", version);
      return;
    }

    Set<Component> componentsToUseWideSubnets = new HashSet<>();
    Component linuxComponent = componentsRepository.get("Linux");
    if (linuxComponent != null) {
      componentsToUseWideSubnets.addAll(linuxComponent.getInternalAncestors());
    }
    Component windowsComponent = componentsRepository.get("Windows");
    if (windowsComponent != null) {
      componentsToUseWideSubnets.addAll(windowsComponent.getInternalAncestors());
    }

    init(
        version,
        DependencyInstance.class,
        V1DependencyInstance.class,
        (DependencyInstance dependencyInstance, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addDependencyInstance(dependencyInstance);

          V1DependencyInstance dependencyInstanceSpec = dependencyInstance.getData();

          V1DependencyInstanceSpec spec = dependencyInstanceSpec.getSpec();
          V1MetadataInstance metadata = dependencyInstanceSpec.getMetadata();

          dependencyInstance.setSourceAsComponent(
              Boolean.TRUE.equals(spec.getIsSourceAsComponent()));

          String landingZoneId = metadata.getLandingzone();
          if (landingZoneId != null && !landingZoneId.isEmpty()) {
            LandingZone landingZone = landingZonesRepository.get(landingZoneId);
            if (landingZone == null) {
              logModelWarning(
                  "DependencyInstance %s references a non-existing LandingZone %s",
                  dependencyInstance.getName(), landingZoneId);
            } else {
              dependencyInstance.setLandingZone(landingZone);
              landingZone.addDependencyInstance(dependencyInstance);
            }
          }

          String viaComponentInstanceId = spec.getViaComponentInstance();
          if (viaComponentInstanceId != null && !viaComponentInstanceId.isEmpty()) {
            ComponentInstance viaComponentInstance =
                componentInstancesRepository.get(viaComponentInstanceId);
            if (viaComponentInstance == null) {
              logModelWarning(
                  "DependencyInstance %s references a non-existing ViaComponentInstance %s",
                  dependencyInstance.getName(), viaComponentInstanceId);
            } else {
              dependencyInstance.setViaComponentInstance(viaComponentInstance);
              if (!dependencyInstance.isSourceAsComponent()) {
                viaComponentInstance.addDependencyInstance(dependencyInstance);
              }
            }

            String viaServiceName = spec.getViaService();
            setViaService(
                servicesRepository, dependencyInstance, viaServiceName, viaComponentInstance);
          }

          String componentInstanceId = spec.getComponentInstance();
          if (componentInstanceId != null && !componentInstanceId.isEmpty()) {
            ComponentInstance componentInstance =
                componentInstancesRepository.get(componentInstanceId);
            if (componentInstance == null) {
              logModelWarning(
                  "DependencyInstance %s references a non-existing ComponentInstance %s",
                  dependencyInstance.getName(), componentInstanceId);
            } else {
              dependencyInstance.setComponentInstance(componentInstance);
              if (!dependencyInstance.isSourceAsComponent()) {
                componentInstance.addDependencyInstance(dependencyInstance);
              }
            }
          }

          Dependency dependency = null;
          String dependencyId = spec.getDependency();
          if (dependencyId != null && !dependencyId.isEmpty()) {
            dependency = dependenciesRepository.get(dependencyId);
            if (dependency == null) {
              logModelWarning(
                  "DependencyInstance %s references a non-existing Dependency %s",
                  dependencyInstance.getName(), dependencyId);
            } else {
              dependencyInstance.setDependency(dependency);

              Component dependencySourceComponent = dependency.getSourceComponent();
              if (dependencySourceComponent != null
                  && Objects.equals(dependencySourceComponent.getName(), "Office users")) {
                dependencyInstance.setSourceAsComponent(true);
              }
              dependency.addDependencyInstance(dependencyInstance);
            }
          }

          ComponentInstance targetComponentInstance = null;
          String targetComponentInstanceId = spec.getTargetComponentInstance();
          if (targetComponentInstanceId != null && !targetComponentInstanceId.isEmpty()) {
            targetComponentInstance = componentInstancesRepository.get(targetComponentInstanceId);
            if (targetComponentInstance == null) {
              logModelWarning(
                  "DependencyInstance %s references a non-existing TargetComponentInstance %s",
                  dependencyInstance.getName(), targetComponentInstanceId);

            } else {
              dependencyInstance.setTargetComponentInstance(targetComponentInstance);
              if (Boolean.FALSE.equals(spec.getIsTargetAsComponent())) {
                targetComponentInstance.addDependencyInstance(dependencyInstance);
              }
            }
          }
          String targetServiceName = spec.getTargetService();
          List<String> targetServices = spec.getTargetServices();
          if (targetServices != null && !targetServices.isEmpty()) {
            for (String serviceName : targetServices) {
              setService(
                  servicesRepository,
                  dependencyInstance,
                  serviceName,
                  targetComponentInstance,
                  dependency);
            }

          } else if (targetServiceName != null && !targetServiceName.isEmpty()) {
            setService(
                servicesRepository,
                dependencyInstance,
                targetServiceName,
                targetComponentInstance,
                dependency);
          }

          dependencyInstance.initInternalUseWideSubnets(componentsToUseWideSubnets);
        });
  }

  private static void setService(
      ConcurrentRepository<Service> servicesRepository,
      DependencyInstance dependencyInstance,
      String serviceName,
      ComponentInstance targetComponentInstance,
      Dependency dependency) {
    Component targetComponent;
    if (targetComponentInstance != null) {
      targetComponent = targetComponentInstance.getComponent();
    } else {
      targetComponent = dependency.getDestinationComponent();
    }
    Service service = getService(servicesRepository, targetComponent, serviceName);
    if (service != null) {
      dependencyInstance.addService(service);
      service.addDependencyInstance(dependencyInstance);
    }
  }

  private static void setViaService(
      ConcurrentRepository<Service> servicesRepository,
      DependencyInstance dependencyInstance,
      String serviceName,
      ComponentInstance targetComponentInstance) {
    Component targetComponent = targetComponentInstance.getComponent();
    Service viaService = getService(servicesRepository, targetComponent, serviceName);
    if (viaService != null) {
      dependencyInstance.setViaService(viaService);
      viaService.addDependencyInstance(dependencyInstance);
    }
  }

  private static Service getService(
      ConcurrentRepository<Service> servicesRepository,
      Component targetComponent,
      String targetServiceName) {
    if (targetComponent != null && targetServiceName != null && !targetServiceName.isEmpty()) {
      Set<Component> parents = targetComponent.getInternalAncestors();
      for (Component parent : parents) {
        String serviceId =
            String.join(BaseInstance.ID_SEPARATOR, parent.getName(), targetServiceName);
        Service service = servicesRepository.get(serviceId);
        if (service != null) {
          return service;
        }
      }
    }
    return null;
  }
}
