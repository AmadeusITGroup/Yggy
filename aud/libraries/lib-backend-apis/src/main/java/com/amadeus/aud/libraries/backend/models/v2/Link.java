package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public sealed interface Link permits JsonPathLink, JsonPathPatternLink, UriLink {
  @Nullable
  @JsonProperty("title")
  String title();

  @JsonProperty("type")
  String type();

  @JsonProperty("display")
  Display display();
}
