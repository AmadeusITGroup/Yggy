package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1SubnetType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
public class SubnetType extends AbstractBase<V1SubnetType> {

  @OneToMany(mappedBy = "subnetType", cascade = CascadeType.ALL)
  private Set<SubnetInstance> subnetInstances;

  public SubnetType(V1SubnetType subnetType) {
    super(
        subnetType,
        Objects.requireNonNull(
                Objects.requireNonNull(subnetType, "subnetType cannot be null").getMetadata(),
                "metadata name cannot be null")
            .getName());
  }

  public SubnetType() {
    super();
  }

  public Set<SubnetInstance> getSubnetInstances() {
    return subnetInstances;
  }

  public void setSubnetInstances(Set<SubnetInstance> subnetInstances) {
    this.subnetInstances = subnetInstances;
  }

  public void addSubnetInstance(SubnetInstance subnetInstance) {
    if (subnetInstances == null) {
      subnetInstances = new HashSet<>();
    }
    subnetInstances.add(subnetInstance);
  }

  public void addSubnetInstances(Set<SubnetInstance> subnetInstances) {
    if (this.subnetInstances == null) {
      this.subnetInstances = new HashSet<>();
    }
    this.subnetInstances.addAll(subnetInstances);
  }

  public List<Map<String, String>> getSimpleSubnetInstances() {
    return getSimpleJsonRepresentation(getSubnetInstances());
  }
}
