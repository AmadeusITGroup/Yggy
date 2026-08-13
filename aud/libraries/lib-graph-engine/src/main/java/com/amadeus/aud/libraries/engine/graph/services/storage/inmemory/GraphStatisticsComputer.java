package com.amadeus.aud.libraries.engine.graph.services.storage.inmemory;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Computes {@link GraphStatistics} from a {@link Graph} and its associated {@link Version}. */
public final class GraphStatisticsComputer {

  private GraphStatisticsComputer() {}

  public static GraphStatistics compute(Graph graph, Version version) {
    Map<String, Long> scopeNbLink = new HashMap<>();
    Map<String, Long> scopeNbNode = new HashMap<>();
    Set<String> countedNodes = new HashSet<>();

    DependencyInstancesRepository depRepo = version.getDependencyInstancesRepository();

    for (com.amadeus.aud.libraries.engine.graph.models.Edge edge : graph.getEdges().values()) {
      String depId = edge.getDependencyInstance().id();
      DependencyInstance di = depRepo != null ? depRepo.get(depId) : null;
      String linkScopeName = di != null ? di.getScope().getName() : "unknown";
      scopeNbLink.merge(linkScopeName, 1L, Long::sum);

      ComponentInstance src = graph.getNodes().get(edge.getSource().id());
      ComponentInstance dst = graph.getNodes().get(edge.getDestination().id());
      if (src != null && countedNodes.add(src.id())) {
        String nodeScopeName = src.getScope().getName();
        scopeNbNode.merge(nodeScopeName, 1L, Long::sum);
      }
      if (dst != null && countedNodes.add(dst.id())) {
        String nodeScopeName = dst.getScope().getName();
        scopeNbNode.merge(nodeScopeName, 1L, Long::sum);
      }
    }

    return new GraphStatistics(
        Map.copyOf(scopeNbLink),
        Map.copyOf(scopeNbNode),
        graph.getNodes().size(),
        graph.getEdges().size());
  }
}
