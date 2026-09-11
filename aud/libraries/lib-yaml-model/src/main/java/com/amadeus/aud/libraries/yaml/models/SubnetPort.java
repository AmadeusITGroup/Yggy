package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  SubnetPort.JSON_PROPERTY_NAME,
  SubnetPort.JSON_PROPERTY_CIDRS,
  SubnetPort.JSON_PROPERTY_DST_CIDRS,
  SubnetPort.JSON_PROPERTY_PORTS,
  SubnetPort.JSON_PROPERTY_PROTOCOL,
  SubnetPort.JSON_PROPERTY_SERVICE_TAG
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubnetPort extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<String> cidrs;

  public static final String JSON_PROPERTY_DST_CIDRS = "dst_cidrs";
  @Nullable private List<String> dstCidrs;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  /** Gets or Sets protocol */
  public enum ProtocolEnum {
    TCP(String.valueOf("Tcp")),

    UDP(String.valueOf("Udp")),

    ICMP(String.valueOf("Icmp")),

    STAR(String.valueOf("*"));

    private String value;

    ProtocolEnum(String value) {
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
    public static ProtocolEnum fromValue(String value) {
      for (ProtocolEnum b : ProtocolEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public static final String JSON_PROPERTY_SERVICE_TAG = "service_tag";
  private String serviceTag;

  public SubnetPort() {}

  public SubnetPort name(String name) {
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

  public SubnetPort cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public SubnetPort addCidrsItem(String cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * Get cidrs
   *
   * @return cidrs
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<String> cidrs) {
    this.cidrs = cidrs;
  }

  public SubnetPort dstCidrs(@Nullable List<String> dstCidrs) {
    this.dstCidrs = dstCidrs;
    return this;
  }

  public SubnetPort addDstCidrsItem(String dstCidrsItem) {
    if (this.dstCidrs == null) {
      this.dstCidrs = new ArrayList<>();
    }
    this.dstCidrs.add(dstCidrsItem);
    return this;
  }

  /**
   * Get dstCidrs
   *
   * @return dstCidrs
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_DST_CIDRS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDstCidrs() {
    return dstCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_CIDRS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDstCidrs(@Nullable List<String> dstCidrs) {
    this.dstCidrs = dstCidrs;
  }

  public SubnetPort ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public SubnetPort addPortsItem(String portsItem) {
    if (this.ports == null) {
      this.ports = new ArrayList<>();
    }
    this.ports.add(portsItem);
    return this;
  }

  /**
   * Get ports
   *
   * @return ports
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public SubnetPort protocol(ProtocolEnum protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * Get protocol
   *
   * @return protocol
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ProtocolEnum getProtocol() {
    return protocol;
  }

  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProtocol(ProtocolEnum protocol) {
    this.protocol = protocol;
  }

  public SubnetPort serviceTag(String serviceTag) {
    this.serviceTag = serviceTag;
    return this;
  }

  /**
   * Get serviceTag
   *
   * @return serviceTag
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SERVICE_TAG, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getServiceTag() {
    return serviceTag;
  }

  @JsonProperty(value = JSON_PROPERTY_SERVICE_TAG, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setServiceTag(String serviceTag) {
    this.serviceTag = serviceTag;
  }

  @Override
  public SubnetPort labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this SubnetPort object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubnetPort subnetPort = (SubnetPort) o;
    return Objects.equals(this.name, subnetPort.name)
        && Objects.equals(this.cidrs, subnetPort.cidrs)
        && Objects.equals(this.dstCidrs, subnetPort.dstCidrs)
        && Objects.equals(this.ports, subnetPort.ports)
        && Objects.equals(this.protocol, subnetPort.protocol)
        && Objects.equals(this.serviceTag, subnetPort.serviceTag)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, cidrs, dstCidrs, ports, protocol, serviceTag, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubnetPort {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    dstCidrs: ").append(toIndentedString(dstCidrs)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    serviceTag: ").append(toIndentedString(serviceTag)).append("\n");
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
