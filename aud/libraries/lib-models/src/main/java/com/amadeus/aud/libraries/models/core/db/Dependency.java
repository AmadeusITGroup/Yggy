package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Dependency extends AbstractBase<V1Dependency> {

  @ManyToOne(targetEntity = Component.class, cascade = CascadeType.ALL)
  private Component sourceComponent;

  @ManyToOne(targetEntity = Component.class, cascade = CascadeType.ALL)
  private Component destinationComponent;

  @OneToMany(mappedBy = "dependency", cascade = CascadeType.ALL)
  private Set<DependencyInstance> dependencyInstances;

  public Dependency(V1Dependency dependency) {
    super(
        dependency,
        Objects.requireNonNull(
                Objects.requireNonNull(dependency, "dependency cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Dependency() {
    super();
  }

  public @Nullable String getSourceComponentName() {
    if (sourceComponent == null) {
      return null;
    }
    return sourceComponent.getName();
  }

  public @Nullable String getDestinationComponentName() {
    if (destinationComponent == null) {
      return null;
    }
    return destinationComponent.getName();
  }

  public Component getSourceComponent() {
    return sourceComponent;
  }

  public void setSourceComponent(Component sourceComponent) {
    this.sourceComponent = sourceComponent;
  }

  public Component getDestinationComponent() {
    return destinationComponent;
  }

  public void setDestinationComponent(Component destinationComponent) {
    this.destinationComponent = destinationComponent;
  }

  public Set<DependencyInstance> getDependencyInstances() {
    return dependencyInstances;
  }

  public void setDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public void addDependencyInstance(DependencyInstance dependencyInstance) {
    if (dependencyInstances == null) {
      dependencyInstances = new HashSet<>();
    }
    dependencyInstances.add(dependencyInstance);
  }

  public void addDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.addAll(dependencyInstances);
  }
}
