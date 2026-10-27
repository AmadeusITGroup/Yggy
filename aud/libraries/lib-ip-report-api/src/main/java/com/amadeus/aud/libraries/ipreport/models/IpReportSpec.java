package com.amadeus.aud.libraries.ipreport.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** IpReport specification */
@JsonPropertyOrder({
  IpReportSpec.JSON_PROPERTY_SUBNETS,
  IpReportSpec.JSON_PROPERTY_CIDRS,
  IpReportSpec.JSON_PROPERTY_IPS,
  IpReportSpec.JSON_PROPERTY_ERRORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpReportSpec {
  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  private List<@Valid IpReportSubnet> subnets;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<@Valid IpReportCidr> cidrs;

  public static final String JSON_PROPERTY_IPS = "ips";
  private List<@Valid IpReportIp> ips;

  public static final String JSON_PROPERTY_ERRORS = "errors";
  @Nullable private List<String> errors;

  public IpReportSpec() {}

  public IpReportSpec subnets(List<@Valid IpReportSubnet> subnets) {
    this.subnets = subnets;
    return this;
  }

  public IpReportSpec addSubnetsItem(IpReportSubnet subnetsItem) {
    if (this.subnets == null) {
      this.subnets = new ArrayList<>();
    }
    this.subnets.add(subnetsItem);
    return this;
  }

  /**
   * Get subnets
   *
   * @return subnets
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportSubnet> getSubnets() {
    return subnets;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnets(List<@Valid IpReportSubnet> subnets) {
    this.subnets = subnets;
  }

  public IpReportSpec cidrs(List<@Valid IpReportCidr> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public IpReportSpec addCidrsItem(IpReportCidr cidrsItem) {
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
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportCidr> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<@Valid IpReportCidr> cidrs) {
    this.cidrs = cidrs;
  }

  public IpReportSpec ips(List<@Valid IpReportIp> ips) {
    this.ips = ips;
    return this;
  }

  public IpReportSpec addIpsItem(IpReportIp ipsItem) {
    if (this.ips == null) {
      this.ips = new ArrayList<>();
    }
    this.ips.add(ipsItem);
    return this;
  }

  /**
   * Get ips
   *
   * @return ips
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_IPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportIp> getIps() {
    return ips;
  }

  @JsonProperty(value = JSON_PROPERTY_IPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIps(List<@Valid IpReportIp> ips) {
    this.ips = ips;
  }

  public IpReportSpec errors(@Nullable List<String> errors) {
    this.errors = errors;
    return this;
  }

  public IpReportSpec addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   *
   * @return errors
   */
  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getErrors() {
    return errors;
  }

  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setErrors(@Nullable List<String> errors) {
    this.errors = errors;
  }

  /** Return true if this IpReportSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpReportSpec ipReportSpec = (IpReportSpec) o;
    return Objects.equals(this.subnets, ipReportSpec.subnets)
        && Objects.equals(this.cidrs, ipReportSpec.cidrs)
        && Objects.equals(this.ips, ipReportSpec.ips)
        && Objects.equals(this.errors, ipReportSpec.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subnets, cidrs, ips, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpReportSpec {\n");
    sb.append("    subnets: ").append(toIndentedString(subnets)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    ips: ").append(toIndentedString(ips)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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
