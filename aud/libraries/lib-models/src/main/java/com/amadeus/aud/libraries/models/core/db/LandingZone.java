package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class LandingZone extends AbstractBase<V1LandingZone> {

  @ManyToOne(targetEntity = Target.class, cascade = CascadeType.PERSIST)
  private Target target;

  @ManyToOne(targetEntity = Region.class, cascade = CascadeType.PERSIST)
  private Region region;

  @ManyToOne(targetEntity = LandingZoneType.class, cascade = CascadeType.PERSIST)
  private LandingZoneType landingZoneType;

  @ManyToOne(targetEntity = MacroPhase.class, cascade = CascadeType.PERSIST)
  private MacroPhase macroPhase;

  @ManyToMany(mappedBy = "landingZones", cascade = CascadeType.PERSIST)
  private Set<LandingZoneFunctionalType> landingZoneFunctionalTypes;

  @OneToMany(
      targetEntity = ZoneInstance.class,
      mappedBy = "landingZone",
      cascade = CascadeType.PERSIST)
  private Set<ZoneInstance> zoneInstances;

  //  @OneToMany(mappedBy = "landingZone", cascade = CascadeType.ALL)
  @Transient private Set<SubnetInstance> subnetInstances;

  //  @OneToMany(mappedBy = "landingZone", cascade = CascadeType.ALL)
  @Transient private Set<ComponentInstance> componentInstances;

  //  @OneToMany(mappedBy = "landingZone", cascade = CascadeType.ALL)
  @Transient private Set<AutomaticTemplateInstance> automaticTemplateInstances;

  //  @OneToMany(mappedBy = "landingZone", cascade = CascadeType.ALL)
  @Transient private Set<IPAddressInstance> ipAddressInstances;

  //  @OneToMany(mappedBy = "landingZone", cascade = CascadeType.ALL)
  @Transient private Set<DependencyInstance> dependencyInstances;

  //  Computed at init time: Target's provider_specific merged with LandingZone's provider_specific.
  @Transient private Map<String, Object> mergedProviderSpecific;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Boolean, Set<Endpoint>> internalEndpoints;

  public LandingZone(V1LandingZone landingZone) {
    super(
        landingZone,
        Objects.requireNonNull(
                Objects.requireNonNull(landingZone, "applicationID cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public LandingZone() {
    super();
  }

  public List<String> getCidrs() {
    return getData().getSpec().getCidrs();
  }

  public @Nullable String getTargetName() {
    if (target == null) {
      return null;
    }
    return target.getName();
  }

  public @Nullable String getRegionName() {
    if (region == null) {
      return null;
    }
    return region.getName();
  }

  public @Nullable String getLandingZoneTypeName() {
    if (landingZoneType == null) {
      return null;
    }
    return landingZoneType.getName();
  }

  public @Nullable String getMacroPhaseName() {
    if (macroPhase == null) {
      return null;
    }
    return macroPhase.getName();
  }

  public @Nullable Set<String> getFunctionalTypeJsonRepresentation() {
    if (landingZoneFunctionalTypes == null) {
      return null;
    }
    Set<String> functionalTypeNames = new HashSet<>();
    for (LandingZoneFunctionalType functionalType : landingZoneFunctionalTypes) {
      functionalTypeNames.add(functionalType.getName());
    }
    return functionalTypeNames;
  }

  public @Nullable Map<String, Object> getMergedProviderSpecific() {
    return mergedProviderSpecific;
  }

  public void setMergedProviderSpecific(@Nullable Map<String, Object> mergedProviderSpecific) {
    this.mergedProviderSpecific = mergedProviderSpecific;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Target target) {
    this.target = target;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public LandingZoneType getLandingZoneType() {
    return landingZoneType;
  }

  public void setLandingZoneType(LandingZoneType landingZoneType) {
    this.landingZoneType = landingZoneType;
  }

  public MacroPhase getMacroPhase() {
    return macroPhase;
  }

  public void setMacroPhase(MacroPhase macroPhase) {
    this.macroPhase = macroPhase;
  }

  public Set<LandingZoneFunctionalType> getLandingZoneFunctionalTypes() {
    return landingZoneFunctionalTypes;
  }

  public void setLandingZoneFunctionalTypes(
      Set<LandingZoneFunctionalType> landingZoneFunctionalTypes) {
    this.landingZoneFunctionalTypes = landingZoneFunctionalTypes;
  }

  public void addLandingZoneFunctionalType(LandingZoneFunctionalType landingZoneFunctionalType) {
    if (landingZoneFunctionalTypes == null) {
      landingZoneFunctionalTypes = new HashSet<>();
    }
    landingZoneFunctionalTypes.add(landingZoneFunctionalType);
  }

  public void addLandingZoneFunctionalTypes(
      Set<LandingZoneFunctionalType> landingZoneFunctionalTypes) {
    if (this.landingZoneFunctionalTypes == null) {
      this.landingZoneFunctionalTypes = new HashSet<>();
    }
    this.landingZoneFunctionalTypes.addAll(landingZoneFunctionalTypes);
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
  }

  public void addSubnetInstances(Set<SubnetInstance> subnetInstances) {
    if (this.subnetInstances == null) {
      this.subnetInstances = new HashSet<>();
    }
    this.subnetInstances.addAll(subnetInstances);
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

  public Set<AutomaticTemplateInstance> getAutomaticTemplateInstances() {
    return automaticTemplateInstances;
  }

  public void setAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    this.automaticTemplateInstances = automaticTemplateInstances;
  }

  public void addAutomaticTemplateInstance(AutomaticTemplateInstance automaticTemplateInstance) {
    if (automaticTemplateInstances == null) {
      automaticTemplateInstances = new HashSet<>();
    }
    automaticTemplateInstances.add(automaticTemplateInstance);
  }

  public void addAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    if (this.automaticTemplateInstances == null) {
      this.automaticTemplateInstances = new HashSet<>();
    }
    this.automaticTemplateInstances.addAll(automaticTemplateInstances);
  }

  public Set<IPAddressInstance> getIpAddressInstances() {
    return ipAddressInstances;
  }

  public void setIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    this.ipAddressInstances = ipAddressInstances;
  }

  public void addIpAddressInstance(IPAddressInstance ipAddressInstance) {
    if (ipAddressInstances == null) {
      ipAddressInstances = new HashSet<>();
    }
    ipAddressInstances.add(ipAddressInstance);
  }

  public void addIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    if (this.ipAddressInstances == null) {
      this.ipAddressInstances = new HashSet<>();
    }
    this.ipAddressInstances.addAll(ipAddressInstances);
  }

  public Set<DependencyInstance> getDependencyInstances() {
    return dependencyInstances;
  }

  public void setDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public void addDependencyInstance(DependencyInstance dependencyInstance) {
    if (dependencyInstances == null) {
      dependencyInstances = new HashSet<>();
    }
    dependencyInstances.add(dependencyInstance);
  }

  public void addDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.addAll(dependencyInstances);
  }

  public Set<Endpoint> getInternalEndpoints(boolean useCidr) {
    if (internalEndpoints == null) {
      return initInternalEndpoints(useCidr);
    }
    return internalEndpoints.getOrDefault(useCidr, initInternalEndpoints(useCidr));
  }

  public Set<Endpoint> getInternalEndpoints() {
    return getInternalEndpoints(false);
  }

  public Set<Endpoint> initInternalEndpoints(boolean useCidr) {
    if (internalEndpoints == null) {
      internalEndpoints = new HashMap<>();
    }

    Set<Endpoint> endpoints = new HashSet<>();
    if (useCidr) {
      addCidrEndpoints(endpoints);
    } else {
      addSubnetEndpoints(endpoints);
    }

    internalEndpoints.put(useCidr, endpoints);
    return endpoints;
  }

  private void addCidrEndpoints(Set<Endpoint> endpoints) {
    for (String cidr : getCidrs()) {
      endpoints.add(
          new Endpoint(
              Endpoint.TypeEnum.CIDR, cidr, String.join(" ", "LandingZone CIDR", getName())));
    }
  }

  private void addSubnetEndpoints(Set<Endpoint> endpoints) {
    Set<SubnetInstance> _subnetInstances = getSubnetInstances();
    if (_subnetInstances == null) {
      return;
    }

    for (SubnetInstance subnetInstance : _subnetInstances) {
      if (shouldIncludeSubnet(subnetInstance)) {
        endpoints.addAll(subnetInstance.getInternalEndpoints(false));
      }
    }
  }

  private boolean shouldIncludeSubnet(SubnetInstance subnetInstance) {
    // Skip group subnets
    if (subnetInstance.isGroup()) {
      return false;
    }

    // Check for the special Global subnet case
    boolean isGlobalSubnet = Objects.equals(subnetInstance.getSubnetTypeName(), "Global");
    if (isGlobalSubnet) {
      String name = getName();
      boolean isGlobalLandingZone = Objects.equals(name, "Global");
      boolean isOfficesLandingZone = Objects.equals(name, "Offices");
      boolean subnetInOffice = Objects.equals(subnetInstance.getLandingZoneName(), "Office");

      // Skip Global subnets in specific scenarios
      return (!isGlobalLandingZone || subnetInOffice) && (!isOfficesLandingZone || !subnetInOffice);
    }

    return true;
  }
}
