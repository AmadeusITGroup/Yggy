package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import io.quarkus.cache.CacheResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/** Dedicated service for caching version graphs. Separates cache concern from business logic. */
@Singleton
public class VersionGraphCacheService {

  private final GraphService graphService;

  @Inject
  public VersionGraphCacheService(GraphService graphService) {
    this.graphService = graphService;
  }

  /**
   * Builds and caches the graph for a given version. The @CacheResult interceptor will now properly
   * intercept via CDI proxy.
   */
  @CacheResult(cacheName = "versionGraph")
  public Graph buildVersionGraph(Version version) {
    GraphBuilder builder = new GraphBuilder(version.getVersionNumber(), true);
    graphService.populateGraphFromDependencies(builder, version);
    return builder.build();
  }
}
