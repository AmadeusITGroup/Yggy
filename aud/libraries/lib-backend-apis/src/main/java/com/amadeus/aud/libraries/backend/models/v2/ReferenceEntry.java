package com.amadeus.aud.libraries.backend.models.v2;

import com.amadeus.aud.libraries.common.WithId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReferenceEntry(
    @JsonProperty("id") String id,
    @JsonProperty("name") @Nullable String name,
    @JsonProperty("scope") @Nullable String scope,
    @JsonProperty("description") @Nullable String description,
    @JsonProperty("isInterface") @Nullable Boolean isInterface,
    @JsonProperty("fromParent") @Nullable String fromParent)
    implements WithId {

  /** Convenience constructor without {@code isInterface} — sets it to {@code null}. */
  public ReferenceEntry(
      String id, @Nullable String name, @Nullable String scope, @Nullable String description) {
    this(id, name, scope, description, null, null);
  }
}
