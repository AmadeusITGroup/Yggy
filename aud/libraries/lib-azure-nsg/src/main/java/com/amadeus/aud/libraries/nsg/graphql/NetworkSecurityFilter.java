package com.amadeus.aud.libraries.nsg.graphql;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(NetworkSecurityFilter.NAME)
public class NetworkSecurityFilter {
  public static final String NAME = "NetworkSecurityFilter";
  public static final String PROVIDER = "landingZoneNetworkSecurity";
  private StringFilter landingZoneName = null;
  private StringFilter cidr = null;
  private StringFilter subnetName = null;
  private StringFilter subnetType = null;
  private StringFilter zoneName = null;
  private StringFilter zoneType = null;
  private List<LabelFilter> labels = Collections.emptyList();

  public boolean matches(String landingZoneName, NetworkSecurityGraphQL networkSecurity) {
    return matchesLandingZoneName(landingZoneName)
        && matchesCidr(networkSecurity)
        && matchesSubnetName(networkSecurity)
        && matchesSubnetType(networkSecurity)
        && matchesZoneName(networkSecurity)
        && matchesZoneType(networkSecurity)
        && matchesLabels(networkSecurity);
  }

  private boolean matchesLandingZoneName(String landingZoneName) {
    return this.landingZoneName == null || this.landingZoneName.matches(landingZoneName);
  }

  private boolean matchesCidr(NetworkSecurityGraphQL networkSecurity) {
    if (cidr == null) {
      return true;
    }
    return checkCidrInNetworks(networkSecurity) || checkCidrInSubnets(networkSecurity);
  }

  private boolean checkCidrInNetworks(NetworkSecurityGraphQL networkSecurity) {
    if (networkSecurity.getNetworks() == null) {
      return false;
    }
    for (NetworkSecurityGraphQL.NetworkGraphQL network : networkSecurity.getNetworks().values()) {
      if (network.getCidrs() != null
          && network.getCidrs().stream().anyMatch(c -> cidr.matches(c))) {
        return true;
      }
    }
    return false;
  }

  private boolean checkCidrInSubnets(NetworkSecurityGraphQL networkSecurity) {
    if (networkSecurity.getSubnets() == null) {
      return false;
    }
    for (NetworkSecurityGraphQL.SubnetGraphQL subnet : networkSecurity.getSubnets().values()) {
      if (subnet.getCidr() != null && cidr.matches(subnet.getCidr())) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesSubnetName(NetworkSecurityGraphQL networkSecurity) {
    if (subnetName == null) {
      return true;
    }
    if (networkSecurity.getSubnets() == null) {
      return false;
    }
    return networkSecurity.getSubnets().values().stream()
        .anyMatch(subnet -> subnet.getName() != null && subnetName.matches(subnet.getName()));
  }

  private boolean matchesSubnetType(NetworkSecurityGraphQL networkSecurity) {
    if (subnetType == null) {
      return true;
    }
    if (networkSecurity.getSubnets() == null) {
      return false;
    }
    return networkSecurity.getSubnets().values().stream()
        .anyMatch(subnet -> subnetType.matches(subnet.getSubnetType()));
  }

  private boolean matchesZoneName(NetworkSecurityGraphQL networkSecurity) {
    if (zoneName == null) {
      return true;
    }
    if (networkSecurity.getSubnets() == null) {
      return false;
    }
    return networkSecurity.getSubnets().values().stream()
        .anyMatch(subnet -> subnet.getZoneName() != null && zoneName.matches(subnet.getZoneName()));
  }

  private boolean matchesZoneType(NetworkSecurityGraphQL networkSecurity) {
    if (zoneType == null) {
      return true;
    }
    if (networkSecurity.getSubnets() == null) {
      return false;
    }
    return networkSecurity.getSubnets().values().stream()
        .anyMatch(subnet -> zoneType.matches(subnet.getZoneType()));
  }

  private boolean matchesLabels(NetworkSecurityGraphQL networkSecurity) {
    if (labels == null || labels.isEmpty()) {
      return true;
    }
    return checkLabelsInNetworks(networkSecurity);
  }

  private boolean checkLabelsInNetworks(NetworkSecurityGraphQL networkSecurity) {
    if (networkSecurity.getNetworks() == null) {
      return false;
    }
    for (NetworkSecurityGraphQL.NetworkGraphQL network : networkSecurity.getNetworks().values()) {
      if (LabelFilter.matchesAllLabels(network.getTags(), labels)) {
        return true;
      }
    }
    return false;
  }

  public StringFilter getLandingZoneName() {
    return landingZoneName;
  }

  public void setLandingZoneName(StringFilter landingZoneName) {
    this.landingZoneName = landingZoneName;
  }

  public StringFilter getCidr() {
    return cidr;
  }

  public void setCidr(StringFilter cidr) {
    this.cidr = cidr;
  }

  public StringFilter getSubnetName() {
    return subnetName;
  }

  public void setSubnetName(StringFilter subnetName) {
    this.subnetName = subnetName;
  }

  public StringFilter getSubnetType() {
    return subnetType;
  }

  public void setSubnetType(StringFilter subnetType) {
    this.subnetType = subnetType;
  }

  public StringFilter getZoneName() {
    return zoneName;
  }

  public void setZoneName(StringFilter zoneName) {
    this.zoneName = zoneName;
  }

  public StringFilter getZoneType() {
    return zoneType;
  }

  public void setZoneType(StringFilter zoneType) {
    this.zoneType = zoneType;
  }

  public List<LabelFilter> getLabels() {
    return labels;
  }

  public void setLabels(List<LabelFilter> labels) {
    this.labels = labels != null ? labels : Collections.emptyList();
  }
}
