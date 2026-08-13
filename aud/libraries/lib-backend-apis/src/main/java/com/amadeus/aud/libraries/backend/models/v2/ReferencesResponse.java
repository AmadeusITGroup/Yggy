package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({"direct", "inherited"})
public record ReferencesResponse(
    @JsonProperty("direct") Map<String, List<ReferenceEntry>> direct,
    @JsonProperty("inherited") Map<String, List<ReferenceEntry>> inherited) {}
