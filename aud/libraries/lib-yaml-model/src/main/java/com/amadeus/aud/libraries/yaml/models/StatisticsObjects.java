package com.amadeus.aud.libraries.yaml.models;

import java.util.Map;

public record StatisticsObjects(
    Map<String, Integer> scopeNbLink,
    Map<String, Integer> scopeNbNode,
    int globalNodesCount,
    int globalLinksCount) {}
