package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({SubnetPeering.JSON_PROPERTY_SOURCE, SubnetPeering.JSON_PROPERTY_DESTINATION})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SubnetPeering {
  public static final String JSON_PROPERTY_SOURCE = "source";
  private V1SubnetInstance source;

  public static final String JSON_PROPERTY_DESTINATION = "destination";
  private V1SubnetInstance destination;

  public SubnetPeering() {}

  public SubnetPeering source(V1SubnetInstance source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   *
   * @return source
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SOURCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public V1SubnetInstance getSource() {
    return source;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSource(V1SubnetInstance source) {
    this.source = source;
  }

  public SubnetPeering destination(V1SubnetInstance destination) {
    this.destination = destination;
    return this;
  }

  /**
   * Get destination
   *
   * @return destination
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public V1SubnetInstance getDestination() {
    return destination;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDestination(V1SubnetInstance destination) {
    this.destination = destination;
  }

  /** Return true if this SubnetPeering object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubnetPeering subnetPeering = (SubnetPeering) o;
    return Objects.equals(this.source, subnetPeering.source)
        && Objects.equals(this.destination, subnetPeering.destination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubnetPeering {\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
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
