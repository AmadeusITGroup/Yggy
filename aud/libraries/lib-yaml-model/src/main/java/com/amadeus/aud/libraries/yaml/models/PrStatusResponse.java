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
@JsonPropertyOrder({PrStatusResponse.JSON_PROPERTY_STATUS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PrStatusResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_STATUS = "status";
  private Object status;

  public PrStatusResponse() {}

  public PrStatusResponse status(Object status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   *
   * @return status
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_STATUS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getStatus() {
    return status;
  }

  @JsonProperty(value = JSON_PROPERTY_STATUS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setStatus(Object status) {
    this.status = status;
  }

  @Override
  public PrStatusResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public PrStatusResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public PrStatusResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public PrStatusResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this PrStatusResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrStatusResponse prStatusResponse = (PrStatusResponse) o;
    return Objects.equals(this.status, prStatusResponse.status) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PrStatusResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
