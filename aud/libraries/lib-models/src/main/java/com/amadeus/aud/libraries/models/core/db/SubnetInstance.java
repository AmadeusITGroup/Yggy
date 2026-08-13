package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.google.common.collect.Range;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class SubnetInstance extends BaseInstance<V1SubnetInstance> implements WithDeprecation {

  @ManyToOne(targetEntity = SubnetType.class, cascade = CascadeType.ALL)
  private SubnetType subnetType;

  @ManyToOne(targetEntity = ZoneInstance.class, cascade = CascadeType.ALL)
  private ZoneInstance zoneInstance;

  @Transient private Set<SubnetInstance> children;

  @Transient private Set<ComponentInstance> componentInstances;

  @Transient private SubnetInstance parent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<Endpoint> internalEndpoints;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private final Range<BigInteger> cidrRange;

  public SubnetInstance(V1SubnetInstance subnetInstance) {
    super(
        subnetInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(subnetInstance, "subnetInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName(),
        Objects.requireNonNull(
                Objects.requireNonNull(subnetInstance, "subnetInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getLandingzone());

    String cidr = subnetInstance.getSpec().getCidr();
    Range<BigInteger> cidrRangeTmp = null;
    if (!cidr.startsWith(Constants.GLOBAL) && !Objects.equals(cidr, Constants.N_A)) {
      try {
        cidrRangeTmp = IPv4Utils.toRange(cidr);
      } catch (Exception e) {
        Log.debugf("Failed to parse CIDR %s", cidr);
      }
    }
    this.cidrRange = cidrRangeTmp;
  }

  public SubnetInstance() {
    super();
    this.cidrRange = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    SubnetInstance subnetInstance = (SubnetInstance) obj;
    return super.equals(obj)
        && Objects.equals(internalEndpoints, subnetInstance.internalEndpoints)
        && Objects.equals(cidrRange, subnetInstance.cidrRange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), internalEndpoints, cidrRange);
  }

  public String getVlan() {
    return getData().getSpec().getVlan();
  }

  public boolean isShared() {
    return getData().getSpec().getIsShared();
  }

  public boolean isGroup() {
    return Boolean.TRUE.equals(getData().getSpec().getIsGroup());
  }

  public boolean isNetworkPrivate() {
    return Boolean.TRUE.equals(getData().getSpec().getIsNetworkPrivate());
  }

  public String getCidr() {
    return getData().getSpec().getCidr();
  }

  @Override
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return getData().getSpec().getDeprecatedStatus();
  }

  @Override
  public @Nullable String getDeprecatedMessage() {
    return getData().getSpec().getDeprecatedMessage();
  }

  public @Nullable String getSubnetTypeName() {
    if (subnetType == null) {
      return null;
    }
    return subnetType.getName();
  }

  public @Nullable String getZoneInstanceName() {
    if (zoneInstance == null) {
      return null;
    }
    return zoneInstance.getName();
  }

  public @Nullable String getParentName() {
    if (parent == null) {
      return null;
    }
    return parent.getName();
  }

  public SubnetType getSubnetType() {
    return subnetType;
  }

  public void setSubnetType(SubnetType subnetType) {
    this.subnetType = subnetType;
  }

  public ZoneInstance getZoneInstance() {
    return zoneInstance;
  }

  public void setZoneInstance(ZoneInstance zoneInstance) {
    this.zoneInstance = zoneInstance;
  }

  public Set<SubnetInstance> getChildren() {
    return children;
  }

  public void setChildren(Set<SubnetInstance> children) {
    this.children = children;
  }

  public void addChild(SubnetInstance child) {
    if (children == null) {
      children = new HashSet<>();
    }
    children.add(child);
  }

  public void addChildren(Set<SubnetInstance> children) {
    if (this.children == null) {
      this.children = new HashSet<>();
    }
    this.children.addAll(children);
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

  public SubnetInstance getParent() {
    return parent;
  }

  public void setParent(SubnetInstance parent) {
    this.parent = parent;
  }

  public Set<Endpoint> getInternalEndpoints(boolean ignoreShared) {
    if (internalEndpoints == null) {
      initInternalEndpoints();
    }

    // TODO: Find a more performant way to do this
    if (isShared() && !ignoreShared) {
      Set<Endpoint> endpoints = new HashSet<>(internalEndpoints);
      endpoints.add(new Endpoint(Endpoint.TypeEnum.CIDR, getCidr(), getName()));
      return endpoints;
    }
    return internalEndpoints;
  }

  public Set<Endpoint> getInternalEndpoints() {
    return getInternalEndpoints(true);
  }

  public void initInternalEndpoints() {
    if (internalEndpoints == null) {
      internalEndpoints = new HashSet<>();
    }

    if (isGlobalSubnetWithLandingZoneEndpoints()) {
      internalEndpoints.addAll(getLandingZone().getInternalEndpoints());
    } else if (isGroup() && !isShared()) {
      addChildrenEndpoints();
    } else if (!isShared()) {
      internalEndpoints.add(new Endpoint(Endpoint.TypeEnum.CIDR, getCidr(), getName()));
    }
  }

  private boolean isGlobalSubnetWithLandingZoneEndpoints() {
    if (!Objects.equals(getSubnetTypeName(), "Global")) {
      return false;
    }

    String name = getName();
    String landingZoneName = getLandingZoneName();

    boolean isGlobalNonOffice =
        Objects.equals(name, "Global") && !Objects.equals(landingZoneName, "Office");
    boolean isOfficesInOffice =
        Objects.equals(name, "Offices") && Objects.equals(landingZoneName, "Office");

    return isGlobalNonOffice || isOfficesInOffice;
  }

  private void addChildrenEndpoints() {
    if (children == null) {
      return;
    }

    for (SubnetInstance child : children) {
      if (!Objects.equals(this, child)) {
        internalEndpoints.addAll(child.getInternalEndpoints());
      }
    }
  }

  public Range<BigInteger> getCidrRange() {
    return cidrRange;
  }
}
