package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;

import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.VersionPostInitHook;
import com.amadeus.aud.libraries.engine.data.services.VersionPreInitHook;
import com.amadeus.aud.libraries.engine.data.services.VersionService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.DataProcessor;
import org.eclipse.microprofile.context.ManagedExecutor;

public final class CoreDbServiceTestGraph {
  private CoreDbServiceTestGraph() {}

  public static VersionService createVersionService(
      VersionPreInitHook versionPreInitHook,
      VersionPostInitHook versionPostInitHook,
      DataProcessor<AudRequirementsRepository> audRequirementsService,
      DataProcessor<AudDataRepository> audDataService,
      AudVersionsService audVersionsService,
      ManagedExecutor managedExecutor,
      VersionCacheProducer versionCacheProducer) {
    AutomaticTemplateInstanceService automaticTemplateInstanceService =
        configure(new AutomaticTemplateInstanceService());
    ComponentInstanceService componentInstanceService =
        configure(
            new ComponentInstanceService(
                automaticTemplateInstanceService, new EmptyComponentInstanceLogic()));

    return new VersionService(
        versionPreInitHook,
        versionPostInitHook,
        audRequirementsService,
        audDataService,
        audVersionsService,
        managedExecutor,
        versionCacheProducer,
        configure(new ApplicationIdService()),
        configure(new ApplicationPhaseService()),
        configure(new BlockedPortService()),
        configure(new BlacklistService()),
        configure(new BlackListedURLService()),
        configure(new BlackListedCIDRService()),
        componentInstanceService,
        configure(new ComponentService()),
        configure(new ConnectionApprovalComponentSkipService()),
        configure(new ConnectionApprovalScopeSkipService()),
        configure(new ConnectionPhaseService()),
        configure(new ConnectionService()),
        configure(new ContinentService()),
        configure(new DependencyInstanceService()),
        configure(new DependencyService()),
        configure(new FirewallGroupService()),
        configure(new FirewallVendorService()),
        configure(new IPAddressInstanceService()),
        configure(new LandingZoneFunctionalTypeService()),
        configure(new LandingZoneService()),
        configure(new LandingZoneTypeService()),
        configure(new MacroPhaseService()),
        configure(new NamespaceService()),
        configure(new OutboundSNATService()),
        configure(new ProviderService()),
        configure(new RegionService()),
        configure(new RoleService()),
        configure(new ScopeReleaseNoteService()),
        configure(new ScopeVersionMappingService()),
        configure(new ServiceService()),
        configure(new ServiceTagService()),
        configure(new SubnetInstanceService()),
        configure(new SubnetTypeService()),
        configure(new TargetService()),
        configure(new TargetTypeService()),
        configure(new ZoneInstanceService()),
        configure(new ZoneTypeService()),
        configure(new AccessControlService()));
  }

  public static DataProcessor<AudRequirementsRepository> unusedRequirementsProcessor() {
    return new DataProcessor<>() {};
  }

  public static DataProcessor<AudDataRepository> unusedDataProcessor() {
    return new DataProcessor<>() {};
  }
}
