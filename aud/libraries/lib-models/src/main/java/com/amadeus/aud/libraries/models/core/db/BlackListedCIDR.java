package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import jakarta.persistence.Entity;
import java.util.List;
import java.util.Objects;

@Entity
public class BlackListedCIDR extends AbstractBase<V1BlackListedCIDR> {

  public BlackListedCIDR(V1BlackListedCIDR blackListedCIDR) {
    super(
        blackListedCIDR,
        Objects.requireNonNull(
                Objects.requireNonNull(blackListedCIDR, "blackListedCIDR cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public BlackListedCIDR() {
    super();
  }

  public String getCidr() {
    return getData().getSpec().getCidr();
  }

  public List<V1BlackListedCIDRSpec.TransportLayerEnum> getTransportLayer() {
    return getData().getSpec().getTransportLayer();
  }

  public List<String> getPorts() {
    return getData().getSpec().getPorts();
  }
}
