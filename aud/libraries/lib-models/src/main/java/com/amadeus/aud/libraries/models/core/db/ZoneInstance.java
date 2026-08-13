package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class ZoneInstance extends BaseInstance<V1ZoneInstance> {

  //  @ManyToOne(targetEntity = ZoneType.class, cascade = CascadeType.ALL)
  @Transient private ZoneType zoneType;

  @ManyToOne(targetEntity = FirewallGroup.class, cascade = CascadeType.ALL)
  private FirewallGroup firewallGroup;

  //  @OneToMany(mappedBy = "zoneInstance", cascade = CascadeType.ALL)
  @Transient private Set<SubnetInstance> subnetInstances;

  @Transient private Set<ComponentInstance> componentInstances;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<Endpoint> internalEndpoints;

  @Transient private RangeSet<BigInteger> internalDstCidrRangeSetForNSG;

  public ZoneInstance(V1ZoneInstance zoneInstance) {
    super(
        zoneInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(zoneInstance, "zoneInstance cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName(),
        Objects.requireNonNull(
                Objects.requireNonNull(zoneInstance, "zoneInstance cannot be null").getMetadata(),
                "metadata cannot be null")
            .getLandingzone());
  }

  public ZoneInstance() {
    super();
  }

  public boolean isNsgNeeded() {
    return Boolean.TRUE.equals(getData().getSpec().getIsNSGNeeded());
  }

  public @Nullable String getZoneTypeName() {
    if (zoneType == null) {
      return null;
    }
    return zoneType.getName();
  }

  public @Nullable String getFirewallGroupName() {
    if (firewallGroup == null) {
      return null;
    }
    return firewallGroup.getName();
  }

  public ZoneType getZoneType() {
    return zoneType;
  }

  public void setZoneType(ZoneType zoneType) {
    this.zoneType = zoneType;
  }

  public FirewallGroup getFirewallGroup() {
    return firewallGroup;
  }

  public void setFirewallGroup(FirewallGroup firewallGroup) {
    this.firewallGroup = firewallGroup;
  }

  public Set<SubnetInstance> getSubnetInstances() {
    return subnetInstances;
  }

  public void setSubnetInstances(Set<SubnetInstance> subnetInstances) {
    this.subnetInstances = subnetInstances;
  }

  public void addSubnetInstance(SubnetInstance subnetInstance) {
    if (subnetInstances == null) {
      subnetInstances = new HashSet<>();
    }
    subnetInstances.add(subnetInstance);
    addInternalEndpoints(subnetInstance.getInternalEndpoints());
  }

  public void addSubnetInstances(Set<SubnetInstance> subnetInstances) {
    if (this.subnetInstances == null) {
      this.subnetInstances = new HashSet<>();
    }
    this.subnetInstances.addAll(subnetInstances);
    for (SubnetInstance subnetInstance : subnetInstances) {
      addInternalEndpoints(subnetInstance.getInternalEndpoints());
    }
  }

  public Set<ComponentInstance> getComponentInstances() {
    return componentInstances;
  }

  public void setComponentInstances(Set<ComponentInstance> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public void addComponentInstance(ComponentInstance componentInstance) {
    if (componentInstances == null) {
      componentInstances = new HashSet<>();
    }
    componentInstances.add(componentInstance);
  }

  public void addComponentInstances(Set<ComponentInstance> componentInstances) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.addAll(componentInstances);
  }

  public Set<Endpoint> getInternalEndpoints() {
    return internalEndpoints;
  }

  public void setInternalEndpoints(Set<Endpoint> internalEndpoints) {
    this.internalEndpoints = internalEndpoints;
  }

  public void addInternalEndpoint(Endpoint internalEndpoint) {
    if (internalEndpoints == null) {
      internalEndpoints = new HashSet<>();
    }
    internalEndpoints.add(internalEndpoint);
  }

  public void addInternalEndpoints(Set<Endpoint> internalEndpoints) {
    if (this.internalEndpoints == null) {
      this.internalEndpoints = new HashSet<>();
    }
    this.internalEndpoints.addAll(internalEndpoints);
  }

  public RangeSet<BigInteger> getInternalDstCidrRangeSetForNSG() {
    if (internalDstCidrRangeSetForNSG != null) {
      return internalDstCidrRangeSetForNSG;
    }

    return initInternalDstCidrRangeSetForNSG();
  }

  private RangeSet<BigInteger> initInternalDstCidrRangeSetForNSG() {
    internalDstCidrRangeSetForNSG = TreeRangeSet.create();
    Set<SubnetInstance> subnetInstances = getSubnetInstances();
    if (subnetInstances == null) {
      return internalDstCidrRangeSetForNSG;
    }

    for (SubnetInstance subnetInstance : subnetInstances) {
      if (!subnetInstance.isGroup()
          && !Objects.equals(subnetInstance.getSubnetTypeName(), Constants.GLOBAL)) {
        Range<BigInteger> cidrRange = subnetInstance.getCidrRange();
        if (cidrRange != null) {
          internalDstCidrRangeSetForNSG.add(subnetInstance.getCidrRange());
        }
      }
    }

    return internalDstCidrRangeSetForNSG;
  }

  public List<Map<String, String>> getSimpleSubnetInstances() {
    return getSimpleJsonRepresentation(getSubnetInstances());
  }
}
