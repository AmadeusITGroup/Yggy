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
  NetworkConfig.JSON_PROPERTY_NETWORK,
  NetworkConfig.JSON_PROPERTY_SUBNETS,
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NetworkConfig extends BaseResponse {
  public static final String JSON_PROPERTY_NETWORK = "network";
  public static final String JSON_PROPERTY_SUBNETS = "subnets";

  @JsonProperty(JSON_PROPERTY_NETWORK)
  private Network network;

  @JsonProperty(JSON_PROPERTY_SUBNETS)
  private Map<String, Subnet> subnets;

  @JsonCreator
  public NetworkConfig() {}

  public NetworkConfig(
      String url,
      String urlVersion,
      String version,
      String apiVersion,
      Network network,
      Map<String, Subnet> subnets) {
    super(url, urlVersion, version, apiVersion);
    this.network = network;
    this.subnets = subnets;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return (o instanceof NetworkConfig that)
        && network.equals(that.network)
        && subnets.equals(that.subnets);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), network, subnets);
    }
    return hashcode;
  }

  public Network getNetwork() {
    return network;
  }

  public void setNetwork(Network network) {
    this.network = network;
  }

  public Map<String, Subnet> getSubnets() {
    return subnets;
  }

  public void setSubnets(Map<String, Subnet> subnets) {
    this.subnets = subnets;
  }
}
