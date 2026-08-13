package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
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
  FirewallGroupMixin.JSON_PROPERTY_FIREWALL_VENDOR,
  FirewallGroupMixin.JSON_PROPERTY_TARGET,
  FirewallGroupMixin.JSON_PROPERTY_ZONE_INSTANCES,
  FirewallGroupMixin.JSON_PROPERTY_EXTERNAL_IPS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(FirewallGroup.class)
public interface FirewallGroupMixin extends AbstractBaseMixin<V1FirewallGroup> {

  String JSON_PROPERTY_FIREWALL_VENDOR = "firewall_vendor";
  String JSON_PROPERTY_TARGET = "target";
  String JSON_PROPERTY_ZONE_INSTANCES = "zone_instances";
  String JSON_PROPERTY_EXTERNAL_IPS = "external_ips";

  @JsonProperty(JSON_PROPERTY_ZONE_INSTANCES)
  List<Map<String, String>> getSimpleZoneInstances();

  @JsonIgnore
  Set<ZoneInstance> getZoneInstances();

  @JsonProperty(JSON_PROPERTY_EXTERNAL_IPS)
  String getExternalIps();

  @JsonProperty(JSON_PROPERTY_FIREWALL_VENDOR)
  String getFirewallVendorName();

  @JsonProperty(JSON_PROPERTY_TARGET)
  String getTargetName();

  @JsonIgnore
  FirewallVendor getFirewallVendor();

  @JsonIgnore
  Target getTarget();
}
