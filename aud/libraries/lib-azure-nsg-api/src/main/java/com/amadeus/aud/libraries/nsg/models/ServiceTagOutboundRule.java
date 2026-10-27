package com.amadeus.aud.libraries.nsg.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Rule to open security for Outbound Service Tags */
@Schema(description = "Rule to open security for Outbound Service Tags")
@JsonPropertyOrder({
  ServiceTagOutboundRule.JSON_PROPERTY_NAME,
  ServiceTagOutboundRule.JSON_PROPERTY_SERVICE_TAG,
  ServiceTagOutboundRule.JSON_PROPERTY_PORTS,
  ServiceTagOutboundRule.JSON_PROPERTY_PROTOCOL,
  ServiceTagOutboundRule.JSON_PROPERTY_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceTagOutboundRule {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_SERVICE_TAG = "service_tag";
  private String serviceTag;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private Protocol protocol;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<String> cidrs;

  public ServiceTagOutboundRule() {}

  public ServiceTagOutboundRule name(String name) {
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

  public ServiceTagOutboundRule serviceTag(String serviceTag) {
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

  public ServiceTagOutboundRule ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public ServiceTagOutboundRule addPortsItem(String portsItem) {
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

  public ServiceTagOutboundRule protocol(Protocol protocol) {
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
  public Protocol getProtocol() {
    return protocol;
  }

  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public ServiceTagOutboundRule cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public ServiceTagOutboundRule addCidrsItem(String cidrsItem) {
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

  /** Return true if this ServiceTagOutboundRule object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceTagOutboundRule serviceTagOutboundRule = (ServiceTagOutboundRule) o;
    return Objects.equals(this.name, serviceTagOutboundRule.name)
        && Objects.equals(this.serviceTag, serviceTagOutboundRule.serviceTag)
        && Objects.equals(this.ports, serviceTagOutboundRule.ports)
        && Objects.equals(this.protocol, serviceTagOutboundRule.protocol)
        && Objects.equals(this.cidrs, serviceTagOutboundRule.cidrs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, serviceTag, ports, protocol, cidrs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceTagOutboundRule {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    serviceTag: ").append(toIndentedString(serviceTag)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
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
