package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Role;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Role extends AbstractBase<V1Role> {

  /** The scopes that use this role */
  @ManyToMany(targetEntity = Scope.class, mappedBy = "roles")
  private Set<Scope> scopes;

  public Role(V1Role role) {
    super(
        role,
        Objects.requireNonNull(
                Objects.requireNonNull(role, "role cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Role() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Role that = (Role) obj;
    return super.equals(obj) && Objects.equals(scopes, that.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getScopes());
  }

  public Set<Scope> getScopes() {
    return scopes;
  }

  public void setScopes(Set<Scope> scopes) {
    this.scopes = scopes;
  }

  public void addScope(Scope scope) {
    if (this.scopes == null) {
      this.scopes = new HashSet<>();
    }
    this.scopes.add(scope);
  }

  public void addScopes(Set<Scope> scopes) {
    if (this.scopes == null) {
      this.scopes = new HashSet<>();
    }
    this.scopes.addAll(scopes);
  }
}
