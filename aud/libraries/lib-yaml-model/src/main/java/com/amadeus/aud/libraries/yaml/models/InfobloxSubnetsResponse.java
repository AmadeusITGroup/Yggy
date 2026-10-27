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

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({InfobloxSubnetsResponse.JSON_PROPERTY_SUBNETS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InfobloxSubnetsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  private List<InfobloxSubnet> subnets;

  public InfobloxSubnetsResponse() {}

  public InfobloxSubnetsResponse subnets(List<InfobloxSubnet> subnets) {
    this.subnets = subnets;
    return this;
  }

  public InfobloxSubnetsResponse addSubnetsItem(InfobloxSubnet subnetsItem) {
    if (this.subnets == null) {
      this.subnets = new ArrayList<>();
    }
    this.subnets.add(subnetsItem);
    return this;
  }

  /**
   * List of Infoblox subnets
   *
   * @return subnets
   */
  @NotNull
  @Schema(required = true, description = "List of Infoblox subnets")
  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid InfobloxSubnet> getSubnets() {
    return subnets;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnets(List<InfobloxSubnet> subnets) {
    this.subnets = subnets;
  }

  @Override
  public InfobloxSubnetsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public InfobloxSubnetsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public InfobloxSubnetsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public InfobloxSubnetsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this InfobloxSubnetsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfobloxSubnetsResponse infobloxSubnetsResponse = (InfobloxSubnetsResponse) o;
    return Objects.equals(this.subnets, infobloxSubnetsResponse.subnets) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subnets, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfobloxSubnetsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    subnets: ").append(toIndentedString(subnets)).append("\n");
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
