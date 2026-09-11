package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** FirewallGroup */
@JsonPropertyOrder({FirewallGroup.JSON_PROPERTY_NAME, FirewallGroup.JSON_PROPERTY_VENDOR})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FirewallGroup {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_VENDOR = "vendor";
  private String vendor;

  public FirewallGroup() {}

  public FirewallGroup name(String name) {
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

  public FirewallGroup vendor(String vendor) {
    this.vendor = vendor;
    return this;
  }

  /**
   * Get vendor
   *
   * @return vendor
   */
  @NotNull
  @Schema(required = true, description = "")
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

  /** Return true if this FirewallGroup object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FirewallGroup firewallGroup = (FirewallGroup) o;
    return Objects.equals(this.name, firewallGroup.name)
        && Objects.equals(this.vendor, firewallGroup.vendor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, vendor);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FirewallGroup {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    vendor: ").append(toIndentedString(vendor)).append("\n");
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
