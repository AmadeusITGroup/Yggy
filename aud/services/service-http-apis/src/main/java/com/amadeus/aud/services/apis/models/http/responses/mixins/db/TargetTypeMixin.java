package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
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
  TargetTypeMixin.JSON_PROPERTY_TARGETS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(TargetType.class)
public interface TargetTypeMixin extends AbstractBaseMixin<V1TargetType> {

  String JSON_PROPERTY_TARGETS = "targets";

  @JsonProperty(JSON_PROPERTY_TARGETS)
  @JsonIgnore
  List<Map<String, String>> getSimpleTargets();

  @JsonIgnore
  Set<Target> getTargets();
}
