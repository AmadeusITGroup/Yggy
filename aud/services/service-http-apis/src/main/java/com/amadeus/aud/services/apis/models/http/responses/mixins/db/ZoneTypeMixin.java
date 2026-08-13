package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
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
  ZoneTypeMixin.JSON_PROPERTY_IS_TECHNICAL,
  ZoneTypeMixin.JSON_PROPERTY_IS_REAL,
  ZoneTypeMixin.JSON_PROPERTY_IS_PAAS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ZoneType.class)
public interface ZoneTypeMixin extends AbstractBaseMixin<V1ZoneType> {

  String JSON_PROPERTY_IS_TECHNICAL = "is_technical";
  String JSON_PROPERTY_IS_REAL = "is_real";
  String JSON_PROPERTY_IS_PAAS = "is_paas";

  @JsonProperty(JSON_PROPERTY_IS_TECHNICAL)
  boolean isTechnical();

  @JsonProperty(JSON_PROPERTY_IS_REAL)
  boolean isReal();

  @JsonProperty(JSON_PROPERTY_IS_PAAS)
  boolean isPaas();

  @JsonIgnore
  Set<Connection> getConnections();

  @JsonIgnore
  Set<ZoneInstance> getZoneInstances();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  Component getComponent();
}
