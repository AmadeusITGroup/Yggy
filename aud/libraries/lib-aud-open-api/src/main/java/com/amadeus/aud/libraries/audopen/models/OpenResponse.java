package com.amadeus.aud.libraries.audopen.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  OpenResponse.JSON_PROPERTY_URL,
  OpenResponse.JSON_PROPERTY_URL_VERSION,
  OpenResponse.JSON_PROPERTY_VERSION,
  OpenResponse.JSON_PROPERTY_API_VERSION,
  OpenResponse.JSON_PROPERTY_RULES,
  OpenResponse.JSON_PROPERTY_SRC_IS_IP,
  OpenResponse.JSON_PROPERTY_DST_IS_IP,
  OpenResponse.JSON_PROPERTY_ERRORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OpenResponse {
  public static final String JSON_PROPERTY_URL = "url";
  @Nullable private String url;

  public static final String JSON_PROPERTY_URL_VERSION = "url_version";
  @Nullable private String urlVersion;

  public static final String JSON_PROPERTY_VERSION = "version";
  @Nullable private String version;

  public static final String JSON_PROPERTY_API_VERSION = "api_version";
  @Nullable private String apiVersion;

  public static final String JSON_PROPERTY_RULES = "rules";
  private List<SecurityRule> rules;

  public static final String JSON_PROPERTY_SRC_IS_IP = "src_is_ip";
  private Boolean srcIsIp;

  public static final String JSON_PROPERTY_DST_IS_IP = "dst_is_ip";
  private Boolean dstIsIp;

  public static final String JSON_PROPERTY_ERRORS = "errors";
  @Nullable private List<String> errors;

  public OpenResponse() {}

  public OpenResponse url(@Nullable String url) {
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

  public OpenResponse urlVersion(@Nullable String urlVersion) {
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

  public OpenResponse version(@Nullable String version) {
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

  public OpenResponse apiVersion(@Nullable String apiVersion) {
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

  public OpenResponse rules(List<SecurityRule> rules) {
    this.rules = rules;
    return this;
  }

  public OpenResponse addRulesItem(SecurityRule rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

  /**
   * Get rules
   *
   * @return rules
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid SecurityRule> getRules() {
    return rules;
  }

  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRules(List<SecurityRule> rules) {
    this.rules = rules;
  }

  public OpenResponse srcIsIp(Boolean srcIsIp) {
    this.srcIsIp = srcIsIp;
    return this;
  }

  /**
   * Get srcIsIp
   *
   * @return srcIsIp
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SRC_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getSrcIsIp() {
    return srcIsIp;
  }

  @JsonProperty(value = JSON_PROPERTY_SRC_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSrcIsIp(Boolean srcIsIp) {
    this.srcIsIp = srcIsIp;
  }

  public OpenResponse dstIsIp(Boolean dstIsIp) {
    this.dstIsIp = dstIsIp;
    return this;
  }

  /**
   * Get dstIsIp
   *
   * @return dstIsIp
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DST_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getDstIsIp() {
    return dstIsIp;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDstIsIp(Boolean dstIsIp) {
    this.dstIsIp = dstIsIp;
  }

  public OpenResponse errors(@Nullable List<String> errors) {
    this.errors = errors;
    return this;
  }

  public OpenResponse addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   *
   * @return errors
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getErrors() {
    return errors;
  }

  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setErrors(@Nullable List<String> errors) {
    this.errors = errors;
  }

  /** Return true if this OpenResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenResponse openResponse = (OpenResponse) o;
    return Objects.equals(this.url, openResponse.url)
        && Objects.equals(this.urlVersion, openResponse.urlVersion)
        && Objects.equals(this.version, openResponse.version)
        && Objects.equals(this.apiVersion, openResponse.apiVersion)
        && Objects.equals(this.rules, openResponse.rules)
        && Objects.equals(this.srcIsIp, openResponse.srcIsIp)
        && Objects.equals(this.dstIsIp, openResponse.dstIsIp)
        && Objects.equals(this.errors, openResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, urlVersion, version, apiVersion, rules, srcIsIp, dstIsIp, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenResponse {\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    urlVersion: ").append(toIndentedString(urlVersion)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    srcIsIp: ").append(toIndentedString(srcIsIp)).append("\n");
    sb.append("    dstIsIp: ").append(toIndentedString(dstIsIp)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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
