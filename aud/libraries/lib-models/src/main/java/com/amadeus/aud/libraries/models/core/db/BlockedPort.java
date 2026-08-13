package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
import jakarta.persistence.Entity;
import java.util.Objects;

@Entity
public class BlockedPort extends AbstractBase<V1BlockedPort> {

  public BlockedPort(V1BlockedPort blockedPort) {
    super(
        blockedPort,
        Objects.requireNonNull(
                Objects.requireNonNull(blockedPort, "blockedPort cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public BlockedPort() {
    super();
  }

  public Integer getPort() {
    return getData().getSpec().getPort();
  }

  public V1BlockedPortSpec.ProtocolEnum getProtocol() {
    return getData().getSpec().getProtocol();
  }
}
