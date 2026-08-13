package com.amadeus.aud.services.engine.services.core;

import com.amadeus.aud.libraries.engine.data.producers.VersionCacheProducer;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import com.amadeus.aud.libraries.engine.graph.services.storage.SimpleGraphVersion;
import com.azure.storage.blob.models.BlobStorageException;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheManager;
import io.quarkus.cache.CompositeCacheKey;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.smallrye.mutiny.subscription.UniEmitter;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class GraphCoreService {
  private static final String GRAPH_INIT_CACHE_NAME = "graph-init-cache";

  private record InitTask(String versionNumber, UniEmitter<? super GraphVersion> emitter) {}

  private record InitializationHandle(Uni<GraphVersion> uni, boolean created) {}

  private final GraphEngineService graphDbService;
  private final AudVersionsService audVersionsService;
  private final VersionCacheProducer versionCacheProducer;
  private final CacheManager cacheManager;
  private final Optional<String> forceVersion;
  private final ConcurrentHashMap<String, Uni<GraphVersion>> inProgressInitializations =
      new ConcurrentHashMap<>();
  private final UnicastProcessor<InitTask> initializationQueue = UnicastProcessor.create();

  @Inject
  public GraphCoreService(
      AudVersionsService audVersionsService,
      GraphEngineService graphDbServiceInstance,
      VersionCacheProducer versionCacheProducer,
      CacheManager cacheManager,
      @ConfigProperty(name = "graph.init.force-version") Optional<String> forceVersion) {
    this.audVersionsService = audVersionsService;
    this.graphDbService = graphDbServiceInstance;
    this.versionCacheProducer = versionCacheProducer;
    this.cacheManager = cacheManager;
    this.forceVersion = forceVersion;
  }

  @PostConstruct
  void startInitializationWorker() {
    initializationQueue
        .onItem()
        .transformToUniAndConcatenate(this::runTask)
        .subscribe()
        .with(
            (Void ignored) -> {},
            failure -> Log.errorf(failure, "Graph initialization worker terminated unexpectedly"));
  }

  public @Nullable GraphVersion getVersion(@Nullable String versionId) {
    String versionNumber = resolveVersionNumber(versionId);

    if (versionNumber == null || versionNumber.trim().isEmpty()) {
      return null;
    }

    if (graphDbService.isReady(versionNumber)) {
      Log.debugf("Found existing graph for version %s", versionNumber);
      return buildGraphVersion(versionNumber);
    }

    return getOrStartInitialization(versionNumber).uni().await().indefinitely();
  }

  public InitRequestStatus requestVersionInitialization(@Nullable String versionId) {
    String versionNumber = resolveVersionNumber(versionId);
    if (versionNumber == null || versionNumber.trim().isEmpty()) {
      return InitRequestStatus.INVALID_VERSION;
    }

    if (graphDbService.isReady(versionNumber)) {
      return InitRequestStatus.ALREADY_READY;
    }

    InitializationHandle initialization = getOrStartInitialization(versionNumber);

    if (initialization.created()) {
      initialization
          .uni()
          .subscribe()
          .with(
              ignored -> Log.infof("Graph initialization finished for version %s", versionNumber),
              failure ->
                  Log.errorf(
                      failure,
                      "Graph initialization failed for version %s: %s",
                      versionNumber,
                      failure.getMessage()));
      return InitRequestStatus.QUEUED;
    }

    return InitRequestStatus.ALREADY_IN_PROGRESS;
  }

  public InitStatus getVersionInitializationStatus(@Nullable String versionId) {
    String versionNumber = resolveVersionNumber(versionId);
    if (versionNumber == null || versionNumber.trim().isEmpty()) {
      return InitStatus.INVALID_VERSION;
    }

    if (graphDbService.isReady(versionNumber)) {
      return InitStatus.READY;
    }

    if (inProgressInitializations.containsKey(versionNumber)) {
      return InitStatus.IN_PROGRESS;
    }

    return InitStatus.NOT_READY;
  }

  private InitializationHandle getOrStartInitialization(String versionNumber) {
    AtomicBoolean created = new AtomicBoolean(false);
    Uni<GraphVersion> initialization =
        inProgressInitializations.computeIfAbsent(
            versionNumber,
            (String version) -> {
              created.set(true);
              return createQueuedInitialization(version);
            });

    return new InitializationHandle(initialization, created.get());
  }

  private Uni<GraphVersion> createQueuedInitialization(String versionNumber) {
    return Uni.createFrom()
        .<GraphVersion>emitter(
            emitter -> initializationQueue.onNext(new InitTask(versionNumber, emitter)))
        .memoize()
        .indefinitely()
        .onItemOrFailure()
        .invoke((item, failure) -> inProgressInitializations.remove(versionNumber));
  }

  private Uni<Void> runTask(InitTask task) {
    Log.debugf("Acquired initialization slot for version %s", task.versionNumber());
    return initializeAndGetGraphAsync(task.versionNumber())
        .onItemOrFailure()
        .invoke(
            (GraphVersion version, Throwable failure) -> {
              if (failure != null) {
                task.emitter().fail(failure);
              } else {
                task.emitter().complete(version);
              }
            })
        .onFailure()
        .recoverWithNull()
        .replaceWithVoid();
  }

  /**
   * Builds a lightweight {@link GraphVersion} from persisted statistics for an already-ready
   * version. Uses the current timestamp as the creation time since the new storage API does not
   * expose per-version metadata directly.
   */
  private @Nullable GraphVersion buildGraphVersion(String versionNumber) {
    GraphStatistics stats = graphDbService.getGraphStatistics(versionNumber).await().indefinitely();
    if (stats == null) {
      Log.warnf("No statistics found for version %s, cannot build GraphVersion", versionNumber);
      return null;
    }
    return new SimpleGraphVersion(versionNumber, LocalDateTime.now(Clock.systemUTC()), stats);
  }

  private String resolveVersionNumber(@Nullable String versionId) {
    if ("latest".equalsIgnoreCase(versionId)) {
      return audVersionsService
          .getLatestVersion()
          .onFailure()
          .invoke(
              failure ->
                  Log.errorf(
                      failure,
                      "Failed to retrieve latest version from storage account: %s",
                      failure.getMessage()))
          .await()
          .indefinitely();
    }
    return versionId;
  }

  /**
   * Asynchronously initializes and retrieves a graph version. Uses Uni to maintain compatibility
   * with reactive streams. Multiple concurrent requests for the same version will share the same
   * initialization.
   */
  private Uni<GraphVersion> initializeAndGetGraphAsync(String versionNumber) {
    Instant initStartTime = Instant.now();

    return Uni.createFrom()
        .item(
            () -> {
              Log.debugf("Starting async initialization for version %s", versionNumber);
              return versionNumber;
            })
        .chain(graphDbService::init)
        .chain(
            (Void unused) -> {
              if (!graphDbService.isReady(versionNumber)) {
                Log.errorf("Graph initialization failed for version %s", versionNumber);
                return Uni.createFrom().nullItem();
              }

              Log.infof(
                  "Graph initialization completed for version %s in %s",
                  versionNumber, Duration.between(initStartTime, Instant.now()));

              invalidateCaches(versionNumber);
              return Uni.createFrom().item(() -> buildGraphVersion(versionNumber));
            })
        .onFailure(BlobStorageException.class)
        .recoverWithItem(
            (BlobStorageException e) -> {
              if (e.getStatusCode() == 404) {
                Log.warnf(
                    "Version %s not found in blob storage (BlobNotFound), treating as non-existent",
                    versionNumber);
                return null;
              }
              throw e;
            });
  }

  /** Invalidates both version and graph-init caches after successful initialization. */
  private void invalidateCaches(String versionNumber) {
    try {
      Log.debugf("Invalidating caches for version %s", versionNumber);
      versionCacheProducer.getCache().invalidate(versionNumber);
      cacheManager
          .getCache(GRAPH_INIT_CACHE_NAME)
          .ifPresent(
              (Cache cache) -> {
                Object cacheKey = new CompositeCacheKey("init", versionNumber);
                cache.invalidate(cacheKey).subscribe().with((Void unused) -> {});
              });
    } catch (Exception e) {
      Log.warnf(e, "Failed to invalidate caches for version %s", versionNumber);
    }
  }

  public void checkForNewVersion() {
    if (forceVersion.isPresent()) {
      String forced = forceVersion.get();
      Log.infof("graph.init.force-version=%s — loading forced version", forced);
      getVersion(forced);
      return;
    }

    Set<String> cachedVersions = graphDbService.getAvailableVersionNumbers();
    String latestCached = cachedVersions.stream().max(Comparator.naturalOrder()).orElse(null);

    String newVersion = audVersionsService.checkNewVersion(latestCached).await().indefinitely();
    if (newVersion != null) {
      getVersion(newVersion);
    }
  }
}
