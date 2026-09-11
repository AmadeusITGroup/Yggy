package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1ServicetagSpec */
@JsonPropertyOrder({V1ServicetagSpec.JSON_PROPERTY_ANY_NEEDED_IN_NSG})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ServicetagSpec {
  public static final String JSON_PROPERTY_ANY_NEEDED_IN_NSG = "any_needed_in_nsg";
  @Nullable private Boolean anyNeededInNsg = false;

  public V1ServicetagSpec() {}

  public V1ServicetagSpec anyNeededInNsg(@Nullable Boolean anyNeededInNsg) {
    this.anyNeededInNsg = anyNeededInNsg;
    return this;
  }

  /**
   * is Namespace authorized in other scopes ?
   *
   * @return anyNeededInNsg
   */
  @Schema(required = false, description = "is Namespace authorized in other scopes ?")
  @JsonProperty(value = JSON_PROPERTY_ANY_NEEDED_IN_NSG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getAnyNeededInNsg() {
    return anyNeededInNsg;
  }

  @JsonProperty(value = JSON_PROPERTY_ANY_NEEDED_IN_NSG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAnyNeededInNsg(@Nullable Boolean anyNeededInNsg) {
    this.anyNeededInNsg = anyNeededInNsg;
  }

  /** Return true if this v1.ServicetagSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ServicetagSpec v1ServicetagSpec = (V1ServicetagSpec) o;
    return Objects.equals(this.anyNeededInNsg, v1ServicetagSpec.anyNeededInNsg);
  }

  @Override
  public int hashCode() {
    return Objects.hash(anyNeededInNsg);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ServicetagSpec {\n");
    sb.append("    anyNeededInNsg: ").append(toIndentedString(anyNeededInNsg)).append("\n");
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
