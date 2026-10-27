package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.yaml.models.Version;
import com.amadeus.aud.services.apis.models.http.requests.VersionLoadRequest;
import com.amadeus.aud.services.apis.models.http.responses.VersionsResponse;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// Service responsible for managing version-related operations. Provides functionality to retrieve
/// current version information, list available versions, and trigger loading of specific versions.
@ApplicationScoped
public class VersionService extends BaseService {

  private final AudService audService;
  private final AudApiConfig audApiConfig;

  private final VersionCacheProducer versionCacheProducer;
  private final GraphEngineService graphEngineService;

  @Inject
  public VersionService(
      AudService audService,
      AudApiConfig audApiConfig,
      VersionCacheProducer versionCacheProducer,
      GraphEngineService graphEngineService) {
    this.audApiConfig = audApiConfig;
    this.versionCacheProducer = versionCacheProducer;
    this.graphEngineService = graphEngineService;
    this.audService = audService;
  }

  private Version createVersionResponseObject(String versionNumber) {
    Version responseVersion = new Version();
    responseVersion.setRequirementsVersion(versionNumber);
    responseVersion.setApiVersion(audApiConfig.version());
    responseVersion.setPlatform(audApiConfig.platform());
    return responseVersion;
  }

  private Version createVersionResponseObject(
      com.amadeus.aud.libraries.engine.data.models.core.internals.Version version) {
    Version responseVersion = new Version();
    responseVersion.setRequirementsVersion(version.getVersionNumber());
    AudRequirementsRepository audRequirementsRepository = version.getAudRequirementsRepository();
    if (audRequirementsRepository != null) {
      responseVersion.setRequirementsMd5(audRequirementsRepository.getDataMD5Hash());
    }
    responseVersion.setApiVersion(audApiConfig.version());
    responseVersion.setPlatform(audApiConfig.platform());
    return responseVersion;
  }

  public @Nullable Version getLatestVersion() {
    com.amadeus.aud.libraries.engine.data.models.core.internals.Version latestVersion =
        audService.getCurrentVersion().await().indefinitely();
    if (latestVersion == null) {
      Log.error("No latest version found.");
      return null;
    }
    return createVersionResponseObject(latestVersion);
  }

  public Optional<Version> maybeGetLatestVersion() {
    com.amadeus.aud.libraries.engine.data.models.core.internals.Version latestVersion =
        audService.getCurrentVersion().await().indefinitely();
    if (latestVersion == null) {
      Log.error("No latest version found.");
      return Optional.empty();
    }
    return Optional.of(createVersionResponseObject(latestVersion));
  }

  public Uni<Version> getVersion(@Nullable String versionNumber) {
    Uni<com.amadeus.aud.libraries.engine.data.models.core.internals.Version> versionUni;
    if (versionNumber == null || versionNumber.isEmpty()) {
      versionUni = audService.getVersion();
    } else {
      versionUni = audService.getVersion(versionNumber);
    }
    return versionUni
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transform(this::createVersionResponseObject);
  }

  public Uni<Response> getVersion(@Nullable VersionLoadRequest versionLoadRequest) {
    String versionToLoad = versionLoadRequest != null ? versionLoadRequest.version() : null;
    return getVersion(versionToLoad)
        .onItem()
        .transform(
            Unchecked.function(
                (Version version) -> {
                  if (version == null) {
                    throw new NotFoundException();
                  }
                  return Response.ok(version).build();
                }))
        .onFailure()
        .invoke((Throwable e) -> Log.warnf("Failed to load version: %s", e.getMessage()));
  }

  /// Retrieves the current version information asynchronously.
  ///
  /// @return A Uni containing a Version object with current version information
  public Uni<Version> getLatestVersionUni() {
    return Uni.createFrom()
        .item(Unchecked.supplier(this::getLatestVersion))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  public Uni<Response> getLoadedVersions(UriInfo uriInfo) {
    List<Version> modelsVersions = new ArrayList<>();
    List<Version> graphVersions = new ArrayList<>();

    LoadingCache<String, com.amadeus.aud.libraries.engine.data.models.core.internals.Version>
        versionCache = versionCacheProducer.getCache();
    for (String key : versionCache.asMap().keySet()) {
      com.amadeus.aud.libraries.engine.data.models.core.internals.Version version =
          versionCache.get(key);
      if (version != null) {
        modelsVersions.add(createVersionResponseObject(version));
      }
    }

    for (String key : graphEngineService.getAvailableVersionNumbers()) {
      graphVersions.add(createVersionResponseObject(key));
    }

    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () ->
                    getOkResponse(
                        new VersionsResponse(
                            UrlUtils.getUrl(uriInfo),
                            audApiConfig.version(),
                            modelsVersions,
                            graphVersions))))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }
}
