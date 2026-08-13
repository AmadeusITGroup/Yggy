package com.amadeus.aud.libraries.nsg.models;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.amadeus.aud.libraries.yaml.models.Network;
import com.amadeus.aud.libraries.yaml.models.Subnet;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;
import java.util.Objects;

@JsonPropertyOrder({
  NetworkSecurity.JSON_PROPERTY_NETWORKS,
  NetworkSecurity.JSON_PROPERTY_SUBNETS,
  NetworkSecurity.JSON_PROPERTY_NSGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NetworkSecurity extends BaseResponse {
  public static final String JSON_PROPERTY_NETWORKS = "networks";
  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  public static final String JSON_PROPERTY_NSGS = "nsgs";

  @JsonProperty(JSON_PROPERTY_NETWORKS)
  private Map<String, Network> networks;

  @JsonProperty(JSON_PROPERTY_SUBNETS)
  private Map<String, Subnet> subnets;

  @JsonProperty(JSON_PROPERTY_NSGS)
  private ZoneSecurityGroups nsgs;

  @JsonCreator
  public NetworkSecurity() {}

  public NetworkSecurity(
      String url,
      String urlVersion,
      String version,
      String apiVersion,
      Map<String, Network> networks,
      Map<String, Subnet> subnets,
      ZoneSecurityGroups nsgs) {
    super(url, urlVersion, version, apiVersion);
    this.networks = networks;
    this.subnets = subnets;
    this.nsgs = nsgs;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return (o instanceof NetworkSecurity that)
        && networks.equals(that.networks)
        && subnets.equals(that.subnets)
        && nsgs.equals(that.nsgs);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), networks, subnets, nsgs);
    }
    return hashcode;
  }

  public Map<String, Network> getNetworks() {
    return networks;
  }

  public void setNetworks(Map<String, Network> networks) {
    this.networks = networks;
  }

  public Map<String, Subnet> getSubnets() {
    return subnets;
  }

  public void setSubnets(Map<String, Subnet> subnets) {
    this.subnets = subnets;
  }

  public ZoneSecurityGroups getNsgs() {
    return nsgs;
  }

  public void setNsgs(ZoneSecurityGroups nsgs) {
    this.nsgs = nsgs;
  }
}
