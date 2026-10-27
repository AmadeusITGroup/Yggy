package com.amadeus.aud.libraries.ipreport.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Ip Report Response */
@JsonPropertyOrder({
  IpResponse.JSON_PROPERTY_URL,
  IpResponse.JSON_PROPERTY_URL_VERSION,
  IpResponse.JSON_PROPERTY_VERSION,
  IpResponse.JSON_PROPERTY_API_VERSION,
  IpResponse.JSON_PROPERTY_SUBNETS,
  IpResponse.JSON_PROPERTY_CIDRS,
  IpResponse.JSON_PROPERTY_IPS,
  IpResponse.JSON_PROPERTY_ERRORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpResponse {
  public static final String JSON_PROPERTY_URL = "url";
  @Nullable private String url;

  public static final String JSON_PROPERTY_URL_VERSION = "url_version";
  @Nullable private String urlVersion;

  public static final String JSON_PROPERTY_VERSION = "version";
  @Nullable private String version;

  public static final String JSON_PROPERTY_API_VERSION = "api_version";
  @Nullable private String apiVersion;

  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  private List<@Valid IpReportSubnet> subnets;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<@Valid IpReportCidr> cidrs;

  public static final String JSON_PROPERTY_IPS = "ips";
  private List<@Valid IpReportIp> ips;

  public static final String JSON_PROPERTY_ERRORS = "errors";
  @Nullable private List<String> errors;

  public IpResponse() {}

  public IpResponse url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   *
   * @return url
   */
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

  public IpResponse urlVersion(@Nullable String urlVersion) {
    this.urlVersion = urlVersion;
    return this;
  }

  /**
   * Get urlVersion
   *
   * @return urlVersion
   */
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

  public IpResponse version(@Nullable String version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   *
   * @return version
   */
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

  public IpResponse apiVersion(@Nullable String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  /**
   * Get apiVersion
   *
   * @return apiVersion
   */
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

  public IpResponse subnets(List<@Valid IpReportSubnet> subnets) {
    this.subnets = subnets;
    return this;
  }

  public IpResponse addSubnetsItem(IpReportSubnet subnetsItem) {
    if (this.subnets == null) {
      this.subnets = new ArrayList<>();
    }
    this.subnets.add(subnetsItem);
    return this;
  }

  /**
   * Get subnets
   *
   * @return subnets
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportSubnet> getSubnets() {
    return subnets;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnets(List<@Valid IpReportSubnet> subnets) {
    this.subnets = subnets;
  }

  public IpResponse cidrs(List<@Valid IpReportCidr> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public IpResponse addCidrsItem(IpReportCidr cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * Get cidrs
   *
   * @return cidrs
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportCidr> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<@Valid IpReportCidr> cidrs) {
    this.cidrs = cidrs;
  }

  public IpResponse ips(List<@Valid IpReportIp> ips) {
    this.ips = ips;
    return this;
  }

  public IpResponse addIpsItem(IpReportIp ipsItem) {
    if (this.ips == null) {
      this.ips = new ArrayList<>();
    }
    this.ips.add(ipsItem);
    return this;
  }

  /**
   * Get ips
   *
   * @return ips
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_IPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportIp> getIps() {
    return ips;
  }

  @JsonProperty(value = JSON_PROPERTY_IPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIps(List<@Valid IpReportIp> ips) {
    this.ips = ips;
  }

  public IpResponse errors(@Nullable List<String> errors) {
    this.errors = errors;
    return this;
  }

  public IpResponse addErrorsItem(String errorsItem) {
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

  /** Return true if this IpResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpResponse ipResponse = (IpResponse) o;
    return Objects.equals(this.url, ipResponse.url)
        && Objects.equals(this.urlVersion, ipResponse.urlVersion)
        && Objects.equals(this.version, ipResponse.version)
        && Objects.equals(this.apiVersion, ipResponse.apiVersion)
        && Objects.equals(this.subnets, ipResponse.subnets)
        && Objects.equals(this.cidrs, ipResponse.cidrs)
        && Objects.equals(this.ips, ipResponse.ips)
        && Objects.equals(this.errors, ipResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, urlVersion, version, apiVersion, subnets, cidrs, ips, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpResponse {\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    urlVersion: ").append(toIndentedString(urlVersion)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    subnets: ").append(toIndentedString(subnets)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    ips: ").append(toIndentedString(ips)).append("\n");
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
