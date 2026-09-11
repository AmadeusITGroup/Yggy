package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Component specification */
@Schema(description = "Component specification")
@JsonPropertyOrder({
  V1ComponentSpec.JSON_PROPERTY_IS_HOSTING,
  V1ComponentSpec.JSON_PROPERTY_IS_CONTAINER,
  V1ComponentSpec.JSON_PROPERTY_IS_TECHNICAL,
  V1ComponentSpec.JSON_PROPERTY_IS_PUBLIC,
  V1ComponentSpec.JSON_PROPERTY_IS_PRIVATE,
  V1ComponentSpec.JSON_PROPERTY_IS_PUBLIC_INHERITANCE,
  V1ComponentSpec.JSON_PROPERTY_IS_PUBLIC_DEPENDENCY,
  V1ComponentSpec.JSON_PROPERTY_DEPRECATED_STATUS,
  V1ComponentSpec.JSON_PROPERTY_DEPRECATED_MESSAGE,
  V1ComponentSpec.JSON_PROPERTY_PARENTS,
  V1ComponentSpec.JSON_PROPERTY_ACCESS_CONTROL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ComponentSpec {
  public static final String JSON_PROPERTY_IS_HOSTING = "isHosting";
  @Nullable private Boolean isHosting = false;

  public static final String JSON_PROPERTY_IS_CONTAINER = "isContainer";
  @Nullable private Boolean isContainer = false;

  public static final String JSON_PROPERTY_IS_TECHNICAL = "isTechnical";
  @Nullable private Boolean isTechnical = false;

  public static final String JSON_PROPERTY_IS_PUBLIC = "isPublic";
  @Nullable private Boolean isPublic = false;

  public static final String JSON_PROPERTY_IS_PRIVATE = "isPrivate";
  @Nullable private Boolean isPrivate = false;

  public static final String JSON_PROPERTY_IS_PUBLIC_INHERITANCE = "isPublicInheritance";
  @Nullable private Boolean isPublicInheritance = false;

  public static final String JSON_PROPERTY_IS_PUBLIC_DEPENDENCY = "isPublicDependency";
  @Nullable private Boolean isPublicDependency = false;

  public static final String JSON_PROPERTY_DEPRECATED_STATUS = "deprecatedStatus";
  @Nullable private DeprecatedStatus deprecatedStatus = DeprecatedStatus.NONE;

  public static final String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecatedMessage";
  @Nullable private String deprecatedMessage;

  public static final String JSON_PROPERTY_PARENTS = "parents";
  private List<String> parents;

  public static final String JSON_PROPERTY_ACCESS_CONTROL = "access_control";
  @Nullable private AccessControl accessControl;

  public V1ComponentSpec() {}

  public V1ComponentSpec isHosting(@Nullable Boolean isHosting) {
    this.isHosting = isHosting;
    return this;
  }

  /**
   * is it an hosting platform?
   *
   * @return isHosting
   */
  @Schema(required = false, description = "is it an hosting platform?")
  @JsonProperty(value = JSON_PROPERTY_IS_HOSTING, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsHosting() {
    return isHosting;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_HOSTING, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsHosting(@Nullable Boolean isHosting) {
    this.isHosting = isHosting;
  }

  public V1ComponentSpec isContainer(@Nullable Boolean isContainer) {
    this.isContainer = isContainer;
    return this;
  }

  /**
   * is application or service hosted in a hosting platform (e.g. Hadoop, Paas, etc.)
   *
   * @return isContainer
   */
  @Schema(
      required = false,
      description =
          "is application or service hosted in a hosting platform (e.g. Hadoop, Paas, etc.)")
  @JsonProperty(value = JSON_PROPERTY_IS_CONTAINER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsContainer() {
    return isContainer;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_CONTAINER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsContainer(@Nullable Boolean isContainer) {
    this.isContainer = isContainer;
  }

  public V1ComponentSpec isTechnical(@Nullable Boolean isTechnical) {
    this.isTechnical = isTechnical;
    return this;
  }

  /**
   * is it used for referencing/inheriting only?
   *
   * @return isTechnical
   */
  @Schema(required = false, description = "is it used for referencing/inheriting only?")
  @JsonProperty(value = JSON_PROPERTY_IS_TECHNICAL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTechnical() {
    return isTechnical;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TECHNICAL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTechnical(@Nullable Boolean isTechnical) {
    this.isTechnical = isTechnical;
  }

  public V1ComponentSpec isPublic(@Nullable Boolean isPublic) {
    this.isPublic = isPublic;
    return this;
  }

  /**
   * This attribute controls the permission to instantiate your component. Set it to true if other
   * teams should be allowed to instantiate your component. (eg. Internet URL component, part of
   * Internet scope, can be instantiated in any scope, to define connectivity from this scope to
   * Internet URL) By default (false value), nobody can instantiate your component outside your
   * scope. / ! \\ is_public is not the opposite of is_private!
   *
   * @return isPublic
   */
  @Schema(
      required = false,
      description =
          """
  This attribute controls the permission to instantiate your component.
  Set it to true if other teams should be allowed to instantiate your component. (eg. Internet URL component, part of Internet scope, can be instantiated in any scope, to define connectivity from this scope to Internet URL)
  By default (false value), nobody can instantiate your component outside your scope.
  ! \\ is_public is not the opposite of is_private!""")
  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPublic() {
    return isPublic;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPublic(@Nullable Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public V1ComponentSpec isPrivate(@Nullable Boolean isPrivate) {
    this.isPrivate = isPrivate;
    return this;
  }

  /**
   * This attribute controls the permission to set your component as a target of a dependency, from
   * another scope than yours. Set it to true, if you want to limit the usage of this component as
   * target, to your scope only. By default (false value), everyone can connect to your component. /
   * ! \\ is_private is not the opposite of is_public!
   *
   * @return isPrivate
   */
  @Schema(
      required = false,
      description =
          """
  This attribute controls the permission to set your component as a target of a dependency, from another scope than yours.
  Set it to true, if you want to limit the usage of this component as target, to your scope only.
  By default (false value), everyone can connect to your component.

  ! \\ is_private is not the opposite of is_public!""")
  @JsonProperty(value = JSON_PROPERTY_IS_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPrivate() {
    return isPrivate;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPrivate(@Nullable Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public V1ComponentSpec isPublicInheritance(@Nullable Boolean isPublicInheritance) {
    this.isPublicInheritance = isPublicInheritance;
    return this;
  }

  /**
   * Other team can inherit from this component ?
   *
   * @return isPublicInheritance
   */
  @Schema(required = false, description = "Other team can inherit from this component ?")
  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC_INHERITANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPublicInheritance() {
    return isPublicInheritance;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC_INHERITANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPublicInheritance(@Nullable Boolean isPublicInheritance) {
    this.isPublicInheritance = isPublicInheritance;
  }

  public V1ComponentSpec isPublicDependency(@Nullable Boolean isPublicDependency) {
    this.isPublicDependency = isPublicDependency;
    return this;
  }

  /**
   * Other team can create dependency from this component ? If true, dependencies from other scopes
   * can have your component as a source.
   *
   * @return isPublicDependency
   */
  @Schema(
      required = false,
      description =
          """
  Other team can create dependency from this component?
  If true, dependencies from other scopes can have your component as a source.""")
  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC_DEPENDENCY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPublicDependency() {
    return isPublicDependency;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PUBLIC_DEPENDENCY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPublicDependency(@Nullable Boolean isPublicDependency) {
    this.isPublicDependency = isPublicDependency;
  }

  public V1ComponentSpec deprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
    return this;
  }

  /**
   * Get deprecatedStatus
   *
   * @return deprecatedStatus
   */
  @Valid
  @Schema(required = false, description = "Deprecated status of the component.")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return deprecatedStatus;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public V1ComponentSpec deprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
    return this;
  }

  /**
   * Additional info on the deprecated component (e.g. replacement information/ owner contact
   * details)
   *
   * @return deprecatedMessage
   */
  @Schema(
      required = false,
      description =
          "Additional info on the deprecated component (e.g. replacement information/ owner contact details)")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeprecatedMessage() {
    return deprecatedMessage;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
  }

  public V1ComponentSpec parents(List<String> parents) {
    this.parents = parents;
    return this;
  }

  public V1ComponentSpec addParentsItem(String parentsItem) {
    if (this.parents == null) {
      this.parents = new ArrayList<>();
    }
    this.parents.add(parentsItem);
    return this;
  }

  /**
   * List of Parents
   *
   * @return parents
   */
  @NotNull
  @Schema(required = true, description = "List of Parents")
  @JsonProperty(value = JSON_PROPERTY_PARENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getParents() {
    return parents;
  }

  @JsonProperty(value = JSON_PROPERTY_PARENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setParents(List<String> parents) {
    this.parents = parents;
  }

  public V1ComponentSpec accessControl(@Nullable AccessControl accessControl) {
    this.accessControl = accessControl;
    return this;
  }

  /**
   * Get accessControl
   *
   * @return accessControl
   */
  @Valid
  @Schema(required = false, description = "Access control configuration for the component.")
  @JsonProperty(value = JSON_PROPERTY_ACCESS_CONTROL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable AccessControl getAccessControl() {
    return accessControl;
  }

  @JsonProperty(value = JSON_PROPERTY_ACCESS_CONTROL, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAccessControl(@Nullable AccessControl accessControl) {
    this.accessControl = accessControl;
  }

  /** Return true if this v1.ComponentSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ComponentSpec v1ComponentSpec = (V1ComponentSpec) o;
    return Objects.equals(this.isHosting, v1ComponentSpec.isHosting)
        && Objects.equals(this.isContainer, v1ComponentSpec.isContainer)
        && Objects.equals(this.isTechnical, v1ComponentSpec.isTechnical)
        && Objects.equals(this.isPublic, v1ComponentSpec.isPublic)
        && Objects.equals(this.isPrivate, v1ComponentSpec.isPrivate)
        && Objects.equals(this.isPublicInheritance, v1ComponentSpec.isPublicInheritance)
        && Objects.equals(this.isPublicDependency, v1ComponentSpec.isPublicDependency)
        && Objects.equals(this.deprecatedStatus, v1ComponentSpec.deprecatedStatus)
        && Objects.equals(this.deprecatedMessage, v1ComponentSpec.deprecatedMessage)
        && Objects.equals(this.parents, v1ComponentSpec.parents)
        && Objects.equals(this.accessControl, v1ComponentSpec.accessControl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        isHosting,
        isContainer,
        isTechnical,
        isPublic,
        isPrivate,
        isPublicInheritance,
        isPublicDependency,
        deprecatedStatus,
        deprecatedMessage,
        parents,
        accessControl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ComponentSpec {\n");
    sb.append("    isHosting: ").append(toIndentedString(isHosting)).append("\n");
    sb.append("    isContainer: ").append(toIndentedString(isContainer)).append("\n");
    sb.append("    isTechnical: ").append(toIndentedString(isTechnical)).append("\n");
    sb.append("    isPublic: ").append(toIndentedString(isPublic)).append("\n");
    sb.append("    isPrivate: ").append(toIndentedString(isPrivate)).append("\n");
    sb.append("    isPublicInheritance: ")
        .append(toIndentedString(isPublicInheritance))
        .append("\n");
    sb.append("    isPublicDependency: ").append(toIndentedString(isPublicDependency)).append("\n");
    sb.append("    deprecatedStatus: ").append(toIndentedString(deprecatedStatus)).append("\n");
    sb.append("    deprecatedMessage: ").append(toIndentedString(deprecatedMessage)).append("\n");
    sb.append("    parents: ").append(toIndentedString(parents)).append("\n");
    sb.append("    accessControl: ").append(toIndentedString(accessControl)).append("\n");
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
