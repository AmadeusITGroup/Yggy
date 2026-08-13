package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScopeVersionDiffItem(
    @JsonProperty("scope") String scope,
    @JsonProperty("change_type") ChangeType changeType,
    @JsonProperty("from_version") @Nullable String fromVersion,
    @JsonProperty("to_version") @Nullable String toVersion,
    @JsonProperty("pull_requests") List<PullRequest> pullRequests,
    @JsonProperty("messages") List<Message> messages) {

  public enum ChangeType {
    ADDED("added"),
    REMOVED("removed"),
    CHANGED("changed");

    private final String value;

    ChangeType(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record PullRequest(
      @JsonProperty("number") String number, @JsonProperty("url") String url) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Message(
      @JsonProperty("message") String message,
      @JsonProperty("timestamp") @Nullable OffsetDateTime timestamp,
      @JsonProperty("url") String url) {}
}
