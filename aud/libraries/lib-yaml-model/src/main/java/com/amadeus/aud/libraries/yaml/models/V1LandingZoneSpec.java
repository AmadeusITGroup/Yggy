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

/** V1LandingZoneSpec */
@JsonPropertyOrder({
  V1LandingZoneSpec.JSON_PROPERTY_TARGET,
  V1LandingZoneSpec.JSON_PROPERTY_REGION,
  V1LandingZoneSpec.JSON_PROPERTY_MACROPHASE,
  V1LandingZoneSpec.JSON_PROPERTY_CIDRS,
  V1LandingZoneSpec.JSON_PROPERTY_LANDINGZONETYPE,
  V1LandingZoneSpec.JSON_PROPERTY_FUNCTIONALTYPES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1LandingZoneSpec {
  public static final String JSON_PROPERTY_TARGET = "target";
  private String target;

  public static final String JSON_PROPERTY_REGION = "region";
  private String region;

  public static final String JSON_PROPERTY_MACROPHASE = "macrophase";
  @Nullable private String macrophase;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<String> cidrs;

  public static final String JSON_PROPERTY_LANDINGZONETYPE = "landingzonetype";
  private String landingzonetype;

  public static final String JSON_PROPERTY_FUNCTIONALTYPES = "functionaltypes";
  @Nullable private List<String> functionaltypes;

  public V1LandingZoneSpec() {}

  public V1LandingZoneSpec target(String target) {
    this.target = target;
    return this;
  }

  /**
   * Name of target
   *
   * @return target
   */
  @NotNull
  @Schema(required = true, description = "Name of target")
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

  public V1LandingZoneSpec region(String region) {
    this.region = region;
    return this;
  }

  /**
   * Name of the region
   *
   * @return region
   */
  @NotNull
  @Schema(required = true, description = "Name of the region")
  @JsonProperty(value = JSON_PROPERTY_REGION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRegion() {
    return region;
  }

  @JsonProperty(value = JSON_PROPERTY_REGION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRegion(String region) {
    this.region = region;
  }

  public V1LandingZoneSpec macrophase(@Nullable String macrophase) {
    this.macrophase = macrophase;
    return this;
  }

  /**
   * Name of the macrophase
   *
   * @return macrophase
   */
  @Schema(description = "Name of the macrophase")
  @JsonProperty(value = JSON_PROPERTY_MACROPHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getMacrophase() {
    return macrophase;
  }

  @JsonProperty(value = JSON_PROPERTY_MACROPHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMacrophase(@Nullable String macrophase) {
    this.macrophase = macrophase;
  }

  public V1LandingZoneSpec cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public V1LandingZoneSpec addCidrsItem(String cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * List of vnet
   *
   * @return cidrs
   */
  @NotNull
  @Schema(required = true, description = "List of vnet")
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<String> cidrs) {
    this.cidrs = cidrs;
  }

  public V1LandingZoneSpec landingzonetype(String landingzonetype) {
    this.landingzonetype = landingzonetype;
    return this;
  }

  /**
   * Type of the Landing Zone
   *
   * @return landingzonetype
   */
  @NotNull
  @Schema(required = true, description = "Type of the Landing Zone")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONETYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzonetype() {
    return landingzonetype;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONETYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzonetype(String landingzonetype) {
    this.landingzonetype = landingzonetype;
  }

  public V1LandingZoneSpec functionaltypes(@Nullable List<String> functionaltypes) {
    this.functionaltypes = functionaltypes;
    return this;
  }

  public V1LandingZoneSpec addFunctionaltypesItem(String functionaltypesItem) {
    if (this.functionaltypes == null) {
      this.functionaltypes = new ArrayList<>();
    }
    this.functionaltypes.add(functionaltypesItem);
    return this;
  }

  /**
   * Functional types of the Landing Zone
   *
   * @return functionaltypes
   */
  @Schema(description = "Functional types of the Landing Zone")
  @JsonProperty(value = JSON_PROPERTY_FUNCTIONALTYPES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getFunctionaltypes() {
    return functionaltypes;
  }

  @JsonProperty(value = JSON_PROPERTY_FUNCTIONALTYPES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFunctionaltypes(@Nullable List<String> functionaltypes) {
    this.functionaltypes = functionaltypes;
  }

  /** Return true if this v1.LandingZoneSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1LandingZoneSpec v1LandingZoneSpec = (V1LandingZoneSpec) o;
    return Objects.equals(this.target, v1LandingZoneSpec.target)
        && Objects.equals(this.region, v1LandingZoneSpec.region)
        && Objects.equals(this.macrophase, v1LandingZoneSpec.macrophase)
        && Objects.equals(this.cidrs, v1LandingZoneSpec.cidrs)
        && Objects.equals(this.landingzonetype, v1LandingZoneSpec.landingzonetype)
        && Objects.equals(this.functionaltypes, v1LandingZoneSpec.functionaltypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(target, region, macrophase, cidrs, landingzonetype, functionaltypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1LandingZoneSpec {\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    landingzonetype: ").append(toIndentedString(landingzonetype)).append("\n");
    sb.append("    functionaltypes: ").append(toIndentedString(functionaltypes)).append("\n");
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
