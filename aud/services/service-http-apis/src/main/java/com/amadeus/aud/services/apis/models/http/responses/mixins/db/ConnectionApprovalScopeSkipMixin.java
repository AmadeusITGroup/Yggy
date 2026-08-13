package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkip;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ConnectionApprovalScopeSkipMixin.JSON_PROPERTY_SKIP_SOURCE,
  ConnectionApprovalScopeSkipMixin.JSON_PROPERTY_SKIP_DESTINATION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ConnectionApprovalScopeSkip.class)
public interface ConnectionApprovalScopeSkipMixin
    extends AbstractBaseMixin<V1ConnectionApprovalScopeSkip> {

  String JSON_PROPERTY_SKIP_SOURCE = "skip_source";
  String JSON_PROPERTY_SKIP_DESTINATION = "skip_destination";

  @JsonProperty(JSON_PROPERTY_SKIP_SOURCE)
  boolean getSkipSource();

  @JsonProperty(JSON_PROPERTY_SKIP_DESTINATION)
  boolean getSkipDestination();

  @JsonIgnore
  String getStatus();
}
