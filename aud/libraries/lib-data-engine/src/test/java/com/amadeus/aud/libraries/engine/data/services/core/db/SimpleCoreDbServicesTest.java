package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.ScopeVersionMapping;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import com.amadeus.aud.libraries.yaml.models.V1BlacklistSpec;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalComponentSkipSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkipSpec;
import com.amadeus.aud.libraries.yaml.models.V1Continent;
import com.amadeus.aud.libraries.yaml.models.V1FirewallVendor;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneFunctionalType;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneType;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhase;
import com.amadeus.aud.libraries.yaml.models.V1Provider;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNoteSpec;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMappingSpec;
import com.amadeus.aud.libraries.yaml.models.V1Servicetag;
import com.amadeus.aud.libraries.yaml.models.V1SubnetType;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneTypeSpec;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SimpleCoreDbServicesTest {
  @Nested
  class CatalogCallbackTests {
    @Test
    void simpleCatalogServicesStoreKnownScopeEntitiesAndRetainOrphans() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.addData(
          V1ApplicationID.class,
          List.of(
              new V1ApplicationID().metadata(metadata("application-id", fixture.scope.getName())),
              new V1ApplicationID().metadata(metadata("orphan-app-id", "missing-scope"))));
      fixture.addData(
          V1BlockedPort.class,
          List.of(
              blockedPort("blocked-port", fixture.scope.getName()),
              blockedPort("orphan-blocked-port", "missing-scope")));
      fixture.addData(
          V1Blacklist.class,
          List.of(
              blacklist("blacklist", fixture.scope.getName()),
              blacklist("orphan-blacklist", "missing-scope")));
      fixture.addData(
          V1BlackListedURL.class,
          List.of(
              new V1BlackListedURL().metadata(metadata("blocked-url", fixture.scope.getName())),
              new V1BlackListedURL().metadata(metadata("orphan-url", "missing-scope"))));
      fixture.addData(
          V1BlackListedCIDR.class,
          List.of(
              new V1BlackListedCIDR().metadata(metadata("blocked-cidr", fixture.scope.getName())),
              new V1BlackListedCIDR().metadata(metadata("orphan-cidr", "missing-scope"))));
      fixture.addData(
          V1FirewallVendor.class,
          List.of(
              new V1FirewallVendor().metadata(metadata("vendor", fixture.scope.getName())),
              new V1FirewallVendor().metadata(metadata("orphan-vendor", "missing-scope"))));
      fixture.addData(
          V1Servicetag.class,
          List.of(
              new V1Servicetag().metadata(metadata("service-tag", fixture.scope.getName())),
              new V1Servicetag().metadata(metadata("orphan-tag", "missing-scope"))));

      // Act
      configure(new ApplicationIdService()).init(fixture.version);
      configure(new BlockedPortService()).init(fixture.version);
      configure(new BlacklistService()).init(fixture.version);
      configure(new BlackListedURLService()).init(fixture.version);
      configure(new BlackListedCIDRService()).init(fixture.version);
      configure(new FirewallVendorService()).init(fixture.version);
      configure(new ServiceTagService()).init(fixture.version);

      // Assert
      ApplicationId applicationId =
          fixture.version.getApplicationIdsRepository().get("application-id");
      BlockedPort blockedPort = fixture.version.getBlockedPortsRepository().get("blocked-port");
      Blacklist blacklist = fixture.version.getBlacklistsRepository().get("blacklist");
      BlackListedURL blockedUrl = fixture.version.getBlackListedURLsRepository().get("blocked-url");
      BlackListedCIDR blockedCidr =
          fixture.version.getBlackListedCIDRsRepository().get("blocked-cidr");
      FirewallVendor vendor = fixture.version.getFirewallVendorsRepository().get("vendor");
      ServiceTag serviceTag = fixture.version.getServiceTagsRepository().get("service-tag");
      assertThat(fixture.scope.getApplicationIds()).contains(applicationId);
      assertThat(fixture.scope.getBlockedPorts()).contains(blockedPort);
      assertThat(fixture.scope.getBlacklists()).contains(blacklist);
      assertThat(fixture.scope.getBlackListedURLs()).contains(blockedUrl);
      assertThat(fixture.scope.getBlackListedCIDRs()).contains(blockedCidr);
      assertThat(fixture.scope.getFirewallVendors()).contains(vendor);
      assertThat(fixture.scope.getServiceTags()).contains(serviceTag);
      assertThat(fixture.version.getApplicationIdsRepository().get("orphan-app-id").getScope())
          .isNull();
      assertThat(fixture.version.getBlockedPortsRepository().get("orphan-blocked-port").getScope())
          .isNull();
      assertThat(fixture.version.getBlacklistsRepository().get("orphan-blacklist").getScope())
          .isNull();
      assertThat(fixture.version.getBlackListedURLsRepository().get("orphan-url").getScope())
          .isNull();
      assertThat(fixture.version.getBlackListedCIDRsRepository().get("orphan-cidr").getScope())
          .isNull();
      assertThat(fixture.version.getFirewallVendorsRepository().get("orphan-vendor").getScope())
          .isNull();
      assertThat(fixture.version.getServiceTagsRepository().get("orphan-tag").getScope()).isNull();
    }

    @Test
    void typeServicesStoreKnownScopeEntitiesAndRetainOrphans() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.addData(
          V1ZoneType.class,
          List.of(
              zoneType("zone-type", fixture.scope.getName()),
              zoneType("orphan-zone-type", "missing-scope")));
      fixture.addData(
          V1TargetType.class,
          List.of(
              new V1TargetType().metadata(metadata("target-type", fixture.scope.getName())),
              new V1TargetType().metadata(metadata("orphan-target-type", "missing-scope"))));
      fixture.addData(
          V1SubnetType.class,
          List.of(
              new V1SubnetType().metadata(metadata("subnet-type", fixture.scope.getName())),
              new V1SubnetType().metadata(metadata("orphan-subnet-type", "missing-scope"))));
      fixture.addData(
          V1LandingzoneType.class,
          List.of(
              new V1LandingzoneType()
                  .metadata(metadata("landing-zone-type", fixture.scope.getName())),
              new V1LandingzoneType()
                  .metadata(metadata("orphan-landing-zone-type", "missing-scope"))));

      // Act
      configure(new ZoneTypeService()).init(fixture.version);
      configure(new TargetTypeService()).init(fixture.version);
      configure(new SubnetTypeService()).init(fixture.version);
      configure(new LandingZoneTypeService()).init(fixture.version);

      // Assert
      ZoneType zoneType = fixture.version.getZoneTypesRepository().get("zone-type");
      TargetType targetType = fixture.version.getTargetTypesRepository().get("target-type");
      SubnetType subnetType = fixture.version.getSubnetTypesRepository().get("subnet-type");
      LandingZoneType landingZoneType =
          fixture.version.getLandingZoneTypesRepository().get("landing-zone-type");
      assertThat(fixture.scope.getZoneTypes()).contains(zoneType);
      assertThat(fixture.scope.getTargetTypes()).contains(targetType);
      assertThat(fixture.scope.getSubnetTypes()).contains(subnetType);
      assertThat(fixture.scope.getLandingZoneTypes()).contains(landingZoneType);
      assertThat(fixture.version.getZoneTypesRepository().get("orphan-zone-type").getScope())
          .isNull();
      assertThat(fixture.version.getTargetTypesRepository().get("orphan-target-type").getScope())
          .isNull();
      assertThat(fixture.version.getSubnetTypesRepository().get("orphan-subnet-type").getScope())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getLandingZoneTypesRepository()
                  .get("orphan-landing-zone-type")
                  .getScope())
          .isNull();
    }

    @Test
    void approvalAndVersionMetadataServicesLinkKnownScopesAndHandleMissingScopes() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.addData(
          V1ConnectionApprovalComponentSkip.class,
          List.of(
              componentSkip("component-skip", fixture.scope.getName()),
              componentSkip("orphan-component-skip", "missing-scope")));
      fixture.addData(
          V1ConnectionApprovalScopeSkip.class,
          List.of(
              scopeSkip("scope-skip", fixture.scope.getName()),
              scopeSkip("orphan-scope-skip", "missing-scope")));
      fixture.addData(
          V1ScopeReleaseNote.class,
          List.of(
              releaseNote("release", fixture.scope.getName(), "1.0"),
              releaseNote("orphan-release", "missing-scope", "1.1")));
      fixture.addData(
          V1ScopeVersionMapping.class,
          List.of(
              versionMapping("mapping", fixture.scope.getName()),
              versionMapping("orphan-mapping", "missing-scope")));

      // Act
      configure(new ConnectionApprovalComponentSkipService()).init(fixture.version);
      configure(new ConnectionApprovalScopeSkipService()).init(fixture.version);
      configure(new ScopeReleaseNoteService()).init(fixture.version);
      configure(new ScopeVersionMappingService()).init(fixture.version);

      // Assert
      ConnectionApprovalComponentSkip componentSkip =
          fixture.version.getConnectionApprovalComponentSkipsRepository().get("component-skip");
      ConnectionApprovalScopeSkip scopeSkip =
          fixture.version.getConnectionApprovalScopeSkipsRepository().get("scope-skip");
      ScopeReleaseNote releaseNote =
          fixture
              .version
              .getScopeReleaseNotesRepository()
              .get(String.join(BaseInstance.ID_SEPARATOR, "release", "1.0"));
      ScopeVersionMapping mapping =
          fixture.version.getScopeVersionMappingsRepository().get("mapping");
      assertThat(componentSkip.getStatus()).isEqualTo("OK");
      assertThat(scopeSkip.getStatus()).isEqualTo("OK");
      assertThat(fixture.scope.getConnectionApprovalComponentSkips()).contains(componentSkip);
      assertThat(fixture.scope.getConnectionApprovalScopeSkips()).contains(scopeSkip);
      assertThat(fixture.scope.getScopeReleaseNotes()).contains(releaseNote);
      assertThat(fixture.scope.getScopeVersionMapping()).contains(mapping);
      assertThat(
              fixture
                  .version
                  .getConnectionApprovalComponentSkipsRepository()
                  .get("orphan-component-skip")
                  .getScope())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getConnectionApprovalScopeSkipsRepository()
                  .get("orphan-scope-skip")
                  .getScope())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getScopeReleaseNotesRepository()
                  .get(String.join(BaseInstance.ID_SEPARATOR, "orphan-release", "1.1"))
                  .getScope())
          .isNull();
      assertThat(
              fixture.version.getScopeVersionMappingsRepository().get("orphan-mapping").getScope())
          .isNull();
    }
  }

  @Nested
  class GovernanceCatalogTests {
    @Test
    void providerServiceCoversMissingPrerequisitesAndGovernanceCreation() {
      // Arrange
      ProviderService sut = configure(new ProviderService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getProvidersRepository()).isNull();
      version.setScopesRepository(new ScopesRepository());
      sut.init(version);
      assertThat(version.getProvidersRepository()).isNull();

      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setLandingZoneFunctionalTypesRepository(
          new LandingZoneFunctionalTypesRepository());
      fixture.addData(
          V1Provider.class,
          List.of(
              new V1Provider().metadata(metadata("provider", fixture.scope.getName())),
              new V1Provider().metadata(metadata("orphan-provider", "missing-scope"))));

      sut.init(fixture.version);

      Provider provider = fixture.version.getProvidersRepository().get("provider");
      assertThat(fixture.scope.getProviders()).contains(provider);
      assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("provider"))
          .isNotNull();
      assertThat(fixture.version.getProvidersRepository().get("orphan-provider").getScope())
          .isNull();
    }

    @Test
    void macroPhaseServiceCoversMissingPrerequisitesAndGovernanceCreation() {
      // Arrange
      MacroPhaseService sut = configure(new MacroPhaseService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getMacroPhasesRepository()).isNull();
      version.setScopesRepository(new ScopesRepository());
      sut.init(version);
      assertThat(version.getMacroPhasesRepository()).isNull();

      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setLandingZoneFunctionalTypesRepository(
          new LandingZoneFunctionalTypesRepository());
      fixture.addData(
          V1MacroPhase.class,
          List.of(
              new V1MacroPhase().metadata(metadata("macro-phase", fixture.scope.getName())),
              new V1MacroPhase().metadata(metadata("orphan-phase", "missing-scope"))));

      sut.init(fixture.version);

      MacroPhase macroPhase = fixture.version.getMacroPhasesRepository().get("macro-phase");
      assertThat(fixture.scope.getMacroPhases()).contains(macroPhase);
      assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("macro-phase"))
          .isNotNull();
      assertThat(fixture.version.getMacroPhasesRepository().get("orphan-phase").getScope())
          .isNull();
    }

    @Test
    void continentServiceCoversMissingPrerequisitesAndGovernanceCreation() {
      // Arrange
      ContinentService sut = configure(new ContinentService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getContinentsRepository()).isNull();
      version.setScopesRepository(new ScopesRepository());
      sut.init(version);
      assertThat(version.getContinentsRepository()).isNull();

      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setLandingZoneFunctionalTypesRepository(
          new LandingZoneFunctionalTypesRepository());
      fixture.addData(
          V1Continent.class,
          List.of(
              new V1Continent().metadata(metadata("continent", fixture.scope.getName())),
              new V1Continent().metadata(metadata("orphan-continent", "missing-scope"))));

      sut.init(fixture.version);

      Continent continent = fixture.version.getContinentsRepository().get("continent");
      assertThat(fixture.scope.getContinents()).contains(continent);
      assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("continent"))
          .isNotNull();
      assertThat(fixture.version.getContinentsRepository().get("orphan-continent").getScope())
          .isNull();
    }

    @Test
    void landingZoneFunctionalTypeServiceHandlesPrerequisitesGenerationAndMissingGlobalScope() {
      // Arrange
      LandingZoneFunctionalTypeService sut = configure(new LandingZoneFunctionalTypeService());
      Version missingScopes = new Version();
      Version missingData = new Version();
      missingData.setScopesRepository(new ScopesRepository());

      // Act and assert
      sut.init(missingScopes);
      sut.init(missingData);
      assertThat(missingScopes.getLandingZoneFunctionalTypesRepository()).isNull();
      assertThat(missingData.getLandingZoneFunctionalTypesRepository()).isNull();

      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.addData(
          V1LandingzoneFunctionalType.class,
          List.of(
              new V1LandingzoneFunctionalType()
                  .metadata(metadata("functional-type", fixture.scope.getName())),
              new V1LandingzoneFunctionalType()
                  .metadata(metadata("orphan-functional-type", "missing-scope"))));
      sut.init(fixture.version);
      LandingZoneFunctionalType functionalType =
          fixture.version.getLandingZoneFunctionalTypesRepository().get("functional-type");
      assertThat(fixture.scope.getLandingZoneFunctionalTypes()).contains(functionalType);
      assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("Cloud"))
          .isNotNull();
      assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("Legacy"))
          .isNotNull();
      assertThat(
              fixture
                  .version
                  .getLandingZoneFunctionalTypesRepository()
                  .get("orphan-functional-type")
                  .getScope())
          .isNull();

      Version missingGlobal = new Version();
      ScopesRepository scopesRepository = new ScopesRepository();
      missingGlobal.setScopesRepository(scopesRepository);
      missingGlobal.setAudDataRepository(new AudDataRepository());
      assertThatThrownBy(() -> sut.init(missingGlobal))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("GlobalDefinitions scope not found");
    }
  }

  private static V1BlockedPort blockedPort(String name, String scopeName) {
    return new V1BlockedPort()
        .metadata(metadata(name, scopeName))
        .spec(new V1BlockedPortSpec().port(25).protocol(V1BlockedPortSpec.ProtocolEnum.TCP));
  }

  private static V1Blacklist blacklist(String name, String scopeName) {
    V1BlacklistSpec spec =
        new V1BlacklistSpec()
            .sourceComponent("*")
            .sourceZones(List.of("*"))
            .sourceMacrophases(List.of("*"))
            .sourceTargets(List.of("*"))
            .destinationPortRanges(List.of("*"));
    return new V1Blacklist().metadata(metadata(name, scopeName)).spec(spec);
  }

  private static V1ZoneType zoneType(String name, String scopeName) {
    return new V1ZoneType()
        .metadata(metadata(name, scopeName))
        .spec(new V1ZoneTypeSpec().isTechnical(false));
  }

  private static V1ConnectionApprovalComponentSkip componentSkip(String name, String scopeName) {
    return new V1ConnectionApprovalComponentSkip()
        .metadata(metadata(name, scopeName))
        .spec(new V1ConnectionApprovalComponentSkipSpec().source(true).destination(false));
  }

  private static V1ConnectionApprovalScopeSkip scopeSkip(String name, String scopeName) {
    return new V1ConnectionApprovalScopeSkip()
        .metadata(metadata(name, scopeName))
        .spec(new V1ConnectionApprovalScopeSkipSpec().source(false).destination(true));
  }

  private static V1ScopeReleaseNote releaseNote(
      String name, String scopeName, String scopeVersion) {
    return new V1ScopeReleaseNote()
        .metadata(metadata(name, scopeName))
        .spec(new V1ScopeReleaseNoteSpec().scopeVersion(scopeVersion));
  }

  private static V1ScopeVersionMapping versionMapping(String name, String scopeName) {
    return new V1ScopeVersionMapping()
        .metadata(metadata(name, scopeName))
        .spec(new V1ScopeVersionMappingSpec().scopeVersion("1.0").audVersion("2.0"));
  }
}
