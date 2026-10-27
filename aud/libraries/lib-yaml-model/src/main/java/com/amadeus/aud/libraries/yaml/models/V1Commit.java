package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1Commit */
@JsonPropertyOrder({V1Commit.JSON_PROPERTY_COMMITID, V1Commit.JSON_PROPERTY_MESSAGE})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Commit {
  public static final String JSON_PROPERTY_COMMITID = "commitid";
  @Nullable private String commitid;

  public static final String JSON_PROPERTY_MESSAGE = "message";
  @Nullable private String message;

  public V1Commit() {}

  public V1Commit commitid(@Nullable String commitid) {
    this.commitid = commitid;
    return this;
  }

  /**
   * commit id in the repo
   *
   * @return commitid
   */
  @Schema(description = "commit id in the repo")
  @JsonProperty(value = JSON_PROPERTY_COMMITID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getCommitid() {
    return commitid;
  }

  @JsonProperty(value = JSON_PROPERTY_COMMITID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCommitid(@Nullable String commitid) {
    this.commitid = commitid;
  }

  public V1Commit message(@Nullable String message) {
    this.message = message;
    return this;
  }

  /**
   * associated commit message
   *
   * @return message
   */
  @Schema(description = "associated commit message")
  @JsonProperty(value = JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getMessage() {
    return message;
  }

  @JsonProperty(value = JSON_PROPERTY_MESSAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMessage(@Nullable String message) {
    this.message = message;
  }

  /** Return true if this v1.Commit object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Commit v1Commit = (V1Commit) o;
    return Objects.equals(this.commitid, v1Commit.commitid)
        && Objects.equals(this.message, v1Commit.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commitid, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Commit {\n");
    sb.append("    commitid: ").append(toIndentedString(commitid)).append("\n");
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
