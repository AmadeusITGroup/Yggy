package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.base.BaseInstanceMixin;
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
  IPAddressInstanceMixin.JSON_PROPERTY_HOST,
  IPAddressInstanceMixin.JSON_PROPERTY_ADVERTISED_TO,
  IPAddressInstanceMixin.JSON_PROPERTY_COMPONENT_INSTANCE
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(IPAddressInstance.class)
public interface IPAddressInstanceMixin extends BaseInstanceMixin<V1IPAddressInstance> {
  String JSON_PROPERTY_HOST = "host";
  String JSON_PROPERTY_ADVERTISED_TO = "advertised_to";
  String JSON_PROPERTY_COMPONENT_INSTANCE = "component_instance";

  @JsonProperty(JSON_PROPERTY_COMPONENT_INSTANCE)
  String getComponentInstanceName();

  @JsonProperty(JSON_PROPERTY_HOST)
  String getHost();

  @JsonProperty(JSON_PROPERTY_ADVERTISED_TO)
  V1IPAddressInstanceSpec.AdvertisedToEnum getAdvertisedTo();

  @JsonIgnore
  ComponentInstance getComponentInstance();
}
