package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ScopeMixin.JSON_PROPERTY_ROLES,
  ScopeMixin.JSON_PROPERTY_REPOSITORY_VERSION,
  ScopeMixin.JSON_PROPERTY_IS_DELETED,
  ScopeMixin.JSON_PROPERTY_SERVICE_NOW_ID
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Scope.class)
public interface ScopeMixin extends AbstractBaseMixin<V1Scope> {

  String JSON_PROPERTY_ROLES = "roles";
  String JSON_PROPERTY_REPOSITORY_VERSION = "repository_version";
  String JSON_PROPERTY_IS_DELETED = "is_deleted";
  String JSON_PROPERTY_SERVICE_NOW_ID = "service_now_id";

  @JsonProperty(JSON_PROPERTY_SERVICE_NOW_ID)
  String getServiceNowId();

  @JsonProperty(JSON_PROPERTY_ROLES)
  Set<String> getRolesJsonRepresentation();

  @JsonProperty(JSON_PROPERTY_REPOSITORY_VERSION)
  String getRepositoryVersion();

  @JsonProperty(JSON_PROPERTY_IS_DELETED)
  boolean isDeleted();

  @JsonIgnore
  Requirement getRequirement();

  @JsonIgnore
  Set<Role> getRoles();

  @JsonIgnore
  Set<Role> getDefinedRoles();

  @JsonIgnore
  Set<Blacklist> getBlacklists();

  @JsonIgnore
  Set<BlackListedURL> getBlackListedURLs();

  @JsonIgnore
  Set<BlackListedCIDR> getBlackListedCIDRs();

  @JsonIgnore
  Set<BlockedPort> getBlockedPorts();

  @JsonIgnore
  Set<MacroPhase> getMacroPhases();

  @JsonIgnore
  Set<ApplicationPhase> getApplicationPhases();

  @JsonIgnore
  Set<SubnetType> getSubnetTypes();

  @JsonIgnore
  Set<Provider> getProviders();

  @JsonIgnore
  Set<Namespace> getNamespaces();

  @JsonIgnore
  Set<ServiceTag> getServiceTags();

  @JsonIgnore
  Set<Continent> getContinents();

  @JsonIgnore
  Set<Region> getRegions();

  @JsonIgnore
  Set<TargetType> getTargetTypes();

  @JsonIgnore
  Set<Target> getTargets();

  @JsonIgnore
  Set<FirewallVendor> getFirewallVendors();

  @JsonIgnore
  Set<FirewallGroup> getFirewallGroups();

  @JsonIgnore
  Set<LandingZoneType> getLandingZoneTypes();

  @JsonIgnore
  Set<LandingZoneFunctionalType> getLandingZoneFunctionalTypes();

  @JsonIgnore
  Set<ZoneType> getZoneTypes();

  @JsonIgnore
  Set<ConnectionPhase> getConnectionPhases();

  @JsonIgnore
  Set<Connection> getConnections();

  @JsonIgnore
  Set<ApplicationId> getApplicationIds();

  @JsonIgnore
  Set<LandingZone> getLandingZones();

  @JsonIgnore
  Set<ZoneInstance> getZoneInstances();

  @JsonIgnore
  Set<SubnetInstance> getSubnetInstances();

  @JsonIgnore
  Set<Component> getComponents();

  @JsonIgnore
  Set<ConnectionApprovalComponentSkip> getConnectionApprovalComponentSkips();

  @JsonIgnore
  Set<ConnectionApprovalScopeSkip> getConnectionApprovalScopeSkips();

  @JsonIgnore
  Set<Service> getServices();

  @JsonIgnore
  Set<Dependency> getDependencies();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();

  @JsonIgnore
  Set<AutomaticTemplateInstance> getAutomaticTemplateInstances();

  @JsonIgnore
  Set<IPAddressInstance> getIpAddressInstances();

  @JsonIgnore
  Set<DependencyInstance> getDependencyInstances();

  @JsonIgnore
  Set<OutboundSNAT> getOutboundSNATs();

  @JsonIgnore
  Set<ScopeReleaseNote> getScopeReleaseNotes();
}
