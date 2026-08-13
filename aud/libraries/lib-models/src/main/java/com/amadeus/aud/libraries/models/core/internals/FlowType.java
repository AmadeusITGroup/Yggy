package com.amadeus.aud.libraries.models.core.internals;

public enum FlowType {
  EGRESS("egress"),
  INGRESS("ingress"),
  INTRA("intra");

  private final String value;

  FlowType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
