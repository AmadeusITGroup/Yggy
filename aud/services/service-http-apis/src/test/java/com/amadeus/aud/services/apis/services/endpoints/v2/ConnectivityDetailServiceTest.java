package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.common.AudCommonConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
class ConnectivityDetailServiceTest {

  @Mock private AudService audService;
  @Mock private GraphEngineService graphEngineService;
  @Mock private AudCommonConfig audCommonConfig;

  @Test
  void getConnectivityDetailResponse_returnsFilteredPagedItems() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));
    when(audCommonConfig.pciPhases()).thenReturn(List.of("PRD", "PCI-1", "PCI-2"));

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target-a");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target-a")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance sourceApproved =
        pciSourceComponentInstance("src-a", "target-a", "PRD", "cde");
    when(sourceApproved.getInternalEndpoints(Direction.OUT))
        .thenReturn(Set.of(endpoint("10.0.0.0/24", "src-a-out-subnet")));
    ComponentInstance targetApproved = componentInstanceWithTarget("dst-a", "target-a");
    when(targetApproved.getInternalEndpoints(Direction.IN))
        .thenReturn(Set.of(endpoint("10.0.1.0/24", "dst-a-in-subnet")));
    DependencyInstance dependencyApproved = dependencyInstance("dep-a");
    Service serviceApproved = service("svc-a-id");

    ComponentInstance sourceRejected = targetComponentInstanceWithId("src-b-id", "target-a");
    ComponentInstance targetRejected = targetComponentInstanceWithId("dst-b-id", "target-a");
    DependencyInstance dependencyRejected = dependencyInstance("dep-b");
    Service serviceRejected = serviceId("svc-b-id");

    ComponentInstance sourceOtherTarget = targetComponentInstanceWithId("src-c-id", "target-b");
    ComponentInstance targetOtherTarget = targetComponentInstanceWithId("dst-c-id", "target-b");
    DependencyInstance dependencyOtherTarget = dependencyInstanceWithId("dep-c-id");
    Service serviceOtherTarget = serviceId("svc-c-id");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target-a",
        new EdgeFixture(sourceApproved, targetApproved, dependencyApproved, serviceApproved),
        new EdgeFixture(sourceRejected, targetRejected, dependencyRejected, serviceRejected),
        new EdgeFixture(
            sourceOtherTarget, targetOtherTarget, dependencyOtherTarget, serviceOtherTarget));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse("latest", "targets", "target-a", null, 1, 1)
            .await()
            .indefinitely();

    assertThat(response.metadata()).isNotNull();
    assertThat(response.metadata().pagination()).isNotNull();
    assertThat(response.metadata().pagination().count()).isEqualTo(2);
    assertThat(response.metadata().pagination().pageNum()).isOne();
    assertThat(response.metadata().pagination().pageSize()).isOne();
    assertThat(response.metadata().initialColumns())
        .containsExactly("name", "source", "target", "dependency_instance", "service", "pci");

    assertThat(response.data()).hasSize(1);

    ConnectivityDetailItem firstItem = response.data().getFirst();
    assertThat(firstItem.pci()).isTrue();
    assertThat(firstItem.source().getName()).isEqualTo("src-a");
    assertThat(firstItem.target().getName()).isEqualTo("dst-a");
  }

  @Test
  void getConnectivityDetailResponse_usesObjectIdForComponentInstanceFilters() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    ComponentInstance filteredComponentInstance = componentInstance("component-instance-name");
    when(componentInstancesRepository.isReady()).thenReturn(true);
    when(componentInstancesRepository.get("component-instance-id"))
        .thenReturn(filteredComponentInstance);
    stubRepository(
        version, ConnectivityDetailFilter.COMPONENT_INSTANCE, componentInstancesRepository);

    ComponentInstance otherSource = componentInstanceWithId("other-source-id");
    ComponentInstance otherTarget = componentInstance("other-target");
    DependencyInstance dependency = dependencyInstance("dep-a");
    Service serviceLink = service("svc-a-id");

    stubEdgesByComponentInstance(
        version,
        componentInstancesRepository,
        new EdgeFixture(filteredComponentInstance, otherTarget, dependency, serviceLink),
        new EdgeFixture(
            otherSource, otherTarget, dependencyInstanceWithId("dep-b-id"), serviceId("svc-b-id")));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                "latest", "componentinstances", "component-instance-id", "both", 1, 10)
            .await()
            .indefinitely();

    assertThat(response.data()).hasSize(1);
    assertThat(response.data().getFirst().source().getName()).isEqualTo("component-instance-name");
  }

  @Test
  void getConnectivityDetailResponse_requiresExactlyOneFilter() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        null,
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10)
                    .await()
                    .indefinitely());

    assertThat(thrown).isNotNull();
    if (thrown == null) {
      return;
    }
    Throwable actualThrown = Objects.requireNonNull(thrown);

    assertThat(actualThrown).isInstanceOf(BadRequestException.class);
    assertThat(Objects.requireNonNull(actualThrown.getMessage()))
        .contains("Connectivity object id must be provided");
  }

  @Test
  void getConnectivityDetailResponse_throwsNotFoundWhenFilterValueDoesNotExist() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("missing-target")).thenReturn(null);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        "missing-target",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10)
                    .await()
                    .indefinitely());

    assertThat(thrown).isNotNull();
    if (thrown == null) {
      return;
    }
    Throwable actualThrown = Objects.requireNonNull(thrown);

    assertThat(actualThrown).isInstanceOf(NotFoundException.class);
    assertThat(Objects.requireNonNull(actualThrown.getMessage()))
        .contains("Connectivity detail not found");
  }

  @Test
  void getConnectivityDetailResponse_rejectsDirectionalServiceFilter() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.SERVICE,
                        "service-a-id",
                        ConnectivityDetailDirection.OUT,
                        1,
                        10)
                    .await()
                    .indefinitely());

    assertThat(thrown).isNotNull();
    if (thrown == null) {
      return;
    }
    Throwable actualThrown = Objects.requireNonNull(thrown);
    String message = Objects.requireNonNull(actualThrown.getMessage());

    assertThat(actualThrown).isInstanceOf(BadRequestException.class);
    assertThat(message).contains("direction=out").contains("services");
  }

  @Test
  void getConnectivityDetailResponse_rejectsDirectionalServiceFilterBeforeLookup() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.SERVICE,
                        "missing-service-id",
                        ConnectivityDetailDirection.OUT,
                        1,
                        10)
                    .await()
                    .indefinitely());

    assertThat(thrown).isNotNull();
    if (thrown == null) {
      return;
    }
    Throwable actualThrown = Objects.requireNonNull(thrown);
    String message = Objects.requireNonNull(actualThrown.getMessage());

    assertThat(actualThrown).isInstanceOf(BadRequestException.class);
    assertThat(message).contains("direction=out").contains("services");
  }

  @Test
  void getConnectivityDetailResponse_filtersNamespacesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    NamespacesRepository namespacesRepository = mock(NamespacesRepository.class);
    Namespace filteredNamespace = namespace("ns-shared");
    when(namespacesRepository.isReady()).thenReturn(true);
    when(namespacesRepository.get("ns-shared-id")).thenReturn(filteredNamespace);
    stubRepository(version, ConnectivityDetailFilter.NAMESPACE, namespacesRepository);

    ComponentInstance outMatchingSource = namespaceComponentInstance("src-out", "ns-shared");
    ComponentInstance outMatchingTarget =
        namespaceComponentInstanceWithId("dst-out-id", "ns-other");

    ComponentInstance inMatchingSource = namespaceComponentInstance("src-in", "ns-third");
    ComponentInstance inMatchingTarget = namespaceComponentInstanceWithId("dst-in-id", "ns-shared");

    stubGraph(
        version,
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.NAMESPACE,
                "ns-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.NAMESPACE,
                "ns-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    verify(graphEngineService, times(2))
        .getAllEdgesByNodeIds(eq(version.getVersionNumber()), anyList(), anyList());

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
  }

  @Test
  void getConnectivityDetailResponse_filtersRegionsByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    RegionsRepository regionsRepository = mock(RegionsRepository.class);
    Region filteredRegion = region("region-shared");
    when(regionsRepository.isReady()).thenReturn(true);
    when(regionsRepository.get("region-shared-id")).thenReturn(filteredRegion);
    stubRepository(version, ConnectivityDetailFilter.REGION, regionsRepository);

    ComponentInstance outMatchingSource = regionComponentInstance("src-out", "region-shared");
    ComponentInstance outMatchingTarget = regionComponentInstanceWithId("dst-out-id", "region-b");

    ComponentInstance inMatchingSource = regionComponentInstance("src-in", "region-c");
    ComponentInstance inMatchingTarget =
        regionComponentInstanceWithId("dst-in-id", "region-shared");

    stubGraph(
        version,
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.REGION,
                "region-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.REGION,
                "region-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    verify(graphEngineService, times(2))
        .getAllEdgesByNodeIds(eq(version.getVersionNumber()), anyList(), anyList());

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
  }

  @Test
  void getConnectivityDetailResponse_filtersZoneTypesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ZoneTypesRepository zoneTypesRepository = mock(ZoneTypesRepository.class);
    ZoneType filteredZoneType = zoneType("zone-shared");
    when(zoneTypesRepository.isReady()).thenReturn(true);
    when(zoneTypesRepository.get("zone-shared-id")).thenReturn(filteredZoneType);
    stubRepository(version, ConnectivityDetailFilter.ZONE_TYPE, zoneTypesRepository);

    ComponentInstance outMatchingSource = sourceZoneTypeComponent("src-out", "zone-shared");
    ComponentInstance outMatchingTarget = targetZoneTypeComponentWithId("dst-out-id", "zone-other");

    ComponentInstance inMatchingSource = sourceZoneTypeComponent("src-in", "zone-other");
    ComponentInstance inMatchingTarget = targetZoneTypeComponentWithId("dst-in-id", "zone-shared");

    stubGraph(
        version,
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.ZONE_TYPE,
                "zone-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.ZONE_TYPE,
                "zone-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    verify(graphEngineService, times(2))
        .getAllEdgesByNodeIds(eq(version.getVersionNumber()), anyList(), anyList());

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
  }

  @Test
  void getConnectivityDetailResponse_returnsEmptyListWhenNoConnectivityFound() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    ComponentInstance orphanComponentInstance = mock(ComponentInstance.class);
    when(orphanComponentInstance.getName()).thenReturn("orphan-component-instance");
    when(componentInstancesRepository.isReady()).thenReturn(true);
    when(componentInstancesRepository.get("orphan-component-instance-id"))
        .thenReturn(orphanComponentInstance);
    stubRepository(
        version, ConnectivityDetailFilter.COMPONENT_INSTANCE, componentInstancesRepository);

    when(graphEngineService.getAllEdgesByNodeIds(
            eq(version.getVersionNumber()), anyList(), anyList()))
        .thenReturn(List.of());

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                "latest", "componentinstances", "orphan-component-instance-id", "both", 1, 100)
            .await()
            .indefinitely();

    assertThat(response.data()).isEmpty();
    assertThat(response.metadata()).isNotNull();
    assertThat(response.metadata().pagination()).isNotNull();
    assertThat(response.metadata().pagination().count()).isZero();
    assertThat(response.metadata().pagination().pageNum()).isOne();
    assertThat(response.metadata().pagination().pageSize()).isEqualTo(100);
    assertThat(response.metadata().initialColumns())
        .containsExactly("name", "source", "target", "dependency_instance", "service", "pci");
  }

  @Test
  void getConnectivityDetailResponse_filtersLandingZonesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    LandingZone filteredLandingZone = mock(LandingZone.class);
    when(filteredLandingZone.getName()).thenReturn("lz-shared");
    when(landingZonesRepository.isReady()).thenReturn(true);
    when(landingZonesRepository.get("lz-shared-id")).thenReturn(filteredLandingZone);
    stubRepository(version, ConnectivityDetailFilter.LANDING_ZONE, landingZonesRepository);

    LandingZone lzShared = mock(LandingZone.class);
    when(lzShared.getName()).thenReturn("lz-shared");
    LandingZone lzOther = mock(LandingZone.class);
    when(lzOther.getName()).thenReturn("lz-other");

    ComponentInstance outMatchingSource = componentInstance("src-out");
    when(outMatchingSource.getLandingZone()).thenReturn(lzShared);
    ComponentInstance outMatchingTarget = componentInstanceWithId("dst-out-id");
    when(outMatchingTarget.getLandingZone()).thenReturn(lzOther);

    ComponentInstance inMatchingSource = componentInstance("src-in");
    when(inMatchingSource.getLandingZone()).thenReturn(lzOther);
    ComponentInstance inMatchingTarget = componentInstanceWithId("dst-in-id");
    when(inMatchingTarget.getLandingZone()).thenReturn(lzShared);

    List<Edge> edges =
        buildEdgesAndRepositories(
            version,
            componentInstancesRepository,
            new EdgeFixture(
                outMatchingSource,
                outMatchingTarget,
                dependencyInstance("dep-out"),
                service("svc-out-id")),
            new EdgeFixture(
                inMatchingSource,
                inMatchingTarget,
                dependencyInstance("dep-in"),
                service("svc-in-id")));

    when(graphEngineService.getAllEdgesByLandingZones(
            eq("latest"), eq(List.of("lz-shared")), eq(EdgeDirection.OUTBOUND)))
        .thenReturn(List.of(edges.get(0)));
    when(graphEngineService.getAllEdgesByLandingZones(
            eq("latest"), eq(List.of("lz-shared")), eq(EdgeDirection.INBOUND)))
        .thenReturn(List.of(edges.get(1)));
    when(graphEngineService.getAllEdgesByLandingZones(
            eq("latest"), eq(List.of("lz-shared")), eq(EdgeDirection.ANY)))
        .thenReturn(edges);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.LANDING_ZONE,
                "lz-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.LANDING_ZONE,
                "lz-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> bothResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.LANDING_ZONE,
                "lz-shared-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
    assertThat(bothResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in", "src-out");
  }

  @Test
  void getConnectivityDetailResponse_filtersScopes() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    @SuppressWarnings("unchecked")
    ConcurrentRepository<Scope> scopesRepository = mock(ConcurrentRepository.class);
    Scope filteredScope = mock(Scope.class);
    when(filteredScope.getName()).thenReturn("scope-shared");
    when(scopesRepository.isReady()).thenReturn(true);
    when(scopesRepository.get("scope-shared-id")).thenReturn(filteredScope);
    stubRepository(version, ConnectivityDetailFilter.SCOPE, scopesRepository);

    Scope scopeShared = mock(Scope.class);
    when(scopeShared.getName()).thenReturn("scope-shared");
    Scope scopeOther = mock(Scope.class);
    when(scopeOther.getName()).thenReturn("scope-other");

    ComponentInstance sourceMatching = componentInstanceWithId("src-match-id");
    when(sourceMatching.getScope()).thenReturn(scopeShared);
    ComponentInstance targetMatching = componentInstanceWithId("dst-match-id");
    when(targetMatching.getScope()).thenReturn(scopeShared);
    ComponentInstance otherComponent = componentInstanceWithId("other-comp-id");
    when(otherComponent.getScope()).thenReturn(scopeOther);

    DependencyInstance depScopeMatching = dependencyInstance("dep-3");
    when(depScopeMatching.getScope()).thenReturn(scopeShared);
    DependencyInstance dep1 = dependencyInstance("dep-1");
    DependencyInstance dep2 = dependencyInstance("dep-2");
    DependencyInstance dep4 = dependencyInstanceWithId("dep-4-id");
    when(dep4.getScope()).thenReturn(scopeOther);

    List<Edge> edges =
        buildEdgesAndRepositories(
            version,
            componentInstancesRepository,
            new EdgeFixture(sourceMatching, otherComponent, dep1, service("svc-1")),
            new EdgeFixture(otherComponent, targetMatching, dep2, service("svc-2")),
            new EdgeFixture(otherComponent, otherComponent, depScopeMatching, service("svc-3")),
            new EdgeFixture(otherComponent, otherComponent, dep4, serviceId("svc-4")));

    when(graphEngineService.getGraphEdges(
            eq("latest"), eq(0), eq(Integer.MAX_VALUE), eq(EdgeDirection.ANY)))
        .thenReturn(Uni.createFrom().item(edges));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.SCOPE,
                "scope-shared-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).hasSize(3);
  }

  @Test
  void getConnectivityDetailResponse_filtersServices() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ServicesRepository servicesRepository = mock(ServicesRepository.class);
    Service filteredService = serviceId("svc-match-id");
    when(servicesRepository.isReady()).thenReturn(true);
    when(servicesRepository.get("svc-match-id")).thenReturn(filteredService);
    stubRepository(version, ConnectivityDetailFilter.SERVICE, servicesRepository);

    ComponentInstance source = componentInstanceWithId("src-id");
    ComponentInstance target = componentInstanceWithId("dst-id");

    List<Edge> edges =
        buildEdgesAndRepositories(
            version,
            componentInstancesRepository,
            new EdgeFixture(source, target, dependencyInstance("dep-1"), service("svc-match-id")),
            new EdgeFixture(
                source, target, dependencyInstanceWithId("dep-2-id"), serviceId("svc-other-id")));

    when(graphEngineService.getGraphEdges(
            eq("latest"), eq(0), eq(Integer.MAX_VALUE), eq(EdgeDirection.ANY)))
        .thenReturn(Uni.createFrom().item(edges));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.SERVICE,
                "svc-match-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data())
        .extracting(item -> item.dependencyInstance().getName())
        .containsExactly("dep-1");
  }

  @Test
  void getConnectivityDetailResponse_filtersDependencies() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    @SuppressWarnings("unchecked")
    ConcurrentRepository<Dependency> dependenciesRepository = mock(ConcurrentRepository.class);
    Dependency filteredDependency = mock(Dependency.class);
    when(filteredDependency.getName()).thenReturn("dep-model-a");
    when(dependenciesRepository.isReady()).thenReturn(true);
    when(dependenciesRepository.get("dep-model-a-id")).thenReturn(filteredDependency);
    stubRepository(version, ConnectivityDetailFilter.DEPENDENCY, dependenciesRepository);

    ComponentInstance source = componentInstanceWithId("src-id");
    ComponentInstance target = componentInstanceWithId("dst-id");

    DependencyInstance depMatching = dependencyInstance("dep-inst-1");
    when(depMatching.getDependencyName()).thenReturn("dep-model-a");
    DependencyInstance depOther = dependencyInstanceWithId("dep-inst-2-id");
    when(depOther.getDependencyName()).thenReturn("dep-model-b");

    List<Edge> edges =
        buildEdgesAndRepositories(
            version,
            componentInstancesRepository,
            new EdgeFixture(source, target, depMatching, service("svc-1")),
            new EdgeFixture(source, target, depOther, serviceId("svc-2")));

    when(graphEngineService.getGraphEdges(
            eq("latest"), eq(0), eq(Integer.MAX_VALUE), eq(EdgeDirection.ANY)))
        .thenReturn(Uni.createFrom().item(edges));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.DEPENDENCY,
                "dep-model-a-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data())
        .extracting(item -> item.dependencyInstance().getName())
        .containsExactly("dep-inst-1");
  }

  @Test
  void getConnectivityDetailResponse_filtersDependencyInstances() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    DependencyInstancesRepository dependencyInstancesRepository =
        mock(DependencyInstancesRepository.class);
    DependencyInstance filteredDepInstance = mock(DependencyInstance.class);
    when(filteredDepInstance.getName()).thenReturn("dep-inst-match");
    when(dependencyInstancesRepository.isReady()).thenReturn(true);
    when(dependencyInstancesRepository.get("dep-inst-match-id")).thenReturn(filteredDepInstance);
    stubRepository(
        version, ConnectivityDetailFilter.DEPENDENCY_INSTANCE, dependencyInstancesRepository);

    ComponentInstance source = componentInstanceWithId("src-id");
    ComponentInstance target = componentInstanceWithId("dst-id");

    DependencyInstance depMatching = dependencyInstance("dep-inst-match");
    DependencyInstance depOther = dependencyInstanceWithId("dep-inst-other-id");
    when(depOther.getName()).thenReturn("dep-inst-other");

    List<Edge> edges =
        buildEdgesAndRepositories(
            version,
            componentInstancesRepository,
            new EdgeFixture(source, target, depMatching, service("svc-1")),
            new EdgeFixture(source, target, depOther, serviceId("svc-2")));

    when(graphEngineService.getGraphEdges(
            eq("latest"), eq(0), eq(Integer.MAX_VALUE), eq(EdgeDirection.ANY)))
        .thenReturn(Uni.createFrom().item(edges));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.DEPENDENCY_INSTANCE,
                "dep-inst-match-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data())
        .extracting(item -> item.dependencyInstance().getName())
        .containsExactly("dep-inst-match");
  }

  @Test
  void getConnectivityDetailResponse_filtersMacroPhasesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    @SuppressWarnings("unchecked")
    ConcurrentRepository<MacroPhase> macroPhasesRepository = mock(ConcurrentRepository.class);
    MacroPhase filteredMacroPhase = mock(MacroPhase.class);
    when(filteredMacroPhase.getName()).thenReturn("phase-shared");
    when(macroPhasesRepository.isReady()).thenReturn(true);
    when(macroPhasesRepository.get("phase-shared-id")).thenReturn(filteredMacroPhase);
    stubRepository(version, ConnectivityDetailFilter.MACRO_PHASE, macroPhasesRepository);

    LandingZone lzShared = mock(LandingZone.class);
    when(lzShared.getMacroPhaseName()).thenReturn("phase-shared");
    LandingZone lzOther = mock(LandingZone.class);
    when(lzOther.getMacroPhaseName()).thenReturn("phase-other");

    ComponentInstance outMatchingSource = componentInstance("src-out");
    when(outMatchingSource.getLandingZone()).thenReturn(lzShared);
    ComponentInstance outMatchingTarget = componentInstanceWithId("dst-out-id");
    when(outMatchingTarget.getLandingZone()).thenReturn(lzOther);

    ComponentInstance inMatchingSource = componentInstance("src-in");
    when(inMatchingSource.getLandingZone()).thenReturn(lzOther);
    ComponentInstance inMatchingTarget = componentInstanceWithId("dst-in-id");
    when(inMatchingTarget.getLandingZone()).thenReturn(lzShared);

    stubGraph(
        version,
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.MACRO_PHASE,
                "phase-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.MACRO_PHASE,
                "phase-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    verify(graphEngineService, times(2))
        .getAllEdgesByNodeIds(eq(version.getVersionNumber()), anyList(), anyList());

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
  }

  @Test
  void getConnectivityDetailResponse_filtersComponentsByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    @SuppressWarnings("unchecked")
    ConcurrentRepository<Component> componentsRepository = mock(ConcurrentRepository.class);
    Component filteredComponent = mock(Component.class);
    when(filteredComponent.getName()).thenReturn("comp-shared");
    when(componentsRepository.isReady()).thenReturn(true);
    when(componentsRepository.get("comp-shared-id")).thenReturn(filteredComponent);
    stubRepository(version, ConnectivityDetailFilter.COMPONENT, componentsRepository);

    ComponentInstance outMatchingSource = componentInstance("src-out");
    when(outMatchingSource.getComponentName()).thenReturn("comp-shared");
    ComponentInstance outMatchingTarget = componentInstanceWithId("dst-out-id");
    when(outMatchingTarget.getComponentName()).thenReturn("comp-other");

    ComponentInstance inMatchingSource = componentInstance("src-in");
    when(inMatchingSource.getComponentName()).thenReturn("comp-other");
    ComponentInstance inMatchingTarget = componentInstanceWithId("dst-in-id");
    when(inMatchingTarget.getComponentName()).thenReturn("comp-shared");

    stubGraph(
        version,
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.COMPONENT,
                "comp-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.COMPONENT,
                "comp-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    verify(graphEngineService, times(2))
        .getAllEdgesByNodeIds(eq(version.getVersionNumber()), anyList(), anyList());

    assertThat(outResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-out");
    assertThat(inResponse.data())
        .extracting(item -> item.source().getName())
        .containsExactly("src-in");
  }

  @ParameterizedTest
  @ValueSource(strings = {"cde", "csz", "dmz", "tnz", "cbz"})
  void getConnectivityDetailResponse_recognizesEveryPciZone(String zone) {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audCommonConfig.pciPhases()).thenReturn(List.of("PCI"));

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance source = componentInstanceWithId("source-id");
    LandingZone lz = mock(LandingZone.class);
    when(lz.getTargetName()).thenReturn("target");
    when(lz.getMacroPhaseName()).thenReturn("PCI");
    when(source.getLandingZone()).thenReturn(lz);
    when(source.getInternalZoneTypeNames(Direction.OUT)).thenReturn(zone);

    ComponentInstance destination = componentInstanceWithId("target-instance-id");
    DependencyInstance dependency = dependencyInstanceWithId("dep-id");
    Service serviceLink = service("svc-id");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target",
        new EdgeFixture(source, destination, dependency, serviceLink));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.TARGET,
                "target",
                ConnectivityDetailDirection.BOTH,
                true,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).hasSize(1);
    assertThat(response.data().getFirst().pci()).isTrue();
  }

  @Test
  void getConnectivityDetailResponse_rejectsNonPciAndNullZoneNames() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audCommonConfig.pciPhases()).thenReturn(List.of("PCI"));

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance nonPci = componentInstanceWithId("non-pci-id");
    LandingZone nonPciLz = mock(LandingZone.class);
    when(nonPciLz.getTargetName()).thenReturn("target");
    when(nonPciLz.getMacroPhaseName()).thenReturn("PCI");
    when(nonPci.getLandingZone()).thenReturn(nonPciLz);
    when(nonPci.getInternalZoneTypeNames(Direction.OUT)).thenReturn("public");

    ComponentInstance destination = targetComponentInstanceWithId("destination-id", "other");
    when(destination.getLandingZone().getMacroPhaseName()).thenReturn("NON_PCI");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target",
        new EdgeFixture(
            nonPci, destination, dependencyInstanceWithId("dep-id"), serviceId("svc-id")));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.TARGET,
                "target",
                ConnectivityDetailDirection.BOTH,
                true,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).isEmpty();
  }

  @Test
  void getConnectivityDetailResponse_handlesNullEmptyAndMixedSubnetEndpoints() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance nullEndpoints = targetComponentInstanceWithId("a-null-id", "target");
    ComponentInstance emptyEndpoints = targetComponentInstanceWithId("b-empty-id", "target");
    ComponentInstance mixedEndpoints = targetComponentInstanceWithId("c-mixed-id", "target");
    when(nullEndpoints.getInternalEndpoints(Direction.OUT)).thenReturn(null);
    when(emptyEndpoints.getInternalEndpoints(Direction.OUT)).thenReturn(Set.of());
    HashSet<Endpoint> endpoints = new HashSet<>();
    endpoints.add(null);
    endpoints.add(endpoint(null, "null-value"));
    endpoints.add(endpoint("10.0.0.2", "second"));
    endpoints.add(endpoint("10.0.0.1", "first"));
    when(mixedEndpoints.getInternalEndpoints(Direction.OUT)).thenReturn(endpoints);

    ComponentInstance destination = componentInstanceWithId("destination-id");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target",
        new EdgeFixture(nullEndpoints, destination, dependencyInstance("dep-a"), service("svc-a")),
        new EdgeFixture(emptyEndpoints, destination, dependencyInstance("dep-b"), service("svc-b")),
        new EdgeFixture(
            mixedEndpoints, destination, dependencyInstance("dep-c"), service("svc-c")));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.TARGET,
                "target",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).hasSize(3);
    assertThat(response.data().get(0).source().subnet()).isEmpty();
    assertThat(response.data().get(1).source().subnet()).isEmpty();
    assertThat(response.data().get(2).source().subnet()).containsExactly("10.0.0.1", "10.0.0.2");
  }

  @Test
  void getConnectivityDetailResponse_validatesPaginationAndPageOverflow() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance source = targetComponentInstanceWithId("src-id", "target");
    ComponentInstance destination = targetComponentInstanceWithId("dst-id", "target");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target",
        new EdgeFixture(source, destination, dependencyInstanceWithId("dep-id"), serviceId("svc")));

    Throwable zeroSize =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        "target",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        0)
                    .await()
                    .indefinitely());
    Throwable excessiveSize =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        "target",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10001)
                    .await()
                    .indefinitely());
    Throwable zeroPage =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        "target",
                        ConnectivityDetailDirection.BOTH,
                        0,
                        1)
                    .await()
                    .indefinitely());
    Throwable overflow =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        version,
                        ConnectivityDetailFilter.TARGET,
                        "target",
                        ConnectivityDetailDirection.BOTH,
                        2,
                        1)
                    .await()
                    .indefinitely());

    assertThat(zeroSize).isInstanceOf(BadRequestException.class);
    assertThat(excessiveSize).isInstanceOf(BadRequestException.class);
    assertThat(zeroPage).isInstanceOf(BadRequestException.class);
    assertThat(Objects.requireNonNull(overflow))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("last page");
  }

  @Test
  void getConnectivityDetailResponse_rejectsMissingBlankAndUnreadyFilterValues() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    Version missingRepository = mock(Version.class);
    when(missingRepository.getVersionNumber()).thenReturn("latest");

    Version blankValue = mock(Version.class);
    when(blankValue.getVersionNumber()).thenReturn("latest");
    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target blankTarget = mock(Target.class);
    when(blankTarget.getName()).thenReturn(" ");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("blank")).thenReturn(blankTarget);
    stubRepository(blankValue, ConnectivityDetailFilter.TARGET, targetsRepository);

    Throwable blankId =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        missingRepository,
                        ConnectivityDetailFilter.TARGET,
                        " ",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10)
                    .await()
                    .indefinitely());
    Throwable missing =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        missingRepository,
                        ConnectivityDetailFilter.TARGET,
                        "target",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10)
                    .await()
                    .indefinitely());
    Throwable blank =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse(
                        blankValue,
                        ConnectivityDetailFilter.TARGET,
                        "blank",
                        ConnectivityDetailDirection.BOTH,
                        1,
                        10)
                    .await()
                    .indefinitely());

    assertThat(blankId).isInstanceOf(BadRequestException.class);
    assertThat(missing).isInstanceOf(jakarta.ws.rs.ServiceUnavailableException.class);
    assertThat(blank).isInstanceOf(NotFoundException.class);
  }

  @Test
  void getConnectivityDetailResponse_handlesMissingNestedFilterRelations() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target targetFilter = mock(Target.class);
    when(targetFilter.getName()).thenReturn("target-filter");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target-id")).thenReturn(targetFilter);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstance sourceNoLandingZone = componentInstanceWithId("source-no-lz-id");
    ComponentInstance targetNoLandingZone = componentInstanceWithId("target-no-lz-id");

    stubEdgesByTarget(
        version,
        componentInstancesRepository,
        "target-filter",
        new EdgeFixture(
            sourceNoLandingZone,
            targetNoLandingZone,
            dependencyInstanceWithId("dep-id"),
            serviceId("svc-id")));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.TARGET,
                "target-id",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).isEmpty();
  }

  @Test
  void getConnectivityDetailResponse_skipsEdgesWithoutLinks() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);
    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    TargetsRepository targetsRepository = mock(TargetsRepository.class);
    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target");
    when(targetsRepository.isReady()).thenReturn(true);
    when(targetsRepository.get("target")).thenReturn(target);
    stubRepository(version, ConnectivityDetailFilter.TARGET, targetsRepository);

    ComponentInstancesRepository ciRepo = mock(ComponentInstancesRepository.class);
    DependencyInstancesRepository depRepo = mock(DependencyInstancesRepository.class);
    ServicesRepository svcRepo = mock(ServicesRepository.class);
    when(ciRepo.get("missing-source")).thenReturn(null);
    when(version.getComponentInstancesRepository()).thenReturn(ciRepo);
    when(version.getDependencyInstancesRepository()).thenReturn(depRepo);
    when(version.getServicesRepository()).thenReturn(svcRepo);

    when(graphEngineService.getAllEdgesByTargets(
            eq("latest"), eq(List.of("target")), any(EdgeDirection.class)))
        .thenReturn(
            List.of(new Edge(null, "dep-id", "svc-id", true, "missing-source", "target-id")));

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.TARGET,
                "target",
                ConnectivityDetailDirection.BOTH,
                1,
                10)
            .await()
            .indefinitely();

    assertThat(response.data()).isEmpty();
  }

  @Test
  void getConnectivityDetailResponse_rejectsUnknownTypeAndDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, graphEngineService, audCommonConfig);

    Throwable type =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse("latest", "unknown", "id", "both", 1, 10)
                    .await()
                    .indefinitely());
    Throwable direction =
        catchThrowable(
            () ->
                service
                    .getConnectivityDetailResponse("latest", "targets", "id", "sideways", 1, 10)
                    .await()
                    .indefinitely());

    assertThat(Objects.requireNonNull(type))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Unknown object type");
    assertThat(Objects.requireNonNull(direction))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Unsupported connectivity direction");
  }

  private void stubGraph(
      Version version,
      ComponentInstancesRepository componentInstancesRepository,
      ComponentInstance outMatchingSource,
      ComponentInstance outMatchingTarget,
      ComponentInstance inMatchingSource,
      ComponentInstance inMatchingTarget) {

    stubComponentInstancesData(
        componentInstancesRepository,
        outMatchingSource,
        outMatchingTarget,
        inMatchingSource,
        inMatchingTarget);

    stubEdges(
        version,
        componentInstancesRepository,
        new EdgeFixture(
            outMatchingSource,
            outMatchingTarget,
            dependencyInstance("dep-out"),
            service("svc-out-id")),
        new EdgeFixture(
            inMatchingSource,
            inMatchingTarget,
            dependencyInstance("dep-in"),
            service("svc-in-id")));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void stubRepository(
      Version version, ConnectivityDetailFilter filter, ConcurrentRepository<?> repository) {
    when(version.getRepository(filter.repositoryKey()))
        .thenReturn((ConcurrentRepository) repository);
  }

  private static ComponentInstance componentInstanceWithId(String id) {
    ComponentInstance componentInstance = mock(ComponentInstance.class);
    when(componentInstance.id()).thenReturn(id);
    return componentInstance;
  }

  private static ComponentInstance componentInstance(String name) {
    ComponentInstance componentInstance = componentInstanceWithId(name + "-id");
    when(componentInstance.getName()).thenReturn(name);
    return componentInstance;
  }

  private static ComponentInstance componentInstanceWithTarget(String name, String targetName) {
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getTargetName()).thenReturn(targetName);

    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    return componentInstance;
  }

  private static ComponentInstance targetComponentInstance(String name, String targetName) {
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getTargetName()).thenReturn(targetName);

    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    return componentInstance;
  }

  private static ComponentInstance targetComponentInstanceWithId(String id, String targetName) {
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getTargetName()).thenReturn(targetName);

    ComponentInstance componentInstance = componentInstanceWithId(id);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    return componentInstance;
  }

  private static ComponentInstance pciSourceComponentInstance(
      String name, String targetName, String macroPhaseName, String zoneTypeName) {
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getTargetName()).thenReturn(targetName);
    when(landingZone.getMacroPhaseName()).thenReturn(macroPhaseName);

    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    when(componentInstance.getInternalZoneTypeNames(Direction.OUT)).thenReturn(zoneTypeName);
    return componentInstance;
  }

  private static ComponentInstance namespaceComponentInstance(String name, String namespaceName) {
    Namespace namespace = namespace(namespaceName);
    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getNamespace()).thenReturn(namespace);
    return componentInstance;
  }

  private static ComponentInstance namespaceComponentInstanceWithId(
      String id, String namespaceName) {
    Namespace namespace = namespace(namespaceName);
    ComponentInstance componentInstance = componentInstanceWithId(id);
    when(componentInstance.getNamespace()).thenReturn(namespace);
    return componentInstance;
  }

  private static ComponentInstance regionComponentInstance(String name, String regionName) {
    Region region = region(regionName);
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getRegion()).thenReturn(region);

    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    return componentInstance;
  }

  private static ComponentInstance regionComponentInstanceWithId(String id, String regionName) {
    Region region = region(regionName);
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getRegion()).thenReturn(region);

    ComponentInstance componentInstance = componentInstanceWithId(id);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    return componentInstance;
  }

  private static ComponentInstance sourceZoneTypeComponent(String name, String zoneTypeName) {
    ZoneInstance zoneInstance = mock(ZoneInstance.class);
    when(zoneInstance.getZoneTypeName()).thenReturn(zoneTypeName);

    ComponentInstance componentInstance = componentInstance(name);
    when(componentInstance.getOutZoneInstance()).thenReturn(zoneInstance);
    return componentInstance;
  }

  private static ComponentInstance targetZoneTypeComponentWithId(String id, String zoneTypeName) {
    ZoneInstance zoneInstance = mock(ZoneInstance.class);
    when(zoneInstance.getZoneTypeName()).thenReturn(zoneTypeName);

    ComponentInstance componentInstance = componentInstanceWithId(id);
    when(componentInstance.getInZoneInstance()).thenReturn(zoneInstance);
    return componentInstance;
  }

  private static Endpoint endpoint(String value, String description) {
    return new Endpoint(Endpoint.TypeEnum.CIDR, value, description);
  }

  private static DependencyInstance dependencyInstanceWithId(String id) {
    DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    when(dependencyInstance.id()).thenReturn(id);
    return dependencyInstance;
  }

  private static DependencyInstance dependencyInstance(String name) {
    DependencyInstance dependencyInstance = dependencyInstanceWithId(name + "-id");
    when(dependencyInstance.getName()).thenReturn(name);
    return dependencyInstance;
  }

  private static Service service(String id) {
    Service service = serviceId(id);
    when(service.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
    return service;
  }

  private static Service service(String id, String name, String ports) {
    return service(id);
  }

  private static Service serviceId(String id) {
    Service service = mock(Service.class);
    when(service.id()).thenReturn(id);
    return service;
  }

  private static Namespace namespace(String name) {
    Namespace namespace = mock(Namespace.class);
    when(namespace.getName()).thenReturn(name);
    return namespace;
  }

  private static Region region(String name) {
    Region region = mock(Region.class);
    when(region.getName()).thenReturn(name);
    return region;
  }

  private static ZoneType zoneType(String name) {
    ZoneType zoneType = mock(ZoneType.class);
    when(zoneType.getName()).thenReturn(name);
    return zoneType;
  }

  private record EdgeFixture(
      ComponentInstance source,
      ComponentInstance target,
      DependencyInstance dependencyInstance,
      Service service) {}

  private void stubEmptyEdges(Version version) {
    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    when(componentInstancesRepository.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getComponentInstancesRepository()).thenReturn(componentInstancesRepository);
    when(graphEngineService.getAllEdgesByNodeIds(
            eq(version.getVersionNumber()), anyList(), anyList()))
        .thenReturn(List.of());
  }

  private void stubComponentInstancesData(
      ComponentInstancesRepository componentInstancesRepository, ComponentInstance... instances) {
    ConcurrentMap<String, ComponentInstance> data = new ConcurrentHashMap<>();
    for (ComponentInstance instance : instances) {
      data.put(instance.id(), instance);
    }
    when(componentInstancesRepository.getData()).thenReturn(data);
  }

  private List<Edge> buildEdgesAndRepositories(
      Version version,
      ComponentInstancesRepository componentInstancesRepository,
      EdgeFixture... fixtures) {
    DependencyInstancesRepository dependencyInstancesRepository =
        mock(DependencyInstancesRepository.class);
    ServicesRepository servicesRepository = mock(ServicesRepository.class);

    List<Edge> edges = new ArrayList<>();

    for (EdgeFixture fixture : fixtures) {
      when(componentInstancesRepository.get(fixture.source().id())).thenReturn(fixture.source());
      when(componentInstancesRepository.get(fixture.target().id())).thenReturn(fixture.target());
      when(dependencyInstancesRepository.get(fixture.dependencyInstance().id()))
          .thenReturn(fixture.dependencyInstance());
      when(servicesRepository.get(fixture.service().id())).thenReturn(fixture.service());

      edges.add(
          new Edge(
              null,
              fixture.dependencyInstance().id(),
              fixture.service().id(),
              true,
              fixture.source().id(),
              fixture.target().id()));
    }

    when(version.getComponentInstancesRepository()).thenReturn(componentInstancesRepository);
    when(version.getDependencyInstancesRepository()).thenReturn(dependencyInstancesRepository);
    when(version.getServicesRepository()).thenReturn(servicesRepository);

    return edges;
  }

  private void stubEdges(
      Version version,
      ComponentInstancesRepository componentInstancesRepository,
      EdgeFixture... fixtures) {
    List<Edge> edges = buildEdgesAndRepositories(version, componentInstancesRepository, fixtures);
    when(graphEngineService.getAllEdgesByNodeIds(
            eq(version.getVersionNumber()), anyList(), anyList()))
        .thenReturn(edges);
  }

  private void stubEdgesByTarget(
      Version version,
      ComponentInstancesRepository componentInstancesRepository,
      String targetName,
      EdgeFixture... fixtures) {
    List<Edge> edges = buildEdgesAndRepositories(version, componentInstancesRepository, fixtures);
    when(graphEngineService.getAllEdgesByTargets(
            eq(version.getVersionNumber()), eq(List.of(targetName)), any(EdgeDirection.class)))
        .thenReturn(edges);
  }

  private void stubEdgesByComponentInstance(
      Version version,
      ComponentInstancesRepository componentInstancesRepository,
      EdgeFixture... fixtures) {
    List<Edge> edges = buildEdgesAndRepositories(version, componentInstancesRepository, fixtures);
    when(graphEngineService.getAllEdgesByNodeIds(
            eq(version.getVersionNumber()), anyList(), anyList()))
        .thenReturn(edges);
  }
}
