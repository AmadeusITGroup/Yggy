package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
public class IPAddressInstance extends BaseInstance<V1IPAddressInstance> {

  @ManyToOne(targetEntity = ComponentInstance.class, cascade = CascadeType.ALL)
  private ComponentInstance componentInstance;

  public IPAddressInstance(V1IPAddressInstance ipAddressInstance) {
    super(
        ipAddressInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(ipAddressInstance, "ipAddressInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName(),
        Objects.requireNonNull(
                Objects.requireNonNull(ipAddressInstance, "ipAddressInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getLandingzone());
  }

  public IPAddressInstance() {
    super();
  }

  public @Nullable String getComponentInstanceName() {
    if (componentInstance == null) {
      return null;
    }
    return componentInstance.getName();
  }

  public String getHost() {
    return getData().getSpec().getHost();
  }

  public V1IPAddressInstanceSpec.AdvertisedToEnum getAdvertisedTo() {
    return getData().getSpec().getAdvertisedTo();
  }

  public ComponentInstance getComponentInstance() {
    return componentInstance;
  }

  public void setComponentInstance(ComponentInstance componentInstance) {
    this.componentInstance = componentInstance;
    this.setUniqueIdFrom(String.join(ID_SEPARATOR, componentInstance.getName(), getName()));
  }
}
