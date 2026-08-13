package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Region;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Region extends AbstractBase<V1Region> {
  @ManyToOne(targetEntity = Provider.class, cascade = CascadeType.ALL)
  private Provider provider;

  @ManyToOne(targetEntity = Continent.class, cascade = CascadeType.ALL)
  private Continent continent;

  //  @OneToMany(mappedBy = "region")
  @Transient private Set<LandingZone> landingZones;

  public Region(V1Region region) {
    super(
        region,
        Objects.requireNonNull(
                Objects.requireNonNull(region, "region cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Region() {
    super();
  }

  public @Nullable String getProviderName() {
    if (provider == null) {
      return null;
    }
    return provider.getName();
  }

  public @Nullable String getContinentName() {
    if (continent == null) {
      return null;
    }
    return continent.getName();
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public Continent getContinent() {
    return continent;
  }

  public void setContinent(Continent continent) {
    this.continent = continent;
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

  public Set<String> getLandingZoneNames() {
    Set<String> names = new HashSet<>();
    if (landingZones != null) {
      for (LandingZone lz : landingZones) {
        names.add(lz.id());
      }
    }
    return names;
  }
}
