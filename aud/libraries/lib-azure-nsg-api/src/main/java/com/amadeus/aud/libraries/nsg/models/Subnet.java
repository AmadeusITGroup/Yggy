package com.amadeus.aud.libraries.nsg.models;

import com.amadeus.aud.libraries.yaml.models.ObjectMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Subnet */
@JsonPropertyOrder({
  Subnet.JSON_PROPERTY_NAME,
  Subnet.JSON_PROPERTY_SUBNET_TYPE,
  Subnet.JSON_PROPERTY_ASSOCIATED_N_S_G,
  Subnet.JSON_PROPERTY_CIDR,
  Subnet.JSON_PROPERTY_ZONE_TYPE,
  Subnet.JSON_PROPERTY_ZONE_NAME,
  Subnet.JSON_PROPERTY_LANDING_ZONE,
  Subnet.JSON_PROPERTY_PROVIDER_SPECIFIC,
  Subnet.JSON_PROPERTY_TAGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Subnet extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_SUBNET_TYPE = "subnetType";
  @Nullable private String subnetType;

  public static final String JSON_PROPERTY_ASSOCIATED_N_S_G = "associatedNSG";
  @Nullable private String associatedNSG;

  public static final String JSON_PROPERTY_CIDR = "cidr";
  private String cidr;

  public static final String JSON_PROPERTY_ZONE_TYPE = "zoneType";
  private String zoneType;

  public static final String JSON_PROPERTY_ZONE_NAME = "zoneName";
  private String zoneName;

  public static final String JSON_PROPERTY_LANDING_ZONE = "landingZone";
  @Nullable private String landingZone;

  public static final String JSON_PROPERTY_PROVIDER_SPECIFIC = "provider_specific";
  @Nullable private Object providerSpecific;

  public static final String JSON_PROPERTY_TAGS = "tags";
  @Nullable private Map<String, String> tags;

  public Subnet() {}

  public Subnet name(String name) {
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

  public Subnet subnetType(@Nullable String subnetType) {
    this.subnetType = subnetType;
    return this;
  }

  /**
   * Get subnetType
   *
   * @return subnetType
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getSubnetType() {
    return subnetType;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSubnetType(@Nullable String subnetType) {
    this.subnetType = subnetType;
  }

  public Subnet associatedNSG(@Nullable String associatedNSG) {
    this.associatedNSG = associatedNSG;
    return this;
  }

  /**
   * Get associatedNSG
   *
   * @return associatedNSG
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_N_S_G)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getAssociatedNSG() {
    return associatedNSG;
  }

  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_N_S_G)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssociatedNSG(@Nullable String associatedNSG) {
    this.associatedNSG = associatedNSG;
  }

  public Subnet cidr(String cidr) {
    this.cidr = cidr;
    return this;
  }

  /**
   * Get cidr
   *
   * @return cidr
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public Subnet zoneType(String zoneType) {
    this.zoneType = zoneType;
    return this;
  }

  /**
   * Get zoneType
   *
   * @return zoneType
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public Subnet zoneName(String zoneName) {
    this.zoneName = zoneName;
    return this;
  }

  /**
   * Get zoneName
   *
   * @return zoneName
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_ZONE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getZoneName() {
    return zoneName;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setZoneName(String zoneName) {
    this.zoneName = zoneName;
  }

  public Subnet landingZone(@Nullable String landingZone) {
    this.landingZone = landingZone;
    return this;
  }

  /**
   * The name of the landing zone
   *
   * @return landingZone
   */
  @Schema(description = "The name of the landing zone")
  @JsonProperty(value = JSON_PROPERTY_LANDING_ZONE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getLandingZone() {
    return landingZone;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDING_ZONE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLandingZone(@Nullable String landingZone) {
    this.landingZone = landingZone;
  }

  public Subnet providerSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
    return this;
  }

  /**
   * Get providerSpecific
   *
   * @return providerSpecific
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getProviderSpecific() {
    return providerSpecific;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProviderSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
  }

  public Subnet tags(@Nullable Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  public Subnet putTagsItem(String key, String tagsItem) {
    if (this.tags == null) {
      this.tags = new HashMap<>();
    }
    this.tags.put(key, tagsItem);
    return this;
  }

  /**
   * Get tags
   *
   * @return tags
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_TAGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Map<String, String> getTags() {
    return tags;
  }

  @JsonProperty(value = JSON_PROPERTY_TAGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTags(@Nullable Map<String, String> tags) {
    this.tags = tags;
  }

  @Override
  public Subnet labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this Subnet object is equal to o. */
  @Override
  public boolean equals(@jakarta.annotation.Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subnet subnet = (Subnet) o;
    return Objects.equals(this.name, subnet.name)
        && Objects.equals(this.subnetType, subnet.subnetType)
        && Objects.equals(this.associatedNSG, subnet.associatedNSG)
        && Objects.equals(this.cidr, subnet.cidr)
        && Objects.equals(this.zoneType, subnet.zoneType)
        && Objects.equals(this.zoneName, subnet.zoneName)
        && Objects.equals(this.landingZone, subnet.landingZone)
        && Objects.equals(this.providerSpecific, subnet.providerSpecific)
        && Objects.equals(this.tags, subnet.tags)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        subnetType,
        associatedNSG,
        cidr,
        zoneType,
        zoneName,
        landingZone,
        providerSpecific,
        tags,
        super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Subnet {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    subnetType: ").append(toIndentedString(subnetType)).append("\n");
    sb.append("    associatedNSG: ").append(toIndentedString(associatedNSG)).append("\n");
    sb.append("    cidr: ").append(toIndentedString(cidr)).append("\n");
    sb.append("    zoneType: ").append(toIndentedString(zoneType)).append("\n");
    sb.append("    zoneName: ").append(toIndentedString(zoneName)).append("\n");
    sb.append("    landingZone: ").append(toIndentedString(landingZone)).append("\n");
    sb.append("    providerSpecific: ").append(toIndentedString(providerSpecific)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
