package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class Connection extends AbstractBase<V1Connection> {

  @ManyToOne(targetEntity = ZoneType.class, cascade = CascadeType.ALL)
  private ZoneType sourceZoneType;

  @ManyToOne(targetEntity = ZoneType.class, cascade = CascadeType.ALL)
  private ZoneType destinationZoneType;

  public Connection(V1Connection connection) {
    super(
        connection,
        Objects.requireNonNull(
                Objects.requireNonNull(connection, "connection cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Connection() {
    super();
  }

  public V1ConnectionSpec.LevelEnum getFirewallLevel() {
    return getData().getSpec().getLevel();
  }

  public V1ConnectionSpec.ControlEnum getSecurityControl() {
    return getData().getSpec().getControl();
  }

  public V1ConnectionSpec.TypeEnum getConnectionType() {
    return getData().getSpec().getType();
  }

  public @Nullable String getSourceZoneTypeName() {
    if (sourceZoneType == null) {
      return null;
    }
    return sourceZoneType.getName();
  }

  public @Nullable String getDestinationZoneTypeName() {
    if (destinationZoneType == null) {
      return null;
    }
    return destinationZoneType.getName();
  }

  public ZoneType getSourceZoneType() {
    return sourceZoneType;
  }

  public void setSourceZoneType(ZoneType sourceZoneType) {
    this.sourceZoneType = sourceZoneType;
  }

  public ZoneType getDestinationZoneType() {
    return destinationZoneType;
  }

  public void setDestinationZoneType(ZoneType destinationZoneType) {
    this.destinationZoneType = destinationZoneType;
  }
}
