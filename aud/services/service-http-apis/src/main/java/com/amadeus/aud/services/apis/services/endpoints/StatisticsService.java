package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.Statistics;
import com.amadeus.aud.libraries.yaml.models.StatisticsObject;
import com.amadeus.aud.libraries.yaml.models.StatisticsScope;
import com.amadeus.aud.services.apis.models.core.statistics.StatisticsObjects;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class StatisticsService extends BaseService {
  private final GraphEngineService graphEngineService;
  private final AudApiConfig audApiConfig;
  private final AudService audService;

  @Inject
  public StatisticsService(
      GraphEngineService graphEngineService, AudApiConfig audApiConfig, AudService audService) {
    this.graphEngineService = graphEngineService;
    this.audApiConfig = audApiConfig;
    this.audService = audService;
  }

  public Uni<Response> getStatistics(String version, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getStatistics(ver, uriInfo));
  }

  @CacheResult(cacheName = "getStatistics", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getStatistics(Version version, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getStatisticsResponse(version, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  public Uni<com.amadeus.aud.libraries.backend.models.v2.Response<StatisticsScope>>
      getStatisticsForScope(String version, String scope) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getStatisticsForScope(ver, scope));
  }

  @CacheResult(cacheName = "getStatisticsForScope", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<com.amadeus.aud.libraries.backend.models.v2.Response<StatisticsScope>>
      getStatisticsForScope(Version version, String scope) {
    return Uni.createFrom()
        .<com.amadeus.aud.libraries.backend.models.v2.Response<StatisticsScope>>item(
            Unchecked.supplier(
                () -> {
                  StatisticsContext ctx = buildStatisticsContext(version);
                  StatisticsScope statisticsScope = findStatisticsByScope(ctx, scope);

                  if (statisticsScope == null) {
                    throw new NotFoundException("Scope not found: " + scope);
                  }

                  return new com.amadeus.aud.libraries.backend.models.v2.Response<>(
                      statisticsScope, null);
                }))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private StatisticsScope findStatisticsByScope(StatisticsContext ctx, String scope) {
    return getStatisticsPerScope(
            ctx.depRepo(),
            ctx.depInstanceRepo(),
            ctx.compRepo(),
            ctx.compInstanceRepo(),
            ctx.serviceRepo(),
            ctx.edgeStats())
        .stream()
        .filter(s -> s.getScope().equalsIgnoreCase(scope))
        .findFirst()
        .orElse(null);
  }

  private Response getStatisticsResponse(Version version, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();
    StatisticsContext ctx = buildStatisticsContext(version);

    return getOkResponse(
        new Statistics()
            .global(
                getGlobalStatistics(
                    ctx.depRepo(),
                    ctx.depInstanceRepo(),
                    ctx.compRepo(),
                    ctx.compInstanceRepo(),
                    ctx.serviceRepo(),
                    ctx.edgeStats()))
            .scopes(
                getStatisticsPerScope(
                    ctx.depRepo(),
                    ctx.depInstanceRepo(),
                    ctx.compRepo(),
                    ctx.compInstanceRepo(),
                    ctx.serviceRepo(),
                    ctx.edgeStats()))
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .version(versionNumber)
            .apiVersion(audApiConfig.version()));
  }

  private StatisticsContext buildStatisticsContext(Version version) {
    DependenciesRepository depRepo = version.getDependenciesRepository();
    DependencyInstancesRepository depInstanceRepo = version.getDependencyInstancesRepository();
    ComponentsRepository compRepo = version.getComponentsRepository();
    ComponentInstancesRepository compInstanceRepo = version.getComponentInstancesRepository();
    ServicesRepository serviceRepo = version.getServicesRepository();

    checkRepository(depRepo, version.getVersionNumber(), "Dependencies");
    checkRepository(depInstanceRepo, version.getVersionNumber(), "Dependency Instances");
    checkRepository(compRepo, version.getVersionNumber(), "Components");
    checkRepository(compInstanceRepo, version.getVersionNumber(), "Component Instances");
    checkRepository(serviceRepo, version.getVersionNumber(), "Services");

    StatisticsObjects edgeStats = computeEdgeStatistics(version);

    return new StatisticsContext(
        depRepo, depInstanceRepo, compRepo, compInstanceRepo, serviceRepo, edgeStats);
  }

  private StatisticsObject getGlobalStatistics(
      DependenciesRepository depRepo,
      DependencyInstancesRepository depInstanceRepo,
      ComponentsRepository compRepo,
      ComponentInstancesRepository compInstanceRepo,
      ServicesRepository serviceRepo,
      StatisticsObjects edgeStats) {

    int dependencyCount = (int) depRepo.getDataSize();
    int dependencyInstanceCount = (int) depInstanceRepo.getDataSize();
    int componentCount = (int) compRepo.getDataSize();
    int componentInstanceCount = (int) compInstanceRepo.getDataSize();
    int serviceCount = (int) serviceRepo.getDataSize();

    return new StatisticsObject()
        .dependencies(dependencyCount)
        .dependencyInstances(dependencyInstanceCount)
        .components(componentCount)
        .componentInstances(componentInstanceCount)
        .services(serviceCount)
        .links(edgeStats.globalLinksCount())
        .nodes(edgeStats.globalNodesCount());
  }

  private List<StatisticsScope> getStatisticsPerScope(
      DependenciesRepository depRepo,
      DependencyInstancesRepository depInstanceRepo,
      ComponentsRepository compRepo,
      ComponentInstancesRepository compInstanceRepo,
      ServicesRepository serviceRepo,
      StatisticsObjects edgeStats) {
    Map<String, Integer> scopeNbDependency =
        getNumberOfObjectsPerScope(depRepo.getData().values(), Dependency::getScope);
    Map<String, Integer> scopeNbDependencyInstance =
        getNumberOfObjectsPerScope(
            depInstanceRepo.getData().values(), DependencyInstance::getScope);
    Map<String, Integer> scopeNbComponent =
        getNumberOfObjectsPerScope(compRepo.getData().values(), Component::getScope);
    Map<String, Integer> scopeNbComponentInstance =
        getNumberOfObjectsPerScope(
            compInstanceRepo.getData().values(), ComponentInstance::getScope);
    Map<String, Integer> scopeNbService =
        getNumberOfObjectsPerScope(serviceRepo.getData().values(), Service::getScope);

    Map<String, Integer> scopeNbNode = edgeStats.scopeNbNode();
    Set<String> scopes = new HashSet<>(scopeNbNode.keySet());
    List<StatisticsScope> output = new ArrayList<>();

    for (String scope : scopes) {
      StatisticsScope statisticsScope =
          new StatisticsScope()
              .scope(scope)
              .links(edgeStats.scopeNbLink().getOrDefault(scope, 0))
              .nodes(scopeNbNode.getOrDefault(scope, 0))
              .dependencies(scopeNbDependency.getOrDefault(scope, 0))
              .dependencyInstances(scopeNbDependencyInstance.getOrDefault(scope, 0))
              .components(scopeNbComponent.getOrDefault(scope, 0))
              .componentInstances(scopeNbComponentInstance.getOrDefault(scope, 0))
              .services(scopeNbService.getOrDefault(scope, 0));
      output.add(statisticsScope);
    }
    output.sort(Comparator.comparing(s -> s.getScope().toLowerCase()));
    return output;
  }

  private <T> Map<String, Integer> getNumberOfObjectsPerScope(
      Collection<T> objects, Function<T, Scope> scopeExtractor) {
    Map<String, Integer> scopeNbObject = new HashMap<>();
    for (T object : objects) {
      Scope scope = scopeExtractor.apply(object);
      if (scope != null) {
        String scopeName = scope.getName();
        scopeNbObject.put(scopeName, scopeNbObject.getOrDefault(scopeName, 0) + 1);
      }
    }
    return scopeNbObject;
  }

  private StatisticsObjects computeEdgeStatistics(Version version) {
    GraphStatistics stats =
        graphEngineService.getGraphStatistics(version.getVersionNumber()).await().indefinitely();

    if (stats == null) {
      return new StatisticsObjects(Map.of(), Map.of(), 0, 0);
    }

    Map<String, Integer> scopeNbLink = new HashMap<>();
    stats.scopeNbLink().forEach((k, v) -> scopeNbLink.put(k, v.intValue()));

    Map<String, Integer> scopeNbNode = new HashMap<>();
    stats.scopeNbNode().forEach((k, v) -> scopeNbNode.put(k, v.intValue()));

    return new StatisticsObjects(
        scopeNbLink, scopeNbNode, (int) stats.globalNodesCount(), (int) stats.globalLinksCount());
  }

  private record StatisticsContext(
      DependenciesRepository depRepo,
      DependencyInstancesRepository depInstanceRepo,
      ComponentsRepository compRepo,
      ComponentInstancesRepository compInstanceRepo,
      ServicesRepository serviceRepo,
      StatisticsObjects edgeStats) {}
}
