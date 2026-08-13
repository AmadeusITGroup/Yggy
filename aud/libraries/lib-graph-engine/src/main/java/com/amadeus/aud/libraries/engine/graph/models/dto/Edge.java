package com.amadeus.aud.libraries.engine.graph.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Edge(
    String name,
    String dependencyInstanceId,
    String serviceId,
    Boolean governanceActive,
    String sourceNodeId,
    String targetNodeId) {

  // Legacy JavaBean-style getters retained for backward compatibility
  public String getName() {
    return name;
  }

  public String getDependencyInstanceId() {
    return dependencyInstanceId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public String getSourceNodeId() {
    return sourceNodeId;
  }

  public String getTargetNodeId() {
    return targetNodeId;
  }
}
