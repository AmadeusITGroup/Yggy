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
@JsonPropertyOrder({SecurityMatrixResponse.JSON_PROPERTY_MATRICES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityMatrixResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_MATRICES = "matrices";
  private Object matrices;

  public SecurityMatrixResponse() {}

  public SecurityMatrixResponse matrices(Object matrices) {
    this.matrices = matrices;
    return this;
  }

  /**
   * Get matrices
   *
   * @return matrices
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_MATRICES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getMatrices() {
    return matrices;
  }

  @JsonProperty(value = JSON_PROPERTY_MATRICES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMatrices(Object matrices) {
    this.matrices = matrices;
  }

  @Override
  public SecurityMatrixResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public SecurityMatrixResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public SecurityMatrixResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public SecurityMatrixResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this SecurityMatrixResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityMatrixResponse securityMatrixResponse = (SecurityMatrixResponse) o;
    return Objects.equals(this.matrices, securityMatrixResponse.matrices) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(matrices, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityMatrixResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    matrices: ").append(toIndentedString(matrices)).append("\n");
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
