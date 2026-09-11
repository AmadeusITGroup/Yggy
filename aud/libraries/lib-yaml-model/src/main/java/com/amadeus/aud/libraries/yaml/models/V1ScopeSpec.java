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

/** Scope specification */
@Schema(description = "Scope specification")
@JsonPropertyOrder({
  V1ScopeSpec.JSON_PROPERTY_IS_DELETED,
  V1ScopeSpec.JSON_PROPERTY_DELETION_DATE,
  V1ScopeSpec.JSON_PROPERTY_ASSIGNEE_GROUP,
  V1ScopeSpec.JSON_PROPERTY_NANO_I_D,
  V1ScopeSpec.JSON_PROPERTY_ADMIN_GROUPS,
  V1ScopeSpec.JSON_PROPERTY_ROLES,
  V1ScopeSpec.JSON_PROPERTY_GOVERNANCE_ACTIVATION_LEVEL,
  V1ScopeSpec.JSON_PROPERTY_OWNERS,
  V1ScopeSpec.JSON_PROPERTY_MEMBERS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ScopeSpec {
  public static final String JSON_PROPERTY_IS_DELETED = "is_deleted";
  @Nullable private Boolean isDeleted;

  public static final String JSON_PROPERTY_DELETION_DATE = "deletion_date";
  @Nullable private String deletionDate;

  public static final String JSON_PROPERTY_ASSIGNEE_GROUP = "assigneeGroup";
  @Nullable private String assigneeGroup;

  public static final String JSON_PROPERTY_NANO_I_D = "nanoID";
  @Nullable private String nanoID;

  public static final String JSON_PROPERTY_ADMIN_GROUPS = "adminGroups";
  @Nullable private List<String> adminGroups;

  public static final String JSON_PROPERTY_ROLES = "roles";
  @Nullable private List<String> roles;

  public static final String JSON_PROPERTY_GOVERNANCE_ACTIVATION_LEVEL =
      "governance_activation_level";
  @Nullable private Integer governanceActivationLevel;

  public static final String JSON_PROPERTY_OWNERS = "owners";
  @Nullable private List<String> owners;

  public static final String JSON_PROPERTY_MEMBERS = "members";
  @Nullable private List<@Valid V1ScopeSpecMembersInner> members;

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
  @Schema(required = false, description = "scope deleted Boolean")
  @JsonProperty(value = JSON_PROPERTY_IS_DELETED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsDeleted() {
    return isDeleted;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_DELETED, required = false)
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
  @Schema(required = false, description = "Date when the scope was deleted.")
  @JsonProperty(value = JSON_PROPERTY_DELETION_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeletionDate() {
    return deletionDate;
  }

  @JsonProperty(value = JSON_PROPERTY_DELETION_DATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeletionDate(@Nullable String deletionDate) {
    this.deletionDate = deletionDate;
  }

  public V1ScopeSpec assigneeGroup(@Nullable String assigneeGroup) {
    this.assigneeGroup = assigneeGroup;
    return this;
  }

  /**
   * WinAProach Assignee Group
   *
   * @return assigneeGroup
   */
  @Schema(required = false, description = "WinAProach Assignee Group")
  @JsonProperty(value = JSON_PROPERTY_ASSIGNEE_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getAssigneeGroup() {
    return assigneeGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_ASSIGNEE_GROUP, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssigneeGroup(@Nullable String assigneeGroup) {
    this.assigneeGroup = assigneeGroup;
  }

  public V1ScopeSpec nanoID(@Nullable String nanoID) {
    this.nanoID = nanoID;
    return this;
  }

  /**
   * NanoID
   *
   * @return nanoID
   */
  @Schema(required = false, description = "NanoID")
  @JsonProperty(value = JSON_PROPERTY_NANO_I_D, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getNanoID() {
    return nanoID;
  }

  @JsonProperty(value = JSON_PROPERTY_NANO_I_D, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNanoID(@Nullable String nanoID) {
    this.nanoID = nanoID;
  }

  public V1ScopeSpec adminGroups(@Nullable List<String> adminGroups) {
    this.adminGroups = adminGroups;
    return this;
  }

  public V1ScopeSpec addAdminGroupsItem(String adminGroupsItem) {
    if (this.adminGroups == null) {
      this.adminGroups = new ArrayList<>();
    }
    this.adminGroups.add(adminGroupsItem);
    return this;
  }

  /**
   * MUCMSPDOM Admin Groups
   *
   * @return adminGroups
   */
  @Schema(required = false, description = "MUCMSPDOM Admin Groups")
  @JsonProperty(value = JSON_PROPERTY_ADMIN_GROUPS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getAdminGroups() {
    return adminGroups;
  }

  @JsonProperty(value = JSON_PROPERTY_ADMIN_GROUPS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAdminGroups(@Nullable List<String> adminGroups) {
    this.adminGroups = adminGroups;
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
  @Schema(required = false, description = "List of roles authorized in the scope")
  @JsonProperty(value = JSON_PROPERTY_ROLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getRoles() {
    return roles;
  }

  @JsonProperty(value = JSON_PROPERTY_ROLES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRoles(@Nullable List<String> roles) {
    this.roles = roles;
  }

  public V1ScopeSpec governanceActivationLevel(@Nullable Integer governanceActivationLevel) {
    this.governanceActivationLevel = governanceActivationLevel;
    return this;
  }

  /**
   * Governance activation level for the scope. Values 1 to 4. 1-&gt; Default. 4-&gt; Connectivity
   * Enabled
   *
   * @return governanceActivationLevel
   */
  @Schema(
      required = false,
      description =
          "Governance activation level for the scope. Values 1 to 4. 1-> Default.  4-> Connectivity Enabled")
  @JsonProperty(value = JSON_PROPERTY_GOVERNANCE_ACTIVATION_LEVEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Integer getGovernanceActivationLevel() {
    return governanceActivationLevel;
  }

  @JsonProperty(value = JSON_PROPERTY_GOVERNANCE_ACTIVATION_LEVEL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setGovernanceActivationLevel(@Nullable Integer governanceActivationLevel) {
    this.governanceActivationLevel = governanceActivationLevel;
  }

  public V1ScopeSpec owners(@Nullable List<String> owners) {
    this.owners = owners;
    return this;
  }

  public V1ScopeSpec addOwnersItem(String ownersItem) {
    if (this.owners == null) {
      this.owners = new ArrayList<>();
    }
    this.owners.add(ownersItem);
    return this;
  }

  /**
   * Owner of admin group
   *
   * @return owners
   */
  @Schema(required = false, description = "Owner of admin group")
  @JsonProperty(value = JSON_PROPERTY_OWNERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getOwners() {
    return owners;
  }

  @JsonProperty(value = JSON_PROPERTY_OWNERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOwners(@Nullable List<String> owners) {
    this.owners = owners;
  }

  public V1ScopeSpec members(@Nullable List<@Valid V1ScopeSpecMembersInner> members) {
    this.members = members;
    return this;
  }

  public V1ScopeSpec addMembersItem(V1ScopeSpecMembersInner membersItem) {
    if (this.members == null) {
      this.members = new ArrayList<>();
    }
    this.members.add(membersItem);
    return this;
  }

  /**
   * Members of admin group
   *
   * @return members
   */
  @Valid
  @Schema(required = false, description = "Members of admin group")
  @JsonProperty(value = JSON_PROPERTY_MEMBERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<@Valid V1ScopeSpecMembersInner> getMembers() {
    return members;
  }

  @JsonProperty(value = JSON_PROPERTY_MEMBERS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMembers(@Nullable List<@Valid V1ScopeSpecMembersInner> members) {
    this.members = members;
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
        && Objects.equals(this.assigneeGroup, v1ScopeSpec.assigneeGroup)
        && Objects.equals(this.nanoID, v1ScopeSpec.nanoID)
        && Objects.equals(this.adminGroups, v1ScopeSpec.adminGroups)
        && Objects.equals(this.roles, v1ScopeSpec.roles)
        && Objects.equals(this.governanceActivationLevel, v1ScopeSpec.governanceActivationLevel)
        && Objects.equals(this.owners, v1ScopeSpec.owners)
        && Objects.equals(this.members, v1ScopeSpec.members);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isDeleted,
        deletionDate,
        assigneeGroup,
        nanoID,
        adminGroups,
        roles,
        governanceActivationLevel,
        owners,
        members);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ScopeSpec {\n");
    sb.append("    isDeleted: ").append(toIndentedString(isDeleted)).append("\n");
    sb.append("    deletionDate: ").append(toIndentedString(deletionDate)).append("\n");
    sb.append("    assigneeGroup: ").append(toIndentedString(assigneeGroup)).append("\n");
    sb.append("    nanoID: ").append(toIndentedString(nanoID)).append("\n");
    sb.append("    adminGroups: ").append(toIndentedString(adminGroups)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    governanceActivationLevel: ")
        .append(toIndentedString(governanceActivationLevel))
        .append("\n");
    sb.append("    owners: ").append(toIndentedString(owners)).append("\n");
    sb.append("    members: ").append(toIndentedString(members)).append("\n");
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
