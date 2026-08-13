package com.amadeus.aud.libraries.backend;

import com.amadeus.aud.libraries.yaml.models.Constants;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public enum ObjectTypeListNamesEnum {
  APPLICATIONIDS(
      "applicationid",
      "applicationids",
      "Application IDs",
      Constants.FIREWALL,
      List.of("name", Constants.SCOPE, Constants.COMMENT),
      "ApplicationId"),
  APPLICATIONPHASES(
      "applicationphase",
      "applicationphases",
      "Application Phases",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, Constants.MACROPHASE, Constants.COMMENT),
      "ApplicationPhase"),
  AUTOMATICTEMPLATEINSTANCES(
      "automatictemplateinstance",
      "automatictemplateinstances",
      "Automatic Template Instances",
      Constants.APPLICATION,
      List.of(
          "name", Constants.SCOPE, Constants.LANDINGZONE, Constants.COMPONENT, Constants.COMMENT),
      "AutomaticTemplateInstance"),
  BLACKLISTEDCIDRS(
      "blacklistedcidr",
      "blacklistedcidrs",
      "Black Listed CIDRs",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "cidr", "transportLayer", "ports"),
      "BlackListedCIDR"),
  BLACKLISTEDURLS(
      "blacklistedurl",
      "blacklistedurls",
      "Black Listed URLs",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "fqdn", "transportLayer", "ports"),
      "BlackListedURL"),
  BLOCKEDPORTS(
      "blockedport",
      "blockedports",
      "Blocked Ports",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "port", Constants.PROTOCOL),
      "BlockedPort"),
  CERTIFICATIONS(
      "certification",
      "certifications",
      "Scope Certifications",
      AmadeusConstants.GOVERNANCE,
      List.of(
          "name",
          Constants.SCOPE,
          "servicenowId",
          "recertificationDate",
          "recertificationFutureDate"),
      null),
  COMPONENTS(
      Constants.COMPONENT,
      "components",
      "Components",
      Constants.APPLICATION,
      List.of("name", Constants.SCOPE, "is_container", "is_technical", Constants.COMMENT),
      "Component"),
  COMPONENTINSTANCES(
      "componentinstance",
      "componentinstances",
      "Component Instances",
      Constants.APPLICATION,
      List.of(
          "name",
          Constants.SCOPE,
          "landingzone",
          Constants.COMPONENT,
          "application_phase",
          "auto_generated",
          Constants.COMMENT),
      "ComponentInstance"),
  COMPONENTPLACEMENTS(
      "componentplacement",
      "componentplacements",
      "Component Placements",
      AmadeusConstants.GOVERNANCE,
      List.of(
          "name",
          Constants.SCOPE,
          Constants.COMPONENT,
          "security_zone",
          AmadeusConstants.SERVICENOWID),
      "ComponentPlacement"),
  CONNECTIONS(
      "connection",
      "connections",
      "Connections",
      AmadeusConstants.SECURITY_OFFICE,
      List.of(
          "name",
          Constants.SCOPE,
          "connection_type",
          "source_zone_type",
          "destination_zone_type",
          "security_control"),
      "Connection"),
  CONNECTIONAPPROVALS(
      "connectionapproval",
      "connectionapprovals",
      "Connection Approvals",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, AmadeusConstants.SERVICENOWID),
      "ConnectionApproval"),
  CONNECTIONAPPROVALCOMPONENTSKIPS(
      "connectionapprovalcomponentskip",
      "connectionapprovalcomponentskips",
      "Connection Approval Component Skips",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "skip_source", "skip_destination"),
      "ConnectionApprovalComponentSkip"),
  CONNECTIONAPPROVALSCOPESKIPS(
      "connectionapprovalscopeskip",
      "connectionapprovalscopeskips",
      "Connection Approval Scope Skips",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "skip_source", "skip_destination", Constants.COMMENT),
      "ConnectionApprovalScopeSkip"),
  CONNECTIONPHASES(
      "connectionphase",
      "connectionphases",
      "Connection Phases",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "source_phase", "destination_phase", "control"),
      "ConnectionPhase"),
  CONTINENTS(
      Constants.CONTINENT,
      "continents",
      "Continents",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE),
      "Continent"),
  DEPENDENCIES(
      "dependency",
      "dependencies",
      "Dependencies",
      Constants.APPLICATION,
      List.of("name", Constants.SCOPE, "source_component", "target_component", Constants.COMMENT),
      "Dependency"),
  DEPENDENCYINSTANCES(
      "dependencyinstance",
      "dependencyinstances",
      "Dependency Instances",
      Constants.APPLICATION,
      List.of(
          "name",
          Constants.SCOPE,
          "dependency",
          Constants.LANDINGZONE,
          "is_source_as_component",
          "is_target_as_component",
          "source_component_instance",
          "target_component_instance",
          "target_services",
          Constants.COMMENT),
      "DependencyInstance"),
  FIREWALLGROUPS(
      "firewallgroup",
      "firewallgroups",
      "Firewall Groups",
      Constants.FIREWALL,
      List.of(
          "name",
          Constants.SCOPE,
          "firewall_vendor",
          Constants.TARGET,
          "external_ips",
          Constants.COMMENT),
      "FirewallGroup"),
  FIREWALLVENDORS(
      "firewallvendor",
      "firewallvendors",
      "Firewall Vendors",
      Constants.FIREWALL,
      List.of("name", Constants.SCOPE),
      "FirewallVendor"),
  IPADDRESSINSTANCES(
      "ipaddressinstance",
      "ipaddressinstances",
      "IP Address Instances",
      Constants.APPLICATION,
      List.of(
          "name",
          Constants.SCOPE,
          "component_instance",
          Constants.LANDINGZONE,
          "host",
          Constants.COMMENT),
      "IPAddressInstance"),
  LANDINGZONES(
      "landingzone",
      "landingzones",
      "Landing Zones",
      Constants.NETWORK,
      List.of(
          "name",
          Constants.SCOPE,
          Constants.TARGET,
          "region",
          Constants.MACROPHASE,
          "landing_zone_type",
          "functional_types",
          Constants.COMMENT),
      "LandingZone"),
  LANDINGZONEFUNCTIONALTYPES(
      "landingzonefunctionaltype",
      "landingzonefunctionaltypes",
      "Landing Zone Functional Types",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, "is_generated", Constants.COMMENT),
      "LandingZoneFunctionalType"),
  LANDINGZONETYPES(
      "landingzonetype",
      "landingzonetypes",
      "Landing Zone Types",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE),
      "LandingZoneType"),
  MACROPHASES(
      "macrophase",
      "macrophases",
      "Macro Phases",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, Constants.COMMENT),
      "MacroPhase"),
  NAMESPACES(
      "namespace",
      "namespaces",
      "Namespaces",
      Constants.APPLICATION,
      List.of("name", Constants.SCOPE, "is_shared", Constants.COMMENT),
      "Namespace"),
  OUTBOUNDSNATS(
      "outboundsnat",
      "outboundsnats",
      "Outbound SNATs",
      AmadeusConstants.LINKS,
      List.of(
          "name",
          Constants.SCOPE,
          "translated_ip",
          Constants.MACROPHASE,
          Constants.CONTINENT,
          "zone_type",
          Constants.PROTOCOL,
          Constants.COMMENT),
      null),
  PROVIDERS(
      Constants.PROVIDER,
      "providers",
      "Providers",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, Constants.COMMENT),
      null),
  REGIONS(
      "region",
      "regions",
      "Regions",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, Constants.PROVIDER, Constants.CONTINENT, Constants.COMMENT),
      null),
  ROLES(
      "role",
      "roles",
      "Roles",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE),
      null),
  SCOPES(
      Constants.SCOPE,
      "scopes",
      "Scopes",
      AmadeusConstants.GOVERNANCE,
      List.of(
          "name",
          Constants.SCOPE,
          "assignee_group",
          "admin_group",
          "recertification_future_date",
          Constants.COMMENT),
      "Scope"),
  SCOPERELEASENOTES(
      "scopereleasenote",
      "scopereleasenotes",
      "Scope Release Notes",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, "scopeVersion", "versionDateTime"),
      "ScopeReleaseNote"),
  SECURITYAREAS(
      "securityarea",
      "securityareas",
      "Security Areas",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, AmadeusConstants.SERVICENOWID, Constants.COMMENT),
      null),
  SECURITYAREAMAPPINGS(
      "securityareamapping",
      "securityareamappings",
      "Security Area Mappings",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, "security_area", "servicenowId"),
      null),
  SERVICES(
      "service",
      "services",
      "Services",
      Constants.APPLICATION,
      List.of("name", Constants.SCOPE, Constants.COMPONENT, Constants.PROTOCOL),
      "Service"),
  SERVICEAPPROVALS(
      "serviceapproval",
      "serviceapprovals",
      "Service Approvals",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "serviceNowId"),
      null),
  SERVICETAGS(
      "servicetag",
      "servicetags",
      "Service Tags",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE, "any_needed_in_nsg"),
      null),
  SUBNETINSTANCES(
      "subnetinstance",
      "subnetinstances",
      "Subnet Instances",
      Constants.NETWORK,
      List.of(
          "name",
          Constants.SCOPE,
          Constants.LANDINGZONE,
          "zone_instance",
          "subnet_type",
          Constants.COMMENT),
      "SubnetInstance"),
  SUBNETTYPES(
      "subnettype",
      "subnettypes",
      "Subnet Types",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE),
      null),
  TARGETS(
      Constants.TARGET,
      "targets",
      "Targets",
      Constants.NETWORK,
      List.of("name", Constants.SCOPE, Constants.PROVIDER, Constants.MACROPHASE),
      "Target"),
  TARGETTYPES(
      "targettype",
      "targettypes",
      "Target Types",
      AmadeusConstants.GOVERNANCE,
      List.of("name", Constants.SCOPE),
      "Target"),
  ZONEINSTANCES(
      "zoneinstance",
      "zoneinstances",
      "Zone Instances",
      Constants.NETWORK,
      List.of("name", Constants.SCOPE, "zone_type", Constants.LANDINGZONE),
      "ZoneInstance"),
  ZONETYPES(
      "zonetype",
      "zonetypes",
      "Zone Types",
      AmadeusConstants.SECURITY_OFFICE,
      List.of("name", Constants.SCOPE, "is_technical", "is_paas", "is_real"),
      "ZoneType");

  private final String repositoryKey;
  private final String uri;
  private final String title;
  private final String category;
  private final List<String> initialColumns;
  private final String graphqlQueryName;

  ObjectTypeListNamesEnum(
      String repositoryKey,
      String uri,
      String title,
      String category,
      List<String> initialColumns,
      @Nullable String graphqlQueryName) {
    this.repositoryKey = repositoryKey;
    this.uri = uri;
    this.title = title;
    this.category = category;
    this.initialColumns = initialColumns;
    this.graphqlQueryName = graphqlQueryName;
  }

  public String getRepositoryKey() {
    return this.repositoryKey;
  }

  @JsonValue
  public String getUri() {
    return this.uri;
  }

  public String getTitle() {
    return this.title;
  }

  public String getCategory() {
    return this.category;
  }

  public @Nullable String graphQLQueryName() {
    return this.graphqlQueryName;
  }

  public List<String> getInitialColumns() {
    return this.initialColumns;
  }

  public static Optional<ObjectTypeListNamesEnum> byUri(String uri) {
    for (ObjectTypeListNamesEnum e : ObjectTypeListNamesEnum.values()) {
      if (e.getUri().equals(uri)) {
        return Optional.of(e);
      }
    }
    return Optional.empty();
  }
}
