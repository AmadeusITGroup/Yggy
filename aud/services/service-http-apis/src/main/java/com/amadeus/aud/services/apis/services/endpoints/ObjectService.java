package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Singleton
public class ObjectService extends BaseService {
  private final AudService audService;
  private final ModelsService modelsService;
  private final ObjectMapper objectMapper;
  private final AudApiConfig audApiConfig;

  @Inject
  public ObjectService(
      AudService audService,
      ModelsService modelsService,
      ObjectMapper objectMapper,
      AudApiConfig audApiConfig) {
    this.audApiConfig = audApiConfig;
    this.objectMapper = objectMapper;
    this.audService = audService;
    this.modelsService = modelsService;
  }

  @CacheResult(cacheName = "getObject", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> get(String version, String objectType, String name, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni(
            (Version ver) ->
                Uni.createFrom()
                    .item(Unchecked.supplier(() -> getResponse(ver, objectType, name, uriInfo)))
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                    .emitOn(Infrastructure.getDefaultWorkerPool()));
  }

  private Response getResponse(Version version, String objectType, String name, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    List<JsonNode> values =
        modelsService.generateResponse(version, objectType, List.of(), uriInfo, Integer.MAX_VALUE);
    JsonNode matchingValue = null;
    for (JsonNode value : values) {
      JsonNode nameNode = value.get("name");
      if (nameNode != null && name.equals(nameNode.asText())) {
        matchingValue = value;
        break;
      }
    }

    if (matchingValue == null) {
      throw new NotFoundException("Name query parameter doesn't match any object");
    }

    return Response.status(Response.Status.OK)
        .entity(
            objectMapper.writeValueAsBytes(
                Map.of(
                    "apiVersion",
                    audApiConfig.version(),
                    "version",
                    versionNumber,
                    "url",
                    UrlUtils.getUrl(uriInfo),
                    "urlVersion",
                    UrlUtils.getVersionedUrl(uriInfo, versionNumber),
                    objectType,
                    matchingValue)))
        .build();
  }
}
