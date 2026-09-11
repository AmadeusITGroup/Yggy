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

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({SecurityRulesResponse.JSON_PROPERTY_SECURITY_RULES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityRulesResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SECURITY_RULES = "security_rules";
  private List<SecurityRule> securityRules;

  public SecurityRulesResponse() {}

  public SecurityRulesResponse securityRules(List<SecurityRule> securityRules) {
    this.securityRules = securityRules;
    return this;
  }

  public SecurityRulesResponse addSecurityRulesItem(SecurityRule securityRulesItem) {
    if (this.securityRules == null) {
      this.securityRules = new ArrayList<>();
    }
    this.securityRules.add(securityRulesItem);
    return this;
  }

  /**
   * List of Security Rules
   *
   * @return securityRules
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "List of Security Rules")
  @JsonProperty(value = JSON_PROPERTY_SECURITY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<SecurityRule> getSecurityRules() {
    return securityRules;
  }

  @JsonProperty(value = JSON_PROPERTY_SECURITY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSecurityRules(List<SecurityRule> securityRules) {
    this.securityRules = securityRules;
  }

  @Override
  public SecurityRulesResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public SecurityRulesResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public SecurityRulesResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public SecurityRulesResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this SecurityRulesResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityRulesResponse securityRulesResponse = (SecurityRulesResponse) o;
    return Objects.equals(this.securityRules, securityRulesResponse.securityRules)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(securityRules, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityRulesResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    securityRules: ").append(toIndentedString(securityRules)).append("\n");
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
