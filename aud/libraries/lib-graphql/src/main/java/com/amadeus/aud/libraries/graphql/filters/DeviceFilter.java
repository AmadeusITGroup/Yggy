package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(DeviceFilter.NAME)
public class DeviceFilter {
  public static final String NAME = "DeviceFilter";

  private @Nullable StringFilter name;
  private @Nullable StringFilter deviceType;

  public @Nullable StringFilter getName() {
    return name;
  }

  public void setName(@Nullable StringFilter name) {
    this.name = name;
  }

  public @Nullable StringFilter getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(@Nullable StringFilter deviceType) {
    this.deviceType = deviceType;
  }

  public boolean matches(@Nullable V1Device device) {
    return device != null
        && (name == null || name.matches(device.getName()))
        && (deviceType == null || deviceType.matches(device.getDeviceType()));
  }
}
