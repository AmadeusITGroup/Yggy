package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Definition of a Conncetion. The connection between two security zone types - Filled by Security
 */
@Schema(
    description =
        "Definition of a Conncetion. The connection between two security zone types - Filled by Security")
@JsonPropertyOrder({V1Connection.JSON_PROPERTY_SPEC})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Connection extends V1Base {
  public static final String JSON_PROPERTY_SPEC = "spec";
  private V1ConnectionSpec spec;

  public V1Connection() {}

  public V1Connection spec(V1ConnectionSpec spec) {
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
  public V1ConnectionSpec getSpec() {
    return spec;
  }

  @JsonProperty(value = JSON_PROPERTY_SPEC, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSpec(V1ConnectionSpec spec) {
    this.spec = spec;
  }

  @Override
  public V1Connection apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1Connection kind(String kind) {
    this.setKind(kind);
    return this;
  }

  @Override
  public V1Connection metadata(V1Metadata metadata) {
    this.setMetadata(metadata);
    return this;
  }

  /** Return true if this v1.Connection object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Connection v1Connection = (V1Connection) o;
    return Objects.equals(this.spec, v1Connection.spec) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Connection {\n");
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
