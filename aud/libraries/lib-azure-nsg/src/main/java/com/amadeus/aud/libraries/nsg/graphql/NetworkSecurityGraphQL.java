package com.amadeus.aud.libraries.nsg.graphql;

import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Type;

@Type("NetworkSecurityDTO")
public class NetworkSecurityGraphQL {
  private String landingZoneId;
  private Map<String, NetworkGraphQL> networks;
  private Map<String, SubnetGraphQL> subnets;
  private ZoneSecurityGroupsGraphQL nsgs;

  public String getLandingZoneId() {
    return landingZoneId;
  }

  public void setLandingZoneId(String landingZoneId) {
    this.landingZoneId = landingZoneId;
  }

  public Map<String, NetworkGraphQL> getNetworks() {
    return networks;
  }

  public void setNetworks(Map<String, NetworkGraphQL> networks) {
    this.networks = networks;
  }

  public Map<String, SubnetGraphQL> getSubnets() {
    return subnets;
  }

  public void setSubnets(Map<String, SubnetGraphQL> subnets) {
    this.subnets = subnets;
  }

  public ZoneSecurityGroupsGraphQL getNsgs() {
    return nsgs;
  }

  public void setNsgs(ZoneSecurityGroupsGraphQL nsgs) {
    this.nsgs = nsgs;
  }

  // Inner DTO classes
  @Type("NetworkDTO")
  public static class NetworkGraphQL {
    private List<String> cidrs;
    private ProviderSpecificGraphQL providerSpecific;
    private Map<String, String> tags;

    public List<String> getCidrs() {
      return cidrs;
    }

    public void setCidrs(List<String> cidrs) {
      this.cidrs = cidrs;
    }

    public ProviderSpecificGraphQL getProviderSpecific() {
      return providerSpecific;
    }

    public void setProviderSpecific(ProviderSpecificGraphQL providerSpecific) {
      this.providerSpecific = providerSpecific;
    }

    public Map<String, String> getTags() {
      return tags;
    }

    public void setTags(Map<String, String> tags) {
      this.tags = tags;
    }
  }

  public static class ProviderSpecificGraphQL {
    private List<String> dnsservers;

    public List<String> getDnsservers() {
      return dnsservers;
    }

    public void setDnsservers(List<String> dnsservers) {
      this.dnsservers = dnsservers;
    }
  }

  @Type("SubnetDTO")
  public static class SubnetGraphQL {
    private String name;
    private String subnetType;
    private String cidr;
    private String zoneName;
    private String zoneType;
    private String associatedNSG;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSubnetType() {
      return subnetType;
    }

    public void setSubnetType(String subnetType) {
      this.subnetType = subnetType;
    }

    public String getCidr() {
      return cidr;
    }

    public void setCidr(String cidr) {
      this.cidr = cidr;
    }

    public String getZoneName() {
      return zoneName;
    }

    public void setZoneName(String zoneName) {
      this.zoneName = zoneName;
    }

    public String getZoneType() {
      return zoneType;
    }

    public void setZoneType(String zoneType) {
      this.zoneType = zoneType;
    }

    public String getAssociatedNSG() {
      return associatedNSG;
    }

    public void setAssociatedNSG(String associatedNSG) {
      this.associatedNSG = associatedNSG;
    }
  }

  public static class ZoneSecurityGroupsGraphQL {
    private Map<String, SecurityGroupMapGraphQL> rules;
    private Map<String, String> tags;

    public Map<String, SecurityGroupMapGraphQL> getRules() {
      return rules;
    }

    public void setRules(Map<String, SecurityGroupMapGraphQL> rules) {
      this.rules = rules;
    }

    public Map<String, String> getTags() {
      return tags;
    }

    public void setTags(Map<String, String> tags) {
      this.tags = tags;
    }
  }

  public static class SecurityGroupMapGraphQL {
    private List<SubnetPortGraphQL> inbound;
    private List<ServiceTagOutboundRuleGraphQL> outbound;

    public List<SubnetPortGraphQL> getInbound() {
      return inbound;
    }

    public void setInbound(List<SubnetPortGraphQL> inbound) {
      this.inbound = inbound;
    }

    public List<ServiceTagOutboundRuleGraphQL> getOutbound() {
      return outbound;
    }

    public void setOutbound(List<ServiceTagOutboundRuleGraphQL> outbound) {
      this.outbound = outbound;
    }
  }

  public static class SubnetPortGraphQL {
    private String name;
    private List<String> cidrs;
    private List<String> dstCidrs;
    private List<String> ports;
    private String protocol;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<String> getCidrs() {
      return cidrs;
    }

    public void setCidrs(List<String> cidrs) {
      this.cidrs = cidrs;
    }

    public List<String> getDstCidrs() {
      return dstCidrs;
    }

    public void setDstCidrs(List<String> dstCidrs) {
      this.dstCidrs = dstCidrs;
    }

    public List<String> getPorts() {
      return ports;
    }

    public void setPorts(List<String> ports) {
      this.ports = ports;
    }

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
    }
  }

  public static class ServiceTagOutboundRuleGraphQL {
    private String name;
    private String serviceTag;
    private List<String> ports;
    private String protocol;
    private List<String> cidrs;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getServiceTag() {
      return serviceTag;
    }

    public void setServiceTag(String serviceTag) {
      this.serviceTag = serviceTag;
    }

    public List<String> getPorts() {
      return ports;
    }

    public void setPorts(List<String> ports) {
      this.ports = ports;
    }

    public String getProtocol() {
      return protocol;
    }

    public void setProtocol(String protocol) {
      this.protocol = protocol;
    }

    public List<String> getCidrs() {
      return cidrs;
    }

    public void setCidrs(List<String> cidrs) {
      this.cidrs = cidrs;
    }
  }
}
