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
@JsonPropertyOrder({SecurityLayersResponse.JSON_PROPERTY_SECURITY_LAYERS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityLayersResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SECURITY_LAYERS = "security_layers";
  private List<SecurityLayer> securityLayers;

  public SecurityLayersResponse() {}

  public SecurityLayersResponse securityLayers(List<SecurityLayer> securityLayers) {
    this.securityLayers = securityLayers;
    return this;
  }

  public SecurityLayersResponse addSecurityLayersItem(SecurityLayer securityLayersItem) {
    if (this.securityLayers == null) {
      this.securityLayers = new ArrayList<>();
    }
    this.securityLayers.add(securityLayersItem);
    return this;
  }

  /**
   * List of securityLayer
   *
   * @return securityLayers
   */
  @NotNull
  @Schema(required = true, description = "List of securityLayer")
  @JsonProperty(value = JSON_PROPERTY_SECURITY_LAYERS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid SecurityLayer> getSecurityLayers() {
    return securityLayers;
  }

  @JsonProperty(value = JSON_PROPERTY_SECURITY_LAYERS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSecurityLayers(List<SecurityLayer> securityLayers) {
    this.securityLayers = securityLayers;
  }

  @Override
  public SecurityLayersResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public SecurityLayersResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public SecurityLayersResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public SecurityLayersResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this SecurityLayersResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityLayersResponse securityLayersResponse = (SecurityLayersResponse) o;
    return Objects.equals(this.securityLayers, securityLayersResponse.securityLayers)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(securityLayers, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityLayersResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    securityLayers: ").append(toIndentedString(securityLayers)).append("\n");
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
