package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
public class FirewallGroup extends AbstractBase<V1FirewallGroup> {

  @ManyToOne(targetEntity = FirewallVendor.class, cascade = CascadeType.ALL)
  private FirewallVendor firewallVendor;

  @ManyToOne(targetEntity = Target.class, cascade = CascadeType.ALL)
  private Target target;

  //  @OneToMany(mappedBy = "firewallGroup", cascade = CascadeType.ALL)
  @Transient private Set<ZoneInstance> zoneInstances;

  public FirewallGroup(V1FirewallGroup firewallGroup) {
    super(
        firewallGroup,
        Objects.requireNonNull(
                Objects.requireNonNull(firewallGroup, "firewallGroup cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public FirewallGroup() {
    super();
  }

  public @Nullable String getExternalIps() {
    List<String> externalIps = getData().getSpec().getExternalIps();
    if (externalIps == null) {
      return null;
    }
    return String.join(",", externalIps);
  }

  public @Nullable String getFirewallVendorName() {
    if (firewallVendor == null) {
      return null;
    }
    return firewallVendor.getName();
  }

  public @Nullable String getTargetName() {
    if (target == null) {
      return null;
    }
    return target.getName();
  }

  public FirewallVendor getFirewallVendor() {
    return firewallVendor;
  }

  public void setFirewallVendor(FirewallVendor firewallVendor) {
    this.firewallVendor = firewallVendor;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Target target) {
    this.target = target;
  }

  public Set<ZoneInstance> getZoneInstances() {
    return zoneInstances;
  }

  public void setZoneInstances(Set<ZoneInstance> zoneInstances) {
    this.zoneInstances = zoneInstances;
  }

  public void addZoneInstance(ZoneInstance zoneInstance) {
    if (zoneInstances == null) {
      zoneInstances = new HashSet<>();
    }
    zoneInstances.add(zoneInstance);
  }

  public void addZoneInstances(Set<ZoneInstance> zoneInstances) {
    if (this.zoneInstances == null) {
      this.zoneInstances = new HashSet<>();
    }
    this.zoneInstances.addAll(zoneInstances);
  }

  public List<Map<String, String>> getSimpleZoneInstances() {
    return getSimpleJsonRepresentation(getZoneInstances());
  }
}
