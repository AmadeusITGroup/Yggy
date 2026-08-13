package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.V1Component;
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
  ComponentMixin.JSON_PROPERTY_DEPRECATED_STATUS,
  ComponentMixin.JSON_PROPERTY_DEPRECATED_MESSAGE,
  ComponentMixin.JSON_PROPERTY_PARENTS,
  ComponentMixin.JSON_PROPERTY_SECURITY_ZONES,
  ComponentMixin.JSON_PROPERTY_SERVICE_NOW_IDS,
  ComponentMixin.JSON_PROPERTY_IS_PUBLIC,
  ComponentMixin.JSON_PROPERTY_IS_PUBLIC_INHERITANCE,
  ComponentMixin.JSON_PROPERTY_IS_PUBLIC_DEPENDENCY,
  ComponentMixin.JSON_PROPERTY_IS_PRIVATE,
  ComponentMixin.JSON_PROPERTY_IS_HOSTING,
  ComponentMixin.JSON_PROPERTY_IS_HOSTED,
  ComponentMixin.JSON_PROPERTY_IS_INTERFACE,
  ComponentMixin.JSON_PROPERTY_ACCESS_CONTROL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Component.class)
public interface ComponentMixin extends AbstractBaseMixin<V1Component> {

  String JSON_PROPERTY_PARENTS = "parents";
  String JSON_PROPERTY_SECURITY_ZONES = "security_zones";
  String JSON_PROPERTY_SERVICE_NOW_IDS = "serviceNowIds";
  String JSON_PROPERTY_DEPRECATED_STATUS = "deprecated_status";
  String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecated_message";
  String JSON_PROPERTY_IS_PUBLIC = "is_public";
  String JSON_PROPERTY_IS_PUBLIC_INHERITANCE = "is_public_inheritance";
  String JSON_PROPERTY_IS_PUBLIC_DEPENDENCY = "is_public_dependency";
  String JSON_PROPERTY_IS_PRIVATE = "is_private";
  String JSON_PROPERTY_IS_HOSTING = "is_hosting";
  String JSON_PROPERTY_IS_HOSTED = "is_hosted";
  String JSON_PROPERTY_IS_INTERFACE = "is_interface";
  String JSON_PROPERTY_ACCESS_CONTROL = "access_control";

  @JsonProperty(JSON_PROPERTY_SECURITY_ZONES)
  Set<String> getSecurityZones();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_STATUS)
  DeprecatedStatus getDeprecatedStatus();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_MESSAGE)
  String getDeprecatedMessage();

  @JsonProperty(JSON_PROPERTY_ACCESS_CONTROL)
  Map<String, Object> getAccessControl();

  @JsonProperty(JSON_PROPERTY_IS_PUBLIC)
  boolean isPublic();

  @JsonProperty(JSON_PROPERTY_IS_PUBLIC_INHERITANCE)
  boolean isPublicInheritance();

  @JsonProperty(JSON_PROPERTY_IS_PUBLIC_DEPENDENCY)
  boolean isPublicDependency();

  @JsonProperty(JSON_PROPERTY_IS_PRIVATE)
  boolean isPrivate();

  @JsonProperty(JSON_PROPERTY_IS_HOSTING)
  boolean isHosting();

  @JsonProperty(JSON_PROPERTY_IS_HOSTED)
  boolean isHosted();

  @JsonIgnore
  boolean isTechnical();

  @JsonProperty(JSON_PROPERTY_IS_INTERFACE)
  boolean isInterface();

  @JsonProperty(JSON_PROPERTY_PARENTS)
  List<Map<String, String>> getParentNames();

  @JsonIgnore
  Set<Component> getParents();

  @JsonIgnore
  Set<Component> getChildren();

  @JsonIgnore
  Set<ZoneType> getZoneTypes();

  @JsonProperty(JSON_PROPERTY_SERVICE_NOW_IDS)
  Set<String> getServiceNowIds();

  @JsonIgnore
  Set<Service> getServices();

  @JsonIgnore
  Set<Dependency> getDependencies();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  Set<AutomaticTemplateInstance> getAutomaticTemplateInstances();

  @JsonIgnore
  Set<Component> getInternalAncestors();

  @JsonIgnore
  Map<Boolean, Set<ComponentInstance>> getAllComponentInstances();

  @JsonIgnore
  boolean isContainer();
}
