package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Dependency specification */
@Schema(description = "Dependency specification")
@JsonPropertyOrder({
  V1DependencySpec.JSON_PROPERTY_COMPONENT,
  V1DependencySpec.JSON_PROPERTY_TARGET_COMPONENT,
  V1DependencySpec.JSON_PROPERTY_SECURITY_JUSTIFICATION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1DependencySpec {
  public static final String JSON_PROPERTY_COMPONENT = "component";
  private String component;

  public static final String JSON_PROPERTY_TARGET_COMPONENT = "targetComponent";
  private String targetComponent;

  public static final String JSON_PROPERTY_SECURITY_JUSTIFICATION = "securityJustification";
  @Nullable private String securityJustification;

  public V1DependencySpec() {}

  public V1DependencySpec component(String component) {
    this.component = component;
    return this;
  }

  /**
   * Name of the source component
   *
   * @return component
   */
  @NotNull
  @Schema(required = true, description = "Name of the source component")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponent() {
    return component;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponent(String component) {
    this.component = component;
  }

  public V1DependencySpec targetComponent(String targetComponent) {
    this.targetComponent = targetComponent;
    return this;
  }

  /**
   * Name of the target component
   *
   * @return targetComponent
   */
  @NotNull
  @Schema(required = true, description = "Name of the target component")
  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTargetComponent() {
    return targetComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTargetComponent(String targetComponent) {
    this.targetComponent = targetComponent;
  }

  public V1DependencySpec securityJustification(@Nullable String securityJustification) {
    this.securityJustification = securityJustification;
    return this;
  }

  /**
   * Each dependency should be justified.
   *
   * @return securityJustification
   */
  @Schema(description = "Each dependency should be justified.")
  @JsonProperty(value = JSON_PROPERTY_SECURITY_JUSTIFICATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getSecurityJustification() {
    return securityJustification;
  }

  @JsonProperty(value = JSON_PROPERTY_SECURITY_JUSTIFICATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSecurityJustification(@Nullable String securityJustification) {
    this.securityJustification = securityJustification;
  }

  /** Return true if this v1.DependencySpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1DependencySpec v1DependencySpec = (V1DependencySpec) o;
    return Objects.equals(this.component, v1DependencySpec.component)
        && Objects.equals(this.targetComponent, v1DependencySpec.targetComponent)
        && Objects.equals(this.securityJustification, v1DependencySpec.securityJustification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(component, targetComponent, securityJustification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1DependencySpec {\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    targetComponent: ").append(toIndentedString(targetComponent)).append("\n");
    sb.append("    securityJustification: ")
        .append(toIndentedString(securityJustification))
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
