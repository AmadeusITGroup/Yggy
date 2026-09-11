package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The target name, target is the environment you need to build. 1 target represent 1 or multiple
 * Hub &amp; multiple spokes with the same region and macro phase.
 */
@Schema(
    description =
        "The target name, target is the environment you need to build. 1 target represent 1 or multiple Hub & multiple spokes with the same region and macro phase.")
@JsonPropertyOrder({V1Target.JSON_PROPERTY_SPEC})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Target extends V1Base {
  public static final String JSON_PROPERTY_SPEC = "spec";
  private V1TargetSpec spec;

  public V1Target() {}

  public V1Target spec(V1TargetSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   *
   * @return spec
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SPEC, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public V1TargetSpec getSpec() {
    return spec;
  }

  @JsonProperty(value = JSON_PROPERTY_SPEC, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSpec(V1TargetSpec spec) {
    this.spec = spec;
  }

  @Override
  public V1Target apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1Target kind(String kind) {
    this.setKind(kind);
    return this;
  }

  @Override
  public V1Target metadata(V1Metadata metadata) {
    this.setMetadata(metadata);
    return this;
  }

  /** Return true if this v1.Target object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Target v1Target = (V1Target) o;
    return Objects.equals(this.spec, v1Target.spec) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Target {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    spec: ").append(toIndentedString(spec)).append("\n");
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
