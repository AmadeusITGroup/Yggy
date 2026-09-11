package com.amadeus.aud.libraries.nsg.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

@JsonPropertyOrder({
  ZoneSecurityGroups.JSON_PROPERTY_RULES,
  ZoneSecurityGroups.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ZoneSecurityGroups.JSON_PROPERTY_TAGS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ZoneSecurityGroups(
    @JsonProperty(JSON_PROPERTY_RULES) Map<String, SecurityGroupMap> rules,
    @JsonProperty(JSON_PROPERTY_PROVIDER_SPECIFIC) Object providerSpecific,
    @JsonProperty(JSON_PROPERTY_TAGS) Map<String, String> tags) {
  public static final String JSON_PROPERTY_RULES = "rules";
  public static final String JSON_PROPERTY_PROVIDER_SPECIFIC = "provider_specific";
  public static final String JSON_PROPERTY_TAGS = "tags";
}
