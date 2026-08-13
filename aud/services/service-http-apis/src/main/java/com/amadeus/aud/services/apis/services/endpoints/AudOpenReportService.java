package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.yaml.models.OpenResponse;
import com.amadeus.aud.libraries.yaml.models.SecurityRule;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec.EndpointTypeEnum;
import com.amadeus.aud.services.apis.utils.SecurityRuleUtils;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class AudOpenReportService extends BaseService {
  static final Set<String> SCOPE_EXCLUSIONS = Set.of("CCS", "Links", "Links_Automated");

  private final AudService audService;
  private final GraphEngineService graphEngineService;
  private final AudApiConfig audApiConfig;

  @Inject
  public AudOpenReportService(
      AudService audService,
      GraphEngineService graphEngineService,
      AudApiConfig audApiConfig,
      IpReportService ipReportService) {
    this.audService = audService;
    this.graphEngineService = graphEngineService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getOpenReport(
      String dataVersion, String source, String destination, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getOpenReport(version, source, destination, uriInfo));
  }

  @CacheResult(cacheName = "getOpenReport", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getOpenReport(
      Version version, String source, String destination, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(
            Unchecked.supplier(() -> getOpenReportResponse(version, source, destination, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  Response getOpenReportResponse(
      Version version, String source, String destination, UriInfo uriInfo)
      throws JsonProcessingException {

    String versionNumber = version.getVersionNumber();
    OpenResponse openResponse =
        new OpenResponse()
            .version(versionNumber)
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .apiVersion(audApiConfig.version());

    ComponentInstancesRepository componentInstancesRepository =
        version.getComponentInstancesRepository();
    checkRepository(componentInstancesRepository, version.getVersionNumber(), "ComponentInstances");
    DependencyInstancesRepository dependencyInstancesRepository =
        version.getDependencyInstancesRepository();
    checkRepository(
        dependencyInstancesRepository, version.getVersionNumber(), "DependencyInstances");

    boolean srcIsIp = IPv4Utils.isValidIpAddress(source);
    boolean dstIsIp = IPv4Utils.isValidIpAddress(destination);

    Map<String, ComponentInstance> sourceInstances =
        getComponentInstances(source, srcIsIp, true, componentInstancesRepository);
    Map<String, ComponentInstance> destinationInstances =
        getComponentInstances(destination, dstIsIp, false, componentInstancesRepository);
    Response errorResponse =
        validateInstancesAndBuildErrorResponse(
            sourceInstances,
            destinationInstances,
            source,
            destination,
            srcIsIp,
            dstIsIp,
            openResponse);
    if (errorResponse != null) {
      return errorResponse;
    }

    List<String> sourceNodeIds = new ArrayList<>(sourceInstances.keySet());
    List<String> destinationNodeIds = new ArrayList<>(destinationInstances.keySet());

    List<Edge> edges =
        graphEngineService
            .getGraphEdgesByNodeIds(
                version.getVersionNumber(), 0, Integer.MAX_VALUE, sourceNodeIds, destinationNodeIds)
            .await()
            .indefinitely();
    Response edgesErrorResponse =
        validateEdgesAndBuildErrorResponse(edges, srcIsIp, dstIsIp, openResponse);
    if (edgesErrorResponse != null) {
      return edgesErrorResponse;
    }

    List<SecurityRule> allSecurityRules =
        generateAllSecurityRules(
            sourceInstances, destinationInstances, edges, dependencyInstancesRepository);
    return getOkResponse(
        openResponse
            .rules(allSecurityRules)
            .srcIsIp(srcIsIp)
            .dstIsIp(dstIsIp)
            .errors(new ArrayList<>()));
  }

  private Response validateInstancesAndBuildErrorResponse(
      Map<String, ComponentInstance> sourceInstances,
      Map<String, ComponentInstance> destinationInstances,
      String source,
      String destination,
      boolean srcIsIp,
      boolean dstIsIp,
      OpenResponse openResponse)
      throws JsonProcessingException {
    if (sourceInstances.isEmpty() || destinationInstances.isEmpty()) {
      List<String> errors = new ArrayList<>();
      if (sourceInstances.isEmpty()) {
        errors.add(
            srcIsIp ? ("No components found for IP: " + source) : ("Source not found: " + source));
      }
      if (destinationInstances.isEmpty()) {
        errors.add(
            dstIsIp
                ? ("No components found for IP: " + destination)
                : ("Destination not found: " + destination));
      }
      return getOkResponse(openResponse.srcIsIp(srcIsIp).dstIsIp(dstIsIp).errors(errors));
    }
    return null;
  }

  private Response validateEdgesAndBuildErrorResponse(
      Collection<Edge> edges, boolean srcIsIp, boolean dstIsIp, OpenResponse openResponse)
      throws JsonProcessingException {
    if (edges == null) {
      return getOkResponse(
          openResponse
              .errors(List.of("No graph data or edges available"))
              .srcIsIp(srcIsIp)
              .dstIsIp(dstIsIp));
    }
    return null;
  }

  private static Map<String, ComponentInstance> getComponentInstances(
      String identifier,
      boolean isIp,
      boolean isSource,
      ComponentInstancesRepository componentInstancesRepository) {

    Map<String, ComponentInstance> instances = new HashMap<>();

    if (isIp) {
      instances.putAll(
          getComponentInstancesFromIp(identifier, componentInstancesRepository, isSource));
    } else {
      ComponentInstance instance = componentInstancesRepository.get(identifier);
      if (instance != null) {
        instances.put(instance.id(), instance);
      }
    }

    return instances;
  }

  private static Map<String, ComponentInstance> getComponentInstancesFromIp(
      String ipAddress, ComponentInstancesRepository componentInstancesRepo, boolean isSource) {
    Map<String, ComponentInstance> result = new HashMap<>();
    ConcurrentMap<String, ComponentInstance> componentData =
        componentInstancesRepo != null ? componentInstancesRepo.getData() : null;
    if (componentData == null) {
      return result;
    }

    for (ComponentInstance ci : componentData.values()) {
      if (SCOPE_EXCLUSIONS.contains(ci.getScopeName())) {
        continue;
      }

      if (matchesIpAddress(ci, ipAddress, isSource)) {
        result.put(ci.id(), ci);
      }
    }
    return result;
  }

  private static boolean matchesIpAddress(
      ComponentInstance ci, String ipAddress, boolean isSource) {
    if (ci.getEndpointType() == null) {
      return matchesInternalEndpoints(ci, ipAddress, isSource);
    } else if (ci.getEndpointType() == EndpointTypeEnum.CID_RLIST) {
      return matchesCidRlistEndpoints(ci, ipAddress);
    }
    return false;
  }

  private static boolean matchesInternalEndpoints(
      ComponentInstance ci, String ipAddress, boolean isSource) {
    Direction direction = isSource ? Direction.OUT : Direction.IN;
    Set<Endpoint> endpoints = ci.getInternalEndpoints(direction);

    for (Endpoint ep : endpoints) {
      if (isIpInCidrEndpoint(ipAddress, ep, ci)) {
        return true;
      }
    }
    return false;
  }

  private static boolean matchesCidRlistEndpoints(ComponentInstance ci, String ipAddress) {
    for (Object endpoint : ci.getEndpoints()) {
      if (endpoint instanceof String endpointStr
          && isIpInStringEndpoint(ipAddress, endpointStr, ci)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isIpInCidrEndpoint(String ipAddress, Endpoint ep, ComponentInstance ci) {
    try {
      return ep.getType() == Endpoint.TypeEnum.CIDR
          && IPv4Utils.isIpInEndpoint(ipAddress, ep.getValue());
    } catch (RuntimeException e) {
      Log.warnf(
          "Error checking IP %s against endpoint %s of Component Instance %s: %s",
          ipAddress, ep.getValue(), ci.id(), e.getMessage());
      return false;
    }
  }

  private static boolean isIpInStringEndpoint(
      String ipAddress, String endpointStr, ComponentInstance ci) {
    try {
      return IPv4Utils.isIpInEndpoint(ipAddress, endpointStr);
    } catch (RuntimeException e) {
      Log.warnf(
          "Error checking IP %s against endpoint %s of Component Instance %s: %s",
          ipAddress, endpointStr, ci.id(), e.getMessage());
      return false;
    }
  }

  private static List<SecurityRule> generateAllSecurityRules(
      Map<String, ComponentInstance> sourceInstances,
      Map<String, ComponentInstance> destinationInstances,
      Collection<Edge> edges,
      DependencyInstancesRepository dependencyInstancesRepository) {
    ArrayList<SecurityRule> securityRules = new ArrayList<>();
    for (Edge edge : edges) {
      ComponentInstance sourceInstance = sourceInstances.get(edge.getSourceNodeId());
      ComponentInstance destinationInstance = destinationInstances.get(edge.getTargetNodeId());

      if (sourceInstance != null && destinationInstance != null) {
        DependencyInstance dependencyInstance =
            findDependencyForEdge(edge, dependencyInstancesRepository);
        Service service = findServiceForEdge(dependencyInstance, edge);
        SecurityRule securityRule =
            SecurityRuleUtils.createSecurityRule(
                dependencyInstance, edge, service, sourceInstance, destinationInstance);
        securityRules.add(securityRule);
      }
    }

    return securityRules;
  }

  private static DependencyInstance findDependencyForEdge(
      Edge edge, DependencyInstancesRepository dependencyInstancesRepository) {
    String dependencyId = edge.getDependencyInstanceId();
    DependencyInstance dependency = dependencyInstancesRepository.get(dependencyId);
    if (dependency != null) {
      return dependency;
    }
    throw new IllegalStateException("Dependency not found for edge dependency id: " + dependencyId);
  }

  private static Service findServiceForEdge(DependencyInstance dependency, Edge edge) {
    for (Service service : dependency.getServices()) {
      if (Objects.equals(service.id(), edge.getServiceId())) {
        return service;
      }
    }
    throw new IllegalStateException(
        "Service not found for edge service id: " + edge.getServiceId());
  }
}
