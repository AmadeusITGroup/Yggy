package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** IPAddressInstance specification */
@Schema(description = "IPAddressInstance specification")
@JsonPropertyOrder({
  V1IPAddressInstanceSpec.JSON_PROPERTY_HOST,
  V1IPAddressInstanceSpec.JSON_PROPERTY_ADVERTISED_TO,
  V1IPAddressInstanceSpec.JSON_PROPERTY_COMPONENT_INSTANCE,
  V1IPAddressInstanceSpec.JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1IPAddressInstanceSpec {
  public static final String JSON_PROPERTY_HOST = "host";
  @Nullable private String host;

  /**
   * For POP IPs located in cbz or dmz, Possibility to make them public toward Internet or Extranet
   */
  public enum AdvertisedToEnum {
    INTERNET(String.valueOf("internet")),

    EXTRANET(String.valueOf("extranet"));

    private String value;

    AdvertisedToEnum(String value) {
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
    public static AdvertisedToEnum fromValue(String value) {
      for (AdvertisedToEnum b : AdvertisedToEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ADVERTISED_TO = "advertised_to";
  @Nullable private AdvertisedToEnum advertisedTo;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCE = "componentInstance";
  private String componentInstance;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE =
      "componentInstanceLandingZone";
  private String componentInstanceLandingZone;

  public V1IPAddressInstanceSpec() {}

  public V1IPAddressInstanceSpec host(@Nullable String host) {
    this.host = host;
    return this;
  }

  /**
   * This is not a FQDN, nor is it known to the DNS. This field is informational only.
   *
   * @return host
   */
  @Schema(
      required = false,
      description =
          "This is not a FQDN, nor is it known to the DNS. This field is informational only.")
  @JsonProperty(value = JSON_PROPERTY_HOST, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getHost() {
    return host;
  }

  @JsonProperty(value = JSON_PROPERTY_HOST, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHost(@Nullable String host) {
    this.host = host;
  }

  public V1IPAddressInstanceSpec advertisedTo(@Nullable AdvertisedToEnum advertisedTo) {
    this.advertisedTo = advertisedTo;
    return this;
  }

  /**
   * For POP IPs located in cbz or dmz, Possibility to make them public toward Internet or Extranet
   *
   * @return advertisedTo
   */
  @Schema(
      required = false,
      description =
          "For POP IPs located in cbz or dmz, Possibility to make them public toward Internet or Extranet")
  @JsonProperty(value = JSON_PROPERTY_ADVERTISED_TO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable AdvertisedToEnum getAdvertisedTo() {
    return advertisedTo;
  }

  @JsonProperty(value = JSON_PROPERTY_ADVERTISED_TO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAdvertisedTo(@Nullable AdvertisedToEnum advertisedTo) {
    this.advertisedTo = advertisedTo;
  }

  public V1IPAddressInstanceSpec componentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Name of the component Instance
   *
   * @return componentInstance
   */
  @NotNull
  @Schema(required = true, description = "Name of the component Instance")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponentInstance() {
    return componentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
  }

  public V1IPAddressInstanceSpec componentInstanceLandingZone(String componentInstanceLandingZone) {
    this.componentInstanceLandingZone = componentInstanceLandingZone;
    return this;
  }

  /**
   * Name of the component Instance LandingZone (for future use)
   *
   * @return componentInstanceLandingZone
   */
  @NotNull
  @Schema(
      required = true,
      description = "Name of the component Instance LandingZone (for future use)")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponentInstanceLandingZone() {
    return componentInstanceLandingZone;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponentInstanceLandingZone(String componentInstanceLandingZone) {
    this.componentInstanceLandingZone = componentInstanceLandingZone;
  }

  /** Return true if this v1.IPAddressInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1IPAddressInstanceSpec v1IPAddressInstanceSpec = (V1IPAddressInstanceSpec) o;
    return Objects.equals(this.host, v1IPAddressInstanceSpec.host)
        && Objects.equals(this.advertisedTo, v1IPAddressInstanceSpec.advertisedTo)
        && Objects.equals(this.componentInstance, v1IPAddressInstanceSpec.componentInstance)
        && Objects.equals(
            this.componentInstanceLandingZone,
            v1IPAddressInstanceSpec.componentInstanceLandingZone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, advertisedTo, componentInstance, componentInstanceLandingZone);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1IPAddressInstanceSpec {\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    advertisedTo: ").append(toIndentedString(advertisedTo)).append("\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
    sb.append("    componentInstanceLandingZone: ")
        .append(toIndentedString(componentInstanceLandingZone))
        .append("\n");
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
