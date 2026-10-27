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

/** FirewallGroup specification */
@Schema(description = "FirewallGroup specification")
@JsonPropertyOrder({
  V1FirewallGroupSpec.JSON_PROPERTY_VENDOR,
  V1FirewallGroupSpec.JSON_PROPERTY_TARGET,
  V1FirewallGroupSpec.JSON_PROPERTY_EXTERNAL_IPS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1FirewallGroupSpec {
  public static final String JSON_PROPERTY_VENDOR = "vendor";
  private String vendor;

  public static final String JSON_PROPERTY_TARGET = "target";
  @Nullable private String target;

  public static final String JSON_PROPERTY_EXTERNAL_IPS = "external_ips";
  @Nullable private List<String> externalIps;

  public V1FirewallGroupSpec() {}

  public V1FirewallGroupSpec vendor(String vendor) {
    this.vendor = vendor;
    return this;
  }

  /**
   * Name of the vendor, e.g. cisco or paloato
   *
   * @return vendor
   */
  @NotNull
  @Schema(required = true, description = "Name of the vendor, e.g. cisco or paloato")
  @JsonProperty(value = JSON_PROPERTY_VENDOR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getVendor() {
    return vendor;
  }

  @JsonProperty(value = JSON_PROPERTY_VENDOR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public V1FirewallGroupSpec target(@Nullable String target) {
    this.target = target;
    return this;
  }

  /**
   * Name of the target
   *
   * @return target
   */
  @Schema(description = "Name of the target")
  @JsonProperty(value = JSON_PROPERTY_TARGET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getTarget() {
    return target;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTarget(@Nullable String target) {
    this.target = target;
  }

  public V1FirewallGroupSpec externalIps(@Nullable List<String> externalIps) {
    this.externalIps = externalIps;
    return this;
  }

  public V1FirewallGroupSpec addExternalIpsItem(String externalIpsItem) {
    if (this.externalIps == null) {
      this.externalIps = new ArrayList<>();
    }
    this.externalIps.add(externalIpsItem);
    return this;
  }

  /**
   * List of IPs for internet facade
   *
   * @return externalIps
   */
  @Schema(description = "List of IPs for internet facade")
  @JsonProperty(value = JSON_PROPERTY_EXTERNAL_IPS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getExternalIps() {
    return externalIps;
  }

  @JsonProperty(value = JSON_PROPERTY_EXTERNAL_IPS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExternalIps(@Nullable List<String> externalIps) {
    this.externalIps = externalIps;
  }

  /** Return true if this v1.FirewallGroupSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1FirewallGroupSpec v1FirewallGroupSpec = (V1FirewallGroupSpec) o;
    return Objects.equals(this.vendor, v1FirewallGroupSpec.vendor)
        && Objects.equals(this.target, v1FirewallGroupSpec.target)
        && Objects.equals(this.externalIps, v1FirewallGroupSpec.externalIps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vendor, target, externalIps);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1FirewallGroupSpec {\n");
    sb.append("    vendor: ").append(toIndentedString(vendor)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    externalIps: ").append(toIndentedString(externalIps)).append("\n");
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
