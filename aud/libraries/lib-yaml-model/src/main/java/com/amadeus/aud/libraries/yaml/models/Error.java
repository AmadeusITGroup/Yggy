package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Error */
@JsonPropertyOrder({Error.JSON_PROPERTY_CODE, Error.JSON_PROPERTY_MESSAGE})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Error {
  public static final String JSON_PROPERTY_CODE = "code";
  private Integer code;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  private String message;

  public Error() {}

  public Error code(Integer code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   *
   * @return code
   */
  @NotNull
  @Schema(
      examples = {"500"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_CODE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getCode() {
    return code;
  }

  @JsonProperty(value = JSON_PROPERTY_CODE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCode(Integer code) {
    this.code = code;
  }

  public Error message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   *
   * @return message
   */
  @NotNull
  @Schema(
      examples = {"SYSTEM ERROR HAS OCCURRED"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_MESSAGE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMessage() {
    return message;
  }

  @JsonProperty(value = JSON_PROPERTY_MESSAGE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMessage(String message) {
    this.message = message;
  }

  /** Return true if this Error object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Error error = (Error) o;
    return Objects.equals(this.code, error.code) && Objects.equals(this.message, error.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
