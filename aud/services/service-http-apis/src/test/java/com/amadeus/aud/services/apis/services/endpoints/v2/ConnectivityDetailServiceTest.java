package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.common.AudCommonConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.NamespacesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.amadeus.aud.services.apis.services.endpoints.VersionGraphCacheService;
import com.google.common.collect.ImmutableMap;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConnectivityDetailServiceTest {

  @Mock private AudService audService;
  @Mock private VersionGraphCacheService versionGraphCacheService;
  @Mock private AudCommonConfig audCommonConfig;

  @Test
  void getConnectivityDetailResponse_returnsFilteredPagedItems() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

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
        componentInstance("src-a", "component-a", "target-a", "PRD", "cde");
    ComponentInstance targetApproved =
        componentInstance("dst-a", "component-b", "target-a", "DEV", "app");
    DependencyInstance dependencyApproved = dependencyInstance("dep-a");
    Service serviceApproved = service("svc-a-id", "svc-a", "443");

    ComponentInstance sourceRejected =
        componentInstance("src-b", "component-c", "target-a", "DEV", "app");
    ComponentInstance targetRejected =
        componentInstance("dst-b", "component-d", "target-a", "DEV", "app");
    DependencyInstance dependencyRejected = dependencyInstance("dep-b");
    Service serviceRejected = service("svc-b-id", "svc-b", "8080");

    ComponentInstance sourceOtherTarget =
        componentInstance("src-c", "component-e", "target-b", "DEV", "app");
    ComponentInstance targetOtherTarget =
        componentInstance("dst-c", "component-f", "target-b", "DEV", "app");
    DependencyInstance dependencyOtherTarget = dependencyInstance("dep-c");
    Service serviceOtherTarget = service("svc-c-id", "svc-c", "8443");

    Edge edge1 =
        new Edge(sourceApproved, targetApproved, dependencyApproved, serviceApproved, true, true);
    Edge edge2 =
        new Edge(sourceRejected, targetRejected, dependencyRejected, serviceRejected, true, true);
    Edge edge3 =
        new Edge(
            sourceOtherTarget,
            targetOtherTarget,
            dependencyOtherTarget,
            serviceOtherTarget,
            true,
            true);
    Graph mockGraph = createMockGraph(edge1, edge2, edge3);
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);

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
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));

    ComponentInstancesRepository componentInstancesRepository =
        mock(ComponentInstancesRepository.class);
    ComponentInstance filteredComponentInstance =
        componentInstance("component-instance-name", "component-a", "target-a", "PRD", "cde");
    when(componentInstancesRepository.isReady()).thenReturn(true);
    when(componentInstancesRepository.get("component-instance-id"))
        .thenReturn(filteredComponentInstance);
    stubRepository(
        version, ConnectivityDetailFilter.COMPONENT_INSTANCE, componentInstancesRepository);

    ComponentInstance otherSource =
        componentInstance("other-source", "component-b", "target-a", "DEV", "app");
    ComponentInstance otherTarget =
        componentInstance("other-target", "component-c", "target-a", "DEV", "app");
    DependencyInstance dependency = dependencyInstance("dep-a");
    Service serviceLink = service("svc-a-id", "svc-a", "443");

    Edge edge1 =
        new Edge(filteredComponentInstance, otherTarget, dependency, serviceLink, true, true);
    Edge edge2 =
        new Edge(
            otherSource,
            otherTarget,
            dependencyInstance("dep-b"),
            service("svc-b-id", "svc-b", "8443"),
            true,
            true);
    Graph mockGraph = createMockGraph(edge1, edge2);
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);

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
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

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

    assertThat(actualThrown instanceof BadRequestException).isTrue();
    assertThat(Objects.requireNonNull(actualThrown.getMessage()))
        .contains("Connectivity object id must be provided");
  }

  @Test
  void getConnectivityDetailResponse_throwsNotFoundWhenFilterValueDoesNotExist() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

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

    assertThat(actualThrown instanceof NotFoundException).isTrue();
    assertThat(Objects.requireNonNull(actualThrown.getMessage()))
        .contains("Connectivity detail not found");
  }

  @Test
  void getConnectivityDetailResponse_rejectsDirectionalServiceFilter() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ServicesRepository servicesRepository = mock(ServicesRepository.class);
    Service filteredService = service("service-a-id", "service-a", "443");
    when(servicesRepository.isReady()).thenReturn(true);
    when(servicesRepository.get("service-a-id")).thenReturn(filteredService);
    when(version.getServicesRepository()).thenReturn(servicesRepository);

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

    assertThat(actualThrown instanceof BadRequestException).isTrue();
    assertThat(message).contains("direction=out").contains("services");
  }

  @Test
  void getConnectivityDetailResponse_rejectsDirectionalServiceFilterBeforeLookup() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ServicesRepository servicesRepository = mock(ServicesRepository.class);
    when(servicesRepository.isReady()).thenReturn(true);
    when(servicesRepository.get("missing-service-id")).thenReturn(null);
    when(version.getServicesRepository()).thenReturn(servicesRepository);

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

    assertThat(actualThrown instanceof BadRequestException).isTrue();
    assertThat(message).contains("direction=out").contains("services");
  }

  @Test
  void getConnectivityDetailResponse_filtersZoneInstancesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ZoneInstancesRepository zoneInstancesRepository = mock(ZoneInstancesRepository.class);
    ZoneInstance filteredZoneInstance = zoneInstance("zone-shared-id", "zone-shared");
    when(zoneInstancesRepository.isReady()).thenReturn(true);
    when(zoneInstancesRepository.get("zone-shared-id")).thenReturn(filteredZoneInstance);
    stubRepository(version, ConnectivityDetailFilter.ZONE_INSTANCE, zoneInstancesRepository);

    ComponentInstance outMatchingSource =
        componentInstanceWithNetwork(
            "src-out",
            "component-a",
            "target-a",
            "PRD",
            "ns-a",
            "region-a",
            "zone-a",
            "zone-other-id",
            "zone-shared-id",
            "subnet-a-in",
            "subnet-a-out");
    ComponentInstance outMatchingTarget =
        componentInstanceWithNetwork(
            "dst-out",
            "component-b",
            "target-a",
            "DEV",
            "ns-b",
            "region-b",
            "zone-b",
            "zone-in-out-id",
            "zone-out-out-id",
            "subnet-b-in",
            "subnet-b-out");

    ComponentInstance inMatchingSource =
        componentInstanceWithNetwork(
            "src-in",
            "component-c",
            "target-a",
            "DEV",
            "ns-c",
            "region-c",
            "zone-c",
            "zone-source-in-id",
            "zone-source-out-id",
            "subnet-c-in",
            "subnet-c-out");
    ComponentInstance inMatchingTarget =
        componentInstanceWithNetwork(
            "dst-in",
            "component-d",
            "target-a",
            "DEV",
            "ns-d",
            "region-d",
            "zone-d",
            "zone-shared-id",
            "zone-target-out-id",
            "subnet-d-in",
            "subnet-d-out");

    Edge edge1 =
        new Edge(
            outMatchingSource,
            outMatchingTarget,
            dependencyInstance("dep-out"),
            service("svc-out-id", "svc-out", "443"),
            true,
            true);
    Edge edge2 =
        new Edge(
            inMatchingSource,
            inMatchingTarget,
            dependencyInstance("dep-in"),
            service("svc-in-id", "svc-in", "8443"),
            true,
            true);
    Graph mockGraph = createMockGraph(edge1, edge2);
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.ZONE_INSTANCE,
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
                ConnectivityDetailFilter.ZONE_INSTANCE,
                "zone-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> bothResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.ZONE_INSTANCE,
                "zone-shared-id",
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
  void getConnectivityDetailResponse_filtersNamespacesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    NamespacesRepository namespacesRepository = mock(NamespacesRepository.class);
    Namespace filteredNamespace = namespace("ns-shared-id", "ns-shared");
    when(namespacesRepository.isReady()).thenReturn(true);
    when(namespacesRepository.get("ns-shared-id")).thenReturn(filteredNamespace);
    stubRepository(version, ConnectivityDetailFilter.NAMESPACE, namespacesRepository);

    ComponentInstance outMatchingSource =
        componentInstanceWithNetwork(
            "src-out",
            "component-a",
            "target-a",
            "PRD",
            "ns-shared",
            "region-a",
            "zone-a",
            "zone-a-in",
            "zone-a-out",
            "subnet-a-in",
            "subnet-a-out");
    ComponentInstance outMatchingTarget =
        componentInstanceWithNetwork(
            "dst-out",
            "component-b",
            "target-a",
            "DEV",
            "ns-other",
            "region-b",
            "zone-b",
            "zone-b-in",
            "zone-b-out",
            "subnet-b-in",
            "subnet-b-out");

    ComponentInstance inMatchingSource =
        componentInstanceWithNetwork(
            "src-in",
            "component-c",
            "target-a",
            "DEV",
            "ns-third",
            "region-c",
            "zone-c",
            "zone-c-in",
            "zone-c-out",
            "subnet-c-in",
            "subnet-c-out");
    ComponentInstance inMatchingTarget =
        componentInstanceWithNetwork(
            "dst-in",
            "component-d",
            "target-a",
            "DEV",
            "ns-shared",
            "region-d",
            "zone-d",
            "zone-d-in",
            "zone-d-out",
            "subnet-d-in",
            "subnet-d-out");

    stubGraph(version, outMatchingSource, outMatchingTarget, inMatchingSource, inMatchingTarget);

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
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    RegionsRepository regionsRepository = mock(RegionsRepository.class);
    Region filteredRegion = region("region-shared-id", "region-shared");
    when(regionsRepository.isReady()).thenReturn(true);
    when(regionsRepository.get("region-shared-id")).thenReturn(filteredRegion);
    stubRepository(version, ConnectivityDetailFilter.REGION, regionsRepository);

    ComponentInstance outMatchingSource =
        componentInstanceWithNetwork(
            "src-out",
            "component-a",
            "target-a",
            "PRD",
            "ns-a",
            "region-shared",
            "zone-a",
            "zone-a-in",
            "zone-a-out",
            "subnet-a-in",
            "subnet-a-out");
    ComponentInstance outMatchingTarget =
        componentInstanceWithNetwork(
            "dst-out",
            "component-b",
            "target-a",
            "DEV",
            "ns-b",
            "region-b",
            "zone-b",
            "zone-b-in",
            "zone-b-out",
            "subnet-b-in",
            "subnet-b-out");

    ComponentInstance inMatchingSource =
        componentInstanceWithNetwork(
            "src-in",
            "component-c",
            "target-a",
            "DEV",
            "ns-c",
            "region-c",
            "zone-c",
            "zone-c-in",
            "zone-c-out",
            "subnet-c-in",
            "subnet-c-out");
    ComponentInstance inMatchingTarget =
        componentInstanceWithNetwork(
            "dst-in",
            "component-d",
            "target-a",
            "DEV",
            "ns-d",
            "region-shared",
            "zone-d",
            "zone-d-in",
            "zone-d-out",
            "subnet-d-in",
            "subnet-d-out");

    stubGraph(version, outMatchingSource, outMatchingTarget, inMatchingSource, inMatchingTarget);

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
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    ZoneTypesRepository zoneTypesRepository = mock(ZoneTypesRepository.class);
    ZoneType filteredZoneType = zoneType("zone-shared-id", "zone-shared");
    when(zoneTypesRepository.isReady()).thenReturn(true);
    when(zoneTypesRepository.get("zone-shared-id")).thenReturn(filteredZoneType);
    stubRepository(version, ConnectivityDetailFilter.ZONE_TYPE, zoneTypesRepository);

    ComponentInstance outMatchingSource =
        componentInstanceWithDirectionalZoneNames(
            "src-out",
            "component-a",
            "target-a",
            "PRD",
            "ns-a",
            "region-a",
            "zone-other",
            "zone-shared");
    ComponentInstance outMatchingTarget =
        componentInstanceWithDirectionalZoneNames(
            "dst-out",
            "component-b",
            "target-a",
            "DEV",
            "ns-b",
            "region-b",
            "zone-other",
            "zone-other");

    ComponentInstance inMatchingSource =
        componentInstanceWithDirectionalZoneNames(
            "src-in",
            "component-c",
            "target-a",
            "DEV",
            "ns-c",
            "region-c",
            "zone-other",
            "zone-other");
    ComponentInstance inMatchingTarget =
        componentInstanceWithDirectionalZoneNames(
            "dst-in",
            "component-d",
            "target-a",
            "DEV",
            "ns-d",
            "region-d",
            "zone-shared",
            "zone-other");

    stubGraph(version, outMatchingSource, outMatchingTarget, inMatchingSource, inMatchingTarget);

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
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));

    com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository
        subnetInstancesRepository =
            mock(
                com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository
                    .class);
    SubnetInstance orphanSubnetInstance = subnetInstance("orphan-subnet-id", "orphan-subnet");
    when(subnetInstancesRepository.isReady()).thenReturn(true);
    when(subnetInstancesRepository.get("orphan-subnet-id")).thenReturn(orphanSubnetInstance);
    stubRepository(version, ConnectivityDetailFilter.SUBNET_INSTANCE, subnetInstancesRepository);

    Graph mockGraph = createMockGraph();
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);

    Response<List<ConnectivityDetailItem>> response =
        service
            .getConnectivityDetailResponse(
                "latest", "subnetinstances", "orphan-subnet-id", "both", 1, 100)
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
  void getConnectivityDetailResponse_filtersSubnetInstancesByDirection() {
    ConnectivityDetailService service =
        new ConnectivityDetailService(audService, versionGraphCacheService, audCommonConfig);

    Version version = mock(Version.class);
    when(version.getVersionNumber()).thenReturn("latest");

    com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository
        subnetInstancesRepository =
            mock(
                com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository
                    .class);
    SubnetInstance filteredSubnetInstance = subnetInstance("subnet-shared-id", "subnet-shared");
    when(subnetInstancesRepository.isReady()).thenReturn(true);
    when(subnetInstancesRepository.get("subnet-shared-id")).thenReturn(filteredSubnetInstance);
    stubRepository(version, ConnectivityDetailFilter.SUBNET_INSTANCE, subnetInstancesRepository);

    ComponentInstance outMatchingSource =
        componentInstanceWithNetwork(
            "src-out",
            "component-a",
            "target-a",
            "PRD",
            "ns-a",
            "region-a",
            "zone-a",
            "zone-a-in",
            "zone-a-out",
            "subnet-a-in",
            "subnet-shared-id");
    ComponentInstance outMatchingTarget =
        componentInstanceWithNetwork(
            "dst-out",
            "component-b",
            "target-a",
            "DEV",
            "ns-b",
            "region-b",
            "zone-b",
            "zone-b-in",
            "zone-b-out",
            "subnet-b-in",
            "subnet-b-out");

    ComponentInstance inMatchingSource =
        componentInstanceWithNetwork(
            "src-in",
            "component-c",
            "target-a",
            "DEV",
            "ns-c",
            "region-c",
            "zone-c",
            "zone-c-in",
            "zone-c-out",
            "subnet-c-in",
            "subnet-c-out");
    ComponentInstance inMatchingTarget =
        componentInstanceWithNetwork(
            "dst-in",
            "component-d",
            "target-a",
            "DEV",
            "ns-d",
            "region-d",
            "zone-d",
            "zone-d-in",
            "zone-d-out",
            "subnet-shared-id",
            "subnet-d-out");

    Edge edge1 =
        new Edge(
            outMatchingSource,
            outMatchingTarget,
            dependencyInstance("dep-out"),
            service("svc-out-id", "svc-out", "443"),
            true,
            true);
    Edge edge2 =
        new Edge(
            inMatchingSource,
            inMatchingTarget,
            dependencyInstance("dep-in"),
            service("svc-in-id", "svc-in", "8443"),
            true,
            true);
    Graph mockGraph = createMockGraph(edge1, edge2);
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);

    Response<List<ConnectivityDetailItem>> outResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.SUBNET_INSTANCE,
                "subnet-shared-id",
                ConnectivityDetailDirection.OUT,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> inResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.SUBNET_INSTANCE,
                "subnet-shared-id",
                ConnectivityDetailDirection.IN,
                1,
                10)
            .await()
            .indefinitely();

    Response<List<ConnectivityDetailItem>> bothResponse =
        service
            .getConnectivityDetailResponse(
                version,
                ConnectivityDetailFilter.SUBNET_INSTANCE,
                "subnet-shared-id",
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

  private void stubGraph(
      Version version,
      ComponentInstance outMatchingSource,
      ComponentInstance outMatchingTarget,
      ComponentInstance inMatchingSource,
      ComponentInstance inMatchingTarget) {

    Edge edge1 =
        new Edge(
            outMatchingSource,
            outMatchingTarget,
            dependencyInstance("dep-out"),
            service("svc-out-id", "svc-out", "443"),
            true,
            true);
    Edge edge2 =
        new Edge(
            inMatchingSource,
            inMatchingTarget,
            dependencyInstance("dep-in"),
            service("svc-in-id", "svc-in", "8443"),
            true,
            true);
    Graph mockGraph = createMockGraph(edge1, edge2);
    when(versionGraphCacheService.buildVersionGraph(eq(version))).thenReturn(mockGraph);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void stubRepository(
      Version version, ConnectivityDetailFilter filter, ConcurrentRepository<?> repository) {
    when(version.getRepository(filter.repositoryKey()))
        .thenReturn((ConcurrentRepository) repository);
  }

  private static ComponentInstance componentInstance(
      String name,
      String componentName,
      String targetName,
      String macroPhaseName,
      String zoneTypeName) {
    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getName()).thenReturn(name + "-lz");
    when(landingZone.getTargetName()).thenReturn(targetName);
    when(landingZone.getMacroPhaseName()).thenReturn(macroPhaseName);

    Region region = region(name + "-region-id", name + "-region");
    when(landingZone.getRegion()).thenReturn(region);

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn(name + "-scope");

    Namespace namespace = namespace(name + "-namespace-id", name + "-namespace");

    ZoneType zoneType = zoneType(name + "-zone-type-id", zoneTypeName);

    ZoneInstance inZoneInstance = zoneInstance(name + "-in-zone-id", name + "-in-zone");
    ZoneInstance outZoneInstance = zoneInstance(name + "-out-zone-id", name + "-out-zone");
    SubnetInstance inSubnetInstance = subnetInstance(name + "-in-subnet-id", name + "-in-subnet");
    SubnetInstance outSubnetInstance =
        subnetInstance(name + "-out-subnet-id", name + "-out-subnet");

    LinkedHashSet<String> componentNames = new LinkedHashSet<>();
    componentNames.add(componentName);

    ComponentInstance componentInstance = mock(ComponentInstance.class);
    when(componentInstance.id()).thenReturn(name + "-id");
    when(componentInstance.getName()).thenReturn(name);
    when(componentInstance.getComponentName()).thenReturn(componentName);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    when(componentInstance.getScope()).thenReturn(scope);
    when(componentInstance.getNamespace()).thenReturn(namespace);
    when(componentInstance.getZoneType()).thenReturn(zoneType);
    when(componentInstance.getInZoneInstance()).thenReturn(inZoneInstance);
    when(componentInstance.getOutZoneInstance()).thenReturn(outZoneInstance);
    when(componentInstance.getInSubnetInstance()).thenReturn(inSubnetInstance);
    when(componentInstance.getOutSubnetInstance()).thenReturn(outSubnetInstance);
    when(componentInstance.componentAndAncestorNames()).thenReturn(componentNames);
    when(componentInstance.getInternalZoneTypeNames(Direction.OUT)).thenReturn(zoneTypeName);
    when(componentInstance.getInternalZoneTypeNames(Direction.IN)).thenReturn(zoneTypeName);
    when(componentInstance.getInternalEndpoints(Direction.OUT))
        .thenReturn(Set.of(endpoint("10.0.0.0/24", name + "-out-subnet")));
    when(componentInstance.getInternalEndpoints(Direction.IN))
        .thenReturn(Set.of(endpoint("10.0.1.0/24", name + "-in-subnet")));
    return componentInstance;
  }

  private static ComponentInstance componentInstanceWithNetwork(
      String name,
      String componentName,
      String targetName,
      String macroPhaseName,
      String namespaceName,
      String regionName,
      String zoneTypeName,
      String inZoneId,
      String outZoneId,
      String inSubnetId,
      String outSubnetId) {
    return componentInstanceWithDirectionalZoneNames(
        name,
        componentName,
        targetName,
        macroPhaseName,
        namespaceName,
        regionName,
        zoneTypeName,
        zoneTypeName,
        inZoneId,
        outZoneId,
        inSubnetId,
        outSubnetId);
  }

  private static ComponentInstance componentInstanceWithDirectionalZoneNames(
      String name,
      String componentName,
      String targetName,
      String macroPhaseName,
      String namespaceName,
      String regionName,
      String inZoneTypeName,
      String outZoneTypeName) {
    return componentInstanceWithDirectionalZoneNames(
        name,
        componentName,
        targetName,
        macroPhaseName,
        namespaceName,
        regionName,
        inZoneTypeName,
        outZoneTypeName,
        name + "-in-zone-id",
        name + "-out-zone-id",
        name + "-in-subnet-id",
        name + "-out-subnet-id");
  }

  private static ComponentInstance componentInstanceWithDirectionalZoneNames(
      String name,
      String componentName,
      String targetName,
      String macroPhaseName,
      String namespaceName,
      String regionName,
      String inZoneTypeName,
      String outZoneTypeName,
      String inZoneId,
      String outZoneId,
      String inSubnetId,
      String outSubnetId) {
    Region region = region(regionName + "-id", regionName);
    Namespace namespace = namespace(namespaceName + "-id", namespaceName);
    ZoneType zoneType = zoneType(outZoneTypeName + "-id", outZoneTypeName);
    ZoneInstance inZoneInstance = zoneInstance(inZoneId, inZoneId);
    ZoneInstance outZoneInstance = zoneInstance(outZoneId, outZoneId);
    SubnetInstance inSubnetInstance = subnetInstance(inSubnetId, inSubnetId);
    SubnetInstance outSubnetInstance = subnetInstance(outSubnetId, outSubnetId);

    LandingZone landingZone = mock(LandingZone.class);
    when(landingZone.getName()).thenReturn(name + "-lz");
    when(landingZone.getTargetName()).thenReturn(targetName);
    when(landingZone.getMacroPhaseName()).thenReturn(macroPhaseName);
    when(landingZone.getRegion()).thenReturn(region);

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn(name + "-scope");

    LinkedHashSet<String> componentNames = new LinkedHashSet<>();
    componentNames.add(componentName);

    ComponentInstance componentInstance = mock(ComponentInstance.class);
    when(componentInstance.id()).thenReturn(name + "-id");
    when(componentInstance.getName()).thenReturn(name);
    when(componentInstance.getComponentName()).thenReturn(componentName);
    when(componentInstance.getLandingZone()).thenReturn(landingZone);
    when(componentInstance.getScope()).thenReturn(scope);
    when(componentInstance.getNamespace()).thenReturn(namespace);
    when(componentInstance.getZoneType()).thenReturn(zoneType);
    when(componentInstance.getInZoneInstance()).thenReturn(inZoneInstance);
    when(componentInstance.getOutZoneInstance()).thenReturn(outZoneInstance);
    when(componentInstance.getInSubnetInstance()).thenReturn(inSubnetInstance);
    when(componentInstance.getOutSubnetInstance()).thenReturn(outSubnetInstance);
    when(componentInstance.componentAndAncestorNames()).thenReturn(componentNames);
    when(componentInstance.getInternalZoneTypeNames(Direction.OUT)).thenReturn(outZoneTypeName);
    when(componentInstance.getInternalZoneTypeNames(Direction.IN)).thenReturn(inZoneTypeName);
    when(componentInstance.getInternalEndpoints(Direction.OUT))
        .thenReturn(Set.of(endpoint("10.1.0.0/24", outSubnetId)));
    when(componentInstance.getInternalEndpoints(Direction.IN))
        .thenReturn(Set.of(endpoint("10.1.1.0/24", inSubnetId)));
    return componentInstance;
  }

  private static Endpoint endpoint(String value, String description) {
    return new Endpoint(Endpoint.TypeEnum.CIDR, value, description);
  }

  private static DependencyInstance dependencyInstance(String name) {
    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn(name + "-scope");

    DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    when(dependencyInstance.id()).thenReturn(name + "-id");
    when(dependencyInstance.getName()).thenReturn(name);
    when(dependencyInstance.getDependencyName()).thenReturn(name + "-dependency");
    when(dependencyInstance.getScope()).thenReturn(scope);
    return dependencyInstance;
  }

  private static Service service(String id, String name, String ports) {
    Service service = mock(Service.class);
    when(service.id()).thenReturn(id);
    when(service.getName()).thenReturn(name);
    when(service.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
    when(service.getPorts()).thenReturn(ports);
    return service;
  }

  private static Namespace namespace(String id, String name) {
    Namespace namespace = mock(Namespace.class);
    when(namespace.id()).thenReturn(id);
    when(namespace.getName()).thenReturn(name);
    return namespace;
  }

  private static Region region(String id, String name) {
    Region region = mock(Region.class);
    when(region.id()).thenReturn(id);
    when(region.getName()).thenReturn(name);
    return region;
  }

  private static ZoneType zoneType(String id, String name) {
    ZoneType zoneType = mock(ZoneType.class);
    when(zoneType.id()).thenReturn(id);
    when(zoneType.getName()).thenReturn(name);
    return zoneType;
  }

  private static ZoneInstance zoneInstance(String id, String name) {
    ZoneInstance zoneInstance = mock(ZoneInstance.class);
    when(zoneInstance.id()).thenReturn(id);
    when(zoneInstance.getName()).thenReturn(name);
    return zoneInstance;
  }

  private static SubnetInstance subnetInstance(String id, String name) {
    SubnetInstance subnetInstance = mock(SubnetInstance.class);
    when(subnetInstance.id()).thenReturn(id);
    when(subnetInstance.getName()).thenReturn(name);
    return subnetInstance;
  }

  private static Graph createMockGraph(Edge... edges) {
    Graph mockGraph = mock(Graph.class);
    ImmutableMap.Builder<String, Edge> builder = ImmutableMap.builder();
    for (Edge edge : edges) {
      builder.put(edge.getId(), edge);
    }
    ImmutableMap<String, Edge> result = builder.build();
    when(mockGraph.getEdges()).thenReturn(result);
    return mockGraph;
  }
}
