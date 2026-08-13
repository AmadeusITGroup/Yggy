package com.amadeus.aud.services.engine.resources.models;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    name = "GraphInitStatusResponse",
    description = "Graph initialization status payload for trigger and polling endpoints")
public class GraphInitStatusResponse {

  @Schema(description = "Requested version identifier")
  private String version;

  @Schema(description = "Initialization status")
  private GraphLoadStatus status;

  @Schema(description = "Optional human-readable status message")
  private String message;

  public GraphInitStatusResponse() {}

  public GraphInitStatusResponse(String version, GraphLoadStatus status, String message) {
    this.version = version;
    this.status = status;
    this.message = message;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public GraphLoadStatus getStatus() {
    return status;
  }

  public void setStatus(GraphLoadStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
