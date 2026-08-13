package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneFunctionalType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class LandingZoneFunctionalType extends AbstractBase<V1LandingzoneFunctionalType> {

  private boolean isGenerated;

  @ManyToMany(targetEntity = LandingZone.class)
  private Set<LandingZone> landingZones;

  @SuppressWarnings("this-escape")
  public LandingZoneFunctionalType(Scope scope, String name, String comment, boolean isGenerated) {
    super(name);
    this.isGenerated = isGenerated;
    this.setScope(scope);
    this.setName(name);
    this.setComment(comment);
  }

  public LandingZoneFunctionalType(V1LandingzoneFunctionalType landingZoneFunctionalType) {
    super(
        landingZoneFunctionalType,
        Objects.requireNonNull(
                Objects.requireNonNull(
                        landingZoneFunctionalType, "landingZoneFunctionalType cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
    this.isGenerated = false;
  }

  public LandingZoneFunctionalType() {
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

    LandingZoneFunctionalType that = (LandingZoneFunctionalType) obj;
    return super.equals(obj) && isGenerated == that.isGenerated;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isGenerated);
  }

  public boolean isGenerated() {
    return isGenerated;
  }

  public void setGenerated(boolean generated) {
    isGenerated = generated;
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
