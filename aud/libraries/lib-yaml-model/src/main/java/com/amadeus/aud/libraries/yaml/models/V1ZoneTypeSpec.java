package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ZoneType specification */
@Schema(description = "ZoneType specification")
@JsonPropertyOrder({
  V1ZoneTypeSpec.JSON_PROPERTY_IS_TECHNICAL,
  V1ZoneTypeSpec.JSON_PROPERTY_IS_PAAS,
  V1ZoneTypeSpec.JSON_PROPERTY_IS_REAL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ZoneTypeSpec {
  public static final String JSON_PROPERTY_IS_TECHNICAL = "isTechnical";
  private Boolean isTechnical = false;

  public static final String JSON_PROPERTY_IS_PAAS = "isPaas";
  @Nullable private Boolean isPaas = false;

  public static final String JSON_PROPERTY_IS_REAL = "isReal";
  @Nullable private Boolean isReal = false;

  public V1ZoneTypeSpec() {}

  public V1ZoneTypeSpec isTechnical(Boolean isTechnical) {
    this.isTechnical = isTechnical;
    return this;
  }

  /**
   * is this part of control or data plane?
   *
   * @return isTechnical
   */
  @NotNull
  @Schema(required = true, description = "is this part of control or data plane?")
  @JsonProperty(value = JSON_PROPERTY_IS_TECHNICAL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsTechnical() {
    return isTechnical;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TECHNICAL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsTechnical(Boolean isTechnical) {
    this.isTechnical = isTechnical;
  }

  public V1ZoneTypeSpec isPaas(@Nullable Boolean isPaas) {
    this.isPaas = isPaas;
    return this;
  }

  /**
   * is this used for ACS?
   *
   * @return isPaas
   */
  @Schema(required = false, description = "is this used for ACS?")
  @JsonProperty(value = JSON_PROPERTY_IS_PAAS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPaas() {
    return isPaas;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PAAS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPaas(@Nullable Boolean isPaas) {
    this.isPaas = isPaas;
  }

  public V1ZoneTypeSpec isReal(@Nullable Boolean isReal) {
    this.isReal = isReal;
    return this;
  }

  /**
   * does it contain an actual Amadeus workload?
   *
   * @return isReal
   */
  @Schema(required = false, description = "does it contain an actual Amadeus workload?")
  @JsonProperty(value = JSON_PROPERTY_IS_REAL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsReal() {
    return isReal;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_REAL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsReal(@Nullable Boolean isReal) {
    this.isReal = isReal;
  }

  /** Return true if this v1.ZoneTypeSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ZoneTypeSpec v1ZoneTypeSpec = (V1ZoneTypeSpec) o;
    return Objects.equals(this.isTechnical, v1ZoneTypeSpec.isTechnical)
        && Objects.equals(this.isPaas, v1ZoneTypeSpec.isPaas)
        && Objects.equals(this.isReal, v1ZoneTypeSpec.isReal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isTechnical, isPaas, isReal);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ZoneTypeSpec {\n");
    sb.append("    isTechnical: ").append(toIndentedString(isTechnical)).append("\n");
    sb.append("    isPaas: ").append(toIndentedString(isPaas)).append("\n");
    sb.append("    isReal: ").append(toIndentedString(isReal)).append("\n");
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
