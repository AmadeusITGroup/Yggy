package com.amadeus.aud.services.apis.models.http.responses.mixins.internals;

import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Range;
import io.quarkus.jackson.JacksonMixin;
import java.math.BigInteger;

@JsonPropertyOrder({
  EndpointMixin.JSON_PROPERTY_TYPE,
  EndpointMixin.JSON_PROPERTY_VALUE,
  EndpointMixin.JSON_PROPERTY_DESCRIPTION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Endpoint.class)
public interface EndpointMixin {
  String JSON_PROPERTY_TYPE = "type";
  String JSON_PROPERTY_VALUE = "value";
  String JSON_PROPERTY_DESCRIPTION = "description";

  @JsonProperty(JSON_PROPERTY_TYPE)
  Endpoint.TypeEnum getType();

  @JsonProperty(JSON_PROPERTY_VALUE)
  String getValue();

  @JsonProperty(JSON_PROPERTY_DESCRIPTION)
  String getDescription();

  @JsonIgnore
  Range<BigInteger> getCidrRange();

  interface TypeEnumMixin {

    @JsonValue
    String getValue();

    @JsonCreator
    Endpoint.TypeEnum fromValue(String value);
  }
}
