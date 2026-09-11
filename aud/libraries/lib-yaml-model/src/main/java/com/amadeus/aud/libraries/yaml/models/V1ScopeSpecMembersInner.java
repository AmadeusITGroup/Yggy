package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1ScopeSpecMembersInner */
@JsonPropertyOrder({
  V1ScopeSpecMembersInner.JSON_PROPERTY_NAME,
  V1ScopeSpecMembersInner.JSON_PROPERTY_EMAIL,
  V1ScopeSpecMembersInner.JSON_PROPERTY_P_ACCOUNT
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ScopeSpecMembersInner {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_EMAIL = "email";
  private String email;

  public static final String JSON_PROPERTY_P_ACCOUNT = "p_account";
  private String pAccount;

  public V1ScopeSpecMembersInner() {}

  public V1ScopeSpecMembersInner name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Schema(required = true, description = "Member name.")
  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(String name) {
    this.name = name;
  }

  public V1ScopeSpecMembersInner email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   *
   * @return email
   */
  @NotNull
  @Schema(required = true, description = "Member email address.")
  @JsonProperty(value = JSON_PROPERTY_EMAIL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEmail() {
    return email;
  }

  @JsonProperty(value = JSON_PROPERTY_EMAIL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEmail(String email) {
    this.email = email;
  }

  public V1ScopeSpecMembersInner pAccount(String pAccount) {
    this.pAccount = pAccount;
    return this;
  }

  /**
   * Get pAccount
   *
   * @return pAccount
   */
  @NotNull
  @Schema(required = true, description = "Member account identifier.")
  @JsonProperty(value = JSON_PROPERTY_P_ACCOUNT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getpAccount() {
    return pAccount;
  }

  @JsonProperty(value = JSON_PROPERTY_P_ACCOUNT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setpAccount(String pAccount) {
    this.pAccount = pAccount;
  }

  /** Return true if this v1_ScopeSpec_members_inner object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ScopeSpecMembersInner v1ScopeSpecMembersInner = (V1ScopeSpecMembersInner) o;
    return Objects.equals(this.name, v1ScopeSpecMembersInner.name)
        && Objects.equals(this.email, v1ScopeSpecMembersInner.email)
        && Objects.equals(this.pAccount, v1ScopeSpecMembersInner.pAccount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, pAccount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ScopeSpecMembersInner {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    pAccount: ").append(toIndentedString(pAccount)).append("\n");
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
