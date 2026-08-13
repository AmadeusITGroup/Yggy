package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.base.BaseInstanceMixin;
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
  ComponentInstanceMixin.JSON_PROPERTY_COMPONENT,
  ComponentInstanceMixin.JSON_PROPERTY_NAMESPACE,
  ComponentInstanceMixin.JSON_PROPERTY_APPLICATION_PHASE,
  ComponentInstanceMixin.JSON_PROPERTY_DEPRECATED_STATUS,
  ComponentInstanceMixin.JSON_PROPERTY_DEPRECATED_MESSAGE,
  ComponentInstanceMixin.JSON_PROPERTY_ENDPOINT_TYPE,
  ComponentInstanceMixin.JSON_PROPERTY_IS_AUTOMATICALLY_INSTANTIATED,
  ComponentInstanceMixin.JSON_PROPERTY_ZONE_TYPE,
  ComponentInstanceMixin.JSON_PROPERTY_K8S_IDENTIFIER,
  ComponentInstanceMixin.JSON_PROPERTY_HOSTING_PLATFORM_TYPE,
  ComponentInstanceMixin.JSON_PROPERTY_HOSTING_PLATFORM_SECURITY,
  ComponentInstanceMixin.JSON_PROPERTY_HOSTING_OVERLAY_SUBNETS,
  ComponentInstanceMixin.JSON_PROPERTY_HOSTED_COMPONENT_INSTANCES,
  ComponentInstanceMixin.JSON_PROPERTY_HOSTING_PLATFORM,
  ComponentInstanceMixin.JSON_PROPERTY_IN_SUBNET_INSTANCE,
  ComponentInstanceMixin.JSON_PROPERTY_OUT_SUBNET_INSTANCE,
  ComponentInstanceMixin.JSON_PROPERTY_IN_ZONE_INSTANCE,
  ComponentInstanceMixin.JSON_PROPERTY_OUT_ZONE_INSTANCE,
  ComponentInstanceMixin.JSON_PROPERTY_SERVICE_TAG,
  ComponentInstanceMixin.JSON_PROPERTY_ENDPOINTS,
  ComponentInstanceMixin.JSON_PROPERTY_K8S_SELECTOR,
  ComponentInstanceMixin.JSON_PROPERTY_SOURCES_SELECTORS,
  ComponentInstanceMixin.JSON_PROPERTY_AUTOMATIC_TEMPLATE_INSTANCES,
  ComponentInstanceMixin.JSON_PROPERTY_CIDR_RANGES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ComponentInstance.class)
public interface ComponentInstanceMixin extends BaseInstanceMixin<V1ComponentInstance> {
  String JSON_PROPERTY_COMPONENT = "component";
  String JSON_PROPERTY_DEPRECATED_STATUS = "deprecated_status";
  String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecated_message";
  String JSON_PROPERTY_ENDPOINT_TYPE = "endpoint_type";
  String JSON_PROPERTY_APPLICATION_PHASE = "application_phase";
  String JSON_PROPERTY_IS_AUTOMATICALLY_INSTANTIATED = "auto_generated";
  String JSON_PROPERTY_NAMESPACE = "namespace";
  String JSON_PROPERTY_ZONE_TYPE = "zone_type";
  String JSON_PROPERTY_K8S_IDENTIFIER = "k8s_identifier";
  String JSON_PROPERTY_HOSTING_PLATFORM_TYPE = "hosting_platform_type";
  String JSON_PROPERTY_HOSTING_PLATFORM_SECURITY = "hosting_platform_security";
  String JSON_PROPERTY_HOSTING_OVERLAY_SUBNETS = "hosting_overlay_subnets";
  String JSON_PROPERTY_HOSTED_COMPONENT_INSTANCES = "hosted_component_instances";
  String JSON_PROPERTY_IN_SUBNET_INSTANCE = "in_subnet_instance";
  String JSON_PROPERTY_OUT_SUBNET_INSTANCE = "out_subnet_instance";
  String JSON_PROPERTY_IN_ZONE_INSTANCE = "in_zone_instance";
  String JSON_PROPERTY_OUT_ZONE_INSTANCE = "out_zone_instance";
  String JSON_PROPERTY_SERVICE_TAG = "service_tag";
  String JSON_PROPERTY_ENDPOINTS = "endpoints";
  String JSON_PROPERTY_K8S_SELECTOR = "k8s_selector";
  String JSON_PROPERTY_SOURCES_SELECTORS = "sources_selectors";
  String JSON_PROPERTY_AUTOMATIC_TEMPLATE_INSTANCES = "automatic_template_instances";
  String JSON_PROPERTY_HOSTING_PLATFORM = "hosting_platform";
  String JSON_PROPERTY_CIDR_RANGES = "cidr_ranges";

  @JsonProperty(JSON_PROPERTY_IS_AUTOMATICALLY_INSTANTIATED)
  boolean isAutomaticallyInstantiated();

  @JsonProperty(JSON_PROPERTY_AUTOMATIC_TEMPLATE_INSTANCES)
  List<Map<String, String>> getSimpleAutomaticTemplateInstances();

  @JsonIgnore
  Set<AutomaticTemplateInstance> getAutomaticTemplateInstances();

  @JsonIgnore
  List<Map<String, String>> getSimpleHostedComponentInstances();

  @JsonIgnore
  Set<ComponentInstance> getHostedComponentInstances();

  @JsonProperty(JSON_PROPERTY_SOURCES_SELECTORS)
  Set<Map<String, String>> getSourcesSelectors();

  @JsonProperty(JSON_PROPERTY_COMPONENT)
  String getComponentName();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_STATUS)
  DeprecatedStatus getDeprecatedStatus();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_MESSAGE)
  String getDeprecatedMessage();

  @JsonProperty(JSON_PROPERTY_ENDPOINT_TYPE)
  V1ComponentInstanceSpec.EndpointTypeEnum getEndpointType();

  @JsonProperty(JSON_PROPERTY_APPLICATION_PHASE)
  String getApplicationPhaseName();

  @JsonProperty(JSON_PROPERTY_NAMESPACE)
  String getNamespaceName();

  @JsonProperty(JSON_PROPERTY_ZONE_TYPE)
  String getZoneTypeName();

  @JsonProperty(JSON_PROPERTY_K8S_IDENTIFIER)
  String getK8sIdentifier();

  @JsonProperty(JSON_PROPERTY_HOSTING_PLATFORM_TYPE)
  V1ComponentInstanceSpec.HostingComponentTypeEnum getHostingPlatformType();

  @JsonProperty(JSON_PROPERTY_HOSTING_PLATFORM_SECURITY)
  V1ComponentInstanceSpec.HostingComponentSecurityEnum getHostingPlatformSecurity();

  @JsonProperty(JSON_PROPERTY_HOSTING_OVERLAY_SUBNETS)
  String getHostingOverlaySubnets();

  @JsonProperty(JSON_PROPERTY_IN_SUBNET_INSTANCE)
  String getInSubnetInstanceName();

  @JsonProperty(JSON_PROPERTY_OUT_SUBNET_INSTANCE)
  String getOutSubnetInstanceName();

  @JsonProperty(JSON_PROPERTY_IN_ZONE_INSTANCE)
  String getInZoneInstanceName();

  @JsonProperty(JSON_PROPERTY_OUT_ZONE_INSTANCE)
  String getOutZoneInstanceName();

  @JsonProperty(JSON_PROPERTY_SERVICE_TAG)
  String getServiceTagName();

  @JsonProperty(JSON_PROPERTY_ENDPOINTS)
  List<Object> getEndpoints();

  @JsonProperty(JSON_PROPERTY_K8S_SELECTOR)
  Map<String, String> getK8sSelector();

  @JsonProperty(JSON_PROPERTY_HOSTING_PLATFORM)
  String getHostingPlatformName();

  @JsonIgnore
  Component getComponent();

  @JsonIgnore
  ApplicationPhase getApplicationPhase();

  @JsonIgnore
  ServiceTag getServiceTag();

  @JsonIgnore
  ZoneType getZoneType();

  @JsonIgnore
  SubnetInstance getInSubnetInstance();

  @JsonIgnore
  SubnetInstance getOutSubnetInstance();

  @JsonIgnore
  ZoneInstance getInZoneInstance();

  @JsonIgnore
  ZoneInstance getOutZoneInstance();

  @JsonIgnore
  Namespace getNamespace();

  @JsonIgnore
  ComponentInstance getDeployComponentInstance();

  @JsonIgnore
  String getDeployComponentInstanceName();

  @JsonIgnore
  Set<ZoneType> getZoneTypes();

  @JsonIgnore
  Set<IPAddressInstance> getIpAddressInstances();

  @JsonIgnore
  Set<DependencyInstance> getDependencyInstances();

  @JsonIgnore
  Map<Integer, String> getInternalZoneTypeNames();

  @JsonIgnore
  Map<Direction, String> getInternalZoneTypeNamesByDirection();

  @JsonIgnore
  Map<Direction, Set<Endpoint>> getInternalEndpoints();

  @JsonIgnore
  Set<SubnetInstance> getPaasZoneOutSubnetInstances();

  @JsonIgnore
  Set<SubnetInstance> getPaasZoneInSubnetInstances();

  @JsonIgnore
  ComponentInstance getHostingPlatform();

  @JsonIgnore
  ObjectGroup getObjectGroupDirection();

  @JsonIgnore
  ObjectGroup.TypeEnum getObjectGroupType();

  @JsonIgnore
  Map<Direction, ObjectGroup> getObjectGroups();

  @JsonProperty(JSON_PROPERTY_CIDR_RANGES)
  Map<String, List<ComponentInstance.CidrRange>> getCidrRanges();
}
