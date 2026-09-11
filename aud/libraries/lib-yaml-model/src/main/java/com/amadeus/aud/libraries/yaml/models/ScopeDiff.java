package com.amadeus.aud.libraries.yaml.models;

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

/** ScopeDiff */
@JsonPropertyOrder({
  ScopeDiff.JSON_PROPERTY_SCOPE,
  ScopeDiff.JSON_PROPERTY_OLD_VERSION,
  ScopeDiff.JSON_PROPERTY_NEW_VERSION,
  ScopeDiff.JSON_PROPERTY_NANOID,
  ScopeDiff.JSON_PROPERTY_PRS,
  ScopeDiff.JSON_PROPERTY_MESSAGES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScopeDiff {
  public static final String JSON_PROPERTY_SCOPE = "scope";
  private String scope;

  public static final String JSON_PROPERTY_OLD_VERSION = "old_version";
  @Nullable private String oldVersion;

  public static final String JSON_PROPERTY_NEW_VERSION = "new_version";
  @Nullable private String newVersion;

  public static final String JSON_PROPERTY_NANOID = "nanoid";
  @Nullable private String nanoid;

  public static final String JSON_PROPERTY_PRS = "prs";
  private List<@Valid PR> prs;

  public static final String JSON_PROPERTY_MESSAGES = "messages";
  private List<@Valid Message> messages;

  public ScopeDiff() {}

  public ScopeDiff scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScope(String scope) {
    this.scope = scope;
  }

  public ScopeDiff oldVersion(@Nullable String oldVersion) {
    this.oldVersion = oldVersion;
    return this;
  }

  /**
   * Get oldVersion
   *
   * @return oldVersion
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_OLD_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getOldVersion() {
    return oldVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_OLD_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOldVersion(@Nullable String oldVersion) {
    this.oldVersion = oldVersion;
  }

  public ScopeDiff newVersion(@Nullable String newVersion) {
    this.newVersion = newVersion;
    return this;
  }

  /**
   * Get newVersion
   *
   * @return newVersion
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_NEW_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getNewVersion() {
    return newVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_NEW_VERSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNewVersion(@Nullable String newVersion) {
    this.newVersion = newVersion;
  }

  public ScopeDiff nanoid(@Nullable String nanoid) {
    this.nanoid = nanoid;
    return this;
  }

  /**
   * Get nanoid
   *
   * @return nanoid
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_NANOID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getNanoid() {
    return nanoid;
  }

  @JsonProperty(value = JSON_PROPERTY_NANOID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNanoid(@Nullable String nanoid) {
    this.nanoid = nanoid;
  }

  public ScopeDiff prs(List<@Valid PR> prs) {
    this.prs = prs;
    return this;
  }

  public ScopeDiff addPrsItem(PR prsItem) {
    if (this.prs == null) {
      this.prs = new ArrayList<>();
    }
    this.prs.add(prsItem);
    return this;
  }

  /**
   * Get prs
   *
   * @return prs
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid PR> getPrs() {
    return prs;
  }

  @JsonProperty(value = JSON_PROPERTY_PRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPrs(List<@Valid PR> prs) {
    this.prs = prs;
  }

  public ScopeDiff messages(List<@Valid Message> messages) {
    this.messages = messages;
    return this;
  }

  public ScopeDiff addMessagesItem(Message messagesItem) {
    if (this.messages == null) {
      this.messages = new ArrayList<>();
    }
    this.messages.add(messagesItem);
    return this;
  }

  /**
   * Get messages
   *
   * @return messages
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_MESSAGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid Message> getMessages() {
    return messages;
  }

  @JsonProperty(value = JSON_PROPERTY_MESSAGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMessages(List<@Valid Message> messages) {
    this.messages = messages;
  }

  /** Return true if this ScopeDiff object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeDiff scopeDiff = (ScopeDiff) o;
    return Objects.equals(this.scope, scopeDiff.scope)
        && Objects.equals(this.oldVersion, scopeDiff.oldVersion)
        && Objects.equals(this.newVersion, scopeDiff.newVersion)
        && Objects.equals(this.nanoid, scopeDiff.nanoid)
        && Objects.equals(this.prs, scopeDiff.prs)
        && Objects.equals(this.messages, scopeDiff.messages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, oldVersion, newVersion, nanoid, prs, messages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDiff {\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    oldVersion: ").append(toIndentedString(oldVersion)).append("\n");
    sb.append("    newVersion: ").append(toIndentedString(newVersion)).append("\n");
    sb.append("    nanoid: ").append(toIndentedString(nanoid)).append("\n");
    sb.append("    prs: ").append(toIndentedString(prs)).append("\n");
    sb.append("    messages: ").append(toIndentedString(messages)).append("\n");
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
