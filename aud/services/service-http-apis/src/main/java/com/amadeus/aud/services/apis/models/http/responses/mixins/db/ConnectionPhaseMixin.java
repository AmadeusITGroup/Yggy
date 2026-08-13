package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
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
  ConnectionPhaseMixin.JSON_PROPERTY_SOURCE_PHASE,
  ConnectionPhaseMixin.JSON_PROPERTY_DESTINATION_PHASE,
  ConnectionPhaseMixin.JSON_PROPERTY_CONTROL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ConnectionPhase.class)
public interface ConnectionPhaseMixin extends AbstractBaseMixin<V1ConnectionPhase> {

  String JSON_PROPERTY_SOURCE_PHASE = "source_phase";
  String JSON_PROPERTY_DESTINATION_PHASE = "destination_phase";
  String JSON_PROPERTY_CONTROL = "control";

  @JsonIgnore
  MacroPhase getSourcePhase();

  @JsonIgnore
  MacroPhase getDestinationPhase();

  @JsonProperty(JSON_PROPERTY_CONTROL)
  V1ConnectionPhaseSpec.ControlEnum getControl();

  @JsonProperty(JSON_PROPERTY_SOURCE_PHASE)
  String getSourcePhaseName();

  @JsonProperty(JSON_PROPERTY_DESTINATION_PHASE)
  String getDestinationPhaseName();
}
