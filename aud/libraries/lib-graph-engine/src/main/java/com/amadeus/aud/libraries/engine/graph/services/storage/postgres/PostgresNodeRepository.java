package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

@ApplicationScoped
public class PostgresNodeRepository implements PanacheRepository<NodeEntity> {
  private static final int NODE_BATCH_SIZE = 50_000;
  private static final String HINT_FETCH_SIZE = "org.hibernate.fetchSize";

  // ---------- Version-range WHERE clause used by all read queries ----------
  private static final String VERSION_RANGE_CLAUSE =
      """
            v.addedInVersionRank <= :versionRank \
            AND (v.removedInVersionRank IS NULL OR v.removedInVersionRank > :versionRank)""";

  private static final String PARAM_VERSION_RANK = "versionRank";

  // Query strings using version-range filter instead of graph_id
  private static final String FIND_BY_GRAPH_QUERY =
      """
            SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Node(\
            v.componentInstanceId, v.landingZoneName, v.platformName, v.targetName) \
            FROM NodeEntity v \
            WHERE\
            """
          + " "
          + VERSION_RANGE_CLAUSE
          + " ORDER BY v.id";

  private static final String FIND_BY_GRAPH_WITH_LANDING_ZONES_QUERY =
      """
            SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Node(\
            v.componentInstanceId, v.landingZoneName, v.platformName, v.targetName) \
            FROM NodeEntity v \
            WHERE\
            """
          + " "
          + VERSION_RANGE_CLAUSE
          + " AND v.landingZoneName IN :landingZones ORDER BY v.id";

  private static final String FIND_BY_GRAPH_WITH_PLATFORMS_QUERY =
      """
                SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Node(\
                v.componentInstanceId, v.landingZoneName, v.platformName, v.targetName) \
                FROM NodeEntity v \
                WHERE\
                """
          + " "
          + VERSION_RANGE_CLAUSE
          + " AND v.platformName IN :platforms ORDER BY v.id";

  private static final String FIND_BY_GRAPH_WITH_LANDING_ZONES_AND_PLATFORMS_QUERY =
      """
                SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Node(\
                v.componentInstanceId, v.landingZoneName, v.platformName, v.targetName) \
                FROM NodeEntity v \
                WHERE\
                """
          + " "
          + VERSION_RANGE_CLAUSE
          + " AND v.landingZoneName IN :landingZones AND v.platformName IN :platforms ORDER BY v.id";

  private static final String FIND_BY_GRAPH_WITH_TARGETS_QUERY =
      """
            SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Node(\
            v.componentInstanceId, v.landingZoneName, v.platformName, v.targetName) \
            FROM NodeEntity v \
            WHERE\
            """
          + " "
          + VERSION_RANGE_CLAUSE
          + " AND v.targetName IN :targets ORDER BY v.id";

  private static final String COUNT_BY_VERSION_RANK_QUERY =
      "SELECT COUNT(v) FROM NodeEntity v WHERE " + VERSION_RANGE_CLAUSE;

  private final Instance<EntityManager> entityManagerInstance;
  private final Instance<DataSource> dataSourceInstance;

  @Inject
  public PostgresNodeRepository(
      Instance<EntityManager> entityManagerInstance, Instance<DataSource> dataSourceInstance) {
    this.entityManagerInstance = entityManagerInstance;
    this.dataSourceInstance = dataSourceInstance;
  }

  private EntityManager entityManager() {
    if (entityManagerInstance.isUnsatisfied()) {
      throw new IllegalStateException(
          "EntityManager is not available. Ensure Hibernate ORM is enabled when using Postgres graph storage.");
    }
    return entityManagerInstance.get();
  }

  private TypedQuery<Node> createQuery(String queryString) {
    return entityManager()
        .createQuery(queryString, Node.class)
        .setHint("org.hibernate.readOnly", true)
        .setHint("org.hibernate.cacheable", true);
  }

  public long countByVersionRank(int versionRank) {
    return entityManager()
        .createQuery(COUNT_BY_VERSION_RANK_QUERY, Long.class)
        .setParameter(PARAM_VERSION_RANK, versionRank)
        .getSingleResult();
  }

  public List<Node> findByVersionRank(int versionRank, int page, int size) {
    return createQuery(FIND_BY_GRAPH_QUERY)
        .setParameter(PARAM_VERSION_RANK, versionRank)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .setHint(HINT_FETCH_SIZE, Math.min(size, NODE_BATCH_SIZE))
        .getResultList();
  }

  public List<Node> findByVersionRankWithLandingZones(
      int versionRank, int page, int size, @Nullable List<String> landingZones) {
    if (landingZones != null && !landingZones.isEmpty()) {
      return createQuery(FIND_BY_GRAPH_WITH_LANDING_ZONES_QUERY)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setParameter("landingZones", landingZones)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, NODE_BATCH_SIZE))
          .getResultList();
    } else {
      return findByVersionRank(versionRank, page, size);
    }
  }

  public List<Node> findByVersionRankWithPlatforms(
      int versionRank, int page, int size, @Nullable List<String> platforms) {
    if (platforms != null && !platforms.isEmpty()) {
      return createQuery(FIND_BY_GRAPH_WITH_PLATFORMS_QUERY)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setParameter("platforms", platforms)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, NODE_BATCH_SIZE))
          .getResultList();
    } else {
      return findByVersionRank(versionRank, page, size);
    }
  }

  public List<Node> findByVersionRankWithTargets(
      int versionRank, int page, int size, @Nullable List<String> targets) {
    if (targets != null && !targets.isEmpty()) {
      return createQuery(FIND_BY_GRAPH_WITH_TARGETS_QUERY)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setParameter("targets", targets)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, NODE_BATCH_SIZE))
          .getResultList();
    } else {
      return findByVersionRank(versionRank, page, size);
    }
  }

  public List<Node> findByVersionRankWithLandingZonesAndPlatforms(
      int versionRank,
      @Nullable List<String> landingZones,
      @Nullable List<String> platforms,
      int page,
      int size) {
    boolean hasLandingZones = landingZones != null && !landingZones.isEmpty();
    boolean hasPlatforms = platforms != null && !platforms.isEmpty();

    if (hasLandingZones && hasPlatforms) {
      return createQuery(FIND_BY_GRAPH_WITH_LANDING_ZONES_AND_PLATFORMS_QUERY)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setParameter("landingZones", landingZones)
          .setParameter("platforms", platforms)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, NODE_BATCH_SIZE))
          .getResultList();
    }
    if (hasLandingZones) {
      return findByVersionRankWithLandingZones(versionRank, page, size, landingZones);
    }
    return hasPlatforms
        ? findByVersionRankWithPlatforms(versionRank, page, size, platforms)
        : findByVersionRank(versionRank, page, size);
  }

  // ==================== VERSION-RANGE WRITE OPERATIONS ====================

  /** Returns the set of active node component instance IDs at the given version rank. */
  public Set<String> findActiveNodeIds(int versionRank) {
    String sql =
        """
                SELECT componentinstanceid FROM nodes \
                WHERE added_in_version_rank <= ? \
                AND (removed_in_version_rank IS NULL OR removed_in_version_rank > ?)""";

    Set<String> ids = new HashSet<>();
    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, versionRank);
      stmt.setInt(2, versionRank);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getString(1));
        }
      }
    } catch (SQLException e) {
      throw new GraphSaveException(
          "Error fetching active node IDs for version rank " + versionRank, e);
    }
    return ids;
  }

  /**
   * Returns a map of (componentInstanceId → database ID) for all active nodes at the given rank.
   */
  public Map<String, Long> loadActiveNodeIdMap(int versionRank) {
    String sql =
        """
                SELECT id, componentinstanceid FROM nodes \
                WHERE added_in_version_rank <= ? \
                AND (removed_in_version_rank IS NULL OR removed_in_version_rank > ?)""";

    Map<String, Long> map = new HashMap<>();
    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, versionRank);
      stmt.setInt(2, versionRank);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          map.put(rs.getString("componentinstanceid"), rs.getLong("id"));
        }
      }
    } catch (SQLException e) {
      throw new GraphSaveException(
          "Error loading active node ID map for version rank " + versionRank, e);
    }
    return map;
  }

  /** Closes the version range (sets removed_in_version_rank) for the given node IDs. */
  public void closeNodeVersionRanges(Set<String> nodeIds, int newRank) {
    if (nodeIds.isEmpty()) {
      return;
    }
    String sql =
        """
                UPDATE nodes SET removed_in_version_rank = ? \
                WHERE componentinstanceid = ? AND removed_in_version_rank IS NULL""";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (String nodeId : nodeIds) {
        stmt.setInt(1, newRank);
        stmt.setString(2, nodeId);
        stmt.addBatch();
      }
      stmt.executeBatch();
      entityManager().getEntityManagerFactory().getCache().evict(NodeEntity.class);
    } catch (SQLException e) {
      throw new GraphSaveException(
          "Error closing node version ranges for version rank " + newRank, e);
    }
  }

  // ==================== HIGH-PERFORMANCE VERTEX BATCH WRITE OPERATIONS ====================

  /** Ultra-high-performance batch insert for nodes using PostgreSQL COPY */
  public Map<ComponentInstance, Long> batchInsertNodesCopy(
      int versionRank, Collection<ComponentInstance> nodes) {
    return PostgresNodeCopyWriter.batchInsertNodesCopy(
        dataSourceInstance.get(), versionRank, nodes, this::createCopyManager);
  }

  // ==================== HELPER METHODS ====================

  org.postgresql.copy.CopyManager createCopyManager(
      org.postgresql.core.BaseConnection baseConnection) throws SQLException {
    return new org.postgresql.copy.CopyManager(baseConnection);
  }

  /** Optimized delete for nodes added at a specific version rank — uses native SQL */
  public void clearNodesForVersionRank(int versionRank) {
    Instant startTime = Instant.now();
    // Delete nodes that were created at this rank
    String deleteSQL = "DELETE FROM nodes WHERE added_in_version_rank = ?";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {

      stmt.setInt(1, versionRank);
      int deletedCount = stmt.executeUpdate();

      Log.debugf(
          "Cleared %d nodes for version rank %d in %s",
          deletedCount, versionRank, Duration.between(startTime, Instant.now()));

      entityManager().getEntityManagerFactory().getCache().evict(NodeEntity.class);

    } catch (SQLException e) {
      Log.errorf(e, "Failed to clear nodes for version rank %d", versionRank);
      throw new GraphSaveException("Failed to clear nodes for version rank " + versionRank, e);
    }
  }

  /** Reopen nodes that were closed (removed) at a specific version rank. */
  public void reopenNodesClosedAtRank(int versionRank) {
    String sql =
        "UPDATE nodes SET removed_in_version_rank = NULL WHERE removed_in_version_rank = ?";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      stmt.setInt(1, versionRank);
      int updated = stmt.executeUpdate();

      Log.debugf("Reopened %d nodes closed at version rank %d", updated, versionRank);
      entityManager().getEntityManagerFactory().getCache().evict(NodeEntity.class);

    } catch (SQLException e) {
      throw new GraphSaveException("Failed to reopen nodes closed at rank " + versionRank, e);
    }
  }
}
