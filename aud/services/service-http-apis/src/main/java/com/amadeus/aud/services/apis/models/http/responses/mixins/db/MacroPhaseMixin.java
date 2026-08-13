package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  MacroPhaseMixin.JSON_PROPERTY_APPLICATION_PHASES,
  MacroPhaseMixin.JSON_PROPERTY_TARGETS,
  MacroPhaseMixin.JSON_PROPERTY_CONNECTION_PHASES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(MacroPhase.class)
public interface MacroPhaseMixin extends AbstractBaseMixin<V1MacroPhase> {

  String JSON_PROPERTY_APPLICATION_PHASES = "application_phases";
  String JSON_PROPERTY_TARGETS = "targets";
  String JSON_PROPERTY_CONNECTION_PHASES = "connection_phases";

  @JsonProperty(JSON_PROPERTY_APPLICATION_PHASES)
  List<String> getApplicationPhaseNames();

  @JsonIgnore
  Set<ApplicationPhase> getApplicationPhases();

  @JsonProperty(JSON_PROPERTY_TARGETS)
  List<String> getTargetNames();

  @JsonIgnore
  Set<Target> getTargets();

  @JsonIgnore
  Set<ConnectionPhase> getConnectionPhases();

  @JsonIgnore
  Integer getAutomationOrder();
}
