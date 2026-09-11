package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  BlacklistMixin.JSON_PROPERTY_SOURCE_COMPONENT,
  BlacklistMixin.JSON_PROPERTY_DESTINATION_HOST_FQDNS,
  BlacklistMixin.JSON_PROPERTY_DESTINATION_HOST_CIDRS,
  BlacklistMixin.JSON_PROPERTY_SOURCE_ZONES,
  BlacklistMixin.JSON_PROPERTY_SOURCE_MACROPHASES,
  BlacklistMixin.JSON_PROPERTY_SOURCE_TARGETS,
  BlacklistMixin.JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS,
  BlacklistMixin.JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS,
  BlacklistMixin.JSON_PROPERTY_DESTINATION_PORT_RANGES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Blacklist.class)
public interface BlacklistMixin extends AbstractBaseMixin<V1Blacklist> {
  String JSON_PROPERTY_SOURCE_COMPONENT = "source_component";
  String JSON_PROPERTY_DESTINATION_HOST_FQDNS = "destination_host_fqdns";
  String JSON_PROPERTY_DESTINATION_HOST_CIDRS = "destination_host_cidrs";
  String JSON_PROPERTY_SOURCE_ZONES = "source_zones";
  String JSON_PROPERTY_SOURCE_MACROPHASES = "source_macrophases";
  String JSON_PROPERTY_SOURCE_TARGETS = "source_targets";
  String JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS = "destination_application_protocols";
  String JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS = "destination_transport_protocols";
  String JSON_PROPERTY_DESTINATION_PORT_RANGES = "destination_port_ranges";

  @JsonProperty(JSON_PROPERTY_SOURCE_COMPONENT)
  String getSourceComponent();

  @JsonProperty(JSON_PROPERTY_DESTINATION_HOST_FQDNS)
  List<String> getDestinationHostFqdns();

  @JsonProperty(JSON_PROPERTY_DESTINATION_HOST_CIDRS)
  List<String> getDestinationHostCidrs();

  @JsonProperty(JSON_PROPERTY_SOURCE_ZONES)
  List<String> getSourceZones();

  @JsonProperty(JSON_PROPERTY_SOURCE_MACROPHASES)
  List<String> getSourceMacrophases();

  @JsonProperty(JSON_PROPERTY_SOURCE_TARGETS)
  List<String> getSourceTargets();

  @JsonProperty(JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS)
  List<String> getDestinationApplicationProtocols();

  @JsonProperty(JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS)
  List<String> getDestinationTransportProtocols();

  @JsonProperty(JSON_PROPERTY_DESTINATION_PORT_RANGES)
  List<String> getDestinationPortRanges();
}
