package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Stateless utility methods for filtering, paginating, and converting {@link StoredEdge}s. */
public final class StoredEdgeFilters {

  private StoredEdgeFilters() {}

  public static List<Edge> filterAndPage(
      List<StoredEdge> edges, int page, int size, Predicate<StoredEdge> filter) {
    List<Edge> result = new ArrayList<>();
    int skip = page * size;
    int count = 0;

    for (StoredEdge stored : edges) {
      if (filter.test(stored)) {
        if (count >= skip && result.size() < size) {
          result.add(toDto(stored));
        }
        count++;
        if (result.size() >= size) {
          break;
        }
      }
    }
    return result;
  }

  static Edge toDto(StoredEdge stored) {
    return new Edge(
        stored.name(),
        stored.dependencyInstanceId(),
        stored.serviceId(),
        stored.governanceActive(),
        stored.sourceNodeId(),
        stored.destinationNodeId());
  }

  public static boolean matchesTargets(
      StoredEdge edge, @Nullable List<String> targets, EdgeDirection direction) {
    if (targets == null || targets.isEmpty()) {
      return true;
    }
    Set<String> targetSet = new HashSet<>(targets);
    boolean srcMatch = targetSet.contains(edge.sourceNodeTarget());
    boolean dstMatch = targetSet.contains(edge.destinationNodeTarget());
    return matchDirection(srcMatch, dstMatch, direction);
  }

  public static boolean matchesPlatforms(
      StoredEdge edge, @Nullable List<String> platforms, EdgeDirection direction) {
    if (platforms == null || platforms.isEmpty()) {
      return true;
    }
    Set<String> platformSet = new HashSet<>(platforms);
    boolean srcMatch = platformSet.contains(edge.sourceNodePlatform());
    boolean dstMatch = platformSet.contains(edge.destinationNodePlatform());
    return matchDirection(srcMatch, dstMatch, direction);
  }

  public static boolean matchesLandingZones(
      StoredEdge edge, @Nullable List<String> landingZones, EdgeDirection direction) {
    if (landingZones == null || landingZones.isEmpty()) {
      return true;
    }
    Set<String> lzSet = new HashSet<>(landingZones);
    boolean srcMatch = lzSet.contains(edge.sourceNodeLandingZone());
    boolean dstMatch = lzSet.contains(edge.destinationNodeLandingZone());
    return matchDirection(srcMatch, dstMatch, direction);
  }

  public static boolean matchesScope(StoredEdge edge, String scope) {

    boolean srcMatch = scope.equals(edge.sourceNodeScope());
    boolean dstMatch = scope.equals(edge.destinationNodeScope());
    return srcMatch || dstMatch;
  }

  static boolean matchDirection(boolean srcMatch, boolean dstMatch, EdgeDirection direction) {
    return switch (direction) {
      case INBOUND -> dstMatch;
      case OUTBOUND -> srcMatch;
      case ANY -> srcMatch || dstMatch;
    };
  }
}
