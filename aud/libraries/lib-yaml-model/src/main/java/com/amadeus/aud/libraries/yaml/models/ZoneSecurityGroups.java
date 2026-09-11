package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ZoneSecurityGroups */
@JsonPropertyOrder({
  ZoneSecurityGroups.JSON_PROPERTY_RULES,
  ZoneSecurityGroups.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ZoneSecurityGroups.JSON_PROPERTY_TAGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ZoneSecurityGroups {
  public static final String JSON_PROPERTY_RULES = "rules";
  private Map<String, SecurityGroupMap> rules;

  public static final String JSON_PROPERTY_PROVIDER_SPECIFIC = "provider_specific";
  @Nullable private Object providerSpecific;

  public static final String JSON_PROPERTY_TAGS = "tags";
  private Map<String, String> tags;

  public ZoneSecurityGroups() {}

  public ZoneSecurityGroups rules(Map<String, SecurityGroupMap> rules) {
    this.rules = rules;
    return this;
  }

  public ZoneSecurityGroups putRulesItem(String key, SecurityGroupMap rulesItem) {
    if (this.rules == null) {
      this.rules = new HashMap<>();
    }
    this.rules.put(key, rulesItem);
    return this;
  }

  /**
   * Get rules
   *
   * @return rules
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Map<String, SecurityGroupMap> getRules() {
    return rules;
  }

  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRules(Map<String, SecurityGroupMap> rules) {
    this.rules = rules;
  }

  public ZoneSecurityGroups providerSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
    return this;
  }

  /**
   * Get providerSpecific
   *
   * @return providerSpecific
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getProviderSpecific() {
    return providerSpecific;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER_SPECIFIC, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProviderSpecific(@Nullable Object providerSpecific) {
    this.providerSpecific = providerSpecific;
  }

  public ZoneSecurityGroups tags(Map<String, String> tags) {
    this.tags = tags;
    return this;
  }

  public ZoneSecurityGroups putTagsItem(String key, String tagsItem) {
    if (this.tags == null) {
      this.tags = new HashMap<>();
    }
    this.tags.put(key, tagsItem);
    return this;
  }

  /**
   * Get tags
   *
   * @return tags
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TAGS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Map<String, String> getTags() {
    return tags;
  }

  @JsonProperty(value = JSON_PROPERTY_TAGS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTags(Map<String, String> tags) {
    this.tags = tags;
  }

  /** Return true if this ZoneSecurityGroups object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ZoneSecurityGroups zoneSecurityGroups = (ZoneSecurityGroups) o;
    return Objects.equals(this.rules, zoneSecurityGroups.rules)
        && Objects.equals(this.providerSpecific, zoneSecurityGroups.providerSpecific)
        && Objects.equals(this.tags, zoneSecurityGroups.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rules, providerSpecific, tags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ZoneSecurityGroups {\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    providerSpecific: ").append(toIndentedString(providerSpecific)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
