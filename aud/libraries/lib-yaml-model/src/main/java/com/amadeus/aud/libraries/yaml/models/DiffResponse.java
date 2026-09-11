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
@JsonPropertyOrder({DiffResponse.JSON_PROPERTY_TARGETS, DiffResponse.JSON_PROPERTY_HIGHLEVELS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DiffResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_TARGETS = "targets";
  private Object targets;

  public static final String JSON_PROPERTY_HIGHLEVELS = "highlevels";
  private Object highlevels;

  public DiffResponse() {}

  public DiffResponse targets(Object targets) {
    this.targets = targets;
    return this;
  }

  /**
   * Get targets
   *
   * @return targets
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getTargets() {
    return targets;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTargets(Object targets) {
    this.targets = targets;
  }

  public DiffResponse highlevels(Object highlevels) {
    this.highlevels = highlevels;
    return this;
  }

  /**
   * Get highlevels
   *
   * @return highlevels
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_HIGHLEVELS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Object getHighlevels() {
    return highlevels;
  }

  @JsonProperty(value = JSON_PROPERTY_HIGHLEVELS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setHighlevels(Object highlevels) {
    this.highlevels = highlevels;
  }

  @Override
  public DiffResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public DiffResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public DiffResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public DiffResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this DiffResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiffResponse diffResponse = (DiffResponse) o;
    return Objects.equals(this.targets, diffResponse.targets)
        && Objects.equals(this.highlevels, diffResponse.highlevels)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets, highlevels, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiffResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
    sb.append("    highlevels: ").append(toIndentedString(highlevels)).append("\n");
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
