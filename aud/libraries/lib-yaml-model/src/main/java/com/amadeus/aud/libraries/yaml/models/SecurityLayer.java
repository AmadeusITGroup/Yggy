package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  SecurityLayer.JSON_PROPERTY_DEVICE_TYPE,
  SecurityLayer.JSON_PROPERTY_DEVICE_NAME
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityLayer extends ObjectMetadata {
  public static final String JSON_PROPERTY_DEVICE_TYPE = "device_type";
  private String deviceType;

  public static final String JSON_PROPERTY_DEVICE_NAME = "device_name";
  private String deviceName;

  public SecurityLayer() {}

  public SecurityLayer deviceType(String deviceType) {
    this.deviceType = deviceType;
    return this;
  }

  /**
   * Get deviceType
   *
   * @return deviceType
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDeviceType() {
    return deviceType;
  }

  @JsonProperty(value = JSON_PROPERTY_DEVICE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public SecurityLayer deviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }

  /**
   * Get deviceName
   *
   * @return deviceName
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDeviceName() {
    return deviceName;
  }

  @JsonProperty(value = JSON_PROPERTY_DEVICE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  @Override
  public SecurityLayer labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this SecurityLayer object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityLayer securityLayer = (SecurityLayer) o;
    return Objects.equals(this.deviceType, securityLayer.deviceType)
        && Objects.equals(this.deviceName, securityLayer.deviceName)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceType, deviceName, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityLayer {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
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
