package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.base.BaseInstanceMixin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.RangeSet;
import io.quarkus.jackson.JacksonMixin;
import java.math.BigInteger;
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
  ZoneInstanceMixin.JSON_PROPERTY_IS_NSG_NEEDED,
  ZoneInstanceMixin.JSON_PROPERTY_ZONE_TYPE,
  ZoneInstanceMixin.JSON_PROPERTY_FIREWALL_GROUP,
  ZoneInstanceMixin.JSON_PROPERTY_SUBNET_INSTANCES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ZoneInstance.class)
public interface ZoneInstanceMixin extends BaseInstanceMixin<V1ZoneInstance> {

  String JSON_PROPERTY_IS_NSG_NEEDED = "is_nsg_needed";
  String JSON_PROPERTY_ZONE_TYPE = "zone_type";
  String JSON_PROPERTY_FIREWALL_GROUP = "firewall_group";
  String JSON_PROPERTY_SUBNET_INSTANCES = "subnet_instances";

  @JsonProperty(JSON_PROPERTY_SUBNET_INSTANCES)
  @JsonIgnore
  List<Map<String, String>> getSimpleSubnetInstances();

  @JsonIgnore
  Set<SubnetInstance> getSubnetInstances();

  @JsonProperty(JSON_PROPERTY_IS_NSG_NEEDED)
  boolean isNsgNeeded();

  @JsonProperty(JSON_PROPERTY_ZONE_TYPE)
  String getZoneTypeName();

  @JsonProperty(JSON_PROPERTY_FIREWALL_GROUP)
  String getFirewallGroupName();

  @JsonIgnore
  ZoneType getZoneType();

  @JsonIgnore
  FirewallGroup getFirewallGroup();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  Set<Endpoint> getInternalEndpoints();

  @JsonIgnore
  RangeSet<BigInteger> getInternalDstCidrRangeSetForNSG();
}
