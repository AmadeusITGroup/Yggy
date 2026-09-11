package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Api providing all objects for network and security groups creation */
@Schema(description = "Api providing all objects for network and security groups creation")
@JsonPropertyOrder({
  NetworkSecurity.JSON_PROPERTY_NETWORKS,
  NetworkSecurity.JSON_PROPERTY_SUBNETS,
  NetworkSecurity.JSON_PROPERTY_NSGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NetworkSecurity extends ApiMetadata {
  public static final String JSON_PROPERTY_NETWORKS = "networks";
  private Map<String, Network> networks;

  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  private Map<String, Subnet> subnets;

  public static final String JSON_PROPERTY_NSGS = "nsgs";
  private ZoneSecurityGroups nsgs;

  public NetworkSecurity() {}

  public NetworkSecurity networks(Map<String, Network> networks) {
    this.networks = networks;
    return this;
  }

  public NetworkSecurity putNetworksItem(String key, Network networksItem) {
    if (this.networks == null) {
      this.networks = new HashMap<>();
    }
    this.networks.put(key, networksItem);
    return this;
  }

  /**
   * Get networks
   *
   * @return networks
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NETWORKS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Map<String, Network> getNetworks() {
    return networks;
  }

  @JsonProperty(value = JSON_PROPERTY_NETWORKS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNetworks(Map<String, Network> networks) {
    this.networks = networks;
  }

  public NetworkSecurity subnets(Map<String, Subnet> subnets) {
    this.subnets = subnets;
    return this;
  }

  public NetworkSecurity putSubnetsItem(String key, Subnet subnetsItem) {
    if (this.subnets == null) {
      this.subnets = new HashMap<>();
    }
    this.subnets.put(key, subnetsItem);
    return this;
  }

  /**
   * Get subnets
   *
   * @return subnets
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Map<String, Subnet> getSubnets() {
    return subnets;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnets(Map<String, Subnet> subnets) {
    this.subnets = subnets;
  }

  public NetworkSecurity nsgs(ZoneSecurityGroups nsgs) {
    this.nsgs = nsgs;
    return this;
  }

  /**
   * Get nsgs
   *
   * @return nsgs
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NSGS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ZoneSecurityGroups getNsgs() {
    return nsgs;
  }

  @JsonProperty(value = JSON_PROPERTY_NSGS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNsgs(ZoneSecurityGroups nsgs) {
    this.nsgs = nsgs;
  }

  @Override
  public NetworkSecurity url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public NetworkSecurity urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public NetworkSecurity version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public NetworkSecurity apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this NetworkSecurity object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkSecurity networkSecurity = (NetworkSecurity) o;
    return Objects.equals(this.networks, networkSecurity.networks)
        && Objects.equals(this.subnets, networkSecurity.subnets)
        && Objects.equals(this.nsgs, networkSecurity.nsgs)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(networks, subnets, nsgs, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NetworkSecurity {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    networks: ").append(toIndentedString(networks)).append("\n");
    sb.append("    subnets: ").append(toIndentedString(subnets)).append("\n");
    sb.append("    nsgs: ").append(toIndentedString(nsgs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
