package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Metadata to identify a resource instance. */
@Schema(description = "Metadata to identify a resource instance.")
@JsonPropertyOrder({
  V1MetadataInstance.JSON_PROPERTY_LANDINGZONE,
  V1MetadataInstance.JSON_PROPERTY_CREATION_DATE,
  V1MetadataInstance.JSON_PROPERTY_UPDATE_DATE,
  V1MetadataInstance.JSON_PROPERTY_CREATION_VERSION,
  V1MetadataInstance.JSON_PROPERTY_UPDATE_VERSION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1MetadataInstance extends V1Metadata {
  public static final String JSON_PROPERTY_LANDINGZONE = "landingzone";
  private String landingzone;

  public static final String JSON_PROPERTY_CREATION_DATE = "creation_date";
  @Nullable private java.time.LocalDateTime creationDate;

  public static final String JSON_PROPERTY_UPDATE_DATE = "update_date";
  @Nullable private java.time.LocalDateTime updateDate;

  public static final String JSON_PROPERTY_CREATION_VERSION = "creation_version";
  @Nullable private String creationVersion;

  public static final String JSON_PROPERTY_UPDATE_VERSION = "update_version";
  @Nullable private String updateVersion;

  public V1MetadataInstance() {}

  public V1MetadataInstance landingzone(String landingzone) {
    this.landingzone = landingzone;
    return this;
  }

  /**
   * The landing zone where this instance will be created. It&#39;s an environment or foundation for
   * hosting your workload and application, basically Hub or Spoke.
   *
   * @return landingzone
   */
  @NotNull
  @Schema(
      required = true,
      description =
          "The landing zone where this instance will be created. It's an environment or foundation for hosting your workload and application, basically Hub or Spoke.")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzone() {
    return landingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzone(String landingzone) {
    this.landingzone = landingzone;
  }

  public V1MetadataInstance creationDate(@Nullable java.time.LocalDateTime creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  /**
   * Instance creation date.
   *
   * @return creationDate
   */
  @Valid
  @Schema(required = false, description = "Instance creation date.")
  @JsonProperty(value = JSON_PROPERTY_CREATION_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable java.time.LocalDateTime getCreationDate() {
    return creationDate;
  }

  @JsonProperty(value = JSON_PROPERTY_CREATION_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCreationDate(@Nullable java.time.LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public V1MetadataInstance updateDate(@Nullable java.time.LocalDateTime updateDate) {
    this.updateDate = updateDate;
    return this;
  }

  /**
   * Instance update date.
   *
   * @return updateDate
   */
  @Valid
  @Schema(required = false, description = "Instance update date.")
  @JsonProperty(value = JSON_PROPERTY_UPDATE_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable java.time.LocalDateTime getUpdateDate() {
    return updateDate;
  }

  @JsonProperty(value = JSON_PROPERTY_UPDATE_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUpdateDate(@Nullable java.time.LocalDateTime updateDate) {
    this.updateDate = updateDate;
  }

  public V1MetadataInstance creationVersion(@Nullable String creationVersion) {
    this.creationVersion = creationVersion;
    return this;
  }

  /**
   * Instance creation requirements version.
   *
   * @return creationVersion
   */
  @Schema(required = false, description = "Instance creation requirements version.")
  @JsonProperty(value = JSON_PROPERTY_CREATION_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getCreationVersion() {
    return creationVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_CREATION_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCreationVersion(@Nullable String creationVersion) {
    this.creationVersion = creationVersion;
  }

  public V1MetadataInstance updateVersion(@Nullable String updateVersion) {
    this.updateVersion = updateVersion;
    return this;
  }

  /**
   * Instance update requirements version.
   *
   * @return updateVersion
   */
  @Schema(required = false, description = "Instance update requirements version.")
  @JsonProperty(value = JSON_PROPERTY_UPDATE_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUpdateVersion() {
    return updateVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_UPDATE_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUpdateVersion(@Nullable String updateVersion) {
    this.updateVersion = updateVersion;
  }

  @Override
  public V1MetadataInstance scope(String scope) {
    this.setScope(scope);
    return this;
  }

  @Override
  public V1MetadataInstance name(String name) {
    this.setName(name);
    return this;
  }

  @Override
  public V1MetadataInstance providerSpecific(@Nullable String providerSpecific) {
    this.setProviderSpecific(providerSpecific);
    return this;
  }

  @Override
  public V1MetadataInstance labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  @Override
  public V1MetadataInstance comment(@Nullable String comment) {
    this.setComment(comment);
    return this;
  }

  /** Return true if this v1MetadataInstance object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1MetadataInstance v1MetadataInstance = (V1MetadataInstance) o;
    return Objects.equals(this.landingzone, v1MetadataInstance.landingzone)
        && Objects.equals(this.creationDate, v1MetadataInstance.creationDate)
        && Objects.equals(this.updateDate, v1MetadataInstance.updateDate)
        && Objects.equals(this.creationVersion, v1MetadataInstance.creationVersion)
        && Objects.equals(this.updateVersion, v1MetadataInstance.updateVersion)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        landingzone, creationDate, updateDate, creationVersion, updateVersion, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1MetadataInstance {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
    sb.append("    updateDate: ").append(toIndentedString(updateDate)).append("\n");
    sb.append("    creationVersion: ").append(toIndentedString(creationVersion)).append("\n");
    sb.append("    updateVersion: ").append(toIndentedString(updateVersion)).append("\n");
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
