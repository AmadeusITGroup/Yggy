package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"data", "metadata"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(
    @JsonProperty("data") T data, @JsonProperty("metadata") Metadata metadata) {}
