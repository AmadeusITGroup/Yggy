package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
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
  DependencyInstanceMixin.JSON_PROPERTY_DEPENDENCY,
  DependencyInstanceMixin.JSON_PROPERTY_SOURCE_COMPONENT_INSTANCE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_AS_COMPONENT,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDING_ZONE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACRO_PHASE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR,
  DependencyInstanceMixin.JSON_PROPERTY_SOURCES_SELECTORS,
  DependencyInstanceMixin.JSON_PROPERTY_TARGET_COMPONENT_INSTANCE,
  DependencyInstanceMixin.JSON_PROPERTY_TARGET_SERVICES,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_AS_COMPONENT,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITY_ZONE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATION_PHASE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDING_ZONE,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT,
  DependencyInstanceMixin.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACRO_PHASE,
  DependencyInstanceMixin.JSON_PROPERTY_TARGETS_SELECTORS,
  DependencyInstanceMixin.JSON_PROPERTY_DELEGATE_GOVERNANCE_TO,
  DependencyInstanceMixin.JSON_PROPERTY_VIA_COMPONENT_INSTANCE,
  DependencyInstanceMixin.JSON_PROPERTY_VIA_SERVICE,
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(DependencyInstance.class)
public interface DependencyInstanceMixin extends BaseInstanceMixin<V1DependencyInstance> {

  String JSON_PROPERTY_DEPENDENCY = "dependency";
  String JSON_PROPERTY_IS_SOURCE_AS_COMPONENT = "is_source_as_component";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY = "is_source_limited_to_legacy_only";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY = "is_source_limited_to_cloud_only";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDING_ZONE = "is_source_limited_to_landing_zone";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET = "is_source_limited_to_target";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT = "is_source_limited_to_continent";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACRO_PHASE = "is_source_limited_to_macro_phase";
  String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR = "is_source_limited_to_selector";
  String JSON_PROPERTY_IS_TARGET_AS_COMPONENT = "is_target_as_component";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY = "is_target_limited_to_legacy_only";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY = "is_target_limited_to_cloud_only";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE =
      "is_target_limited_to_source_namespace";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS = "is_target_limited_to_source_paas";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITY_ZONE =
      "is_target_limited_to_source_security_zone";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATION_PHASE =
      "is_target_limited_to_source_application_phase";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDING_ZONE =
      "is_target_limited_to_source_landing_zone";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET = "is_target_limited_to_source_target";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT =
      "is_target_limited_to_source_continent";
  String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACRO_PHASE =
      "is_target_limited_to_source_macro_phase";
  String JSON_PROPERTY_DELEGATE_GOVERNANCE_TO = "delegate_governance_to";
  String JSON_PROPERTY_VIA_COMPONENT_INSTANCE = "via_component_instance";
  String JSON_PROPERTY_VIA_SERVICE = "via_service";
  String JSON_PROPERTY_SOURCE_COMPONENT_INSTANCE = "source_component_instance";
  String JSON_PROPERTY_TARGET_COMPONENT_INSTANCE = "target_component_instance";
  String JSON_PROPERTY_TARGET_SERVICES = "target_services";
  String JSON_PROPERTY_SOURCES_SELECTORS = "sources_selectors";
  String JSON_PROPERTY_TARGETS_SELECTORS = "targets_selectors";

  @JsonProperty(JSON_PROPERTY_TARGET_SERVICES)
  List<String> getServiceNames();

  @JsonIgnore
  Set<Service> getServices();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_AS_COMPONENT)
  boolean isSourceAsComponent();

  @JsonProperty(JSON_PROPERTY_SOURCES_SELECTORS)
  Set<Map<String, String>> getSourcesSelectors();

  @JsonProperty(JSON_PROPERTY_TARGETS_SELECTORS)
  Set<Map<String, String>> getTargetsSelectors();

  @JsonProperty(JSON_PROPERTY_DEPENDENCY)
  String getDependencyName();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY)
  boolean isSourceLimitedToLegacyOnly();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY)
  boolean isSourceLimitedToCloudOnly();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDING_ZONE)
  boolean isSourceLimitedToLandingZone();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET)
  boolean isSourceLimitedToTarget();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT)
  boolean isSourceLimitedToContinent();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACRO_PHASE)
  boolean isSourceLimitedToMacroPhase();

  @JsonProperty(JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR)
  boolean isSourceLimitedToSelector();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_AS_COMPONENT)
  boolean isTargetAsComponent();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY)
  boolean isTargetLimitedToLegacyOnly();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY)
  boolean isTargetLimitedToCloudOnly();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE)
  boolean isTargetLimitedToSourceNamespace();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS)
  boolean isTargetLimitedToSourcePaas();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITY_ZONE)
  boolean isTargetLimitedToSourceSecurityZone();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATION_PHASE)
  boolean isTargetLimitedToSourceApplicationPhase();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDING_ZONE)
  boolean isTargetLimitedToSourceLandingZone();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET)
  boolean isTargetLimitedToSourceTarget();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT)
  boolean isTargetLimitedToSourceContinent();

  @JsonProperty(JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACRO_PHASE)
  boolean isTargetLimitedToSourceMacroPhase();

  @JsonProperty(JSON_PROPERTY_DELEGATE_GOVERNANCE_TO)
  V1DependencyInstanceSpec.DelegateGovernanceToEnum getDelegatedGovernanceTo();

  @JsonProperty(JSON_PROPERTY_VIA_COMPONENT_INSTANCE)
  String getViaComponentInstanceName();

  @JsonProperty(JSON_PROPERTY_VIA_SERVICE)
  String getViaServiceName();

  @JsonProperty(JSON_PROPERTY_SOURCE_COMPONENT_INSTANCE)
  String getSourceComponentInstanceName();

  @JsonProperty(JSON_PROPERTY_TARGET_COMPONENT_INSTANCE)
  String getTargetComponentInstanceName();

  @JsonIgnore
  ComponentInstance getComponentInstance();

  @JsonIgnore
  Dependency getDependency();

  @JsonIgnore
  ComponentInstance getTargetComponentInstance();

  @JsonIgnore
  Map<Component, Boolean> getInternalUseWideSubnets();
}
