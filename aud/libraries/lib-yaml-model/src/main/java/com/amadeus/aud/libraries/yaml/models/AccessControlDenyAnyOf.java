package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** AccessControlDenyAnyOf */
@JsonPropertyOrder({AccessControlDenyAnyOf.JSON_PROPERTY_ACCESS_RIGHTS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessControlDenyAnyOf {
  public static final String JSON_PROPERTY_ACCESS_RIGHTS = "access_rights";
  @Nullable private List<@Valid AccessRightsEntry> accessRights;

  public AccessControlDenyAnyOf() {}

  public AccessControlDenyAnyOf accessRights(
      @Nullable List<@Valid AccessRightsEntry> accessRights) {
    this.accessRights = accessRights;
    return this;
  }

  public AccessControlDenyAnyOf addAccessRightsItem(AccessRightsEntry accessRightsItem) {
    if (this.accessRights == null) {
      this.accessRights = new ArrayList<>();
    }
    this.accessRights.add(accessRightsItem);
    return this;
  }

  /**
   * the section specifying access rights to deny
   *
   * @return accessRights
   */
  @Valid
  @Schema(required = false, description = "the section specifying access rights to deny")
  @JsonProperty(value = JSON_PROPERTY_ACCESS_RIGHTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<@Valid AccessRightsEntry> getAccessRights() {
    return accessRights;
  }

  @JsonProperty(value = JSON_PROPERTY_ACCESS_RIGHTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAccessRights(@Nullable List<@Valid AccessRightsEntry> accessRights) {
    this.accessRights = accessRights;
  }

  /** Return true if this AccessControlDeny_anyOf object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessControlDenyAnyOf accessControlDenyAnyOf = (AccessControlDenyAnyOf) o;
    return Objects.equals(this.accessRights, accessControlDenyAnyOf.accessRights);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessRights);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessControlDenyAnyOf {\n");
    sb.append("    accessRights: ").append(toIndentedString(accessRights)).append("\n");
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
