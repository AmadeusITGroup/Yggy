package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  InfobloxSubnet.JSON_PROPERTY_NAME,
  InfobloxSubnet.JSON_PROPERTY_NETWORK,
  InfobloxSubnet.JSON_PROPERTY_NETMASK
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InfobloxSubnet extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_NETWORK = "network";
  private String network;

  public static final String JSON_PROPERTY_NETMASK = "netmask";
  private String netmask;

  public InfobloxSubnet() {}

  public InfobloxSubnet name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(String name) {
    this.name = name;
  }

  public InfobloxSubnet network(String network) {
    this.network = network;
    return this;
  }

  /**
   * Get network
   *
   * @return network
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NETWORK, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNetwork() {
    return network;
  }

  @JsonProperty(value = JSON_PROPERTY_NETWORK, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNetwork(String network) {
    this.network = network;
  }

  public InfobloxSubnet netmask(String netmask) {
    this.netmask = netmask;
    return this;
  }

  /**
   * Get netmask
   *
   * @return netmask
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NETMASK, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNetmask() {
    return netmask;
  }

  @JsonProperty(value = JSON_PROPERTY_NETMASK, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNetmask(String netmask) {
    this.netmask = netmask;
  }

  @Override
  public InfobloxSubnet labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this InfobloxSubnet object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfobloxSubnet infobloxSubnet = (InfobloxSubnet) o;
    return Objects.equals(this.name, infobloxSubnet.name)
        && Objects.equals(this.network, infobloxSubnet.network)
        && Objects.equals(this.netmask, infobloxSubnet.netmask)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, network, netmask, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfobloxSubnet {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
    sb.append("    netmask: ").append(toIndentedString(netmask)).append("\n");
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
