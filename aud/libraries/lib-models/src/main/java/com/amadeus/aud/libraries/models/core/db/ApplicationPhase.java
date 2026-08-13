package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhase;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class ApplicationPhase extends AbstractBase<V1ApplicationPhase> {

  @ManyToOne(targetEntity = MacroPhase.class, cascade = CascadeType.PERSIST)
  private MacroPhase macroPhase;

  //  @OneToMany(mappedBy = "applicationPhase", cascade = CascadeType.PERSIST)
  @Transient private Set<ComponentInstance> componentInstances;

  public ApplicationPhase(V1ApplicationPhase applicationPhase) {
    super(
        applicationPhase,
        Objects.requireNonNull(
                Objects.requireNonNull(applicationPhase, "applicationPhase cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ApplicationPhase() {
    super();
  }

  public @Nullable String getMacroPhaseName() {
    if (macroPhase == null) {
      return null;
    }
    return macroPhase.getName();
  }

  public boolean isCustomerFacing() {
    return getData().getSpec().getIsCustomerFacing();
  }

  public MacroPhase getMacroPhase() {
    return macroPhase;
  }

  public void setMacroPhase(MacroPhase macroPhase) {
    this.macroPhase = macroPhase;
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
}
