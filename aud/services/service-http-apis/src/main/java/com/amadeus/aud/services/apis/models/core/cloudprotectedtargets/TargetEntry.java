package com.amadeus.aud.services.apis.models.core.cloudprotectedtargets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record TargetEntry(
    String name, Set<Map<String, String>> firewallgroups, List<String> cidrs) {}
