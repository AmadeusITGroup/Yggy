package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import jakarta.annotation.Nullable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
    name = "nodes",
    indexes = {
      @Index(columnList = "added_in_version_rank, removed_in_version_rank"),
      @Index(columnList = "componentInstanceId"),
    })
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NodeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String componentInstanceId;
  private String landingZoneName;
  private String platformName;
  private String targetName;
  private String scopeName;

  @Column(name = "added_in_version_rank", nullable = false)
  private int addedInVersionRank;

  @Nullable
  @Column(name = "removed_in_version_rank")
  private Integer removedInVersionRank;

  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(String componentInstanceId) {
    this.componentInstanceId = componentInstanceId;
  }

  public int getAddedInVersionRank() {
    return addedInVersionRank;
  }

  public void setAddedInVersionRank(int addedInVersionRank) {
    this.addedInVersionRank = addedInVersionRank;
  }

  public @Nullable Integer getRemovedInVersionRank() {
    return removedInVersionRank;
  }

  public void setRemovedInVersionRank(@Nullable Integer removedInVersionRank) {
    this.removedInVersionRank = removedInVersionRank;
  }

  public Long getId() {
    return id;
  }

  public String getLandingZoneName() {
    return landingZoneName;
  }

  public void setLandingZoneName(String landingZoneName) {
    this.landingZoneName = landingZoneName;
  }

  public String getPlatformName() {
    return platformName;
  }

  public void setPlatformName(String platformName) {
    this.platformName = platformName;
  }

  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public String getScopeName() {
    return scopeName;
  }

  public void setScopeName(String scopeName) {
    this.scopeName = scopeName;
  }
}
