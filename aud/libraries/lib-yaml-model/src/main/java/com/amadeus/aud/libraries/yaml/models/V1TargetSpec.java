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

/** V1TargetSpec */
@JsonPropertyOrder({
  V1TargetSpec.JSON_PROPERTY_PROVIDER,
  V1TargetSpec.JSON_PROPERTY_CIDRS,
  V1TargetSpec.JSON_PROPERTY_IS_CLOUD,
  V1TargetSpec.JSON_PROPERTY_IS_AMADEUS,
  V1TargetSpec.JSON_PROPERTY_IS_AUD_MANAGED,
  V1TargetSpec.JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY,
  V1TargetSpec.JSON_PROPERTY_ENABLE_VNET_TO_VNET,
  V1TargetSpec.JSON_PROPERTY_ENABLE_JUMPSERVER_CONNECTIVITY,
  V1TargetSpec.JSON_PROPERTY_MACRO_PHASE,
  V1TargetSpec.JSON_PROPERTY_CONTINENT,
  V1TargetSpec.JSON_PROPERTY_TARGETTYPE,
  V1TargetSpec.JSON_PROPERTY_POP_PRIMARY,
  V1TargetSpec.JSON_PROPERTY_POP_SECONDARY
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1TargetSpec {
  public static final String JSON_PROPERTY_PROVIDER = "provider";
  private String provider;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  @Nullable private List<String> cidrs;

  public static final String JSON_PROPERTY_IS_CLOUD = "isCloud";
  @Nullable private Boolean isCloud = false;

  public static final String JSON_PROPERTY_IS_AMADEUS = "isAmadeus";
  @Nullable private Boolean isAmadeus = false;

  public static final String JSON_PROPERTY_IS_AUD_MANAGED = "isAudManaged";
  @Nullable private Boolean isAudManaged = false;

  public static final String JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY = "enableOfficeConnectivity";
  @Nullable private Boolean enableOfficeConnectivity = false;

  public static final String JSON_PROPERTY_ENABLE_VNET_TO_VNET = "enableVnetToVnet";
  @Nullable private Boolean enableVnetToVnet = false;

  public static final String JSON_PROPERTY_ENABLE_JUMPSERVER_CONNECTIVITY =
      "enableJumpserverConnectivity";
  @Nullable private Boolean enableJumpserverConnectivity = false;

  public static final String JSON_PROPERTY_MACRO_PHASE = "macroPhase";
  private String macroPhase;

  public static final String JSON_PROPERTY_CONTINENT = "continent";
  @Nullable private String continent;

  public static final String JSON_PROPERTY_TARGETTYPE = "targettype";
  @Nullable private String targettype;

  public static final String JSON_PROPERTY_POP_PRIMARY = "popPrimary";
  @Nullable private String popPrimary;

  public static final String JSON_PROPERTY_POP_SECONDARY = "popSecondary";
  @Nullable private String popSecondary;

  public V1TargetSpec() {}

  public V1TargetSpec provider(String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Name of provider
   *
   * @return provider
   */
  @NotNull
  @Schema(required = true, description = "Name of provider")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProvider() {
    return provider;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProvider(String provider) {
    this.provider = provider;
  }

  public V1TargetSpec cidrs(@Nullable List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public V1TargetSpec addCidrsItem(String cidrsItem) {
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
  @Schema(description = "List of vnet")
  @JsonProperty(value = JSON_PROPERTY_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCidrs(@Nullable List<String> cidrs) {
    this.cidrs = cidrs;
  }

  public V1TargetSpec isCloud(@Nullable Boolean isCloud) {
    this.isCloud = isCloud;
    return this;
  }

  /**
   * is it a cloud target?
   *
   * @return isCloud
   */
  @Schema(description = "is it a cloud target?")
  @JsonProperty(value = JSON_PROPERTY_IS_CLOUD)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsCloud() {
    return isCloud;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_CLOUD)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsCloud(@Nullable Boolean isCloud) {
    this.isCloud = isCloud;
  }

  public V1TargetSpec isAmadeus(@Nullable Boolean isAmadeus) {
    this.isAmadeus = isAmadeus;
    return this;
  }

  /**
   * is is managed by Amadeus?
   *
   * @return isAmadeus
   */
  @Schema(description = "is is managed by Amadeus?")
  @JsonProperty(value = JSON_PROPERTY_IS_AMADEUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAmadeus() {
    return isAmadeus;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_AMADEUS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAmadeus(@Nullable Boolean isAmadeus) {
    this.isAmadeus = isAmadeus;
  }

  public V1TargetSpec isAudManaged(@Nullable Boolean isAudManaged) {
    this.isAudManaged = isAudManaged;
    return this;
  }

  /**
   * is it AUD managed?
   *
   * @return isAudManaged
   */
  @Schema(description = "is it AUD managed?")
  @JsonProperty(value = JSON_PROPERTY_IS_AUD_MANAGED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAudManaged() {
    return isAudManaged;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_AUD_MANAGED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAudManaged(@Nullable Boolean isAudManaged) {
    this.isAudManaged = isAudManaged;
  }

  public V1TargetSpec enableOfficeConnectivity(@Nullable Boolean enableOfficeConnectivity) {
    this.enableOfficeConnectivity = enableOfficeConnectivity;
    return this;
  }

  /**
   * is Office Connectivity enabled?
   *
   * @return enableOfficeConnectivity
   */
  @Schema(description = "is Office Connectivity enabled?")
  @JsonProperty(value = JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getEnableOfficeConnectivity() {
    return enableOfficeConnectivity;
  }

  @JsonProperty(value = JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEnableOfficeConnectivity(@Nullable Boolean enableOfficeConnectivity) {
    this.enableOfficeConnectivity = enableOfficeConnectivity;
  }

  public V1TargetSpec enableVnetToVnet(@Nullable Boolean enableVnetToVnet) {
    this.enableVnetToVnet = enableVnetToVnet;
    return this;
  }

  /**
   * is Vnet to Vnet enabled?
   *
   * @return enableVnetToVnet
   */
  @Schema(description = "is Vnet to Vnet enabled?")
  @JsonProperty(value = JSON_PROPERTY_ENABLE_VNET_TO_VNET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getEnableVnetToVnet() {
    return enableVnetToVnet;
  }

  @JsonProperty(value = JSON_PROPERTY_ENABLE_VNET_TO_VNET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEnableVnetToVnet(@Nullable Boolean enableVnetToVnet) {
    this.enableVnetToVnet = enableVnetToVnet;
  }

  public V1TargetSpec enableJumpserverConnectivity(@Nullable Boolean enableJumpserverConnectivity) {
    this.enableJumpserverConnectivity = enableJumpserverConnectivity;
    return this;
  }

  /**
   * is Jumpserver Connectivity enabled?
   *
   * @return enableJumpserverConnectivity
   */
  @Schema(description = "is Jumpserver Connectivity enabled?")
  @JsonProperty(value = JSON_PROPERTY_ENABLE_JUMPSERVER_CONNECTIVITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getEnableJumpserverConnectivity() {
    return enableJumpserverConnectivity;
  }

  @JsonProperty(value = JSON_PROPERTY_ENABLE_JUMPSERVER_CONNECTIVITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEnableJumpserverConnectivity(@Nullable Boolean enableJumpserverConnectivity) {
    this.enableJumpserverConnectivity = enableJumpserverConnectivity;
  }

  public V1TargetSpec macroPhase(String macroPhase) {
    this.macroPhase = macroPhase;
    return this;
  }

  /**
   * Name of the macro phase
   *
   * @return macroPhase
   */
  @NotNull
  @Schema(required = true, description = "Name of the macro phase")
  @JsonProperty(value = JSON_PROPERTY_MACRO_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMacroPhase() {
    return macroPhase;
  }

  @JsonProperty(value = JSON_PROPERTY_MACRO_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMacroPhase(String macroPhase) {
    this.macroPhase = macroPhase;
  }

  public V1TargetSpec continent(@Nullable String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Name of the continent
   *
   * @return continent
   */
  @Schema(description = "Name of the continent")
  @JsonProperty(value = JSON_PROPERTY_CONTINENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getContinent() {
    return continent;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTINENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setContinent(@Nullable String continent) {
    this.continent = continent;
  }

  public V1TargetSpec targettype(@Nullable String targettype) {
    this.targettype = targettype;
    return this;
  }

  /**
   * Type of the Target
   *
   * @return targettype
   */
  @Schema(description = "Type of the Target")
  @JsonProperty(value = JSON_PROPERTY_TARGETTYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getTargettype() {
    return targettype;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGETTYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargettype(@Nullable String targettype) {
    this.targettype = targettype;
  }

  public V1TargetSpec popPrimary(@Nullable String popPrimary) {
    this.popPrimary = popPrimary;
    return this;
  }

  /**
   * Name of the POP target used as primary
   *
   * @return popPrimary
   */
  @Schema(description = "Name of the POP target used as primary")
  @JsonProperty(value = JSON_PROPERTY_POP_PRIMARY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getPopPrimary() {
    return popPrimary;
  }

  @JsonProperty(value = JSON_PROPERTY_POP_PRIMARY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPopPrimary(@Nullable String popPrimary) {
    this.popPrimary = popPrimary;
  }

  public V1TargetSpec popSecondary(@Nullable String popSecondary) {
    this.popSecondary = popSecondary;
    return this;
  }

  /**
   * Name of the POP target used as backup
   *
   * @return popSecondary
   */
  @Schema(description = "Name of the POP target used as backup")
  @JsonProperty(value = JSON_PROPERTY_POP_SECONDARY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getPopSecondary() {
    return popSecondary;
  }

  @JsonProperty(value = JSON_PROPERTY_POP_SECONDARY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPopSecondary(@Nullable String popSecondary) {
    this.popSecondary = popSecondary;
  }

  /** Return true if this v1.TargetSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1TargetSpec v1TargetSpec = (V1TargetSpec) o;
    return Objects.equals(this.provider, v1TargetSpec.provider)
        && Objects.equals(this.cidrs, v1TargetSpec.cidrs)
        && Objects.equals(this.isCloud, v1TargetSpec.isCloud)
        && Objects.equals(this.isAmadeus, v1TargetSpec.isAmadeus)
        && Objects.equals(this.isAudManaged, v1TargetSpec.isAudManaged)
        && Objects.equals(this.enableOfficeConnectivity, v1TargetSpec.enableOfficeConnectivity)
        && Objects.equals(this.enableVnetToVnet, v1TargetSpec.enableVnetToVnet)
        && Objects.equals(
            this.enableJumpserverConnectivity, v1TargetSpec.enableJumpserverConnectivity)
        && Objects.equals(this.macroPhase, v1TargetSpec.macroPhase)
        && Objects.equals(this.continent, v1TargetSpec.continent)
        && Objects.equals(this.targettype, v1TargetSpec.targettype)
        && Objects.equals(this.popPrimary, v1TargetSpec.popPrimary)
        && Objects.equals(this.popSecondary, v1TargetSpec.popSecondary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        provider,
        cidrs,
        isCloud,
        isAmadeus,
        isAudManaged,
        enableOfficeConnectivity,
        enableVnetToVnet,
        enableJumpserverConnectivity,
        macroPhase,
        continent,
        targettype,
        popPrimary,
        popSecondary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1TargetSpec {\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    isCloud: ").append(toIndentedString(isCloud)).append("\n");
    sb.append("    isAmadeus: ").append(toIndentedString(isAmadeus)).append("\n");
    sb.append("    isAudManaged: ").append(toIndentedString(isAudManaged)).append("\n");
    sb.append("    enableOfficeConnectivity: ")
        .append(toIndentedString(enableOfficeConnectivity))
        .append("\n");
    sb.append("    enableVnetToVnet: ").append(toIndentedString(enableVnetToVnet)).append("\n");
    sb.append("    enableJumpserverConnectivity: ")
        .append(toIndentedString(enableJumpserverConnectivity))
        .append("\n");
    sb.append("    macroPhase: ").append(toIndentedString(macroPhase)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
    sb.append("    targettype: ").append(toIndentedString(targettype)).append("\n");
    sb.append("    popPrimary: ").append(toIndentedString(popPrimary)).append("\n");
    sb.append("    popSecondary: ").append(toIndentedString(popSecondary)).append("\n");
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
