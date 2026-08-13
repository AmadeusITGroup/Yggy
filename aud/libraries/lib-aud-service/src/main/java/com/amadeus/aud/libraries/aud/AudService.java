package com.amadeus.aud.libraries.aud;

import com.amadeus.aud.libraries.engine.data.DataEngine;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.configs.GraphEngineConfig;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;

@Singleton
@Startup
public class AudService {
  private final GraphEngineService graphEngineService;
  private final DataEngine dataEngine;
  private final GraphEngineConfig graphEngineConfig;

  @Inject
  public AudService(
      GraphEngineService graphEngineService,
      DataEngine dataEngine,
      GraphEngineConfig graphEngineConfig) {
    this.graphEngineService = graphEngineService;
    this.dataEngine = dataEngine;
    this.graphEngineConfig = graphEngineConfig;
  }

  public Uni<Version> getCurrentVersion() {
    return dataEngine
        .getCurrentVersion()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  /// Initializes AUD service with the specified version or latest if none specified. Returns a Uni
  /// that can be used to chain operations or wait for completion.
  ///
  /// @param versionId The version Id to initialize, or null for the latest
  /// @return A Uni that completes when initialization is done, containing the initialized version
  public Uni<Version> getVersion(String versionId) {
    return dataEngine
        .getVersion(versionId)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .map(
            (Version modelsVersion) -> {
              String versionNumber = modelsVersion.getVersionNumber();
              graphEngineService.init(versionNumber).await().indefinitely();
              waitUntilGraphReady(versionNumber);
              return modelsVersion;
            });
  }

  private void waitUntilGraphReady(String versionNumber) {
    Instant deadline = Instant.now().plus(graphEngineConfig.init().readyTimeout());

    while (Instant.now().isBefore(deadline)) {
      if (graphEngineService.isReady(versionNumber)) {
        return;
      }

      try {
        Thread.sleep(graphEngineConfig.init().readyPollInterval().toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
            "Interrupted while waiting for graph to become ready for version " + versionNumber, e);
      }
    }

    throw new IllegalStateException(
        "Timed out waiting for graph to become ready for version "
            + versionNumber
            + " after "
            + graphEngineConfig.init().readyTimeout());
  }

  /// Initializes AUD service with the specified version or latest if none specified. Returns a Uni
  /// that can be used to chain operations or wait for completion. It doesn't initialize the graph
  ///
  /// @param versionId The version Id to initialize, or null for the latest
  /// @return A Uni that completes when initialization is done, containing the initialized version
  public Uni<Version> getVersionData(String versionId) {
    return dataEngine
        .getVersion(versionId)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  /// Initializes AUD service with the latest version at application startup. This method is
  /// automatically called during application bootstrap.
  @PostConstruct
  public void initStartup() {
    try {
      getVersion().await().indefinitely();
      Log.info("AUD service startup initialization completed");
    } catch (Exception e) {
      Log.error("Failed to initialize AUD service at startup", e);
      throw new IllegalStateException("AUD service startup initialization failed", e);
    }
  }

  public Uni<Version> getVersion() {
    return dataEngine
        .getCurrentVersion()
        .onItem()
        .transformToUni(
            (Version version) -> {
              if (version == null) {
                return checkForNewVersion();
              }
              return Uni.createFrom().item(version);
            })
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }

  public Uni<Version> checkForNewVersion() {
    Temporal audReqStartTime = Instant.now();
    return dataEngine
        .checkForNewVersion()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(
            (String newVersion) -> {
              if (newVersion != null) {
                return getVersion(newVersion)
                    .onItem()
                    .invoke(
                        (Version version) ->
                            Log.infof(
                                "The application became ready to serve version %s in %s",
                                newVersion, Duration.between(audReqStartTime, Instant.now())));
              } else {
                return Uni.createFrom().nullItem();
              }
            })
        .onFailure()
        .invoke(
            (Throwable e) -> Log.errorf("Failed to get latest version. Error: %s", e.getMessage()));
  }
}
