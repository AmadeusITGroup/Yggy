package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Definition of an IPAddressInstance. For Legacy system, the IP address is mandatory to be enter in
 * the AUD system for each ComponentInstance.
 */
@Schema(
    description =
        "Definition of an IPAddressInstance. For Legacy system, the IP address is mandatory to be enter in the AUD system for each ComponentInstance.")
@JsonPropertyOrder({V1IPAddressInstance.JSON_PROPERTY_SPEC})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1IPAddressInstance extends V1BaseInstance {
  public static final String JSON_PROPERTY_SPEC = "spec";
  private V1IPAddressInstanceSpec spec;

  public V1IPAddressInstance() {}

  public V1IPAddressInstance spec(V1IPAddressInstanceSpec spec) {
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
  public V1IPAddressInstanceSpec getSpec() {
    return spec;
  }

  @JsonProperty(value = JSON_PROPERTY_SPEC, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSpec(V1IPAddressInstanceSpec spec) {
    this.spec = spec;
  }

  @Override
  public V1IPAddressInstance apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1IPAddressInstance kind(String kind) {
    this.setKind(kind);
    return this;
  }

  /** Return true if this v1.IPAddressInstance object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1IPAddressInstance v1IPAddressInstance = (V1IPAddressInstance) o;
    return Objects.equals(this.spec, v1IPAddressInstance.spec) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spec, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1IPAddressInstance {\n");
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
