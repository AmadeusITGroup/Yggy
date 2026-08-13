package com.amadeus.aud.libraries.engine.data.utils;

import java.util.Optional;

public final class UrlUtils {
  private static final int SCHEME_SEPARATOR_LENGTH = 3;

  private UrlUtils() {}

  public static Optional<String> extractScheme(String url) {
    int schemeEnd = url.indexOf("://");
    return schemeEnd > 0 ? Optional.of(url.substring(0, schemeEnd)) : Optional.empty();
  }

  public static Optional<String> extractHostname(String url) {
    int schemeEnd = url.indexOf("://");
    if (schemeEnd < 0) {
      return Optional.empty();
    }
    int hostStart = schemeEnd + SCHEME_SEPARATOR_LENGTH;
    int pathStart = url.indexOf('/', hostStart);
    int queryStart = url.indexOf('?', hostStart);
    int fragmentStart = url.indexOf('#', hostStart);

    int authorityEnd = url.length();
    if (pathStart > 0 && pathStart < authorityEnd) {
      authorityEnd = pathStart;
    }
    if (queryStart > 0 && queryStart < authorityEnd) {
      authorityEnd = queryStart;
    }
    if (fragmentStart > 0 && fragmentStart < authorityEnd) {
      authorityEnd = fragmentStart;
    }

    String authority = url.substring(hostStart, authorityEnd);
    int atIndex = authority.lastIndexOf('@');
    if (atIndex >= 0) {
      authority = authority.substring(atIndex + 1);
    }
    int portIndex = authority.indexOf(':');
    return Optional.of(portIndex >= 0 ? authority.substring(0, portIndex) : authority);
  }
}
