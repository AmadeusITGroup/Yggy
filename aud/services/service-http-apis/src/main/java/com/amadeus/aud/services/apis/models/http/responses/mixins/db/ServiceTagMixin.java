package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.yaml.models.V1Servicetag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ServiceTagMixin.JSON_PROPERTY_COMPONENT_INSTANCES,
  ServiceTagMixin.JSON_PROPERTY_ANY_NEEDED_IN_NSG
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ServiceTag.class)
public interface ServiceTagMixin extends AbstractBaseMixin<V1Servicetag> {

  String JSON_PROPERTY_COMPONENT_INSTANCES = "component_instances";
  String JSON_PROPERTY_ANY_NEEDED_IN_NSG = "any_needed_in_nsg";

  @JsonProperty(JSON_PROPERTY_COMPONENT_INSTANCES)
  List<Map<String, String>> getSimpleComponentInstances();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonProperty(JSON_PROPERTY_ANY_NEEDED_IN_NSG)
  boolean getAnyNeededInNsg();
}
