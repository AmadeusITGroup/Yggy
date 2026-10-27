package com.amadeus.aud.libraries.ipreport.service;

import com.amadeus.aud.libraries.ipreport.models.IpReportAs;
import com.amadeus.aud.libraries.ipreport.models.IpReportCidr;
import com.amadeus.aud.libraries.ipreport.models.IpReportIp;
import com.amadeus.aud.libraries.ipreport.models.IpReportSubnet;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import jakarta.annotation.Nullable;
import java.util.ArrayList;

/// Mapper class responsible for transforming domain entities into IP Report DTOs.
public final class IpReportUtils {
  private IpReportUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static @Nullable IpReportSubnet toIpReportSubnet(@Nullable SubnetInstance subnetInstance) {
    if (subnetInstance == null) {
      return null;
    }

    IpReportSubnet ipReportSubnet = new IpReportSubnet();
    String cidr = subnetInstance.getCidr();
    String name = subnetInstance.getName();

    ipReportSubnet.setCidr(cidr);
    ipReportSubnet.setName(name);
    ipReportSubnet.setScope(subnetInstance.getScopeName());

    LandingZone landingZone = subnetInstance.getLandingZone();
    Target target = landingZone.getTarget();
    Region region = landingZone.getRegion();

    ipReportSubnet.setProvider(target.getProviderName());
    ipReportSubnet.setContinent(region.getContinentName());
    ipReportSubnet.setRegion(landingZone.getRegionName());
    ipReportSubnet.setTarget(landingZone.getTargetName());
    ipReportSubnet.setFirewallgroup(
        target.isCloud() && !target.getFirewallGroups().isEmpty()
            ? target.getFirewallGroups().iterator().next().getName()
            : "");
    ipReportSubnet.setLandingzoneType(landingZone.getLandingZoneTypeName());
    ipReportSubnet.setLandingzone(subnetInstance.getLandingZoneName());
    ipReportSubnet.setMacrophase(landingZone.getMacroPhaseName());
    ipReportSubnet.setSubnetType(subnetInstance.getSubnetTypeName());
    ipReportSubnet.setSubnetShared(subnetInstance.isShared());
    ipReportSubnet.setImplementationZone(subnetInstance.getZoneInstanceName());

    ZoneInstance instance = subnetInstance.getZoneInstance();
    if (instance != null) {
      ipReportSubnet.setSecurityZone(instance.getZoneTypeName());
      ipReportSubnet.setIzUrlHash(instance.getURLHash());
    }

    ipReportSubnet.setSnUrlHash(subnetInstance.getURLHash());
    ipReportSubnet.setReUrlHash(region.getURLHash());

    ipReportSubnet.setAsIncoming(new ArrayList<>());
    ipReportSubnet.setAsOutgoing(new ArrayList<>());

    // Combine component instances from both subnet and zone, if present.
    if (subnetInstance.getComponentInstances() != null) {
      for (ComponentInstance ci : subnetInstance.getComponentInstances()) {
        IpReportAs ipAs = toIpReportAs(ci);
        ipReportSubnet.addAsIncomingItem(ipAs);
        ipReportSubnet.addAsOutgoingItem(ipAs);
      }
    }
    if (instance != null && instance.getComponentInstances() != null) {
      for (ComponentInstance ci : instance.getComponentInstances()) {
        IpReportAs ipAs = toIpReportAs(ci);
        ipReportSubnet.addAsIncomingItem(ipAs);
        ipReportSubnet.addAsOutgoingItem(ipAs);
      }
    }

    return ipReportSubnet;
  }

  public static @Nullable IpReportCidr toIpReportCidr(
      @Nullable ComponentInstance componentInstance) {
    if (componentInstance == null) {
      return null;
    }

    final IpReportCidr report = new IpReportCidr();
    final String name = componentInstance.getName();
    final LandingZone landingZone = componentInstance.getLandingZone();
    final Target target = landingZone.getTarget();
    final Region region = landingZone.getRegion();

    report.setName(name);
    report.setScope(componentInstance.getScopeName());
    report.setProvider(target.getProviderName());
    report.setContinent(region.getContinentName());
    report.setRegion(landingZone.getRegionName());
    report.setTarget(landingZone.getTargetName());
    report.setFirewallgroup(
        target.isCloud() && !target.getFirewallGroups().isEmpty()
            ? target.getFirewallGroups().iterator().next().getName()
            : "");
    report.setLandingzone(componentInstance.getLandingZoneName());
    report.setMacrophase(landingZone.getMacroPhaseName());
    report.setComponent(componentInstance.getComponentName());
    report.setComponentInstance(name);
    report.setApplicationphase(componentInstance.getApplicationPhaseName());
    report.setReUrlHash(region.getURLHash());
    report.setCiUrlHash(componentInstance.getURLHash());

    return report;
  }

  public static @Nullable IpReportIp toIpReportIp(@Nullable IPAddressInstance ipAddressInstance) {
    if (ipAddressInstance == null) {
      return null;
    }

    IpReportIp ipReportIp = new IpReportIp();

    ipReportIp.setName(ipAddressInstance.getName());
    ipReportIp.setHost(ipAddressInstance.getHost());

    ipReportIp.setScope(ipAddressInstance.getScopeName());

    LandingZone landingZone = ipAddressInstance.getLandingZone();
    Target target = landingZone.getTarget();
    Region region = landingZone.getRegion();

    ipReportIp.setProvider(target.getProviderName());
    ipReportIp.setContinent(region.getContinentName());
    ipReportIp.setRegion(landingZone.getRegionName());
    ipReportIp.setTarget(landingZone.getTargetName());
    ipReportIp.setFirewallgroup(
        target.isCloud() && !target.getFirewallGroups().isEmpty()
            ? target.getFirewallGroups().iterator().next().getName()
            : "");
    ipReportIp.setLandingzone(ipAddressInstance.getLandingZoneName());
    ipReportIp.setMacrophase(landingZone.getMacroPhaseName());

    ComponentInstance component = ipAddressInstance.getComponentInstance();
    if (component != null) {
      ipReportIp.setComponent(component.getComponentName());
      ipReportIp.setComponentInstance(component.getName());
      ipReportIp.setApplicationphase(component.getApplicationPhaseName());
      ipReportIp.setCiUrlHash(component.getURLHash());

      SubnetInstance subnetInstance = component.getInSubnetInstance();
      if (subnetInstance != null) {
        ZoneInstance zoneInstance = subnetInstance.getZoneInstance();
        if (zoneInstance != null) {
          ipReportIp.setImplementationZone(zoneInstance.getName());
          ipReportIp.setIzUrlHash(zoneInstance.getURLHash());
        } else {
          ipReportIp.setImplementationZone("");
          ipReportIp.setIzUrlHash("");
        }
      } else {
        ipReportIp.setImplementationZone("");
        ipReportIp.setIzUrlHash("");
      }
    }

    ipReportIp.setIpaUrlHash(ipAddressInstance.getURLHash());
    ipReportIp.setReUrlHash(region.getURLHash());

    return ipReportIp;
  }

  public static @Nullable IpReportAs toIpReportAs(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null) {
      return null;
    }

    final IpReportAs report = new IpReportAs();
    report.setComponentInstance(componentInstance.getName());
    report.setCiUrlHash(componentInstance.getURLHash());
    return report;
  }
}
