package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import java.util.Objects;

public class Edge {
  private static final String ID_SEPARATOR = "***";

  private int hashCode;

  private final String id;
  private final String name;

  private final ComponentInstance source;
  private final ComponentInstance destination;
  private final DependencyInstance dependencyInstance;
  private final Service service;
  private final boolean governanceActive;

  @JsonIgnore private Link link;

  public Edge(
      ComponentInstance source,
      ComponentInstance destination,
      DependencyInstance dependencyInstance,
      Service service,
      boolean governanceActive,
      boolean storeFullLink) {
    this(source, destination, dependencyInstance, service, governanceActive);
    if (storeFullLink) {
      this.link = new Link(source, destination, dependencyInstance, service);
    }
  }

  public Edge(
      ComponentInstance source,
      ComponentInstance destination,
      DependencyInstance dependencyInstance,
      Service service,
      boolean governanceActive) {
    this.id = getUniqueId(source, destination, service, dependencyInstance);
    this.hashCode = this.id.hashCode();
    this.name = getName(source, destination, service, dependencyInstance);

    this.source = source;
    this.destination = destination;
    this.dependencyInstance = dependencyInstance;
    this.service = service;
    this.governanceActive = governanceActive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    return (o instanceof Edge edge)
        && governanceActive == edge.governanceActive
        && Objects.equals(id, edge.id)
        && Objects.equals(name, edge.name);
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = this.id.hashCode();
    }
    return hashCode;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ComponentInstance getSource() {
    return source;
  }

  public ComponentInstance getDestination() {
    return destination;
  }

  public DependencyInstance getDependencyInstance() {
    return dependencyInstance;
  }

  public Service getService() {
    return service;
  }

  public boolean isGovernanceActive() {
    return governanceActive;
  }

  public @Nullable Link getLink() {
    return link;
  }

  public static String getUniqueId(
      ComponentInstance source,
      ComponentInstance destination,
      Service service,
      DependencyInstance dependencyInstance) {
    return String.join(
        ID_SEPARATOR, source.id(), destination.id(), service.id(), dependencyInstance.id());
  }

  public static String getName(
      ComponentInstance source,
      ComponentInstance destination,
      Service service,
      DependencyInstance dependencyInstance) {
    return String.join(
        ID_SEPARATOR,
        source.getName(),
        destination.getName(),
        service.id(),
        dependencyInstance.getName());
  }
}
