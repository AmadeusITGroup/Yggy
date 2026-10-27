package com.amadeus.aud.libraries.v2.objects.service;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.BadRequestException;
import java.util.Locale;

public enum ConnectivityDetailDirection {
  BOTH("both"),
  IN("in"),
  OUT("out");

  private final String queryValue;

  ConnectivityDetailDirection(String queryValue) {
    this.queryValue = queryValue;
  }

  public String queryValue() {
    return queryValue;
  }

  public static ConnectivityDetailDirection fromQueryValue(@Nullable String direction) {
    if (direction == null || direction.isBlank()) {
      return BOTH;
    }

    String normalizedDirection = direction.trim().toLowerCase(Locale.ROOT);
    for (ConnectivityDetailDirection value : values()) {
      if (value.queryValue.equals(normalizedDirection)) {
        return value;
      }
    }

    throw new BadRequestException(
        "Unsupported connectivity direction: "
            + direction
            + ". Supported directions: both, in, out");
  }
}
