package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  BlackListedCIDRMixin.JSON_PROPERTY_CIDR,
  BlackListedCIDRMixin.JSON_PROPERTY_TRANSPORT_LAYER,
  BlackListedCIDRMixin.JSON_PROPERTY_PORTS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(BlackListedCIDR.class)
public interface BlackListedCIDRMixin extends AbstractBaseMixin<V1BlackListedCIDR> {

  String JSON_PROPERTY_CIDR = "cidr";
  String JSON_PROPERTY_TRANSPORT_LAYER = "transportLayer";
  String JSON_PROPERTY_PORTS = "ports";

  @JsonProperty(JSON_PROPERTY_CIDR)
  String getCidr();

  @JsonProperty(JSON_PROPERTY_TRANSPORT_LAYER)
  List<V1BlackListedCIDRSpec.TransportLayerEnum> getTransportLayer();

  @JsonProperty(JSON_PROPERTY_PORTS)
  List<String> getPorts();
}
