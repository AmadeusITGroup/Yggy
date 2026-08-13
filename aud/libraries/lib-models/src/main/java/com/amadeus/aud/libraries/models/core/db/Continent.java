package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Continent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class Continent extends AbstractBase<V1Continent> {

  @OneToMany(mappedBy = "continent", cascade = CascadeType.ALL)
  private Set<Region> regions;

  @OneToMany(mappedBy = "continent", cascade = CascadeType.ALL)
  private Set<Target> targets;

  public Continent(V1Continent continent) {
    super(
        continent,
        Objects.requireNonNull(
                Objects.requireNonNull(continent, "continent cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Continent() {
    super();
  }

  @JsonIgnore
  public Set<Region> getRegions() {
    return regions;
  }

  @JsonProperty("regions")
  public List<String> getRegionNames() {
    List<String> names = new ArrayList<>();
    if (regions != null) {
      for (Region region : regions) {
        names.add(region.id());
      }
    }
    return names;
  }

  public void setRegions(Set<Region> regions) {
    this.regions = regions;
  }

  public void addRegion(Region region) {
    if (this.regions == null) {
      this.regions = new HashSet<>();
    }
    this.regions.add(region);
  }

  public void addRegions(Set<Region> regions) {
    if (this.regions == null) {
      this.regions = new HashSet<>();
    }
    this.regions.addAll(regions);
  }

  public Set<Target> getTargets() {
    return targets;
  }

  @JsonProperty("targets")
  public List<String> getTargetNames() {
    List<String> names = new ArrayList<>();
    if (targets != null) {
      for (Target target : targets) {
        names.add(target.id());
      }
    }
    return names;
  }

  public void setTargets(Set<Target> targets) {
    this.targets = targets;
  }

  public void addTarget(Target target) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.add(target);
  }

  public void addTargets(Set<Target> targets) {
    if (this.targets == null) {
      this.targets = new HashSet<>();
    }
    this.targets.addAll(targets);
  }
}
