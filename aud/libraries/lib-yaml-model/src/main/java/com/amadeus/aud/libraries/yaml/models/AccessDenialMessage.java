package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** AccessDenialMessage */
@JsonPropertyOrder({
  AccessDenialMessage.JSON_PROPERTY_USER_MESSAGE,
  AccessDenialMessage.JSON_PROPERTY_USER_MESSAGE_SEVERITY
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessDenialMessage {
  public static final String JSON_PROPERTY_USER_MESSAGE = "user_message";
  @Nullable private String userMessage;

  /** The severity of the denial; empty value defaults to ERROR in AUD engine; one of */
  public enum UserMessageSeverityEnum {
    ERROR(String.valueOf("ERROR")),

    WARNING(String.valueOf("WARNING")),

    EMPTY(String.valueOf(""));

    private String value;

    UserMessageSeverityEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UserMessageSeverityEnum fromValue(String value) {
      for (UserMessageSeverityEnum b : UserMessageSeverityEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_USER_MESSAGE_SEVERITY = "user_message_severity";
  @Nullable private UserMessageSeverityEnum userMessageSeverity = UserMessageSeverityEnum.EMPTY;

  public AccessDenialMessage() {}

  public AccessDenialMessage userMessage(@Nullable String userMessage) {
    this.userMessage = userMessage;
    return this;
  }

  /**
   * Message displayed to user; if null default hardcoded message will be displayed; users can use
   * two placeholders {edge} and {checked_object} they will be replaced by AUD engine
   *
   * @return userMessage
   */
  @Schema(
      description =
          "Message displayed to user; if null default hardcoded message will be displayed; users can use two placeholders {edge} and {checked_object} they will be replaced by AUD engine")
  @JsonProperty(value = JSON_PROPERTY_USER_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUserMessage() {
    return userMessage;
  }

  @JsonProperty(value = JSON_PROPERTY_USER_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUserMessage(@Nullable String userMessage) {
    this.userMessage = userMessage;
  }

  public AccessDenialMessage userMessageSeverity(
      @Nullable UserMessageSeverityEnum userMessageSeverity) {
    this.userMessageSeverity = userMessageSeverity;
    return this;
  }

  /**
   * The severity of the denial; empty value defaults to ERROR in AUD engine; one of
   *
   * @return userMessageSeverity
   */
  @Schema(
      description =
          "The severity of the denial; empty value defaults to ERROR in AUD engine; one of")
  @JsonProperty(value = JSON_PROPERTY_USER_MESSAGE_SEVERITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable UserMessageSeverityEnum getUserMessageSeverity() {
    return userMessageSeverity;
  }

  @JsonProperty(value = JSON_PROPERTY_USER_MESSAGE_SEVERITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUserMessageSeverity(@Nullable UserMessageSeverityEnum userMessageSeverity) {
    this.userMessageSeverity = userMessageSeverity;
  }

  /** Return true if this AccessDenialMessage object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessDenialMessage accessDenialMessage = (AccessDenialMessage) o;
    return Objects.equals(this.userMessage, accessDenialMessage.userMessage)
        && Objects.equals(this.userMessageSeverity, accessDenialMessage.userMessageSeverity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userMessage, userMessageSeverity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessDenialMessage {\n");
    sb.append("    userMessage: ").append(toIndentedString(userMessage)).append("\n");
    sb.append("    userMessageSeverity: ")
        .append(toIndentedString(userMessageSeverity))
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
