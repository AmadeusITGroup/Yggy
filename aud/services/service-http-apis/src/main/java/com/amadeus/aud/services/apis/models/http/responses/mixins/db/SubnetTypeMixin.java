package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.yaml.models.V1SubnetType;
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
  SubnetTypeMixin.JSON_PROPERTY_SUBNET_INSTANCES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(SubnetType.class)
public interface SubnetTypeMixin extends AbstractBaseMixin<V1SubnetType> {

  String JSON_PROPERTY_SUBNET_INSTANCES = "subnet_instances";

  @JsonProperty(JSON_PROPERTY_SUBNET_INSTANCES)
  List<Map<String, String>> getSimpleSubnetInstances();

  @JsonIgnore
  Set<SubnetInstance> getSubnetInstances();
}
