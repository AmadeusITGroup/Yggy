package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ObjectMetadata */
@JsonPropertyOrder({ObjectMetadata.JSON_PROPERTY_LABELS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectMetadata {
  public static final String JSON_PROPERTY_LABELS = "labels";
  @Nullable private Map<String, String> labels;

  public ObjectMetadata() {}

  public ObjectMetadata labels(@Nullable Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  public ObjectMetadata putLabelsItem(String key, String labelsItem) {
    if (this.labels == null) {
      this.labels = new HashMap<>();
    }
    this.labels.put(key, labelsItem);
    return this;
  }

  /**
   * labels to further describe the resource.
   *
   * @return labels
   */
  @Schema(required = false, description = "labels to further describe the resource.")
  @JsonProperty(value = JSON_PROPERTY_LABELS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Map<String, String> getLabels() {
    return labels;
  }

  @JsonProperty(value = JSON_PROPERTY_LABELS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLabels(@Nullable Map<String, String> labels) {
    this.labels = labels;
  }

  /** Return true if this ObjectMetadata object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectMetadata objectMetadata = (ObjectMetadata) o;
    return Objects.equals(this.labels, objectMetadata.labels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(labels);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ObjectMetadata {\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
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
