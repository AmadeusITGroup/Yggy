package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Access Control entry bearing access right permission and the list of AUD objects Access Control
 * applies to
 */
@Schema(
    description =
        "Access Control entry bearing access right permission and the list of AUD objects Access Control applies to")
@JsonPropertyOrder({
  AccessRightsEntry.JSON_PROPERTY_PERMISSION,
  AccessRightsEntry.JSON_PROPERTY_REPORT_IF_DENIED,
  AccessRightsEntry.JSON_PROPERTY_APPLY_TO
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessRightsEntry {
  /**
   * Permission identifier; can be omitted, empty value defaults to COMPONENT_INHERIT in AUD engine;
   * one of
   */
  public enum PermissionEnum {
    COMPONENT_INHERIT(String.valueOf("COMPONENT_INHERIT")),

    COMPONENT_DEPENDENCY_SRC(String.valueOf("COMPONENT_DEPENDENCY_SRC")),

    COMPONENT_DEPENDENCY_DST(String.valueOf("COMPONENT_DEPENDENCY_DST")),

    EMPTY(String.valueOf(""));

    private String value;

    PermissionEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PermissionEnum fromValue(String value) {
      for (PermissionEnum b : PermissionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PERMISSION = "permission";
  @Nullable private PermissionEnum permission = PermissionEnum.EMPTY;

  public static final String JSON_PROPERTY_REPORT_IF_DENIED = "report_if_denied";
  @Nullable private AccessDenialMessage reportIfDenied;

  public static final String JSON_PROPERTY_APPLY_TO = "apply_to";
  private AccessRightsEntryScopes applyTo;

  public AccessRightsEntry() {}

  public AccessRightsEntry permission(@Nullable PermissionEnum permission) {
    this.permission = permission;
    return this;
  }

  /**
   * Permission identifier; can be omitted, empty value defaults to COMPONENT_INHERIT in AUD engine;
   * one of
   *
   * @return permission
   */
  @Schema(
      required = false,
      description =
          "Permission identifier; can be omitted, empty value defaults to COMPONENT_INHERIT in AUD engine; one of")
  @JsonProperty(value = JSON_PROPERTY_PERMISSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable PermissionEnum getPermission() {
    return permission;
  }

  @JsonProperty(value = JSON_PROPERTY_PERMISSION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPermission(@Nullable PermissionEnum permission) {
    this.permission = permission;
  }

  public AccessRightsEntry reportIfDenied(@Nullable AccessDenialMessage reportIfDenied) {
    this.reportIfDenied = reportIfDenied;
    return this;
  }

  /**
   * Get reportIfDenied
   *
   * @return reportIfDenied
   */
  @Valid
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_REPORT_IF_DENIED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable AccessDenialMessage getReportIfDenied() {
    return reportIfDenied;
  }

  @JsonProperty(value = JSON_PROPERTY_REPORT_IF_DENIED, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setReportIfDenied(@Nullable AccessDenialMessage reportIfDenied) {
    this.reportIfDenied = reportIfDenied;
  }

  public AccessRightsEntry applyTo(AccessRightsEntryScopes applyTo) {
    this.applyTo = applyTo;
    return this;
  }

  /**
   * Get applyTo
   *
   * @return applyTo
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_APPLY_TO, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public AccessRightsEntryScopes getApplyTo() {
    return applyTo;
  }

  @JsonProperty(value = JSON_PROPERTY_APPLY_TO, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApplyTo(AccessRightsEntryScopes applyTo) {
    this.applyTo = applyTo;
  }

  /** Return true if this AccessRightsEntry object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessRightsEntry accessRightsEntry = (AccessRightsEntry) o;
    return Objects.equals(this.permission, accessRightsEntry.permission)
        && Objects.equals(this.reportIfDenied, accessRightsEntry.reportIfDenied)
        && Objects.equals(this.applyTo, accessRightsEntry.applyTo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permission, reportIfDenied, applyTo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessRightsEntry {\n");
    sb.append("    permission: ").append(toIndentedString(permission)).append("\n");
    sb.append("    reportIfDenied: ").append(toIndentedString(reportIfDenied)).append("\n");
    sb.append("    applyTo: ").append(toIndentedString(applyTo)).append("\n");
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
