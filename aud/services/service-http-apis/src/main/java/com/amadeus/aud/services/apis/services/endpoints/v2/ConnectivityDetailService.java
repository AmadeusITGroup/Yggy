package com.amadeus.aud.services.apis.services.endpoints.v2;

import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_NUM;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.MAX_PAGE_SIZE;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.common.AudCommonConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.services.apis.services.endpoints.VersionGraphCacheService;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Singleton
public class ConnectivityDetailService extends BaseService {

  private static final List<String> INITIAL_COLUMNS =
      List.of("name", "source", "target", "dependency_instance", "service", "pci");

  private static final Comparator<Link> LINK_COMPARATOR =
      Comparator.comparing((Link link) -> link.dependencyInstance().getName())
          .thenComparing(link -> link.source().getName())
          .thenComparing(link -> link.target().getName())
          .thenComparing(link -> link.service().getName());

  private final AudService audService;
  private final VersionGraphCacheService versionGraphCacheService;
  private final AudCommonConfig audCommonConfig;

  @Inject
  public ConnectivityDetailService(
      AudService audService,
      VersionGraphCacheService versionGraphCacheService,
      AudCommonConfig audCommonConfig) {
    this.audService = audService;
    this.versionGraphCacheService = versionGraphCacheService;
    this.audCommonConfig = audCommonConfig;
  }

  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      String version,
      String objectType,
      @Nullable String objectId,
      @Nullable String direction,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    return getConnectivityDetailResponse(
        version, objectType, objectId, direction, null, pageNum, pageSize);
  }

  public Uni<Response<List<ConnectivityDetailItem>>> getConnectivityDetailResponse(
      String version,
      String objectType,
      @Nullable String objectId,
      @Nullable String direction,
      @Nullable Boolean pci,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    ConnectivityDetailFilter filter = resolveFilter(objectType);
    ConnectivityDetailDirection connectivityDirection =
        ConnectivityDetailDirection.fromQueryValue(direction);
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni(
            resolvedVersion ->
                getConnectivityDetailResponse(
                    resolvedVersion,
                    filter,
                    objectId,
                    connectivityDirection,
                    pci,
                    pageNum,
                    pageSize));
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
    ConnectivityDetailFilter filter = ConnectivityDetailFilter.fromObjectTypeUri(objectType);
    if (filter == null) {
      throw new BadRequestException("Unsupported connectivity object type: " + objectType);
    }
    return filter;
  }

  private ConnectivityFilterRequest resolveFilterRequest(
      Version version,
      ConnectivityDetailFilter filter,
      @Nullable String objectId,
      ConnectivityDetailDirection direction) {
    validateDirection(filter, direction);
    String normalizedObjectId = normalizeObjectId(objectId);
    String filterValue = resolveFilterValue(version, filter, normalizedObjectId);
    return new ConnectivityFilterRequest(filter, filterValue, direction);
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
    Graph graph = versionGraphCacheService.buildVersionGraph(version);

    List<Link> filteredLinks = new ArrayList<>();
    for (Edge edge : graph.getEdges().values()) {
      Link link = edge.getLink();
      if (link != null && filterRequest.matches(link) && (!pciOnly || isPci(link))) {
        filteredLinks.add(link);
      }
    }
    return filteredLinks;
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
        isPci(link));
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

  private boolean isPci(Link link) {
    return isPciSource(link.source()) || isPciTarget(link.target());
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
    return componentInstance.getInternalZoneTypeNames(direction);
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

    private boolean matches(Link link) {
      return switch (filterType) {
        case TARGET -> matchesByDirection(targetName(link.source()), targetName(link.target()));
        case LANDING_ZONE ->
            matchesByDirection(landingZoneName(link.source()), landingZoneName(link.target()));
        case SCOPE ->
            Objects.equals(scopeName(link.source()), filterValue)
                || Objects.equals(scopeName(link.target()), filterValue)
                || Objects.equals(scopeName(link.dependencyInstance()), filterValue);
        case SERVICE -> Objects.equals(link.service().id(), filterValue);
        case DEPENDENCY ->
            Objects.equals(link.dependencyInstance().getDependencyName(), filterValue);
        case DEPENDENCY_INSTANCE ->
            Objects.equals(link.dependencyInstance().getName(), filterValue);
        case COMPONENT_INSTANCE ->
            matchesByDirection(link.source().getName(), link.target().getName());
        case MACRO_PHASE ->
            matchesByDirection(macroPhaseName(link.source()), macroPhaseName(link.target()));
        case COMPONENT ->
            matchesByDirection(componentName(link.source()), componentName(link.target()));
        case NAMESPACE ->
            matchesByDirection(namespaceName(link.source()), namespaceName(link.target()));
        case REGION -> matchesByDirection(regionName(link.source()), regionName(link.target()));
        case ZONE_TYPE ->
            matchesByDirection(
                zoneTypeName(link.source(), Direction.OUT),
                zoneTypeName(link.target(), Direction.IN));
        case ZONE_INSTANCE ->
            matchesByDirection(outZoneInstanceId(link.source()), inZoneInstanceId(link.target()));
        case SUBNET_INSTANCE ->
            matchesByDirection(
                outSubnetInstanceId(link.source()), inSubnetInstanceId(link.target()));
      };
    }
  }
}
