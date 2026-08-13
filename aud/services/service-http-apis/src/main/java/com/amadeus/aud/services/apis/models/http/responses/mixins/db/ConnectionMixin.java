package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
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
  ConnectionMixin.JSON_PROPERTY_SOURCE_ZONE_TYPE,
  ConnectionMixin.JSON_PROPERTY_DESTINATION_ZONE_TYPE,
  ConnectionMixin.JSON_PROPERTY_FIREWALL_LEVEL,
  ConnectionMixin.JSON_PROPERTY_SECURITY_CONTROL,
  ConnectionMixin.JSON_PROPERTY_CONNECTION_TYPE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Connection.class)
public interface ConnectionMixin extends AbstractBaseMixin<V1Connection> {

  String JSON_PROPERTY_SOURCE_ZONE_TYPE = "source_zone_type";
  String JSON_PROPERTY_DESTINATION_ZONE_TYPE = "destination_zone_type";
  String JSON_PROPERTY_FIREWALL_LEVEL = "firewall_level";
  String JSON_PROPERTY_SECURITY_CONTROL = "security_control";
  String JSON_PROPERTY_CONNECTION_TYPE = "connection_type";

  @JsonProperty(JSON_PROPERTY_FIREWALL_LEVEL)
  V1ConnectionSpec.LevelEnum getFirewallLevel();

  @JsonProperty(JSON_PROPERTY_SECURITY_CONTROL)
  V1ConnectionSpec.ControlEnum getSecurityControl();

  @JsonProperty(JSON_PROPERTY_CONNECTION_TYPE)
  V1ConnectionSpec.TypeEnum getConnectionType();

  @JsonProperty(JSON_PROPERTY_SOURCE_ZONE_TYPE)
  String getSourceZoneTypeName();

  @JsonProperty(JSON_PROPERTY_DESTINATION_ZONE_TYPE)
  String getDestinationZoneTypeName();

  @JsonIgnore
  ZoneType getSourceZoneType();

  @JsonIgnore
  ZoneType getDestinationZoneType();
}
