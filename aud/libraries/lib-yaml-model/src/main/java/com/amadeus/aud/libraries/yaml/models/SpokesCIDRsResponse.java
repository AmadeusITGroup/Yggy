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
@JsonPropertyOrder({SpokesCIDRsResponse.JSON_PROPERTY_SPOKES_CIDRS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SpokesCIDRsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SPOKES_CIDRS = "spokes_cidrs";
  private List<SpokeCIDRs> spokesCidrs;

  public SpokesCIDRsResponse() {}

  public SpokesCIDRsResponse spokesCidrs(List<SpokeCIDRs> spokesCidrs) {
    this.spokesCidrs = spokesCidrs;
    return this;
  }

  public SpokesCIDRsResponse addSpokesCidrsItem(SpokeCIDRs spokesCidrsItem) {
    if (this.spokesCidrs == null) {
      this.spokesCidrs = new ArrayList<>();
    }
    this.spokesCidrs.add(spokesCidrsItem);
    return this;
  }

  /**
   * Get spokesCidrs
   *
   * @return spokesCidrs
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SPOKES_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid SpokeCIDRs> getSpokesCidrs() {
    return spokesCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_SPOKES_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSpokesCidrs(List<SpokeCIDRs> spokesCidrs) {
    this.spokesCidrs = spokesCidrs;
  }

  @Override
  public SpokesCIDRsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public SpokesCIDRsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public SpokesCIDRsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public SpokesCIDRsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this SpokesCIDRsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SpokesCIDRsResponse spokesCIDRsResponse = (SpokesCIDRsResponse) o;
    return Objects.equals(this.spokesCidrs, spokesCIDRsResponse.spokesCidrs) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(spokesCidrs, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SpokesCIDRsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    spokesCidrs: ").append(toIndentedString(spokesCidrs)).append("\n");
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
