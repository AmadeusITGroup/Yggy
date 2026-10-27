package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1ServiceInstanceSpec */
@JsonPropertyOrder({
  V1ServiceInstanceSpec.JSON_PROPERTY_COMPONENT_INSTANCE,
  V1ServiceInstanceSpec.JSON_PROPERTY_SERVICE,
  V1ServiceInstanceSpec.JSON_PROPERTY_PORTS,
  V1ServiceInstanceSpec.JSON_PROPERTY_PROTOCOL,
  V1ServiceInstanceSpec.JSON_PROPERTY_LB_PUBLIC_IP,
  V1ServiceInstanceSpec.JSON_PROPERTY_LB_PUBLIC_PORT,
  V1ServiceInstanceSpec.JSON_PROPERTY_LB_PUBLIC_PROTOCOL,
  V1ServiceInstanceSpec.JSON_PROPERTY_DNS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ServiceInstanceSpec {
  public static final String JSON_PROPERTY_COMPONENT_INSTANCE = "componentInstance";
  private String componentInstance;

  public static final String JSON_PROPERTY_SERVICE = "service";
  private String service;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public static final String JSON_PROPERTY_LB_PUBLIC_IP = "lbPublicIp";
  @Nullable private String lbPublicIp;

  public static final String JSON_PROPERTY_LB_PUBLIC_PORT = "lbPublicPort";
  @Nullable private Integer lbPublicPort;

  public static final String JSON_PROPERTY_LB_PUBLIC_PROTOCOL = "lbPublicProtocol";
  @Nullable private String lbPublicProtocol;

  public static final String JSON_PROPERTY_DNS = "dns";
  @Nullable private String dns;

  public V1ServiceInstanceSpec() {}

  public V1ServiceInstanceSpec componentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Name of the component instance
   *
   * @return componentInstance
   */
  @NotNull
  @Schema(required = true, description = "Name of the component instance")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponentInstance() {
    return componentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
  }

  public V1ServiceInstanceSpec service(String service) {
    this.service = service;
    return this;
  }

  /**
   * Name of the service
   *
   * @return service
   */
  @NotNull
  @Schema(required = true, description = "Name of the service")
  @JsonProperty(value = JSON_PROPERTY_SERVICE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getService() {
    return service;
  }

  @JsonProperty(value = JSON_PROPERTY_SERVICE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setService(String service) {
    this.service = service;
  }

  public V1ServiceInstanceSpec ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public V1ServiceInstanceSpec addPortsItem(String portsItem) {
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

  public V1ServiceInstanceSpec protocol(ProtocolEnum protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * The protocol e.g. TCP, UDP, Icmp.
   *
   * @return protocol
   */
  @NotNull
  @Schema(required = true, description = "The protocol e.g. TCP, UDP, Icmp.")
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

  public V1ServiceInstanceSpec lbPublicIp(@Nullable String lbPublicIp) {
    this.lbPublicIp = lbPublicIp;
    return this;
  }

  /**
   * TBD
   *
   * @return lbPublicIp
   */
  @Schema(description = "TBD")
  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_IP)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getLbPublicIp() {
    return lbPublicIp;
  }

  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_IP)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLbPublicIp(@Nullable String lbPublicIp) {
    this.lbPublicIp = lbPublicIp;
  }

  public V1ServiceInstanceSpec lbPublicPort(@Nullable Integer lbPublicPort) {
    this.lbPublicPort = lbPublicPort;
    return this;
  }

  /**
   * TBD
   *
   * @return lbPublicPort
   */
  @Schema(description = "TBD")
  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_PORT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Integer getLbPublicPort() {
    return lbPublicPort;
  }

  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_PORT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLbPublicPort(@Nullable Integer lbPublicPort) {
    this.lbPublicPort = lbPublicPort;
  }

  public V1ServiceInstanceSpec lbPublicProtocol(@Nullable String lbPublicProtocol) {
    this.lbPublicProtocol = lbPublicProtocol;
    return this;
  }

  /**
   * TBD
   *
   * @return lbPublicProtocol
   */
  @Schema(description = "TBD")
  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_PROTOCOL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getLbPublicProtocol() {
    return lbPublicProtocol;
  }

  @JsonProperty(value = JSON_PROPERTY_LB_PUBLIC_PROTOCOL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLbPublicProtocol(@Nullable String lbPublicProtocol) {
    this.lbPublicProtocol = lbPublicProtocol;
  }

  public V1ServiceInstanceSpec dns(@Nullable String dns) {
    this.dns = dns;
    return this;
  }

  /**
   * TBD
   *
   * @return dns
   */
  @Schema(description = "TBD")
  @JsonProperty(value = JSON_PROPERTY_DNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDns() {
    return dns;
  }

  @JsonProperty(value = JSON_PROPERTY_DNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDns(@Nullable String dns) {
    this.dns = dns;
  }

  /** Return true if this v1.ServiceInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ServiceInstanceSpec v1ServiceInstanceSpec = (V1ServiceInstanceSpec) o;
    return Objects.equals(this.componentInstance, v1ServiceInstanceSpec.componentInstance)
        && Objects.equals(this.service, v1ServiceInstanceSpec.service)
        && Objects.equals(this.ports, v1ServiceInstanceSpec.ports)
        && Objects.equals(this.protocol, v1ServiceInstanceSpec.protocol)
        && Objects.equals(this.lbPublicIp, v1ServiceInstanceSpec.lbPublicIp)
        && Objects.equals(this.lbPublicPort, v1ServiceInstanceSpec.lbPublicPort)
        && Objects.equals(this.lbPublicProtocol, v1ServiceInstanceSpec.lbPublicProtocol)
        && Objects.equals(this.dns, v1ServiceInstanceSpec.dns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        componentInstance,
        service,
        ports,
        protocol,
        lbPublicIp,
        lbPublicPort,
        lbPublicProtocol,
        dns);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ServiceInstanceSpec {\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
    sb.append("    service: ").append(toIndentedString(service)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    lbPublicIp: ").append(toIndentedString(lbPublicIp)).append("\n");
    sb.append("    lbPublicPort: ").append(toIndentedString(lbPublicPort)).append("\n");
    sb.append("    lbPublicProtocol: ").append(toIndentedString(lbPublicProtocol)).append("\n");
    sb.append("    dns: ").append(toIndentedString(dns)).append("\n");
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
