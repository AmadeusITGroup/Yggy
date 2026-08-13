package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1FirewallVendor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class FirewallVendor extends AbstractBase<V1FirewallVendor> {

  @OneToMany(mappedBy = "firewallVendor", cascade = CascadeType.ALL)
  private Set<FirewallGroup> firewallGroups;

  public FirewallVendor(V1FirewallVendor firewallVendor) {
    super(
        firewallVendor,
        Objects.requireNonNull(
                Objects.requireNonNull(firewallVendor, "firewallVendor cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public FirewallVendor() {
    super();
  }

  @JsonIgnore
  public Set<FirewallGroup> getFirewallGroups() {
    return firewallGroups;
  }

  public Set<String> getFirewallGroupNames() {
    Set<String> names = new HashSet<>();
    if (firewallGroups != null) {
      for (FirewallGroup group : firewallGroups) {
        names.add(group.id());
      }
    }
    return names;
  }

  public void setFirewallGroups(Set<FirewallGroup> firewallGroups) {
    this.firewallGroups = firewallGroups;
  }

  public void addFirewallGroup(FirewallGroup firewallGroup) {
    if (firewallGroups == null) {
      firewallGroups = new HashSet<>();
    }
    firewallGroups.add(firewallGroup);
  }

  public void addFirewallGroups(Set<FirewallGroup> firewallGroups) {
    if (this.firewallGroups == null) {
      this.firewallGroups = new HashSet<>();
    }
    this.firewallGroups.addAll(firewallGroups);
  }
}
