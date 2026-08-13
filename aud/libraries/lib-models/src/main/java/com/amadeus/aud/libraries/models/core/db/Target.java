package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1Target;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Target extends AbstractBase<V1Target> {

  @ManyToOne(targetEntity = Provider.class, cascade = CascadeType.PERSIST)
  private Provider provider;

  @ManyToOne(targetEntity = TargetType.class, cascade = CascadeType.PERSIST)
  private TargetType targetType;

  @ManyToOne(targetEntity = MacroPhase.class, cascade = CascadeType.PERSIST)
  private MacroPhase macroPhase;

  @ManyToOne(targetEntity = Continent.class, cascade = CascadeType.PERSIST)
  private Continent continent;

  @ManyToOne(targetEntity = Target.class, cascade = CascadeType.PERSIST)
  private Target popPrimary;

  @ManyToOne(targetEntity = Target.class, cascade = CascadeType.PERSIST)
  private Target popSecondary;

  @OneToMany(mappedBy = "target", cascade = CascadeType.PERSIST)
  private Set<FirewallGroup> firewallGroups;

  @OneToMany(mappedBy = "target", cascade = CascadeType.PERSIST)
  private Set<LandingZone> landingZones;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Boolean, Set<Endpoint>> internalEndpoints;

  public Target(V1Target target) {
    super(
        target,
        Objects.requireNonNull(
                Objects.requireNonNull(target, "target cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Target() {
    super();
  }

  public List<String> getCidrs() {
    return getData().getSpec().getCidrs();
  }

  public @Nullable String getTargetTypeName() {
    if (targetType == null) {
      return null;
    }
    return targetType.getName();
  }

  public boolean isEnableOfficeConnectivity() {
    return Boolean.TRUE.equals(getData().getSpec().getEnableOfficeConnectivity());
  }

  public boolean isEnableJumpServerAccess() {
    return Boolean.TRUE.equals(getData().getSpec().getEnableJumpserverConnectivity());
  }

  public boolean isEnableVnetToVnet() {
    return Boolean.TRUE.equals(getData().getSpec().getEnableVnetToVnet());
  }

  public boolean isCloud() {
    return Boolean.TRUE.equals(getData().getSpec().getIsCloud());
  }

  public boolean isAmadeus() {
    return Boolean.TRUE.equals(getData().getSpec().getIsAmadeus());
  }

  public boolean isAudManaged() {
    return Boolean.TRUE.equals(getData().getSpec().getIsAudManaged());
  }

  public @Nullable String getProviderName() {
    if (provider == null) {
      return null;
    }
    return provider.getName();
  }

  public @Nullable String getMacroPhaseName() {
    if (macroPhase == null) {
      return null;
    }
    return macroPhase.getName();
  }

  public int getMacroPhaseOrder() {
    return macroPhase.getAutomationOrder();
  }

  public @Nullable String getContinentName() {
    if (continent == null) {
      return null;
    }
    return continent.getName();
  }

  public @Nullable String getPopPrimaryName() {
    if (popPrimary == null) {
      return null;
    }
    return popPrimary.getName();
  }

  public @Nullable String getPopSecondaryName() {
    if (popSecondary == null) {
      return null;
    }
    return popSecondary.getName();
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public void setTargetType(TargetType targetType) {
    this.targetType = targetType;
  }

  public MacroPhase getMacroPhase() {
    return macroPhase;
  }

  public void setMacroPhase(MacroPhase macroPhase) {
    this.macroPhase = macroPhase;
  }

  public Continent getContinent() {
    return continent;
  }

  public void setContinent(Continent continent) {
    this.continent = continent;
  }

  public Target getPopPrimary() {
    return popPrimary;
  }

  public void setPopPrimary(Target popPrimary) {
    this.popPrimary = popPrimary;
  }

  public Target getPopSecondary() {
    return popSecondary;
  }

  public void setPopSecondary(Target popSecondary) {
    this.popSecondary = popSecondary;
  }

  public Set<FirewallGroup> getFirewallGroups() {
    return firewallGroups;
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

  public Set<LandingZone> getLandingZones() {
    return landingZones;
  }

  public void setLandingZones(Set<LandingZone> landingZones) {
    this.landingZones = landingZones;
  }

  public void addLandingZone(LandingZone landingZone) {
    if (landingZones == null) {
      landingZones = new HashSet<>();
    }
    landingZones.add(landingZone);
  }

  public void addLandingZones(Set<LandingZone> landingZones) {
    if (this.landingZones == null) {
      this.landingZones = new HashSet<>();
    }
    this.landingZones.addAll(landingZones);
  }

  @JsonIgnore
  public Set<Endpoint> getInternalEndpoints() {
    return getInternalEndpoints(false);
  }

  public Set<Endpoint> getInternalEndpoints(boolean useCidr) {
    if (internalEndpoints == null) {
      return initInternalEndpoints(useCidr);
    }

    Set<Endpoint> endpoints = internalEndpoints.get(useCidr);
    if (endpoints == null) {
      endpoints = initInternalEndpoints(useCidr);
    }
    return endpoints;
  }

  private Set<Endpoint> initInternalEndpoints(boolean useCidr) {
    if (internalEndpoints == null) {
      internalEndpoints = new HashMap<>();
    }
    Set<Endpoint> endpoints = new HashSet<>();

    if (useCidr) {
      addCidrEndpoints(endpoints);
    } else {
      addLandingZoneEndpoints(endpoints);
    }

    internalEndpoints.put(useCidr, endpoints);
    return endpoints;
  }

  private void addCidrEndpoints(Set<Endpoint> endpoints) {
    List<String> cidrs = getCidrs();
    if (cidrs == null) {
      return;
    }

    for (String cidr : cidrs) {
      if (!Objects.equals(cidr, Constants.N_A)) {
        endpoints.add(
            new Endpoint(Endpoint.TypeEnum.CIDR, cidr, String.join(" ", "Target CIDR", getName())));
      }
    }
  }

  private void addLandingZoneEndpoints(Set<Endpoint> endpoints) {
    if (this.landingZones == null) {
      return;
    }

    for (LandingZone landingZone : landingZones) {
      endpoints.addAll(landingZone.getInternalEndpoints(false));
    }
  }
}
