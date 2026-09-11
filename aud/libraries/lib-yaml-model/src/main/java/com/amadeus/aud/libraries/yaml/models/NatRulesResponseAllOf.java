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

/** NatRulesResponseAllOf */
@JsonPropertyOrder({NatRulesResponseAllOf.JSON_PROPERTY_NAT_RULES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NatRulesResponseAllOf {
  public static final String JSON_PROPERTY_NAT_RULES = "nat_rules";
  @Nullable private List<NatRules> natRules;

  public NatRulesResponseAllOf() {}

  public NatRulesResponseAllOf natRules(@Nullable List<NatRules> natRules) {
    this.natRules = natRules;
    return this;
  }

  public NatRulesResponseAllOf addNatRulesItem(NatRules natRulesItem) {
    if (this.natRules == null) {
      this.natRules = new ArrayList<>();
    }
    this.natRules.add(natRulesItem);
    return this;
  }

  /**
   * List of NAT Policies
   *
   * @return natRules
   */
  @Valid
  @Schema(required = false, description = "List of NAT Policies")
  @JsonProperty(value = JSON_PROPERTY_NAT_RULES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<NatRules> getNatRules() {
    return natRules;
  }

  @JsonProperty(value = JSON_PROPERTY_NAT_RULES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNatRules(@Nullable List<NatRules> natRules) {
    this.natRules = natRules;
  }

  /** Return true if this NatRulesResponse_allOf object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NatRulesResponseAllOf natRulesResponseAllOf = (NatRulesResponseAllOf) o;
    return Objects.equals(this.natRules, natRulesResponseAllOf.natRules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(natRules);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NatRulesResponseAllOf {\n");
    sb.append("    natRules: ").append(toIndentedString(natRules)).append("\n");
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
