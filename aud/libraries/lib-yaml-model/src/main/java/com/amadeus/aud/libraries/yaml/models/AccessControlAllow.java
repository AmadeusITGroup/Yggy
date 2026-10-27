package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Allow clause may contain list of access rights */
@Schema(description = "Allow clause may contain list of access rights")
@JsonPropertyOrder({AccessControlAllow.JSON_PROPERTY_ACCESS_RIGHTS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessControlAllow {
  public static final String JSON_PROPERTY_ACCESS_RIGHTS = "access_rights";
  private List<@Valid AccessRightsEntry> accessRights;

  public AccessControlAllow() {}

  public AccessControlAllow accessRights(List<@Valid AccessRightsEntry> accessRights) {
    this.accessRights = accessRights;
    return this;
  }

  public AccessControlAllow addAccessRightsItem(AccessRightsEntry accessRightsItem) {
    if (this.accessRights == null) {
      this.accessRights = new ArrayList<>();
    }
    this.accessRights.add(accessRightsItem);
    return this;
  }

  /**
   * the section specifying access rights to allow
   *
   * @return accessRights
   */
  @NotNull
  @Schema(required = true, description = "the section specifying access rights to allow")
  @JsonProperty(value = JSON_PROPERTY_ACCESS_RIGHTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid AccessRightsEntry> getAccessRights() {
    return accessRights;
  }

  @JsonProperty(value = JSON_PROPERTY_ACCESS_RIGHTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAccessRights(List<@Valid AccessRightsEntry> accessRights) {
    this.accessRights = accessRights;
  }

  /** Return true if this AccessControlAllow object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessControlAllow accessControlAllow = (AccessControlAllow) o;
    return Objects.equals(this.accessRights, accessControlAllow.accessRights);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessRights);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessControlAllow {\n");
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
