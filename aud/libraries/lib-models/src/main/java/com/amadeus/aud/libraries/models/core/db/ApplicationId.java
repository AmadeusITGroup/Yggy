package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class ApplicationId extends AbstractBase<V1ApplicationID> {

  @OneToMany(mappedBy = "applicationId")
  private Set<Service> services;

  public ApplicationId(V1ApplicationID applicationID) {
    super(
        applicationID,
        Objects.requireNonNull(
                Objects.requireNonNull(applicationID, "applicationID cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ApplicationId() {
    super();
  }

  public Set<Service> getServices() {
    return services;
  }

  public void setServices(Set<Service> services) {
    this.services = services;
  }

  public void addService(Service service) {
    if (services == null) {
      services = new HashSet<>();
    }
    services.add(service);
  }

  public void addServices(Set<Service> services) {
    if (this.services == null) {
      this.services = new HashSet<>();
    }
    this.services.addAll(services);
  }
}
