package com.amadeus.aud.libraries.engine.graph.clients;

import java.util.List;

public class EdgesByNodeIdsRequest {

  private List<String> sourceNodeIds;
  private List<String> destinationNodeIds;

  public EdgesByNodeIdsRequest() {}

  public EdgesByNodeIdsRequest(List<String> sourceNodeIds, List<String> destinationNodeIds) {
    this.sourceNodeIds = sourceNodeIds;
    this.destinationNodeIds = destinationNodeIds;
  }

  public List<String> getSourceNodeIds() {
    return sourceNodeIds;
  }

  public void setSourceNodeIds(List<String> sourceNodeIds) {
    this.sourceNodeIds = sourceNodeIds;
  }

  public List<String> getDestinationNodeIds() {
    return destinationNodeIds;
  }

  public void setDestinationNodeIds(List<String> destinationNodeIds) {
    this.destinationNodeIds = destinationNodeIds;
  }
}
