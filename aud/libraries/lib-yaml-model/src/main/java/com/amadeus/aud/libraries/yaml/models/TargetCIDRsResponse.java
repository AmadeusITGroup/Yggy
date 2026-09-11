package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Response object of the UDR webservice */
@Schema(description = "Response object of the UDR webservice")
@JsonPropertyOrder({
  TargetCIDRsResponse.JSON_PROPERTY_TARGET_CIDRS,
  TargetCIDRsResponse.JSON_PROPERTY_LANDINGZONE_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TargetCIDRsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_TARGET_CIDRS = "target_cidrs";
  private List<String> targetCidrs;

  public static final String JSON_PROPERTY_LANDINGZONE_CIDRS = "landingzone_cidrs";
  private List<SpokeCIDRs> landingzoneCidrs;

  public TargetCIDRsResponse() {}

  public TargetCIDRsResponse targetCidrs(List<String> targetCidrs) {
    this.targetCidrs = targetCidrs;
    return this;
  }

  public TargetCIDRsResponse addTargetCidrsItem(String targetCidrsItem) {
    if (this.targetCidrs == null) {
      this.targetCidrs = new ArrayList<>();
    }
    this.targetCidrs.add(targetCidrsItem);
    return this;
  }

  /**
   * Get targetCidrs
   *
   * @return targetCidrs
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TARGET_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getTargetCidrs() {
    return targetCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTargetCidrs(List<String> targetCidrs) {
    this.targetCidrs = targetCidrs;
  }

  public TargetCIDRsResponse landingzoneCidrs(List<SpokeCIDRs> landingzoneCidrs) {
    this.landingzoneCidrs = landingzoneCidrs;
    return this;
  }

  public TargetCIDRsResponse addLandingzoneCidrsItem(SpokeCIDRs landingzoneCidrsItem) {
    if (this.landingzoneCidrs == null) {
      this.landingzoneCidrs = new ArrayList<>();
    }
    this.landingzoneCidrs.add(landingzoneCidrsItem);
    return this;
  }

  /**
   * Get landingzoneCidrs
   *
   * @return landingzoneCidrs
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<SpokeCIDRs> getLandingzoneCidrs() {
    return landingzoneCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzoneCidrs(List<SpokeCIDRs> landingzoneCidrs) {
    this.landingzoneCidrs = landingzoneCidrs;
  }

  @Override
  public TargetCIDRsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public TargetCIDRsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public TargetCIDRsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public TargetCIDRsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this TargetCIDRsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TargetCIDRsResponse targetCIDRsResponse = (TargetCIDRsResponse) o;
    return Objects.equals(this.targetCidrs, targetCIDRsResponse.targetCidrs)
        && Objects.equals(this.landingzoneCidrs, targetCIDRsResponse.landingzoneCidrs)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetCidrs, landingzoneCidrs, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TargetCIDRsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    targetCidrs: ").append(toIndentedString(targetCidrs)).append("\n");
    sb.append("    landingzoneCidrs: ").append(toIndentedString(landingzoneCidrs)).append("\n");
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
