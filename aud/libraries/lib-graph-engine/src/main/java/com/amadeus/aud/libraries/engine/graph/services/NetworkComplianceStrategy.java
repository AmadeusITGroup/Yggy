package com.amadeus.aud.libraries.engine.graph.services;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import java.util.function.BiPredicate;

/**
 * Strategy interface for determining whether network compliance verification is required for an
 * edge between two component instances. Network compliance covers firewall rules, NSGs, and network
 * policies.
 *
 * <p>This is distinct from scope-level governance ({@code Scope.governanceActivationLevel}), which
 * controls whether connection approvals are enforced for a scope.
 *
 * <p>The default strategy {@link #NONE} always returns {@code false}, which is the behavior used by
 * the checker CLI where network compliance is not relevant.
 */
@FunctionalInterface
public interface NetworkComplianceStrategy
    extends BiPredicate<ComponentInstance, ComponentInstance> {

  /** Default strategy: network compliance is never required. Used by the checker CLI. */
  NetworkComplianceStrategy NONE = (source, destination) -> false;

  /**
   * Determine whether network compliance verification is required for an edge between the given
   * source and destination.
   *
   * @param source the source component instance
   * @param destination the destination component instance
   * @return {@code true} if network compliance is required for this edge
   */
  boolean isNetworkComplianceRequired(ComponentInstance source, ComponentInstance destination);

  @Override
  default boolean test(ComponentInstance source, ComponentInstance destination) {
    return isNetworkComplianceRequired(source, destination);
  }
}
