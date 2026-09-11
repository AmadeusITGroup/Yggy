package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

/** GenericResponse */
@JsonPropertyOrder({})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GenericResponse extends ApiMetadata {
  public GenericResponse() {}

  @Override
  public GenericResponse url(@jakarta.annotation.Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public GenericResponse urlVersion(@jakarta.annotation.Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public GenericResponse version(@jakarta.annotation.Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public GenericResponse apiVersion(@jakarta.annotation.Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this GenericResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenericResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
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
