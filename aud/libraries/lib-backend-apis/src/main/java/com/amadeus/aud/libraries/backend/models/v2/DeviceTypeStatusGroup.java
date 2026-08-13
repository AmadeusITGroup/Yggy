package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Groups devices by type, then by AUD version. Used for the global scope status to avoid redundancy
 * of device types.
 */
public record DeviceTypeStatusGroup(
    @JsonProperty("device_type") String deviceType,
    @JsonProperty("versions") List<VersionGroup> versions) {

  public record VersionGroup(
      @JsonProperty("aud_version") String audVersion,
      @JsonProperty("device_names") List<String> deviceNames) {}
}
