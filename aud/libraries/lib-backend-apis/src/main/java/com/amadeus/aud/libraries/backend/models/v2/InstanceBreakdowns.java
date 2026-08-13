package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record InstanceBreakdowns(
    @JsonProperty("continent") List<BreakdownCount> continent,
    @JsonProperty("landingZone") List<BreakdownCount> landingZone,
    @JsonProperty("macrophase") List<BreakdownCount> macrophase,
    @JsonProperty("namespace") List<BreakdownCount> namespace,
    @JsonProperty("paas") List<BreakdownCount> paas,
    @JsonProperty("securityZone") List<BreakdownCount> securityZone,
    @JsonProperty("target") List<BreakdownCount> target) {}
