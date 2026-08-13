package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.amadeus.aud.services.apis.utils.FullListResponseTransformer;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class FullListService extends BaseService {
  private static final String SNOW_START_FILTER = "SNOWStartFilter";
  private static final String SNOW_NUMBER_FILTER = "SNOWNumberFilter";
  private static final String SNOW_FILTER = "SNOWFilter";

  /**
   * Object types that support SNOW offset/limit pagination via SNOWStartFilter+SNOWNumberFilter.
   */
  private static final java.util.Set<String> SNOW_PAGINATED_TYPES =
      java.util.Set.of("service", "component");

  private final AudService audService;
  private final AudApiConfig audApiConfig;
  private ObjectMapper objectMapper;

  @Inject
  public FullListService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  @Inject
  void injectObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.objectMapper.findAndRegisterModules();
  }

  /** SDN Fulllist endpoint not aligned with python */
  public Uni<Response> getFullListJavaFormat(String version, String objectType, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getFullListJavaFormat(ver, objectType, uriInfo));
  }

  @CacheResult(cacheName = "getFullListJavaFormat", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getFullListJavaFormat(Version version, String objectType, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getResponseDeprecated(version, objectType, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getResponseDeprecated(Version version, String objectType, UriInfo uriInfo)
      throws JsonProcessingException {

    ConcurrentRepository<? extends BomElement> repository =
        version.getRepository(objectType.toLowerCase(Locale.ROOT));
    if (repository == null) {
      throw new NotFoundException();
    }

    ConcurrentMap<String, ? extends BomElement> data = repository.getData();

    Collection<? extends BomElement> values;
    if (data == null) {
      values = new ArrayList<>();
    } else {
      values = data.values();
    }

    String versionNumber = version.getVersionNumber();

    return getOkResponse(
        new GenericResponse<>(
            audApiConfig.version(),
            versionNumber,
            UrlUtils.getUrl(uriInfo),
            UrlUtils.getVersionedUrl(uriInfo, versionNumber),
            values));
  }

  /** Fulllist endpoint aligned with python */
  public Uni<Response> getFullList(String version, String objectType, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getFullList(ver, objectType, uriInfo));
  }

  @CacheResult(cacheName = "getFullList", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getFullList(Version version, String objectType, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getResponse(version, objectType, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getResponse(Version version, String objectType, UriInfo uriInfo)
      throws IOException {
    String versionNumber = version.getVersionNumber();

    ConcurrentRepository<? extends BomElement> repository =
        version.getRepository(objectType.toLowerCase(Locale.ROOT));
    if (repository == null) {
      throw new NotFoundException();
    }

    ConcurrentMap<String, ? extends BomElement> data = repository.getData();

    Collection<? extends BomElement> values;
    if (data == null) {
      values = new ArrayList<>();
    } else {
      values = data.values();
    }

    // Serialize via existing pipeline (applies all @JacksonMixin annotations)
    byte[] raw =
        objectMapper.writeValueAsBytes(
            new GenericResponse<>(
                audApiConfig.version(),
                versionNumber,
                UrlUtils.getUrl(uriInfo),
                UrlUtils.getVersionedUrl(uriInfo, versionNumber),
                values));

    byte[] transformed = FullListResponseTransformer.transform(objectMapper, raw, objectType);

    return Response.status(Response.Status.OK).entity(transformed).build();
  }

  /**
   * Applies query parameter filtering on a cached full-list response. Called AFTER the cached
   * method so the cache always holds the unfiltered result.
   *
   * <p>Each query parameter is treated as a field filter: {@code ?endpoint_type=ZonedPaaS} keeps
   * only items whose {@code endpoint_type} field equals {@code "ZonedPaaS"}. Multiple parameters
   * are ANDed. Unknown / complex (nested) fields are ignored.
   */
  public Response applyQueryParamFilter(
      Response cachedResponse,
      String objectType,
      @Nullable MultivaluedMap<String, String> queryParams) {
    if (queryParams == null || queryParams.isEmpty()) {
      return cachedResponse;
    }
    try {
      return buildFilteredResponse(cachedResponse, objectType, queryParams);
    } catch (IOException e) {
      return cachedResponse;
    }
  }

  private Response buildFilteredResponse(
      Response cachedResponse, String objectType, MultivaluedMap<String, String> queryParams)
      throws IOException {
    byte[] bytes = (byte[]) cachedResponse.getEntity();
    JsonNode root = objectMapper.readTree(bytes);
    if (!(root instanceof ObjectNode responseNode)) {
      return cachedResponse;
    }

    String typeKey = objectType.toLowerCase(Locale.ROOT).replace("instance", "_instance");
    JsonNode itemsNode = responseNode.get(typeKey);
    if (!(itemsNode instanceof ArrayNode itemsArray)) {
      return cachedResponse;
    }

    Integer[] snowPage = resolveSnowPage(objectType, queryParams);
    Map<String, List<String>> filters = buildFieldFilters(queryParams);
    ArrayNode filtered = filterItems(itemsArray, filters, snowPage[0], snowPage[1]);

    responseNode.set(typeKey, filtered);
    byte[] filteredBytes = objectMapper.writeValueAsBytes(responseNode);
    return Response.status(Response.Status.OK).entity(filteredBytes).build();
  }

  private static Integer[] resolveSnowPage(
      String objectType, MultivaluedMap<String, String> queryParams) {
    if (!SNOW_PAGINATED_TYPES.contains(objectType.toLowerCase(Locale.ROOT))) {
      return new Integer[] {null, null};
    }
    return new Integer[] {
      resolveSnowParam(queryParams, SNOW_START_FILTER),
      resolveSnowParam(queryParams, SNOW_NUMBER_FILTER)
    };
  }

  /** Collects field-level filter criteria, excluding SNOW pagination params. */
  private static Map<String, List<String>> buildFieldFilters(
      MultivaluedMap<String, String> queryParams) {
    Map<String, List<String>> filters = new java.util.LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      if (SNOW_START_FILTER.equals(key)
          || SNOW_NUMBER_FILTER.equals(key)
          || SNOW_FILTER.equals(key)) {
        continue;
      }
      List<String> values = entry.getValue();
      if (values != null && !values.isEmpty()) {
        filters.put(key.toLowerCase(Locale.ROOT), values);
      }
    }
    return filters;
  }

  private ArrayNode filterItems(
      ArrayNode itemsArray,
      Map<String, List<String>> filters,
      Integer snowStart,
      Integer snowNumber) {
    ArrayNode filtered = objectMapper.createArrayNode();
    int index = 0;
    for (JsonNode item : itemsArray) {
      if (matchesAllFilters(item, filters) && isInSnowPage(index, snowStart, snowNumber)) {
        filtered.add(item);
      }
      index++;
    }
    return filtered;
  }

  private static Integer resolveSnowParam(
      MultivaluedMap<String, String> queryParams, String paramName) {
    String val = queryParams.getFirst(paramName);
    return val != null ? Integer.parseInt(val) : null;
  }

  private static boolean isInSnowPage(int index, Integer snowStart, Integer snowNumber) {
    if (snowStart == null || snowNumber == null) {
      return true;
    }
    return index >= snowStart && index < snowStart + snowNumber;
  }

  private static boolean matchesAllFilters(JsonNode item, Map<String, List<String>> filters) {
    for (Map.Entry<String, List<String>> filter : filters.entrySet()) {
      if (!matchesSingleFilter(item, filter.getKey(), filter.getValue())) {
        return false;
      }
    }
    return true;
  }

  /** Returns false if the item fails the given single-field filter, true otherwise. */
  private static boolean matchesSingleFilter(
      JsonNode item, String fieldName, List<String> acceptedValues) {
    JsonNode fieldValue = findFieldValue(item, fieldName);

    if (fieldValue == null || fieldValue.isNull()) {
      return false;
    }
    // Skip complex objects — they are not filterable
    if (fieldValue.isObject() || fieldValue.isArray()) {
      return true;
    }

    String itemValue = fieldValue.asText();
    return acceptedValues.stream().anyMatch(accepted -> accepted.equalsIgnoreCase(itemValue));
  }

  /** Finds a field in a JsonNode by case-insensitive name, or returns null if not found. */
  private static @Nullable JsonNode findFieldValue(JsonNode item, String fieldName) {
    java.util.Iterator<Map.Entry<String, JsonNode>> fields = item.properties().iterator();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      if (field.getKey().toLowerCase(Locale.ROOT).equals(fieldName)) {
        return field.getValue();
      }
    }
    return null;
  }
}
