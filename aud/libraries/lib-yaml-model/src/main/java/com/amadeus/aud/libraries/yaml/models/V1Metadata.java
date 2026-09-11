package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Metadata to identify a resource - Mandatory part of all the AUD objects */
@Schema(description = "Metadata to identify a resource - Mandatory part of all the AUD objects")
@JsonPropertyOrder({
  V1Metadata.JSON_PROPERTY_SCOPE,
  V1Metadata.JSON_PROPERTY_NAME,
  V1Metadata.JSON_PROPERTY_PROVIDER_SPECIFIC,
  V1Metadata.JSON_PROPERTY_LABELS,
  V1Metadata.JSON_PROPERTY_COMMENT
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1Metadata {
  public static final String JSON_PROPERTY_SCOPE = "scope";
  private String scope;

  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_PROVIDER_SPECIFIC = "providerSpecific";
  @Nullable private String providerSpecific;

  public static final String JSON_PROPERTY_LABELS = "labels";
  @Nullable private Map<String, String> labels;

  public static final String JSON_PROPERTY_COMMENT = "comment";
  @Nullable private String comment;

  public V1Metadata() {}

  public V1Metadata scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * The scope of the resource (RBAC), tool or application and a place where they are defined
   *
   * @return scope
   */
  @NotNull
  @Schema(
      required = true,
      description =
          "The scope of the resource (RBAC), tool or application and a place where they are defined")
  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScope(String scope) {
    this.scope = scope;
  }

  public V1Metadata name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The name of the resource, must be unique.
   *
   * @return name
   */
  @NotNull
  @Schema(required = true, description = "The name of the resource, must be unique.")
  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(String name) {
    this.name = name;
  }

  public V1Metadata providerSpecific(@Nullable String providerSpecific) {
    this.providerSpecific = providerSpecific;
    return this;
  }

  /**
   * Json including all properties that are provider specific.
   *
   * @return providerSpecific
   */
  @Schema(
      required = false,
      description = "Json including all properties that are provider specific.")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getProviderSpecific() {
    return providerSpecific;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProviderSpecific(@Nullable String providerSpecific) {
    this.providerSpecific = providerSpecific;
  }

  public V1Metadata labels(@Nullable Map<String, String> labels) {
    this.labels = labels;
    return this;
  }

  public V1Metadata putLabelsItem(String key, String labelsItem) {
    if (this.labels == null) {
      this.labels = new HashMap<>();
    }
    this.labels.put(key, labelsItem);
    return this;
  }

  /**
   * Labels to further describe the resource.
   *
   * @return labels
   */
  @Schema(required = false, description = "Labels to further describe the resource.")
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

  public V1Metadata comment(@Nullable String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Any comment related to this object.
   *
   * @return comment
   */
  @Schema(required = false, description = "Any comment related to this object.")
  @JsonProperty(value = JSON_PROPERTY_COMMENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getComment() {
    return comment;
  }

  @JsonProperty(value = JSON_PROPERTY_COMMENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComment(@Nullable String comment) {
    this.comment = comment;
  }

  /** Return true if this v1Metadata object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1Metadata v1Metadata = (V1Metadata) o;
    return Objects.equals(this.scope, v1Metadata.scope)
        && Objects.equals(this.name, v1Metadata.name)
        && Objects.equals(this.providerSpecific, v1Metadata.providerSpecific)
        && Objects.equals(this.labels, v1Metadata.labels)
        && Objects.equals(this.comment, v1Metadata.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, name, providerSpecific, labels, comment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1Metadata {\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    providerSpecific: ").append(toIndentedString(providerSpecific)).append("\n");
    sb.append("    labels: ").append(toIndentedString(labels)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
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
