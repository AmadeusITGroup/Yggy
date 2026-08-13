package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;

@Singleton
public class ModelsService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;
  private final ObjectMapper objectMapper;

  @Inject
  public ModelsService(
      AudService audService, AudApiConfig audApiConfig, ObjectMapper objectMapper) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
    this.objectMapper = objectMapper;
  }

  public Uni<Response> get(
      String objectType, String version, UriInfo uriInfo, List<String> select, int limit) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni(
            (Version ver) ->
                Uni.createFrom()
                    .item(
                        Unchecked.supplier(
                            () -> getResponse(ver, objectType, select, uriInfo, limit)))
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool()));
  }

  private static Map<String, Set<String>> getConditions(UriInfo uriInfo) {
    final MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();

    final Map<String, Set<String>> conditions = new HashMap<>();
    for (final Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith("where[") && key.endsWith("]")) {
        String innerKey = key.substring("where[".length(), key.length() - 1);
        conditions.put(innerKey, new HashSet<>(entry.getValue()));
      }
    }
    return conditions;
  }

  List<JsonNode> generateResponse(
      Version version, String objectType, List<String> select, UriInfo uriInfo, int limit) {
    if (limit <= 0) {
      throw new BadRequestException("limit must be a positive integer");
    }
    if (version == null) {
      throw new ServiceUnavailableException();
    }

    ConcurrentRepository<? extends BomElement> repository = version.getRepository(objectType);
    if (repository == null) {
      handleUnknownType(version);
    }

    final Map<String, Set<String>> conditions = getConditions(uriInfo);

    List<JsonNode> values = new ArrayList<>(Math.min(limit, repository.getData().size()));
    for (BomElement value : repository.getData().values()) {
      JsonNode node = objectMapper.valueToTree(value);
      if (isNodeKept(node, conditions)) {
        values.add(getJsonNodeWithFields(node, select));
      }
      if (values.size() >= limit) {
        break;
      }
    }
    return values;
  }

  private record TypeAndFields(String type, List<String> fields) {}

  private void handleUnknownType(Version version) {
    List<String> types = new ArrayList<>(version.getRepositoryNames());
    types.sort(Comparator.naturalOrder());
    StringBuilder buffer =
        new StringBuilder()
            .append("Unknown object type. Here are the known types and their fields:");
    for (String type : types) {
      // Streams are used here by exception: this diagnostic helper is slated for
      // removal and is not on any hot path.
      Class<? extends BomElement> typeClass =
          version.getRepository(type).getData().values().stream()
              .findFirst()
              .orElse(new ApplicationId())
              .getClass();
      List<String> fields = getSerializableFields(typeClass);
      var datum = new TypeAndFields(type, fields);
      try {
        buffer.append("\n").append(this.objectMapper.writeValueAsString(datum));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
    throw new NotFoundException(buffer.toString());
  }

  private List<String> getSerializableFields(Class<?> type) {
    // Streams are used here by exception: this diagnostic helper is slated for
    // removal and is not on any hot path.
    return objectMapper
        .getSerializationConfig()
        .introspect(objectMapper.getTypeFactory().constructType(type))
        .findProperties()
        .stream()
        .map(BeanPropertyDefinition::getName)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  public Response getResponse(
      Version version, String objectType, List<String> select, UriInfo uriInfo, int limit)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();
    List<JsonNode> values = generateResponse(version, objectType, select, uriInfo, limit);
    byte[] body =
        objectMapper.writeValueAsBytes(
            new GenericResponse<>(
                audApiConfig.version(),
                versionNumber,
                UrlUtils.getUrl(uriInfo),
                UrlUtils.getVersionedUrl(uriInfo, versionNumber),
                values));

    return Response.ok(body).type(MediaType.APPLICATION_JSON).build();
  }

  private JsonNode getJsonNodeWithFields(JsonNode node, List<String> select) {
    if (select.isEmpty()) {
      return node;
    }

    ObjectNode filteredNode = objectMapper.createObjectNode();
    for (String field : select) {
      if (node.has(field)) {
        filteredNode.set(field, node.get(field));
      }
    }

    return filteredNode;
  }

  private static boolean isNodeKept(JsonNode node, Map<String, Set<String>> conditions) {
    if (conditions == null || conditions.isEmpty()) {
      return true;
    }
    // where[fieldName]=value1&where[fieldName]=value2 means name=value1 AND name=value2
    for (Map.Entry<String, Set<String>> condition : conditions.entrySet()) {
      if (!matchesCondition(node, condition.getKey(), condition.getValue())) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchesCondition(JsonNode node, String fieldName, Set<String> patterns) {
    if (!node.has(fieldName)) {
      return false;
    }

    String fieldValue = node.get(fieldName).asText();
    for (String pattern : patterns) {
      if (FilenameUtils.wildcardMatch(fieldValue, pattern)) {
        return true;
      }
    }
    return false;
  }
}
