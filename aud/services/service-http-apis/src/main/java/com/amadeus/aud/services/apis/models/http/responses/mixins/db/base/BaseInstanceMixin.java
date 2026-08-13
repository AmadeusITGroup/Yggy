package com.amadeus.aud.services.apis.models.http.responses.mixins.db.base;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.time.LocalDateTime;
import java.util.Map;

@JsonPropertyOrder({
  BaseInstanceMixin.JSON_PROPERTY_LANDING_ZONE,
  BaseInstanceMixin.JSON_PROPERTY_CREATION_DATE,
  BaseInstanceMixin.JSON_PROPERTY_CREATION_VERSION,
  BaseInstanceMixin.JSON_PROPERTY_UPDATE_DATE,
  BaseInstanceMixin.JSON_PROPERTY_UPDATE_VERSION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(BaseInstance.class)
public interface BaseInstanceMixin<T> extends AbstractBaseMixin<T> {
  String JSON_PROPERTY_CREATION_DATE = "creation_date";
  String JSON_PROPERTY_CREATION_VERSION = "creation_version";
  String JSON_PROPERTY_UPDATE_DATE = "update_date";
  String JSON_PROPERTY_UPDATE_VERSION = "update_version";
  String JSON_PROPERTY_LANDING_ZONE = "landing_zone";

  String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  @JsonProperty(JSON_PROPERTY_CREATION_DATE)
  @JsonFormat(pattern = DATE_FORMAT)
  LocalDateTime getCreationDate();

  @JsonProperty(JSON_PROPERTY_UPDATE_DATE)
  @JsonFormat(pattern = DATE_FORMAT)
  LocalDateTime getUpdateDate();

  @JsonProperty(JSON_PROPERTY_CREATION_VERSION)
  String getCreationVersion();

  @JsonProperty(JSON_PROPERTY_UPDATE_VERSION)
  String getUpdateVersion();

  @JsonProperty(JSON_PROPERTY_LANDING_ZONE)
  String getLandingZoneName();

  @JsonIgnore
  LandingZone getLandingZone();

  @JsonIgnore
  Map<String, String> getSimpleJsonRepresentation();
}
