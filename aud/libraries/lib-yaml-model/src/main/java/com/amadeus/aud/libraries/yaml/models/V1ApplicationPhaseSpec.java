package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ApplicationPhase specifciation */
@Schema(description = "ApplicationPhase specifciation")
@JsonPropertyOrder({
  V1ApplicationPhaseSpec.JSON_PROPERTY_MACRO_PHASE,
  V1ApplicationPhaseSpec.JSON_PROPERTY_IS_CUSTOMER_FACING
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ApplicationPhaseSpec {
  public static final String JSON_PROPERTY_MACRO_PHASE = "macroPhase";
  @Nullable private String macroPhase;

  public static final String JSON_PROPERTY_IS_CUSTOMER_FACING = "isCustomerFacing";
  private Boolean isCustomerFacing = false;

  public V1ApplicationPhaseSpec() {}

  public V1ApplicationPhaseSpec macroPhase(@Nullable String macroPhase) {
    this.macroPhase = macroPhase;
    return this;
  }

  /**
   * Name of macro Phase
   *
   * @return macroPhase
   */
  @Schema(description = "Name of macro Phase")
  @JsonProperty(value = JSON_PROPERTY_MACRO_PHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getMacroPhase() {
    return macroPhase;
  }

  @JsonProperty(value = JSON_PROPERTY_MACRO_PHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMacroPhase(@Nullable String macroPhase) {
    this.macroPhase = macroPhase;
  }

  public V1ApplicationPhaseSpec isCustomerFacing(Boolean isCustomerFacing) {
    this.isCustomerFacing = isCustomerFacing;
    return this;
  }

  /**
   * is it Customer Facing?
   *
   * @return isCustomerFacing
   */
  @NotNull
  @Schema(required = true, description = "is it Customer Facing?")
  @JsonProperty(value = JSON_PROPERTY_IS_CUSTOMER_FACING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsCustomerFacing() {
    return isCustomerFacing;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_CUSTOMER_FACING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsCustomerFacing(Boolean isCustomerFacing) {
    this.isCustomerFacing = isCustomerFacing;
  }

  /** Return true if this v1.ApplicationPhaseSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ApplicationPhaseSpec v1ApplicationPhaseSpec = (V1ApplicationPhaseSpec) o;
    return Objects.equals(this.macroPhase, v1ApplicationPhaseSpec.macroPhase)
        && Objects.equals(this.isCustomerFacing, v1ApplicationPhaseSpec.isCustomerFacing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(macroPhase, isCustomerFacing);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ApplicationPhaseSpec {\n");
    sb.append("    macroPhase: ").append(toIndentedString(macroPhase)).append("\n");
    sb.append("    isCustomerFacing: ").append(toIndentedString(isCustomerFacing)).append("\n");
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
