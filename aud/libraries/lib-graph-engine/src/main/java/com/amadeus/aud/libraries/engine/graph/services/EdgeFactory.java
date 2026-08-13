package com.amadeus.aud.libraries.engine.graph.services;

import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class responsible for creating edges and validating nodes in the connectivity graph.
 *
 * <p>Extracted from {@link DefaultGraphService} to reduce class coupling. Contains all the
 * filtering logic that determines whether a dependency instance should produce an edge, and the
 * node-validation logic that decides whether a component instance qualifies as a graph node.
 */
final class EdgeFactory {

  private EdgeFactory() {
    // utility class
  }

  /**
   * Attempts to add an edge between two component instances to the graph, applying all dependency
   * instance filtering rules. Both endpoints must pass node validation before the edge is created.
   */
  static void addEdge(
      ComponentInstance sourceComponentInstance,
      ComponentInstance destinationComponentInstance,
      DependencyInstance dependencyInstance,
      GraphBuilder builder,
      NetworkComplianceStrategy networkComplianceStrategy) {
    if (hasNullArguments(
        sourceComponentInstance, destinationComponentInstance, dependencyInstance, builder)) {
      return;
    }

    Component sourceComponent = sourceComponentInstance.getComponent();
    Component destinationComponent = destinationComponentInstance.getComponent();
    Set<Service> dependencyInstanceServices = dependencyInstance.getServices();
    if (hasInvalidComponents(sourceComponent, destinationComponent, dependencyInstanceServices)) {
      return;
    }

    if (isFilteredByProviderLimits(
            sourceComponentInstance, destinationComponentInstance, dependencyInstance)
        || isFilteredBySourceGeography(sourceComponentInstance, dependencyInstance)
        || isFilteredByTargetPaas(
            sourceComponentInstance, destinationComponentInstance, dependencyInstance)
        || isFilteredByTargetNamespace(
            sourceComponentInstance,
            destinationComponentInstance,
            sourceComponent,
            destinationComponent,
            dependencyInstance)
        || isFilteredByTargetConstraints(
            sourceComponentInstance, destinationComponentInstance, dependencyInstance)
        || isFilteredBySelectors(
            sourceComponentInstance, destinationComponentInstance, dependencyInstance)) {
      return;
    }

    insertEdges(
        sourceComponentInstance,
        destinationComponentInstance,
        dependencyInstance,
        builder,
        dependencyInstanceServices,
        networkComplianceStrategy);
  }

  private static boolean hasNullArguments(
      ComponentInstance source,
      ComponentInstance destination,
      DependencyInstance dep,
      GraphBuilder builder) {
    return source == null || destination == null || dep == null || builder == null;
  }

  private static boolean hasInvalidComponents(
      Component sourceComponent,
      Component destinationComponent,
      Set<Service> dependencyInstanceServices) {
    return sourceComponent == null
        || destinationComponent == null
        || sourceComponent.isTechnical()
        || destinationComponent.isTechnical()
        || dependencyInstanceServices == null
        || dependencyInstanceServices.isEmpty();
  }

  /** Checks provider-based cloud/legacy filtering rules. */
  private static boolean isFilteredByProviderLimits(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    LandingZone sourceLz = source.getLandingZone();
    String sourceProvider = sourceLz.getTarget().getProviderName();
    boolean isSourceAmadeus = Objects.equals(sourceProvider, "Amadeus");

    if ((dep.isSourceLimitedToLegacyOnly() && !isSourceAmadeus)
        || (dep.isSourceLimitedToCloudOnly() && isSourceAmadeus)) {
      return true;
    }

    String destProvider = destination.getLandingZone().getTarget().getProviderName();
    boolean isDestAmadeus = Objects.equals(destProvider, "Amadeus");

    return (dep.isTargetLimitedToLegacyOnly() && !isDestAmadeus)
        || (dep.isTargetLimitedToCloudOnly() && isDestAmadeus);
  }

  /** Checks source geographic filtering (landing zone, target, continent, macro phase). */
  private static boolean isFilteredBySourceGeography(
      ComponentInstance source, DependencyInstance dep) {
    LandingZone sourceLz = source.getLandingZone();
    LandingZone depLz = dep.getLandingZone();

    if ((dep.isSourceLimitedToLandingZone() && !Objects.equals(sourceLz, depLz))
        || (dep.isSourceLimitedToTarget()
            && !Objects.equals(sourceLz.getTarget(), depLz.getTarget()))) {
      return true;
    }
    return (dep.isSourceLimitedToContinent()
            && !Objects.equals(
                sourceLz.getRegion().getContinent(), depLz.getRegion().getContinent()))
        || (dep.isSourceLimitedToMacroPhase()
            && !Objects.equals(sourceLz.getMacroPhase(), depLz.getMacroPhase()));
  }

  /** Checks target-limited-to-source PaaS filtering. */
  private static boolean isFilteredByTargetPaas(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    if (!dep.isTargetLimitedToSourcePaas()) {
      return false;
    }
    Component sourceComponent = source.getComponent();
    boolean isContainer = sourceComponent.isContainer();
    boolean isHosting = sourceComponent.isHosting();
    if (!isContainer && !isHosting) {
      return true;
    }
    ComponentInstance destDeploy = destination.getDeployComponentInstance();
    return (isContainer && !Objects.equals(source.getDeployComponentInstance(), destDeploy))
        || (isHosting && !Objects.equals(source, destDeploy));
  }

  /** Checks target-limited-to-source namespace filtering. */
  private static boolean isFilteredByTargetNamespace(
      ComponentInstance source,
      ComponentInstance destination,
      Component sourceComponent,
      Component destinationComponent,
      DependencyInstance dep) {
    if (!dep.isTargetLimitedToSourceNamespace()) {
      return false;
    }
    if (!sourceComponent.isContainer() || !destinationComponent.isContainer()) {
      return true;
    }
    String sourceNs = source.getNamespaceName();
    return sourceNs == null
        || sourceNs.isBlank()
        || !Objects.equals(sourceNs, destination.getNamespaceName());
  }

  /**
   * Checks target identity and geography constraints (security zone, application phase, landing
   * zone, target, continent, macro phase).
   */
  private static boolean isFilteredByTargetConstraints(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    return isFilteredByTargetIdentity(source, destination, dep)
        || isFilteredByTargetGeography(source, destination, dep);
  }

  /** Checks target identity filtering (security zone, application phase, landing zone). */
  private static boolean isFilteredByTargetIdentity(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    if (dep.isTargetLimitedToSourceSecurityZone()
        && !Objects.equals(
            source.getInternalZoneTypeNames(Direction.IN),
            destination.getInternalZoneTypeNames(Direction.IN))) {
      return true;
    }
    if (dep.isTargetLimitedToSourceApplicationPhase()
        && !Objects.equals(source.getApplicationPhase(), destination.getApplicationPhase())) {
      return true;
    }
    LandingZone sourceLz = source.getLandingZone();
    LandingZone destLz = destination.getLandingZone();
    return dep.isTargetLimitedToSourceLandingZone() && !Objects.equals(sourceLz, destLz);
  }

  /** Checks target geography filtering (target, continent, macro phase). */
  private static boolean isFilteredByTargetGeography(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    LandingZone sourceLz = source.getLandingZone();
    LandingZone destLz = destination.getLandingZone();
    if (dep.isTargetLimitedToSourceTarget()
        && !Objects.equals(sourceLz.getTarget(), destLz.getTarget())) {
      return true;
    }
    if (dep.isTargetLimitedToSourceContinent()
        && !Objects.equals(
            sourceLz.getRegion().getContinent(), destLz.getRegion().getContinent())) {
      return true;
    }
    return dep.isTargetLimitedToSourceMacroPhase()
        && !Objects.equals(sourceLz.getMacroPhase(), destLz.getMacroPhase());
  }

  /** Checks label-selector filtering. */
  private static boolean isFilteredBySelectors(
      ComponentInstance source, ComponentInstance destination, DependencyInstance dep) {
    if (dep.isSourceLimitedToSelector()
        && !source.areLabelsMatching(destination.getSourcesSelectors())) {
      return true;
    }

    Set<Map<String, String>> sourcesSelectors = dep.getSourcesSelectors();
    if (sourcesSelectors != null
        && !sourcesSelectors.isEmpty()
        && !source.areLabelsMatching(sourcesSelectors)) {
      return true;
    }

    Set<Map<String, String>> destSelectors = dep.getTargetsSelectors();
    return destSelectors != null
        && !destSelectors.isEmpty()
        && !destination.areLabelsMatching(destSelectors);
  }

  /** Creates edge entries in the graph after all filters have passed. */
  private static void insertEdges(
      ComponentInstance source,
      ComponentInstance destination,
      DependencyInstance dep,
      GraphBuilder builder,
      Set<Service> services,
      NetworkComplianceStrategy networkComplianceStrategy) {
    if (!addNode(source, builder) || !addNode(destination, builder)) {
      return;
    }
    boolean isNetworkComplianceRequired =
        networkComplianceStrategy.isNetworkComplianceRequired(source, destination);
    for (Service service : services) {
      String uniqueId = Edge.getUniqueId(source, destination, service, dep);
      if (builder.getEdge(uniqueId) == null) {
        builder.putEdge(
            uniqueId,
            new Edge(
                source,
                destination,
                dep,
                service,
                isNetworkComplianceRequired,
                builder.isStoreFullLink()));
      }
    }
  }

  /**
   * Validates and adds a component instance as a node in the graph.
   *
   * @return {@code true} if the node is valid and was added (or already existed)
   */
  static boolean addNode(ComponentInstance componentInstance, GraphBuilder builder) {
    if (componentInstance == null || builder == null) {
      return false;
    }

    String uniqueId = componentInstance.id();
    if (!builder.containsNode(uniqueId)) {
      Component component = componentInstance.getComponent();
      if (component.isContainer()
          && (componentInstance.getZoneType() == null
              || isInvalidNode(componentInstance, component))) {
        return false;
      }
      builder.putNode(uniqueId, componentInstance);
    }
    return true;
  }

  private static boolean isInvalidNode(ComponentInstance componentInstance, Component component) {
    if (!(component.isContainer()
        && Objects.equals(componentInstance.getZoneTypeName(), "TechnicalZone"))) {
      if (component.isContainer() && componentInstance.getDeployComponentInstance() == null) {
        return true;
      }

      Set<Endpoint> internalOutEndpoints = componentInstance.getInternalEndpoints(Direction.OUT);
      Set<Endpoint> internalInEndpoints = componentInstance.getInternalEndpoints(Direction.IN);
      return componentInstance.getServiceTag() == null
          && ((internalOutEndpoints != null && internalOutEndpoints.isEmpty())
              || (internalInEndpoints != null && internalInEndpoints.isEmpty()));
    }
    return false;
  }
}
