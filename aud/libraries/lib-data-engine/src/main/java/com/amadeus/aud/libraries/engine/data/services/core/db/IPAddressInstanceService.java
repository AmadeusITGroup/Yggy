package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.IPAddressInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class IPAddressInstanceService extends BaseService<IPAddressInstance, V1IPAddressInstance> {

  @Override
  protected IPAddressInstancesRepository getRepository(Version version) {
    IPAddressInstancesRepository ipAddressInstancesRepository = new IPAddressInstancesRepository();
    version.setIpAddressInstancesRepository(ipAddressInstancesRepository);
    return ipAddressInstancesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
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

    init(
        version,
        IPAddressInstance.class,
        V1IPAddressInstance.class,
        (IPAddressInstance ipAddressInstance, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addIpAddressInstance(ipAddressInstance);
          linkLandingZoneAndComponentInstance(
              ipAddressInstance, landingZonesRepository, componentInstancesRepository);
        });
  }

  private void linkLandingZoneAndComponentInstance(
      IPAddressInstance ipAddressInstance,
      LandingZonesRepository landingZonesRepository,
      ComponentInstancesRepository componentInstancesRepository) {

    V1IPAddressInstance ipAddressInstanceData = ipAddressInstance.getData();
    V1IPAddressInstanceSpec spec = ipAddressInstanceData.getSpec();
    V1MetadataInstance metadata = ipAddressInstanceData.getMetadata();

    String landingZoneId = metadata.getLandingzone();
    if (landingZoneId != null && !landingZoneId.isEmpty()) {
      LandingZone landingZone = landingZonesRepository.get(landingZoneId);
      if (landingZone == null) {
        logModelWarning(
            "IPAddressInstance %s references a non-existing LandingZone %s",
            ipAddressInstance.getName(), landingZoneId);
      } else {
        ipAddressInstance.setLandingZone(landingZone);
        landingZone.addIpAddressInstance(ipAddressInstance);
      }
    }

    String componentInstanceId = spec.getComponentInstance();
    if (componentInstanceId != null && !componentInstanceId.isEmpty()) {
      ComponentInstance componentInstance = componentInstancesRepository.get(componentInstanceId);
      if (componentInstance == null) {
        logModelWarning(
            "IPAddressInstance %s references a non-existing ComponentInstance %s",
            ipAddressInstance.getName(), componentInstanceId);
      } else {
        ipAddressInstance.setComponentInstance(componentInstance);
        componentInstance.addIpAddressInstance(ipAddressInstance);
      }
    }
  }
}
