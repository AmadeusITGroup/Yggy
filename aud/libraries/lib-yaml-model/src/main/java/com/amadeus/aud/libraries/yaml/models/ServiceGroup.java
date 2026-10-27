package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
  ServiceGroup.JSON_PROPERTY_NAME,
  ServiceGroup.JSON_PROPERTY_UID,
  ServiceGroup.JSON_PROPERTY_PORTS,
  ServiceGroup.JSON_PROPERTY_PROTOCOL,
  ServiceGroup.JSON_PROPERTY_SCOPE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceGroup extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_UID = "uid";
  private String uid;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public static final String JSON_PROPERTY_SCOPE = "scope";
  @Nullable private String scope;

  public ServiceGroup() {}

  public ServiceGroup name(String name) {
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

  public ServiceGroup uid(String uid) {
    this.uid = uid;
    return this;
  }

  /**
   * Get uid
   *
   * @return uid
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getUid() {
    return uid;
  }

  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUid(String uid) {
    this.uid = uid;
  }

  public ServiceGroup ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public ServiceGroup addPortsItem(String portsItem) {
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

  public ServiceGroup protocol(ProtocolEnum protocol) {
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

  public ServiceGroup scope(@Nullable String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScope(@Nullable String scope) {
    this.scope = scope;
  }

  @Override
  public ServiceGroup labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this ServiceGroup object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceGroup serviceGroup = (ServiceGroup) o;
    return Objects.equals(this.name, serviceGroup.name)
        && Objects.equals(this.uid, serviceGroup.uid)
        && Objects.equals(this.ports, serviceGroup.ports)
        && Objects.equals(this.protocol, serviceGroup.protocol)
        && Objects.equals(this.scope, serviceGroup.scope)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, uid, ports, protocol, scope, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceGroup {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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
