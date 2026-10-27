package com.amadeus.aud.libraries.nsg.models;

import com.amadeus.aud.libraries.yaml.models.ObjectMetadata;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  Network.JSON_PROPERTY_CIDRS,
  Network.JSON_PROPERTY_PROVIDER_SPECIFIC,
  Network.JSON_PROPERTY_TAGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Network extends ObjectMetadata {
  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  private List<String> cidrs;

  public static final String JSON_PROPERTY_PROVIDER_SPECIFIC = "provider_specific";
  @Nullable private Object providerSpecific;

  public static final String JSON_PROPERTY_TAGS = "tags";
  @Nullable private Map<String, String> tags;

  public Network() {}

  public Network cidrs(List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public Network addCidrsItem(String cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * Get cidrs
   *
   * @return cidrs
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidrs(List<String> cidrs) {
    this.cidrs = cidrs;
  }

  public Network providerSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
    return this;
  }

  /**
   * Get providerSpecific
   *
   * @return providerSpecific
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getProviderSpecific() {
    return providerSpecific;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProviderSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
  }

  public Network tags(@Nullable Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  public Network putTagsItem(String key, String tagsItem) {
    if (this.tags == null) {
      this.tags = new HashMap<>();
    }
    this.tags.put(key, tagsItem);
    return this;
  }

  /**
   * Get tags
   *
   * @return tags
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_TAGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Map<String, String> getTags() {
    return tags;
  }

  @JsonProperty(value = JSON_PROPERTY_TAGS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTags(@Nullable Map<String, String> tags) {
    this.tags = tags;
  }

  @Override
  public Network labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this Network object is equal to o. */
  @Override
  public boolean equals(@jakarta.annotation.Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Network network = (Network) o;
    return Objects.equals(this.cidrs, network.cidrs)
        && Objects.equals(this.providerSpecific, network.providerSpecific)
        && Objects.equals(this.tags, network.tags)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cidrs, providerSpecific, tags, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Network {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    providerSpecific: ").append(toIndentedString(providerSpecific)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
