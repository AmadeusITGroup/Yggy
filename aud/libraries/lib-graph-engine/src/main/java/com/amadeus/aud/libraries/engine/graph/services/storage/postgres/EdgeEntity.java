package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import jakarta.annotation.Nullable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
    name = "edges",
    indexes = {
      @Index(
          name = "idx_edges_version_rank",
          columnList = "added_in_version_rank, removed_in_version_rank")
    })
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EdgeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 1024)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, targetEntity = NodeEntity.class)
  @JoinColumn(name = "sourcenode_id", foreignKey = @ForeignKey(name = "fk_edges_source_node"))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NodeEntity sourceNode;

  @ManyToOne(fetch = FetchType.LAZY, targetEntity = NodeEntity.class)
  @JoinColumn(
      name = "destinationnode_id",
      foreignKey = @ForeignKey(name = "fk_edges_destination_node"))
  @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
  private NodeEntity destinationNode;

  // Denormalized copies of sourceNode/destinationNode's componentInstanceId, so the
  // version-range diff/close queries can match on edges alone, without joining nodes.
  private String sourceComponentInstanceId;
  private String destinationComponentInstanceId;

  private String dependencyInstanceId;
  private String serviceId;

  private Boolean governanceActive;

  @Column(name = "added_in_version_rank", nullable = false)
  private int addedInVersionRank;

  @Nullable
  @Column(name = "removed_in_version_rank")
  private Integer removedInVersionRank;

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

  public NodeEntity getSourceNode() {
    return sourceNode;
  }

  public void setSourceNode(NodeEntity sourceVertex) {
    this.sourceNode = sourceVertex;
  }

  public NodeEntity getDestinationNode() {
    return destinationNode;
  }

  public void setDestinationNode(NodeEntity destinationVertex) {
    this.destinationNode = destinationVertex;
  }

  public String getSourceComponentInstanceId() {
    return sourceComponentInstanceId;
  }

  public void setSourceComponentInstanceId(String sourceComponentInstanceId) {
    this.sourceComponentInstanceId = sourceComponentInstanceId;
  }

  public String getDestinationComponentInstanceId() {
    return destinationComponentInstanceId;
  }

  public void setDestinationComponentInstanceId(String destinationComponentInstanceId) {
    this.destinationComponentInstanceId = destinationComponentInstanceId;
  }

  public String getDependencyInstanceId() {
    return dependencyInstanceId;
  }

  public void setDependencyInstanceId(String dependencyInstanceId) {
    this.dependencyInstanceId = dependencyInstanceId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public Boolean getGovernanceActive() {
    return governanceActive;
  }

  public void setGovernanceActive(Boolean governanceActive) {
    this.governanceActive = governanceActive;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String edgeId) {
    this.name = edgeId;
  }
}
