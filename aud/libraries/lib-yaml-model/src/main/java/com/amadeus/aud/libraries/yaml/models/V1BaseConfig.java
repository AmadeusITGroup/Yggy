package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Base for config definition objects. */
@Schema(description = "Base for config definition objects.")
@JsonPropertyOrder({V1BaseConfig.JSON_PROPERTY_METADATA})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1BaseConfig extends V1Base {
  public static final String JSON_PROPERTY_METADATA = "metadata";
  private V1MetadataConfig metadata;

  public V1BaseConfig() {}

  public V1BaseConfig metadata(V1MetadataConfig metadata) {
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
  public V1MetadataConfig getMetadata() {
    return metadata;
  }

  @JsonProperty(value = JSON_PROPERTY_METADATA, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMetadata(V1MetadataConfig metadata) {
    this.metadata = metadata;
  }

  @Override
  public V1BaseConfig apiVersion(String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  @Override
  public V1BaseConfig kind(String kind) {
    this.setKind(kind);
    return this;
  }

  /** Return true if this v1BaseConfig object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1BaseConfig v1BaseConfig = (V1BaseConfig) o;
    return Objects.equals(this.metadata, v1BaseConfig.metadata) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metadata, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1BaseConfig {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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
