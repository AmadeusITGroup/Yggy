package com.amadeus.aud.services.apis.models.http.responses;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.amadeus.aud.libraries.yaml.models.Version;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({
  VersionsResponse.JSON_PROPERTY_SIZES,
  VersionsResponse.JSON_PROPERTY_MODEL_VERSIONS,
  VersionsResponse.JSON_PROPERTY_GRAPH_VERSIONS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VersionsResponse extends BaseResponse {

  public static final String JSON_PROPERTY_SIZES = "count";
  public static final String JSON_PROPERTY_MODEL_VERSIONS = "model_versions";
  public static final String JSON_PROPERTY_GRAPH_VERSIONS = "graph_versions";

  @JsonProperty(JSON_PROPERTY_MODEL_VERSIONS)
  private final List<Version> modelVersions;

  @JsonProperty(JSON_PROPERTY_GRAPH_VERSIONS)
  private final List<Version> graphVersions;

  public VersionsResponse(
      String url, String apiVersion, List<Version> modelVersions, List<Version> graphVersions) {
    super(url, null, null, apiVersion);
    this.modelVersions = modelVersions;
    this.graphVersions = graphVersions;
  }

  @JsonProperty(JSON_PROPERTY_SIZES)
  public Map<String, Integer> getSizes() {
    return Map.of(
        JSON_PROPERTY_MODEL_VERSIONS, modelVersions != null ? modelVersions.size() : 0,
        JSON_PROPERTY_GRAPH_VERSIONS, graphVersions != null ? graphVersions.size() : 0);
  }
}
