package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** The Target Type e.g. Regional Hub, Satellite, Satellite Native. */
@Schema(description = "The Target Type e.g. Regional Hub, Satellite, Satellite Native.")
@JsonPropertyOrder({})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1TargetType extends V1Base {
  public V1TargetType() {}

  @Override
  public V1TargetType apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1TargetType kind(String kind) {
    this.setKind(kind);
    return this;
  }

  @Override
  public V1TargetType metadata(V1Metadata metadata) {
    this.setMetadata(metadata);
    return this;
  }

  /** Return true if this v1.TargetType object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1TargetType {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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
