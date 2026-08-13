package com.amadeus.aud.libraries.engine.graph.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Graph(
    LocalDateTime createdAt,
    LocalDateTime deletedAt,
    String version,
    long verticesCount,
    long edgesCount) {}
