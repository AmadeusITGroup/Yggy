package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The status reported to other users - None (not deprecated), Warning (informational, not blocking)
 * or Error (blocking the merge of PR).
 */
@Schema(
    description =
        "The status reported to other users: None (not deprecated), Warning (informational, not blocking), or Error (blocking the merge of a PR).")
public enum DeprecatedStatus {
  NONE("None"),

  WARNING("Warning"),

  ERROR("Error");

  private final String value;

  DeprecatedStatus(String value) {
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
  public static DeprecatedStatus fromValue(String value) {
    for (DeprecatedStatus b : DeprecatedStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}
