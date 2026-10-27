package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProtocolEnum {
  TCP("Tcp"),
  UDP("Udp"),
  ICMP("Icmp"),
  STAR("*");

  private final String value;

  ProtocolEnum(String value) {
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
  public static ProtocolEnum fromValue(String value) {
    for (ProtocolEnum b : ProtocolEnum.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
