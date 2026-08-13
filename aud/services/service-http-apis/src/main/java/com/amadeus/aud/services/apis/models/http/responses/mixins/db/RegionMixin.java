package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.yaml.models.V1Region;
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
  RegionMixin.JSON_PROPERTY_LANDING_ZONES,
  RegionMixin.JSON_PROPERTY_PROVIDER,
  RegionMixin.JSON_PROPERTY_CONTINENT
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Region.class)
public interface RegionMixin extends AbstractBaseMixin<V1Region> {

  String JSON_PROPERTY_LANDING_ZONES = "landing_zones";
  String JSON_PROPERTY_PROVIDER = "provider";
  String JSON_PROPERTY_CONTINENT = "continent";

  @JsonProperty(JSON_PROPERTY_LANDING_ZONES)
  List<String> getLandingZoneNames();

  @JsonIgnore
  Set<LandingZone> getLandingZones();

  @JsonProperty(JSON_PROPERTY_PROVIDER)
  String getProviderName();

  @JsonProperty(JSON_PROPERTY_CONTINENT)
  String getContinentName();

  @JsonIgnore
  Provider getProvider();

  @JsonIgnore
  Continent getContinent();
}
