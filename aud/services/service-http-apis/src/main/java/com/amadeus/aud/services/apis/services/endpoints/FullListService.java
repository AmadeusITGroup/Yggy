package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.amadeus.aud.services.apis.utils.FullListResponseTransformer;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
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
  private static final String TARGET_TYPE = "targettype";
  private static final String ZONE_INSTANCE = "zoneinstance";

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
        .item(Unchecked.supplier(() -> getResponseJavaFormat(version, objectType, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getResponseJavaFormat(Version version, String objectType, UriInfo uriInfo)
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

  /** Fulllist endpoint aligned with python format */
  public Uni<Response> getFullListPythonFormat(String version, String objectType, UriInfo uriInfo) {
    return getFullListPythonFormat(version, objectType, uriInfo, null);
  }

  public Uni<Response> getFullListPythonFormat(
      String version,
      String objectType,
      UriInfo uriInfo,
      @Nullable MultivaluedMap<String, String> queryParams) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni(
            (Version ver) ->
                getFullListPythonFormatFiltered(ver, objectType, uriInfo, queryParams));
  }

  public Uni<Response> getFullListPythonFormatFiltered(
      Version version,
      String objectType,
      UriInfo uriInfo,
      @Nullable MultivaluedMap<String, String> queryParams) {
    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () -> getResponseFiltered(version, objectType, uriInfo, queryParams)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  private Response getResponseFiltered(
      Version version,
      String objectType,
      UriInfo uriInfo,
      @Nullable MultivaluedMap<String, String> queryParams)
      throws IOException {
    String versionNumber = version.getVersionNumber();

    String javaType = getJavaObjectType(objectType);
    ConcurrentRepository<? extends BomElement> repository = version.getRepository(javaType);
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

    Collection<? extends BomElement> filteredValues =
        filterBomElements(values, javaType, queryParams);
    Collection<?> dataToSerialize = filteredValues;

    List<Map<String, Object>> enrichedData = enrichWithChildArrays(filteredValues, javaType);
    if (enrichedData != null) {
      dataToSerialize = enrichedData;
    }

    byte[] raw =
        objectMapper.writeValueAsBytes(
            new GenericResponse<>(
                audApiConfig.version(),
                versionNumber,
                UrlUtils.getUrl(uriInfo),
                UrlUtils.getVersionedUrl(uriInfo, versionNumber),
                dataToSerialize));

    byte[] transformed = FullListResponseTransformer.transform(objectMapper, raw, javaType);

    return Response.status(Response.Status.OK).entity(transformed).build();
  }

  private Collection<? extends BomElement> filterBomElements(
      Collection<? extends BomElement> items,
      String javaType,
      @Nullable MultivaluedMap<String, String> queryParams)
      throws BadRequestException {

    if (queryParams == null || queryParams.isEmpty()) {
      return items;
    }

    Map<String, List<String>> filters = buildFieldFilters(queryParams);
    Integer[] snowPage = resolveSnowPage(javaType, queryParams);

    validateFilterFields(items, filters);

    List<BomElement> filtered = new ArrayList<>();
    int index = 0;

    for (BomElement item : items) {
      if (matchesAllFilters(item, filters) && isInSnowPage(index, snowPage[0], snowPage[1])) {
        filtered.add(item);
      }
      index++;
    }

    return filtered;
  }

  private void validateFilterFields(
      Collection<? extends BomElement> items, Map<String, List<String>> filters)
      throws BadRequestException {

    if (items.isEmpty()) {
      return;
    }

    BomElement sample = items.iterator().next();

    for (String fieldName : filters.keySet()) {
      if (!fieldExists(sample, fieldName)) {
        throw new BadRequestException(
            "Invalid filter field: '" + fieldName + "' does not exist on this object type.");
      }
    }
  }

  private boolean matchesAllFilters(BomElement item, Map<String, List<String>> filters) {
    for (Map.Entry<String, List<String>> filter : filters.entrySet()) {
      if (!matchesSingleFilter(item, filter.getKey(), filter.getValue())) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesSingleFilter(
      BomElement item, String fieldName, List<String> acceptedValues) {

    Object fieldValue = findFieldValueFromBomElement(item, fieldName);

    if (fieldValue == null) {
      return false;
    }

    String itemValue = extractFilterValue(fieldValue);

    if (itemValue == null) {
      return false;
    }

    for (String accepted : acceptedValues) {
      if (accepted.equalsIgnoreCase(itemValue)) {
        return true;
      }
    }
    return false;
  }

  private @Nullable String extractFilterValue(Object fieldValue) {
    if (fieldValue == null) {
      return null;
    }

    Class<?> clazz = fieldValue.getClass();

    if (!isComplexObject(fieldValue)) {
      return fieldValue.toString();
    }

    if (clazz.isEnum()) {
      String value = invokeGetterIfExists(fieldValue, "getValue");
      if (value != null) {
        return value;
      }
      return ((Enum<?>) fieldValue).name();
    }

    String[] possibleGetters = {"getName", "getValue"};
    for (String getterName : possibleGetters) {
      String value = invokeGetterIfExists(fieldValue, getterName);
      if (value != null) {
        return value;
      }
    }

    return null;
  }

  private @Nullable String invokeGetterIfExists(Object obj, String getterName) {
    try {
      java.lang.reflect.Method getter = findMethodCaseInsensitive(obj.getClass(), getterName);
      if (getter != null) {
        Object result = getter.invoke(obj);
        if (result != null) {
          return result.toString();
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  private boolean fieldExists(BomElement sample, String fieldName) {
    return findGetterMethod(sample, fieldName) != null;
  }

  private @Nullable Object findFieldValueFromBomElement(BomElement item, String fieldName) {
    java.lang.reflect.Method method = findGetterMethod(item, fieldName);
    if (method == null) {
      return null;
    }

    try {
      return method.invoke(item);
    } catch (Exception e) {
      return null;
    }
  }

  private @Nullable java.lang.reflect.Method findGetterMethod(BomElement item, String fieldName) {
    if (item == null) {
      return null;
    }

    try {
      Class<?> itemClass = item.getClass();
      String[] fieldVariants = {fieldName, pythonToJavaFieldName(fieldName), capitalize(fieldName)};

      for (String variant : fieldVariants) {
        String[] getterNames = {"get" + capitalize(variant), "is" + capitalize(variant)};

        for (String getterName : getterNames) {
          java.lang.reflect.Method foundMethod = findMethodCaseInsensitive(itemClass, getterName);
          if (foundMethod != null) {
            return foundMethod;
          }
        }

        Class<?> parent = itemClass.getSuperclass();
        while (parent != null && !parent.equals(Object.class)) {
          for (String getterName : getterNames) {
            java.lang.reflect.Method foundMethod = findMethodCaseInsensitive(parent, getterName);
            if (foundMethod != null) {
              return foundMethod;
            }
          }
          parent = parent.getSuperclass();
        }
      }
    } catch (Exception e) {
    }

    return null;
  }

  private @Nullable Object tryGetterOnClass(BomElement item, Class<?> clazz, String getterName) {
    try {
      java.lang.reflect.Method getter = clazz.getMethod(getterName);
      return getter.invoke(item);
    } catch (Exception e) {
      return null;
    }
  }

  private @Nullable java.lang.reflect.Method findMethodCaseInsensitive(
      Class<?> clazz, String methodName) {
    try {
      for (java.lang.reflect.Method method : clazz.getMethods()) {
        if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == 0) {
          return method;
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  private String getJavaObjectType(String objectType) {
    return pythonToJavaFieldName(objectType.toLowerCase(Locale.ROOT));
  }

  private String pythonToJavaFieldName(String pythonName) {
    if (!pythonName.contains("_")) {
      return pythonName;
    }

    String[] parts = pythonName.split("_");
    StringBuilder javaName = new StringBuilder(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      if (!part.isEmpty()) {
        javaName
            .append(Character.toUpperCase(part.charAt(0)))
            .append(part.substring(1).toLowerCase());
      }
    }
    return javaName.toString();
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  private boolean isComplexObject(Object value) {
    if (value == null) {
      return false;
    }

    Class<?> clazz = value.getClass();
    if (clazz.isPrimitive()
        || clazz == String.class
        || clazz == Boolean.class
        || clazz == Integer.class
        || clazz == Long.class
        || clazz == Float.class
        || clazz == Double.class) {
      return false;
    }

    return true;
  }

  private boolean isInSnowPage(
      int index, @Nullable Integer snowStart, @Nullable Integer snowNumber) {
    if (snowStart == null || snowNumber == null) {
      return true;
    }
    return index >= snowStart && index < snowStart + snowNumber;
  }

  private Integer[] resolveSnowPage(String javaType, MultivaluedMap<String, String> queryParams) {
    if (!SNOW_PAGINATED_TYPES.contains(javaType)) {
      return new Integer[] {null, null};
    }
    return new Integer[] {
      resolveSnowParam(queryParams, SNOW_START_FILTER),
      resolveSnowParam(queryParams, SNOW_NUMBER_FILTER)
    };
  }

  private Map<String, List<String>> buildFieldFilters(MultivaluedMap<String, String> queryParams) {
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
        filters.put(key, values);
      }
    }
    return filters;
  }

  private @Nullable List<Map<String, Object>> enrichWithChildArrays(
      Collection<? extends BomElement> items, String javaType) {
    String lowerCase = javaType.toLowerCase(Locale.ROOT);
    if (!(TARGET_TYPE.equals(lowerCase) || ZONE_INSTANCE.equals(lowerCase))) {
      return null;
    }

    List<Map<String, Object>> enrichedList = new ArrayList<>();

    for (BomElement item : items) {
      Map<String, Object> itemMap =
          objectMapper.convertValue(
              item, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

      switch (lowerCase) {
        case TARGET_TYPE:
          if (item instanceof TargetType tt) {
            List<Map<String, String>> targets = new ArrayList<>();
            if (tt.getTargets() != null) {
              for (Target target : tt.getTargets()) {
                targets.add(target.getSimpleJsonRepresentation());
              }
            }
            itemMap.put("targets", targets);
          }
          break;

        case ZONE_INSTANCE:
          if (item instanceof ZoneInstance zi) {
            List<Map<String, String>> subnets = new ArrayList<>();
            if (zi.getSubnetInstances() != null) {
              for (SubnetInstance subnet : zi.getSubnetInstances()) {
                subnets.add(subnet.getSimpleJsonRepresentation());
              }
            }
            itemMap.put("subnet_instances", subnets);
          }
          break;
      }

      enrichedList.add(itemMap);
    }

    return enrichedList;
  }

  private @Nullable Integer resolveSnowParam(
      MultivaluedMap<String, String> queryParams, String paramName) {
    String val = queryParams.getFirst(paramName);
    if (val == null) {
      return null;
    }
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException e) {
      throw new BadRequestException(paramName + " must be an integer: " + val);
    }
  }
}
