package com.amadeus.aud.libraries.engine.graph.services.storage;

import com.amadeus.aud.libraries.engine.graph.services.storage.inmemory.InMemoryGraphEngine;
import com.amadeus.aud.libraries.engine.graph.services.storage.postgres.PostgresGraphEngine;
import com.amadeus.aud.libraries.engine.graph.services.storage.remote.RemoteGraphEngine;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Locale;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class GraphEngineServiceProducer {

  private static final String DEFAULT_GRAPH_ENGINE_TYPE = "inmemory";

  @Inject @InMemoryGraphEngine Instance<GraphEngineService> inMemoryGraphEngineService;

  @Inject @PostgresGraphEngine Instance<GraphEngineService> postgresGraphEngineService;

  @Inject @RemoteGraphEngine Instance<GraphEngineService> remoteGraphEngineService;

  @ConfigProperty(name = "graph.database.type", defaultValue = DEFAULT_GRAPH_ENGINE_TYPE)
  String graphDatabaseType;

  @Produces
  public GraphEngineService produceGraphEngineService() {
    String graphEngineType = graphDatabaseType.trim().toLowerCase(Locale.ROOT);
    if ("inmemory".equals(graphEngineType)) {
      Log.infof("Using graph engine implementation: %s", graphEngineType);
      return resolveInMemoryGraphEngineService();
    }

    if ("postgres".equals(graphEngineType)) {
      return resolveOrFallback(postgresGraphEngineService, graphEngineType);
    }

    if ("remote".equals(graphEngineType)) {
      return resolveOrFallback(remoteGraphEngineService, graphEngineType);
    }

    {
      Log.warnf(
          "Unknown graph.database.type '%s'. Falling back to '%s'",
          graphDatabaseType, DEFAULT_GRAPH_ENGINE_TYPE);
      return resolveInMemoryGraphEngineService();
    }
  }

  private GraphEngineService resolveOrFallback(
      Instance<GraphEngineService> graphEngineInstance, String graphEngineType) {
    if (graphEngineInstance.isUnsatisfied() || graphEngineInstance.isAmbiguous()) {
      Log.warnf(
          "graph.database.type is '%s' but '%s' graph engine bean is not resolvable. Falling back to '%s'",
          graphDatabaseType, graphEngineType, DEFAULT_GRAPH_ENGINE_TYPE);
      return resolveInMemoryGraphEngineService();
    }
    Log.infof("Using graph engine implementation: %s", graphEngineType);
    return graphEngineInstance.get();
  }

  private GraphEngineService resolveInMemoryGraphEngineService() {
    if (inMemoryGraphEngineService.isUnsatisfied() || inMemoryGraphEngineService.isAmbiguous()) {
      throw new IllegalStateException("In-memory graph engine bean is not resolvable");
    }
    return inMemoryGraphEngineService.get();
  }
}
