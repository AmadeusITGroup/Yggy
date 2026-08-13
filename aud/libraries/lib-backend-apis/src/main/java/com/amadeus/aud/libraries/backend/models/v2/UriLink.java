package com.amadeus.aud.libraries.backend.models.v2;

public record UriLink(String title, String uri, Display display) implements Link {
  @Override
  public String type() {
    return "uri";
  }
}
