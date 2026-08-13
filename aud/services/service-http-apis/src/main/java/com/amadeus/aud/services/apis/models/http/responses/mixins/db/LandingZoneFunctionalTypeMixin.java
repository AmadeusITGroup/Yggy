package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneFunctionalType;
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
  LandingZoneFunctionalTypeMixin.JSON_PROPERTY_LANDING_ZONES,
  LandingZoneFunctionalTypeMixin.JSON_PROPERTY_IS_GENERATED
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(LandingZoneFunctionalType.class)
public interface LandingZoneFunctionalTypeMixin
    extends AbstractBaseMixin<V1LandingzoneFunctionalType> {

  String JSON_PROPERTY_LANDING_ZONES = "landing_zones";
  String JSON_PROPERTY_IS_GENERATED = "is_generated";

  @JsonProperty(JSON_PROPERTY_IS_GENERATED)
  boolean isGenerated();

  @JsonProperty(JSON_PROPERTY_LANDING_ZONES)
  List<String> getLandingZoneNames();

  @JsonIgnore
  Set<LandingZone> getLandingZones();
}
