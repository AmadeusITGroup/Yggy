package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** MacroPhase specification */
@Schema(description = "MacroPhase specification")
@JsonPropertyOrder({V1MacroPhaseSpec.JSON_PROPERTY_AUTOMATION_ORDER})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1MacroPhaseSpec {
  public static final String JSON_PROPERTY_AUTOMATION_ORDER = "automation_order";
  @Nullable private Integer automationOrder;

  public V1MacroPhaseSpec() {}

  public V1MacroPhaseSpec automationOrder(@Nullable Integer automationOrder) {
    this.automationOrder = automationOrder;
    return this;
  }

  /**
   * Automation order in ascending order
   *
   * @return automationOrder
   */
  @Schema(required = false, description = "Automation order in ascending order")
  @JsonProperty(value = JSON_PROPERTY_AUTOMATION_ORDER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Integer getAutomationOrder() {
    return automationOrder;
  }

  @JsonProperty(value = JSON_PROPERTY_AUTOMATION_ORDER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAutomationOrder(@Nullable Integer automationOrder) {
    this.automationOrder = automationOrder;
  }

  /** Return true if this v1.MacroPhaseSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1MacroPhaseSpec v1MacroPhaseSpec = (V1MacroPhaseSpec) o;
    return Objects.equals(this.automationOrder, v1MacroPhaseSpec.automationOrder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(automationOrder);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1MacroPhaseSpec {\n");
    sb.append("    automationOrder: ").append(toIndentedString(automationOrder)).append("\n");
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
