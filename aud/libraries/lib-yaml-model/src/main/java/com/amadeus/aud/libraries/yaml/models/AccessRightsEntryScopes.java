package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** AccessRightsEntryScopes */
@JsonPropertyOrder({AccessRightsEntryScopes.JSON_PROPERTY_SCOPES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessRightsEntryScopes {
  public static final String JSON_PROPERTY_SCOPES = "scopes";
  @Nullable private List<String> scopes;

  public AccessRightsEntryScopes() {}

  public AccessRightsEntryScopes scopes(@Nullable List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public AccessRightsEntryScopes addScopesItem(String scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

  /**
   * List of AUD Scope names which are allowed or denied to inherit from controlled components
   *
   * @return scopes
   */
  @Schema(
      required = false,
      description =
          "List of AUD Scope names which are allowed or denied to inherit from controlled components")
  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getScopes() {
    return scopes;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScopes(@Nullable List<String> scopes) {
    this.scopes = scopes;
  }

  /** Return true if this AccessRightsEntry_scopes object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessRightsEntryScopes accessRightsEntryScopes = (AccessRightsEntryScopes) o;
    return Objects.equals(this.scopes, accessRightsEntryScopes.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessRightsEntryScopes {\n");
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
