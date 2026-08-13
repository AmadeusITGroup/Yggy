package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Provider;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Provider extends AbstractBase<V1Provider> {

  @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
  private Set<Region> regions;

  @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
  private Set<Target> targets;

  public Provider(V1Provider provider) {
    super(
        provider,
        Objects.requireNonNull(
                Objects.requireNonNull(provider, "provider cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Provider() {
    super();
  }

  public Set<Region> getRegions() {
    return regions;
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

  public Set<String> getTargetNames() {
    Set<String> names = new HashSet<>();
    if (targets != null) {
      for (Target target : targets) {
        names.add(target.id());
      }
    }
    return names;
  }

  public Set<String> getRegionNames() {
    Set<String> names = new HashSet<>();
    if (regions != null) {
      for (Region region : regions) {
        names.add(region.id());
      }
    }
    return names;
  }
}
