package com.amadeus.aud.libraries.models.core.internals;

import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;

/*
 * Access Rights enum
 * */
public enum AccessRightsPermission {
  COMPONENT_INHERIT(AccessRightsEntry.PermissionEnum.COMPONENT_INHERIT.getValue()),
  COMPONENT_DEPENDENCY_SRC(AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_SRC.getValue()),
  COMPONENT_DEPENDENCY_DST(AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_DST.getValue()),
  EMPTY(AccessRightsEntry.PermissionEnum.EMPTY.getValue());

  private final String value;

  AccessRightsPermission(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static AccessRightsPermission fromValue(String value) {
    for (AccessRightsPermission b : AccessRightsPermission.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    return EMPTY;
  }
}
