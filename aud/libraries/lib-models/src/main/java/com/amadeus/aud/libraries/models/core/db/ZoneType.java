package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class ZoneType extends AbstractBase<V1ZoneType> {

  @Transient private Set<Connection> connections;

  //  @OneToMany(targetEntity = ZoneInstance.class, mappedBy = "zoneType")
  @Transient private Set<ZoneInstance> zoneInstances;

  @OneToMany(targetEntity = ComponentInstance.class, mappedBy = "zoneType")
  private Set<ComponentInstance> componentInstances;

  @ManyToOne(targetEntity = Component.class, cascade = CascadeType.ALL)
  private Component component;

  public ZoneType(V1ZoneType zoneType) {
    super(
        zoneType,
        Objects.requireNonNull(
                Objects.requireNonNull(zoneType, "zoneType cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ZoneType() {
    super();
  }

  public boolean isTechnical() {
    return getData().getSpec().getIsTechnical();
  }

  public boolean isReal() {
    return Boolean.TRUE.equals(getData().getSpec().getIsReal());
  }

  public boolean isPaas() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPaas());
  }

  public Set<Connection> getConnections() {
    return connections;
  }

  public void setConnections(Set<Connection> connections) {
    this.connections = connections;
  }

  public void addConnection(Connection connection) {
    if (connections == null) {
      connections = new HashSet<>();
    }
    connections.add(connection);
  }

  public void addConnections(Set<Connection> connections) {
    if (this.connections == null) {
      this.connections = new HashSet<>();
    }
    this.connections.addAll(connections);
  }

  public Set<ZoneInstance> getZoneInstances() {
    return zoneInstances;
  }

  public void setZoneInstances(Set<ZoneInstance> zoneInstances) {
    this.zoneInstances = zoneInstances;
  }

  public void addZoneInstance(ZoneInstance zoneInstance) {
    if (zoneInstances == null) {
      zoneInstances = new HashSet<>();
    }
    zoneInstances.add(zoneInstance);
  }

  public void addZoneInstances(Set<ZoneInstance> zoneInstances) {
    if (this.zoneInstances == null) {
      this.zoneInstances = new HashSet<>();
    }
    this.zoneInstances.addAll(zoneInstances);
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

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }
}
