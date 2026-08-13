package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecurityLayerResult(
    @JsonProperty("device_type") String deviceType,
    @JsonProperty("device_name") String deviceName,
    @JsonProperty("has_additions") boolean hasAdditions,
    @JsonProperty("has_deletions") boolean hasDeletions) {

  public static final String ACTION_ADDED = "added";
  public static final String ACTION_DELETED = "deleted";

  public static SecurityLayerResult withAction(
      String deviceType, String deviceName, String action) {
    boolean added = ACTION_ADDED.equalsIgnoreCase(action);
    boolean deleted = ACTION_DELETED.equalsIgnoreCase(action);
    return new SecurityLayerResult(deviceType, deviceName, added, deleted);
  }

  public SecurityLayerResult merge(SecurityLayerResult other) {
    return new SecurityLayerResult(
        deviceType,
        deviceName,
        this.hasAdditions || other.hasAdditions,
        this.hasDeletions || other.hasDeletions);
  }

  public String key() {
    return deviceType + ":" + deviceName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SecurityLayerResult that)) {
      return false;
    }
    return Objects.equals(deviceType, that.deviceType)
        && Objects.equals(deviceName, that.deviceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceType, deviceName);
  }
}
