package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.common.LogExecutionTime;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallGroupsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class ZoneInstanceService extends BaseService<ZoneInstance, V1ZoneInstance> {

  @Override
  protected ZoneInstancesRepository getRepository(Version version) {
    ZoneInstancesRepository zoneInstancesRepository = new ZoneInstancesRepository();
    version.setZoneInstancesRepository(zoneInstancesRepository);
    return zoneInstancesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("LandingZone Repository not found for version %s", version);
      return;
    }

    ZoneTypesRepository zoneTypesRepository = version.getZoneTypesRepository();
    if (zoneTypesRepository == null) {
      Log.errorf("ZoneType Repository not found for version %s", version);
      return;
    }

    FirewallGroupsRepository firewallGroupsRepository = version.getFirewallGroupsRepository();
    if (firewallGroupsRepository == null) {
      Log.errorf("FirewallGroup Repository not found for version %s", version);
      return;
    }

    init(
        version,
        ZoneInstance.class,
        V1ZoneInstance.class,
        (ZoneInstance zoneInstance, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addZoneInstance(zoneInstance);
          linkReferences(
              zoneInstance, landingZonesRepository, zoneTypesRepository, firewallGroupsRepository);
        });
  }

  private void linkReferences(
      ZoneInstance zoneInstance,
      LandingZonesRepository landingZonesRepository,
      ZoneTypesRepository zoneTypesRepository,
      FirewallGroupsRepository firewallGroupsRepository) {

    linkLandingZone(zoneInstance, landingZonesRepository);
    linkZoneType(zoneInstance, zoneTypesRepository);
    linkFirewallGroup(zoneInstance, firewallGroupsRepository);
  }

  private void linkLandingZone(
      ZoneInstance zoneInstance, LandingZonesRepository landingZonesRepository) {
    String landingZoneName = zoneInstance.getData().getMetadata().getLandingzone();
    if (landingZoneName == null || landingZoneName.isEmpty()) {
      return;
    }

    LandingZone landingZone = landingZonesRepository.get(landingZoneName);
    if (landingZone == null) {
      logModelWarning(
          "ZoneInstance %s references a non-existing LandingZone %s",
          zoneInstance.id(), landingZoneName);
    } else {
      zoneInstance.setLandingZone(landingZone);
      landingZone.addZoneInstance(zoneInstance);
    }
  }

  private void linkZoneType(ZoneInstance zoneInstance, ZoneTypesRepository zoneTypesRepository) {
    String zoneTypeName = zoneInstance.getData().getSpec().getZoneType();
    if (zoneTypeName == null || zoneTypeName.isEmpty()) {
      return;
    }

    ZoneType zoneType = zoneTypesRepository.get(zoneTypeName);
    if (zoneType == null) {
      logModelWarning(
          "ZoneInstance %s references a non-existing ZoneType %s", zoneInstance.id(), zoneTypeName);
    } else {
      zoneInstance.setZoneType(zoneType);
      zoneType.addZoneInstance(zoneInstance);
    }
  }

  private void linkFirewallGroup(
      ZoneInstance zoneInstance, FirewallGroupsRepository firewallGroupsRepository) {
    String firewallGroupName = zoneInstance.getData().getSpec().getFirewallGroup();
    if (firewallGroupName == null || firewallGroupName.isEmpty()) {
      return;
    }

    FirewallGroup firewallGroup = firewallGroupsRepository.get(firewallGroupName);
    if (firewallGroup == null) {
      logModelWarning(
          "ZoneInstance %s references a non-existing FirewallGroup %s",
          zoneInstance.id(), firewallGroupName);
    } else {
      zoneInstance.setFirewallGroup(firewallGroup);
      firewallGroup.addZoneInstance(zoneInstance);
    }
  }

  public void initInternals(Version version) {
    ZoneInstancesRepository zoneInstancesRepository = version.getZoneInstancesRepository();
    if (zoneInstancesRepository == null) {
      Log.errorf("Zone Instance Repository not found for version %s", version);
      return;
    }

    ConcurrentMap<String, ZoneInstance> data = zoneInstancesRepository.getData();
    if (data == null) {
      Log.errorf("No Zone Instance data for version %s", version);
      return;
    }

    for (ZoneInstance zoneInstance : data.values()) {
      zoneInstance.getInternalDstCidrRangeSetForNSG();
    }
  }
}
