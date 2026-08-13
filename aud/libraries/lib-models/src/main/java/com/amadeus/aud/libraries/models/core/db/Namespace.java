package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Namespace;
import com.amadeus.aud.libraries.yaml.models.V1NamespaceSpec;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
public class Namespace extends AbstractBase<V1Namespace> {

  @ManyToMany(targetEntity = Scope.class)
  private Set<Scope> authorizedScopes;

  @OneToMany(mappedBy = "namespace", cascade = CascadeType.ALL)
  private Set<ComponentInstance> componentInstances;

  private boolean automaticallyGenerated;

  public Namespace(V1Namespace namespace) {
    super(
        namespace,
        Objects.requireNonNull(
                Objects.requireNonNull(namespace, "namespace cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Namespace() {
    super();
  }

  public boolean isShared() {
    V1Namespace data = getData();
    if (data == null) {
      return false;
    }

    V1NamespaceSpec spec = data.getSpec();
    if (spec == null) {
      return false;
    }
    return Boolean.TRUE.equals(spec.getIsShared());
  }

  public Set<Scope> getAuthorizedScopes() {
    return authorizedScopes;
  }

  public void setAuthorizedScopes(Set<Scope> authorizedScopes) {
    this.authorizedScopes = authorizedScopes;
  }

  public void addAuthorizedScope(Scope scope) {
    if (authorizedScopes == null) {
      authorizedScopes = new HashSet<>();
    }
    authorizedScopes.add(scope);
  }

  public void addAuthorizedScopes(Set<Scope> scopes) {
    if (authorizedScopes == null) {
      authorizedScopes = new HashSet<>();
    }
    authorizedScopes.addAll(scopes);
  }

  public Set<ComponentInstance> getComponentInstances() {
    return componentInstances;
  }

  public void setComponentInstances(Set<ComponentInstance> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public void addComponentInstance(ComponentInstance componentInstance) {
    if (componentInstances == null) {
      componentInstances = new HashSet<>();
    }
    componentInstances.add(componentInstance);
  }

  public void addComponentInstances(Set<ComponentInstance> componentInstances) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.addAll(componentInstances);
  }

  public boolean isAutomaticallyGenerated() {
    return automaticallyGenerated;
  }

  public void setAutomaticallyGenerated(boolean automaticallyGenerated) {
    this.automaticallyGenerated = automaticallyGenerated;
  }

  public List<String> getAuthorizedScopesNames() {
    return Optional.ofNullable(getAuthorizedScopes()).orElse(Set.of()).stream()
        .map(Scope::getName)
        .toList();
  }
}
