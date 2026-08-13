package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.yaml.models.V1Target;
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
  TargetMixin.JSON_PROPERTY_PROVIDER,
  TargetMixin.JSON_PROPERTY_MACRO_PHASE,
  TargetMixin.JSON_PROPERTY_MACRO_PHASE_ORDER,
  TargetMixin.JSON_PROPERTY_LANDING_ZONES,
  TargetMixin.JSON_PROPERTY_CONTINENT,
  TargetMixin.JSON_PROPERTY_CIDRS,
  TargetMixin.JSON_PROPERTY_TARGET_TYPE,
  TargetMixin.JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY,
  TargetMixin.JSON_PROPERTY_ENABLE_JUMP_SERVER_ACCESS,
  TargetMixin.JSON_PROPERTY_ENABLE_VNET_TO_VNET,
  TargetMixin.JSON_PROPERTY_IS_CLOUD,
  TargetMixin.JSON_PROPERTY_IS_AMADEUS,
  TargetMixin.JSON_PROPERTY_IS_AUD_MANAGED,
  TargetMixin.JSON_PROPERTY_POP_PRIMARY,
  TargetMixin.JSON_PROPERTY_POP_SECONDARY,
  TargetMixin.JSON_PROPERTY_FIREWALL_GROUPS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Target.class)
public interface TargetMixin extends AbstractBaseMixin<V1Target> {

  String JSON_PROPERTY_CIDRS = "cidrs";
  String JSON_PROPERTY_TARGET_TYPE = "target_type";
  String JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY = "enable_office_connectivity";
  String JSON_PROPERTY_ENABLE_JUMP_SERVER_ACCESS = "enable_erding_jump_server_access";
  String JSON_PROPERTY_ENABLE_VNET_TO_VNET = "enable_vnet_to_vnet";
  String JSON_PROPERTY_IS_CLOUD = "is_cloud";
  String JSON_PROPERTY_IS_AMADEUS = "is_amadeus";
  String JSON_PROPERTY_IS_AUD_MANAGED = "is_aud_modelized";
  String JSON_PROPERTY_PROVIDER = "provider";
  String JSON_PROPERTY_MACRO_PHASE = "macro_phase";
  String JSON_PROPERTY_MACRO_PHASE_ORDER = "macro_phase_order";
  String JSON_PROPERTY_LANDING_ZONES = "landing_zones";
  String JSON_PROPERTY_CONTINENT = "continent";
  String JSON_PROPERTY_POP_PRIMARY = "pop_primary";
  String JSON_PROPERTY_POP_SECONDARY = "pop_secondary";
  String JSON_PROPERTY_FIREWALL_GROUPS = "firewall_groups";

  @JsonIgnore
  @JsonProperty(JSON_PROPERTY_FIREWALL_GROUPS)
  Set<FirewallGroup> getFirewallGroups();

  @JsonIgnore
  @JsonProperty(JSON_PROPERTY_LANDING_ZONES)
  Set<LandingZone> getLandingZones();

  @JsonProperty(JSON_PROPERTY_CIDRS)
  List<String> getCidrs();

  @JsonProperty(JSON_PROPERTY_TARGET_TYPE)
  String getTargetTypeName();

  @JsonProperty(JSON_PROPERTY_ENABLE_OFFICE_CONNECTIVITY)
  boolean isEnableOfficeConnectivity();

  @JsonProperty(JSON_PROPERTY_ENABLE_JUMP_SERVER_ACCESS)
  boolean isEnableJumpServerAccess();

  @JsonProperty(JSON_PROPERTY_ENABLE_VNET_TO_VNET)
  boolean isEnableVnetToVnet();

  @JsonProperty(JSON_PROPERTY_IS_CLOUD)
  boolean isCloud();

  @JsonProperty(JSON_PROPERTY_IS_AMADEUS)
  boolean isAmadeus();

  @JsonProperty(JSON_PROPERTY_IS_AUD_MANAGED)
  boolean isAudManaged();

  @JsonProperty(JSON_PROPERTY_PROVIDER)
  String getProviderName();

  @JsonProperty(JSON_PROPERTY_MACRO_PHASE)
  String getMacroPhaseName();

  @JsonProperty(JSON_PROPERTY_MACRO_PHASE_ORDER)
  int getMacroPhaseOrder();

  @JsonProperty(JSON_PROPERTY_CONTINENT)
  String getContinentName();

  @JsonProperty(JSON_PROPERTY_POP_PRIMARY)
  String getPopPrimaryName();

  @JsonProperty(JSON_PROPERTY_POP_SECONDARY)
  String getPopSecondaryName();

  @JsonIgnore
  Provider getProvider();

  @JsonIgnore
  TargetType getTargetType();

  @JsonIgnore
  MacroPhase getMacroPhase();

  @JsonIgnore
  Continent getContinent();

  @JsonIgnore
  Target getPopPrimary();

  @JsonIgnore
  Target getPopSecondary();
}
