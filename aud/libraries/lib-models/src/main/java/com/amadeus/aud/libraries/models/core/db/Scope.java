package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.V1ScopeSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Scope extends AbstractBase<V1Scope> {

  private String defaultScopesBitbucketProject = "AUD";

  @OneToOne private Requirement requirement;

  /**
   * Roles that are used by this scope This points to the {@code Role} object unlike the {@code
   * scope.getSpec().getRoles()} is a {@code List<String>}
   */
  @ManyToMany(targetEntity = Role.class, cascade = CascadeType.PERSIST)
  private Set<Role> roles;

  /** Roles that are defined in this scope */
  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<Role> definedRoles;

  private String serviceNowId;

  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<BlockedPort> blockedPorts;

  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<Blacklist> blacklists;

  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<BlackListedURL> blackListedURLs;

  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<BlackListedCIDR> blackListedCIDRs;

  @OneToMany(mappedBy = "scope", cascade = CascadeType.PERSIST)
  private Set<MacroPhase> macroPhases;

  @OneToMany(mappedBy = "scope")
  private Set<ApplicationPhase> applicationPhases;

  @OneToMany(mappedBy = "scope")
  private Set<SubnetType> subnetTypes;

  @OneToMany(mappedBy = "scope")
  private Set<Provider> providers;

  @OneToMany(mappedBy = "scope")
  private Set<Namespace> namespaces;

  @OneToMany(mappedBy = "scope")
  private Set<ServiceTag> serviceTags;

  @OneToMany(mappedBy = "scope")
  private Set<Continent> continents;

  @OneToMany(mappedBy = "scope")
  private Set<Region> regions;

  @OneToMany(mappedBy = "scope")
  private Set<TargetType> targetTypes;

  @OneToMany(mappedBy = "scope")
  private Set<Target> targets;

  @OneToMany(mappedBy = "scope")
  private Set<FirewallVendor> firewallVendors;

  @OneToMany(mappedBy = "scope")
  private Set<FirewallGroup> firewallGroups;

  @OneToMany(mappedBy = "scope")
  private Set<LandingZoneType> landingZoneTypes;

  @OneToMany(mappedBy = "scope")
  private Set<LandingZoneFunctionalType> landingZoneFunctionalTypes;

  @OneToMany(mappedBy = "scope")
  private Set<ZoneType> zoneTypes;

  @OneToMany(mappedBy = "scope")
  private Set<ConnectionPhase> connectionPhases;

  @OneToMany(mappedBy = "scope")
  private Set<Connection> connections;

  @OneToMany(mappedBy = "scope")
  private Set<ApplicationId> applicationIds;

  @OneToMany(mappedBy = "scope")
  private Set<LandingZone> landingZones;

  @OneToMany(mappedBy = "scope")
  private Set<ZoneInstance> zoneInstances;

  @OneToMany(mappedBy = "scope")
  private Set<SubnetInstance> subnetInstances;

  @OneToMany(mappedBy = "scope")
  private Set<Component> components;

  @OneToMany(mappedBy = "scope")
  private Set<Service> services;

  @OneToMany(mappedBy = "scope")
  private Set<Dependency> dependencies;

  @OneToMany(mappedBy = "scope")
  private Set<ComponentInstance> componentInstances;

  @OneToMany(mappedBy = "scope")
  private Set<AutomaticTemplateInstance> automaticTemplateInstances;

  @OneToMany(mappedBy = "scope")
  private Set<IPAddressInstance> ipAddressInstances;

  @OneToMany(mappedBy = "scope")
  private Set<DependencyInstance> dependencyInstances;

  @OneToMany(mappedBy = "scope")
  private Set<OutboundSNAT> outboundSNATs;

  @JsonIgnore
  @OneToMany(mappedBy = "scope")
  private Set<ScopeReleaseNote> scopeReleaseNotes;

  @JsonIgnore
  @OneToMany(mappedBy = "scope")
  private Set<ScopeVersionMapping> scopeVersionMapping;

  @OneToMany(mappedBy = "scope")
  private Set<ConnectionApprovalComponentSkip> connectionApprovalComponentSkips;

  @OneToMany(mappedBy = "scope")
  private Set<ConnectionApprovalScopeSkip> connectionApprovalScopeSkips;

  public Scope() {
    super();
  }

  public @Nullable Set<String> getRolesJsonRepresentation() {
    if (roles == null) {
      return null;
    }
    Set<String> result = new HashSet<>();
    for (Role role : roles) {
      result.add(role.getName());
    }
    return result;
  }

  public @Nullable String getRepositoryVersion() {
    if (requirement == null) {
      return null;
    }
    return requirement.getVersion();
  }

  public boolean isDeleted() {
    if (getData() == null) {
      return true;
    }
    V1ScopeSpec spec = getData().getSpec();
    if (spec == null) {
      return true;
    }
    return Boolean.TRUE.equals(spec.getIsDeleted());
  }

  public Requirement getRequirement() {
    return requirement;
  }

  public void setRequirement(Requirement requirement) {
    this.requirement = requirement;
  }

  public Set<Role> getDefinedRoles() {
    return definedRoles;
  }

  public void setDefinedRoles(Set<Role> definedRoles) {
    this.definedRoles = definedRoles;
  }

  public void addDefinedRole(Role role) {
    if (this.definedRoles == null) {
      this.definedRoles = new HashSet<>();
    }
    this.definedRoles.add(role);
  }

  public void addDefinedRoles(Set<Role> roles) {
    if (this.definedRoles == null) {
      this.definedRoles = new HashSet<>();
    }
    this.definedRoles.addAll(roles);
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public void addRole(Role role) {
    if (this.roles == null) {
      this.roles = new HashSet<>();
    }
    this.roles.add(role);
  }

  public void addRoles(Set<Role> roles) {
    if (this.roles == null) {
      this.roles = new HashSet<>();
    }
    this.roles.addAll(roles);
  }

  public String getServiceNowId() {
    return serviceNowId;
  }

  public void setServiceNowId(String serviceNowId) {
    this.serviceNowId = serviceNowId;
  }

  public Set<BlockedPort> getBlockedPorts() {
    return blockedPorts;
  }

  public void setBlockedPorts(Set<BlockedPort> blockedPorts) {
    this.blockedPorts = blockedPorts;
  }

  public void addBlockedPort(BlockedPort blockedPort) {
    if (this.blockedPorts == null) {
      this.blockedPorts = new HashSet<>();
    }
    this.blockedPorts.add(blockedPort);
  }

  public void addBlockedPorts(Set<BlockedPort> blockedPorts) {
    if (this.blockedPorts == null) {
      this.blockedPorts = new HashSet<>();
    }
    this.blockedPorts.addAll(blockedPorts);
  }

  public Set<Blacklist> getBlacklists() {
    return blacklists;
  }

  public void setBlacklists(Set<Blacklist> blacklists) {
    this.blacklists = blacklists;
  }

  public void addBlacklist(Blacklist blacklist) {
    if (this.blacklists == null) {
      this.blacklists = new HashSet<>();
    }
    this.blacklists.add(blacklist);
  }

  public void addBlacklists(Set<Blacklist> blacklists) {
    if (this.blacklists == null) {
      this.blacklists = new HashSet<>();
    }
    this.blacklists.addAll(blacklists);
  }

  public Set<BlackListedURL> getBlackListedURLs() {
    return blackListedURLs;
  }

  public void setBlackListedURLs(Set<BlackListedURL> blackListedURLs) {
    this.blackListedURLs = blackListedURLs;
  }

  public void addBlackListedURL(BlackListedURL blackListedURL) {
    if (this.blackListedURLs == null) {
      this.blackListedURLs = new HashSet<>();
    }
    this.blackListedURLs.add(blackListedURL);
  }

  public void addBlackListedURLs(Set<BlackListedURL> blackListedURLs) {
    if (this.blackListedURLs == null) {
      this.blackListedURLs = new HashSet<>();
    }
    this.blackListedURLs.addAll(blackListedURLs);
  }

  public Set<BlackListedCIDR> getBlackListedCIDRs() {
    return blackListedCIDRs;
  }

  public void setBlackListedCIDRs(Set<BlackListedCIDR> blackListedCIDRs) {
    this.blackListedCIDRs = blackListedCIDRs;
  }

  public void addBlackListedCIDR(BlackListedCIDR blackListedCIDR) {
    if (this.blackListedCIDRs == null) {
      this.blackListedCIDRs = new HashSet<>();
    }
    this.blackListedCIDRs.add(blackListedCIDR);
  }

  public void addBlackListedCIDRs(Set<BlackListedCIDR> blackListedCIDRs) {
    if (this.blackListedCIDRs == null) {
      this.blackListedCIDRs = new HashSet<>();
    }
    this.blackListedCIDRs.addAll(blackListedCIDRs);
  }

  public Set<MacroPhase> getMacroPhases() {
    return macroPhases;
  }

  public void setMacroPhases(Set<MacroPhase> macroPhases) {
    this.macroPhases = macroPhases;
  }

  public void addMacroPhase(MacroPhase macroPhase) {
    if (this.macroPhases == null) {
      this.macroPhases = new HashSet<>();
    }
    this.macroPhases.add(macroPhase);
  }

  public void addMacroPhases(Set<MacroPhase> macroPhases) {
    if (this.macroPhases == null) {
      this.macroPhases = new HashSet<>();
    }
    this.macroPhases.addAll(macroPhases);
  }

  public Set<ApplicationPhase> getApplicationPhases() {
    return applicationPhases;
  }

  public void setApplicationPhases(Set<ApplicationPhase> applicationPhases) {
    this.applicationPhases = applicationPhases;
  }

  public void addApplicationPhase(ApplicationPhase applicationPhase) {
    if (this.applicationPhases == null) {
      this.applicationPhases = new HashSet<>();
    }
    this.applicationPhases.add(applicationPhase);
  }

  public void addApplicationPhases(Set<ApplicationPhase> applicationPhases) {
    if (this.applicationPhases == null) {
      this.applicationPhases = new HashSet<>();
    }
    this.applicationPhases.addAll(applicationPhases);
  }

  public Set<SubnetType> getSubnetTypes() {
    return subnetTypes;
  }

  public void setSubnetTypes(Set<SubnetType> subnetTypes) {
    this.subnetTypes = subnetTypes;
  }

  public void addSubnetType(SubnetType subnetType) {
    if (this.subnetTypes == null) {
      this.subnetTypes = new HashSet<>();
    }
    this.subnetTypes.add(subnetType);
  }

  public void addSubnetTypes(Set<SubnetType> subnetTypes) {
    if (this.subnetTypes == null) {
      this.subnetTypes = new HashSet<>();
    }
    this.subnetTypes.addAll(subnetTypes);
  }

  public Set<Provider> getProviders() {
    return providers;
  }

  public void setProviders(Set<Provider> providers) {
    this.providers = providers;
  }

  public void addProvider(Provider provider) {
    if (this.providers == null) {
      this.providers = new HashSet<>();
    }
    this.providers.add(provider);
  }

  public void addProviders(Set<Provider> providers) {
    if (this.providers == null) {
      this.providers = new HashSet<>();
    }
    this.providers.addAll(providers);
  }

  public Set<Namespace> getNamespaces() {
    return namespaces;
  }

  public void setNamespaces(Set<Namespace> namespaces) {
    this.namespaces = namespaces;
  }

  public void addNamespace(Namespace namespace) {
    if (this.namespaces == null) {
      this.namespaces = new HashSet<>();
    }
    this.namespaces.add(namespace);
  }

  public void addNamespaces(Set<Namespace> namespaces) {
    if (this.namespaces == null) {
      this.namespaces = new HashSet<>();
    }
    this.namespaces.addAll(namespaces);
  }

  public Set<ServiceTag> getServiceTags() {
    return serviceTags;
  }

  public void setServiceTags(Set<ServiceTag> serviceTags) {
    this.serviceTags = serviceTags;
  }

  public void addServiceTag(ServiceTag serviceTag) {
    if (this.serviceTags == null) {
      this.serviceTags = new HashSet<>();
    }
    this.serviceTags.add(serviceTag);
  }

  public void addServiceTags(Set<ServiceTag> serviceTags) {
    if (this.serviceTags == null) {
      this.serviceTags = new HashSet<>();
    }
    this.serviceTags.addAll(serviceTags);
  }

  public Set<Continent> getContinents() {
    return continents;
  }

  public void setContinents(Set<Continent> continents) {
    this.continents = continents;
  }

  public void addContinent(Continent continent) {
    if (this.continents == null) {
      this.continents = new HashSet<>();
    }
    this.continents.add(continent);
  }

  public void addContinents(Set<Continent> continents) {
    if (this.continents == null) {
      this.continents = new HashSet<>();
    }
    this.continents.addAll(continents);
  }

  public Set<Region> getRegions() {
    return regions;
  }

  public void setRegions(Set<Region> regions) {
    this.regions = regions;
  }

  public void addRegion(Region region) {
    if (this.regions == null) {
      this.regions = new HashSet<>();
    }
    this.regions.add(region);
  }

  public void addRegions(Set<Region> regions) {
    if (this.regions == null) {
      this.regions = new HashSet<>();
    }
    this.regions.addAll(regions);
  }

  public Set<TargetType> getTargetTypes() {
    return targetTypes;
  }

  public void setTargetTypes(Set<TargetType> targetTypes) {
    this.targetTypes = targetTypes;
  }

  public void addTargetType(TargetType targetType) {
    if (this.targetTypes == null) {
      this.targetTypes = new HashSet<>();
    }
    this.targetTypes.add(targetType);
  }

  public void addTargetTypes(Set<TargetType> targetTypes) {
    if (this.targetTypes == null) {
      this.targetTypes = new HashSet<>();
    }
    this.targetTypes.addAll(targetTypes);
  }

  public Set<Target> getTargets() {
    return targets;
  }

  public void setTargets(Set<Target> targets) {
    this.targets = targets;
  }

  public void addTarget(Target target) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.add(target);
  }

  public void addTargets(Set<Target> targets) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.addAll(targets);
  }

  public Set<FirewallVendor> getFirewallVendors() {
    return firewallVendors;
  }

  public void setFirewallVendors(Set<FirewallVendor> firewallVendors) {
    this.firewallVendors = firewallVendors;
  }

  public void addFirewallVendor(FirewallVendor firewallVendor) {
    if (this.firewallVendors == null) {
      this.firewallVendors = new HashSet<>();
    }
    this.firewallVendors.add(firewallVendor);
  }

  public void addFirewallVendors(Set<FirewallVendor> firewallVendors) {
    if (this.firewallVendors == null) {
      this.firewallVendors = new HashSet<>();
    }
    this.firewallVendors.addAll(firewallVendors);
  }

  public Set<FirewallGroup> getFirewallGroups() {
    return firewallGroups;
  }

  public void setFirewallGroups(Set<FirewallGroup> firewallGroups) {
    this.firewallGroups = firewallGroups;
  }

  public void addFirewallGroup(FirewallGroup firewallGroup) {
    if (firewallGroups == null) {
      firewallGroups = new HashSet<>();
    }
    firewallGroups.add(firewallGroup);
  }

  public void addFirewallGroups(Set<FirewallGroup> firewallGroups) {
    if (this.firewallGroups == null) {
      this.firewallGroups = new HashSet<>();
    }
    this.firewallGroups.addAll(firewallGroups);
  }

  public Set<LandingZoneType> getLandingZoneTypes() {
    return landingZoneTypes;
  }

  public void setLandingZoneTypes(Set<LandingZoneType> landingZoneTypes) {
    this.landingZoneTypes = landingZoneTypes;
  }

  public void addLandingZoneType(LandingZoneType landingZoneType) {
    if (landingZoneTypes == null) {
      landingZoneTypes = new HashSet<>();
    }
    landingZoneTypes.add(landingZoneType);
  }

  public void addLandingZoneTypes(Set<LandingZoneType> landingZoneTypes) {
    if (this.landingZoneTypes == null) {
      this.landingZoneTypes = new HashSet<>();
    }
    this.landingZoneTypes.addAll(landingZoneTypes);
  }

  public Set<LandingZoneFunctionalType> getLandingZoneFunctionalTypes() {
    return landingZoneFunctionalTypes;
  }

  public void setLandingZoneFunctionalTypes(
      Set<LandingZoneFunctionalType> landingZoneFunctionalTypes) {
    this.landingZoneFunctionalTypes = landingZoneFunctionalTypes;
  }

  public void addLandingZoneFunctionalType(LandingZoneFunctionalType landingZoneFunctionalType) {
    if (landingZoneFunctionalTypes == null) {
      landingZoneFunctionalTypes = new HashSet<>();
    }
    landingZoneFunctionalTypes.add(landingZoneFunctionalType);
  }

  public void addLandingZoneFunctionalTypes(
      Set<LandingZoneFunctionalType> landingZoneFunctionalTypes) {
    if (this.landingZoneFunctionalTypes == null) {
      this.landingZoneFunctionalTypes = new HashSet<>();
    }
    this.landingZoneFunctionalTypes.addAll(landingZoneFunctionalTypes);
  }

  public Set<ZoneType> getZoneTypes() {
    return zoneTypes;
  }

  public void setZoneTypes(Set<ZoneType> zoneTypes) {
    this.zoneTypes = zoneTypes;
  }

  public void addZoneType(ZoneType zoneType) {
    if (zoneTypes == null) {
      zoneTypes = new HashSet<>();
    }
    zoneTypes.add(zoneType);
  }

  public void addZoneTypes(Set<ZoneType> zoneTypes) {
    if (this.zoneTypes == null) {
      this.zoneTypes = new HashSet<>();
    }
    this.zoneTypes.addAll(zoneTypes);
  }

  public Set<ConnectionPhase> getConnectionPhases() {
    return connectionPhases;
  }

  public void setConnectionPhases(Set<ConnectionPhase> connectionPhases) {
    this.connectionPhases = connectionPhases;
  }

  public void addConnectionPhase(ConnectionPhase connectionPhase) {
    if (connectionPhases == null) {
      connectionPhases = new HashSet<>();
    }
    connectionPhases.add(connectionPhase);
  }

  public void addConnectionPhases(Set<ConnectionPhase> connectionPhases) {
    if (this.connectionPhases == null) {
      this.connectionPhases = new HashSet<>();
    }
    this.connectionPhases.addAll(connectionPhases);
  }

  public Set<Connection> getConnections() {
    return connections;
  }

  public void setConnections(Set<Connection> connections) {
    this.connections = connections;
  }

  public void addConnection(Connection connection) {
    if (connections == null) {
      connections = new HashSet<>();
    }
    connections.add(connection);
  }

  public void addConnections(Set<Connection> connections) {
    if (this.connections == null) {
      this.connections = new HashSet<>();
    }
    this.connections.addAll(connections);
  }

  public Set<ApplicationId> getApplicationIds() {
    return applicationIds;
  }

  public void setApplicationIds(Set<ApplicationId> applicationIds) {
    this.applicationIds = applicationIds;
  }

  public void addApplicationId(ApplicationId applicationId) {
    if (applicationIds == null) {
      applicationIds = new HashSet<>();
    }
    applicationIds.add(applicationId);
  }

  public void addApplicationIds(Set<ApplicationId> applicationIds) {
    if (this.applicationIds == null) {
      this.applicationIds = new HashSet<>();
    }
    this.applicationIds.addAll(applicationIds);
  }

  public Set<LandingZone> getLandingZones() {
    return landingZones;
  }

  public void setLandingZones(Set<LandingZone> landingZones) {
    this.landingZones = landingZones;
  }

  public void addLandingZone(LandingZone landingZone) {
    if (landingZones == null) {
      landingZones = new HashSet<>();
    }
    landingZones.add(landingZone);
  }

  public void addLandingZones(Set<LandingZone> landingZones) {
    if (this.landingZones == null) {
      this.landingZones = new HashSet<>();
    }
    this.landingZones.addAll(landingZones);
  }

  public Set<ZoneInstance> getZoneInstances() {
    return zoneInstances;
  }

  public void setZoneInstances(Set<ZoneInstance> zoneInstances) {
    this.zoneInstances = zoneInstances;
  }

  public void addZoneInstance(ZoneInstance zoneInstance) {
    if (zoneInstances == null) {
      zoneInstances = new HashSet<>();
    }
    zoneInstances.add(zoneInstance);
  }

  public void addZoneInstances(Set<ZoneInstance> zoneInstances) {
    if (this.zoneInstances == null) {
      this.zoneInstances = new HashSet<>();
    }
    this.zoneInstances.addAll(zoneInstances);
  }

  public Set<SubnetInstance> getSubnetInstances() {
    return subnetInstances;
  }

  public void setSubnetInstances(Set<SubnetInstance> subnetInstances) {
    this.subnetInstances = subnetInstances;
  }

  public void addSubnetInstance(SubnetInstance subnetInstance) {
    if (subnetInstances == null) {
      subnetInstances = new HashSet<>();
    }
    subnetInstances.add(subnetInstance);
  }

  public void addSubnetInstances(Set<SubnetInstance> subnetInstances) {
    if (this.subnetInstances == null) {
      this.subnetInstances = new HashSet<>();
    }
    this.subnetInstances.addAll(subnetInstances);
  }

  public Set<Component> getComponents() {
    return components;
  }

  public void setComponents(Set<Component> components) {
    this.components = components;
  }

  public void addComponent(Component component) {
    if (components == null) {
      components = new HashSet<>();
    }
    components.add(component);
  }

  public void addComponents(Set<Component> components) {
    if (this.components == null) {
      this.components = new HashSet<>();
    }
    this.components.addAll(components);
  }

  public Set<Service> getServices() {
    return services;
  }

  public void setServices(Set<Service> services) {
    this.services = services;
  }

  public void addService(Service service) {
    if (services == null) {
      services = new HashSet<>();
    }
    services.add(service);
  }

  public void addServices(Set<Service> services) {
    if (this.services == null) {
      this.services = new HashSet<>();
    }
    this.services.addAll(services);
  }

  public Set<Dependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(Set<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public void addDependency(Dependency dependency) {
    if (dependencies == null) {
      dependencies = new HashSet<>();
    }
    dependencies.add(dependency);
  }

  public void addDependencies(Set<Dependency> dependencies) {
    if (this.dependencies == null) {
      this.dependencies = new HashSet<>();
    }
    this.dependencies.addAll(dependencies);
  }

  public Set<ComponentInstance> getComponentInstances() {
    return componentInstances;
  }

  public void setComponentInstances(Set<ComponentInstance> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public void addComponentInstance(ComponentInstance componentInstance) {
    if (componentInstances == null) {
      componentInstances = new HashSet<>();
    }
    componentInstances.add(componentInstance);
  }

  public void addComponentInstances(Set<ComponentInstance> componentInstances) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.addAll(componentInstances);
  }

  public Set<AutomaticTemplateInstance> getAutomaticTemplateInstances() {
    return automaticTemplateInstances;
  }

  public void setAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    this.automaticTemplateInstances = automaticTemplateInstances;
  }

  public void addAutomaticTemplateInstance(AutomaticTemplateInstance automaticTemplateInstance) {
    if (automaticTemplateInstances == null) {
      automaticTemplateInstances = new HashSet<>();
    }
    automaticTemplateInstances.add(automaticTemplateInstance);
  }

  public void addAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    if (this.automaticTemplateInstances == null) {
      this.automaticTemplateInstances = new HashSet<>();
    }
    this.automaticTemplateInstances.addAll(automaticTemplateInstances);
  }

  public Set<IPAddressInstance> getIpAddressInstances() {
    return ipAddressInstances;
  }

  public void setIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    this.ipAddressInstances = ipAddressInstances;
  }

  public void addIpAddressInstance(IPAddressInstance ipAddressInstance) {
    if (ipAddressInstances == null) {
      ipAddressInstances = new HashSet<>();
    }
    ipAddressInstances.add(ipAddressInstance);
  }

  public void addIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    if (this.ipAddressInstances == null) {
      this.ipAddressInstances = new HashSet<>();
    }
    this.ipAddressInstances.addAll(ipAddressInstances);
  }

  public Set<DependencyInstance> getDependencyInstances() {
    return dependencyInstances;
  }

  public void setDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public void addDependencyInstance(DependencyInstance dependencyInstance) {
    if (dependencyInstances == null) {
      dependencyInstances = new HashSet<>();
    }
    dependencyInstances.add(dependencyInstance);
  }

  public void addDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.addAll(dependencyInstances);
  }

  public Set<OutboundSNAT> getOutboundSNATs() {
    return outboundSNATs;
  }

  public void setOutboundSNATs(Set<OutboundSNAT> outboundSNATs) {
    this.outboundSNATs = outboundSNATs;
  }

  public void addOutboundSNAT(OutboundSNAT outboundSNAT) {
    if (outboundSNATs == null) {
      outboundSNATs = new HashSet<>();
    }
    outboundSNATs.add(outboundSNAT);
  }

  public void addOutboundSNATs(Set<OutboundSNAT> outboundSNATs) {
    if (this.outboundSNATs == null) {
      this.outboundSNATs = new HashSet<>();
    }
    this.outboundSNATs.addAll(outboundSNATs);
  }

  public void setDefaultScopesBitbucketProject(String bitbucketProject) {
    this.defaultScopesBitbucketProject = bitbucketProject;
  }

  public Set<ScopeReleaseNote> getScopeReleaseNotes() {
    return scopeReleaseNotes;
  }

  public void setScopeReleaseNotes(Set<ScopeReleaseNote> scopeReleaseNotes) {
    this.scopeReleaseNotes = scopeReleaseNotes;
  }

  public void addScopeReleaseNotes(Set<ScopeReleaseNote> scopeReleaseNotes) {
    if (this.scopeReleaseNotes == null) {
      this.scopeReleaseNotes = new HashSet<>();
    }
    this.scopeReleaseNotes.addAll(scopeReleaseNotes);
  }

  public void addScopeReleaseNote(ScopeReleaseNote scopeReleaseNote) {
    if (this.scopeReleaseNotes == null) {
      this.scopeReleaseNotes = new HashSet<>();
    }
    this.scopeReleaseNotes.add(scopeReleaseNote);
  }

  public Set<ScopeVersionMapping> getScopeVersionMapping() {
    return scopeVersionMapping;
  }

  public void setScopeVersionMapping(Set<ScopeVersionMapping> scopeVersionMapping) {
    this.scopeVersionMapping = scopeVersionMapping;
  }

  public void addScopeVersionMapping(ScopeVersionMapping scopeVersionMapping) {
    if (this.scopeVersionMapping == null) {
      this.scopeVersionMapping = new HashSet<>();
    }
    this.scopeVersionMapping.add(scopeVersionMapping);
  }

  public void addScopeVersionMappings(Set<ScopeVersionMapping> scopeVersionMappings) {
    if (this.scopeVersionMapping == null) {
      this.scopeVersionMapping = new HashSet<>();
    }
    this.scopeVersionMapping.addAll(scopeVersionMappings);
  }

  public void addConnectionApprovalComponentSkip(
      ConnectionApprovalComponentSkip connectionApprovalComponentSkip) {
    if (connectionApprovalComponentSkips == null) {
      connectionApprovalComponentSkips = new HashSet<>();
    }
    connectionApprovalComponentSkips.add(connectionApprovalComponentSkip);
  }

  public void addConnectionApprovalComponentSkips(
      Set<ConnectionApprovalComponentSkip> connectionApprovalComponentSkips) {
    if (this.connectionApprovalComponentSkips == null) {
      this.connectionApprovalComponentSkips = new HashSet<>();
    }
    this.connectionApprovalComponentSkips.addAll(connectionApprovalComponentSkips);
  }

  public void addConnectionApprovalScopeSkip(
      ConnectionApprovalScopeSkip connectionApprovalScopeSkip) {
    if (connectionApprovalScopeSkips == null) {
      connectionApprovalScopeSkips = new HashSet<>();
    }
    connectionApprovalScopeSkips.add(connectionApprovalScopeSkip);
  }

  public Set<ConnectionApprovalComponentSkip> getConnectionApprovalComponentSkips() {
    return connectionApprovalComponentSkips;
  }

  public Set<ConnectionApprovalScopeSkip> getConnectionApprovalScopeSkips() {
    return connectionApprovalScopeSkips;
  }

  public enum GroupTypes {
    NORMAL
  }
}
