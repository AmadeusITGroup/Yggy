package com.amadeus.aud.services.apis.models.core.statistics;

import java.util.Map;

public record StatisticsObjects(
    Map<String, Integer> scopeNbLink,
    Map<String, Integer> scopeNbNode,
    int globalNodesCount,
    int globalLinksCount) {}
