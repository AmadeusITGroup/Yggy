package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Display {
  BUTTON("button"),
  TAB("tab"),
  HREF("href");

  private final String value;

  Display(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
