package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
public class TargetType extends AbstractBase<V1TargetType> {

  @OneToMany(mappedBy = "targetType", cascade = CascadeType.ALL)
  private Set<Target> targets;

  public TargetType(V1TargetType targetType) {
    super(
        targetType,
        Objects.requireNonNull(
                Objects.requireNonNull(targetType, "targetType cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public TargetType() {
    super();
  }

  public Set<Target> getTargets() {
    return targets;
  }

  public void setTargets(Set<Target> targets) {
    this.targets = targets;
  }

  public void addTarget(Target target) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.add(target);
  }

  public void addTargets(Set<Target> targets) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.addAll(targets);
  }

  public List<Map<String, String>> getSimpleTargets() {
    return getSimpleJsonRepresentation(getTargets());
  }
}
