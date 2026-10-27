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

/** Rule to open security for Service Tags */
@Schema(description = "Rule to open security for Service Tags")
@JsonPropertyOrder({
  RuleOpenServiceTags.JSON_PROPERTY_NAME,
  RuleOpenServiceTags.JSON_PROPERTY_SERVICE_TAG,
  RuleOpenServiceTags.JSON_PROPERTY_PORTS,
  RuleOpenServiceTags.JSON_PROPERTY_PROTOCOL,
  RuleOpenServiceTags.JSON_PROPERTY_DST_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RuleOpenServiceTags {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_SERVICE_TAG = "service_tag";
  private String serviceTag;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public static final String JSON_PROPERTY_DST_CIDRS = "dst_cidrs";
  @Nullable private List<String> dstCidrs;

  public RuleOpenServiceTags() {}

  public RuleOpenServiceTags name(String name) {
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

  public RuleOpenServiceTags serviceTag(String serviceTag) {
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

  public RuleOpenServiceTags ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public RuleOpenServiceTags addPortsItem(String portsItem) {
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

  public RuleOpenServiceTags protocol(ProtocolEnum protocol) {
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

  public RuleOpenServiceTags dstCidrs(@Nullable List<String> dstCidrs) {
    this.dstCidrs = dstCidrs;
    return this;
  }

  public RuleOpenServiceTags addDstCidrsItem(String dstCidrsItem) {
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
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_DST_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDstCidrs() {
    return dstCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDstCidrs(@Nullable List<String> dstCidrs) {
    this.dstCidrs = dstCidrs;
  }

  /** Return true if this RuleOpenServiceTags object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RuleOpenServiceTags ruleOpenServiceTags = (RuleOpenServiceTags) o;
    return Objects.equals(this.name, ruleOpenServiceTags.name)
        && Objects.equals(this.serviceTag, ruleOpenServiceTags.serviceTag)
        && Objects.equals(this.ports, ruleOpenServiceTags.ports)
        && Objects.equals(this.protocol, ruleOpenServiceTags.protocol)
        && Objects.equals(this.dstCidrs, ruleOpenServiceTags.dstCidrs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, serviceTag, ports, protocol, dstCidrs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RuleOpenServiceTags {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    serviceTag: ").append(toIndentedString(serviceTag)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    dstCidrs: ").append(toIndentedString(dstCidrs)).append("\n");
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
