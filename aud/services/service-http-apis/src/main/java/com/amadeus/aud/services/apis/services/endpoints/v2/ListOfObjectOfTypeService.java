package com.amadeus.aud.services.apis.services.endpoints.v2;

import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_NUM;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.services.apis.services.endpoints.v2.PaginationDefaults.MAX_PAGE_SIZE;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class ListOfObjectOfTypeService extends BaseService {

  private final AudService audService;

  @Inject
  public ListOfObjectOfTypeService(AudService audService) {
    this.audService = audService;
  }

  public Uni<Response<Collection<BomElement>>> getObjectTypeResponse(
      String version, String objectType, @Nullable Integer pageNum, @Nullable Integer pageSize) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getObjectTypeResponse(ver, objectType, pageNum, pageSize));
  }

  @CacheResult(cacheName = "getObjectTypeResponse", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response<Collection<BomElement>>> getObjectTypeResponse(
      Version version, String objectType, @Nullable Integer pageNum, @Nullable Integer pageSize) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getResponse(version, objectType, pageNum, pageSize)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private static Response<Collection<BomElement>> getResponse(
      Version version, String objectType, @Nullable Integer pageNum, @Nullable Integer pageSize) {
    ObjectTypeListNamesEnum objectTypeEnum =
        ObjectTypeListNamesEnum.byUri(objectType).orElseThrow(NotFoundException::new);

    ConcurrentRepository<? extends BomElement> repository =
        version.getRepository(objectTypeEnum.getRepositoryKey());

    ConcurrentMap<String, ? extends BomElement> data = repository.getData();

    return getPaginatedResponse(objectTypeEnum, data, pageNum, pageSize);
  }

  private static Response<Collection<BomElement>> getPaginatedResponse(
      ObjectTypeListNamesEnum objectTypeEnum,
      ConcurrentMap<String, ? extends BomElement> data,
      @Nullable Integer pageNum,
      @Nullable Integer pageSize) {
    if (pageSize != null && pageSize < 1) {
      throw new BadRequestException("pageSize must be >= 1");
    }
    if (pageSize != null && pageSize > MAX_PAGE_SIZE) {
      throw new BadRequestException("pageSize must be <= %s".formatted(MAX_PAGE_SIZE));
    }
    if (pageNum != null && pageNum < 1) {
      throw new BadRequestException("pageNum must be >= 1");
    }

    // Apply defaults
    int effectivePageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
    int effectivePageNum = pageNum != null ? pageNum : DEFAULT_PAGE_NUM;

    List<BomElement> allValues = data != null ? new ArrayList<>(data.values()) : new ArrayList<>();

    allValues.sort(Comparator.comparing(WithId::id));

    int totalCount = allValues.size();
    int lastPage = Math.max(1, (int) Math.ceil((double) totalCount / effectivePageSize));
    if (effectivePageNum > lastPage) {
      throw new BadRequestException(
          "pageNum (" + effectivePageNum + ") exceeds last page (" + lastPage + ")");
    }

    int offset = (effectivePageNum - 1) * effectivePageSize;
    Collection<BomElement> values =
        allValues.subList(offset, Math.min(offset + effectivePageSize, totalCount));
    Metadata metadata =
        new Metadata()
            .withInitialColumns(objectTypeEnum.getInitialColumns())
            .withPagination(new Pagination(totalCount, effectivePageNum, effectivePageSize));

    return new Response<>(values, metadata);
  }
}
