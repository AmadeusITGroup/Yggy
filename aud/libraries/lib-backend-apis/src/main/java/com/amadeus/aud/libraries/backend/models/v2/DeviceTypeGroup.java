package com.amadeus.aud.libraries.backend.models.v2;

import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record DeviceTypeGroup(
    @JsonProperty("device_type") String deviceType,
    @JsonProperty("device_names") List<String> deviceNames) {

  /**
   * Groups a flat list of {@link SecurityLayerResult} by device type, sorting names
   * case-insensitively within each group. Device types are ordered alphabetically.
   */
  public static List<DeviceTypeGroup> fromSecurityLayers(List<SecurityLayerResult> layers) {
    Map<String, List<String>> grouped = new TreeMap<>();
    for (SecurityLayerResult r : layers) {
      grouped.computeIfAbsent(r.deviceType(), k -> new ArrayList<>()).add(r.deviceName());
    }
    return toGroups(grouped);
  }

  /**
   * Groups a raw device list (device_type + name) by device type, filtering out entries without a
   * name, sorting names case-insensitively. Null input returns null.
   */
  public static @Nullable List<DeviceTypeGroup> fromV1Devices(
      @Nullable List<? extends V1Device> devices) {
    if (devices == null) {
      return null;
    }
    Map<String, List<String>> grouped = new TreeMap<>();
    for (V1Device d : devices) {
      if (d != null && d.getName() != null) {
        grouped.computeIfAbsent(d.getDeviceType(), k -> new ArrayList<>()).add(d.getName());
      }
    }
    return toGroups(grouped);
  }

  private static List<DeviceTypeGroup> toGroups(Map<String, List<String>> grouped) {
    List<DeviceTypeGroup> result = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
      List<String> names = new ArrayList<>(entry.getValue());
      names.sort(String.CASE_INSENSITIVE_ORDER);
      result.add(new DeviceTypeGroup(entry.getKey(), names));
    }
    return result;
  }
}
