package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ApiMetadata */
@JsonPropertyOrder({
  ApiMetadata.JSON_PROPERTY_URL,
  ApiMetadata.JSON_PROPERTY_URL_VERSION,
  ApiMetadata.JSON_PROPERTY_VERSION,
  ApiMetadata.JSON_PROPERTY_API_VERSION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiMetadata {
  public static final String JSON_PROPERTY_URL = "url";
  @Nullable private String url;

  public static final String JSON_PROPERTY_URL_VERSION = "url_version";
  @Nullable private String urlVersion;

  public static final String JSON_PROPERTY_VERSION = "version";
  @Nullable private String version;

  public static final String JSON_PROPERTY_API_VERSION = "api_version";
  @Nullable private String apiVersion;

  public ApiMetadata() {}

  public ApiMetadata url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   *
   * @return url
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUrl() {
    return url;
  }

  @JsonProperty(value = JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrl(@Nullable String url) {
    this.url = url;
  }

  public ApiMetadata urlVersion(@Nullable String urlVersion) {
    this.urlVersion = urlVersion;
    return this;
  }

  /**
   * Get urlVersion
   *
   * @return urlVersion
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_URL_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUrlVersion() {
    return urlVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_URL_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrlVersion(@Nullable String urlVersion) {
    this.urlVersion = urlVersion;
  }

  public ApiMetadata version(@Nullable String version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   *
   * @return version
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getVersion() {
    return version;
  }

  @JsonProperty(value = JSON_PROPERTY_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVersion(@Nullable String version) {
    this.version = version;
  }

  public ApiMetadata apiVersion(@Nullable String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  /**
   * Get apiVersion
   *
   * @return apiVersion
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_API_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getApiVersion() {
    return apiVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_API_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setApiVersion(@Nullable String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /** Return true if this ApiMetadata object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiMetadata apiMetadata = (ApiMetadata) o;
    return Objects.equals(this.url, apiMetadata.url)
        && Objects.equals(this.urlVersion, apiMetadata.urlVersion)
        && Objects.equals(this.version, apiMetadata.version)
        && Objects.equals(this.apiVersion, apiMetadata.apiVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, urlVersion, version, apiVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiMetadata {\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    urlVersion: ").append(toIndentedString(urlVersion)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
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
