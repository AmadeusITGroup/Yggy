package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
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

  @Mock private GraphEngineService graphEngineService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private AudService audService;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private StatisticsService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service = new StatisticsService(graphEngineService, audApiConfig, audService);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/statistics"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/statistics"));
  }

  @Test
  @DisplayName("should return statistics with components and dependencies")
  void getStatisticsWithValidVersion() throws Exception {
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

    Response response = service.getStatistics(version, uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    Statistics body = objectMapper.readValue(bodyBytes, Statistics.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");

    assertThat(body.getGlobal()).isNotNull();
    assertThat(body.getGlobal().getComponents()).isEqualTo(2);
    assertThat(body.getGlobal().getDependencies()).isEqualTo(2);
    assertThat(body.getGlobal().getServices()).isOne();

    assertThat(body.getScopes()).isNotNull();
    assertThat(body.getScopes()).hasSize(2);
  }

  @Test
  @DisplayName("should retrieve version from audService when using string version")
  void getStatisticsWithStringVersion() throws Exception {
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

    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

    Response response = service.getStatistics("1.0.0", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    Statistics body = objectMapper.readValue(bodyBytes, Statistics.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getGlobal().getComponents()).isOne();
  }
}
