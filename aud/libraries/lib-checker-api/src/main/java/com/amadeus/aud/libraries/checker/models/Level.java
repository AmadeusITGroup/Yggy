package com.amadeus.aud.libraries.checker.models;

public enum Level {
  ERROR("ERROR"),
  WARNING("WARNING"),
  INFO("INFO"),
  CONTEXT("CONTEXT");

  private final CharSequence value;

  Level(CharSequence string) {
    this.value = string;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
