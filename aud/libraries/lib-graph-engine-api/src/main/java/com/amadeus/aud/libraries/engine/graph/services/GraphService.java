package com.amadeus.aud.libraries.engine.graph.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for graph population services. Implementations are responsible for populating a graph
 * from dependency instance data for a given version.
 */
public interface GraphService {

  /**
   * Populate the given graph builder with edges from dependency instances for the given version.
   */
  void populateGraphFromDependencies(GraphBuilder builder, Version version);

  /** Returns all links in the graph that belong to the given scope. */
  static List<Link> linksLimitedToScope(Graph graph, CharSequence scopeName) {
    List<Link> links = new ArrayList<>();
    graph
        .getEdges()
        .values()
        .forEach(
            (Edge edge) -> {
              Link link = edge.getLink();
              if (link != null
                  && (link.source().getScope().getName().contentEquals(scopeName)
                      || link.target().getScope().getName().contentEquals(scopeName)
                      || link.dependencyInstance().getScope().getName().contentEquals(scopeName))) {
                links.add(link);
              }
            });
    return links;
  }

  /** Returns links partitioned by scope for the given collection of scopes. */
  static Map<String, List<Link>> linksPartitionedByScopes(Graph graph, Collection<String> scopes) {
    Map<String, List<Link>> graphPerScope = HashMap.newHashMap(scopes.size());
    if (scopes.size() == 1) {
      String scope = scopes.iterator().next();
      graphPerScope.put(scope, linksLimitedToScope(graph, scope));
      return graphPerScope;
    }

    for (String scope : scopes) {
      graphPerScope.put(scope, new ArrayList<>());
    }

    for (Edge edge : graph.getEdges().values()) {
      Link link = edge.getLink();
      if (link == null) {
        continue;
      }
      String srcScope = link.source().getScope().getName();
      String dstScope = link.target().getScope().getName();
      String depScope = link.dependencyInstance().getScope().getName();

      List<Link> srcList = graphPerScope.get(srcScope);
      if (srcList != null) {
        srcList.add(link);
      }
      List<Link> dstList = graphPerScope.get(dstScope);
      if (dstList != null && !dstScope.equals(srcScope)) {
        dstList.add(link);
      }
      List<Link> depList = graphPerScope.get(depScope);
      if (depList != null && !depScope.equals(srcScope) && !depScope.equals(dstScope)) {
        depList.add(link);
      }
    }
    return graphPerScope;
  }
}
