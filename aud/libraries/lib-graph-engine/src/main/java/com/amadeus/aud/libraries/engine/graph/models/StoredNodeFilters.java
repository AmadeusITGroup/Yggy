package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Stateless utility methods for filtering, paginating, and converting {@link StoredNode}s. */
public final class StoredNodeFilters {
  private StoredNodeFilters() {}

  public static List<Node> filterAndPage(
      List<StoredNode> nodes, int page, int size, Predicate<StoredNode> filter) {
    List<Node> result = new ArrayList<>();
    int skip = page * size;
    int count = 0;

    for (StoredNode stored : nodes) {
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

  static Node toDto(StoredNode stored) {
    return new Node(
        stored.componentInstanceId(),
        stored.landingZoneName(),
        stored.platformName(),
        stored.targetName());
  }

  public static boolean matchesTargets(StoredNode node, @Nullable List<String> targets) {
    if (targets == null || targets.isEmpty()) {
      return true;
    }
    Set<String> targetSet = new HashSet<>(targets);
    return targetSet.contains(node.targetName());
  }

  public static boolean matchesPlatforms(StoredNode node, @Nullable List<String> platforms) {
    if (platforms == null || platforms.isEmpty()) {
      return true;
    }
    Set<String> platformSet = new HashSet<>(platforms);
    return platformSet.contains(node.platformName());
  }

  public static boolean matchesLandingZones(StoredNode node, @Nullable List<String> landingZones) {
    if (landingZones == null || landingZones.isEmpty()) {
      return true;
    }
    Set<String> lzSet = new HashSet<>(landingZones);
    return lzSet.contains(node.landingZoneName());
  }
}
