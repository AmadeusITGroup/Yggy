package com.amadeus.aud.services.apis.models.core.securityzoning;

import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import jakarta.annotation.Nullable;

public record SecurityZoningSubnet(
    String target,
    String landingzone,
    String implementationzone,
    String name,
    String cidr,
    boolean is_group,
    @Nullable String parent,
    String zonetype) {

  public static SecurityZoningSubnet fromSubnetInstance(SubnetInstance subnetInstance) {
    LandingZone landingZone = subnetInstance.getLandingZone();
    ZoneInstance zoneInstance = subnetInstance.getZoneInstance();
    return new SecurityZoningSubnet(
        landingZone.getTarget().getName(),
        landingZone.getName(),
        zoneInstance.getName(),
        subnetInstance.getName(),
        subnetInstance.getCidr(),
        subnetInstance.isGroup(),
        subnetInstance.getParent() != null ? subnetInstance.getParent().getName() : null,
        zoneInstance.getZoneType().getName());
  }
}
