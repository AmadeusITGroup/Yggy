package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkip;
import jakarta.persistence.Entity;
import java.util.Objects;

@Entity
public class ConnectionApprovalScopeSkip extends AbstractBase<V1ConnectionApprovalScopeSkip> {

  private String status;

  public ConnectionApprovalScopeSkip(V1ConnectionApprovalScopeSkip connectionApprovalScopeSkip) {
    super(
        connectionApprovalScopeSkip,
        Objects.requireNonNull(
                Objects.requireNonNull(
                        connectionApprovalScopeSkip, "connectionApprovalScopeSkip cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ConnectionApprovalScopeSkip() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ConnectionApprovalScopeSkip that = (ConnectionApprovalScopeSkip) obj;
    return super.equals(obj) && Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), status);
  }

  public boolean getSkipSource() {
    return getData().getSpec().getSource();
  }

  public boolean getSkipDestination() {
    return getData().getSpec().getDestination();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
