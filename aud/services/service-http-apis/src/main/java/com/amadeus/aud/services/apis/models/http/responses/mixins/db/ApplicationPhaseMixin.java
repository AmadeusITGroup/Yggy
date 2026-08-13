package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ApplicationPhaseMixin.JSON_PROPERTY_IS_CUSTOMER_FACING,
  ApplicationPhaseMixin.JSON_PROPERTY_MACRO_PHASE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ApplicationPhase.class)
public interface ApplicationPhaseMixin extends AbstractBaseMixin<V1ApplicationPhase> {

  String JSON_PROPERTY_MACRO_PHASE = "macro_phase";
  String JSON_PROPERTY_IS_CUSTOMER_FACING = "is_customer_facing";

  @JsonProperty(JSON_PROPERTY_MACRO_PHASE)
  String getMacroPhaseName();

  @JsonProperty(JSON_PROPERTY_IS_CUSTOMER_FACING)
  boolean isCustomerFacing();

  @JsonIgnore
  MacroPhase getMacroPhase();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();
}
