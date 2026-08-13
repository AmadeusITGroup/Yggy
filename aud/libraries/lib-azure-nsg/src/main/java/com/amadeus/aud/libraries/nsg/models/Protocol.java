package com.amadeus.aud.libraries.nsg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

public enum Protocol {
  TCP("Tcp"),

  UDP("Udp"),

  ICMP("Icmp"),

  STAR("*");

  private final String value;

  Protocol(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static Protocol fromValue(String value) {
    for (Protocol b : Protocol.values()) {
      if (Objects.equals(b.getValue(), value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
