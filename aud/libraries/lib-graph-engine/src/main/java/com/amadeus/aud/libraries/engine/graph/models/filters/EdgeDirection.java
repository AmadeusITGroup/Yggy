package com.amadeus.aud.libraries.engine.graph.models.filters;

/**
 * Enum representing the direction of an edge relative to a vertex. INBOUND: Edge where the vertex
 * is the destination (incoming edge) OUTBOUND: Edge where the vertex is the source (outgoing edge)
 */
public enum EdgeDirection {
  // destinationVertex lookup
  INBOUND,
  // sourceVertex lookup
  OUTBOUND,
  // both directions, used for general queries without specific vertex context
  ANY
}
