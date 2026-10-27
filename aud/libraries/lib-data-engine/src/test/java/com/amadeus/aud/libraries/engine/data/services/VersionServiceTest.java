package com.amadeus.aud.libraries.engine.data.services;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestGraph.createVersionService;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestGraph.unusedDataProcessor;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestGraph.unusedRequirementsProcessor;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.modelConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.AudDataLocalFileService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

class VersionServiceTest {
  private static final String VERSION_NUMBER = "1.0.1";
  private static final String AZURE_LANDING_ZONE = "azure-west-europe-spoke-1-net";

  @Test
  void buildVersionFromNetworkFixtureInitializesRealServicesAndRelationships() throws IOException {
    AudDataRepository dataRepository = loadNetworkData();
    AtomicBoolean scopesInstalled = new AtomicBoolean();
    AtomicReference<Boolean> associatedWithRequirements = new AtomicReference<>();
    AtomicReference<Version> postInitializedVersion = new AtomicReference<>();
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    ManagedExecutor managedExecutor = synchronousManagedExecutor();
    VersionCacheProducer versionCacheProducer = mock(VersionCacheProducer.class);
    LoadingCache<String, Version> versionCache =
        com.github.benmanes.caffeine.cache.Caffeine.<String, Version>newBuilder()
            .build((String ignored) -> postInitializedVersion.get());
    when(versionCacheProducer.getCache()).thenReturn(versionCache);
    when(audVersionsService.checkNewVersion(VERSION_NUMBER))
        .thenReturn(Uni.createFrom().item("1.0.2"));
    VersionPreInitHook preInitHook =
        scopeInstallingHook(dataRepository, scopesInstalled, associatedWithRequirements);
    VersionService sut =
        createVersionService(
            preInitHook,
            postInitializedVersion::set,
            unusedRequirementsProcessor(),
            unusedDataProcessor(),
            audVersionsService,
            managedExecutor,
            versionCacheProducer);
    sut.init();

    Version version = sut.buildVersion(VERSION_NUMBER, dataRepository);
    Version currentVersion = sut.getCurrentVersion().await().atMost(Duration.ofSeconds(1));
    String newVersion = sut.checkForNewVersion().await().atMost(Duration.ofSeconds(1));

    assertThat(dataRepository.getData())
        .containsKeys(
            V1Component.class,
            V1ComponentInstance.class,
            V1Dependency.class,
            V1DependencyInstance.class,
            V1LandingZone.class,
            V1SubnetInstance.class,
            V1ZoneInstance.class,
            V1ScopeVersionMapping.class,
            V1ScopeReleaseNote.class);
    assertThat(dataRepository.getData().get(V1Component.class))
        .hasSize(9)
        .allSatisfy(model -> assertThat(model).isExactlyInstanceOf(V1Component.class));
    assertThat(version.getVersionNumber()).isEqualTo(VERSION_NUMBER);
    assertThat(version.getAudDataRepository()).isSameAs(dataRepository);
    assertThat(version.getRepositorySizes())
        .containsEntry("component", 9L)
        .containsEntry("componentinstance", 6L)
        .containsEntry("dependency", 4L)
        .containsEntry("dependencyinstance", 4L)
        .containsEntry("landingzone", 5L)
        .containsEntry("namespace", 3L)
        .containsEntry("provider", 4L)
        .containsEntry("region", 19L)
        .containsEntry("scope", 8L)
        .containsEntry("scopereleasenote", 5L)
        .containsEntry("scopeversionmapping", 5L)
        .containsEntry("subnetinstance", 17L)
        .containsEntry("target", 4L)
        .containsEntry("zoneinstance", 16L);
    assertThat(version.getApplicationIdsRepository()).isNotNull();
    assertThat(version.getAutomaticTemplateInstancesRepository()).isNotNull();
    assertThat(version.getBlacklistsRepository()).isNotNull();
    assertThat(version.getConnectionsRepository()).isNotNull();
    assertThat(version.getFirewallGroupsRepository()).isNotNull();
    assertThat(version.getIpAddressInstancesRepository()).isNotNull();
    assertThat(version.getOutboundSNATsRepository()).isNotNull();

    Scope applicationScope = version.getScopesRepository().get("Application-1");
    Scope networkScope = version.getScopesRepository().get("Network_Azure");
    LandingZone landingZone = version.getLandingZonesRepository().get(AZURE_LANDING_ZONE);
    Component backendComponent = version.getComponentsRepository().get("Backend API");
    Component storageComponent = version.getComponentsRepository().get("Storage Account");
    ComponentInstance backendInstance =
        version.getComponentInstancesRepository().get("Backend API TST");
    ComponentInstance storageInstance =
        version.getComponentInstancesRepository().get("Storage Account TST");
    String subnetId = String.join(BaseInstance.ID_SEPARATOR, AZURE_LANDING_ZONE, "app_comp1");
    SubnetInstance subnetInstance = version.getSubnetInstancesRepository().get(subnetId);
    ZoneInstance zoneInstance = version.getZoneInstancesRepository().get(subnetId);
    Dependency dependency =
        version.getDependenciesRepository().get("Backend API to Storage Account");
    String dependencyInstanceId =
        String.join(
            BaseInstance.ID_SEPARATOR,
            AZURE_LANDING_ZONE,
            "Backend API TST to Storage Account TST");
    DependencyInstance dependencyInstance =
        version.getDependencyInstancesRepository().get(dependencyInstanceId);
    Target target = version.getTargetsRepository().get("azure-west-europe");

    assertThat(applicationScope).isNotNull();
    assertThat(networkScope).isNotNull();
    assertThat(landingZone).isNotNull();
    assertThat(landingZone.getScope()).isSameAs(networkScope);
    assertThat(networkScope.getLandingZones()).contains(landingZone);
    assertThat(storageInstance).isNotNull();
    assertThat(storageInstance.getScope()).isSameAs(applicationScope);
    assertThat(storageInstance.getComponent()).isSameAs(storageComponent);
    assertThat(storageInstance.getLandingZone()).isSameAs(landingZone);
    assertThat(storageInstance.getInSubnetInstance()).isSameAs(subnetInstance);
    assertThat(applicationScope.getComponentInstances()).contains(storageInstance);
    assertThat(landingZone.getComponentInstances()).contains(storageInstance);
    assertThat(subnetInstance.getZoneInstance()).isSameAs(zoneInstance);
    assertThat(zoneInstance.getLandingZone()).isSameAs(landingZone);
    assertThat(dependency.getSourceComponent()).isSameAs(backendComponent);
    assertThat(dependency.getDestinationComponent()).isSameAs(storageComponent);
    assertThat(dependency.getScope()).isSameAs(applicationScope);
    assertThat(dependencyInstance.getDependency()).isSameAs(dependency);
    assertThat(dependencyInstance.getLandingZone()).isSameAs(landingZone);
    assertThat(dependencyInstance.getComponentInstance()).isSameAs(backendInstance);
    assertThat(dependencyInstance.getTargetComponentInstance()).isSameAs(storageInstance);
    assertThat(dependencyInstance.getServices()).isNotEmpty();
    assertThat(dependency.getDependencyInstances()).contains(dependencyInstance);
    assertThat(landingZone.getTarget()).isSameAs(target);
    assertThat(landingZone.getRegion()).isSameAs(version.getRegionsRepository().get("West Europe"));
    assertThat(target.getProvider())
        .isSameAs(version.getProvidersRepository().get("Microsoft - Azure"));
    assertThat(scopesInstalled).isTrue();
    assertThat(associatedWithRequirements).hasValue(false);
    assertThat(postInitializedVersion).hasValue(version);
    assertThat(currentVersion).isSameAs(version);
    assertThat(newVersion).isEqualTo("1.0.2");
    verify(versionCacheProducer).setLoader(any());
    verify(audVersionsService).checkNewVersion(VERSION_NUMBER);
  }

  @Test
  void checkForNewVersionWithoutCurrentVersionUsesLatestVersionCheck() {
    AudVersionsService audVersionsService = mock(AudVersionsService.class);
    when(audVersionsService.checkNewVersion()).thenReturn(Uni.createFrom().item("1.0.1"));
    VersionService sut =
        createVersionService(
            (Version version, boolean associateWithRequirements) -> Uni.createFrom().voidItem(),
            (Version version) -> {},
            unusedRequirementsProcessor(),
            unusedDataProcessor(),
            audVersionsService,
            synchronousManagedExecutor(),
            mock(VersionCacheProducer.class));

    String newVersion = sut.checkForNewVersion().await().atMost(Duration.ofSeconds(1));

    assertThat(newVersion).isEqualTo("1.0.1");
    verify(audVersionsService).checkNewVersion();
  }

  private static AudDataRepository loadNetworkData() throws IOException {
    AudDataLocalFileService fileService =
        new AudDataLocalFileService(mock(LocalFileService.class), modelConfig());
    String[] contents = {
      readFixture("api.yml"), readFixture("metadata.yml"), readFixture("release.yml")
    };
    return fileService.processResponses(VERSION_NUMBER, contents, false);
  }

  private static String readFixture(String fileName) throws IOException {
    String resourceName = "/network-data/" + fileName;
    try (InputStream inputStream = VersionServiceTest.class.getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        throw new IOException("Missing test fixture " + resourceName);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static VersionPreInitHook scopeInstallingHook(
      AudDataRepository dataRepository,
      AtomicBoolean scopesInstalled,
      AtomicReference<Boolean> associatedWithRequirements) {
    return (Version version, boolean shouldAssociateWithRequirements) ->
        Uni.createFrom()
            .voidItem()
            .invoke(
                () -> {
                  ScopesRepository scopesRepository = new ScopesRepository();
                  scopesRepository.autoCreateScope();
                  for (List<? extends ModelElement> models : dataRepository.getData().values()) {
                    for (ModelElement model : models) {
                      String scopeName = model.getMetadata().getScope();
                      if (scopeName != null && !scopeName.isBlank()) {
                        scopesRepository.get(scopeName);
                      }
                    }
                  }
                  scopesRepository.get("Governance");
                  version.setScopesRepository(scopesRepository);
                  associatedWithRequirements.set(shouldAssociateWithRequirements);
                  scopesInstalled.set(true);
                });
  }

  private static ManagedExecutor synchronousManagedExecutor() {
    ManagedExecutor managedExecutor = mock(ManagedExecutor.class);
    when(managedExecutor.runAsync(any(Runnable.class)))
        .thenAnswer(
            (InvocationOnMock invocation) -> {
              Runnable runnable = invocation.getArgument(0, Runnable.class);
              runnable.run();
              return CompletableFuture.completedFuture(null);
            });
    return managedExecutor;
  }
}
