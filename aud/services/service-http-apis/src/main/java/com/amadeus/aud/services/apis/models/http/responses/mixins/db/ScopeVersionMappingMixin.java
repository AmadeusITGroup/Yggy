package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;

@JsonPropertyOrder({
  ScopeVersionMappingMixin.JSON_PROPERTY_SCOPE_VERSION,
  ScopeVersionMappingMixin.JSON_PROPERTY_AUD_VERSION,
  ScopeVersionMappingMixin.JSON_PROPERTY_VERSION_DATETIME
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ScopeVersionMapping.class)
public interface ScopeVersionMappingMixin extends AbstractBaseMixin<V1ScopeVersionMapping> {

  String JSON_PROPERTY_SCOPE_VERSION = "scope_version";
  String JSON_PROPERTY_AUD_VERSION = "aud_version";
  String JSON_PROPERTY_VERSION_DATETIME = "version_datetime";

  @JsonProperty(JSON_PROPERTY_SCOPE_VERSION)
  String getScopeVersion();

  @JsonProperty(JSON_PROPERTY_AUD_VERSION)
  String getAudVersion();

  @JsonProperty(JSON_PROPERTY_VERSION_DATETIME)
  String getVersionDateTime();
}
