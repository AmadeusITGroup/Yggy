package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.VersionBuilder;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.AccessControlService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ApplicationIdService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ApplicationPhaseService;
import com.amadeus.aud.libraries.engine.data.services.core.db.BlackListedCIDRService;
import com.amadeus.aud.libraries.engine.data.services.core.db.BlackListedURLService;
import com.amadeus.aud.libraries.engine.data.services.core.db.BlacklistService;
import com.amadeus.aud.libraries.engine.data.services.core.db.BlockedPortService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ComponentInstanceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ComponentService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ConnectionApprovalComponentSkipService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ConnectionApprovalScopeSkipService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ConnectionPhaseService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ConnectionService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ContinentService;
import com.amadeus.aud.libraries.engine.data.services.core.db.DependencyInstanceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.DependencyService;
import com.amadeus.aud.libraries.engine.data.services.core.db.FirewallGroupService;
import com.amadeus.aud.libraries.engine.data.services.core.db.FirewallVendorService;
import com.amadeus.aud.libraries.engine.data.services.core.db.IPAddressInstanceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.LandingZoneFunctionalTypeService;
import com.amadeus.aud.libraries.engine.data.services.core.db.LandingZoneService;
import com.amadeus.aud.libraries.engine.data.services.core.db.LandingZoneTypeService;
import com.amadeus.aud.libraries.engine.data.services.core.db.MacroPhaseService;
import com.amadeus.aud.libraries.engine.data.services.core.db.NamespaceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.OutboundSNATService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ProviderService;
import com.amadeus.aud.libraries.engine.data.services.core.db.RegionService;
import com.amadeus.aud.libraries.engine.data.services.core.db.RoleService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ScopeReleaseNoteService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ScopeVersionMappingService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ServiceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ServiceTagService;
import com.amadeus.aud.libraries.engine.data.services.core.db.SubnetInstanceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.SubnetTypeService;
import com.amadeus.aud.libraries.engine.data.services.core.db.TargetService;
import com.amadeus.aud.libraries.engine.data.services.core.db.TargetTypeService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ZoneInstanceService;
import com.amadeus.aud.libraries.engine.data.services.core.db.ZoneTypeService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.DataProcessor;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.context.ManagedExecutor;

@ApplicationScoped
public class VersionService implements VersionBuilder {

  private final AtomicReference<String> currentVersion = new AtomicReference<>();
  private final VersionPreInitHook versionPreInitHook;
  private final VersionPostInitHook versionPostInit;
  private final DataProcessor<AudRequirementsRepository> audRequirementsService;
  private final DataProcessor<AudDataRepository> audDataService;
  private final AudVersionsService audVersionsService;
  private final ManagedExecutor managedExecutor;
  private final VersionCacheProducer versionCacheProducer;
  private LoadingCache<String, Version> versionCache;

  private final ApplicationIdService applicationIdService;
  private final ApplicationPhaseService applicationPhaseService;
  private final BlacklistService blacklistService;
  private final BlackListedURLService blackListedURLService;
  private final BlackListedCIDRService blackListedCIDRService;
  private final BlockedPortService blockedPortService;
  private final ComponentInstanceService componentInstanceService;
  private final ComponentService componentService;
  private final ConnectionApprovalComponentSkipService connectionApprovalComponentSkipService;
  private final ConnectionApprovalScopeSkipService connectionApprovalScopeSkipService;
  private final ConnectionPhaseService connectionPhaseService;
  private final ConnectionService connectionService;
  private final ContinentService continentService;
  private final DependencyInstanceService dependencyInstanceService;
  private final DependencyService dependencyService;
  private final FirewallGroupService firewallGroupService;
  private final FirewallVendorService firewallVendorService;
  private final IPAddressInstanceService ipAddressInstanceService;
  private final LandingZoneFunctionalTypeService landingZoneFunctionalTypeService;
  private final LandingZoneService landingZoneService;
  private final LandingZoneTypeService landingZoneTypeService;
  private final MacroPhaseService macroPhaseService;
  private final NamespaceService namespaceService;
  private final OutboundSNATService outboundSNATService;
  private final ProviderService providerService;
  private final RegionService regionService;
  private final RoleService roleService;
  private final ScopeReleaseNoteService scopeReleaseNoteService;
  private final ScopeVersionMappingService scopeVersionMappingService;
  private final ServiceService serviceService;
  private final ServiceTagService serviceTagService;
  private final SubnetInstanceService subnetInstanceService;
  private final SubnetTypeService subnetTypeService;
  private final TargetService targetService;
  private final TargetTypeService targetTypeService;
  private final ZoneInstanceService zoneInstanceService;
  private final ZoneTypeService zoneTypeService;
  private final AccessControlService accessControlService;

  @Inject
  public VersionService(
      VersionPreInitHook versionPreInitHook,
      VersionPostInitHook versionPostInit,
      DataProcessor<AudRequirementsRepository> audRequirementsService,
      DataProcessor<AudDataRepository> audDataService,
      AudVersionsService audVersionsService,
      ManagedExecutor managedExecutor,
      VersionCacheProducer versionCacheProducer,
      ApplicationIdService applicationIdService,
      ApplicationPhaseService applicationPhaseService,
      BlockedPortService blockedPortService,
      BlacklistService blacklistService,
      BlackListedURLService blackListedURLService,
      BlackListedCIDRService blackListedCIDRService,
      ComponentInstanceService componentInstanceService,
      ComponentService componentService,
      ConnectionApprovalComponentSkipService connectionApprovalComponentSkipService,
      ConnectionApprovalScopeSkipService connectionApprovalScopeSkipService,
      ConnectionPhaseService connectionPhaseService,
      ConnectionService connectionService,
      ContinentService continentService,
      DependencyInstanceService dependencyInstanceService,
      DependencyService dependencyService,
      FirewallGroupService firewallGroupService,
      FirewallVendorService firewallVendorService,
      IPAddressInstanceService ipAddressInstanceService,
      LandingZoneFunctionalTypeService landingZoneFunctionalTypeService,
      LandingZoneService landingZoneService,
      LandingZoneTypeService landingZoneTypeService,
      MacroPhaseService macroPhaseService,
      NamespaceService namespaceService,
      OutboundSNATService outboundSNATService,
      ProviderService providerService,
      RegionService regionService,
      RoleService roleService,
      ScopeReleaseNoteService scopeReleaseNoteService,
      ScopeVersionMappingService scopeVersionMappingService,
      ServiceService serviceService,
      ServiceTagService serviceTagService,
      SubnetInstanceService subnetInstanceService,
      SubnetTypeService subnetTypeService,
      TargetService targetService,
      TargetTypeService targetTypeService,
      ZoneInstanceService zoneInstanceService,
      ZoneTypeService zoneTypeService,
      AccessControlService accessControlService) {
    this.versionPreInitHook = versionPreInitHook;
    this.versionPostInit = versionPostInit;
    this.audRequirementsService = audRequirementsService;
    this.audDataService = audDataService;
    this.audVersionsService = audVersionsService;
    this.managedExecutor = managedExecutor;
    this.versionCacheProducer = versionCacheProducer;
    this.applicationIdService = applicationIdService;
    this.applicationPhaseService = applicationPhaseService;
    this.blockedPortService = blockedPortService;
    this.blacklistService = blacklistService;
    this.blackListedURLService = blackListedURLService;
    this.blackListedCIDRService = blackListedCIDRService;
    this.componentInstanceService = componentInstanceService;
    this.componentService = componentService;
    this.connectionApprovalComponentSkipService = connectionApprovalComponentSkipService;
    this.connectionApprovalScopeSkipService = connectionApprovalScopeSkipService;
    this.connectionPhaseService = connectionPhaseService;
    this.connectionService = connectionService;
    this.continentService = continentService;
    this.dependencyInstanceService = dependencyInstanceService;
    this.dependencyService = dependencyService;
    this.firewallGroupService = firewallGroupService;
    this.firewallVendorService = firewallVendorService;
    this.ipAddressInstanceService = ipAddressInstanceService;
    this.landingZoneFunctionalTypeService = landingZoneFunctionalTypeService;
    this.landingZoneService = landingZoneService;
    this.landingZoneTypeService = landingZoneTypeService;
    this.macroPhaseService = macroPhaseService;
    this.namespaceService = namespaceService;
    this.outboundSNATService = outboundSNATService;
    this.providerService = providerService;
    this.regionService = regionService;
    this.roleService = roleService;
    this.scopeReleaseNoteService = scopeReleaseNoteService;
    this.scopeVersionMappingService = scopeVersionMappingService;
    this.serviceService = serviceService;
    this.serviceTagService = serviceTagService;
    this.subnetInstanceService = subnetInstanceService;
    this.subnetTypeService = subnetTypeService;
    this.targetService = targetService;
    this.targetTypeService = targetTypeService;
    this.zoneInstanceService = zoneInstanceService;
    this.zoneTypeService = zoneTypeService;
    this.accessControlService = accessControlService;
  }

  @PostConstruct
  public void init() {
    versionCacheProducer.setLoader(this::buildVersion);
    this.versionCache = versionCacheProducer.getCache();
  }

  public Uni<Version> getCurrentVersion() {
    String currentVersionId = currentVersion.get();
    if (currentVersionId == null) {
      return Uni.createFrom().nullItem();
    }
    return getVersion(currentVersionId);
  }

  public Uni<String> checkForNewVersion() {
    return getCurrentVersion()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            (Version currentVersion) -> {
              if (currentVersion == null) {
                return audVersionsService.checkNewVersion();
              } else {
                return audVersionsService.checkNewVersion(currentVersion.getVersionNumber());
              }
            });
  }

  public Uni<Version> getVersion(String versionNumber) {
    return Uni.createFrom()
        .item(() -> versionCache.get(versionNumber))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  public Version buildVersion(String key) {
    return buildVersion(key, null);
  }

  @Override
  public Version buildVersion(String key, @Nullable AudDataRepository overrideDataRepository) {
    Version version = new Version();
    version.setVersionNumber(key);

    // Instantiate and set all repositories
    Temporal audReqStartTime = Instant.now();
    if (overrideDataRepository != null) {
      version.setAudDataRepository(overrideDataRepository);
      return initModels(version, false)
          .onItem()
          .transform(
              (Void ignored) -> {
                String currentVersion = this.currentVersion.get();
                if (currentVersion == null || key.compareTo(currentVersion) > 0) {
                  this.currentVersion.set(key);
                }
                this.versionPostInit.run(version);
                return version;
              })
          .onFailure()
          .invoke((Throwable throwable) -> Log.errorf(throwable.getMessage()))
          .await()
          .indefinitely();
    }

    return audRequirementsService
        .initObjectsWithFallback(key)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            (AudRequirementsRepository requirementsRepository) -> {
              version.setAudRequirementsRepository(requirementsRepository);

              String requirementsVersion = requirementsRepository.getDataVersion();

              Log.infof(
                  "AUD Requirements (%s) have been initialized in %s",
                  requirementsVersion, Duration.between(audReqStartTime, Instant.now()));

              Temporal audDataStartTime = Instant.now();
              return audDataService
                  .initObjectsWithFallback(requirementsVersion)
                  .onItem()
                  .transformToUni(
                      (AudDataRepository dataRepository) -> {
                        version.setAudDataRepository(dataRepository);

                        Log.infof(
                            "AUD Data (%s) has been initialized in %s",
                            requirementsVersion, Duration.between(audDataStartTime, Instant.now()));

                        Temporal audServicesStartTime = Instant.now();
                        return initModels(version, true)
                            .onItem()
                            .transform(
                                (Void ignored) -> {
                                  Log.infof(
                                      "All model services for version %s have been initialized in %s",
                                      requirementsVersion,
                                      Duration.between(audServicesStartTime, Instant.now()));

                                  String currentVersion = this.currentVersion.get();
                                  if (currentVersion == null || key.compareTo(currentVersion) > 0) {
                                    this.currentVersion.set(key);
                                  }
                                  this.versionPostInit.run(version);
                                  return version;
                                })
                            .onFailure()
                            .invoke((Throwable throwable) -> Log.errorf(throwable.getMessage()));
                      });
            })
        .await()
        .indefinitely();
  }

  private Uni<Void> initModels(Version version, boolean associateWithRequirement) {
    Objects.requireNonNull(version, "Version cannot be null");
    return versionPreInitHook
        .run(version, associateWithRequirement)
        .onItem()
        .transformToUni(
            (Void ignored) -> {
              initBomVersion(version);
              return Uni.createFrom().voidItem();
            });
  }

  public void initBomVersion(Version version) {
    // IMPORTANT: KEEP THE ORDER OF INITIALIZATION

    // From now on, some services can be run in parallel
    CompletableFuture<Void> futureScopeReleaseNotes =
        managedExecutor.runAsync(() -> scopeReleaseNoteService.init(version));
    CompletableFuture<Void> futureScopeVersionMappings =
        managedExecutor.runAsync(() -> scopeVersionMappingService.init(version));

    CompletableFuture<Void> futureRoles = managedExecutor.runAsync(() -> roleService.init(version));

    CompletableFuture<Void> futureBlockedPorts =
        managedExecutor.runAsync(() -> blockedPortService.init(version));
    CompletableFuture<Void> futureBlacklists =
        managedExecutor.runAsync(() -> blacklistService.init(version));
    CompletableFuture<Void> futureBlackListedURLs =
        managedExecutor.runAsync(() -> blackListedURLService.init(version));
    CompletableFuture<Void> futureBlackListedCIDRs =
        managedExecutor.runAsync(() -> blackListedCIDRService.init(version));
    CompletableFuture<Void> futureLandingZoneFunctionalTypes =
        managedExecutor.runAsync(() -> landingZoneFunctionalTypeService.init(version));
    CompletableFuture<Void> futureMacroPhase =
        managedExecutor.runAsync(
            () -> {
              futureLandingZoneFunctionalTypes.join();
              macroPhaseService.init(version);
            });

    CompletableFuture<Void> futureApplicationPhases =
        managedExecutor.runAsync(
            () -> {
              futureMacroPhase.join();
              applicationPhaseService.init(version);
            });

    CompletableFuture<Void> futureSubnetTypes =
        managedExecutor.runAsync(() -> subnetTypeService.init(version));
    CompletableFuture<Void> futureProviders =
        managedExecutor.runAsync(
            () -> {
              futureLandingZoneFunctionalTypes.join();
              providerService.init(version);
            });
    CompletableFuture<Void> futureContinents =
        managedExecutor.runAsync(
            () -> {
              futureLandingZoneFunctionalTypes.join();
              continentService.init(version);
            });

    CompletableFuture<Void> futureRegions =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureProviders, futureContinents).join();
              regionService.init(version);
            });

    CompletableFuture<Void> futureNamespaces =
        managedExecutor.runAsync(() -> namespaceService.init(version));
    CompletableFuture<Void> futureServiceTags =
        managedExecutor.runAsync(() -> serviceTagService.init(version));
    CompletableFuture<Void> futureTargetTypes =
        managedExecutor.runAsync(() -> targetTypeService.init(version));

    CompletableFuture<Void> futureTargets =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(
                      futureLandingZoneFunctionalTypes,
                      futureProviders,
                      futureTargetTypes,
                      futureMacroPhase,
                      futureContinents)
                  .join();
              targetService.init(version);
            });

    CompletableFuture<Void> futureFirewallVendors =
        managedExecutor.runAsync(() -> firewallVendorService.init(version));

    CompletableFuture<Void> futureFirewallGroups =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureFirewallVendors, futureTargets).join();
              firewallGroupService.init(version);
            });

    CompletableFuture<Void> futureLandingZoneTypes =
        managedExecutor.runAsync(() -> landingZoneTypeService.init(version));
    CompletableFuture<Void> futureZoneTypes =
        managedExecutor.runAsync(() -> zoneTypeService.init(version));

    CompletableFuture<Void> futureConnectionPhases =
        managedExecutor.runAsync(
            () -> {
              futureMacroPhase.join();
              connectionPhaseService.init(version);
            });

    CompletableFuture<Void> futureConnections =
        managedExecutor.runAsync(
            () -> {
              futureZoneTypes.join();
              connectionService.init(version);
            });

    CompletableFuture<Void> futureApplicationIds =
        managedExecutor.runAsync(() -> applicationIdService.init(version));

    CompletableFuture<Void> futureLandingZones =
        managedExecutor.runAsync(
            () -> {
              // We need to wait for all the processes that create items in
              // LandingZoneFunctionalTypesRepository
              CompletableFuture.allOf(
                      futureTargets,
                      futureRegions,
                      futureContinents,
                      futureLandingZoneTypes,
                      futureMacroPhase,
                      futureLandingZoneFunctionalTypes)
                  .join();
              landingZoneService.init(version);
            });

    CompletableFuture<Void> futureZoneInstances =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureLandingZones, futureZoneTypes, futureFirewallGroups)
                  .join();
              zoneInstanceService.init(version);
            });

    CompletableFuture<Void> futureSubnetInstances =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureLandingZones, futureSubnetTypes, futureZoneInstances)
                  .join();
              subnetInstanceService.init(version);
            });

    CompletableFuture<Void> futureComponents =
        managedExecutor.runAsync(() -> componentService.init(version));

    CompletableFuture<Void> futureComponentPlacements =
        managedExecutor.runAsync(
            () -> CompletableFuture.allOf(futureZoneTypes, futureComponents).join());

    CompletableFuture<Void> futureConnectionApprovalComponentSkips =
        managedExecutor.runAsync(() -> connectionApprovalComponentSkipService.init(version));

    CompletableFuture<Void> futureConnectionApprovalScopeSkips =
        managedExecutor.runAsync(() -> connectionApprovalScopeSkipService.init(version));

    CompletableFuture<Void> futureServices =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureApplicationIds, futureBlockedPorts, futureComponents)
                  .join();
              serviceService.init(version);
            });

    CompletableFuture<Void> futureConnectionApprovals =
        managedExecutor.runAsync(
            () ->
                CompletableFuture.allOf(
                        futureConnectionPhases, futureConnections, futureServices, futureComponents)
                    .join());

    CompletableFuture<Void> futureDependencies =
        managedExecutor.runAsync(
            () -> {
              futureComponents.join();
              dependencyService.init(version);
            });

    CompletableFuture<Void> futureComponentInstances =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(
                      futureLandingZones,
                      futureComponents,
                      futureApplicationPhases,
                      futureServiceTags,
                      futureZoneTypes,
                      futureSubnetInstances,
                      futureZoneInstances,
                      futureNamespaces)
                  .join();
              componentInstanceService.init(version);
            });

    CompletableFuture<Void> futureIPAddressInstances =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureLandingZones, futureComponentInstances).join();
              ipAddressInstanceService.init(version);
            });

    CompletableFuture<Void> futureDependencyInstances =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(
                      futureLandingZones,
                      futureComponentInstances,
                      futureDependencies,
                      futureServices)
                  .join();
              dependencyInstanceService.init(version);
            });

    futureServices.join();

    CompletableFuture<Void> futureOutboundSNATs =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(
                      futureMacroPhase,
                      futureContinents,
                      futureZoneTypes,
                      futureComponents,
                      futureComponentInstances)
                  .join();
              outboundSNATService.init(version);
            });

    CompletableFuture<Void> futureComponentInstancesInternals =
        managedExecutor.runAsync(
            () -> {
              futureIPAddressInstances.join();
              componentInstanceService.initInternals(version);
            });

    CompletableFuture<Void> futureLandingZoneInstanceInternals =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureLandingZones, futureSubnetInstances).join();
              landingZoneService.initInternals(version);
            });

    CompletableFuture<Void> futureTargetServiceInternals =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureTargets, futureLandingZoneInstanceInternals).join();
              targetService.initInternals(version);
            });

    CompletableFuture<Void> futureZoneInstanceInternals =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureZoneInstances, futureSubnetInstances).join();
              zoneInstanceService.initInternals(version);
            });

    CompletableFuture<Void> futureAccessControls =
        managedExecutor.runAsync(
            () -> {
              CompletableFuture.allOf(futureComponents, futureDependencies).join();
              accessControlService.init(version);
            });

    CompletableFuture.allOf(
            futureScopeReleaseNotes,
            futureScopeVersionMappings,
            futureRoles,
            futureBlockedPorts,
            futureBlacklists,
            futureBlackListedURLs,
            futureBlackListedCIDRs,
            futureApplicationPhases,
            futureSubnetTypes,
            futureNamespaces,
            futureServiceTags,
            futureRegions,
            futureTargetTypes,
            futureTargets,
            futureFirewallVendors,
            futureFirewallGroups,
            futureLandingZoneTypes,
            futureLandingZoneFunctionalTypes,
            futureZoneTypes,
            futureConnectionPhases,
            futureConnections,
            futureConnectionApprovals,
            futureApplicationIds,
            futureLandingZones,
            futureZoneInstances,
            futureSubnetInstances,
            futureComponents,
            futureComponentPlacements,
            futureConnectionApprovalComponentSkips,
            futureConnectionApprovalScopeSkips,
            futureServices,
            futureDependencies,
            futureComponentInstances,
            futureIPAddressInstances,
            futureDependencyInstances,
            futureOutboundSNATs,
            futureComponentInstancesInternals,
            futureTargetServiceInternals,
            futureZoneInstanceInternals,
            futureLandingZoneInstanceInternals,
            futureAccessControls)
        .orTimeout(10, TimeUnit.MINUTES)
        .join();
    // NOTHING after this point should be added
  }
}
