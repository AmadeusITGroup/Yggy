package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ZoneInstance specification */
@Schema(description = "ZoneInstance specification")
@JsonPropertyOrder({
  V1ZoneInstanceSpec.JSON_PROPERTY_ZONE_TYPE,
  V1ZoneInstanceSpec.JSON_PROPERTY_IS_N_S_G_NEEDED,
  V1ZoneInstanceSpec.JSON_PROPERTY_FIREWALL_GROUP
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ZoneInstanceSpec {
  public static final String JSON_PROPERTY_ZONE_TYPE = "zoneType";
  private String zoneType;

  public static final String JSON_PROPERTY_IS_N_S_G_NEEDED = "isNSGNeeded";
  @Nullable private Boolean isNSGNeeded = false;

  public static final String JSON_PROPERTY_FIREWALL_GROUP = "firewallGroup";
  @Nullable private String firewallGroup;

  public V1ZoneInstanceSpec() {}

  public V1ZoneInstanceSpec zoneType(String zoneType) {
    this.zoneType = zoneType;
    return this;
  }

  /**
   * Name of Zone Type
   *
   * @return zoneType
   */
  @NotNull
  @Schema(required = true, description = "Name of Zone Type")
  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getZoneType() {
    return zoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setZoneType(String zoneType) {
    this.zoneType = zoneType;
  }

  public V1ZoneInstanceSpec isNSGNeeded(@Nullable Boolean isNSGNeeded) {
    this.isNSGNeeded = isNSGNeeded;
    return this;
  }

  /**
   * Create or not an NSG on Azure (for technical zone only)
   *
   * @return isNSGNeeded
   */
  @Schema(required = false, description = "Create or not an NSG on Azure (for technical zone only)")
  @JsonProperty(value = JSON_PROPERTY_IS_N_S_G_NEEDED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsNSGNeeded() {
    return isNSGNeeded;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_N_S_G_NEEDED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsNSGNeeded(@Nullable Boolean isNSGNeeded) {
    this.isNSGNeeded = isNSGNeeded;
  }

  public V1ZoneInstanceSpec firewallGroup(@Nullable String firewallGroup) {
    this.firewallGroup = firewallGroup;
    return this;
  }

  /**
   * Name of the firewall Group
   *
   * @return firewallGroup
   */
  @Schema(required = false, description = "Name of the firewall Group")
  @JsonProperty(value = JSON_PROPERTY_FIREWALL_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getFirewallGroup() {
    return firewallGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_FIREWALL_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFirewallGroup(@Nullable String firewallGroup) {
    this.firewallGroup = firewallGroup;
  }

  /** Return true if this v1.ZoneInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ZoneInstanceSpec v1ZoneInstanceSpec = (V1ZoneInstanceSpec) o;
    return Objects.equals(this.zoneType, v1ZoneInstanceSpec.zoneType)
        && Objects.equals(this.isNSGNeeded, v1ZoneInstanceSpec.isNSGNeeded)
        && Objects.equals(this.firewallGroup, v1ZoneInstanceSpec.firewallGroup);
  }

  @Override
  public int hashCode() {
    return Objects.hash(zoneType, isNSGNeeded, firewallGroup);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ZoneInstanceSpec {\n");
    sb.append("    zoneType: ").append(toIndentedString(zoneType)).append("\n");
    sb.append("    isNSGNeeded: ").append(toIndentedString(isNSGNeeded)).append("\n");
    sb.append("    firewallGroup: ").append(toIndentedString(firewallGroup)).append("\n");
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
