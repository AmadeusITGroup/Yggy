package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** SpokeCIDRs */
@JsonPropertyOrder({
  SpokeCIDRs.JSON_PROPERTY_LANDINGZONE,
  SpokeCIDRs.JSON_PROPERTY_LANDINGZONETYPE,
  SpokeCIDRs.JSON_PROPERTY_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SpokeCIDRs extends ObjectMetadata {
  public static final String JSON_PROPERTY_LANDINGZONE = "landingzone";
  private String landingzone;

  public static final String JSON_PROPERTY_LANDINGZONETYPE = "landingzonetype";
  @Nullable private String landingzonetype;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<String> cidrs;

  public SpokeCIDRs() {}

  public SpokeCIDRs landingzone(String landingzone) {
    this.landingzone = landingzone;
    return this;
  }

  /**
   * Get landingzone
   *
   * @return landingzone
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzone() {
    return landingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzone(String landingzone) {
    this.landingzone = landingzone;
  }

  public SpokeCIDRs landingzonetype(@Nullable String landingzonetype) {
    this.landingzonetype = landingzonetype;
    return this;
  }

  /**
   * Get landingzonetype
   *
   * @return landingzonetype
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONETYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getLandingzonetype() {
    return landingzonetype;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONETYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLandingzonetype(@Nullable String landingzonetype) {
    this.landingzonetype = landingzonetype;
  }

  public SpokeCIDRs cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public SpokeCIDRs addCidrsItem(String cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * Get cidrs
   *
   * @return cidrs
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<String> cidrs) {
    this.cidrs = cidrs;
  }

  @Override
  public SpokeCIDRs labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this SpokeCIDRs object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SpokeCIDRs spokeCIDRs = (SpokeCIDRs) o;
    return Objects.equals(this.landingzone, spokeCIDRs.landingzone)
        && Objects.equals(this.landingzonetype, spokeCIDRs.landingzonetype)
        && Objects.equals(this.cidrs, spokeCIDRs.cidrs)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(landingzone, landingzonetype, cidrs, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SpokeCIDRs {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    landingzonetype: ").append(toIndentedString(landingzonetype)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
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
