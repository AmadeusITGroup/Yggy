package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Black list cidr specification. Filled by Security */
@Schema(description = "Black list cidr specification. Filled by Security")
@JsonPropertyOrder({
  V1BlackListedCIDRSpec.JSON_PROPERTY_TRANSPORT_LAYER,
  V1BlackListedCIDRSpec.JSON_PROPERTY_CIDR,
  V1BlackListedCIDRSpec.JSON_PROPERTY_PORTS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1BlackListedCIDRSpec {
  /** Gets or Sets transportLayer */
  public enum TransportLayerEnum {
    TCP(String.valueOf("Tcp")),

    UDP(String.valueOf("Udp")),

    ICMP(String.valueOf("Icmp"));

    private String value;

    TransportLayerEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TransportLayerEnum fromValue(String value) {
      for (TransportLayerEnum b : TransportLayerEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TRANSPORT_LAYER = "transportLayer";
  private List<TransportLayerEnum> transportLayer;

  public static final String JSON_PROPERTY_CIDR = "cidr";
  private String cidr;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public V1BlackListedCIDRSpec() {}

  public V1BlackListedCIDRSpec transportLayer(List<TransportLayerEnum> transportLayer) {
    this.transportLayer = transportLayer;
    return this;
  }

  public V1BlackListedCIDRSpec addTransportLayerItem(TransportLayerEnum transportLayerItem) {
    if (this.transportLayer == null) {
      this.transportLayer = new ArrayList<>();
    }
    this.transportLayer.add(transportLayerItem);
    return this;
  }

  /**
   * The low level protocol e.g. TCP, UDP, Icmp.
   *
   * @return transportLayer
   */
  @NotNull
  @Schema(required = true, description = "The low level protocol e.g. TCP, UDP, Icmp.")
  @JsonProperty(value = JSON_PROPERTY_TRANSPORT_LAYER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<TransportLayerEnum> getTransportLayer() {
    return transportLayer;
  }

  @JsonProperty(value = JSON_PROPERTY_TRANSPORT_LAYER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTransportLayer(List<TransportLayerEnum> transportLayer) {
    this.transportLayer = transportLayer;
  }

  public V1BlackListedCIDRSpec cidr(String cidr) {
    this.cidr = cidr;
    return this;
  }

  /**
   * CIDR
   *
   * @return cidr
   */
  @NotNull
  @Schema(required = true, description = "CIDR")
  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCidr() {
    return cidr;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

  public V1BlackListedCIDRSpec ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public V1BlackListedCIDRSpec addPortsItem(String portsItem) {
    if (this.ports == null) {
      this.ports = new ArrayList<>();
    }
    this.ports.add(portsItem);
    return this;
  }

  /**
   * The port information (list of port or port-range)
   *
   * @return ports
   */
  @NotNull
  @Schema(required = true, description = "The port information (list of port or port-range)")
  @JsonProperty(value = JSON_PROPERTY_PORTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getPorts() {
    return ports;
  }

  @JsonProperty(value = JSON_PROPERTY_PORTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPorts(List<String> ports) {
    this.ports = ports;
  }

  /** Return true if this v1.BlackListedCIDRSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1BlackListedCIDRSpec v1BlackListedCIDRSpec = (V1BlackListedCIDRSpec) o;
    return Objects.equals(this.transportLayer, v1BlackListedCIDRSpec.transportLayer)
        && Objects.equals(this.cidr, v1BlackListedCIDRSpec.cidr)
        && Objects.equals(this.ports, v1BlackListedCIDRSpec.ports);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transportLayer, cidr, ports);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1BlackListedCIDRSpec {\n");
    sb.append("    transportLayer: ").append(toIndentedString(transportLayer)).append("\n");
    sb.append("    cidr: ").append(toIndentedString(cidr)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
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
