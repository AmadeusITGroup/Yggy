package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.engine.graph.services.storage.inmemory.InMemoryDeduplicationPool;

/**
 * Denormalized edge record for in-memory storage. Contains node metadata (landing zone, platform,
 * target) inlined from source and destination nodes so that filtering can be done without looking
 * up {@code ComponentInstance} objects.
 *
 * <p>Because this is a Java record, structurally equal instances share the same {@code hashCode()}
 * and {@code equals()} semantics — enabling Flyweight deduplication via {@link
 * InMemoryDeduplicationPool}.
 */
public record StoredEdge(
    String name,
    String dependencyInstanceId,
    String serviceId,
    boolean governanceActive,
    String sourceNodeId,
    String destinationNodeId,
    String sourceNodeScope,
    String destinationNodeScope,
    String sourceNodeLandingZone,
    String sourceNodePlatform,
    String sourceNodeTarget,
    String destinationNodeLandingZone,
    String destinationNodePlatform,
    String destinationNodeTarget) {}
