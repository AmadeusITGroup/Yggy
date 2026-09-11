package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Access Control specification on AUD object or on its scope; may include &#x60;deny&#x60; and
 * &#x60;allow&#x60; sections; &#x60;deny&#x60; always takes precedence over &#x60;allow&#x60;
 */
@Schema(
    description =
        "Access Control specification on AUD object or on its scope; may include `deny` and `allow` sections; `deny` always takes precedence over `allow`")
@JsonPropertyOrder({AccessControl.JSON_PROPERTY_DENY, AccessControl.JSON_PROPERTY_ALLOW})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccessControl {
  public static final String JSON_PROPERTY_DENY = "deny";
  @Nullable private AccessControlDeny deny;

  public static final String JSON_PROPERTY_ALLOW = "allow";
  @Nullable private AccessControlAllow allow;

  public AccessControl() {}

  public AccessControl deny(@Nullable AccessControlDeny deny) {
    this.deny = deny;
    return this;
  }

  /**
   * Get deny
   *
   * @return deny
   */
  @Valid
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_DENY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable AccessControlDeny getDeny() {
    return deny;
  }

  @JsonProperty(value = JSON_PROPERTY_DENY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeny(@Nullable AccessControlDeny deny) {
    this.deny = deny;
  }

  public AccessControl allow(@Nullable AccessControlAllow allow) {
    this.allow = allow;
    return this;
  }

  /**
   * Get allow
   *
   * @return allow
   */
  @Valid
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_ALLOW, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable AccessControlAllow getAllow() {
    return allow;
  }

  @JsonProperty(value = JSON_PROPERTY_ALLOW, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAllow(@Nullable AccessControlAllow allow) {
    this.allow = allow;
  }

  /** Return true if this AccessControl object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccessControl accessControl = (AccessControl) o;
    return Objects.equals(this.deny, accessControl.deny)
        && Objects.equals(this.allow, accessControl.allow);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deny, allow);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccessControl {\n");
    sb.append("    deny: ").append(toIndentedString(deny)).append("\n");
    sb.append("    allow: ").append(toIndentedString(allow)).append("\n");
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
