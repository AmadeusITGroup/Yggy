package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNAT;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNATSpec;
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
  OutboundSNATMixin.JSON_PROPERTY_MACRO_PHASE,
  OutboundSNATMixin.JSON_PROPERTY_CONTINENT,
  OutboundSNATMixin.JSON_PROPERTY_ZONE_TYPE,
  OutboundSNATMixin.JSON_PROPERTY_COMPONENTS,
  OutboundSNATMixin.JSON_PROPERTY_COMPONENT_INSTANCES,
  OutboundSNATMixin.JSON_PROPERTY_PROTOCOL,
  OutboundSNATMixin.JSON_PROPERTY_TRANSLATED_IP,
  OutboundSNATMixin.JSON_PROPERTY_CIDRS,
  OutboundSNATMixin.JSON_PROPERTY_DESTINATION_CIDRS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(OutboundSNAT.class)
public interface OutboundSNATMixin extends AbstractBaseMixin<V1OutboundSNAT> {
  String JSON_PROPERTY_MACRO_PHASE = "macro_phase";
  String JSON_PROPERTY_CONTINENT = "continent";
  String JSON_PROPERTY_ZONE_TYPE = "zone_type";
  String JSON_PROPERTY_COMPONENTS = "components";
  String JSON_PROPERTY_COMPONENT_INSTANCES = "component_instances";
  String JSON_PROPERTY_PROTOCOL = "protocol";
  String JSON_PROPERTY_TRANSLATED_IP = "translated_ip";
  String JSON_PROPERTY_CIDRS = "cidrs";
  String JSON_PROPERTY_DESTINATION_CIDRS = "destination_cidrs";

  @JsonProperty(JSON_PROPERTY_PROTOCOL)
  V1OutboundSNATSpec.ProtocolEnum getProtocol();

  @JsonProperty(JSON_PROPERTY_TRANSLATED_IP)
  String getTranslatedIp();

  @JsonProperty(JSON_PROPERTY_CIDRS)
  List<String> getCidrs();

  @JsonProperty(JSON_PROPERTY_DESTINATION_CIDRS)
  List<String> getDestinationCidrs();

  @JsonProperty(JSON_PROPERTY_MACRO_PHASE)
  String getMacroPhaseName();

  @JsonProperty(JSON_PROPERTY_CONTINENT)
  String getContinentName();

  @JsonProperty(JSON_PROPERTY_ZONE_TYPE)
  String getZoneTypeName();

  @JsonProperty(JSON_PROPERTY_COMPONENTS)
  List<String> getComponentNames();

  @JsonProperty(JSON_PROPERTY_COMPONENT_INSTANCES)
  List<String> getComponentInstanceNames();

  @JsonIgnore
  MacroPhase getMacroPhase();

  @JsonIgnore
  Continent getContinent();

  @JsonIgnore
  ZoneType getZoneType();

  @JsonIgnore
  Set<Component> getComponents();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();
}
