package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1Device */
@JsonPropertyOrder({V1Device.JSON_PROPERTY_DEVICE_TYPE, V1Device.JSON_PROPERTY_NAME})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Device {
  public static final String JSON_PROPERTY_DEVICE_TYPE = "device_type";
  @Nullable private String deviceType;

  public static final String JSON_PROPERTY_NAME = "name";
  @Nullable private String name;

  public V1Device() {}

  public V1Device deviceType(@Nullable String deviceType) {
    this.deviceType = deviceType;
    return this;
  }

  /**
   * type of the device
   *
   * @return deviceType
   */
  @Schema(description = "type of the device")
  @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeviceType() {
    return deviceType;
  }

  @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeviceType(@Nullable String deviceType) {
    this.deviceType = deviceType;
  }

  public V1Device name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * name of the device
   *
   * @return name
   */
  @Schema(description = "name of the device")
  @JsonProperty(value = JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setName(@Nullable String name) {
    this.name = name;
  }

  /** Return true if this v1.Device object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Device v1Device = (V1Device) o;
    return Objects.equals(this.deviceType, v1Device.deviceType)
        && Objects.equals(this.name, v1Device.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceType, name);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Device {\n");
    sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
