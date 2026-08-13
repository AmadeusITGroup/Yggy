package com.amadeus.aud.libraries.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

@JsonPropertyOrder({
  BaseResponse.JSON_PROPERTY_URL,
  BaseResponse.JSON_PROPERTY_URL_VERSION,
  BaseResponse.JSON_PROPERTY_VERSION,
  BaseResponse.JSON_PROPERTY_API_VERSION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BaseResponse {

  public static final String JSON_PROPERTY_URL = "url";
  public static final String JSON_PROPERTY_URL_VERSION = "url_version";
  public static final String JSON_PROPERTY_VERSION = "version";
  public static final String JSON_PROPERTY_API_VERSION = "api_version";

  @JsonProperty(JSON_PROPERTY_URL)
  private String url;

  @JsonProperty(JSON_PROPERTY_URL_VERSION)
  private String urlVersion;

  @JsonProperty(JSON_PROPERTY_VERSION)
  private String version;

  @JsonProperty(JSON_PROPERTY_API_VERSION)
  private String apiVersion;

  @JsonIgnore protected int hashcode;

  @JsonCreator
  public BaseResponse() {}

  public BaseResponse(String url, String urlVersion, String version, String apiVersion) {
    this.url = url;
    this.urlVersion = urlVersion;
    this.version = version;
    this.apiVersion = apiVersion;
  }

  @Override
  public boolean equals(Object o) {
    return this == o
        || ((o instanceof BaseResponse that)
            && url.equals(that.url)
            && urlVersion.equals(that.urlVersion)
            && version.equals(that.version)
            && apiVersion.equals(that.apiVersion));
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(url, urlVersion, version, apiVersion);
    }
    return hashcode;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrlVersion() {
    return urlVersion;
  }

  public void setUrlVersion(String urlVersion) {
    this.urlVersion = urlVersion;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }
}
