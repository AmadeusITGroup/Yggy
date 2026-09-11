package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1RegionSpec */
@JsonPropertyOrder({V1RegionSpec.JSON_PROPERTY_PROVIDER, V1RegionSpec.JSON_PROPERTY_CONTINENT})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1RegionSpec {
  public static final String JSON_PROPERTY_PROVIDER = "provider";
  private String provider;

  public static final String JSON_PROPERTY_CONTINENT = "continent";
  private String continent;

  public V1RegionSpec() {}

  public V1RegionSpec provider(String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Name of the provider.
   *
   * @return provider
   */
  @NotNull
  @Schema(required = true, description = "Name of the provider.")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProvider() {
    return provider;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProvider(String provider) {
    this.provider = provider;
  }

  public V1RegionSpec continent(String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Name of continent.
   *
   * @return continent
   */
  @NotNull
  @Schema(required = true, description = "Name of continent.")
  @JsonProperty(value = JSON_PROPERTY_CONTINENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getContinent() {
    return continent;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTINENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setContinent(String continent) {
    this.continent = continent;
  }

  /** Return true if this v1.RegionSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1RegionSpec v1RegionSpec = (V1RegionSpec) o;
    return Objects.equals(this.provider, v1RegionSpec.provider)
        && Objects.equals(this.continent, v1RegionSpec.continent);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, continent);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1RegionSpec {\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
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
