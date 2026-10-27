package com.amadeus.aud.libraries.v2.objects.service;

import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.DEFAULT_PAGE_NUM;
import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.v2.objects.service.PaginationDefaults.MAX_PAGE_SIZE;

import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.common.AudCommonConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@ApplicationScoped
public class ConnectivityDetailService {

  private static final List<String> INITIAL_COLUMNS =
      List.of("name", "source", "target", "dependency_instance", "service", "pci");
  private static final Duration DEFAULT_GRAPH_TIMEOUT = Duration.ofSeconds(30);

  private static final Comparator<Link> LINK_COMPARATOR =
      Comparator.comparing((Link link) -> link.dependencyInstance().getName())
          .thenComparing(link -> link.source().getName())
          .thenComparing(link -> link.target().getName())
          .thenComparing(link -> link.service().getName());

  private final GraphEngineService graphEngineService;
  private final AudCommonConfig audCommonConfig;

  @Inject
  public ConnectivityDetailService(
      GraphEngineService graphEngineService, AudCommonConfig audCommonConfig) {
    this.graphEngineService = graphEngineService;
    this.audCommonConfig = audCommonConfig;
  }

  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      Version version,
      String objectType,
      @Nullable String objectId,
      @Nullable String direction,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    return getConnectivityDetailResponse(
        version, objectType, objectId, direction, null, pageNum, pageSize);
  }

  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      Version version,
      String objectType,
      @Nullable String objectId,
      @Nullable String direction,
      @Nullable Boolean pci,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    ConnectivityDetailFilter filter = resolveFilter(objectType);
    ConnectivityDetailDirection connectivityDirection =
        ConnectivityDetailDirection.fromQueryValue(direction);
    return getConnectivityDetailResponse(
        version, filter, objectId, connectivityDirection, pci, pageNum, pageSize);
  }

  @CacheResult(
      cacheName = "getConnectivityDetailResponseV2",
      keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      Version version,
      ConnectivityDetailFilter filter,
      @Nullable String objectId,
      ConnectivityDetailDirection direction,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    return getConnectivityDetailResponse(
        version, filter, objectId, direction, null, pageNum, pageSize);
  }

  @CacheResult(
      cacheName = "getConnectivityDetailResponseV2",
      keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      Version version,
      ConnectivityDetailFilter filter,
      @Nullable String objectId,
      ConnectivityDetailDirection direction,
      @Nullable Boolean pci,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () -> buildResponse(version, filter, objectId, direction, pci, pageNum, pageSize)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response<List<ConnectivityDetailItem>> buildResponse(
      Version version,
      ConnectivityDetailFilter filter,
      @Nullable String objectId,
      ConnectivityDetailDirection direction,
      @Nullable Boolean pci,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    validatePagination(pageNum, pageSize);

    ConnectivityFilterRequest filterRequest =
        resolveFilterRequest(version, filter, objectId, direction);

    List<Link> filteredLinks = getFilteredLinks(version, filterRequest, Boolean.TRUE.equals(pci));
    if (filteredLinks.isEmpty()) {
      Metadata metadata =
          new Metadata()
              .withInitialColumns(INITIAL_COLUMNS)
              .withPagination(new Pagination(0, DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE));
      return new Response<>(List.of(), metadata);
    }

    filteredLinks.sort(LINK_COMPARATOR);

    int effectivePageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
    int effectivePageNum = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;
    int totalCount = filteredLinks.size();
    int lastPage = Math.max(1, (int) Math.ceil((double) totalCount / effectivePageSize));

    if (effectivePageNum > lastPage) {
      throw new BadRequestException(
          "pageNum (" + effectivePageNum + ") exceeds last page (" + lastPage + ")");
    }

    int offset = (effectivePageNum - 1) * effectivePageSize;
    int endIndex = Math.min(offset + effectivePageSize, totalCount);
    List<ConnectivityDetailItem> items = new ArrayList<>(endIndex - offset);
    for (int i = offset; i < endIndex; i++) {
      items.add(toItem(filteredLinks.get(i)));
    }

    Metadata metadata =
        new Metadata()
            .withInitialColumns(INITIAL_COLUMNS)
            .withPagination(new Pagination(totalCount, effectivePageNum, effectivePageSize));

    return new Response<>(items, metadata);
  }

  private static ConnectivityDetailFilter resolveFilter(String objectType) {
    return ConnectivityDetailFilter.fromObjectTypeUri(objectType);
  }

  private ConnectivityFilterRequest resolveFilterRequest(
      Version version,
      ConnectivityDetailFilter filter,
      @Nullable String objectId,
      ConnectivityDetailDirection direction) {
    validateDirection(filter, direction);
    String normalizedObjectId = normalizeObjectId(objectId);
    String filterValue = resolveFilterValue(version, filter, normalizedObjectId);
    return new ConnectivityFilterRequest(filter, normalizedObjectId, filterValue, direction);
  }

  private String resolveFilterValue(
      Version version, ConnectivityDetailFilter filter, String objectId) {
    String versionNumber = version.getVersionNumber();
    String repositoryKey = filter.repositoryKey();
    return repositoryValue(
        version.getRepository(repositoryKey),
        versionNumber,
        repositoryKey,
        objectId,
        filter::extractValue);
  }

  private static void validateDirection(
      ConnectivityDetailFilter filter, ConnectivityDetailDirection direction) {
    if (direction == ConnectivityDetailDirection.BOTH) {
      return;
    }

    if (switch (filter) {
      case SCOPE, SERVICE, DEPENDENCY, DEPENDENCY_INSTANCE -> true;
      default -> false;
    }) {
      throw new BadRequestException(
          "direction="
              + direction.queryValue()
              + " is not supported for connectivity object type: "
              + filter.objectType().getUri());
    }
  }

  private <T> String repositoryValue(
      @Nullable ConcurrentRepository<T> repository,
      String versionNumber,
      String repositoryName,
      String objectId,
      Function<T, String> valueExtractor) {
    checkRepository(repository, versionNumber, repositoryName);
    if (repository == null) {
      throw new NotFoundException("Connectivity detail not found");
    }
    T object = repository.get(objectId);
    if (object == null) {
      throw new NotFoundException("Connectivity detail not found");
    }
    String value = valueExtractor.apply(object);
    if (value == null || value.isBlank()) {
      throw new NotFoundException("Connectivity detail not found");
    }
    return value;
  }

  private static String normalizeObjectId(@Nullable String objectId) {
    if (objectId == null) {
      throw new BadRequestException("Connectivity object id must be provided.");
    }
    String trimmedObjectId = objectId.trim();
    if (trimmedObjectId.isEmpty()) {
      throw new BadRequestException("Connectivity object id must be provided.");
    }
    return trimmedObjectId;
  }

  private static void validatePagination(@Nullable Integer pageNum, @Nullable Integer pageSize) {
    if (pageSize != null && pageSize < 1) {
      throw new BadRequestException("pageSize must be >= 1");
    }
    if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
      throw new BadRequestException("pageSize must be <= %s".formatted(MAX_PAGE_SIZE));
    }
    if (pageNum != null && pageNum < 1) {
      throw new BadRequestException("pageNum must be >= 1");
    }
  }

  private List<Link> getFilteredLinks(
      Version version, ConnectivityFilterRequest filterRequest, boolean pciOnly) {
    List<Edge> edges = getEdges(version, filterRequest);

    List<Link> filteredLinks = new ArrayList<>();
    for (Edge dtoEdge : edges) {
      ComponentInstance source =
          version.getComponentInstancesRepository().get(dtoEdge.getSourceNodeId());
      ComponentInstance destination =
          version.getComponentInstancesRepository().get(dtoEdge.getTargetNodeId());
      DependencyInstance depInstance =
          version.getDependencyInstancesRepository().get(dtoEdge.getDependencyInstanceId());
      Service service = version.getServicesRepository().get(dtoEdge.getServiceId());

      if (source != null && destination != null && depInstance != null && service != null) {
        if (filterRequest.matches(source, destination, depInstance, service)
            && (!pciOnly || isPci(source, destination))) {
          Link link = new Link(source, destination, depInstance, service);
          filteredLinks.add(link);
        }
      }
    }
    return filteredLinks;
  }

  private List<Edge> getEdges(Version version, ConnectivityFilterRequest filterRequest) {
    String versionNumber = version.getVersionNumber();
    EdgeDirection edgeDirection = mapToEdgeDirection(filterRequest.direction());
    return switch (filterRequest.filterType()) {
      case TARGET ->
          graphEngineService.getAllEdgesByTargets(
              versionNumber, List.of(filterRequest.filterValue()), edgeDirection);
      case LANDING_ZONE ->
          graphEngineService.getAllEdgesByLandingZones(
              versionNumber, List.of(filterRequest.filterValue()), edgeDirection);
      // Scope doesn't take into account the dependency instance's scope
      // case SCOPE ->
      //     graphEngineService.getAllEdgesByScope(versionNumber, filterRequest.filterValue());
      case COMPONENT_INSTANCE -> getComponentInstanceEdges(versionNumber, filterRequest);
      case MACRO_PHASE ->
          getComponentAttributeEdges(
              version,
              filterRequest,
              ConnectivityDetailService::macroPhaseName,
              ConnectivityDetailService::macroPhaseName);
      case COMPONENT ->
          getComponentAttributeEdges(
              version,
              filterRequest,
              ConnectivityDetailService::componentName,
              ConnectivityDetailService::componentName);
      case NAMESPACE ->
          getComponentAttributeEdges(
              version,
              filterRequest,
              ConnectivityDetailService::namespaceName,
              ConnectivityDetailService::namespaceName);
      case REGION ->
          getComponentAttributeEdges(
              version,
              filterRequest,
              ConnectivityDetailService::regionName,
              ConnectivityDetailService::regionName);
      case ZONE_TYPE ->
          getComponentAttributeEdges(
              version,
              filterRequest,
              ci -> zoneTypeName(ci, Direction.OUT),
              ci -> zoneTypeName(ci, Direction.IN));
      default ->
          graphEngineService
              .getGraphEdges(versionNumber, 0, Integer.MAX_VALUE, edgeDirection)
              .await()
              .atMost(DEFAULT_GRAPH_TIMEOUT);
    };
  }

  private List<Edge> getComponentInstanceEdges(
      String versionNumber, ConnectivityFilterRequest filterRequest) {
    return switch (filterRequest.direction()) {
      case OUT ->
          graphEngineService.getAllEdgesByNodeIds(
              versionNumber, List.of(filterRequest.objectId()), List.of());
      case IN ->
          graphEngineService.getAllEdgesByNodeIds(
              versionNumber, List.of(), List.of(filterRequest.objectId()));
      case BOTH -> {
        List<Edge> outgoing =
            graphEngineService.getAllEdgesByNodeIds(
                versionNumber, List.of(filterRequest.objectId()), List.of());
        List<Edge> incoming =
            graphEngineService.getAllEdgesByNodeIds(
                versionNumber, List.of(), List.of(filterRequest.objectId()));
        Set<Edge> edges = new LinkedHashSet<>(outgoing);
        edges.addAll(incoming);
        yield new ArrayList<>(edges);
      }
    };
  }

  private List<Edge> getComponentAttributeEdges(
      Version version,
      ConnectivityFilterRequest filterRequest,
      Function<ComponentInstance, String> sourceAttribute,
      Function<ComponentInstance, String> destinationAttribute) {
    String versionNumber = version.getVersionNumber();
    String filterValue = filterRequest.filterValue();
    return switch (filterRequest.direction()) {
      case OUT ->
          graphEngineService.getAllEdgesByNodeIds(
              versionNumber, matchingNodeIds(version, sourceAttribute, filterValue), List.of());
      case IN ->
          graphEngineService.getAllEdgesByNodeIds(
              versionNumber,
              List.of(),
              matchingNodeIds(version, destinationAttribute, filterValue));
      case BOTH -> {
        List<Edge> outgoing =
            graphEngineService.getAllEdgesByNodeIds(
                versionNumber, matchingNodeIds(version, sourceAttribute, filterValue), List.of());
        List<Edge> incoming =
            graphEngineService.getAllEdgesByNodeIds(
                versionNumber,
                List.of(),
                matchingNodeIds(version, destinationAttribute, filterValue));
        Set<Edge> edges = new LinkedHashSet<>(outgoing);
        edges.addAll(incoming);
        yield new ArrayList<>(edges);
      }
    };
  }

  private static List<String> matchingNodeIds(
      Version version, Function<ComponentInstance, String> attribute, String filterValue) {
    List<String> nodeIds = new ArrayList<>();
    for (ComponentInstance componentInstance :
        version.getComponentInstancesRepository().getData().values()) {
      if (Objects.equals(attribute.apply(componentInstance), filterValue)) {
        nodeIds.add(componentInstance.id());
      }
    }
    return nodeIds;
  }

  private EdgeDirection mapToEdgeDirection(ConnectivityDetailDirection direction) {
    return switch (direction) {
      case OUT -> EdgeDirection.OUTBOUND;
      case IN -> EdgeDirection.INBOUND;
      case BOTH -> EdgeDirection.ANY;
    };
  }

  private ConnectivityDetailItem toItem(Link link) {
    return new ConnectivityDetailItem(
        link.name(),
        new ConnectivityDetailItem.ConnectivityDetailSide(
            link.source(), subnetValues(link.source(), Direction.OUT)),
        new ConnectivityDetailItem.ConnectivityDetailSide(
            link.target(), subnetValues(link.target(), Direction.IN)),
        link.dependencyInstance(),
        link.service(),
        isPci(link.source(), link.target()));
  }

  private static List<String> subnetValues(
      @Nullable ComponentInstance componentInstance, Direction direction) {
    if (componentInstance == null) {
      return List.of();
    }

    Set<Endpoint> endpoints = componentInstance.getInternalEndpoints(direction);
    if (endpoints == null || endpoints.isEmpty()) {
      return List.of();
    }

    return endpoints.stream()
        .filter(Objects::nonNull)
        .map(Endpoint::getValue)
        .filter(Objects::nonNull)
        .sorted(String::compareToIgnoreCase)
        .toList();
  }

  private boolean isPci(ComponentInstance source, ComponentInstance destination) {
    return isPciSource(source) || isPciTarget(destination);
  }

  private boolean isPciSource(ComponentInstance componentInstance) {
    LandingZone landingZone = componentInstance.getLandingZone();
    return landingZone != null
        && isPciMacroPhase(landingZone.getMacroPhaseName())
        && isPciZone(componentInstance.getInternalZoneTypeNames(Direction.OUT));
  }

  private boolean isPciTarget(ComponentInstance componentInstance) {
    LandingZone landingZone = componentInstance.getLandingZone();
    return landingZone != null
        && isPciMacroPhase(landingZone.getMacroPhaseName())
        && isPciZone(componentInstance.getInternalZoneTypeNames(Direction.IN));
  }

  private boolean isPciMacroPhase(String macroPhaseName) {
    return audCommonConfig.pciPhases().contains(macroPhaseName);
  }

  private boolean isPciZone(String zoneName) {
    if (zoneName == null) {
      return false;
    }
    return Objects.equals(zoneName, "cde")
        || Objects.equals(zoneName, "csz")
        || Objects.equals(zoneName, "dmz")
        || Objects.equals(zoneName, "tnz")
        || Objects.equals(zoneName, "cbz");
  }

  private static @Nullable String scopeName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getScope() == null) {
      return null;
    }
    return componentInstance.getScope().getName();
  }

  private static @Nullable String scopeName(@Nullable DependencyInstance dependencyInstance) {
    if (dependencyInstance == null || dependencyInstance.getScope() == null) {
      return null;
    }
    return dependencyInstance.getScope().getName();
  }

  private static @Nullable String targetName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getLandingZone() == null) {
      return null;
    }
    return componentInstance.getLandingZone().getTargetName();
  }

  private static @Nullable String landingZoneName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getLandingZone() == null) {
      return null;
    }
    return componentInstance.getLandingZone().getName();
  }

  private static @Nullable String macroPhaseName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getLandingZone() == null) {
      return null;
    }
    return componentInstance.getLandingZone().getMacroPhaseName();
  }

  private static @Nullable String componentName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null) {
      return null;
    }
    return componentInstance.getComponentName();
  }

  private static @Nullable String namespaceName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getNamespace() == null) {
      return null;
    }
    return componentInstance.getNamespace().getName();
  }

  private static @Nullable String regionName(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null
        || componentInstance.getLandingZone() == null
        || componentInstance.getLandingZone().getRegion() == null) {
      return null;
    }
    return componentInstance.getLandingZone().getRegion().getName();
  }

  private static @Nullable String zoneTypeName(
      @Nullable ComponentInstance componentInstance, Direction direction) {
    if (componentInstance == null) {
      return null;
    }
    if (componentInstance.getZoneType() != null) {
      return componentInstance.getZoneType().getName();
    }
    if (direction == Direction.OUT) {
      if (componentInstance.getOutZoneInstance() != null) {
        return componentInstance.getOutZoneInstance().getZoneTypeName();
      }
      if (componentInstance.getOutSubnetInstance() != null
          && componentInstance.getOutSubnetInstance().getZoneInstance() != null) {
        return componentInstance.getOutSubnetInstance().getZoneInstance().getZoneTypeName();
      }
    } else {
      if (componentInstance.getInZoneInstance() != null) {
        return componentInstance.getInZoneInstance().getZoneTypeName();
      }
      if (componentInstance.getInSubnetInstance() != null
          && componentInstance.getInSubnetInstance().getZoneInstance() != null) {
        return componentInstance.getInSubnetInstance().getZoneInstance().getZoneTypeName();
      }
    }
    return null;
  }

  private static @Nullable String inZoneInstanceId(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getInZoneInstance() == null) {
      return null;
    }
    return componentInstance.getInZoneInstance().id();
  }

  private static @Nullable String outZoneInstanceId(@Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getOutZoneInstance() == null) {
      return null;
    }
    return componentInstance.getOutZoneInstance().id();
  }

  private static @Nullable String inSubnetInstanceId(
      @Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getInSubnetInstance() == null) {
      return null;
    }
    return componentInstance.getInSubnetInstance().id();
  }

  private static @Nullable String outSubnetInstanceId(
      @Nullable ComponentInstance componentInstance) {
    if (componentInstance == null || componentInstance.getOutSubnetInstance() == null) {
      return null;
    }
    return componentInstance.getOutSubnetInstance().id();
  }

  private record ConnectivityFilterRequest(
      ConnectivityDetailFilter filterType,
      String objectId,
      String filterValue,
      ConnectivityDetailDirection direction) {

    private boolean matchesByDirection(@Nullable String sourceValue, @Nullable String targetValue) {
      return switch (direction) {
        case BOTH ->
            Objects.equals(sourceValue, filterValue) || Objects.equals(targetValue, filterValue);
        case OUT -> Objects.equals(sourceValue, filterValue);
        case IN -> Objects.equals(targetValue, filterValue);
      };
    }

    private boolean matches(
        ComponentInstance source,
        ComponentInstance destination,
        DependencyInstance depInstance,
        Service service) {
      return switch (filterType) {
        case TARGET -> matchesByDirection(targetName(source), targetName(destination));
        case LANDING_ZONE ->
            matchesByDirection(landingZoneName(source), landingZoneName(destination));
        case SCOPE ->
            Objects.equals(scopeName(source), filterValue)
                || Objects.equals(scopeName(destination), filterValue)
                || Objects.equals(scopeName(depInstance), filterValue);
        case SERVICE -> Objects.equals(service.id(), filterValue);
        case DEPENDENCY -> Objects.equals(depInstance.getDependencyName(), filterValue);
        case DEPENDENCY_INSTANCE -> Objects.equals(depInstance.getName(), filterValue);
        case COMPONENT_INSTANCE -> matchesByDirection(source.getName(), destination.getName());
        case MACRO_PHASE -> matchesByDirection(macroPhaseName(source), macroPhaseName(destination));
        case COMPONENT -> matchesByDirection(componentName(source), componentName(destination));
        case NAMESPACE -> matchesByDirection(namespaceName(source), namespaceName(destination));
        case REGION -> matchesByDirection(regionName(source), regionName(destination));
        case ZONE_TYPE ->
            matchesByDirection(
                zoneTypeName(source, Direction.OUT), zoneTypeName(destination, Direction.IN));
      };
    }
  }

  private static void checkRepository(
      @Nullable ConcurrentRepository<?> repository, String versionNumber, String repoName) {
    if (repository == null || !repository.isReady()) {
      throw new ServiceUnavailableException(
          repoName + " repository is not ready for version: " + versionNumber);
    }
  }
}
