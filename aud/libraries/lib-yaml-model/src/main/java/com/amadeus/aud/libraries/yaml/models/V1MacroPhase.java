package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Definition of the MacroPhase */
@Schema(description = "Definition of the MacroPhase")
@JsonPropertyOrder({V1MacroPhase.JSON_PROPERTY_SPEC})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1MacroPhase extends V1Base {
  public static final String JSON_PROPERTY_SPEC = "spec";
  @Nullable private V1MacroPhaseSpec spec;

  public V1MacroPhase() {}

  public V1MacroPhase spec(@Nullable V1MacroPhaseSpec spec) {
    this.spec = spec;
    return this;
  }

  /**
   * Get spec
   *
   * @return spec
   */
  @Valid
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_SPEC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable V1MacroPhaseSpec getSpec() {
    return spec;
  }

  @JsonProperty(value = JSON_PROPERTY_SPEC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSpec(@Nullable V1MacroPhaseSpec spec) {
    this.spec = spec;
  }

  @Override
  public V1MacroPhase apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1MacroPhase kind(String kind) {
    this.setKind(kind);
    return this;
  }

  @Override
  public V1MacroPhase metadata(V1Metadata metadata) {
    this.setMetadata(metadata);
    return this;
  }

  /** Return true if this v1.MacroPhase object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1MacroPhase v1MacroPhase = (V1MacroPhase) o;
    return Objects.equals(this.spec, v1MacroPhase.spec) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1MacroPhase {\n");
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
