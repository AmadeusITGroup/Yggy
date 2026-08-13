package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InstanceRepartition(
    @JsonProperty("total") int total, @JsonProperty("breakdowns") InstanceBreakdowns breakdowns) {}
