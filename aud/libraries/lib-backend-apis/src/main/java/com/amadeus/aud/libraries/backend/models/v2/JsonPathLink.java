package com.amadeus.aud.libraries.backend.models.v2;

public record JsonPathLink(String title, String jsonPath, String uri, Display display)
    implements Link {
  public JsonPathLink(String jsonPath, String uri, Display display) {
    this(null, jsonPath, uri, display);
  }

  @Override
  public String type() {
    return "jsonpath";
  }
}
