package com.amadeus.aud.libraries.engine.data.models.core.internals;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record SingleVersion(
    @JsonProperty("version") String version,
    @JsonProperty("timestamp") OffsetDateTime timestamp,
    @JsonProperty("links") long links) {}
