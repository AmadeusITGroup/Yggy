package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class ConnectionPhase extends AbstractBase<V1ConnectionPhase> {

  @ManyToOne(targetEntity = MacroPhase.class, cascade = CascadeType.ALL)
  private MacroPhase sourcePhase;

  @ManyToOne(targetEntity = MacroPhase.class, cascade = CascadeType.ALL)
  private MacroPhase destinationPhase;

  public ConnectionPhase(V1ConnectionPhase connectionPhase) {
    super(
        connectionPhase,
        Objects.requireNonNull(
                Objects.requireNonNull(connectionPhase, "connectionPhase cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ConnectionPhase() {
    super();
  }

  public V1ConnectionPhaseSpec.ControlEnum getControl() {
    return getData().getSpec().getControl();
  }

  public @Nullable String getSourcePhaseName() {
    if (sourcePhase == null) {
      return null;
    }
    return sourcePhase.getName();
  }

  public @Nullable String getDestinationPhaseName() {
    if (destinationPhase == null) {
      return null;
    }
    return destinationPhase.getName();
  }

  public MacroPhase getSourcePhase() {
    return sourcePhase;
  }

  public void setSourcePhase(MacroPhase sourcePhase) {
    this.sourcePhase = sourcePhase;
  }

  public MacroPhase getDestinationPhase() {
    return destinationPhase;
  }

  public void setDestinationPhase(MacroPhase destinationPhase) {
    this.destinationPhase = destinationPhase;
  }
}
