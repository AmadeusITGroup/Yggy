package com.amadeus.aud.libraries.engine.graph.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of {@link GraphService} that populates a graph from dependency instance
 * data. This bean is used by the checker CLI and other consumers that need in-memory graph
 * population without database persistence.
 */
@Unremovable
@Singleton
public class DefaultGraphService implements GraphService {

  /** Interval (number of dependency instances) between progress log messages. */
  private static final int LOG_PROGRESS_INTERVAL = 10_000;

  /** Multiplier to convert a ratio to a percentage. */
  private static final int PERCENTAGE_MULTIPLIER = 100;

  private final NetworkComplianceStrategy networkComplianceStrategy;

  /** Default constructor for CDI — uses {@link NetworkComplianceStrategy#NONE}. */
  public DefaultGraphService() {
    this(NetworkComplianceStrategy.NONE);
  }

  /**
   * Constructor with a custom network compliance strategy.
   *
   * @param networkComplianceStrategy the strategy to determine network compliance for each edge
   */
  public DefaultGraphService(NetworkComplianceStrategy networkComplianceStrategy) {
    this.networkComplianceStrategy = networkComplianceStrategy;
  }

  @Override
  public void populateGraphFromDependencies(GraphBuilder builder, Version version) {
    DependencyInstancesRepository dependencyInstancesRepository =
        version.getDependencyInstancesRepository();
    if (dependencyInstancesRepository == null) {
      Log.errorf("Dependency Instances Repository not found for version %s", version);
      throw new IllegalStateException();
    }

    ConcurrentMap<String, DependencyInstance> dependencyInstancesData =
        dependencyInstancesRepository.getData();
    if (dependencyInstancesData == null || dependencyInstancesData.isEmpty()) {
      Log.warnf("No Dependency Instances found for version %s", version);
      return;
    }

    processDependencyInstances(
        builder, dependencyInstancesData.values(), networkComplianceStrategy);
  }

  private static void processDependencyInstances(
      GraphBuilder builder,
      Collection<DependencyInstance> dependencyInstances,
      NetworkComplianceStrategy networkComplianceStrategy) {
    if (dependencyInstances == null || dependencyInstances.isEmpty()) {
      Log.warnf(
          "No Dependency Instances found to process for graph %s", builder.getVersionNumber());
      return;
    }

    AtomicInteger count = new AtomicInteger(0);
    int valuesSize = dependencyInstances.size();
    Temporal mapPhaseStartTime = Instant.now();

    Multi.createFrom()
        .iterable(dependencyInstances)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUniAndMerge(
            dependencyInstance ->
                createDependencyProcessingUni(
                    dependencyInstance,
                    builder,
                    count,
                    valuesSize,
                    mapPhaseStartTime,
                    networkComplianceStrategy))
        .collect()
        .asList()
        .await()
        .indefinitely();

    Log.debugf(
        "Processed %d Dependency Instances in %s. Graph sizes - Vertices: %d, Edges: %d",
        dependencyInstances.size(),
        Duration.between(mapPhaseStartTime, Instant.now()),
        builder.nodesSize(),
        builder.edgesSize());
  }

  private static Uni<Void> createDependencyProcessingUni(
      DependencyInstance dependencyInstance,
      GraphBuilder builder,
      AtomicInteger count,
      int valuesSize,
      Temporal mapPhaseStartTime,
      NetworkComplianceStrategy networkComplianceStrategy) {
    return Uni.createFrom()
        .voidItem()
        .onItem()
        .invoke(
            () ->
                processDependencyInstance(
                    dependencyInstance,
                    builder,
                    count,
                    valuesSize,
                    mapPhaseStartTime,
                    networkComplianceStrategy))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  private static void processDependencyInstance(
      DependencyInstance dependencyInstance,
      GraphBuilder builder,
      AtomicInteger count,
      int valuesSize,
      Temporal startTime,
      NetworkComplianceStrategy networkComplianceStrategy) {

    boolean isSourceAsComponent = dependencyInstance.isSourceAsComponent();
    boolean isTargetAsComponent = dependencyInstance.isTargetAsComponent();

    Dependency dependency = dependencyInstance.getDependency();
    Component sourceComponent = dependency.getSourceComponent();
    Component destinationComponent = dependency.getDestinationComponent();

    if ("toplevel".equalsIgnoreCase(sourceComponent.getName())
        && "toplevel".equalsIgnoreCase(destinationComponent.getName())
        && isSourceAsComponent
        && isTargetAsComponent) {
      return;
    }

    if (!isSourceAsComponent) {
      processNonGenericSource(
          dependencyInstance,
          destinationComponent,
          isTargetAsComponent,
          builder,
          networkComplianceStrategy);
    } else {
      processGenericSource(
          dependencyInstance,
          sourceComponent,
          destinationComponent,
          isTargetAsComponent,
          builder,
          networkComplianceStrategy);
    }

    logProgress(count, valuesSize, startTime);
  }

  /** Handles cases where the source is a specific component instance (Cases 1 & 2). */
  private static void processNonGenericSource(
      DependencyInstance dependencyInstance,
      Component destinationComponent,
      boolean isTargetAsComponent,
      GraphBuilder builder,
      NetworkComplianceStrategy networkComplianceStrategy) {
    ComponentInstance sourceInstance = dependencyInstance.getComponentInstance();
    if (sourceInstance == null) {
      Log.infof("Component Instance is null for dependency instance %s", dependencyInstance.id());
      return;
    }
    if (isTargetAsComponent) {
      Log.tracef(
          "Case 1: Source is not generic and target is generic for dependency instance %s",
          dependencyInstance);
      for (ComponentInstance dest : destinationComponent.getAllComponentInstances()) {
        EdgeFactory.addEdge(
            sourceInstance, dest, dependencyInstance, builder, networkComplianceStrategy);
      }
    } else {
      ComponentInstance targetInstance = dependencyInstance.getTargetComponentInstance();
      if (targetInstance == null) {
        Log.infof(
            "Target Component Instance is null for dependency instance %s",
            dependencyInstance.id());
        return;
      }
      Log.tracef(
          "Case 2: Source is not generic and target is not generic for dependency instance %s",
          dependencyInstance.id());
      EdgeFactory.addEdge(
          sourceInstance, targetInstance, dependencyInstance, builder, networkComplianceStrategy);
    }
  }

  /** Handles cases where the source is generic (all component instances — Cases 3 & 4). */
  private static void processGenericSource(
      DependencyInstance dependencyInstance,
      Component sourceComponent,
      Component destinationComponent,
      boolean isTargetAsComponent,
      GraphBuilder builder,
      NetworkComplianceStrategy networkComplianceStrategy) {
    Set<ComponentInstance> allSources = sourceComponent.getAllComponentInstances();
    if (isTargetAsComponent) {
      Log.tracef(
          "Case 3: Source is generic and target is generic for dependency instance %s",
          dependencyInstance);
      Set<ComponentInstance> allDests = destinationComponent.getAllComponentInstances();
      for (ComponentInstance src : allSources) {
        for (ComponentInstance dest : allDests) {
          EdgeFactory.addEdge(src, dest, dependencyInstance, builder, networkComplianceStrategy);
        }
      }
    } else {
      Log.tracef(
          "Case 4: Source is generic and target is not generic for dependency instance %s",
          dependencyInstance);
      ComponentInstance targetInstance = dependencyInstance.getTargetComponentInstance();
      for (ComponentInstance src : allSources) {
        EdgeFactory.addEdge(
            src, targetInstance, dependencyInstance, builder, networkComplianceStrategy);
      }
    }
  }

  private static void logProgress(AtomicInteger count, int valuesSize, Temporal startTime) {
    if (Log.isDebugEnabled()) {
      int diProcessed = count.incrementAndGet();
      if (diProcessed % LOG_PROGRESS_INTERVAL == 0) {
        Log.debugf(
            "Processed %d/%d (%.0f%%) Dependency Instances in %s",
            diProcessed,
            valuesSize,
            (double) diProcessed / valuesSize * PERCENTAGE_MULTIPLIER,
            Duration.between(startTime, Instant.now()));
      }
    }
  }
}
