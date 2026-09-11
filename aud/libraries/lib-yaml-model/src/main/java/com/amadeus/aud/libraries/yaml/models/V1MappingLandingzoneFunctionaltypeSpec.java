package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** MappingLandingzoneFunctionaltype specification */
@Schema(description = "MappingLandingzoneFunctionaltype specification")
@JsonPropertyOrder({
  V1MappingLandingzoneFunctionaltypeSpec.JSON_PROPERTY_LANDINGZONE,
  V1MappingLandingzoneFunctionaltypeSpec.JSON_PROPERTY_FUNCTIONALTYPE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1MappingLandingzoneFunctionaltypeSpec {
  public static final String JSON_PROPERTY_LANDINGZONE = "landingzone";
  private String landingzone;

  public static final String JSON_PROPERTY_FUNCTIONALTYPE = "functionaltype";
  private String functionaltype;

  public V1MappingLandingzoneFunctionaltypeSpec() {}

  public V1MappingLandingzoneFunctionaltypeSpec landingzone(String landingzone) {
    this.landingzone = landingzone;
    return this;
  }

  /**
   * Name of the landingzone
   *
   * @return landingzone
   */
  @NotNull
  @Schema(required = true, description = "Name of the landingzone")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzone() {
    return landingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzone(String landingzone) {
    this.landingzone = landingzone;
  }

  public V1MappingLandingzoneFunctionaltypeSpec functionaltype(String functionaltype) {
    this.functionaltype = functionaltype;
    return this;
  }

  /**
   * Name of the functional type
   *
   * @return functionaltype
   */
  @NotNull
  @Schema(required = true, description = "Name of the functional type")
  @JsonProperty(value = JSON_PROPERTY_FUNCTIONALTYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getFunctionaltype() {
    return functionaltype;
  }

  @JsonProperty(value = JSON_PROPERTY_FUNCTIONALTYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFunctionaltype(String functionaltype) {
    this.functionaltype = functionaltype;
  }

  /** Return true if this v1.MappingLandingzoneFunctionaltypeSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1MappingLandingzoneFunctionaltypeSpec v1MappingLandingzoneFunctionaltypeSpec =
        (V1MappingLandingzoneFunctionaltypeSpec) o;
    return Objects.equals(this.landingzone, v1MappingLandingzoneFunctionaltypeSpec.landingzone)
        && Objects.equals(
            this.functionaltype, v1MappingLandingzoneFunctionaltypeSpec.functionaltype);
  }

  @Override
  public int hashCode() {
    return Objects.hash(landingzone, functionaltype);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1MappingLandingzoneFunctionaltypeSpec {\n");
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    functionaltype: ").append(toIndentedString(functionaltype)).append("\n");
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
