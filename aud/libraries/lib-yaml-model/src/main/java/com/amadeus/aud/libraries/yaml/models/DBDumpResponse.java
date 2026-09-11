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
@JsonPropertyOrder({DBDumpResponse.JSON_PROPERTY_TARGETS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DBDumpResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_TARGETS = "targets";
  private Object targets;

  public DBDumpResponse() {}

  public DBDumpResponse targets(Object targets) {
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

  @Override
  public DBDumpResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public DBDumpResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public DBDumpResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public DBDumpResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this DBDumpResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DBDumpResponse dbDumpResponse = (DBDumpResponse) o;
    return Objects.equals(this.targets, dbDumpResponse.targets) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targets, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DBDumpResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
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
