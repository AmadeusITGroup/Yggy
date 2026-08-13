package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.base.BaseInstanceMixin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  AutomaticTemplateInstanceMixin.JSON_PROPERTY_COMPONENT
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(AutomaticTemplateInstance.class)
public interface AutomaticTemplateInstanceMixin extends BaseInstanceMixin<V1ComponentInstance> {
  String JSON_PROPERTY_COMPONENT = "component";

  @JsonProperty(JSON_PROPERTY_COMPONENT)
  String getComponentName();

  @JsonIgnore
  Component getComponent();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  List<Set<String>> getAssociatedFunctionalTypes();

  @JsonIgnore
  List<Set<String>> getExcludedFunctionalTypes();
}
