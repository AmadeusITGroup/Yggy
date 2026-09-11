package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Connection specification */
@Schema(description = "Connection specification")
@JsonPropertyOrder({
  V1ConnectionSpec.JSON_PROPERTY_SOURCE_ZONE_TYPE,
  V1ConnectionSpec.JSON_PROPERTY_DESTINATION_ZONE_TYPE,
  V1ConnectionSpec.JSON_PROPERTY_CONTROL,
  V1ConnectionSpec.JSON_PROPERTY_LEVEL,
  V1ConnectionSpec.JSON_PROPERTY_TYPE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ConnectionSpec {
  public static final String JSON_PROPERTY_SOURCE_ZONE_TYPE = "sourceZoneType";
  private String sourceZoneType;

  public static final String JSON_PROPERTY_DESTINATION_ZONE_TYPE = "destinationZoneType";
  private String destinationZoneType;

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

  /** The protocol level check that needs to be implemented */
  public enum LevelEnum {
    L7(String.valueOf("L7")),

    L4(String.valueOf("L4"));

    private String value;

    LevelEnum(String value) {
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
    public static LevelEnum fromValue(String value) {
      for (LevelEnum b : LevelEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_LEVEL = "level";
  private LevelEnum level;

  /** Is the connectivity internal or external to the target? */
  public enum TypeEnum {
    INTERNAL(String.valueOf("Internal")),

    EXTERNAL(String.valueOf("External"));

    private String value;

    TypeEnum(String value) {
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
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TYPE = "type";
  private TypeEnum type;

  public V1ConnectionSpec() {}

  public V1ConnectionSpec sourceZoneType(String sourceZoneType) {
    this.sourceZoneType = sourceZoneType;
    return this;
  }

  /**
   * Name of the source zone type
   *
   * @return sourceZoneType
   */
  @NotNull
  @Schema(required = true, description = "Name of the source zone type")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSourceZoneType() {
    return sourceZoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceZoneType(String sourceZoneType) {
    this.sourceZoneType = sourceZoneType;
  }

  public V1ConnectionSpec destinationZoneType(String destinationZoneType) {
    this.destinationZoneType = destinationZoneType;
    return this;
  }

  /**
   * Name of destination Zone Type
   *
   * @return destinationZoneType
   */
  @NotNull
  @Schema(required = true, description = "Name of destination Zone Type")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDestinationZoneType() {
    return destinationZoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDestinationZoneType(String destinationZoneType) {
    this.destinationZoneType = destinationZoneType;
  }

  public V1ConnectionSpec control(ControlEnum control) {
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

  public V1ConnectionSpec level(LevelEnum level) {
    this.level = level;
    return this;
  }

  /**
   * The protocol level check that needs to be implemented
   *
   * @return level
   */
  @NotNull
  @Schema(required = true, description = "The protocol level check that needs to be implemented")
  @JsonProperty(value = JSON_PROPERTY_LEVEL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LevelEnum getLevel() {
    return level;
  }

  @JsonProperty(value = JSON_PROPERTY_LEVEL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLevel(LevelEnum level) {
    this.level = level;
  }

  public V1ConnectionSpec type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Is the connectivity internal or external to the target?
   *
   * @return type
   */
  @NotNull
  @Schema(required = true, description = "Is the connectivity internal or external to the target?")
  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /** Return true if this v1.ConnectionSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ConnectionSpec v1ConnectionSpec = (V1ConnectionSpec) o;
    return Objects.equals(this.sourceZoneType, v1ConnectionSpec.sourceZoneType)
        && Objects.equals(this.destinationZoneType, v1ConnectionSpec.destinationZoneType)
        && Objects.equals(this.control, v1ConnectionSpec.control)
        && Objects.equals(this.level, v1ConnectionSpec.level)
        && Objects.equals(this.type, v1ConnectionSpec.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceZoneType, destinationZoneType, control, level, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ConnectionSpec {\n");
    sb.append("    sourceZoneType: ").append(toIndentedString(sourceZoneType)).append("\n");
    sb.append("    destinationZoneType: ")
        .append(toIndentedString(destinationZoneType))
        .append("\n");
    sb.append("    control: ").append(toIndentedString(control)).append("\n");
    sb.append("    level: ").append(toIndentedString(level)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
