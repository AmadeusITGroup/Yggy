package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** SecurityGroupMap */
@JsonPropertyOrder({
  SecurityGroupMap.JSON_PROPERTY_INBOUND,
  SecurityGroupMap.JSON_PROPERTY_OUTBOUND
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityGroupMap {
  public static final String JSON_PROPERTY_INBOUND = "inbound";
  private List<SubnetPort> inbound;

  public static final String JSON_PROPERTY_OUTBOUND = "outbound";
  private List<@Valid ServiceTagOutboundRule> outbound;

  public SecurityGroupMap() {}

  public SecurityGroupMap inbound(List<SubnetPort> inbound) {
    this.inbound = inbound;
    return this;
  }

  public SecurityGroupMap addInboundItem(SubnetPort inboundItem) {
    if (this.inbound == null) {
      this.inbound = new ArrayList<>();
    }
    this.inbound.add(inboundItem);
    return this;
  }

  /**
   * Get inbound
   *
   * @return inbound
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_INBOUND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<SubnetPort> getInbound() {
    return inbound;
  }

  @JsonProperty(value = JSON_PROPERTY_INBOUND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setInbound(List<SubnetPort> inbound) {
    this.inbound = inbound;
  }

  public SecurityGroupMap outbound(List<@Valid ServiceTagOutboundRule> outbound) {
    this.outbound = outbound;
    return this;
  }

  public SecurityGroupMap addOutboundItem(ServiceTagOutboundRule outboundItem) {
    if (this.outbound == null) {
      this.outbound = new ArrayList<>();
    }
    this.outbound.add(outboundItem);
    return this;
  }

  /**
   * List of outbound service tag rules
   *
   * @return outbound
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "List of outbound service tag rules")
  @JsonProperty(value = JSON_PROPERTY_OUTBOUND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid ServiceTagOutboundRule> getOutbound() {
    return outbound;
  }

  @JsonProperty(value = JSON_PROPERTY_OUTBOUND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOutbound(List<@Valid ServiceTagOutboundRule> outbound) {
    this.outbound = outbound;
  }

  /** Return true if this SecurityGroupMap object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityGroupMap securityGroupMap = (SecurityGroupMap) o;
    return Objects.equals(this.inbound, securityGroupMap.inbound)
        && Objects.equals(this.outbound, securityGroupMap.outbound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inbound, outbound);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityGroupMap {\n");
    sb.append("    inbound: ").append(toIndentedString(inbound)).append("\n");
    sb.append("    outbound: ").append(toIndentedString(outbound)).append("\n");
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
