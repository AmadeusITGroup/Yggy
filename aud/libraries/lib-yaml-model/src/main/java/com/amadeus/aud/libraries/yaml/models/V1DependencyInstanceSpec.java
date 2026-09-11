package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** DependencyInstance specification */
@Schema(description = "DependencyInstance specification")
@JsonPropertyOrder({
  V1DependencyInstanceSpec.JSON_PROPERTY_DELEGATE_GOVERNANCE_TO,
  V1DependencyInstanceSpec.JSON_PROPERTY_VIA_COMPONENT_INSTANCE,
  V1DependencyInstanceSpec.JSON_PROPERTY_VIA_COMPONENT_INSTANCE_LANDING_ZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_VIA_SERVICE,
  V1DependencyInstanceSpec.JSON_PROPERTY_COMPONENT_INSTANCE,
  V1DependencyInstanceSpec.JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_AS_COMPONENT,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDINGZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACROPHASE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR,
  V1DependencyInstanceSpec.JSON_PROPERTY_DEPENDENCY,
  V1DependencyInstanceSpec.JSON_PROPERTY_TARGET_COMPONENT_INSTANCE,
  V1DependencyInstanceSpec.JSON_PROPERTY_TARGET_COMPONENT_INSTANCE_LANDING_ZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_AS_COMPONENT,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY,
  V1DependencyInstanceSpec.JSON_PROPERTY_TARGET_SERVICE,
  V1DependencyInstanceSpec.JSON_PROPERTY_TARGET_SERVICES,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITYZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATIONPHASE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDINGZONE,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT,
  V1DependencyInstanceSpec.JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACROPHASE,
  V1DependencyInstanceSpec.JSON_PROPERTY_SOURCES_SELECTORS,
  V1DependencyInstanceSpec.JSON_PROPERTY_TARGETS_SELECTORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1DependencyInstanceSpec {
  /** Gets or Sets delegateGovernanceTo */
  public enum DelegateGovernanceToEnum {
    OWNER(String.valueOf("Owner")),

    SOURCE(String.valueOf("Source")),

    DESTINATION(String.valueOf("Destination"));

    private String value;

    DelegateGovernanceToEnum(String value) {
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
    public static DelegateGovernanceToEnum fromValue(String value) {
      for (DelegateGovernanceToEnum b : DelegateGovernanceToEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_DELEGATE_GOVERNANCE_TO = "delegateGovernanceTo";
  @Nullable private DelegateGovernanceToEnum delegateGovernanceTo = DelegateGovernanceToEnum.OWNER;

  public static final String JSON_PROPERTY_VIA_COMPONENT_INSTANCE = "viaComponentInstance";
  @Nullable private String viaComponentInstance;

  public static final String JSON_PROPERTY_VIA_COMPONENT_INSTANCE_LANDING_ZONE =
      "viaComponentInstanceLandingZone";
  @Nullable private String viaComponentInstanceLandingZone;

  public static final String JSON_PROPERTY_VIA_SERVICE = "viaService";
  @Nullable private String viaService;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCE = "componentInstance";
  @Nullable private String componentInstance;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE =
      "componentInstanceLandingZone";
  @Nullable private String componentInstanceLandingZone;

  public static final String JSON_PROPERTY_IS_SOURCE_AS_COMPONENT = "isSourceAsComponent";
  @Nullable private Boolean isSourceAsComponent = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY =
      "isSourceLimitedToLegacyOnly";
  @Nullable private Boolean isSourceLimitedToLegacyOnly = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY =
      "isSourceLimitedToCloudOnly";
  @Nullable private Boolean isSourceLimitedToCloudOnly = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDINGZONE =
      "isSourceLimitedToLandingzone";
  @Nullable private Boolean isSourceLimitedToLandingzone = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET = "isSourceLimitedToTarget";
  @Nullable private Boolean isSourceLimitedToTarget = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT =
      "isSourceLimitedToContinent";
  @Nullable private Boolean isSourceLimitedToContinent = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACROPHASE =
      "isSourceLimitedToMacrophase";
  @Nullable private Boolean isSourceLimitedToMacrophase = false;

  public static final String JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR =
      "isSourceLimitedToSelector";
  @Nullable private Boolean isSourceLimitedToSelector = false;

  public static final String JSON_PROPERTY_DEPENDENCY = "dependency";
  @Nullable private String dependency;

  public static final String JSON_PROPERTY_TARGET_COMPONENT_INSTANCE = "targetComponentInstance";
  @Nullable private String targetComponentInstance;

  public static final String JSON_PROPERTY_TARGET_COMPONENT_INSTANCE_LANDING_ZONE =
      "targetComponentInstanceLandingZone";
  @Nullable private String targetComponentInstanceLandingZone;

  public static final String JSON_PROPERTY_IS_TARGET_AS_COMPONENT = "isTargetAsComponent";
  @Nullable private Boolean isTargetAsComponent = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY =
      "isTargetLimitedToLegacyOnly";
  @Nullable private Boolean isTargetLimitedToLegacyOnly = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY =
      "isTargetLimitedToCloudOnly";
  @Nullable private Boolean isTargetLimitedToCloudOnly = false;

  public static final String JSON_PROPERTY_TARGET_SERVICE = "targetService";
  @Nullable private String targetService;

  public static final String JSON_PROPERTY_TARGET_SERVICES = "targetServices";
  @Nullable private List<String> targetServices;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE =
      "isTargetLimitedToSourceNamespace";
  @Nullable private Boolean isTargetLimitedToSourceNamespace = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS =
      "isTargetLimitedToSourcePaas";
  @Nullable private Boolean isTargetLimitedToSourcePaas = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITYZONE =
      "isTargetLimitedToSourceSecurityzone";
  @Nullable private Boolean isTargetLimitedToSourceSecurityzone = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATIONPHASE =
      "isTargetLimitedToSourceApplicationphase";
  @Nullable private Boolean isTargetLimitedToSourceApplicationphase = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDINGZONE =
      "isTargetLimitedToSourceLandingzone";
  @Nullable private Boolean isTargetLimitedToSourceLandingzone = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET =
      "isTargetLimitedToSourceTarget";
  @Nullable private Boolean isTargetLimitedToSourceTarget = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT =
      "isTargetLimitedToSourceContinent";
  @Nullable private Boolean isTargetLimitedToSourceContinent = false;

  public static final String JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACROPHASE =
      "isTargetLimitedToSourceMacrophase";
  @Nullable private Boolean isTargetLimitedToSourceMacrophase = false;

  public static final String JSON_PROPERTY_SOURCES_SELECTORS = "sources_selectors";
  @Nullable private List<Map<String, String>> sourcesSelectors;

  public static final String JSON_PROPERTY_TARGETS_SELECTORS = "targets_selectors";
  @Nullable private List<Map<String, String>> targetsSelectors;

  public V1DependencyInstanceSpec() {}

  public V1DependencyInstanceSpec delegateGovernanceTo(
      @Nullable DelegateGovernanceToEnum delegateGovernanceTo) {
    this.delegateGovernanceTo = delegateGovernanceTo;
    return this;
  }

  /**
   * Get delegateGovernanceTo
   *
   * @return delegateGovernanceTo
   */
  @Schema(required = false, description = "Scope to which governance is delegated.")
  @JsonProperty(value = JSON_PROPERTY_DELEGATE_GOVERNANCE_TO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable DelegateGovernanceToEnum getDelegateGovernanceTo() {
    return delegateGovernanceTo;
  }

  @JsonProperty(value = JSON_PROPERTY_DELEGATE_GOVERNANCE_TO, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDelegateGovernanceTo(@Nullable DelegateGovernanceToEnum delegateGovernanceTo) {
    this.delegateGovernanceTo = delegateGovernanceTo;
  }

  public V1DependencyInstanceSpec viaComponentInstance(@Nullable String viaComponentInstance) {
    this.viaComponentInstance = viaComponentInstance;
    return this;
  }

  /**
   * Name of the component instance used for the VIA feature (transparent Transit NVA)
   *
   * @return viaComponentInstance
   */
  @Schema(
      required = false,
      description =
          "Name of the component instance used for the VIA feature (transparent Transit NVA)")
  @JsonProperty(value = JSON_PROPERTY_VIA_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getViaComponentInstance() {
    return viaComponentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_VIA_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setViaComponentInstance(@Nullable String viaComponentInstance) {
    this.viaComponentInstance = viaComponentInstance;
  }

  public V1DependencyInstanceSpec viaComponentInstanceLandingZone(
      @Nullable String viaComponentInstanceLandingZone) {
    this.viaComponentInstanceLandingZone = viaComponentInstanceLandingZone;
    return this;
  }

  /**
   * Name of the component Instance LandingZone used for the VIA feature (transparent Transit NVA)
   * (for future use)
   *
   * @return viaComponentInstanceLandingZone
   */
  @Schema(
      required = false,
      description =
          "Name of the component Instance LandingZone used for the VIA feature (transparent Transit NVA) (for future use)")
  @JsonProperty(value = JSON_PROPERTY_VIA_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getViaComponentInstanceLandingZone() {
    return viaComponentInstanceLandingZone;
  }

  @JsonProperty(value = JSON_PROPERTY_VIA_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setViaComponentInstanceLandingZone(@Nullable String viaComponentInstanceLandingZone) {
    this.viaComponentInstanceLandingZone = viaComponentInstanceLandingZone;
  }

  public V1DependencyInstanceSpec viaService(@Nullable String viaService) {
    this.viaService = viaService;
    return this;
  }

  /**
   * Name of the service targetted on the VIA component
   *
   * @return viaService
   */
  @Schema(required = false, description = "Name of the service targetted on the VIA component")
  @JsonProperty(value = JSON_PROPERTY_VIA_SERVICE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getViaService() {
    return viaService;
  }

  @JsonProperty(value = JSON_PROPERTY_VIA_SERVICE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setViaService(@Nullable String viaService) {
    this.viaService = viaService;
  }

  public V1DependencyInstanceSpec componentInstance(@Nullable String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Name of the source component instance
   *
   * @return componentInstance
   */
  @Schema(required = false, description = "Name of the source component instance")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getComponentInstance() {
    return componentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComponentInstance(@Nullable String componentInstance) {
    this.componentInstance = componentInstance;
  }

  public V1DependencyInstanceSpec componentInstanceLandingZone(
      @Nullable String componentInstanceLandingZone) {
    this.componentInstanceLandingZone = componentInstanceLandingZone;
    return this;
  }

  /**
   * Name of the source component Instance LandingZone (for future use)
   *
   * @return componentInstanceLandingZone
   */
  @Schema(
      required = false,
      description = "Name of the source component Instance LandingZone (for future use)")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getComponentInstanceLandingZone() {
    return componentInstanceLandingZone;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComponentInstanceLandingZone(@Nullable String componentInstanceLandingZone) {
    this.componentInstanceLandingZone = componentInstanceLandingZone;
  }

  public V1DependencyInstanceSpec isSourceAsComponent(@Nullable Boolean isSourceAsComponent) {
    this.isSourceAsComponent = isSourceAsComponent;
    return this;
  }

  /**
   * Generic source ? take into account all the instances of the source component
   *
   * @return isSourceAsComponent
   */
  @Schema(
      required = false,
      description = "Generic source ? take into account all the instances of the source component")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_AS_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceAsComponent() {
    return isSourceAsComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_AS_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceAsComponent(@Nullable Boolean isSourceAsComponent) {
    this.isSourceAsComponent = isSourceAsComponent;
  }

  public V1DependencyInstanceSpec isSourceLimitedToLegacyOnly(
      @Nullable Boolean isSourceLimitedToLegacyOnly) {
    this.isSourceLimitedToLegacyOnly = isSourceLimitedToLegacyOnly;
    return this;
  }

  /**
   * is the source instance limited to legacy on-premises systems
   *
   * @return isSourceLimitedToLegacyOnly
   */
  @Schema(
      required = false,
      description = "is the source instance limited to legacy on-premises systems")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToLegacyOnly() {
    return isSourceLimitedToLegacyOnly;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LEGACY_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToLegacyOnly(@Nullable Boolean isSourceLimitedToLegacyOnly) {
    this.isSourceLimitedToLegacyOnly = isSourceLimitedToLegacyOnly;
  }

  public V1DependencyInstanceSpec isSourceLimitedToCloudOnly(
      @Nullable Boolean isSourceLimitedToCloudOnly) {
    this.isSourceLimitedToCloudOnly = isSourceLimitedToCloudOnly;
    return this;
  }

  /**
   * is source instance limited to the cloud systems (non legacy mainly)
   *
   * @return isSourceLimitedToCloudOnly
   */
  @Schema(
      required = false,
      description = "is source instance limited to the cloud systems (non legacy mainly)")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToCloudOnly() {
    return isSourceLimitedToCloudOnly;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CLOUD_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToCloudOnly(@Nullable Boolean isSourceLimitedToCloudOnly) {
    this.isSourceLimitedToCloudOnly = isSourceLimitedToCloudOnly;
  }

  public V1DependencyInstanceSpec isSourceLimitedToLandingzone(
      @Nullable Boolean isSourceLimitedToLandingzone) {
    this.isSourceLimitedToLandingzone = isSourceLimitedToLandingzone;
    return this;
  }

  /**
   * is source instance limited to the landing zone of the dependency instance
   *
   * @return isSourceLimitedToLandingzone
   */
  @Schema(
      required = false,
      description = "is source instance limited to the landing zone of the dependency instance")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDINGZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToLandingzone() {
    return isSourceLimitedToLandingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_LANDINGZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToLandingzone(@Nullable Boolean isSourceLimitedToLandingzone) {
    this.isSourceLimitedToLandingzone = isSourceLimitedToLandingzone;
  }

  public V1DependencyInstanceSpec isSourceLimitedToTarget(
      @Nullable Boolean isSourceLimitedToTarget) {
    this.isSourceLimitedToTarget = isSourceLimitedToTarget;
    return this;
  }

  /**
   * is source instance limited to the target of the dependency instance
   *
   * @return isSourceLimitedToTarget
   */
  @Schema(
      required = false,
      description = "is source instance limited to the target of the dependency instance")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToTarget() {
    return isSourceLimitedToTarget;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToTarget(@Nullable Boolean isSourceLimitedToTarget) {
    this.isSourceLimitedToTarget = isSourceLimitedToTarget;
  }

  public V1DependencyInstanceSpec isSourceLimitedToContinent(
      @Nullable Boolean isSourceLimitedToContinent) {
    this.isSourceLimitedToContinent = isSourceLimitedToContinent;
    return this;
  }

  /**
   * is source instance limited to the continent of the dependency instance
   *
   * @return isSourceLimitedToContinent
   */
  @Schema(
      required = false,
      description = "is source instance limited to the continent of the dependency instance")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToContinent() {
    return isSourceLimitedToContinent;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToContinent(@Nullable Boolean isSourceLimitedToContinent) {
    this.isSourceLimitedToContinent = isSourceLimitedToContinent;
  }

  public V1DependencyInstanceSpec isSourceLimitedToMacrophase(
      @Nullable Boolean isSourceLimitedToMacrophase) {
    this.isSourceLimitedToMacrophase = isSourceLimitedToMacrophase;
    return this;
  }

  /**
   * is source instance limited to the macrophase of the dependency instance
   *
   * @return isSourceLimitedToMacrophase
   */
  @Schema(
      required = false,
      description = "is source instance limited to the macrophase of the dependency instance")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACROPHASE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToMacrophase() {
    return isSourceLimitedToMacrophase;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_MACROPHASE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToMacrophase(@Nullable Boolean isSourceLimitedToMacrophase) {
    this.isSourceLimitedToMacrophase = isSourceLimitedToMacrophase;
  }

  public V1DependencyInstanceSpec isSourceLimitedToSelector(
      @Nullable Boolean isSourceLimitedToSelector) {
    this.isSourceLimitedToSelector = isSourceLimitedToSelector;
    return this;
  }

  /**
   * is source instance limited to the selectors defined in the destination component instance
   *
   * @return isSourceLimitedToSelector
   */
  @Schema(
      required = false,
      description =
          "is source instance limited to the selectors defined in the destination component instance")
  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsSourceLimitedToSelector() {
    return isSourceLimitedToSelector;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_SOURCE_LIMITED_TO_SELECTOR, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsSourceLimitedToSelector(@Nullable Boolean isSourceLimitedToSelector) {
    this.isSourceLimitedToSelector = isSourceLimitedToSelector;
  }

  public V1DependencyInstanceSpec dependency(@Nullable String dependency) {
    this.dependency = dependency;
    return this;
  }

  /**
   * Name of the dependency
   *
   * @return dependency
   */
  @Schema(required = false, description = "Name of the dependency")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDependency() {
    return dependency;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDependency(@Nullable String dependency) {
    this.dependency = dependency;
  }

  public V1DependencyInstanceSpec targetComponentInstance(
      @Nullable String targetComponentInstance) {
    this.targetComponentInstance = targetComponentInstance;
    return this;
  }

  /**
   * Name of the target Component Instance
   *
   * @return targetComponentInstance
   */
  @Schema(required = false, description = "Name of the target Component Instance")
  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getTargetComponentInstance() {
    return targetComponentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT_INSTANCE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargetComponentInstance(@Nullable String targetComponentInstance) {
    this.targetComponentInstance = targetComponentInstance;
  }

  public V1DependencyInstanceSpec targetComponentInstanceLandingZone(
      @Nullable String targetComponentInstanceLandingZone) {
    this.targetComponentInstanceLandingZone = targetComponentInstanceLandingZone;
    return this;
  }

  /**
   * Name of the target Component Instance Landing Zone (for future use)
   *
   * @return targetComponentInstanceLandingZone
   */
  @Schema(
      required = false,
      description = "Name of the target Component Instance Landing Zone (for future use)")
  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getTargetComponentInstanceLandingZone() {
    return targetComponentInstanceLandingZone;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_COMPONENT_INSTANCE_LANDING_ZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargetComponentInstanceLandingZone(
      @Nullable String targetComponentInstanceLandingZone) {
    this.targetComponentInstanceLandingZone = targetComponentInstanceLandingZone;
  }

  public V1DependencyInstanceSpec isTargetAsComponent(@Nullable Boolean isTargetAsComponent) {
    this.isTargetAsComponent = isTargetAsComponent;
    return this;
  }

  /**
   * Generic target ? take into account all the instances of the target component
   *
   * @return isTargetAsComponent
   */
  @Schema(
      required = false,
      description = "Generic target ? take into account all the instances of the target component")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_AS_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetAsComponent() {
    return isTargetAsComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_AS_COMPONENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetAsComponent(@Nullable Boolean isTargetAsComponent) {
    this.isTargetAsComponent = isTargetAsComponent;
  }

  public V1DependencyInstanceSpec isTargetLimitedToLegacyOnly(
      @Nullable Boolean isTargetLimitedToLegacyOnly) {
    this.isTargetLimitedToLegacyOnly = isTargetLimitedToLegacyOnly;
    return this;
  }

  /**
   * is the target instance limited to legacy on-premises systems
   *
   * @return isTargetLimitedToLegacyOnly
   */
  @Schema(
      required = false,
      description = "is the target instance limited to legacy on-premises systems")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToLegacyOnly() {
    return isTargetLimitedToLegacyOnly;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_LEGACY_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToLegacyOnly(@Nullable Boolean isTargetLimitedToLegacyOnly) {
    this.isTargetLimitedToLegacyOnly = isTargetLimitedToLegacyOnly;
  }

  public V1DependencyInstanceSpec isTargetLimitedToCloudOnly(
      @Nullable Boolean isTargetLimitedToCloudOnly) {
    this.isTargetLimitedToCloudOnly = isTargetLimitedToCloudOnly;
    return this;
  }

  /**
   * is target instance limited to the cloud systems (non legacy mainly)
   *
   * @return isTargetLimitedToCloudOnly
   */
  @Schema(
      required = false,
      description = "is target instance limited to the cloud systems (non legacy mainly)")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToCloudOnly() {
    return isTargetLimitedToCloudOnly;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_CLOUD_ONLY, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToCloudOnly(@Nullable Boolean isTargetLimitedToCloudOnly) {
    this.isTargetLimitedToCloudOnly = isTargetLimitedToCloudOnly;
  }

  public V1DependencyInstanceSpec targetService(@Nullable String targetService) {
    this.targetService = targetService;
    return this;
  }

  /**
   * Name of the target service
   *
   * @return targetService
   */
  @Schema(required = false, description = "Name of the target service")
  @JsonProperty(value = JSON_PROPERTY_TARGET_SERVICE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getTargetService() {
    return targetService;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_SERVICE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargetService(@Nullable String targetService) {
    this.targetService = targetService;
  }

  public V1DependencyInstanceSpec targetServices(@Nullable List<String> targetServices) {
    this.targetServices = targetServices;
    return this;
  }

  public V1DependencyInstanceSpec addTargetServicesItem(String targetServicesItem) {
    if (this.targetServices == null) {
      this.targetServices = new ArrayList<>();
    }
    this.targetServices.add(targetServicesItem);
    return this;
  }

  /**
   * List of targeted services
   *
   * @return targetServices
   */
  @Schema(required = false, description = "List of targeted services")
  @JsonProperty(value = JSON_PROPERTY_TARGET_SERVICES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getTargetServices() {
    return targetServices;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET_SERVICES, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargetServices(@Nullable List<String> targetServices) {
    this.targetServices = targetServices;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceNamespace(
      @Nullable Boolean isTargetLimitedToSourceNamespace) {
    this.isTargetLimitedToSourceNamespace = isTargetLimitedToSourceNamespace;
    return this;
  }

  /**
   * is target instances limited to the source namespace
   *
   * @return isTargetLimitedToSourceNamespace
   */
  @Schema(required = false, description = "is target instances limited to the source namespace")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceNamespace() {
    return isTargetLimitedToSourceNamespace;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_NAMESPACE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceNamespace(
      @Nullable Boolean isTargetLimitedToSourceNamespace) {
    this.isTargetLimitedToSourceNamespace = isTargetLimitedToSourceNamespace;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourcePaas(
      @Nullable Boolean isTargetLimitedToSourcePaas) {
    this.isTargetLimitedToSourcePaas = isTargetLimitedToSourcePaas;
    return this;
  }

  /**
   * is target instances limited to the source paas
   *
   * @return isTargetLimitedToSourcePaas
   */
  @Schema(required = false, description = "is target instances limited to the source paas")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourcePaas() {
    return isTargetLimitedToSourcePaas;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_PAAS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourcePaas(@Nullable Boolean isTargetLimitedToSourcePaas) {
    this.isTargetLimitedToSourcePaas = isTargetLimitedToSourcePaas;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceSecurityzone(
      @Nullable Boolean isTargetLimitedToSourceSecurityzone) {
    this.isTargetLimitedToSourceSecurityzone = isTargetLimitedToSourceSecurityzone;
    return this;
  }

  /**
   * is target instances limited to the source security zone
   *
   * @return isTargetLimitedToSourceSecurityzone
   */
  @Schema(required = false, description = "is target instances limited to the source security zone")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITYZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceSecurityzone() {
    return isTargetLimitedToSourceSecurityzone;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_SECURITYZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceSecurityzone(
      @Nullable Boolean isTargetLimitedToSourceSecurityzone) {
    this.isTargetLimitedToSourceSecurityzone = isTargetLimitedToSourceSecurityzone;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceApplicationphase(
      @Nullable Boolean isTargetLimitedToSourceApplicationphase) {
    this.isTargetLimitedToSourceApplicationphase = isTargetLimitedToSourceApplicationphase;
    return this;
  }

  /**
   * is target instances limited to the source applicationphase
   *
   * @return isTargetLimitedToSourceApplicationphase
   */
  @Schema(
      required = false,
      description = "is target instances limited to the source applicationphase")
  @JsonProperty(
      value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATIONPHASE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceApplicationphase() {
    return isTargetLimitedToSourceApplicationphase;
  }

  @JsonProperty(
      value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_APPLICATIONPHASE,
      required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceApplicationphase(
      @Nullable Boolean isTargetLimitedToSourceApplicationphase) {
    this.isTargetLimitedToSourceApplicationphase = isTargetLimitedToSourceApplicationphase;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceLandingzone(
      @Nullable Boolean isTargetLimitedToSourceLandingzone) {
    this.isTargetLimitedToSourceLandingzone = isTargetLimitedToSourceLandingzone;
    return this;
  }

  /**
   * is target instances limited to the source landing zone
   *
   * @return isTargetLimitedToSourceLandingzone
   */
  @Schema(required = false, description = "is target instances limited to the source landing zone")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDINGZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceLandingzone() {
    return isTargetLimitedToSourceLandingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_LANDINGZONE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceLandingzone(
      @Nullable Boolean isTargetLimitedToSourceLandingzone) {
    this.isTargetLimitedToSourceLandingzone = isTargetLimitedToSourceLandingzone;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceTarget(
      @Nullable Boolean isTargetLimitedToSourceTarget) {
    this.isTargetLimitedToSourceTarget = isTargetLimitedToSourceTarget;
    return this;
  }

  /**
   * is target instances limited to the source target
   *
   * @return isTargetLimitedToSourceTarget
   */
  @Schema(required = false, description = "is target instances limited to the source target")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceTarget() {
    return isTargetLimitedToSourceTarget;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_TARGET, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceTarget(@Nullable Boolean isTargetLimitedToSourceTarget) {
    this.isTargetLimitedToSourceTarget = isTargetLimitedToSourceTarget;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceContinent(
      @Nullable Boolean isTargetLimitedToSourceContinent) {
    this.isTargetLimitedToSourceContinent = isTargetLimitedToSourceContinent;
    return this;
  }

  /**
   * is target instances limited to the source continent
   *
   * @return isTargetLimitedToSourceContinent
   */
  @Schema(required = false, description = "is target instances limited to the source continent")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceContinent() {
    return isTargetLimitedToSourceContinent;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_CONTINENT, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceContinent(
      @Nullable Boolean isTargetLimitedToSourceContinent) {
    this.isTargetLimitedToSourceContinent = isTargetLimitedToSourceContinent;
  }

  public V1DependencyInstanceSpec isTargetLimitedToSourceMacrophase(
      @Nullable Boolean isTargetLimitedToSourceMacrophase) {
    this.isTargetLimitedToSourceMacrophase = isTargetLimitedToSourceMacrophase;
    return this;
  }

  /**
   * is target instances limited to the source macrophase
   *
   * @return isTargetLimitedToSourceMacrophase
   */
  @Schema(required = false, description = "is target instances limited to the source macrophase")
  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACROPHASE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsTargetLimitedToSourceMacrophase() {
    return isTargetLimitedToSourceMacrophase;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_TARGET_LIMITED_TO_SOURCE_MACROPHASE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsTargetLimitedToSourceMacrophase(
      @Nullable Boolean isTargetLimitedToSourceMacrophase) {
    this.isTargetLimitedToSourceMacrophase = isTargetLimitedToSourceMacrophase;
  }

  public V1DependencyInstanceSpec sourcesSelectors(
      @Nullable List<Map<String, String>> sourcesSelectors) {
    this.sourcesSelectors = sourcesSelectors;
    return this;
  }

  public V1DependencyInstanceSpec addSourcesSelectorsItem(
      Map<String, String> sourcesSelectorsItem) {
    if (this.sourcesSelectors == null) {
      this.sourcesSelectors = new ArrayList<>();
    }
    this.sourcesSelectors.add(sourcesSelectorsItem);
    return this;
  }

  /**
   * List of filtering on sources
   *
   * @return sourcesSelectors
   */
  @Valid
  @Schema(required = false, description = "List of filtering on sources")
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

  public V1DependencyInstanceSpec targetsSelectors(
      @Nullable List<Map<String, String>> targetsSelectors) {
    this.targetsSelectors = targetsSelectors;
    return this;
  }

  public V1DependencyInstanceSpec addTargetsSelectorsItem(
      Map<String, String> targetsSelectorsItem) {
    if (this.targetsSelectors == null) {
      this.targetsSelectors = new ArrayList<>();
    }
    this.targetsSelectors.add(targetsSelectorsItem);
    return this;
  }

  /**
   * List of filtering on targets
   *
   * @return targetsSelectors
   */
  @Valid
  @Schema(required = false, description = "List of filtering on targets")
  @JsonProperty(value = JSON_PROPERTY_TARGETS_SELECTORS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<Map<String, String>> getTargetsSelectors() {
    return targetsSelectors;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGETS_SELECTORS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTargetsSelectors(@Nullable List<Map<String, String>> targetsSelectors) {
    this.targetsSelectors = targetsSelectors;
  }

  /** Return true if this v1.DependencyInstanceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1DependencyInstanceSpec v1DependencyInstanceSpec = (V1DependencyInstanceSpec) o;
    return Objects.equals(this.delegateGovernanceTo, v1DependencyInstanceSpec.delegateGovernanceTo)
        && Objects.equals(this.viaComponentInstance, v1DependencyInstanceSpec.viaComponentInstance)
        && Objects.equals(
            this.viaComponentInstanceLandingZone,
            v1DependencyInstanceSpec.viaComponentInstanceLandingZone)
        && Objects.equals(this.viaService, v1DependencyInstanceSpec.viaService)
        && Objects.equals(this.componentInstance, v1DependencyInstanceSpec.componentInstance)
        && Objects.equals(
            this.componentInstanceLandingZone,
            v1DependencyInstanceSpec.componentInstanceLandingZone)
        && Objects.equals(this.isSourceAsComponent, v1DependencyInstanceSpec.isSourceAsComponent)
        && Objects.equals(
            this.isSourceLimitedToLegacyOnly, v1DependencyInstanceSpec.isSourceLimitedToLegacyOnly)
        && Objects.equals(
            this.isSourceLimitedToCloudOnly, v1DependencyInstanceSpec.isSourceLimitedToCloudOnly)
        && Objects.equals(
            this.isSourceLimitedToLandingzone,
            v1DependencyInstanceSpec.isSourceLimitedToLandingzone)
        && Objects.equals(
            this.isSourceLimitedToTarget, v1DependencyInstanceSpec.isSourceLimitedToTarget)
        && Objects.equals(
            this.isSourceLimitedToContinent, v1DependencyInstanceSpec.isSourceLimitedToContinent)
        && Objects.equals(
            this.isSourceLimitedToMacrophase, v1DependencyInstanceSpec.isSourceLimitedToMacrophase)
        && Objects.equals(
            this.isSourceLimitedToSelector, v1DependencyInstanceSpec.isSourceLimitedToSelector)
        && Objects.equals(this.dependency, v1DependencyInstanceSpec.dependency)
        && Objects.equals(
            this.targetComponentInstance, v1DependencyInstanceSpec.targetComponentInstance)
        && Objects.equals(
            this.targetComponentInstanceLandingZone,
            v1DependencyInstanceSpec.targetComponentInstanceLandingZone)
        && Objects.equals(this.isTargetAsComponent, v1DependencyInstanceSpec.isTargetAsComponent)
        && Objects.equals(
            this.isTargetLimitedToLegacyOnly, v1DependencyInstanceSpec.isTargetLimitedToLegacyOnly)
        && Objects.equals(
            this.isTargetLimitedToCloudOnly, v1DependencyInstanceSpec.isTargetLimitedToCloudOnly)
        && Objects.equals(this.targetService, v1DependencyInstanceSpec.targetService)
        && Objects.equals(this.targetServices, v1DependencyInstanceSpec.targetServices)
        && Objects.equals(
            this.isTargetLimitedToSourceNamespace,
            v1DependencyInstanceSpec.isTargetLimitedToSourceNamespace)
        && Objects.equals(
            this.isTargetLimitedToSourcePaas, v1DependencyInstanceSpec.isTargetLimitedToSourcePaas)
        && Objects.equals(
            this.isTargetLimitedToSourceSecurityzone,
            v1DependencyInstanceSpec.isTargetLimitedToSourceSecurityzone)
        && Objects.equals(
            this.isTargetLimitedToSourceApplicationphase,
            v1DependencyInstanceSpec.isTargetLimitedToSourceApplicationphase)
        && Objects.equals(
            this.isTargetLimitedToSourceLandingzone,
            v1DependencyInstanceSpec.isTargetLimitedToSourceLandingzone)
        && Objects.equals(
            this.isTargetLimitedToSourceTarget,
            v1DependencyInstanceSpec.isTargetLimitedToSourceTarget)
        && Objects.equals(
            this.isTargetLimitedToSourceContinent,
            v1DependencyInstanceSpec.isTargetLimitedToSourceContinent)
        && Objects.equals(
            this.isTargetLimitedToSourceMacrophase,
            v1DependencyInstanceSpec.isTargetLimitedToSourceMacrophase)
        && Objects.equals(this.sourcesSelectors, v1DependencyInstanceSpec.sourcesSelectors)
        && Objects.equals(this.targetsSelectors, v1DependencyInstanceSpec.targetsSelectors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        delegateGovernanceTo,
        viaComponentInstance,
        viaComponentInstanceLandingZone,
        viaService,
        componentInstance,
        componentInstanceLandingZone,
        isSourceAsComponent,
        isSourceLimitedToLegacyOnly,
        isSourceLimitedToCloudOnly,
        isSourceLimitedToLandingzone,
        isSourceLimitedToTarget,
        isSourceLimitedToContinent,
        isSourceLimitedToMacrophase,
        isSourceLimitedToSelector,
        dependency,
        targetComponentInstance,
        targetComponentInstanceLandingZone,
        isTargetAsComponent,
        isTargetLimitedToLegacyOnly,
        isTargetLimitedToCloudOnly,
        targetService,
        targetServices,
        isTargetLimitedToSourceNamespace,
        isTargetLimitedToSourcePaas,
        isTargetLimitedToSourceSecurityzone,
        isTargetLimitedToSourceApplicationphase,
        isTargetLimitedToSourceLandingzone,
        isTargetLimitedToSourceTarget,
        isTargetLimitedToSourceContinent,
        isTargetLimitedToSourceMacrophase,
        sourcesSelectors,
        targetsSelectors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1DependencyInstanceSpec {\n");
    sb.append("    delegateGovernanceTo: ")
        .append(toIndentedString(delegateGovernanceTo))
        .append("\n");
    sb.append("    viaComponentInstance: ")
        .append(toIndentedString(viaComponentInstance))
        .append("\n");
    sb.append("    viaComponentInstanceLandingZone: ")
        .append(toIndentedString(viaComponentInstanceLandingZone))
        .append("\n");
    sb.append("    viaService: ").append(toIndentedString(viaService)).append("\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
    sb.append("    componentInstanceLandingZone: ")
        .append(toIndentedString(componentInstanceLandingZone))
        .append("\n");
    sb.append("    isSourceAsComponent: ")
        .append(toIndentedString(isSourceAsComponent))
        .append("\n");
    sb.append("    isSourceLimitedToLegacyOnly: ")
        .append(toIndentedString(isSourceLimitedToLegacyOnly))
        .append("\n");
    sb.append("    isSourceLimitedToCloudOnly: ")
        .append(toIndentedString(isSourceLimitedToCloudOnly))
        .append("\n");
    sb.append("    isSourceLimitedToLandingzone: ")
        .append(toIndentedString(isSourceLimitedToLandingzone))
        .append("\n");
    sb.append("    isSourceLimitedToTarget: ")
        .append(toIndentedString(isSourceLimitedToTarget))
        .append("\n");
    sb.append("    isSourceLimitedToContinent: ")
        .append(toIndentedString(isSourceLimitedToContinent))
        .append("\n");
    sb.append("    isSourceLimitedToMacrophase: ")
        .append(toIndentedString(isSourceLimitedToMacrophase))
        .append("\n");
    sb.append("    isSourceLimitedToSelector: ")
        .append(toIndentedString(isSourceLimitedToSelector))
        .append("\n");
    sb.append("    dependency: ").append(toIndentedString(dependency)).append("\n");
    sb.append("    targetComponentInstance: ")
        .append(toIndentedString(targetComponentInstance))
        .append("\n");
    sb.append("    targetComponentInstanceLandingZone: ")
        .append(toIndentedString(targetComponentInstanceLandingZone))
        .append("\n");
    sb.append("    isTargetAsComponent: ")
        .append(toIndentedString(isTargetAsComponent))
        .append("\n");
    sb.append("    isTargetLimitedToLegacyOnly: ")
        .append(toIndentedString(isTargetLimitedToLegacyOnly))
        .append("\n");
    sb.append("    isTargetLimitedToCloudOnly: ")
        .append(toIndentedString(isTargetLimitedToCloudOnly))
        .append("\n");
    sb.append("    targetService: ").append(toIndentedString(targetService)).append("\n");
    sb.append("    targetServices: ").append(toIndentedString(targetServices)).append("\n");
    sb.append("    isTargetLimitedToSourceNamespace: ")
        .append(toIndentedString(isTargetLimitedToSourceNamespace))
        .append("\n");
    sb.append("    isTargetLimitedToSourcePaas: ")
        .append(toIndentedString(isTargetLimitedToSourcePaas))
        .append("\n");
    sb.append("    isTargetLimitedToSourceSecurityzone: ")
        .append(toIndentedString(isTargetLimitedToSourceSecurityzone))
        .append("\n");
    sb.append("    isTargetLimitedToSourceApplicationphase: ")
        .append(toIndentedString(isTargetLimitedToSourceApplicationphase))
        .append("\n");
    sb.append("    isTargetLimitedToSourceLandingzone: ")
        .append(toIndentedString(isTargetLimitedToSourceLandingzone))
        .append("\n");
    sb.append("    isTargetLimitedToSourceTarget: ")
        .append(toIndentedString(isTargetLimitedToSourceTarget))
        .append("\n");
    sb.append("    isTargetLimitedToSourceContinent: ")
        .append(toIndentedString(isTargetLimitedToSourceContinent))
        .append("\n");
    sb.append("    isTargetLimitedToSourceMacrophase: ")
        .append(toIndentedString(isTargetLimitedToSourceMacrophase))
        .append("\n");
    sb.append("    sourcesSelectors: ").append(toIndentedString(sourcesSelectors)).append("\n");
    sb.append("    targetsSelectors: ").append(toIndentedString(targetsSelectors)).append("\n");
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
