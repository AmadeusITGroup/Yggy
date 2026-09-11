package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Scope is your tool or application and a place where they are defined */
@Schema(description = "Scope is your tool or application and a place where they are defined")
@JsonPropertyOrder({V1Scope.JSON_PROPERTY_SPEC})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Scope extends V1Base {
  public static final String JSON_PROPERTY_SPEC = "spec";
  @Nullable private V1ScopeSpec spec;

  public V1Scope() {}

  public V1Scope spec(@Nullable V1ScopeSpec spec) {
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
  public @Nullable V1ScopeSpec getSpec() {
    return spec;
  }

  @JsonProperty(value = JSON_PROPERTY_SPEC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSpec(@Nullable V1ScopeSpec spec) {
    this.spec = spec;
  }

  @Override
  public V1Scope apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1Scope kind(String kind) {
    this.setKind(kind);
    return this;
  }

  @Override
  public V1Scope metadata(V1Metadata metadata) {
    this.setMetadata(metadata);
    return this;
  }

  /** Return true if this v1.Scope object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Scope v1Scope = (V1Scope) o;
    return Objects.equals(this.spec, v1Scope.spec) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Scope {\n");
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
