package com.amadeus.aud.libraries.engine.graph.services.storage.inmemory;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.StoredEdge;
import com.amadeus.aud.libraries.engine.graph.models.StoredNode;
import com.amadeus.aud.libraries.engine.graph.models.VersionData;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import io.quarkus.logging.Log;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link VersionData} from a {@link Version} by constructing a temporary {@link Graph},
 * converting its nodes and edges to denormalized {@link StoredNode}/{@link StoredEdge} records
 * (deduplicated via an {@link InMemoryDeduplicationPool}), computing statistics, and discarding the
 * original graph.
 */
final class GraphDataMapper {

  private final GraphService graphService;
  private final InMemoryDeduplicationPool deduplicationPool;

  GraphDataMapper(GraphService graphService, InMemoryDeduplicationPool deduplicationPool) {
    this.graphService = graphService;
    this.deduplicationPool = deduplicationPool;
  }

  VersionData buildVersionData(Version version) {
    Temporal start = Instant.now();
    GraphBuilder builder = new GraphBuilder(version.getVersionNumber());
    graphService.populateGraphFromDependencies(builder, version);
    Graph graph = builder.build();

    List<StoredNode> nodes = new ArrayList<>(graph.getNodes().size());
    List<StoredEdge> edges = new ArrayList<>(graph.getEdges().size());

    for (ComponentInstance ci : graph.getNodes().values()) {
      nodes.add(deduplicationPool.internNode(toStoredNode(ci)));
    }

    for (com.amadeus.aud.libraries.engine.graph.models.Edge edge : graph.getEdges().values()) {
      edges.add(deduplicationPool.internEdge(toStoredEdge(edge, graph)));
    }

    GraphStatistics statistics = GraphStatisticsComputer.compute(graph, version);

    Log.infof(
        "Built graph for version %s in %s — %d nodes, %d edges",
        version.getVersionNumber(),
        Duration.between(start, Instant.now()),
        nodes.size(),
        edges.size());
    deduplicationPool.logStats();

    // Graph/GraphBuilder/Edge wrappers are now eligible for GC.
    return new VersionData(edges, nodes, statistics);
  }

  private StoredNode toStoredNode(ComponentInstance ci) {
    LandingZone lz = ci.getLandingZone();
    Target target = lz != null ? lz.getTarget() : null;
    ComponentInstance deploy = ci.getDeployComponentInstance();
    return new StoredNode(
        deduplicationPool.internString(ci.id()),
        deduplicationPool.internString(lz != null ? lz.getName() : null),
        deduplicationPool.internString(deploy != null ? deploy.getName() : null),
        deduplicationPool.internString(target != null ? target.getName() : null));
  }

  private StoredEdge toStoredEdge(
      com.amadeus.aud.libraries.engine.graph.models.Edge edge, Graph graph) {
    ComponentInstance src = graph.getNodes().get(edge.getSource().id());
    ComponentInstance dst = graph.getNodes().get(edge.getDestination().id());

    LandingZone srcLz = src != null ? src.getLandingZone() : null;
    Target srcTarget = srcLz != null ? srcLz.getTarget() : null;
    ComponentInstance srcDeploy = src != null ? src.getDeployComponentInstance() : null;

    LandingZone dstLz = dst != null ? dst.getLandingZone() : null;
    Target dstTarget = dstLz != null ? dstLz.getTarget() : null;
    ComponentInstance dstDeploy = dst != null ? dst.getDeployComponentInstance() : null;

    return new StoredEdge(
        deduplicationPool.internString(edge.getName()),
        deduplicationPool.internString(edge.getDependencyInstance().id()),
        deduplicationPool.internString(edge.getService().id()),
        edge.isGovernanceActive(),
        deduplicationPool.internString(src != null ? src.id() : null),
        deduplicationPool.internString(dst != null ? dst.id() : null),
        deduplicationPool.internString(src != null ? src.getScopeName() : null),
        deduplicationPool.internString(dst != null ? dst.getScopeName() : null),
        deduplicationPool.internString(srcLz != null ? srcLz.getName() : null),
        deduplicationPool.internString(srcDeploy != null ? srcDeploy.getName() : null),
        deduplicationPool.internString(srcTarget != null ? srcTarget.getName() : null),
        deduplicationPool.internString(dstLz != null ? dstLz.getName() : null),
        deduplicationPool.internString(dstDeploy != null ? dstDeploy.getName() : null),
        deduplicationPool.internString(dstTarget != null ? dstTarget.getName() : null));
  }
}
