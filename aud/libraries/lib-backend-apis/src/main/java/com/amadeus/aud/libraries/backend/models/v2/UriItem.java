package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;

@JsonPropertyOrder({"title", "uri", "category"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UriItem(
    @JsonProperty("title") String title,
    @JsonProperty("uri") String uri,
    @JsonProperty("category") String category,
    @JsonProperty("graphqlQueryName") @Nullable String graphqlQueryName) {}
