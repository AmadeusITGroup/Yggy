package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1ScopeVersionMappingSpec */
@JsonPropertyOrder({
  V1ScopeVersionMappingSpec.JSON_PROPERTY_VERSION_DATETIME,
  V1ScopeVersionMappingSpec.JSON_PROPERTY_SCOPE_VERSION,
  V1ScopeVersionMappingSpec.JSON_PROPERTY_AUD_VERSION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ScopeVersionMappingSpec {
  public static final String JSON_PROPERTY_VERSION_DATETIME = "version_datetime";
  @Nullable private String versionDatetime;

  public static final String JSON_PROPERTY_SCOPE_VERSION = "scope_version";
  @Nullable private String scopeVersion;

  public static final String JSON_PROPERTY_AUD_VERSION = "aud_version";
  @Nullable private String audVersion;

  public V1ScopeVersionMappingSpec() {}

  public V1ScopeVersionMappingSpec versionDatetime(@Nullable String versionDatetime) {
    this.versionDatetime = versionDatetime;
    return this;
  }

  /**
   * Timestamp of scope version creation
   *
   * @return versionDatetime
   */
  @Schema(required = false, description = "Timestamp of scope version creation")
  @JsonProperty(value = JSON_PROPERTY_VERSION_DATETIME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getVersionDatetime() {
    return versionDatetime;
  }

  @JsonProperty(value = JSON_PROPERTY_VERSION_DATETIME, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVersionDatetime(@Nullable String versionDatetime) {
    this.versionDatetime = versionDatetime;
  }

  public V1ScopeVersionMappingSpec scopeVersion(@Nullable String scopeVersion) {
    this.scopeVersion = scopeVersion;
    return this;
  }

  /**
   * Version of the scope
   *
   * @return scopeVersion
   */
  @Schema(required = false, description = "Version of the scope")
  @JsonProperty(value = JSON_PROPERTY_SCOPE_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getScopeVersion() {
    return scopeVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScopeVersion(@Nullable String scopeVersion) {
    this.scopeVersion = scopeVersion;
  }

  public V1ScopeVersionMappingSpec audVersion(@Nullable String audVersion) {
    this.audVersion = audVersion;
    return this;
  }

  /**
   * Version of the scope
   *
   * @return audVersion
   */
  @Schema(required = false, description = "Version of the scope")
  @JsonProperty(value = JSON_PROPERTY_AUD_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getAudVersion() {
    return audVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_AUD_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAudVersion(@Nullable String audVersion) {
    this.audVersion = audVersion;
  }

  /** Return true if this v1.ScopeVersionMappingSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ScopeVersionMappingSpec v1ScopeVersionMappingSpec = (V1ScopeVersionMappingSpec) o;
    return Objects.equals(this.versionDatetime, v1ScopeVersionMappingSpec.versionDatetime)
        && Objects.equals(this.scopeVersion, v1ScopeVersionMappingSpec.scopeVersion)
        && Objects.equals(this.audVersion, v1ScopeVersionMappingSpec.audVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionDatetime, scopeVersion, audVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ScopeVersionMappingSpec {\n");
    sb.append("    versionDatetime: ").append(toIndentedString(versionDatetime)).append("\n");
    sb.append("    scopeVersion: ").append(toIndentedString(scopeVersion)).append("\n");
    sb.append("    audVersion: ").append(toIndentedString(audVersion)).append("\n");
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
