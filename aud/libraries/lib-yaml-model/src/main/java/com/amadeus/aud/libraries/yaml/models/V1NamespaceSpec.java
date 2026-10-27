package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1NamespaceSpec */
@JsonPropertyOrder({
  V1NamespaceSpec.JSON_PROPERTY_IS_SHARED,
  V1NamespaceSpec.JSON_PROPERTY_AUTHORIZED_SCOPES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1NamespaceSpec {
  public static final String JSON_PROPERTY_IS_SHARED = "is_shared";
  @Nullable private Boolean isShared = false;

  public static final String JSON_PROPERTY_AUTHORIZED_SCOPES = "authorized_scopes";
  @Nullable private List<String> authorizedScopes;

  public V1NamespaceSpec() {}

  public V1NamespaceSpec isShared(@Nullable Boolean isShared) {
    this.isShared = isShared;
    return this;
  }

  /**
   * is Namespace authorized in other scopes ?
   *
   * @return isShared
   */
  @Schema(description = "is Namespace authorized in other scopes ?")
  @JsonProperty(value = JSON_PROPERTY_IS_SHARED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsShared() {
    return isShared;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SHARED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsShared(@Nullable Boolean isShared) {
    this.isShared = isShared;
  }

  public V1NamespaceSpec authorizedScopes(@Nullable List<String> authorizedScopes) {
    this.authorizedScopes = authorizedScopes;
    return this;
  }

  public V1NamespaceSpec addAuthorizedScopesItem(String authorizedScopesItem) {
    if (this.authorizedScopes == null) {
      this.authorizedScopes = new ArrayList<>();
    }
    this.authorizedScopes.add(authorizedScopesItem);
    return this;
  }

  /**
   * List of scopes allowed to use this namespace
   *
   * @return authorizedScopes
   */
  @Schema(description = "List of scopes allowed to use this namespace")
  @JsonProperty(value = JSON_PROPERTY_AUTHORIZED_SCOPES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getAuthorizedScopes() {
    return authorizedScopes;
  }

  @JsonProperty(value = JSON_PROPERTY_AUTHORIZED_SCOPES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAuthorizedScopes(@Nullable List<String> authorizedScopes) {
    this.authorizedScopes = authorizedScopes;
  }

  /** Return true if this v1.NamespaceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1NamespaceSpec v1NamespaceSpec = (V1NamespaceSpec) o;
    return Objects.equals(this.isShared, v1NamespaceSpec.isShared)
        && Objects.equals(this.authorizedScopes, v1NamespaceSpec.authorizedScopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isShared, authorizedScopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1NamespaceSpec {\n");
    sb.append("    isShared: ").append(toIndentedString(isShared)).append("\n");
    sb.append("    authorizedScopes: ").append(toIndentedString(authorizedScopes)).append("\n");
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
