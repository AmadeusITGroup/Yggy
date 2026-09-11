package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** SubnetInstance specification */
@Schema(description = "SubnetInstance specification")
@JsonPropertyOrder({
  V1SubnetInstanceSpec.JSON_PROPERTY_VLAN,
  V1SubnetInstanceSpec.JSON_PROPERTY_IS_SHARED,
  V1SubnetInstanceSpec.JSON_PROPERTY_IS_GROUP,
  V1SubnetInstanceSpec.JSON_PROPERTY_IS_NETWORK_PRIVATE,
  V1SubnetInstanceSpec.JSON_PROPERTY_SUBNET_TYPE,
  V1SubnetInstanceSpec.JSON_PROPERTY_ZONE_INSTANCE,
  V1SubnetInstanceSpec.JSON_PROPERTY_PARENT,
  V1SubnetInstanceSpec.JSON_PROPERTY_CIDR,
  V1SubnetInstanceSpec.JSON_PROPERTY_DEPRECATED_STATUS,
  V1SubnetInstanceSpec.JSON_PROPERTY_DEPRECATED_MESSAGE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1SubnetInstanceSpec {
  public static final String JSON_PROPERTY_VLAN = "vlan";
  @Nullable private String vlan;

  public static final String JSON_PROPERTY_IS_SHARED = "isShared";
  private Boolean isShared = false;

  public static final String JSON_PROPERTY_IS_GROUP = "isGroup";
  @Nullable private Boolean isGroup = false;

  public static final String JSON_PROPERTY_IS_NETWORK_PRIVATE = "isNetworkPrivate";
  @Nullable private Boolean isNetworkPrivate = false;

  public static final String JSON_PROPERTY_SUBNET_TYPE = "subnetType";
  private String subnetType;

  public static final String JSON_PROPERTY_ZONE_INSTANCE = "zoneInstance";
  private String zoneInstance;

  public static final String JSON_PROPERTY_PARENT = "parent";
  @Nullable private String parent;

  public static final String JSON_PROPERTY_CIDR = "cidr";
  private String cidr;

  public static final String JSON_PROPERTY_DEPRECATED_STATUS = "deprecatedStatus";
  @Nullable private DeprecatedStatus deprecatedStatus = DeprecatedStatus.NONE;

  public static final String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecatedMessage";
  @Nullable private String deprecatedMessage;

  public V1SubnetInstanceSpec() {}

  public V1SubnetInstanceSpec vlan(@Nullable String vlan) {
    this.vlan = vlan;
    return this;
  }

  /**
   * The vlan that correspond to the subnet
   *
   * @return vlan
   */
  @Schema(required = false, description = "The vlan that correspond to the subnet")
  @JsonProperty(value = JSON_PROPERTY_VLAN, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getVlan() {
    return vlan;
  }

  @JsonProperty(value = JSON_PROPERTY_VLAN, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVlan(@Nullable String vlan) {
    this.vlan = vlan;
  }

  public V1SubnetInstanceSpec isShared(Boolean isShared) {
    this.isShared = isShared;
    return this;
  }

  /**
   * is it shared between different workload?
   *
   * @return isShared
   */
  @NotNull
  @Schema(required = true, description = "is it shared between different workload?")
  @JsonProperty(value = JSON_PROPERTY_IS_SHARED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsShared() {
    return isShared;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SHARED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsShared(Boolean isShared) {
    this.isShared = isShared;
  }

  public V1SubnetInstanceSpec isGroup(@Nullable Boolean isGroup) {
    this.isGroup = isGroup;
    return this;
  }

  /**
   * is it a group of subnet?
   *
   * @return isGroup
   */
  @Schema(required = false, description = "is it a group of subnet?")
  @JsonProperty(value = JSON_PROPERTY_IS_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsGroup() {
    return isGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsGroup(@Nullable Boolean isGroup) {
    this.isGroup = isGroup;
  }

  public V1SubnetInstanceSpec isNetworkPrivate(@Nullable Boolean isNetworkPrivate) {
    this.isNetworkPrivate = isNetworkPrivate;
    return this;
  }

  /**
   * is the subnet for network team private usage?
   *
   * @return isNetworkPrivate
   */
  @Schema(required = false, description = "is the subnet for network team private usage?")
  @JsonProperty(value = JSON_PROPERTY_IS_NETWORK_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsNetworkPrivate() {
    return isNetworkPrivate;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_NETWORK_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsNetworkPrivate(@Nullable Boolean isNetworkPrivate) {
    this.isNetworkPrivate = isNetworkPrivate;
  }

  public V1SubnetInstanceSpec subnetType(String subnetType) {
    this.subnetType = subnetType;
    return this;
  }

  /**
   * Name of subnet Type
   *
   * @return subnetType
   */
  @NotNull
  @Schema(required = true, description = "Name of subnet Type")
  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSubnetType() {
    return subnetType;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnetType(String subnetType) {
    this.subnetType = subnetType;
  }

  public V1SubnetInstanceSpec zoneInstance(String zoneInstance) {
    this.zoneInstance = zoneInstance;
    return this;
  }

  /**
   * Name of zone instances or implementation zone.
   *
   * @return zoneInstance
   */
  @NotNull
  @Schema(required = true, description = "Name of zone instances or implementation zone.")
  @JsonProperty(value = JSON_PROPERTY_ZONE_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getZoneInstance() {
    return zoneInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setZoneInstance(String zoneInstance) {
    this.zoneInstance = zoneInstance;
  }

  public V1SubnetInstanceSpec parent(@Nullable String parent) {
    this.parent = parent;
    return this;
  }

  /**
   * Name of parent in case of grouping
   *
   * @return parent
   */
  @Schema(required = false, description = "Name of parent in case of grouping")
  @JsonProperty(value = JSON_PROPERTY_PARENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getParent() {
    return parent;
  }

  @JsonProperty(value = JSON_PROPERTY_PARENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setParent(@Nullable String parent) {
    this.parent = parent;
  }

  public V1SubnetInstanceSpec cidr(String cidr) {
    this.cidr = cidr;
    return this;
  }

  /**
   * cidr
   *
   * @return cidr
   */
  @NotNull
  @Schema(required = true, description = "cidr")
  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCidr() {
    return cidr;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

  public V1SubnetInstanceSpec deprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
    return this;
  }

  /**
   * Get deprecatedStatus
   *
   * @return deprecatedStatus
   */
  @Valid
  @Schema(required = false, description = "Deprecated status of the subnet instance.")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return deprecatedStatus;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public V1SubnetInstanceSpec deprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
    return this;
  }

  /**
   * Additional info on the deprecated component (e.g. replacement information/ owner contact
   * details)
   *
   * @return deprecatedMessage
   */
  @Schema(
      required = false,
      description =
          "Additional info on the deprecated component (e.g. replacement information/ owner contact details)")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeprecatedMessage() {
    return deprecatedMessage;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
  }

  /** Return true if this v1.SubnetInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1SubnetInstanceSpec v1SubnetInstanceSpec = (V1SubnetInstanceSpec) o;
    return Objects.equals(this.vlan, v1SubnetInstanceSpec.vlan)
        && Objects.equals(this.isShared, v1SubnetInstanceSpec.isShared)
        && Objects.equals(this.isGroup, v1SubnetInstanceSpec.isGroup)
        && Objects.equals(this.isNetworkPrivate, v1SubnetInstanceSpec.isNetworkPrivate)
        && Objects.equals(this.subnetType, v1SubnetInstanceSpec.subnetType)
        && Objects.equals(this.zoneInstance, v1SubnetInstanceSpec.zoneInstance)
        && Objects.equals(this.parent, v1SubnetInstanceSpec.parent)
        && Objects.equals(this.cidr, v1SubnetInstanceSpec.cidr)
        && Objects.equals(this.deprecatedStatus, v1SubnetInstanceSpec.deprecatedStatus)
        && Objects.equals(this.deprecatedMessage, v1SubnetInstanceSpec.deprecatedMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        vlan,
        isShared,
        isGroup,
        isNetworkPrivate,
        subnetType,
        zoneInstance,
        parent,
        cidr,
        deprecatedStatus,
        deprecatedMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1SubnetInstanceSpec {\n");
    sb.append("    vlan: ").append(toIndentedString(vlan)).append("\n");
    sb.append("    isShared: ").append(toIndentedString(isShared)).append("\n");
    sb.append("    isGroup: ").append(toIndentedString(isGroup)).append("\n");
    sb.append("    isNetworkPrivate: ").append(toIndentedString(isNetworkPrivate)).append("\n");
    sb.append("    subnetType: ").append(toIndentedString(subnetType)).append("\n");
    sb.append("    zoneInstance: ").append(toIndentedString(zoneInstance)).append("\n");
    sb.append("    parent: ").append(toIndentedString(parent)).append("\n");
    sb.append("    cidr: ").append(toIndentedString(cidr)).append("\n");
    sb.append("    deprecatedStatus: ").append(toIndentedString(deprecatedStatus)).append("\n");
    sb.append("    deprecatedMessage: ").append(toIndentedString(deprecatedMessage)).append("\n");
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
