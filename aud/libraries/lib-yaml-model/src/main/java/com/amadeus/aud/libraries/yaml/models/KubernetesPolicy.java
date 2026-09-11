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

/** Kubernetes policy */
@Schema(description = "Kubernetes policy")
@JsonPropertyOrder({
  KubernetesPolicy.JSON_PROPERTY_NAME,
  KubernetesPolicy.JSON_PROPERTY_SOURCE,
  KubernetesPolicy.JSON_PROPERTY_TARGET,
  KubernetesPolicy.JSON_PROPERTY_TYPE,
  KubernetesPolicy.JSON_PROPERTY_PORTS,
  KubernetesPolicy.JSON_PROPERTY_PROTOCOL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KubernetesPolicy extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_SOURCE = "source";
  private String source;

  public static final String JSON_PROPERTY_TARGET = "target";
  private String target;

  public static final String JSON_PROPERTY_TYPE = "type";
  private String type;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private String protocol;

  public KubernetesPolicy() {}

  public KubernetesPolicy name(String name) {
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

  public KubernetesPolicy source(String source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   *
   * @return source
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SOURCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSource() {
    return source;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSource(String source) {
    this.source = source;
  }

  public KubernetesPolicy target(String target) {
    this.target = target;
    return this;
  }

  /**
   * Get target
   *
   * @return target
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TARGET, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTarget() {
    return target;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTarget(String target) {
    this.target = target;
  }

  public KubernetesPolicy type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getType() {
    return type;
  }

  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setType(String type) {
    this.type = type;
  }

  public KubernetesPolicy ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public KubernetesPolicy addPortsItem(String portsItem) {
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

  public KubernetesPolicy protocol(String protocol) {
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
  public String getProtocol() {
    return protocol;
  }

  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  @Override
  public KubernetesPolicy labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this KubernetesPolicy object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KubernetesPolicy kubernetesPolicy = (KubernetesPolicy) o;
    return Objects.equals(this.name, kubernetesPolicy.name)
        && Objects.equals(this.source, kubernetesPolicy.source)
        && Objects.equals(this.target, kubernetesPolicy.target)
        && Objects.equals(this.type, kubernetesPolicy.type)
        && Objects.equals(this.ports, kubernetesPolicy.ports)
        && Objects.equals(this.protocol, kubernetesPolicy.protocol)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, source, target, type, ports, protocol, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KubernetesPolicy {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
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
