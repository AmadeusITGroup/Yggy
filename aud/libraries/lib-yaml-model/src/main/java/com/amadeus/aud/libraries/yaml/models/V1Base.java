package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Base for model definition objects. */
@Schema(description = "Base for model definition objects.")
@JsonPropertyOrder({
  V1Base.JSON_PROPERTY_API_VERSION,
  V1Base.JSON_PROPERTY_KIND,
  V1Base.JSON_PROPERTY_METADATA
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Base implements ModelElement {
  public static final String JSON_PROPERTY_API_VERSION = "apiVersion";
  private String apiVersion;

  public static final String JSON_PROPERTY_KIND = "kind";
  private String kind;

  public static final String JSON_PROPERTY_METADATA = "metadata";
  private V1Metadata metadata;

  public V1Base() {}

  public V1Base apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  @NotNull
  @Schema(required = true, description = "AUD Model Version")
  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  @Pattern(regexp = "v1")
  public String getApiVersion() {
    return apiVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_API_VERSION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  public V1Base kind(String kind) {
    this.kind = kind;
    return this;
  }

  /**
   * Kind of Object.
   *
   * @return kind
   */
  @NotNull
  @Schema(required = true, description = "Kind of Object.")
  @JsonProperty(value = JSON_PROPERTY_KIND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getKind() {
    return kind;
  }

  @JsonProperty(value = JSON_PROPERTY_KIND, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setKind(String kind) {
    this.kind = kind;
  }

  public V1Base metadata(V1Metadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   *
   * @return metadata
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_METADATA, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public V1Metadata getMetadata() {
    return metadata;
  }

  @JsonProperty(value = JSON_PROPERTY_METADATA, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMetadata(V1Metadata metadata) {
    this.metadata = metadata;
  }

  /** Return true if this v1Base object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Base v1Base = (V1Base) o;
    return Objects.equals(this.apiVersion, v1Base.apiVersion)
        && Objects.equals(this.kind, v1Base.kind)
        && Objects.equals(this.metadata, v1Base.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiVersion, kind, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Base {\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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
