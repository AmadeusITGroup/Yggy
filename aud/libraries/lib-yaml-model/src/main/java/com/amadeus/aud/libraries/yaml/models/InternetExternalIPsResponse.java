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

/** InternetExternalIPsResponse */
@JsonPropertyOrder({InternetExternalIPsResponse.JSON_PROPERTY_INTERNETEXTERNALIPS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InternetExternalIPsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_INTERNETEXTERNALIPS = "internetexternalips";
  private List<String> internetexternalips;

  public InternetExternalIPsResponse() {}

  public InternetExternalIPsResponse internetexternalips(List<String> internetexternalips) {
    this.internetexternalips = internetexternalips;
    return this;
  }

  public InternetExternalIPsResponse addInternetexternalipsItem(String internetexternalipsItem) {
    if (this.internetexternalips == null) {
      this.internetexternalips = new ArrayList<>();
    }
    this.internetexternalips.add(internetexternalipsItem);
    return this;
  }

  /**
   * List of public ips
   *
   * @return internetexternalips
   */
  @NotNull
  @Schema(required = true, description = "List of public ips")
  @JsonProperty(value = JSON_PROPERTY_INTERNETEXTERNALIPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getInternetexternalips() {
    return internetexternalips;
  }

  @JsonProperty(value = JSON_PROPERTY_INTERNETEXTERNALIPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setInternetexternalips(List<String> internetexternalips) {
    this.internetexternalips = internetexternalips;
  }

  @Override
  public InternetExternalIPsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public InternetExternalIPsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public InternetExternalIPsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public InternetExternalIPsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this InternetExternalIPsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InternetExternalIPsResponse internetExternalIPsResponse = (InternetExternalIPsResponse) o;
    return Objects.equals(this.internetexternalips, internetExternalIPsResponse.internetexternalips)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(internetexternalips, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InternetExternalIPsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    internetexternalips: ")
        .append(toIndentedString(internetexternalips))
        .append("\n");
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
