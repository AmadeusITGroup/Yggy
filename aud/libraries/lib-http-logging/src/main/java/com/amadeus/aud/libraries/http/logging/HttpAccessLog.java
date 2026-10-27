package com.amadeus.aud.libraries.http.logging;

import java.util.Map;

record HttpAccessLog(
    String method,
    int status,
    String uri,
    String proto,
    Map<String, String> requestHeaders,
    Map<String, String> responseHeaders) {
  HttpAccessLog {
    requestHeaders = Map.copyOf(requestHeaders);
    responseHeaders = Map.copyOf(responseHeaders);
  }

  @Override
  public String toString() {
    return "%s %s: %d".formatted(method, uri, status);
  }
}
