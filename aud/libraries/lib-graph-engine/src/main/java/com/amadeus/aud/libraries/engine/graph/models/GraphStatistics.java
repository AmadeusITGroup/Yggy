package com.amadeus.aud.libraries.engine.graph.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Pre-computed graph statistics retrieved from the graph engine. Since the graph is immutable,
 * these statistics are computed once at initialization and never change.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GraphStatistics(
    Map<String, Long> scopeNbLink,
    Map<String, Long> scopeNbNode,
    long globalNodesCount,
    long globalLinksCount) {}
