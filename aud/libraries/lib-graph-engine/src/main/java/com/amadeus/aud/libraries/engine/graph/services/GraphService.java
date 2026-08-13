package com.amadeus.aud.libraries.engine.graph.services;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import java.util.ArrayList;
import java.util.List;

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
}
