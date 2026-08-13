package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.V1Continent;
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
  ContinentMixin.JSON_PROPERTY_REGIONS,
  ContinentMixin.JSON_PROPERTY_TARGETS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Continent.class)
public interface ContinentMixin extends AbstractBaseMixin<V1Continent> {

  String JSON_PROPERTY_REGIONS = "regions";
  String JSON_PROPERTY_TARGETS = "targets";

  @JsonProperty(JSON_PROPERTY_REGIONS)
  List<String> getRegionNames();

  @JsonIgnore
  Set<Region> getRegions();

  @JsonProperty(JSON_PROPERTY_TARGETS)
  List<String> getTargetNames();

  @JsonIgnore
  Set<Target> getTargets();
}
