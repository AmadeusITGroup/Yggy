package com.amadeus.aud.libraries.models.core.internals;

import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;

public enum AccessRightsDenialSeverity {
  ERROR(AccessDenialMessage.UserMessageSeverityEnum.ERROR.getValue()),
  WARNING(AccessDenialMessage.UserMessageSeverityEnum.WARNING.getValue()),
  EMPTY(AccessDenialMessage.UserMessageSeverityEnum.EMPTY.getValue());

  private final String value;

  AccessRightsDenialSeverity(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static AccessRightsDenialSeverity fromValue(String value) {
    for (AccessRightsDenialSeverity b : AccessRightsDenialSeverity.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    return EMPTY;
  }
}
