package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.yaml.models.V1FirewallVendor;
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
  FirewallVendorMixin.JSON_PROPERTY_FIREWALL_GROUPS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(FirewallVendor.class)
public interface FirewallVendorMixin extends AbstractBaseMixin<V1FirewallVendor> {

  String JSON_PROPERTY_FIREWALL_GROUPS = "firewall_groups";

  @JsonProperty(JSON_PROPERTY_FIREWALL_GROUPS)
  List<String> getFirewallGroupNames();

  @JsonIgnore
  Set<FirewallGroup> getFirewallGroups();
}
