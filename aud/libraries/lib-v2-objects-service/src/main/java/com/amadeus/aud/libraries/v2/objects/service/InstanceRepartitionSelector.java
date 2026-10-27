package com.amadeus.aud.libraries.v2.objects.service;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.BadRequestException;
import java.util.Locale;

public enum InstanceRepartitionSelector {
  BOTH("both"),
  DIRECT("direct"),
  INDIRECT("indirect");

  private final String queryValue;

  InstanceRepartitionSelector(String queryValue) {
    this.queryValue = queryValue;
  }

  public String queryValue() {
    return queryValue;
  }

  public static InstanceRepartitionSelector fromQueryValue(@Nullable String selector) {
    if (selector == null || selector.isBlank()) {
      return BOTH;
    }

    String normalizedSelector = selector.trim().toLowerCase(Locale.ROOT);
    for (InstanceRepartitionSelector value : values()) {
      if (value.queryValue.equals(normalizedSelector)) {
        return value;
      }
    }

    throw new BadRequestException(
        "Unsupported instance repartition selector: "
            + selector
            + ". Supported selectors: both, direct, indirect");
  }
}
