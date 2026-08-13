package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({GraphResponse.JSON_PROPERTY_NODES, GraphResponse.JSON_PROPERTY_EDGES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GraphResponse extends BaseResponse {

  public static final String JSON_PROPERTY_NODES = "nodes";
  public static final String JSON_PROPERTY_EDGES = "edges";

  @JsonProperty(JSON_PROPERTY_NODES)
  private List<String> nodesIds;

  @JsonProperty(JSON_PROPERTY_EDGES)
  private List<String> edgesIds;

  @JsonCreator
  public GraphResponse() {}

  public GraphResponse(
      String url,
      String urlVersion,
      String version,
      String apiVersion,
      List<String> nodesIds,
      List<String> edgesIds) {
    super(url, urlVersion, version, apiVersion);

    this.nodesIds = nodesIds;
    this.edgesIds = edgesIds;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return (o instanceof GraphResponse that)
        && nodesIds.equals(that.nodesIds)
        && edgesIds.equals(that.edgesIds);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), nodesIds, edgesIds);
    }
    return hashcode;
  }

  public List<String> getNodesIds() {
    return nodesIds;
  }

  public void setNodesIds(List<String> nodesIds) {
    this.nodesIds = nodesIds;
  }

  public List<String> getEdgesIds() {
    return edgesIds;
  }

  public void setEdgesIds(List<String> edgesIds) {
    this.edgesIds = edgesIds;
  }
}
