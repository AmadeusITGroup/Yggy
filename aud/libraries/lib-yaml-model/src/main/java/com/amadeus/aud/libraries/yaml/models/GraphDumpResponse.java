package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({GraphDumpResponse.JSON_PROPERTY_NODES, GraphDumpResponse.JSON_PROPERTY_EDGES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GraphDumpResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_NODES = "nodes";
  private List<String> nodes;

  public static final String JSON_PROPERTY_EDGES = "edges";
  private List<String> edges;

  public GraphDumpResponse() {}

  public GraphDumpResponse nodes(List<String> nodes) {
    this.nodes = nodes;
    return this;
  }

  public GraphDumpResponse addNodesItem(String nodesItem) {
    if (this.nodes == null) {
      this.nodes = new ArrayList<>();
    }
    this.nodes.add(nodesItem);
    return this;
  }

  /**
   * Get nodes
   *
   * @return nodes
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NODES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getNodes() {
    return nodes;
  }

  @JsonProperty(value = JSON_PROPERTY_NODES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNodes(List<String> nodes) {
    this.nodes = nodes;
  }

  public GraphDumpResponse edges(List<String> edges) {
    this.edges = edges;
    return this;
  }

  public GraphDumpResponse addEdgesItem(String edgesItem) {
    if (this.edges == null) {
      this.edges = new ArrayList<>();
    }
    this.edges.add(edgesItem);
    return this;
  }

  /**
   * Get edges
   *
   * @return edges
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_EDGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getEdges() {
    return edges;
  }

  @JsonProperty(value = JSON_PROPERTY_EDGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEdges(List<String> edges) {
    this.edges = edges;
  }

  @Override
  public GraphDumpResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public GraphDumpResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public GraphDumpResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public GraphDumpResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this GraphDumpResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphDumpResponse graphDumpResponse = (GraphDumpResponse) o;
    return Objects.equals(this.nodes, graphDumpResponse.nodes)
        && Objects.equals(this.edges, graphDumpResponse.edges)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodes, edges, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphDumpResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
    sb.append("    edges: ").append(toIndentedString(edges)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
