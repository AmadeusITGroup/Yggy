package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
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
  LandingZoneMixin.JSON_PROPERTY_TARGET,
  LandingZoneMixin.JSON_PROPERTY_REGION,
  LandingZoneMixin.JSON_PROPERTY_LANDING_ZONE_TYPE,
  LandingZoneMixin.JSON_PROPERTY_MACRO_PHASE,
  LandingZoneMixin.JSON_PROPERTY_FUNCTIONAL_TYPES,
  LandingZoneMixin.JSON_PROPERTY_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(LandingZone.class)
public interface LandingZoneMixin extends AbstractBaseMixin<V1LandingZone> {

  String JSON_PROPERTY_TARGET = "target";
  String JSON_PROPERTY_REGION = "region";
  String JSON_PROPERTY_LANDING_ZONE_TYPE = "landing_zone_type";
  String JSON_PROPERTY_MACRO_PHASE = "macro_phase";
  String JSON_PROPERTY_FUNCTIONAL_TYPES = "functional_types";
  String JSON_PROPERTY_CIDRS = "cidrs";

  @JsonProperty(JSON_PROPERTY_CIDRS)
  List<String> getCidrs();

  @JsonProperty(JSON_PROPERTY_TARGET)
  String getTargetName();

  @JsonProperty(JSON_PROPERTY_REGION)
  String getRegionName();

  @JsonProperty(JSON_PROPERTY_LANDING_ZONE_TYPE)
  String getLandingZoneTypeName();

  @JsonProperty(JSON_PROPERTY_MACRO_PHASE)
  String getMacroPhaseName();

  @JsonProperty(JSON_PROPERTY_FUNCTIONAL_TYPES)
  Set<String> getFunctionalTypeJsonRepresentation();

  @JsonIgnore
  Target getTarget();

  @JsonIgnore
  Region getRegion();

  @JsonIgnore
  LandingZoneType getLandingZoneType();

  @JsonIgnore
  MacroPhase getMacroPhase();

  @JsonIgnore
  Set<LandingZoneFunctionalType> getLandingZoneFunctionalTypes();

  @JsonIgnore
  Set<ZoneInstance> getZoneInstances();

  @JsonIgnore
  Set<SubnetInstance> getSubnetInstances();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  Set<AutomaticTemplateInstance> getAutomaticTemplateInstances();

  @JsonIgnore
  Set<IPAddressInstance> getIpAddressInstances();

  @JsonIgnore
  Set<DependencyInstance> getDependencyInstances();

  @JsonIgnore
  Map<Boolean, Set<Endpoint>> getInternalEndpoints();
}
