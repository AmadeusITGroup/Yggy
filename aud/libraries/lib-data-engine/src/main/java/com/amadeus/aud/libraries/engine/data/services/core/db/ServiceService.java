package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationIdsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlockedPortsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class ServiceService extends BaseService<Service, V1Service> {

  @Override
  protected ServicesRepository getRepository(Version version) {
    ServicesRepository servicesRepository = new ServicesRepository();
    version.setServicesRepository(servicesRepository);
    return servicesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("Component Repository not found for version %s", version);
      return;
    }

    ApplicationIdsRepository applicationIdsRepository = version.getApplicationIdsRepository();
    if (applicationIdsRepository == null) {
      Log.errorf("ApplicationId Repository not found for version %s", version);
      return;
    }

    BlockedPortsRepository blockedPortsRepository = version.getBlockedPortsRepository();
    if (blockedPortsRepository == null) {
      Log.errorf("BlockedPorts Repository not found for version %s", version);
      return;
    }

    init(
        version,
        Service.class,
        V1Service.class,
        (Service service, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addService(service);
          initServiceReferences(
              service, componentsRepository, applicationIdsRepository, blockedPortsRepository);
        });

    initDefaultServices(version, blockedPortsRepository);
  }

  private void initServiceReferences(
      Service service,
      ComponentsRepository componentsRepository,
      ApplicationIdsRepository applicationIdsRepository,
      BlockedPortsRepository blockedPortsRepository) {

    V1ServiceSpec spec = service.getData().getSpec();

    linkComponent(service, spec, componentsRepository);
    linkApplicationId(service, spec, applicationIdsRepository);
    initPortsAndProtocol(service, spec, blockedPortsRepository);

    service.setSecurityJustification(spec.getSecurityJustification());
  }

  private void linkComponent(
      Service service, V1ServiceSpec spec, ComponentsRepository componentsRepository) {
    String componentName = spec.getComponent();
    if (componentName == null || componentName.isEmpty()) {
      return;
    }

    Component component = componentsRepository.get(componentName);
    if (component == null) {
      logModelWarning(
          "Service %s references a non-existing Component %s", service.id(), componentName);
    } else {
      service.setComponent(component);
      component.addService(service);
    }
  }

  private void linkApplicationId(
      Service service, V1ServiceSpec spec, ApplicationIdsRepository applicationIdsRepository) {
    String applicationIdName = spec.getApplicationId();
    if (applicationIdName == null || applicationIdName.isEmpty()) {
      return;
    }

    ApplicationId applicationId = applicationIdsRepository.get(applicationIdName);
    if (applicationId == null) {
      logModelWarning(
          "Service %s references a non-existing ApplicationID %s", service.id(), applicationIdName);
    } else {
      service.setApplicationId(applicationId);
      applicationId.addService(service);
    }
  }

  private void initPortsAndProtocol(
      Service service, V1ServiceSpec spec, BlockedPortsRepository blockedPortsRepository) {
    RangeSet<Integer> portRangeSet = TreeRangeSet.create();

    if (spec.getProtocol() == ProtocolEnum.ICMP) {
      // auto approved, no governance
      // TODO: Check if it is safe to put ALL_PORTS here instead of empty
      service.setPortsRange(portRangeSet);
      service.setProtocol(ProtocolEnum.ICMP);
      service.setApprovedPorts(TreeRangeSet.create());
      service.setApprovedProtocol(ProtocolEnum.ICMP);
      service.setServiceNowId("ICMP AUTO approved");
      return;
    }

    List<String> currentPorts = spec.getPorts();
    if (Objects.equals(currentPorts.getFirst(), "NonBlockedPorts")) {
      portRangeSet.add(PortUtils.ALL_RANGE);
      portRangeSet.removeAll(
          blockedPortsRepository.getBlockedPortRanges(spec.getProtocol().getValue()));
    } else {
      portRangeSet.addAll(PortUtils.toRangeSet(currentPorts));
    }

    service.setPortsRange(portRangeSet);
    service.setProtocol(spec.getProtocol());
  }

  private void initDefaultServices(Version version, BlockedPortsRepository blockedPortsRepository) {
    ServicesRepository servicesRepository = version.getServicesRepository();
    if (servicesRepository == null) {
      Log.errorf("Services Repository not found for version %s", version);
      return;
    }

    servicesRepository.setUdpService(
        createDefaultService(
            "UDP", ServicesRepository.UDP_SERVICE_NAME, ProtocolEnum.UDP, blockedPortsRepository));
    servicesRepository.setTcpService(
        createDefaultService(
            "TCP", ServicesRepository.TCP_SERVICE_NAME, ProtocolEnum.TCP, blockedPortsRepository));
    servicesRepository.setAllService(
        createDefaultService(null, ServicesRepository.ALL_SERVICE_NAME, ProtocolEnum.STAR, null));
  }

  private Service createDefaultService(
      String blockedPortsProtocol,
      String serviceName,
      ProtocolEnum protocol,
      BlockedPortsRepository blockedPortsRepository) {

    RangeSet<Integer> portRangeSet = TreeRangeSet.create();
    portRangeSet.add(PortUtils.ALL_RANGE);

    if (blockedPortsRepository != null && blockedPortsProtocol != null) {
      portRangeSet.removeAll(blockedPortsRepository.getBlockedPortRanges(blockedPortsProtocol));
    }

    Service service =
        new Service(
            new V1Service()
                .spec(new V1ServiceSpec().protocol(protocol))
                .metadata(new V1Metadata().name(serviceName).scope(BomElement.DEFAULT_DOMAIN)));
    service.setPortsRange(portRangeSet);

    return service;
  }
}
