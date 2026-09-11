package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({SecurityZoningResponse.JSON_PROPERTY_SUBNETS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityZoningResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SUBNETS = "subnets";
  private Object subnets;

  public SecurityZoningResponse() {}

  public SecurityZoningResponse subnets(Object subnets) {
    this.subnets = subnets;
    return this;
  }

  /**
   * Get subnets
   *
   * @return subnets
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getSubnets() {
    return subnets;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnets(Object subnets) {
    this.subnets = subnets;
  }

  @Override
  public SecurityZoningResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public SecurityZoningResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public SecurityZoningResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public SecurityZoningResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this SecurityZoningResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityZoningResponse securityZoningResponse = (SecurityZoningResponse) o;
    return Objects.equals(this.subnets, securityZoningResponse.subnets) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subnets, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityZoningResponse {\n");
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
