package com.amadeus.aud.libraries.engine.data.models.core.internals;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessControlEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessRightsEntryEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationIdsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AutomaticTemplateInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlackListedCIDRsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlackListedURLsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlacklistsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlockedPortsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionApprovalComponentSkipsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionApprovalScopeSkipsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ConnectionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallGroupsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallVendorsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.IPAddressInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.OutboundSNATsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RolesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopeReleaseNotesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopeVersionMappingsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServiceTagsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
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
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Version {
  private static final Pattern repositoryNamePattern = Pattern.compile(".*\\.");
  private final Map<String, ConcurrentRepository<? extends BomElement>> modelsRepositoryMap =
      new ConcurrentHashMap<>();
  private final Map<Type, String> singleTypeToRepositoryName = new HashMap<>();
  private int hashCode;
  private String versionNumber;
  private long versionLong;
  private AudDataRepository audDataRepository;
  private AudRequirementsRepository audRequirementsRepository;
  private AccessControlEntitiesRepository accessControlEntitiesRepository;
  private AccessRightsEntryEntitiesRepository accessRightsEntryEntitiesRepository;

  @Override
  public String toString() {
    return getVersionNumber();
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(getVersionNumber());
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return (obj instanceof Version v) && Objects.equals(getVersionNumber(), v.getVersionNumber());
  }

  public String getRepositoryNameFromSingleType(@Nullable Class<?> type) {
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null");
    }
    return this.singleTypeToRepositoryName.getOrDefault(type, "");
  }

  private static String getRepositoryName(@Nullable ConcurrentRepository<?> repository) {
    if (repository == null) {
      throw new IllegalArgumentException("Repository cannot be null");
    }
    return repositoryNamePattern
        .matcher(repository.getClass().getTypeName())
        .replaceAll("")
        .replace("iesRepository", "y")
        .replace("sRepository", "")
        .toLowerCase(Locale.ROOT);
  }

  public Set<String> getRepositoryNames() {
    return this.modelsRepositoryMap.keySet();
  }

  public <T extends BomElement> ConcurrentRepository<T> getRepository(
      ConcurrentRepository<?> repository) {
    getRepositoryName(repository);
    return getRepository(getRepositoryName(repository));
  }

  public <T extends BomElement> ConcurrentRepository<T> getRepository(String objectType) {
    String key = objectType.toLowerCase(Locale.ROOT);
    @SuppressWarnings("unchecked")
    ConcurrentRepository<T> output = (ConcurrentRepository<T>) modelsRepositoryMap.get(key);
    if (output == null) {
      Log.infof("Getting repo of type %s failed", key);
    }
    return output;
  }

  public <T extends BomElement> ConcurrentRepository<T> getRepository(Class<T> type) {
    String key = type.getSimpleName();
    return getRepository(key);
  }

  public synchronized void addRepository(ConcurrentRepository<? extends BomElement> repository) {
    String key = getRepositoryName(repository);
    this.modelsRepositoryMap.put(key, repository);
    Type superClass = repository.getClass().getGenericSuperclass();
    if (superClass instanceof ParameterizedType parameterizedType) {
      this.singleTypeToRepositoryName.put(parameterizedType.getActualTypeArguments()[0], key);
    }
  }

  public Map<String, Long> getRepositorySizes() {
    Map<String, Long> repositorySizes = new HashMap<>();
    for (Map.Entry<String, ConcurrentRepository<? extends BomElement>> entry :
        modelsRepositoryMap.entrySet()) {
      ConcurrentRepository<?> value = entry.getValue();
      Long dataSize = value != null ? value.getDataSize() : 0L;
      repositorySizes.put(entry.getKey(), dataSize);
    }
    return repositorySizes;
  }

  public String getVersionNumber() {
    return versionNumber;
  }

  public void setVersionNumber(String versionNumber) {
    this.versionNumber = versionNumber;
    this.versionLong = VersionUtils.toLong(getVersionNumber());
  }

  public long getVersionLong() {
    if (versionLong == 0) {
      versionLong = VersionUtils.toLong(getVersionNumber());
    }
    return versionLong;
  }

  public AudDataRepository getAudDataRepository() {
    return audDataRepository;
  }

  public void setAudDataRepository(AudDataRepository audDataRepository) {
    this.audDataRepository = audDataRepository;
  }

  public AudRequirementsRepository getAudRequirementsRepository() {
    return audRequirementsRepository;
  }

  public void setAudRequirementsRepository(AudRequirementsRepository audRequirementsRepository) {
    this.audRequirementsRepository = audRequirementsRepository;
  }

  public ApplicationIdsRepository getApplicationIdsRepository() {
    return (ApplicationIdsRepository) getRepository(ApplicationId.class);
  }

  public void setApplicationIdsRepository(ApplicationIdsRepository applicationIdsRepository) {
    addRepository(applicationIdsRepository);
  }

  public ApplicationPhasesRepository getApplicationPhasesRepository() {
    return (ApplicationPhasesRepository) getRepository(ApplicationPhase.class);
  }

  public void setApplicationPhasesRepository(
      ApplicationPhasesRepository applicationPhasesRepository) {
    addRepository(applicationPhasesRepository);
  }

  public AutomaticTemplateInstancesRepository getAutomaticTemplateInstancesRepository() {
    return (AutomaticTemplateInstancesRepository) getRepository(AutomaticTemplateInstance.class);
  }

  public void setAutomaticTemplateInstancesRepository(
      AutomaticTemplateInstancesRepository automaticTemplateInstancesRepository) {
    addRepository(automaticTemplateInstancesRepository);
  }

  public BlacklistsRepository getBlacklistsRepository() {
    return (BlacklistsRepository) getRepository(Blacklist.class);
  }

  public void setBlacklistsRepository(BlacklistsRepository blacklistsRepository) {
    addRepository(blacklistsRepository);
  }

  public BlackListedURLsRepository getBlackListedURLsRepository() {
    return (BlackListedURLsRepository) getRepository(BlackListedURL.class);
  }

  public void setBlackListedURLsRepository(BlackListedURLsRepository blackListedURLsRepository) {
    addRepository(blackListedURLsRepository);
  }

  public BlackListedCIDRsRepository getBlackListedCIDRsRepository() {
    return (BlackListedCIDRsRepository) getRepository(BlackListedCIDR.class);
  }

  public void setBlackListedCIDRsRepository(BlackListedCIDRsRepository blackListedCIDRsRepository) {
    addRepository(blackListedCIDRsRepository);
  }

  public BlockedPortsRepository getBlockedPortsRepository() {
    return (BlockedPortsRepository) getRepository(BlockedPort.class);
  }

  public void setBlockedPortsRepository(BlockedPortsRepository blockedPortsRepository) {
    addRepository(blockedPortsRepository);
  }

  public ComponentInstancesRepository getComponentInstancesRepository() {
    return (ComponentInstancesRepository) getRepository(ComponentInstance.class);
  }

  public void setComponentInstancesRepository(
      ComponentInstancesRepository componentInstancesRepository) {
    addRepository(componentInstancesRepository);
  }

  public ComponentsRepository getComponentsRepository() {
    return (ComponentsRepository) getRepository(Component.class);
  }

  public void setComponentsRepository(ComponentsRepository componentsRepository) {
    addRepository(componentsRepository);
  }

  public ConnectionApprovalComponentSkipsRepository
      getConnectionApprovalComponentSkipsRepository() {
    return (ConnectionApprovalComponentSkipsRepository)
        getRepository(ConnectionApprovalComponentSkip.class);
  }

  public void setConnectionApprovalComponentSkipsRepository(
      ConnectionApprovalComponentSkipsRepository connectionApprovalComponentSkipsRepository) {
    addRepository(connectionApprovalComponentSkipsRepository);
  }

  public ConnectionApprovalScopeSkipsRepository getConnectionApprovalScopeSkipsRepository() {
    return (ConnectionApprovalScopeSkipsRepository)
        getRepository(ConnectionApprovalScopeSkip.class);
  }

  public void setConnectionApprovalScopeSkipsRepository(
      ConnectionApprovalScopeSkipsRepository connectionApprovalScopeSkipsRepository) {
    addRepository(connectionApprovalScopeSkipsRepository);
  }

  public ConnectionPhasesRepository getConnectionPhasesRepository() {
    return (ConnectionPhasesRepository) getRepository(ConnectionPhase.class);
  }

  public void setConnectionPhasesRepository(ConnectionPhasesRepository connectionPhasesRepository) {
    addRepository(connectionPhasesRepository);
  }

  public ConnectionsRepository getConnectionsRepository() {
    return (ConnectionsRepository) getRepository(Connection.class);
  }

  public void setConnectionsRepository(ConnectionsRepository connectionsRepository) {
    addRepository(connectionsRepository);
  }

  public ContinentsRepository getContinentsRepository() {
    return (ContinentsRepository) getRepository(Continent.class);
  }

  public void setContinentsRepository(ContinentsRepository continentsRepository) {
    addRepository(continentsRepository);
  }

  public DependenciesRepository getDependenciesRepository() {
    return (DependenciesRepository) getRepository(Dependency.class);
  }

  public void setDependenciesRepository(DependenciesRepository dependenciesRepository) {
    addRepository(dependenciesRepository);
  }

  public DependencyInstancesRepository getDependencyInstancesRepository() {
    return (DependencyInstancesRepository) getRepository(DependencyInstance.class);
  }

  public void setDependencyInstancesRepository(
      DependencyInstancesRepository dependencyInstancesRepository) {
    addRepository(dependencyInstancesRepository);
  }

  public FirewallGroupsRepository getFirewallGroupsRepository() {
    return (FirewallGroupsRepository) getRepository(FirewallGroup.class);
  }

  public void setFirewallGroupsRepository(FirewallGroupsRepository firewallGroupsRepository) {
    addRepository(firewallGroupsRepository);
  }

  public FirewallVendorsRepository getFirewallVendorsRepository() {
    return (FirewallVendorsRepository) getRepository(FirewallVendor.class);
  }

  public void setFirewallVendorsRepository(FirewallVendorsRepository firewallVendorsRepository) {
    addRepository(firewallVendorsRepository);
  }

  public IPAddressInstancesRepository getIpAddressInstancesRepository() {
    return (IPAddressInstancesRepository) getRepository(IPAddressInstance.class);
  }

  public void setIpAddressInstancesRepository(
      IPAddressInstancesRepository ipAddressInstancesRepository) {
    addRepository(ipAddressInstancesRepository);
  }

  public LandingZoneFunctionalTypesRepository getLandingZoneFunctionalTypesRepository() {
    return (LandingZoneFunctionalTypesRepository) getRepository(LandingZoneFunctionalType.class);
  }

  public void setLandingZoneFunctionalTypesRepository(
      LandingZoneFunctionalTypesRepository landingZoneFunctionalTypesRepository) {
    addRepository(landingZoneFunctionalTypesRepository);
  }

  public LandingZonesRepository getLandingZonesRepository() {
    return (LandingZonesRepository) getRepository(LandingZone.class);
  }

  public void setLandingZonesRepository(LandingZonesRepository landingZonesRepository) {
    addRepository(landingZonesRepository);
  }

  public LandingZoneTypesRepository getLandingZoneTypesRepository() {
    return (LandingZoneTypesRepository) getRepository(LandingZoneType.class);
  }

  public void setLandingZoneTypesRepository(LandingZoneTypesRepository landingZoneTypesRepository) {
    addRepository(landingZoneTypesRepository);
  }

  public MacroPhasesRepository getMacroPhasesRepository() {
    return (MacroPhasesRepository) getRepository(MacroPhase.class);
  }

  public void setMacroPhasesRepository(MacroPhasesRepository macroPhasesRepository) {
    addRepository(macroPhasesRepository);
  }

  public NamespacesRepository getNamespacesRepository() {
    return (NamespacesRepository) getRepository(Namespace.class);
  }

  public void setNamespacesRepository(NamespacesRepository namespacesRepository) {
    addRepository(namespacesRepository);
  }

  public OutboundSNATsRepository getOutboundSNATsRepository() {
    return (OutboundSNATsRepository) getRepository(OutboundSNAT.class);
  }

  public void setOutboundSNATsRepository(OutboundSNATsRepository outboundSNATsRepository) {
    addRepository(outboundSNATsRepository);
  }

  public ProvidersRepository getProvidersRepository() {
    return (ProvidersRepository) getRepository(Provider.class);
  }

  public void setProvidersRepository(ProvidersRepository providersRepository) {
    addRepository(providersRepository);
  }

  public RegionsRepository getRegionsRepository() {
    return (RegionsRepository) getRepository(Region.class);
  }

  public void setRegionsRepository(RegionsRepository regionsRepository) {
    addRepository(regionsRepository);
  }

  public RolesRepository getRolesRepository() {
    return (RolesRepository) getRepository(Role.class);
  }

  public void setRolesRepository(RolesRepository rolesRepository) {
    addRepository(rolesRepository);
  }

  public ScopeReleaseNotesRepository getScopeReleaseNotesRepository() {
    return (ScopeReleaseNotesRepository) getRepository(ScopeReleaseNote.class);
  }

  public void setScopeReleaseNotesRepository(
      ScopeReleaseNotesRepository scopeReleaseNotesRepository) {
    addRepository(scopeReleaseNotesRepository);
  }

  public ScopeVersionMappingsRepository getScopeVersionMappingsRepository() {
    return (ScopeVersionMappingsRepository) getRepository(ScopeVersionMapping.class);
  }

  public void setScopeVersionMappingsRepository(
      ScopeVersionMappingsRepository scopeVersionMappingsRepository) {
    addRepository(scopeVersionMappingsRepository);
  }

  public ScopesRepository getScopesRepository() {
    return (ScopesRepository) getRepository(Scope.class);
  }

  public void setScopesRepository(ScopesRepository scopesRepository) {
    addRepository(scopesRepository);
  }

  public ServicesRepository getServicesRepository() {
    return (ServicesRepository) getRepository(Service.class);
  }

  public void setServicesRepository(ServicesRepository servicesRepository) {
    addRepository(servicesRepository);
  }

  public ServiceTagsRepository getServiceTagsRepository() {
    return (ServiceTagsRepository) getRepository(ServiceTag.class);
  }

  public void setServiceTagsRepository(ServiceTagsRepository serviceTagsRepository) {
    addRepository(serviceTagsRepository);
  }

  public SubnetInstancesRepository getSubnetInstancesRepository() {
    return (SubnetInstancesRepository) getRepository(SubnetInstance.class);
  }

  public void setSubnetInstancesRepository(SubnetInstancesRepository subnetInstancesRepository) {
    addRepository(subnetInstancesRepository);
  }

  public SubnetTypesRepository getSubnetTypesRepository() {
    return (SubnetTypesRepository) getRepository(SubnetType.class);
  }

  public void setSubnetTypesRepository(SubnetTypesRepository subnetTypesRepository) {
    addRepository(subnetTypesRepository);
  }

  public TargetsRepository getTargetsRepository() {
    return (TargetsRepository) getRepository(Target.class);
  }

  public void setTargetsRepository(TargetsRepository targetsRepository) {
    addRepository(targetsRepository);
  }

  public TargetTypesRepository getTargetTypesRepository() {
    return (TargetTypesRepository) getRepository(TargetType.class);
  }

  public void setTargetTypesRepository(TargetTypesRepository targetTypesRepository) {
    addRepository(targetTypesRepository);
  }

  public ZoneInstancesRepository getZoneInstancesRepository() {
    return (ZoneInstancesRepository) getRepository(ZoneInstance.class);
  }

  public void setZoneInstancesRepository(ZoneInstancesRepository zoneInstancesRepository) {
    addRepository(zoneInstancesRepository);
  }

  public ZoneTypesRepository getZoneTypesRepository() {
    return (ZoneTypesRepository) getRepository(ZoneType.class);
  }

  public void setZoneTypesRepository(ZoneTypesRepository zoneTypesRepository) {
    addRepository(zoneTypesRepository);
  }

  public AccessControlEntitiesRepository getAccessControlEntitiesRepository() {
    return accessControlEntitiesRepository;
  }

  public void setAccessControlEntitiesRepository(
      AccessControlEntitiesRepository accessControlEntityRepository) {
    this.accessControlEntitiesRepository = accessControlEntityRepository;
  }

  public AccessRightsEntryEntitiesRepository getAccessRightsEntryEntitiesRepository() {
    return accessRightsEntryEntitiesRepository;
  }

  public void setAccessRightsEntryEntitiesRepository(
      AccessRightsEntryEntitiesRepository accessRightsEntryEntitiesRepository) {
    this.accessRightsEntryEntitiesRepository = accessRightsEntryEntitiesRepository;
  }

  public Iterator<BomElement> all() {
    Iterator<ConcurrentRepository<? extends BomElement>> repositories =
        modelsRepositoryMap.values().iterator();

    return new Iterator<>() {
      private Iterator<? extends BomElement> bomIterator;

      @Override
      public boolean hasNext() {
        while ((bomIterator == null || !bomIterator.hasNext()) && repositories.hasNext()) {
          ConcurrentRepository<? extends BomElement> nextRepo = repositories.next();
          bomIterator = nextRepo.getData().values().iterator();
        }
        return bomIterator != null && bomIterator.hasNext();
      }

      @Override
      public BomElement next() {
        if (!hasNext()) {
          throw new IllegalArgumentException("");
        }
        return bomIterator.next();
      }
    };
  }
}
