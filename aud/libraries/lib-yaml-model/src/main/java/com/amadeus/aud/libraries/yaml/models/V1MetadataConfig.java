package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Metadata to identify a resource config. */
@Schema(description = "Metadata to identify a resource config.")
@JsonPropertyOrder({
  V1MetadataConfig.JSON_PROPERTY_CONFIG_HOST,
  V1MetadataConfig.JSON_PROPERTY_CONFIG_HOST_LANDINGZONE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1MetadataConfig extends V1Metadata {
  public static final String JSON_PROPERTY_CONFIG_HOST = "config_host";
  private String configHost;

  public static final String JSON_PROPERTY_CONFIG_HOST_LANDINGZONE = "config_host_landingzone";
  private String configHostLandingzone;

  public V1MetadataConfig() {}

  public V1MetadataConfig configHost(String configHost) {
    this.configHost = configHost;
    return this;
  }

  /**
   * The instance hosting the configuration
   *
   * @return configHost
   */
  @NotNull
  @Schema(required = true, description = "The instance hosting the configuration")
  @JsonProperty(value = JSON_PROPERTY_CONFIG_HOST, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getConfigHost() {
    return configHost;
  }

  @JsonProperty(value = JSON_PROPERTY_CONFIG_HOST, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setConfigHost(String configHost) {
    this.configHost = configHost;
  }

  public V1MetadataConfig configHostLandingzone(String configHostLandingzone) {
    this.configHostLandingzone = configHostLandingzone;
    return this;
  }

  /**
   * The landingzone of the instance hosting the configuration
   *
   * @return configHostLandingzone
   */
  @NotNull
  @Schema(
      required = true,
      description = "The landingzone of the instance hosting the configuration")
  @JsonProperty(value = JSON_PROPERTY_CONFIG_HOST_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getConfigHostLandingzone() {
    return configHostLandingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_CONFIG_HOST_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setConfigHostLandingzone(String configHostLandingzone) {
    this.configHostLandingzone = configHostLandingzone;
  }

  @Override
  public V1MetadataConfig scope(String scope) {
    this.setScope(scope);
    return this;
  }

  @Override
  public V1MetadataConfig name(String name) {
    this.setName(name);
    return this;
  }

  @Override
  public V1MetadataConfig providerSpecific(@Nullable String providerSpecific) {
    this.setProviderSpecific(providerSpecific);
    return this;
  }

  @Override
  public V1MetadataConfig labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  @Override
  public V1MetadataConfig comment(@Nullable String comment) {
    this.setComment(comment);
    return this;
  }

  /** Return true if this v1MetadataConfig object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1MetadataConfig v1MetadataConfig = (V1MetadataConfig) o;
    return Objects.equals(this.configHost, v1MetadataConfig.configHost)
        && Objects.equals(this.configHostLandingzone, v1MetadataConfig.configHostLandingzone)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(configHost, configHostLandingzone, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1MetadataConfig {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    configHost: ").append(toIndentedString(configHost)).append("\n");
    sb.append("    configHostLandingzone: ")
        .append(toIndentedString(configHostLandingzone))
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
