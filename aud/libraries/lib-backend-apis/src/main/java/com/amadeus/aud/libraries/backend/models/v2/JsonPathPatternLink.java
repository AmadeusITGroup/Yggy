package com.amadeus.aud.libraries.backend.models.v2;

public record JsonPathPatternLink(String title, String path, String replacement, Display display)
    implements Link {
  public JsonPathPatternLink(String path, String replacement, Display display) {
    this(null, path, replacement, display);
  }

  @Override
  public String type() {
    return "jsonpathpattern";
  }
}
