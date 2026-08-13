package com.amadeus.aud.libraries.engine.graph.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Node(
    String componentInstanceId, String landingZoneName, String platformName, String targetName) {}
