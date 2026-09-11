package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  NatRulesResponse.JSON_PROPERTY_URL,
  NatRulesResponse.JSON_PROPERTY_URL_VERSION,
  NatRulesResponse.JSON_PROPERTY_VERSION,
  NatRulesResponse.JSON_PROPERTY_API_VERSION,
  NatRulesResponse.JSON_PROPERTY_NAT_RULES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NatRulesResponse {
  public static final String JSON_PROPERTY_URL = "url";
  @Nullable private String url;

  public static final String JSON_PROPERTY_URL_VERSION = "url_version";
  @Nullable private String urlVersion;

  public static final String JSON_PROPERTY_VERSION = "version";
  @Nullable private String version;

  public static final String JSON_PROPERTY_API_VERSION = "api_version";
  @Nullable private String apiVersion;

  public static final String JSON_PROPERTY_NAT_RULES = "nat_rules";
  @Nullable private List<NatRules> natRules;

  public NatRulesResponse() {}

  public NatRulesResponse url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   *
   * @return url
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_URL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUrl() {
    return url;
  }

  @JsonProperty(value = JSON_PROPERTY_URL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrl(@Nullable String url) {
    this.url = url;
  }

  public NatRulesResponse urlVersion(@Nullable String urlVersion) {
    this.urlVersion = urlVersion;
    return this;
  }

  /**
   * Get urlVersion
   *
   * @return urlVersion
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_URL_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUrlVersion() {
    return urlVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_URL_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrlVersion(@Nullable String urlVersion) {
    this.urlVersion = urlVersion;
  }

  public NatRulesResponse version(@Nullable String version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   *
   * @return version
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getVersion() {
    return version;
  }

  @JsonProperty(value = JSON_PROPERTY_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVersion(@Nullable String version) {
    this.version = version;
  }

  public NatRulesResponse apiVersion(@Nullable String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  /**
   * Get apiVersion
   *
   * @return apiVersion
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getApiVersion() {
    return apiVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setApiVersion(@Nullable String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public NatRulesResponse natRules(@Nullable List<NatRules> natRules) {
    this.natRules = natRules;
    return this;
  }

  public NatRulesResponse addNatRulesItem(NatRules natRulesItem) {
    if (this.natRules == null) {
      this.natRules = new ArrayList<>();
    }
    this.natRules.add(natRulesItem);
    return this;
  }

  /**
   * List of NAT Policies
   *
   * @return natRules
   */
  @Valid
  @Schema(required = false, description = "List of NAT Policies")
  @JsonProperty(value = JSON_PROPERTY_NAT_RULES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<NatRules> getNatRules() {
    return natRules;
  }

  @JsonProperty(value = JSON_PROPERTY_NAT_RULES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNatRules(@Nullable List<NatRules> natRules) {
    this.natRules = natRules;
  }

  /** Return true if this NatRulesResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NatRulesResponse natRulesResponse = (NatRulesResponse) o;
    return Objects.equals(this.url, natRulesResponse.url)
        && Objects.equals(this.urlVersion, natRulesResponse.urlVersion)
        && Objects.equals(this.version, natRulesResponse.version)
        && Objects.equals(this.apiVersion, natRulesResponse.apiVersion)
        && Objects.equals(this.natRules, natRulesResponse.natRules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, urlVersion, version, apiVersion, natRules);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NatRulesResponse {\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    urlVersion: ").append(toIndentedString(urlVersion)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    natRules: ").append(toIndentedString(natRules)).append("\n");
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
