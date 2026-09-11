package com.amadeus.aud.services.apis.services.endpoints.v2;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class ObjectByIdService extends BaseService {
  public final AudService audService;
  public final ObjectUtils objectUtils;

  @Inject
  public ObjectByIdService(AudService audService, ObjectUtils objectUtils) {
    this.audService = audService;
    this.objectUtils = objectUtils;
  }

  public Uni<Response<Object>> getObjectByIdResponse(
      String version, String objectType, String id, boolean rawData) {
    ObjectTypeListNamesEnum objectTypeEnum =
        ObjectTypeListNamesEnum.byUri(objectType)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        "Unknown object type: "
                            + objectType
                            + ". Known types: "
                            + ObjectTypeListNamesEnum.knownRepositoryKeys()));

    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getObjectByIdResponse(ver, objectTypeEnum, id, rawData));
  }

  @CacheResult(cacheName = "getObjectByIdResponse", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response<Object>> getObjectByIdResponse(
      Version version, ObjectTypeListNamesEnum objectTypeEnum, String id, boolean rawData) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getResponse(version, objectTypeEnum, id, rawData)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response<Object> getResponse(
      Version version, ObjectTypeListNamesEnum objectTypeEnum, String id, boolean rawData) {
    ConcurrentRepository<? extends BomElement> repository =
        version.getRepository(objectTypeEnum.getRepositoryKey());
    if (repository == null) {
      throw new NotFoundException("No data for object type: " + objectTypeEnum.getUri());
    }

    ConcurrentMap<String, ? extends BomElement> data = repository.getData();

    Collection<? extends BomElement> values = data != null ? data.values() : new ArrayList<>();

    int count = 0;
    Response<Object> output = null;
    for (BomElement element : values) {
      if (id.equals(element.id())) {
        ++count;
        if (output == null) {
          if (rawData) {
            if (element instanceof AbstractBase<?> baseElement) {
              output = new Response<>(baseElement.getData(), new Metadata());
            } else {
              output = new Response<>(null, new Metadata());
            }
          } else {
            output =
                new Response<>(
                    element,
                    new Metadata().withLinks(this.objectUtils.links(element, objectTypeEnum)));
          }
        }
      }
    }
    if (count > 1) {
      // If this happens, id() method for this specific object type is not correct.
      throw new IllegalStateException(
          "Multiple objects found with id: " + id + " in object type: " + objectTypeEnum);
    }
    if (output == null) {
      throw new NotFoundException("No object found with id: " + id);
    }
    return output;
  }
}
