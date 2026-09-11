package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Version */
@JsonPropertyOrder({
  Version.JSON_PROPERTY_REQUIREMENTS_VERSION,
  Version.JSON_PROPERTY_API_VERSION,
  Version.JSON_PROPERTY_REQUIREMENTS_MD5,
  Version.JSON_PROPERTY_PLATFORM
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Version {
  public static final String JSON_PROPERTY_REQUIREMENTS_VERSION = "requirements_version";
  private String requirementsVersion;

  public static final String JSON_PROPERTY_API_VERSION = "api_version";
  private String apiVersion;

  public static final String JSON_PROPERTY_REQUIREMENTS_MD5 = "requirements_md5";
  @Nullable private String requirementsMd5;

  public static final String JSON_PROPERTY_PLATFORM = "platform";
  private String platform;

  public Version() {}

  public Version requirementsVersion(String requirementsVersion) {
    this.requirementsVersion = requirementsVersion;
    return this;
  }

  /**
   * Get requirementsVersion
   *
   * @return requirementsVersion
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_REQUIREMENTS_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRequirementsVersion() {
    return requirementsVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_REQUIREMENTS_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRequirementsVersion(String requirementsVersion) {
    this.requirementsVersion = requirementsVersion;
  }

  public Version apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  /**
   * Get apiVersion
   *
   * @return apiVersion
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getApiVersion() {
    return apiVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public Version requirementsMd5(@Nullable String requirementsMd5) {
    this.requirementsMd5 = requirementsMd5;
    return this;
  }

  /**
   * Get requirementsMd5
   *
   * @return requirementsMd5
   * @deprecated
   */
  @Deprecated
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_REQUIREMENTS_MD5, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getRequirementsMd5() {
    return requirementsMd5;
  }

  @JsonProperty(value = JSON_PROPERTY_REQUIREMENTS_MD5, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRequirementsMd5(@Nullable String requirementsMd5) {
    this.requirementsMd5 = requirementsMd5;
  }

  public Version platform(String platform) {
    this.platform = platform;
    return this;
  }

  /**
   * Get platform
   *
   * @return platform
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PLATFORM, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPlatform() {
    return platform;
  }

  @JsonProperty(value = JSON_PROPERTY_PLATFORM, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /** Return true if this Version object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version version = (Version) o;
    return Objects.equals(this.requirementsVersion, version.requirementsVersion)
        && Objects.equals(this.apiVersion, version.apiVersion)
        && Objects.equals(this.requirementsMd5, version.requirementsMd5)
        && Objects.equals(this.platform, version.platform);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requirementsVersion, apiVersion, requirementsMd5, platform);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Version {\n");
    sb.append("    requirementsVersion: ")
        .append(toIndentedString(requirementsVersion))
        .append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    requirementsMd5: ").append(toIndentedString(requirementsMd5)).append("\n");
    sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
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
