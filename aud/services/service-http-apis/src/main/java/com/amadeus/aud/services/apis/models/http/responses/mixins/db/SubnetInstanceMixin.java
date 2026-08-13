package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.services.apis.models.http.responses.mixins.db.base.BaseInstanceMixin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Range;
import io.quarkus.jackson.JacksonMixin;
import java.math.BigInteger;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  SubnetInstanceMixin.JSON_PROPERTY_PARENT,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  SubnetInstanceMixin.JSON_PROPERTY_VLAN,
  SubnetInstanceMixin.JSON_PROPERTY_IS_SHARED,
  SubnetInstanceMixin.JSON_PROPERTY_IS_GROUP,
  SubnetInstanceMixin.JSON_PROPERTY_IS_NETWORK_PRIVATE,
  SubnetInstanceMixin.JSON_PROPERTY_CIDR,
  SubnetInstanceMixin.JSON_PROPERTY_SUBNET_TYPE,
  SubnetInstanceMixin.JSON_PROPERTY_ZONE_INSTANCE,
  SubnetInstanceMixin.JSON_PROPERTY_DEPRECATED_STATUS,
  SubnetInstanceMixin.JSON_PROPERTY_DEPRECATED_MESSAGE,
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(SubnetInstance.class)
public interface SubnetInstanceMixin extends BaseInstanceMixin<V1SubnetInstance> {

  String JSON_PROPERTY_VLAN = "vlan";
  String JSON_PROPERTY_IS_SHARED = "is_shared";
  String JSON_PROPERTY_IS_GROUP = "is_group";
  String JSON_PROPERTY_IS_NETWORK_PRIVATE = "is_network_private";
  String JSON_PROPERTY_CIDR = "cidr";
  String JSON_PROPERTY_SUBNET_TYPE = "subnet_type";
  String JSON_PROPERTY_ZONE_INSTANCE = "zone_instance";
  String JSON_PROPERTY_PARENT = "parent";
  String JSON_PROPERTY_DEPRECATED_STATUS = "deprecated_status";
  String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecated_message";

  @JsonProperty(JSON_PROPERTY_VLAN)
  String getVlan();

  @JsonProperty(JSON_PROPERTY_IS_SHARED)
  Boolean isShared();

  @JsonProperty(JSON_PROPERTY_IS_GROUP)
  boolean isGroup();

  @JsonProperty(JSON_PROPERTY_IS_NETWORK_PRIVATE)
  Boolean isNetworkPrivate();

  @JsonProperty(JSON_PROPERTY_CIDR)
  String getCidr();

  @JsonProperty(JSON_PROPERTY_SUBNET_TYPE)
  String getSubnetTypeName();

  @JsonProperty(JSON_PROPERTY_ZONE_INSTANCE)
  String getZoneInstanceName();

  @JsonProperty(JSON_PROPERTY_PARENT)
  String getParentName();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_STATUS)
  DeprecatedStatus getDeprecatedStatus();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_MESSAGE)
  String getDeprecatedMessage();

  @JsonIgnore
  SubnetType getSubnetType();

  @JsonIgnore
  ZoneInstance getZoneInstance();

  @JsonIgnore
  Set<SubnetInstance> getChildren();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  SubnetInstance getParent();

  @JsonIgnore
  Set<Endpoint> getInternalEndpoints();

  @JsonIgnore
  Range<BigInteger> getCidrRange();
}
