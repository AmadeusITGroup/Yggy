package com.amadeus.aud.libraries.engine.graph.models;

import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import jakarta.annotation.Nullable;
import java.util.Objects;

public final class Link {
  private final ComponentInstance source;
  private final ComponentInstance target;
  private final DependencyInstance dependencyInstance;
  @Nullable private final Service service;

  @Nullable private Rule rule = null;

  public Link(
      ComponentInstance source,
      ComponentInstance target,
      DependencyInstance dependencyInstance,
      @Nullable Service service) {
    this.source = source;
    this.target = target;
    this.dependencyInstance = dependencyInstance;
    this.service = service;
  }

  public String name() {
    return dependencyInstance.getName()
        + " - "
        + (service != null ? service.getProtocol().getValue() : null)
        + " - "
        + (service != null ? service.getPorts() : null);
  }

  public ComponentInstance source() {
    return source;
  }

  public ComponentInstance target() {
    return target;
  }

  public DependencyInstance dependencyInstance() {
    return dependencyInstance;
  }

  public @Nullable Service service() {
    return service;
  }

  public @Nullable Rule rule() {
    return rule;
  }

  public void setRule(@Nullable Rule rule) {
    this.rule = rule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (Link) obj;
    return Objects.equals(this.source, that.source)
        && Objects.equals(this.target, that.target)
        && Objects.equals(this.dependencyInstance, that.dependencyInstance)
        && Objects.equals(this.service, that.service);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, target, dependencyInstance, service);
  }

  @Override
  public String toString() {
    return "Link["
        + "source="
        + source
        + ", "
        + "target="
        + target
        + ", "
        + "dependencyInstance="
        + dependencyInstance
        + ", "
        + "service="
        + service
        + ']';
  }

  /**
   * A link is based on a source and destination component instance for a given service.
   *
   * <p>The DependencyInstance itself is not part of the functional identity of the link.
   *
   * <p>This is because component instances names are unique, as well as service (the service is
   * linked to the target component instance).
   *
   * <p>Enforced by NormalizedComponentInstanceNameValid.java check.
   */
  public String functionalName() {
    return new StringBuilder()
        .append("From CI: ")
        .append(source.getName())
        .append("\nTo CI: ")
        .append(target.getName())
        .append("\nService: ")
        .append(service.getName())
        .toString();
  }
}
