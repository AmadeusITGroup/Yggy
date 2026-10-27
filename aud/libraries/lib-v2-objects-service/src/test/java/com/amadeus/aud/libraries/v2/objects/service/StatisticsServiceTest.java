package com.amadeus.aud.libraries.v2.objects.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.Statistics;
import com.amadeus.aud.libraries.yaml.models.StatisticsScope;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("StatisticsService Tests")
class StatisticsServiceTest {
  private static final String REQUEST_URI = "http://localhost:8080/api/statistics";
  private static final String VERSIONED_REQUEST_URI =
      "http://localhost:8080/api/versions/1.0.0/statistics";

  @Mock private GraphEngineService graphEngineService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private Version version;

  private StatisticsService service;

  @BeforeEach
  void setUp() {
    service = new StatisticsService(graphEngineService, audApiConfig);

    when(version.getVersionNumber()).thenReturn("1.0.0");
  }

  @Test
  @DisplayName("should return statistics with components and dependencies")
  void getStatisticsWithValidVersion() {
    when(audApiConfig.version()).thenReturn("v1");
    Scope networkScope = mock(Scope.class);
    when(networkScope.getName()).thenReturn("networking");

    Scope securityScope = mock(Scope.class);
    when(securityScope.getName()).thenReturn("security");

    Component comp1 = mock(Component.class);
    when(comp1.getScope()).thenReturn(networkScope);

    Component comp2 = mock(Component.class);
    when(comp2.getScope()).thenReturn(securityScope);

    Dependency dep1 = mock(Dependency.class);
    when(dep1.getScope()).thenReturn(networkScope);

    Dependency dep2 = mock(Dependency.class);
    when(dep2.getScope()).thenReturn(securityScope);

    Service svc1 = mock(Service.class);
    when(svc1.getScope()).thenReturn(networkScope);

    ComponentsRepository compRepo = mock(ComponentsRepository.class);
    ConcurrentHashMap<String, Component> compData = new ConcurrentHashMap<>();
    compData.put("comp1", comp1);
    compData.put("comp2", comp2);
    when(compRepo.getData()).thenReturn(compData);
    when(compRepo.getDataSize()).thenReturn(2L);
    when(compRepo.isReady()).thenReturn(true);
    when(version.getComponentsRepository()).thenReturn(compRepo);

    DependenciesRepository depRepo = mock(DependenciesRepository.class);
    ConcurrentHashMap<String, Dependency> depData = new ConcurrentHashMap<>();
    depData.put("dep1", dep1);
    depData.put("dep2", dep2);
    when(depRepo.getData()).thenReturn(depData);
    when(depRepo.getDataSize()).thenReturn(2L);
    when(depRepo.isReady()).thenReturn(true);
    when(version.getDependenciesRepository()).thenReturn(depRepo);

    ServicesRepository serviceRepo = mock(ServicesRepository.class);
    ConcurrentHashMap<String, Service> serviceData = new ConcurrentHashMap<>();
    serviceData.put("svc1", svc1);
    when(serviceRepo.getData()).thenReturn(serviceData);
    when(serviceRepo.getDataSize()).thenReturn(1L);
    when(serviceRepo.isReady()).thenReturn(true);
    when(version.getServicesRepository()).thenReturn(serviceRepo);

    ComponentInstancesRepository compInstanceRepo = mock(ComponentInstancesRepository.class);
    when(compInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(compInstanceRepo.getDataSize()).thenReturn(0L);
    when(compInstanceRepo.isReady()).thenReturn(true);
    when(version.getComponentInstancesRepository()).thenReturn(compInstanceRepo);

    DependencyInstancesRepository depInstanceRepo = mock(DependencyInstancesRepository.class);
    when(depInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(depInstanceRepo.getDataSize()).thenReturn(0L);
    when(depInstanceRepo.isReady()).thenReturn(true);
    when(version.getDependencyInstancesRepository()).thenReturn(depInstanceRepo);

    GraphStatistics graphStats =
        new GraphStatistics(
            Map.of("networking", 5L, "security", 3L),
            Map.of("networking", 10L, "security", 8L),
            18L,
            8L);

    when(graphEngineService.getGraphStatistics("1.0.0"))
        .thenReturn(Uni.createFrom().item(graphStats));

    Statistics body =
        service.getStatistics(version, REQUEST_URI, VERSIONED_REQUEST_URI).await().indefinitely();

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getUrl()).isEqualTo(REQUEST_URI);
    assertThat(body.getUrlVersion()).isEqualTo(VERSIONED_REQUEST_URI);

    assertThat(body.getGlobal()).isNotNull();
    assertThat(body.getGlobal().getComponents()).isEqualTo(2);
    assertThat(body.getGlobal().getDependencies()).isEqualTo(2);
    assertThat(body.getGlobal().getServices()).isOne();

    assertThat(body.getScopes()).isNotNull();
    assertThat(body.getScopes()).hasSize(2);
  }

  @Test
  @DisplayName("should return single-scope statistics for the supplied version")
  void getStatisticsWithSingleScope() {
    when(audApiConfig.version()).thenReturn("v1");
    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("default");

    Component comp = mock(Component.class);
    when(comp.getScope()).thenReturn(scope);

    ComponentsRepository compRepo = mock(ComponentsRepository.class);
    ConcurrentHashMap<String, Component> compData = new ConcurrentHashMap<>();
    compData.put("comp1", comp);
    when(compRepo.getData()).thenReturn(compData);
    when(compRepo.getDataSize()).thenReturn(1L);
    when(compRepo.isReady()).thenReturn(true);
    when(version.getComponentsRepository()).thenReturn(compRepo);

    DependenciesRepository depRepo = mock(DependenciesRepository.class);
    when(depRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(depRepo.getDataSize()).thenReturn(0L);
    when(depRepo.isReady()).thenReturn(true);
    when(version.getDependenciesRepository()).thenReturn(depRepo);

    ComponentInstancesRepository compInstanceRepo = mock(ComponentInstancesRepository.class);
    when(compInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(compInstanceRepo.getDataSize()).thenReturn(0L);
    when(compInstanceRepo.isReady()).thenReturn(true);
    when(version.getComponentInstancesRepository()).thenReturn(compInstanceRepo);

    DependencyInstancesRepository depInstanceRepo = mock(DependencyInstancesRepository.class);
    when(depInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(depInstanceRepo.getDataSize()).thenReturn(0L);
    when(depInstanceRepo.isReady()).thenReturn(true);
    when(version.getDependencyInstancesRepository()).thenReturn(depInstanceRepo);

    ServicesRepository serviceRepo = mock(ServicesRepository.class);
    when(serviceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(serviceRepo.isReady()).thenReturn(true);
    when(version.getServicesRepository()).thenReturn(serviceRepo);

    GraphStatistics graphStats =
        new GraphStatistics(Map.of("default", 2L), Map.of("default", 4L), 4L, 2L);

    when(graphEngineService.getGraphStatistics("1.0.0"))
        .thenReturn(Uni.createFrom().item(graphStats));

    Statistics body =
        service.getStatistics(version, REQUEST_URI, VERSIONED_REQUEST_URI).await().indefinitely();

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getGlobal().getComponents()).isOne();
    assertThat(body.getScopes())
        .singleElement()
        .satisfies(
            (StatisticsScope scopeStatistics) -> {
              assertThat(scopeStatistics.getScope()).isEqualTo("default");
              assertThat(scopeStatistics.getComponents()).isOne();
              assertThat(scopeStatistics.getLinks()).isEqualTo(2);
              assertThat(scopeStatistics.getNodes()).isEqualTo(4);
            });
  }

  @Test
  void getStatisticsForScopeMatchesNameIgnoringCase() {
    stubEmptyRepositories();
    when(graphEngineService.getGraphStatistics("1.0.0"))
        .thenReturn(
            Uni.createFrom()
                .item(
                    new GraphStatistics(
                        Map.of("networking", 3L), Map.of("networking", 4L), 4L, 3L)));

    Response<StatisticsScope> response =
        service.getStatisticsForScope(version, "NETWORKING").await().indefinitely();

    assertThat(response.data().getScope()).isEqualTo("networking");
    assertThat(response.data().getLinks()).isEqualTo(3);
    assertThat(response.data().getNodes()).isEqualTo(4);
    assertThat(response.metadata()).isNull();
  }

  @Test
  void getStatisticsForScopeRejectsUnknownScopeWithoutGraphData() {
    stubEmptyRepositories();
    when(graphEngineService.getGraphStatistics("1.0.0")).thenReturn(Uni.createFrom().nullItem());

    assertThatThrownBy(
            () -> service.getStatisticsForScope(version, "missing").await().indefinitely())
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Scope not found: missing");
  }

  private void stubEmptyRepositories() {
    DependenciesRepository depRepo = mock(DependenciesRepository.class);
    when(depRepo.isReady()).thenReturn(true);
    when(depRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getDependenciesRepository()).thenReturn(depRepo);

    DependencyInstancesRepository depInstanceRepo = mock(DependencyInstancesRepository.class);
    when(depInstanceRepo.isReady()).thenReturn(true);
    when(depInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getDependencyInstancesRepository()).thenReturn(depInstanceRepo);

    ComponentsRepository compRepo = mock(ComponentsRepository.class);
    when(compRepo.isReady()).thenReturn(true);
    when(compRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getComponentsRepository()).thenReturn(compRepo);

    ComponentInstancesRepository compInstanceRepo = mock(ComponentInstancesRepository.class);
    when(compInstanceRepo.isReady()).thenReturn(true);
    when(compInstanceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getComponentInstancesRepository()).thenReturn(compInstanceRepo);

    ServicesRepository serviceRepo = mock(ServicesRepository.class);
    when(serviceRepo.isReady()).thenReturn(true);
    when(serviceRepo.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getServicesRepository()).thenReturn(serviceRepo);
  }
}
