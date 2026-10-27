package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstanceSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class SubnetInstanceService extends BaseService<SubnetInstance, V1SubnetInstance> {

  @Override
  protected SubnetInstancesRepository getRepository(Version version) {
    SubnetInstancesRepository subnetInstancesRepository = new SubnetInstancesRepository();
    version.setSubnetInstancesRepository(subnetInstancesRepository);
    return subnetInstancesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("LandingZone Repository not found for version %s", version);
      return;
    }

    SubnetTypesRepository subnetTypesRepository = version.getSubnetTypesRepository();
    if (subnetTypesRepository == null) {
      Log.errorf("SubnetType Repository not found for version %s", version);
      return;
    }

    ZoneInstancesRepository zoneInstancesRepository = version.getZoneInstancesRepository();
    if (zoneInstancesRepository == null) {
      Log.errorf("ZoneInstance Repository not found for version %s", version);
      return;
    }

    ConcurrentRepository<SubnetInstance> repository =
        init(
            version,
            SubnetInstance.class,
            V1SubnetInstance.class,
            (SubnetInstance subnetInstance, Scope scope) -> {
              if (scope == null) {
                return;
              }
              scope.addSubnetInstance(subnetInstance);
              linkReferences(
                  subnetInstance,
                  landingZonesRepository,
                  subnetTypesRepository,
                  zoneInstancesRepository);
            });

    ConcurrentMap<String, SubnetInstance> data = repository.getData();
    initSubnetInstanceParents(data);
    initInternals(data);
  }

  private void linkReferences(
      SubnetInstance subnetInstance,
      LandingZonesRepository landingZonesRepository,
      SubnetTypesRepository subnetTypesRepository,
      ZoneInstancesRepository zoneInstancesRepository) {

    V1MetadataInstance metadata = subnetInstance.getData().getMetadata();
    V1SubnetInstanceSpec spec = subnetInstance.getData().getSpec();

    linkLandingZone(subnetInstance, metadata, landingZonesRepository);
    linkSubnetType(subnetInstance, spec, subnetTypesRepository);
    linkZoneInstance(subnetInstance, metadata, spec, zoneInstancesRepository);
  }

  private void linkLandingZone(
      SubnetInstance subnetInstance,
      V1MetadataInstance metadata,
      LandingZonesRepository landingZonesRepository) {

    String landingZoneName = metadata.getLandingzone();
    if (landingZoneName == null || landingZoneName.isEmpty()) {
      return;
    }

    LandingZone landingZone = landingZonesRepository.get(landingZoneName);
    if (landingZone == null) {
      logModelWarning(
          "SubnetInstance %s references a non-existing LandingZone %s",
          subnetInstance.id(), landingZoneName);
    } else {
      subnetInstance.setLandingZone(landingZone);
      landingZone.addSubnetInstance(subnetInstance);
    }
  }

  private void linkSubnetType(
      SubnetInstance subnetInstance,
      V1SubnetInstanceSpec spec,
      SubnetTypesRepository subnetTypesRepository) {

    String subnetTypeName = spec.getSubnetType();
    if (subnetTypeName == null || subnetTypeName.isEmpty()) {
      return;
    }

    SubnetType subnetType = subnetTypesRepository.get(subnetTypeName);
    if (subnetType == null) {
      logModelWarning(
          "SubnetInstance %s references a non-existing SubnetType %s",
          subnetInstance.id(), subnetTypeName);
    } else {
      subnetInstance.setSubnetType(subnetType);
      subnetType.addSubnetInstance(subnetInstance);
    }
  }

  private void linkZoneInstance(
      SubnetInstance subnetInstance,
      V1MetadataInstance metadata,
      V1SubnetInstanceSpec spec,
      ZoneInstancesRepository zoneInstancesRepository) {

    // FIXME: this lookup is not ideal and should be revisited.
    //   Names should be unique and referenced properly in the YAML files.
    String zoneInstanceName =
        String.join(SubnetInstance.ID_SEPARATOR, metadata.getLandingzone(), spec.getZoneInstance());

    if (zoneInstanceName.isEmpty()) {
      return;
    }

    ZoneInstance zoneInstance = zoneInstancesRepository.get(zoneInstanceName);
    if (zoneInstance == null) {
      logModelWarning(
          "SubnetInstance %s references a non-existing ZoneInstance %s",
          subnetInstance.id(), zoneInstanceName);
    } else {
      subnetInstance.setZoneInstance(zoneInstance);
      zoneInstance.addSubnetInstance(subnetInstance);
    }
  }

  private void initInternals(ConcurrentMap<String, SubnetInstance> data) {
    for (SubnetInstance subnetInstance : data.values()) {
      subnetInstance.initInternalEndpoints();
    }
  }

  // getData().getMetadata().getLandingzone() cannot return null today,
  // but we won't know tomorrow since it is an autogenerated class

  private void initSubnetInstanceParents(ConcurrentMap<String, SubnetInstance> subnetInstancesMap) {
    subnetInstancesMap.forEach(
        (String s, SubnetInstance subnetInstance) -> {
          String parentName = subnetInstance.getData().getSpec().getParent();
          String landingZoneName = subnetInstance.getData().getMetadata().getLandingzone();

          if (parentName != null
              && !parentName.isEmpty()
              && landingZoneName != null
              && !landingZoneName.isEmpty()) {
            String subnetInstanceName =
                String.join(SubnetInstance.ID_SEPARATOR, landingZoneName, parentName);
            SubnetInstance parent =
                subnetInstancesMap.get(AbstractBase.generateUniqueId(subnetInstanceName));
            if (parent == null) {
              logModelWarning(
                  "SubnetInstance %s references a non-existing parent %s",
                  subnetInstance.id(), subnetInstanceName);
            } else {
              parent.addChild(subnetInstance);
              subnetInstance.setParent(parent);
            }
          }
        });
  }
}
