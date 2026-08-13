package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class LandingZoneType extends AbstractBase<V1LandingzoneType> {

  @OneToMany(mappedBy = "landingZoneType", cascade = CascadeType.ALL)
  private Set<LandingZone> landingZones;

  public LandingZoneType(V1LandingzoneType landingZoneType) {
    super(
        landingZoneType,
        Objects.requireNonNull(
                Objects.requireNonNull(landingZoneType, "landingZoneType cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public LandingZoneType() {
    super();
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
