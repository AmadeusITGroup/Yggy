package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({CloudProtectedTargetsResponse.JSON_PROPERTY_CLOUD_PROTECTED_TARGETS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CloudProtectedTargetsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_CLOUD_PROTECTED_TARGETS = "cloud_protected_targets";
  private List<Object> cloudProtectedTargets;

  public CloudProtectedTargetsResponse() {}

  public CloudProtectedTargetsResponse cloudProtectedTargets(List<Object> cloudProtectedTargets) {
    this.cloudProtectedTargets = cloudProtectedTargets;
    return this;
  }

  public CloudProtectedTargetsResponse addCloudProtectedTargetsItem(
      Object cloudProtectedTargetsItem) {
    if (this.cloudProtectedTargets == null) {
      this.cloudProtectedTargets = new ArrayList<>();
    }
    this.cloudProtectedTargets.add(cloudProtectedTargetsItem);
    return this;
  }

  /**
   * List of Cloud Protected Target
   *
   * @return cloudProtectedTargets
   */
  @NotNull
  @Schema(required = true, description = "List of Cloud Protected Target")
  @JsonProperty(value = JSON_PROPERTY_CLOUD_PROTECTED_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<Object> getCloudProtectedTargets() {
    return cloudProtectedTargets;
  }

  @JsonProperty(value = JSON_PROPERTY_CLOUD_PROTECTED_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCloudProtectedTargets(List<Object> cloudProtectedTargets) {
    this.cloudProtectedTargets = cloudProtectedTargets;
  }

  @Override
  public CloudProtectedTargetsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public CloudProtectedTargetsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public CloudProtectedTargetsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public CloudProtectedTargetsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this CloudProtectedTargetsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CloudProtectedTargetsResponse cloudProtectedTargetsResponse = (CloudProtectedTargetsResponse) o;
    return Objects.equals(
            this.cloudProtectedTargets, cloudProtectedTargetsResponse.cloudProtectedTargets)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cloudProtectedTargets, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CloudProtectedTargetsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    cloudProtectedTargets: ")
        .append(toIndentedString(cloudProtectedTargets))
        .append("\n");
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
