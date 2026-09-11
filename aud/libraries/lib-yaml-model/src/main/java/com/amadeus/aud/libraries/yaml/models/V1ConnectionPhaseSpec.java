package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Connection Phase specification */
@Schema(description = "Connection Phase specification")
@JsonPropertyOrder({
  V1ConnectionPhaseSpec.JSON_PROPERTY_DESTINATION_PHASE,
  V1ConnectionPhaseSpec.JSON_PROPERTY_CONTROL,
  V1ConnectionPhaseSpec.JSON_PROPERTY_SOURCE_PHASE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ConnectionPhaseSpec {
  public static final String JSON_PROPERTY_DESTINATION_PHASE = "destinationPhase";
  private String destinationPhase;

  /** The value of the control, if permitted */
  public enum ControlEnum {
    GREEN(String.valueOf("green")),

    AMBER(String.valueOf("amber")),

    RED(String.valueOf("red"));

    private String value;

    ControlEnum(String value) {
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
    public static ControlEnum fromValue(String value) {
      for (ControlEnum b : ControlEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_CONTROL = "control";
  private ControlEnum control;

  public static final String JSON_PROPERTY_SOURCE_PHASE = "sourcePhase";
  private String sourcePhase;

  public V1ConnectionPhaseSpec() {}

  public V1ConnectionPhaseSpec destinationPhase(String destinationPhase) {
    this.destinationPhase = destinationPhase;
    return this;
  }

  /**
   * Name of the destination macro-phase
   *
   * @return destinationPhase
   */
  @NotNull
  @Schema(required = true, description = "Name of the destination macro-phase")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDestinationPhase() {
    return destinationPhase;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDestinationPhase(String destinationPhase) {
    this.destinationPhase = destinationPhase;
  }

  public V1ConnectionPhaseSpec control(ControlEnum control) {
    this.control = control;
    return this;
  }

  /**
   * The value of the control, if permitted
   *
   * @return control
   */
  @NotNull
  @Schema(required = true, description = "The value of the control, if permitted")
  @JsonProperty(value = JSON_PROPERTY_CONTROL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ControlEnum getControl() {
    return control;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTROL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setControl(ControlEnum control) {
    this.control = control;
  }

  public V1ConnectionPhaseSpec sourcePhase(String sourcePhase) {
    this.sourcePhase = sourcePhase;
    return this;
  }

  /**
   * Name of the source macro-phase
   *
   * @return sourcePhase
   */
  @NotNull
  @Schema(required = true, description = "Name of the source macro-phase")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSourcePhase() {
    return sourcePhase;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourcePhase(String sourcePhase) {
    this.sourcePhase = sourcePhase;
  }

  /** Return true if this v1.ConnectionPhaseSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ConnectionPhaseSpec v1ConnectionPhaseSpec = (V1ConnectionPhaseSpec) o;
    return Objects.equals(this.destinationPhase, v1ConnectionPhaseSpec.destinationPhase)
        && Objects.equals(this.control, v1ConnectionPhaseSpec.control)
        && Objects.equals(this.sourcePhase, v1ConnectionPhaseSpec.sourcePhase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(destinationPhase, control, sourcePhase);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ConnectionPhaseSpec {\n");
    sb.append("    destinationPhase: ").append(toIndentedString(destinationPhase)).append("\n");
    sb.append("    control: ").append(toIndentedString(control)).append("\n");
    sb.append("    sourcePhase: ").append(toIndentedString(sourcePhase)).append("\n");
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
