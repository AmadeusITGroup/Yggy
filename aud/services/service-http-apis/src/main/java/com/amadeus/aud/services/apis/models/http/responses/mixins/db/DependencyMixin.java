package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  DependencyMixin.JSON_PROPERTY_SOURCE_COMPONENT,
  DependencyMixin.JSON_PROPERTY_DESTINATION_COMPONENT
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Dependency.class)
public interface DependencyMixin extends AbstractBaseMixin<V1Dependency> {

  String JSON_PROPERTY_SOURCE_COMPONENT = "source_component";
  String JSON_PROPERTY_DESTINATION_COMPONENT = "target_component";

  @JsonProperty(JSON_PROPERTY_SOURCE_COMPONENT)
  String getSourceComponentName();

  @JsonProperty(JSON_PROPERTY_DESTINATION_COMPONENT)
  String getDestinationComponentName();

  @JsonIgnore
  Component getSourceComponent();

  @JsonIgnore
  Component getDestinationComponent();

  @JsonIgnore
  Set<DependencyInstance> getDependencyInstances();
}
