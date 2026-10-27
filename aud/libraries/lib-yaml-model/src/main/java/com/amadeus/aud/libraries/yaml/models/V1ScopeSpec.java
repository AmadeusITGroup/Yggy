package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Scope specification */
@Schema(description = "Scope specification")
@JsonPropertyOrder({
  V1ScopeSpec.JSON_PROPERTY_IS_DELETED,
  V1ScopeSpec.JSON_PROPERTY_DELETION_DATE,
  V1ScopeSpec.JSON_PROPERTY_ROLES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ScopeSpec {
  public static final String JSON_PROPERTY_IS_DELETED = "is_deleted";
  @Nullable private Boolean isDeleted;

  public static final String JSON_PROPERTY_DELETION_DATE = "deletion_date";
  @Nullable private String deletionDate;

  public static final String JSON_PROPERTY_ROLES = "roles";
  @Nullable private List<String> roles;

  public V1ScopeSpec() {}

  public V1ScopeSpec isDeleted(@Nullable Boolean isDeleted) {
    this.isDeleted = isDeleted;
    return this;
  }

  /**
   * scope deleted Boolean
   *
   * @return isDeleted
   */
  @Schema(description = "scope deleted Boolean")
  @JsonProperty(value = JSON_PROPERTY_IS_DELETED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsDeleted() {
    return isDeleted;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_DELETED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsDeleted(@Nullable Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public V1ScopeSpec deletionDate(@Nullable String deletionDate) {
    this.deletionDate = deletionDate;
    return this;
  }

  /**
   * Get deletionDate
   *
   * @return deletionDate
   */
  @Schema(description = "Date when the scope was deleted.")
  @JsonProperty(value = JSON_PROPERTY_DELETION_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeletionDate() {
    return deletionDate;
  }

  @JsonProperty(value = JSON_PROPERTY_DELETION_DATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeletionDate(@Nullable String deletionDate) {
    this.deletionDate = deletionDate;
  }

  public V1ScopeSpec roles(@Nullable List<String> roles) {
    this.roles = roles;
    return this;
  }

  public V1ScopeSpec addRolesItem(String rolesItem) {
    if (this.roles == null) {
      this.roles = new ArrayList<>();
    }
    this.roles.add(rolesItem);
    return this;
  }

  /**
   * List of roles authorized in the scope
   *
   * @return roles
   */
  @Schema(description = "List of roles authorized in the scope")
  @JsonProperty(value = JSON_PROPERTY_ROLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getRoles() {
    return roles;
  }

  @JsonProperty(value = JSON_PROPERTY_ROLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRoles(@Nullable List<String> roles) {
    this.roles = roles;
  }

  /** Return true if this v1ScopeSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ScopeSpec v1ScopeSpec = (V1ScopeSpec) o;
    return Objects.equals(this.isDeleted, v1ScopeSpec.isDeleted)
        && Objects.equals(this.deletionDate, v1ScopeSpec.deletionDate)
        && Objects.equals(this.roles, v1ScopeSpec.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isDeleted, deletionDate, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ScopeSpec {\n");
    sb.append("    isDeleted: ").append(toIndentedString(isDeleted)).append("\n");
    sb.append("    deletionDate: ").append(toIndentedString(deletionDate)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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
