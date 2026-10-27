package com.amadeus.aud.libraries.nsg.services;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.nsg.models.Subnet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class LandingZoneUtils {
  private LandingZoneUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static Set<SubnetInstance> getSubnetInstances(LandingZone landingZone) {
    Set<SubnetInstance> filteredSubnetInstances = new HashSet<>();

    Set<SubnetInstance> subnetInstances = landingZone.getSubnetInstances();
    if (subnetInstances == null) {
      return filteredSubnetInstances;
    }

    for (SubnetInstance subnetInstance : subnetInstances) {
      if (!subnetInstance.isGroup()
          && !Objects.equals(subnetInstance.getSubnetTypeName(), Constants.GLOBAL)) {
        filteredSubnetInstances.add(subnetInstance);
      }
    }
    return filteredSubnetInstances;
  }

  public static Map<String, Subnet> getSubnets(Iterable<SubnetInstance> filteredSubnetInstances) {
    Map<String, Subnet> subnetMap = new HashMap<>();
    for (SubnetInstance subnetInstance : filteredSubnetInstances) {
      ZoneInstance zoneInstance = subnetInstance.getZoneInstance();
      String zoneTypeName = zoneInstance.getZoneTypeName();

      String zoneInstanceName = zoneInstance.getName();
      String cidr = subnetInstance.getCidr();
      Subnet subnet =
          new Subnet()
              .name(subnetInstance.getName())
              .subnetType(subnetInstance.getSubnetTypeName())
              .cidr(cidr)
              .zoneName(zoneInstanceName)
              .tags(subnetInstance.getTagsAsMap())
              .zoneType(zoneTypeName)
              .providerSpecific(subnetInstance.getProviderSpecificAsMap());

      boolean isZoneTechnicalZone = Objects.equals(zoneTypeName, Constants.TECHNICAL_ZONE);
      if (!isZoneTechnicalZone || zoneInstance.isNsgNeeded()) {
        subnet.associatedNSG(String.join("-", zoneTypeName, zoneInstanceName));
      }
      if (isZoneTechnicalZone && zoneInstance.isNsgNeeded()) {
        subnet.setZoneType(Constants.TECHNICAL_ZONE_WITH_NSG);
      }

      subnetMap.put(cidr, subnet);
    }
    return subnetMap;
  }
}
