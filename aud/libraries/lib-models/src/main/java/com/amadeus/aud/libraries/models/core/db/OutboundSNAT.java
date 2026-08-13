package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNAT;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNATSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class OutboundSNAT extends AbstractBase<V1OutboundSNAT> {

  @ManyToOne(targetEntity = MacroPhase.class)
  private MacroPhase macroPhase;

  @ManyToOne(targetEntity = Continent.class)
  private Continent continent;

  @ManyToOne(targetEntity = ZoneType.class)
  private ZoneType zoneType;

  @ManyToMany(targetEntity = Component.class)
  private Set<Component> components;

  @ManyToMany(targetEntity = ComponentInstance.class)
  private @Nullable Set<ComponentInstance> componentInstances;

  public OutboundSNAT(V1OutboundSNAT outboundSNAT) {
    super(
        outboundSNAT,
        Objects.requireNonNull(
                Objects.requireNonNull(outboundSNAT, "outboundSNAT cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public OutboundSNAT() {
    super();
  }

  public V1OutboundSNATSpec.ProtocolEnum getProtocol() {
    return getData().getSpec().getProtocol();
  }

  public String getTranslatedIp() {
    return getData().getSpec().getTranslatedip();
  }

  public List<String> getCidrs() {
    return getData().getSpec().getCidrs();
  }

  public List<String> getDestinationCidrs() {
    return getData().getSpec().getDestinationCidrs();
  }

  public @Nullable String getMacroPhaseName() {
    if (macroPhase == null) {
      return null;
    }
    return macroPhase.getName();
  }

  public @Nullable String getContinentName() {
    if (continent == null) {
      return null;
    }
    return continent.getName();
  }

  public @Nullable String getZoneTypeName() {
    if (zoneType == null) {
      return null;
    }
    return zoneType.getName();
  }

  public List<String> getComponentNames() {
    if (components == null) {
      return List.of();
    }

    List<String> result = new ArrayList<>();
    for (Component component : components) {
      result.add(component.getName());
    }
    return result;
  }

  public List<String> getComponentInstanceNames() {
    if (componentInstances == null) {
      return List.of();
    }
    List<String> result = new ArrayList<>();
    for (ComponentInstance componentInstance : componentInstances) {
      result.add(componentInstance.id());
    }
    return result;
  }

  public MacroPhase getMacroPhase() {
    return macroPhase;
  }

  public void setMacroPhase(MacroPhase macroPhase) {
    this.macroPhase = macroPhase;
  }

  public Continent getContinent() {
    return continent;
  }

  public void setContinent(Continent continent) {
    this.continent = continent;
  }

  public ZoneType getZoneType() {
    return zoneType;
  }

  public void setZoneType(ZoneType zoneType) {
    this.zoneType = zoneType;
  }

  public Set<Component> getComponents() {
    return components;
  }

  public void setComponents(Set<Component> components) {
    this.components = components;
  }

  public void addComponent(Component component) {
    if (this.components == null) {
      this.components = new HashSet<>();
    }
    this.components.add(component);
  }

  public void addComponents(Set<Component> components) {
    if (this.components == null) {
      this.components = new HashSet<>();
    }
    this.components.addAll(components);
  }

  public @Nullable Set<ComponentInstance> getComponentInstances() {
    return componentInstances;
  }

  public void setComponentInstances(Set<ComponentInstance> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public void addComponentInstance(ComponentInstance componentInstance) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.add(componentInstance);
  }

  public void addComponentInstances(Set<ComponentInstance> componentInstances) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.addAll(componentInstances);
  }
}
