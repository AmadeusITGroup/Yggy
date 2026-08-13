package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Servicetag;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
public class ServiceTag extends AbstractBase<V1Servicetag> {

  @OneToMany(mappedBy = "serviceTag", cascade = CascadeType.ALL)
  private Set<ComponentInstance> componentInstances;

  public ServiceTag(V1Servicetag serviceTag) {
    super(
        serviceTag,
        Objects.requireNonNull(
                Objects.requireNonNull(serviceTag, "serviceTag cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ServiceTag() {
    super();
  }

  public boolean getAnyNeededInNsg() {
    return Boolean.TRUE.equals(getData().getSpec().getAnyNeededInNsg());
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

  public List<Map<String, String>> getSimpleComponentInstances() {
    return getSimpleJsonRepresentation(getComponentInstances());
  }
}
