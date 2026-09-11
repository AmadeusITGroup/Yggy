package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ComponentInstance specification */
@Schema(description = "ComponentInstance specification")
@JsonPropertyOrder({
  V1ComponentInstanceSpec.JSON_PROPERTY_COMPONENT,
  V1ComponentInstanceSpec.JSON_PROPERTY_APPLICATION_PHASE,
  V1ComponentInstanceSpec.JSON_PROPERTY_IN_SUBNET_INSTANCE,
  V1ComponentInstanceSpec.JSON_PROPERTY_OUT_SUBNET_INSTANCE,
  V1ComponentInstanceSpec.JSON_PROPERTY_IN_ZONE_INSTANCE,
  V1ComponentInstanceSpec.JSON_PROPERTY_OUT_ZONE_INSTANCE,
  V1ComponentInstanceSpec.JSON_PROPERTY_SERVICETAG,
  V1ComponentInstanceSpec.JSON_PROPERTY_HOSTING_COMPONENT,
  V1ComponentInstanceSpec.JSON_PROPERTY_HOSTING_COMPONENT_TYPE,
  V1ComponentInstanceSpec.JSON_PROPERTY_HOSTING_COMPONENT_SECURITY,
  V1ComponentInstanceSpec.JSON_PROPERTY_ZONE_TYPE,
  V1ComponentInstanceSpec.JSON_PROPERTY_NAMESPACE,
  V1ComponentInstanceSpec.JSON_PROPERTY_K8S_IDENTIFIER,
  V1ComponentInstanceSpec.JSON_PROPERTY_K8S_SELECTOR,
  V1ComponentInstanceSpec.JSON_PROPERTY_OVERLAY_SUBNETS,
  V1ComponentInstanceSpec.JSON_PROPERTY_ENDPOINT_TYPE,
  V1ComponentInstanceSpec.JSON_PROPERTY_ASSOCIATED_IMPLEMENTATION_ZONE,
  V1ComponentInstanceSpec.JSON_PROPERTY_ASSOCIATED_PLATFORM_COMPONENT,
  V1ComponentInstanceSpec.JSON_PROPERTY_ASSOCIATED_FUNCTIONAL_TYPES,
  V1ComponentInstanceSpec.JSON_PROPERTY_EXCLUDED_FUNCTIONAL_TYPES,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LEGACY_ONLY,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CLOUD_ONLY,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LANDINGZONE,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_TARGET,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CONTINENT,
  V1ComponentInstanceSpec.JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_MACROPHASE,
  V1ComponentInstanceSpec.JSON_PROPERTY_DEPRECATED_STATUS,
  V1ComponentInstanceSpec.JSON_PROPERTY_DEPRECATED_MESSAGE,
  V1ComponentInstanceSpec.JSON_PROPERTY_ENDPOINTS,
  V1ComponentInstanceSpec.JSON_PROPERTY_SOURCES_SELECTORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ComponentInstanceSpec {
  public static final String JSON_PROPERTY_COMPONENT = "component";
  private String component;

  public static final String JSON_PROPERTY_APPLICATION_PHASE = "applicationPhase";
  private String applicationPhase;

  public static final String JSON_PROPERTY_IN_SUBNET_INSTANCE = "inSubnetInstance";
  @Nullable private String inSubnetInstance;

  public static final String JSON_PROPERTY_OUT_SUBNET_INSTANCE = "outSubnetInstance";
  @Nullable private String outSubnetInstance;

  public static final String JSON_PROPERTY_IN_ZONE_INSTANCE = "inZoneInstance";
  @Nullable private String inZoneInstance;

  public static final String JSON_PROPERTY_OUT_ZONE_INSTANCE = "outZoneInstance";
  @Nullable private String outZoneInstance;

  public static final String JSON_PROPERTY_SERVICETAG = "servicetag";
  @Nullable private String servicetag;

  public static final String JSON_PROPERTY_HOSTING_COMPONENT = "hostingComponent";
  @Nullable private String hostingComponent;

  /** Type of the hosting Component, if isHosting. */
  public enum HostingComponentTypeEnum {
    AKS(String.valueOf("AKS")),

    OPEN_SHIFT(String.valueOf("OpenShift"));

    private String value;

    HostingComponentTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static HostingComponentTypeEnum fromValue(String value) {
      for (HostingComponentTypeEnum b : HostingComponentTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_HOSTING_COMPONENT_TYPE = "hosting_component_type";
  @Nullable private HostingComponentTypeEnum hostingComponentType;

  /** Type of the hosting Component Security, if isHosting. */
  public enum HostingComponentSecurityEnum {
    CALICO(String.valueOf("Calico")),

    OVN(String.valueOf("OVN")),

    NONE(String.valueOf("None"));

    private String value;

    HostingComponentSecurityEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static HostingComponentSecurityEnum fromValue(String value) {
      for (HostingComponentSecurityEnum b : HostingComponentSecurityEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_HOSTING_COMPONENT_SECURITY =
      "hosting_component_security";
  @Nullable private HostingComponentSecurityEnum hostingComponentSecurity;

  public static final String JSON_PROPERTY_ZONE_TYPE = "zoneType";
  @Nullable private String zoneType;

  public static final String JSON_PROPERTY_NAMESPACE = "namespace";
  @Nullable private String namespace;

  public static final String JSON_PROPERTY_K8S_IDENTIFIER = "k8s_identifier";
  @Nullable private String k8sIdentifier;

  public static final String JSON_PROPERTY_K8S_SELECTOR = "k8s_selector";
  @Nullable private Map<String, String> k8sSelector;

  public static final String JSON_PROPERTY_OVERLAY_SUBNETS = "overlaySubnets";
  @Nullable private List<String> overlaySubnets;

  /** The way to describe the endpoint associated to this instance */
  public enum EndpointTypeEnum {
    CID_RLIST(String.valueOf("CIDRlist")),

    UR_LLIST(String.valueOf("URLlist")),

    ZONED_PAA_S(String.valueOf("ZonedPaaS")),

    MULTI_SUBNET_PAA_S(String.valueOf("MultiSubnetPaaS"));

    private String value;

    EndpointTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static EndpointTypeEnum fromValue(String value) {
      for (EndpointTypeEnum b : EndpointTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ENDPOINT_TYPE = "endpoint_type";
  @Nullable private EndpointTypeEnum endpointType;

  public static final String JSON_PROPERTY_ASSOCIATED_IMPLEMENTATION_ZONE =
      "associatedImplementationZone";
  @Nullable private String associatedImplementationZone;

  public static final String JSON_PROPERTY_ASSOCIATED_PLATFORM_COMPONENT =
      "associatedPlatformComponent";
  @Nullable private String associatedPlatformComponent;

  public static final String JSON_PROPERTY_ASSOCIATED_FUNCTIONAL_TYPES =
      "associatedFunctionalTypes";
  @Nullable private List<String> associatedFunctionalTypes;

  public static final String JSON_PROPERTY_EXCLUDED_FUNCTIONAL_TYPES = "excludedFunctionalTypes";
  @Nullable private List<String> excludedFunctionalTypes;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LEGACY_ONLY =
      "isAssociatedPlatformLimitedToLegacyOnly";
  @Nullable private Boolean isAssociatedPlatformLimitedToLegacyOnly = false;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CLOUD_ONLY =
      "isAssociatedPlatformLimitedToCloudOnly";
  @Nullable private Boolean isAssociatedPlatformLimitedToCloudOnly = false;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LANDINGZONE =
      "isAssociatedPlatformLimitedToLandingzone";
  @Nullable private Boolean isAssociatedPlatformLimitedToLandingzone = false;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_TARGET =
      "isAssociatedPlatformLimitedToTarget";
  @Nullable private Boolean isAssociatedPlatformLimitedToTarget = false;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CONTINENT =
      "isAssociatedPlatformLimitedToContinent";
  @Nullable private Boolean isAssociatedPlatformLimitedToContinent = false;

  public static final String JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_MACROPHASE =
      "isAssociatedPlatformLimitedToMacrophase";
  @Nullable private Boolean isAssociatedPlatformLimitedToMacrophase = false;

  public static final String JSON_PROPERTY_DEPRECATED_STATUS = "deprecatedStatus";
  @Nullable private DeprecatedStatus deprecatedStatus = DeprecatedStatus.NONE;

  public static final String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecatedMessage";
  @Nullable private String deprecatedMessage;

  public static final String JSON_PROPERTY_ENDPOINTS = "endpoints";
  @Nullable private List<Object> endpoints;

  public static final String JSON_PROPERTY_SOURCES_SELECTORS = "sources_selectors";
  @Nullable private List<Map<String, String>> sourcesSelectors;

  public V1ComponentInstanceSpec() {}

  public V1ComponentInstanceSpec component(String component) {
    this.component = component;
    return this;
  }

  /**
   * Name of the component
   *
   * @return component
   */
  @NotNull
  @Schema(required = true, description = "Name of the component")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponent() {
    return component;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponent(String component) {
    this.component = component;
  }

  public V1ComponentInstanceSpec applicationPhase(String applicationPhase) {
    this.applicationPhase = applicationPhase;
    return this;
  }

  /**
   * Name of the application phase
   *
   * @return applicationPhase
   */
  @NotNull
  @Schema(required = true, description = "Name of the application phase")
  @JsonProperty(value = JSON_PROPERTY_APPLICATION_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getApplicationPhase() {
    return applicationPhase;
  }

  @JsonProperty(value = JSON_PROPERTY_APPLICATION_PHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApplicationPhase(String applicationPhase) {
    this.applicationPhase = applicationPhase;
  }

  public V1ComponentInstanceSpec inSubnetInstance(@Nullable String inSubnetInstance) {
    this.inSubnetInstance = inSubnetInstance;
    return this;
  }

  /**
   * Name of the incoming traffic subnet instance
   *
   * @return inSubnetInstance
   */
  @Schema(required = false, description = "Name of the incoming traffic subnet instance")
  @JsonProperty(value = JSON_PROPERTY_IN_SUBNET_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getInSubnetInstance() {
    return inSubnetInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_IN_SUBNET_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInSubnetInstance(@Nullable String inSubnetInstance) {
    this.inSubnetInstance = inSubnetInstance;
  }

  public V1ComponentInstanceSpec outSubnetInstance(@Nullable String outSubnetInstance) {
    this.outSubnetInstance = outSubnetInstance;
    return this;
  }

  /**
   * Name of the outcoming traffic subnet instance
   *
   * @return outSubnetInstance
   */
  @Schema(required = false, description = "Name of the outcoming traffic subnet instance")
  @JsonProperty(value = JSON_PROPERTY_OUT_SUBNET_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getOutSubnetInstance() {
    return outSubnetInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_OUT_SUBNET_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOutSubnetInstance(@Nullable String outSubnetInstance) {
    this.outSubnetInstance = outSubnetInstance;
  }

  public V1ComponentInstanceSpec inZoneInstance(@Nullable String inZoneInstance) {
    this.inZoneInstance = inZoneInstance;
    return this;
  }

  /**
   * Name of the incoming traffic zone instance
   *
   * @return inZoneInstance
   */
  @Schema(required = false, description = "Name of the incoming traffic zone instance")
  @JsonProperty(value = JSON_PROPERTY_IN_ZONE_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getInZoneInstance() {
    return inZoneInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_IN_ZONE_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInZoneInstance(@Nullable String inZoneInstance) {
    this.inZoneInstance = inZoneInstance;
  }

  public V1ComponentInstanceSpec outZoneInstance(@Nullable String outZoneInstance) {
    this.outZoneInstance = outZoneInstance;
    return this;
  }

  /**
   * Name of the outcoming traffic zone instance
   *
   * @return outZoneInstance
   */
  @Schema(required = false, description = "Name of the outcoming traffic zone instance")
  @JsonProperty(value = JSON_PROPERTY_OUT_ZONE_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getOutZoneInstance() {
    return outZoneInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_OUT_ZONE_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOutZoneInstance(@Nullable String outZoneInstance) {
    this.outZoneInstance = outZoneInstance;
  }

  public V1ComponentInstanceSpec servicetag(@Nullable String servicetag) {
    this.servicetag = servicetag;
    return this;
  }

  /**
   * The name of a service tag (string, length &lt;&#x3D; 256) requested by a source of a
   * dependency; the presence of a service tag allows manipulating (opening) a target of the
   * dependency; mutually exclusive with inSubnetInstance, outSubnetInstance
   *
   * @return servicetag
   */
  @Schema(
      required = false,
      description =
          "The name of a service tag (string, length <= 256) requested by a source of a dependency; the presence of a service tag allows manipulating (opening) a target of the dependency; mutually exclusive with inSubnetInstance, outSubnetInstance")
  @JsonProperty(value = JSON_PROPERTY_SERVICETAG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getServicetag() {
    return servicetag;
  }

  @JsonProperty(value = JSON_PROPERTY_SERVICETAG, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setServicetag(@Nullable String servicetag) {
    this.servicetag = servicetag;
  }

  public V1ComponentInstanceSpec hostingComponent(@Nullable String hostingComponent) {
    this.hostingComponent = hostingComponent;
    return this;
  }

  /**
   * Name of the hosting Component, if hosted.
   *
   * @return hostingComponent
   */
  @Schema(required = false, description = "Name of the hosting Component, if hosted.")
  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getHostingComponent() {
    return hostingComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHostingComponent(@Nullable String hostingComponent) {
    this.hostingComponent = hostingComponent;
  }

  public V1ComponentInstanceSpec hostingComponentType(
      @Nullable HostingComponentTypeEnum hostingComponentType) {
    this.hostingComponentType = hostingComponentType;
    return this;
  }

  /**
   * Type of the hosting Component, if isHosting.
   *
   * @return hostingComponentType
   */
  @Schema(required = false, description = "Type of the hosting Component, if isHosting.")
  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable HostingComponentTypeEnum getHostingComponentType() {
    return hostingComponentType;
  }

  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHostingComponentType(@Nullable HostingComponentTypeEnum hostingComponentType) {
    this.hostingComponentType = hostingComponentType;
  }

  public V1ComponentInstanceSpec hostingComponentSecurity(
      @Nullable HostingComponentSecurityEnum hostingComponentSecurity) {
    this.hostingComponentSecurity = hostingComponentSecurity;
    return this;
  }

  /**
   * Type of the hosting Component Security, if isHosting.
   *
   * @return hostingComponentSecurity
   */
  @Schema(required = false, description = "Type of the hosting Component Security, if isHosting.")
  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT_SECURITY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable HostingComponentSecurityEnum getHostingComponentSecurity() {
    return hostingComponentSecurity;
  }

  @JsonProperty(value = JSON_PROPERTY_HOSTING_COMPONENT_SECURITY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setHostingComponentSecurity(
      @Nullable HostingComponentSecurityEnum hostingComponentSecurity) {
    this.hostingComponentSecurity = hostingComponentSecurity;
  }

  public V1ComponentInstanceSpec zoneType(@Nullable String zoneType) {
    this.zoneType = zoneType;
    return this;
  }

  /**
   * Name of the zone type, if hosted
   *
   * @return zoneType
   */
  @Schema(required = false, description = "Name of the zone type, if hosted")
  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getZoneType() {
    return zoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setZoneType(@Nullable String zoneType) {
    this.zoneType = zoneType;
  }

  public V1ComponentInstanceSpec namespace(@Nullable String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Name of the namespace, if hosted
   *
   * @return namespace
   */
  @Schema(required = false, description = "Name of the namespace, if hosted")
  @JsonProperty(value = JSON_PROPERTY_NAMESPACE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getNamespace() {
    return namespace;
  }

  @JsonProperty(value = JSON_PROPERTY_NAMESPACE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setNamespace(@Nullable String namespace) {
    this.namespace = namespace;
  }

  public V1ComponentInstanceSpec k8sIdentifier(@Nullable String k8sIdentifier) {
    this.k8sIdentifier = k8sIdentifier;
    return this;
  }

  /**
   * The value of kubernetes&#39; label, if of type Container; 63 chars max,
   * [a-z0-9A-Z][a-z0-9A-Z\\-\\_\\.]*[a-z0-9A-Z]
   *
   * @return k8sIdentifier
   */
  @Schema(
      required = false,
      description =
          "The value of kubernetes' label, if of type Container; 63 chars max, [a-z0-9A-Z][a-z0-9A-Z\\-\\_\\.]*[a-z0-9A-Z]")
  @JsonProperty(value = JSON_PROPERTY_K8S_IDENTIFIER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getK8sIdentifier() {
    return k8sIdentifier;
  }

  @JsonProperty(value = JSON_PROPERTY_K8S_IDENTIFIER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setK8sIdentifier(@Nullable String k8sIdentifier) {
    this.k8sIdentifier = k8sIdentifier;
  }

  public V1ComponentInstanceSpec k8sSelector(@Nullable Map<String, String> k8sSelector) {
    this.k8sSelector = k8sSelector;
    return this;
  }

  public V1ComponentInstanceSpec putK8sSelectorItem(String key, String k8sSelectorItem) {
    if (this.k8sSelector == null) {
      this.k8sSelector = new HashMap<>();
    }
    this.k8sSelector.put(key, k8sSelectorItem);
    return this;
  }

  /**
   * To be used only if the k8s_identifier is not fulfilling the need (e.g. kafka and splunk
   * operators). Override k8s_identifier definition.
   *
   * @return k8sSelector
   */
  @Schema(
      required = false,
      description =
          "To be used only if the k8s_identifier is not fulfilling the need (e.g. kafka and splunk operators). Override k8s_identifier definition.")
  @JsonProperty(value = JSON_PROPERTY_K8S_SELECTOR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Map<String, String> getK8sSelector() {
    return k8sSelector;
  }

  @JsonProperty(value = JSON_PROPERTY_K8S_SELECTOR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setK8sSelector(@Nullable Map<String, String> k8sSelector) {
    this.k8sSelector = k8sSelector;
  }

  public V1ComponentInstanceSpec overlaySubnets(@Nullable List<String> overlaySubnets) {
    this.overlaySubnets = overlaySubnets;
    return this;
  }

  public V1ComponentInstanceSpec addOverlaySubnetsItem(String overlaySubnetsItem) {
    if (this.overlaySubnets == null) {
      this.overlaySubnets = new ArrayList<>();
    }
    this.overlaySubnets.add(overlaySubnetsItem);
    return this;
  }

  /**
   * List of overlay Subnets if hosting
   *
   * @return overlaySubnets
   */
  @Schema(required = false, description = "List of overlay Subnets if hosting")
  @JsonProperty(value = JSON_PROPERTY_OVERLAY_SUBNETS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getOverlaySubnets() {
    return overlaySubnets;
  }

  @JsonProperty(value = JSON_PROPERTY_OVERLAY_SUBNETS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOverlaySubnets(@Nullable List<String> overlaySubnets) {
    this.overlaySubnets = overlaySubnets;
  }

  public V1ComponentInstanceSpec endpointType(@Nullable EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
    return this;
  }

  /**
   * The way to describe the endpoint associated to this instance
   *
   * @return endpointType
   */
  @Schema(
      required = false,
      description = "The way to describe the endpoint associated to this instance")
  @JsonProperty(value = JSON_PROPERTY_ENDPOINT_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable EndpointTypeEnum getEndpointType() {
    return endpointType;
  }

  @JsonProperty(value = JSON_PROPERTY_ENDPOINT_TYPE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEndpointType(@Nullable EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }

  public V1ComponentInstanceSpec associatedImplementationZone(
      @Nullable String associatedImplementationZone) {
    this.associatedImplementationZone = associatedImplementationZone;
    return this;
  }

  /**
   * This instance should be created each time an instance of the implemetationzone exist
   *
   * @return associatedImplementationZone
   */
  @Schema(
      required = false,
      description =
          "This instance should be created each time an instance of the implemetationzone exist")
  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_IMPLEMENTATION_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getAssociatedImplementationZone() {
    return associatedImplementationZone;
  }

  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_IMPLEMENTATION_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssociatedImplementationZone(@Nullable String associatedImplementationZone) {
    this.associatedImplementationZone = associatedImplementationZone;
  }

  public V1ComponentInstanceSpec associatedPlatformComponent(
      @Nullable String associatedPlatformComponent) {
    this.associatedPlatformComponent = associatedPlatformComponent;
    return this;
  }

  /**
   * Does this instance should be created each time an instance of the platform component is created
   *
   * @return associatedPlatformComponent
   */
  @Schema(
      required = false,
      description =
          "Does this instance should be created each time an instance of the platform component is created")
  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_PLATFORM_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getAssociatedPlatformComponent() {
    return associatedPlatformComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_PLATFORM_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssociatedPlatformComponent(@Nullable String associatedPlatformComponent) {
    this.associatedPlatformComponent = associatedPlatformComponent;
  }

  public V1ComponentInstanceSpec associatedFunctionalTypes(
      @Nullable List<String> associatedFunctionalTypes) {
    this.associatedFunctionalTypes = associatedFunctionalTypes;
    return this;
  }

  public V1ComponentInstanceSpec addAssociatedFunctionalTypesItem(
      String associatedFunctionalTypesItem) {
    if (this.associatedFunctionalTypes == null) {
      this.associatedFunctionalTypes = new ArrayList<>();
    }
    this.associatedFunctionalTypes.add(associatedFunctionalTypesItem);
    return this;
  }

  /**
   * List of functional type where to instantiate
   *
   * @return associatedFunctionalTypes
   */
  @Schema(required = false, description = "List of functional type where to instantiate")
  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_FUNCTIONAL_TYPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getAssociatedFunctionalTypes() {
    return associatedFunctionalTypes;
  }

  @JsonProperty(value = JSON_PROPERTY_ASSOCIATED_FUNCTIONAL_TYPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAssociatedFunctionalTypes(@Nullable List<String> associatedFunctionalTypes) {
    this.associatedFunctionalTypes = associatedFunctionalTypes;
  }

  public V1ComponentInstanceSpec excludedFunctionalTypes(
      @Nullable List<String> excludedFunctionalTypes) {
    this.excludedFunctionalTypes = excludedFunctionalTypes;
    return this;
  }

  public V1ComponentInstanceSpec addExcludedFunctionalTypesItem(
      String excludedFunctionalTypesItem) {
    if (this.excludedFunctionalTypes == null) {
      this.excludedFunctionalTypes = new ArrayList<>();
    }
    this.excludedFunctionalTypes.add(excludedFunctionalTypesItem);
    return this;
  }

  /**
   * List of functional type where to exclude from instantiation
   *
   * @return excludedFunctionalTypes
   */
  @Schema(
      required = false,
      description = "List of functional type where to exclude from instantiation")
  @JsonProperty(value = JSON_PROPERTY_EXCLUDED_FUNCTIONAL_TYPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getExcludedFunctionalTypes() {
    return excludedFunctionalTypes;
  }

  @JsonProperty(value = JSON_PROPERTY_EXCLUDED_FUNCTIONAL_TYPES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExcludedFunctionalTypes(@Nullable List<String> excludedFunctionalTypes) {
    this.excludedFunctionalTypes = excludedFunctionalTypes;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToLegacyOnly(
      @Nullable Boolean isAssociatedPlatformLimitedToLegacyOnly) {
    this.isAssociatedPlatformLimitedToLegacyOnly = isAssociatedPlatformLimitedToLegacyOnly;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the Legacy target
   *
   * @return isAssociatedPlatformLimitedToLegacyOnly
   */
  @Schema(
      required = false,
      description = "The automatic instantiation will only occurs in the Legacy target")
  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LEGACY_ONLY,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToLegacyOnly() {
    return isAssociatedPlatformLimitedToLegacyOnly;
  }

  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LEGACY_ONLY,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToLegacyOnly(
      @Nullable Boolean isAssociatedPlatformLimitedToLegacyOnly) {
    this.isAssociatedPlatformLimitedToLegacyOnly = isAssociatedPlatformLimitedToLegacyOnly;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToCloudOnly(
      @Nullable Boolean isAssociatedPlatformLimitedToCloudOnly) {
    this.isAssociatedPlatformLimitedToCloudOnly = isAssociatedPlatformLimitedToCloudOnly;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the cloud target
   *
   * @return isAssociatedPlatformLimitedToCloudOnly
   */
  @Schema(
      required = false,
      description = "The automatic instantiation will only occurs in the cloud target")
  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CLOUD_ONLY,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToCloudOnly() {
    return isAssociatedPlatformLimitedToCloudOnly;
  }

  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CLOUD_ONLY,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToCloudOnly(
      @Nullable Boolean isAssociatedPlatformLimitedToCloudOnly) {
    this.isAssociatedPlatformLimitedToCloudOnly = isAssociatedPlatformLimitedToCloudOnly;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToLandingzone(
      @Nullable Boolean isAssociatedPlatformLimitedToLandingzone) {
    this.isAssociatedPlatformLimitedToLandingzone = isAssociatedPlatformLimitedToLandingzone;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the landingzone of the generic instance
   *
   * @return isAssociatedPlatformLimitedToLandingzone
   */
  @Schema(
      required = false,
      description =
          "The automatic instantiation will only occurs in the landingzone of the generic instance")
  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LANDINGZONE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToLandingzone() {
    return isAssociatedPlatformLimitedToLandingzone;
  }

  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_LANDINGZONE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToLandingzone(
      @Nullable Boolean isAssociatedPlatformLimitedToLandingzone) {
    this.isAssociatedPlatformLimitedToLandingzone = isAssociatedPlatformLimitedToLandingzone;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToTarget(
      @Nullable Boolean isAssociatedPlatformLimitedToTarget) {
    this.isAssociatedPlatformLimitedToTarget = isAssociatedPlatformLimitedToTarget;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the target of the generic instance
   *
   * @return isAssociatedPlatformLimitedToTarget
   */
  @Schema(
      required = false,
      description =
          "The automatic instantiation will only occurs in the target of the generic instance")
  @JsonProperty(value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToTarget() {
    return isAssociatedPlatformLimitedToTarget;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToTarget(
      @Nullable Boolean isAssociatedPlatformLimitedToTarget) {
    this.isAssociatedPlatformLimitedToTarget = isAssociatedPlatformLimitedToTarget;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToContinent(
      @Nullable Boolean isAssociatedPlatformLimitedToContinent) {
    this.isAssociatedPlatformLimitedToContinent = isAssociatedPlatformLimitedToContinent;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the continent of the generic instance
   *
   * @return isAssociatedPlatformLimitedToContinent
   */
  @Schema(
      required = false,
      description =
          "The automatic instantiation will only occurs in the continent of the generic instance")
  @JsonProperty(value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToContinent() {
    return isAssociatedPlatformLimitedToContinent;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToContinent(
      @Nullable Boolean isAssociatedPlatformLimitedToContinent) {
    this.isAssociatedPlatformLimitedToContinent = isAssociatedPlatformLimitedToContinent;
  }

  public V1ComponentInstanceSpec isAssociatedPlatformLimitedToMacrophase(
      @Nullable Boolean isAssociatedPlatformLimitedToMacrophase) {
    this.isAssociatedPlatformLimitedToMacrophase = isAssociatedPlatformLimitedToMacrophase;
    return this;
  }

  /**
   * The automatic instantiation will only occurs in the macrophase of the generic instance
   *
   * @return isAssociatedPlatformLimitedToMacrophase
   */
  @Schema(
      required = false,
      description =
          "The automatic instantiation will only occurs in the macrophase of the generic instance")
  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_MACROPHASE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsAssociatedPlatformLimitedToMacrophase() {
    return isAssociatedPlatformLimitedToMacrophase;
  }

  @JsonProperty(
      value = JSON_PROPERTY_IS_ASSOCIATED_PLATFORM_LIMITED_TO_MACROPHASE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsAssociatedPlatformLimitedToMacrophase(
      @Nullable Boolean isAssociatedPlatformLimitedToMacrophase) {
    this.isAssociatedPlatformLimitedToMacrophase = isAssociatedPlatformLimitedToMacrophase;
  }

  public V1ComponentInstanceSpec deprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
    return this;
  }

  /**
   * Get deprecatedStatus
   *
   * @return deprecatedStatus
   */
  @Valid
  @Schema(required = false, description = "Deprecated status of the component instance.")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return deprecatedStatus;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public V1ComponentInstanceSpec deprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
    return this;
  }

  /**
   * Additional info on the deprecated component instance (e.g. replacement information/ owner
   * contact details)
   *
   * @return deprecatedMessage
   */
  @Schema(
      required = false,
      description =
          "Additional info on the deprecated component instance (e.g. replacement information/ owner contact details)")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeprecatedMessage() {
    return deprecatedMessage;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
  }

  public V1ComponentInstanceSpec endpoints(@Nullable List<Object> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public V1ComponentInstanceSpec addEndpointsItem(Object endpointsItem) {
    if (this.endpoints == null) {
      this.endpoints = new ArrayList<>();
    }
    this.endpoints.add(endpointsItem);
    return this;
  }

  /**
   * List of endpoints. Content depends on the endpoint_type
   *
   * @return endpoints
   */
  @Schema(required = false, description = "List of endpoints. Content depends on the endpoint_type")
  @JsonProperty(value = JSON_PROPERTY_ENDPOINTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<Object> getEndpoints() {
    return endpoints;
  }

  @JsonProperty(value = JSON_PROPERTY_ENDPOINTS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEndpoints(@Nullable List<Object> endpoints) {
    this.endpoints = endpoints;
  }

  public V1ComponentInstanceSpec sourcesSelectors(
      @Nullable List<Map<String, String>> sourcesSelectors) {
    this.sourcesSelectors = sourcesSelectors;
    return this;
  }

  public V1ComponentInstanceSpec addSourcesSelectorsItem(Map<String, String> sourcesSelectorsItem) {
    if (this.sourcesSelectors == null) {
      this.sourcesSelectors = new ArrayList<>();
    }
    this.sourcesSelectors.add(sourcesSelectorsItem);
    return this;
  }

  /**
   * List of labels selectors to select component instance that will be able to connect if the
   * dependency instance is defined with is_source_limited_to_target_selector.
   *
   * @return sourcesSelectors
   */
  @Valid
  @Schema(
      required = false,
      description =
          "List of labels selectors to select component instance that will be able to connect if the dependency instance is defined with is_source_limited_to_target_selector.")
  @JsonProperty(value = JSON_PROPERTY_SOURCES_SELECTORS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<Map<String, String>> getSourcesSelectors() {
    return sourcesSelectors;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCES_SELECTORS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSourcesSelectors(@Nullable List<Map<String, String>> sourcesSelectors) {
    this.sourcesSelectors = sourcesSelectors;
  }

  /** Return true if this v1.ComponentInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ComponentInstanceSpec v1ComponentInstanceSpec = (V1ComponentInstanceSpec) o;
    return Objects.equals(this.component, v1ComponentInstanceSpec.component)
        && Objects.equals(this.applicationPhase, v1ComponentInstanceSpec.applicationPhase)
        && Objects.equals(this.inSubnetInstance, v1ComponentInstanceSpec.inSubnetInstance)
        && Objects.equals(this.outSubnetInstance, v1ComponentInstanceSpec.outSubnetInstance)
        && Objects.equals(this.inZoneInstance, v1ComponentInstanceSpec.inZoneInstance)
        && Objects.equals(this.outZoneInstance, v1ComponentInstanceSpec.outZoneInstance)
        && Objects.equals(this.servicetag, v1ComponentInstanceSpec.servicetag)
        && Objects.equals(this.hostingComponent, v1ComponentInstanceSpec.hostingComponent)
        && Objects.equals(this.hostingComponentType, v1ComponentInstanceSpec.hostingComponentType)
        && Objects.equals(
            this.hostingComponentSecurity, v1ComponentInstanceSpec.hostingComponentSecurity)
        && Objects.equals(this.zoneType, v1ComponentInstanceSpec.zoneType)
        && Objects.equals(this.namespace, v1ComponentInstanceSpec.namespace)
        && Objects.equals(this.k8sIdentifier, v1ComponentInstanceSpec.k8sIdentifier)
        && Objects.equals(this.k8sSelector, v1ComponentInstanceSpec.k8sSelector)
        && Objects.equals(this.overlaySubnets, v1ComponentInstanceSpec.overlaySubnets)
        && Objects.equals(this.endpointType, v1ComponentInstanceSpec.endpointType)
        && Objects.equals(
            this.associatedImplementationZone, v1ComponentInstanceSpec.associatedImplementationZone)
        && Objects.equals(
            this.associatedPlatformComponent, v1ComponentInstanceSpec.associatedPlatformComponent)
        && Objects.equals(
            this.associatedFunctionalTypes, v1ComponentInstanceSpec.associatedFunctionalTypes)
        && Objects.equals(
            this.excludedFunctionalTypes, v1ComponentInstanceSpec.excludedFunctionalTypes)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToLegacyOnly,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToLegacyOnly)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToCloudOnly,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToCloudOnly)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToLandingzone,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToLandingzone)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToTarget,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToTarget)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToContinent,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToContinent)
        && Objects.equals(
            this.isAssociatedPlatformLimitedToMacrophase,
            v1ComponentInstanceSpec.isAssociatedPlatformLimitedToMacrophase)
        && Objects.equals(this.deprecatedStatus, v1ComponentInstanceSpec.deprecatedStatus)
        && Objects.equals(this.deprecatedMessage, v1ComponentInstanceSpec.deprecatedMessage)
        && Objects.equals(this.endpoints, v1ComponentInstanceSpec.endpoints)
        && Objects.equals(this.sourcesSelectors, v1ComponentInstanceSpec.sourcesSelectors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        component,
        applicationPhase,
        inSubnetInstance,
        outSubnetInstance,
        inZoneInstance,
        outZoneInstance,
        servicetag,
        hostingComponent,
        hostingComponentType,
        hostingComponentSecurity,
        zoneType,
        namespace,
        k8sIdentifier,
        k8sSelector,
        overlaySubnets,
        endpointType,
        associatedImplementationZone,
        associatedPlatformComponent,
        associatedFunctionalTypes,
        excludedFunctionalTypes,
        isAssociatedPlatformLimitedToLegacyOnly,
        isAssociatedPlatformLimitedToCloudOnly,
        isAssociatedPlatformLimitedToLandingzone,
        isAssociatedPlatformLimitedToTarget,
        isAssociatedPlatformLimitedToContinent,
        isAssociatedPlatformLimitedToMacrophase,
        deprecatedStatus,
        deprecatedMessage,
        endpoints,
        sourcesSelectors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ComponentInstanceSpec {\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    applicationPhase: ").append(toIndentedString(applicationPhase)).append("\n");
    sb.append("    inSubnetInstance: ").append(toIndentedString(inSubnetInstance)).append("\n");
    sb.append("    outSubnetInstance: ").append(toIndentedString(outSubnetInstance)).append("\n");
    sb.append("    inZoneInstance: ").append(toIndentedString(inZoneInstance)).append("\n");
    sb.append("    outZoneInstance: ").append(toIndentedString(outZoneInstance)).append("\n");
    sb.append("    servicetag: ").append(toIndentedString(servicetag)).append("\n");
    sb.append("    hostingComponent: ").append(toIndentedString(hostingComponent)).append("\n");
    sb.append("    hostingComponentType: ")
        .append(toIndentedString(hostingComponentType))
        .append("\n");
    sb.append("    hostingComponentSecurity: ")
        .append(toIndentedString(hostingComponentSecurity))
        .append("\n");
    sb.append("    zoneType: ").append(toIndentedString(zoneType)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    k8sIdentifier: ").append(toIndentedString(k8sIdentifier)).append("\n");
    sb.append("    k8sSelector: ").append(toIndentedString(k8sSelector)).append("\n");
    sb.append("    overlaySubnets: ").append(toIndentedString(overlaySubnets)).append("\n");
    sb.append("    endpointType: ").append(toIndentedString(endpointType)).append("\n");
    sb.append("    associatedImplementationZone: ")
        .append(toIndentedString(associatedImplementationZone))
        .append("\n");
    sb.append("    associatedPlatformComponent: ")
        .append(toIndentedString(associatedPlatformComponent))
        .append("\n");
    sb.append("    associatedFunctionalTypes: ")
        .append(toIndentedString(associatedFunctionalTypes))
        .append("\n");
    sb.append("    excludedFunctionalTypes: ")
        .append(toIndentedString(excludedFunctionalTypes))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToLegacyOnly: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToLegacyOnly))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToCloudOnly: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToCloudOnly))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToLandingzone: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToLandingzone))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToTarget: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToTarget))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToContinent: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToContinent))
        .append("\n");
    sb.append("    isAssociatedPlatformLimitedToMacrophase: ")
        .append(toIndentedString(isAssociatedPlatformLimitedToMacrophase))
        .append("\n");
    sb.append("    deprecatedStatus: ").append(toIndentedString(deprecatedStatus)).append("\n");
    sb.append("    deprecatedMessage: ").append(toIndentedString(deprecatedMessage)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
    sb.append("    sourcesSelectors: ").append(toIndentedString(sourcesSelectors)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}
