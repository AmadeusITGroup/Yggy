package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({ScopeDiffResponse.JSON_PROPERTY_SCOPES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScopeDiffResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SCOPES = "scopes";
  private ScopeDiffs scopes;

  public ScopeDiffResponse() {}

  public ScopeDiffResponse scopes(ScopeDiffs scopes) {
    this.scopes = scopes;
    return this;
  }

  /**
   * Get scopes
   *
   * @return scopes
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ScopeDiffs getScopes() {
    return scopes;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScopes(ScopeDiffs scopes) {
    this.scopes = scopes;
  }

  @Override
  public ScopeDiffResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public ScopeDiffResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public ScopeDiffResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public ScopeDiffResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this ScopeDiffResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeDiffResponse scopeDiffResponse = (ScopeDiffResponse) o;
    return Objects.equals(this.scopes, scopeDiffResponse.scopes) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scopes, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDiffResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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
