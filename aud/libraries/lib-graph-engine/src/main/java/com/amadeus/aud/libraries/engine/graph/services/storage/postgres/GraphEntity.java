package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphVersion;
import com.amadeus.aud.libraries.engine.graph.services.storage.inmemory.GraphStatisticsComputer;
import jakarta.annotation.Nullable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "graphs",
    indexes = {
      @Index(columnList = "version"),
      @Index(columnList = "version_rank"),
    })
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class GraphEntity implements GraphVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(unique = true, nullable = false)
  private String version;

  @Column(name = "version_rank", unique = true, nullable = false)
  private int versionRank;

  // Pre-computed statistics (stored at save time, graph is immutable)
  @Column(name = "statistics_global_links_count")
  private long statisticsGlobalLinksCount;

  @Column(name = "statistics_global_nodes_count")
  private long statisticsGlobalNodesCount;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "statistics_scope_nb_link")
  private Map<String, Long> statisticsScopeNbLink;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "statistics_scope_nb_node")
  private Map<String, Long> statisticsScopeNbNode;

  public GraphEntity() {}

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  @Override
  public int getVersionRank() {
    return versionRank;
  }

  public void setVersionRank(int versionRank) {
    this.versionRank = versionRank;
  }

  public long getStatisticsGlobalLinksCount() {
    return statisticsGlobalLinksCount;
  }

  public void setStatisticsGlobalLinksCount(long statisticsGlobalLinksCount) {
    this.statisticsGlobalLinksCount = statisticsGlobalLinksCount;
  }

  public long getStatisticsGlobalNodesCount() {
    return statisticsGlobalNodesCount;
  }

  public void setStatisticsGlobalNodesCount(long statisticsGlobalNodesCount) {
    this.statisticsGlobalNodesCount = statisticsGlobalNodesCount;
  }

  public @Nullable Map<String, Long> getStatisticsScopeNbLink() {
    return statisticsScopeNbLink != null ? Map.copyOf(statisticsScopeNbLink) : null;
  }

  public void setStatisticsScopeNbLink(Map<String, Long> statisticsScopeNbLink) {
    this.statisticsScopeNbLink =
        statisticsScopeNbLink != null ? Map.copyOf(statisticsScopeNbLink) : null;
  }

  public @Nullable Map<String, Long> getStatisticsScopeNbNode() {
    return statisticsScopeNbNode != null ? Map.copyOf(statisticsScopeNbNode) : null;
  }

  public void setStatisticsScopeNbNode(Map<String, Long> statisticsScopeNbNode) {
    this.statisticsScopeNbNode =
        statisticsScopeNbNode != null ? Map.copyOf(statisticsScopeNbNode) : null;
  }

  /** Build a {@link GraphStatistics} DTO from the pre-computed fields stored on this entity. */
  @Override
  public GraphStatistics toStatistics() {
    return new GraphStatistics(
        statisticsScopeNbLink,
        statisticsScopeNbNode,
        statisticsGlobalNodesCount,
        statisticsGlobalLinksCount);
  }

  /** Applies pre-computed statistics from a {@link GraphStatistics} instance to this entity. */
  public void applyStatistics(GraphStatistics statistics) {
    this.statisticsGlobalLinksCount = statistics.globalLinksCount();
    this.statisticsGlobalNodesCount = statistics.globalNodesCount();
    this.statisticsScopeNbLink = statistics.scopeNbLink();
    this.statisticsScopeNbNode = statistics.scopeNbNode();
  }

  /**
   * Computes statistics from a {@link Graph} and applies them to this entity. Convenience method
   * that avoids callers needing to depend on {@link GraphStatistics} directly.
   */
  public void computeAndApplyStatistics(Graph graph, Version version) {
    applyStatistics(GraphStatisticsComputer.compute(graph, version));
  }
}
