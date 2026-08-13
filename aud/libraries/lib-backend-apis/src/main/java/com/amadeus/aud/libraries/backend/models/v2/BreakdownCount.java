package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BreakdownCount(@JsonProperty("name") String name, @JsonProperty("count") int count) {}
