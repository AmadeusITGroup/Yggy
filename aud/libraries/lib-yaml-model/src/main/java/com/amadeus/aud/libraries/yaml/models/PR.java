package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** PR */
@JsonPropertyOrder({PR.JSON_PROPERTY_NUMBER, PR.JSON_PROPERTY_URL})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PR {
  public static final String JSON_PROPERTY_NUMBER = "number";
  private String number;

  public static final String JSON_PROPERTY_URL = "url";
  private String url;

  public PR() {}

  public PR number(String number) {
    this.number = number;
    return this;
  }

  /**
   * Get number
   *
   * @return number
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NUMBER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNumber() {
    return number;
  }

  @JsonProperty(value = JSON_PROPERTY_NUMBER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNumber(String number) {
    this.number = number;
  }

  public PR url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   *
   * @return url
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_URL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getUrl() {
    return url;
  }

  @JsonProperty(value = JSON_PROPERTY_URL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUrl(String url) {
    this.url = url;
  }

  /** Return true if this PR object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PR pr = (PR) o;
    return Objects.equals(this.number, pr.number) && Objects.equals(this.url, pr.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, url);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PR {\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
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
