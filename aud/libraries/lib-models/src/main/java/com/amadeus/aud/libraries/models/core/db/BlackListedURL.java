package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import jakarta.persistence.Entity;
import java.util.List;
import java.util.Objects;

@Entity
public class BlackListedURL extends AbstractBase<V1BlackListedURL> {

  public BlackListedURL(V1BlackListedURL blackListedURL) {
    super(
        blackListedURL,
        Objects.requireNonNull(
                Objects.requireNonNull(blackListedURL, "blackListedURL cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public BlackListedURL() {
    super();
  }

  public String getFqdn() {
    return getData().getSpec().getFqdn();
  }

  public List<V1BlackListedURLSpec.TransportLayerEnum> getTransportLayer() {
    return getData().getSpec().getTransportLayer();
  }

  public List<String> getPorts() {
    return getData().getSpec().getPorts();
  }
}
