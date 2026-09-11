package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Message */
@JsonPropertyOrder({
  Message.JSON_PROPERTY_MESSAGE,
  Message.JSON_PROPERTY_TIMESTAMP,
  Message.JSON_PROPERTY_URL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Message {
  public static final String JSON_PROPERTY_MESSAGE = "message";
  private String message;

  public static final String JSON_PROPERTY_TIMESTAMP = "timestamp";
  private String timestamp;

  public static final String JSON_PROPERTY_URL = "url";
  private String url;

  public Message() {}

  public Message message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   *
   * @return message
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public Message timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   *
   * @return timestamp
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TIMESTAMP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTimestamp() {
    return timestamp;
  }

  @JsonProperty(value = JSON_PROPERTY_TIMESTAMP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Message url(String url) {
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

  /** Return true if this Message object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Message message = (Message) o;
    return Objects.equals(this.message, message.message)
        && Objects.equals(this.timestamp, message.timestamp)
        && Objects.equals(this.url, message.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, timestamp, url);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Message {\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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
