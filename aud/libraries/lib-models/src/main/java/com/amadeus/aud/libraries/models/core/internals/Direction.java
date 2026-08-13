package com.amadeus.aud.libraries.models.core.internals;

public enum Direction {
  IN("IN"),
  OUT("OUT"),
  D_IN("D_IN"),
  D_OUT("D_OUT"),
  D_INOUT("D_INOUT"),
  NONE(null);

  private final String value;

  Direction(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public boolean isOutDirection() {
    return this == OUT || this == D_OUT || this == D_INOUT;
  }
}
