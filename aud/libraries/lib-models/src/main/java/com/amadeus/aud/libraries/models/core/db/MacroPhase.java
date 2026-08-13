package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhase;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhaseSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class MacroPhase extends AbstractBase<V1MacroPhase> {

  @OneToMany(mappedBy = "macroPhase", cascade = CascadeType.PERSIST)
  private Set<ApplicationPhase> applicationPhases;

  @OneToMany(mappedBy = "macroPhase", cascade = CascadeType.PERSIST)
  private Set<Target> targets;

  @Transient private Set<ConnectionPhase> connectionPhases;

  public MacroPhase(V1MacroPhase macroPhase) {
    super(
        macroPhase,
        Objects.requireNonNull(
                Objects.requireNonNull(macroPhase, "macroPhase cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public MacroPhase() {
    super();
  }

  public @Nullable Integer getAutomationOrder() {
    V1MacroPhaseSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getAutomationOrder();
  }

  public Set<ApplicationPhase> getApplicationPhases() {
    return applicationPhases;
  }

  public void setApplicationPhases(Set<ApplicationPhase> applicationPhases) {
    this.applicationPhases = applicationPhases;
  }

  public void addApplicationPhase(ApplicationPhase applicationPhase) {
    if (applicationPhases == null) {
      applicationPhases = new HashSet<>();
    }
    applicationPhases.add(applicationPhase);
  }

  public void addApplicationPhases(Set<ApplicationPhase> applicationPhases) {
    if (this.applicationPhases == null) {
      this.applicationPhases = new HashSet<>();
    }
    this.applicationPhases.addAll(applicationPhases);
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

  public Set<ConnectionPhase> getConnectionPhases() {
    return connectionPhases;
  }

  public void setConnectionPhases(Set<ConnectionPhase> connectionPhases) {
    this.connectionPhases = connectionPhases;
  }

  public void addConnectionPhase(ConnectionPhase connectionPhase) {
    if (connectionPhases == null) {
      connectionPhases = new HashSet<>();
    }
    connectionPhases.add(connectionPhase);
  }

  public void addConnectionPhases(Set<ConnectionPhase> connectionPhases) {
    if (this.connectionPhases == null) {
      this.connectionPhases = new HashSet<>();
    }
    this.connectionPhases.addAll(connectionPhases);
  }

  public Set<String> getTargetNames() {
    Set<String> names = new HashSet<>();
    if (targets != null) {
      for (Target target : targets) {
        names.add(target.id());
      }
    }
    return names;
  }

  public Set<String> getApplicationPhaseNames() {
    Set<String> names = new HashSet<>();
    if (applicationPhases != null) {
      for (ApplicationPhase phase : applicationPhases) {
        names.add(phase.id());
      }
    }
    return names;
  }
}
