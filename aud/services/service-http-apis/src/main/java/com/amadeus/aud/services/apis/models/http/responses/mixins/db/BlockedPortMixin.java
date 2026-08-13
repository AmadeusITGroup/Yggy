package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
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
  BlockedPortMixin.JSON_PROPERTY_PORT,
  BlockedPortMixin.JSON_PROPERTY_PROTOCOL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(BlockedPort.class)
public interface BlockedPortMixin extends AbstractBaseMixin<V1BlockedPort> {
  String JSON_PROPERTY_PORT = "port";
  String JSON_PROPERTY_PROTOCOL = "protocol";

  @JsonProperty(JSON_PROPERTY_PORT)
  Integer getPort();

  @JsonProperty(JSON_PROPERTY_PROTOCOL)
  V1BlockedPortSpec.ProtocolEnum getProtocol();
}
