package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

@ApplicationScoped
public class PostgresEdgeRepository implements PanacheRepository<EdgeEntity> {

  // Optimal batch size for edges
  private static final int OPTIMAL_BATCH_SIZE = 100_000;

  // ---------- Version-range WHERE clause ----------
  private static final String EDGE_VERSION_RANGE =
      """
          e.addedInVersionRank <= :versionRank \
          AND (e.removedInVersionRank IS NULL OR e.removedInVersionRank > :versionRank)""";

  private static final String PARAM_VERSION_RANK = "versionRank";

  // Base query components
  private static final String EDGE_SELECT_CLAUSE =
      """
          SELECT new com.amadeus.aud.libraries.engine.graph.models.dto.Edge(\
          e.name, e.dependencyInstanceId, e.serviceId, e.governanceActive, \
          sv.componentInstanceId, dv.componentInstanceId)
          """;

  private static final String EDGE_FROM_CLAUSE = "FROM EdgeEntity e ";

  private static final String JOIN_WITH_NODES_CLAUSE =
      """
          LEFT JOIN e.sourceNode sv \
          LEFT JOIN e.destinationNode dv
          """;

  private static final String EDGE_BASE_WHERE_CLAUSE = "WHERE " + EDGE_VERSION_RANGE + " ";
  private static final String EDGE_ORDER_CLAUSE = "ORDER BY e.id";

  private static final String FIND_BY_GRAPH_RAW_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_INBOUND_LANDING_ZONES_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND dv.landingZoneName IN :landingZones "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_OUTBOUND_LANDING_ZONES_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND sv.landingZoneName IN :landingZones "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_ALL_LANDING_ZONES_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND (sv.landingZoneName IN :landingZones OR dv.landingZoneName IN :landingZones) "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_SCOPE_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND (sv.scopeName = :scope OR dv.scopeName = :scope) "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_INBOUND_PLATFORMS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND dv.platformName IN :platforms "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_OUTBOUND_PLATFORMS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND sv.platformName IN :platforms "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_ALL_PLATFORMS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND (sv.platformName IN :platforms OR dv.platformName IN :platforms) "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_INBOUND_TARGETS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND dv.targetName IN :targets "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_OUTBOUND_TARGETS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND sv.targetName IN :targets "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_ALL_TARGETS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND (sv.targetName IN :targets OR dv.targetName IN :targets) "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_NODE_IDS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND sv.componentInstanceId IN :sourceNodeIds "
          + "AND dv.componentInstanceId IN :destinationNodeIds "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_SOURCE_NODE_IDS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND sv.componentInstanceId IN :sourceNodeIds "
          + EDGE_ORDER_CLAUSE;

  private static final String FIND_BY_GRAPH_WITH_DESTINATION_NODE_IDS_QUERY =
      EDGE_SELECT_CLAUSE
          + EDGE_FROM_CLAUSE
          + JOIN_WITH_NODES_CLAUSE
          + EDGE_BASE_WHERE_CLAUSE
          + "AND dv.componentInstanceId IN :destinationNodeIds "
          + EDGE_ORDER_CLAUSE;

  private static final String COUNT_BY_VERSION_RANK_QUERY =
      "SELECT COUNT(e) FROM EdgeEntity e WHERE " + EDGE_VERSION_RANGE;

  private static final String HINT_FETCH_SIZE = "org.hibernate.fetchSize";

  private final Instance<EntityManager> entityManagerInstance;
  private final Instance<DataSource> dataSourceInstance;

  @Inject
  public PostgresEdgeRepository(
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

  private TypedQuery<Edge> createEdgeQuery(String queryString) {
    return entityManager()
        .createQuery(queryString, Edge.class)
        .setHint("org.hibernate.readOnly", true)
        .setHint("org.hibernate.cacheable", true);
  }

  public List<Edge> findByVersionRankWithLandingZones(
      int versionRank,
      int page,
      int size,
      @Nullable List<String> landingZones,
      @Nullable EdgeDirection direction) {

    if (direction == null) {
      direction = EdgeDirection.ANY;
    }

    if (landingZones != null && !landingZones.isEmpty()) {
      String queryString =
          switch (direction) {
            case INBOUND -> FIND_BY_GRAPH_WITH_INBOUND_LANDING_ZONES_QUERY;
            case OUTBOUND -> FIND_BY_GRAPH_WITH_OUTBOUND_LANDING_ZONES_QUERY;
            case ANY -> FIND_BY_GRAPH_WITH_ALL_LANDING_ZONES_QUERY;
          };

      return createEdgeQuery(queryString)
          .setParameter("landingZones", landingZones)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE))
          .getResultList();
    }

    return findByVersionRank(versionRank, page, size);
  }

  public List<Edge> findByVersionRankWithScope(int versionRank, int page, int size, String scope) {
    return createEdgeQuery(FIND_BY_GRAPH_WITH_SCOPE_QUERY)
        .setParameter("scope", scope)
        .setParameter(PARAM_VERSION_RANK, versionRank)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE))
        .getResultList();
  }

  public List<Edge> findByVersionRankWithPlatforms(
      int versionRank,
      int page,
      int size,
      @Nullable List<String> platforms,
      @Nullable EdgeDirection direction) {

    if (direction == null) {
      direction = EdgeDirection.ANY;
    }

    if (platforms != null && !platforms.isEmpty()) {
      String queryString =
          switch (direction) {
            case INBOUND -> FIND_BY_GRAPH_WITH_INBOUND_PLATFORMS_QUERY;
            case OUTBOUND -> FIND_BY_GRAPH_WITH_OUTBOUND_PLATFORMS_QUERY;
            case ANY -> FIND_BY_GRAPH_WITH_ALL_PLATFORMS_QUERY;
          };

      return createEdgeQuery(queryString)
          .setParameter("platforms", platforms)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE))
          .getResultList();
    }

    return findByVersionRank(versionRank, page, size);
  }

  public List<Edge> findByVersionRankWithTargets(
      int versionRank,
      int page,
      int size,
      @Nullable List<String> targets,
      @Nullable EdgeDirection direction) {

    if (direction == null) {
      direction = EdgeDirection.ANY;
    }

    if (targets != null && !targets.isEmpty()) {
      String queryString =
          switch (direction) {
            case INBOUND -> FIND_BY_GRAPH_WITH_INBOUND_TARGETS_QUERY;
            case OUTBOUND -> FIND_BY_GRAPH_WITH_OUTBOUND_TARGETS_QUERY;
            case ANY -> FIND_BY_GRAPH_WITH_ALL_TARGETS_QUERY;
          };

      return createEdgeQuery(queryString)
          .setParameter("targets", targets)
          .setParameter(PARAM_VERSION_RANK, versionRank)
          .setFirstResult(page * size)
          .setMaxResults(size)
          .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE))
          .getResultList();
    }

    return findByVersionRank(versionRank, page, size);
  }

  public List<Edge> findByVersionRank(int versionRank, int page, int size) {
    return createEdgeQuery(FIND_BY_GRAPH_RAW_QUERY)
        .setParameter(PARAM_VERSION_RANK, versionRank)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE))
        .getResultList();
  }

  public List<Edge> findByVersionRankWithNodeIds(
      int versionRank,
      @Nullable List<String> sourceNodeIds,
      @Nullable List<String> destinationNodeIds,
      int page,
      int size) {

    boolean hasSourceFilter = sourceNodeIds != null && !sourceNodeIds.isEmpty();
    boolean hasDestinationFilter = destinationNodeIds != null && !destinationNodeIds.isEmpty();

    if (!hasSourceFilter && !hasDestinationFilter) {
      Log.infof(
          "No source or destination node IDs provided for version rank %d. Returning all edges.",
          versionRank);
      return findByVersionRank(versionRank, page, size);
    }

    Set<String> srcSet = hasSourceFilter ? Set.copyOf(sourceNodeIds) : Set.of();
    Set<String> dstSet = hasDestinationFilter ? Set.copyOf(destinationNodeIds) : Set.of();

    String queryString;
    if (!hasSourceFilter) {
      queryString = FIND_BY_GRAPH_WITH_DESTINATION_NODE_IDS_QUERY;
    } else if (!hasDestinationFilter) {
      queryString = FIND_BY_GRAPH_WITH_SOURCE_NODE_IDS_QUERY;
    } else {
      queryString = FIND_BY_GRAPH_WITH_NODE_IDS_QUERY;
    }

    TypedQuery<Edge> query =
        createEdgeQuery(queryString)
            .setParameter(PARAM_VERSION_RANK, versionRank)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .setHint(HINT_FETCH_SIZE, Math.min(size, OPTIMAL_BATCH_SIZE));

    if (hasSourceFilter) {
      query.setParameter("sourceNodeIds", srcSet);
    }

    if (hasDestinationFilter) {
      query.setParameter("destinationNodeIds", dstSet);
    }

    return query.getResultList();
  }

  public long countByVersionRank(int versionRank) {
    return entityManager()
        .createQuery(COUNT_BY_VERSION_RANK_QUERY, Long.class)
        .setParameter(PARAM_VERSION_RANK, versionRank)
        .getSingleResult();
  }

  // ==================== VERSION-RANGE WRITE OPERATIONS ====================

  private static final String EDGE_KEY_SEPARATOR = "***";

  /**
   * Returns the set of active edge identity keys at the given version rank using native SQL. The
   * key is reconstructed from columns stored directly on edges (no join to nodes needed):
   * sourceComponentInstanceId***destinationComponentInstanceId***serviceid***dependencyinstanceid
   */
  public Set<String> findActiveEdgeKeys(int versionRank) {
    String sql =
        """
              SELECT sourcecomponentinstanceid, destinationcomponentinstanceid, serviceid, dependencyinstanceid \
              FROM edges \
              WHERE added_in_version_rank <= ? \
              AND (removed_in_version_rank IS NULL OR removed_in_version_rank > ?)""";

    Set<String> keys = new HashSet<>();
    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, versionRank);
      stmt.setInt(2, versionRank);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          keys.add(
              String.join(
                  EDGE_KEY_SEPARATOR,
                  rs.getString(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4)));
        }
      }
    } catch (SQLException e) {
      throw new GraphSaveException("Failed to load active edge keys for rank " + versionRank, e);
    }
    return keys;
  }

  /** Closes the version range for edges matching the given identity keys. */
  public void closeEdgeVersionRanges(Set<String> edgeKeys, int newRank) {
    if (edgeKeys.isEmpty()) {
      return;
    }
    // Matched against the full concatenated key, not the 4 individual columns: serviceid/
    // dependencyinstanceid values can themselves contain "***", so splitting the key back into
    // parts is ambiguous. Uses the || operator (not CONCAT(), which is STABLE not IMMUTABLE and
    // can't be indexed) so it matches the idx_edges_identity expression index for a sargable
    // lookup, no join to nodes needed.
    String sql =
        """
              UPDATE edges SET removed_in_version_rank = ? \
              WHERE sourcecomponentinstanceid || '***' || destinationcomponentinstanceid || '***' || serviceid || '***' || dependencyinstanceid = ? \
              AND removed_in_version_rank IS NULL""";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (String edgeKey : edgeKeys) {
        stmt.setInt(1, newRank);
        stmt.setString(2, edgeKey);
        stmt.addBatch();
      }
      stmt.executeBatch();
      entityManager().getEntityManagerFactory().getCache().evict(EdgeEntity.class);
    } catch (SQLException e) {
      throw new GraphSaveException("Failed to close edge version ranges at rank " + newRank, e);
    }
  }

  // ==================== HIGH-PERFORMANCE EDGE BATCH WRITE OPERATIONS ====================

  /** Ultra-high-performance batch insert for edges using PostgreSQL COPY with BINARY format */
  public void batchInsertEdgesCopyBinary(
      int versionRank,
      Collection<com.amadeus.aud.libraries.engine.graph.models.Edge> edges,
      Map<ComponentInstance, Long> nodeIdMap) {

    PostgresEdgeCopyWriter.batchInsertEdgesCopyBinary(
        dataSourceInstance.get(), versionRank, edges, nodeIdMap, this::createCopyManager);
  }

  org.postgresql.copy.CopyManager createCopyManager(
      org.postgresql.core.BaseConnection baseConnection) throws SQLException {
    return new org.postgresql.copy.CopyManager(baseConnection);
  }

  /**
   * Drops the FK constraints and non-PK indexes on {@code edges}. Intended to be called only around
   * the very first (full snapshot) load, on a table known to be empty, so the bulk COPY doesn't pay
   * per-row FK trigger checks and incremental B-tree maintenance.
   */
  public void dropIndexesAndConstraintsForBulkLoad() {
    String sql =
        """
              ALTER TABLE edges DROP CONSTRAINT IF EXISTS fk_edges_source_node; \
              ALTER TABLE edges DROP CONSTRAINT IF EXISTS fk_edges_destination_node; \
              DROP INDEX IF EXISTS idx_edges_version_rank; \
              DROP INDEX IF EXISTS idx_edges_identity; \
              DROP INDEX IF EXISTS idx_edges_sourcenode_id; \
              DROP INDEX IF EXISTS idx_edges_destinationnode_id""";

    try (Connection connection = dataSourceInstance.get().getConnection();
        java.sql.Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
      Log.info("Dropped edges FK constraints and indexes for bulk load");
    } catch (SQLException e) {
      throw new GraphSaveException("Failed to drop edges FK constraints/indexes for bulk load", e);
    }
  }

  /**
   * Recreates the FK constraints and indexes dropped by {@link
   * #dropIndexesAndConstraintsForBulkLoad()}, once the bulk COPY has completed. Rebuilding an index
   * from a full-table scan/sort and validating FKs once is far cheaper than maintaining them
   * row-by-row during the COPY.
   */
  public void recreateIndexesAndConstraintsAfterBulkLoad() {
    Instant startTime = Instant.now();
    // idx_edges_identity is an expression index (not a plain composite one): closeEdgeVersionRanges
    // matches on the full concatenated key since serviceid/dependencyinstanceid can contain "***"
    // themselves, so a plain per-column index would never be used by the planner. Uses the ||
    // operator, not CONCAT(), which is STABLE (not IMMUTABLE) and rejected in index expressions.
    String sql =
        """
              CREATE INDEX IF NOT EXISTS idx_edges_version_rank \
              ON edges (added_in_version_rank, removed_in_version_rank); \
              CREATE INDEX IF NOT EXISTS idx_edges_identity \
              ON edges (( \
                sourcecomponentinstanceid || '***' || destinationcomponentinstanceid || '***' || serviceid || '***' || dependencyinstanceid \
              )); \
              CREATE INDEX IF NOT EXISTS idx_edges_sourcenode_id \
              ON edges (sourcenode_id); \
              CREATE INDEX IF NOT EXISTS idx_edges_destinationnode_id \
              ON edges (destinationnode_id); \
              ALTER TABLE edges ADD CONSTRAINT fk_edges_source_node \
              FOREIGN KEY (sourcenode_id) REFERENCES nodes (id); \
              ALTER TABLE edges ADD CONSTRAINT fk_edges_destination_node \
              FOREIGN KEY (destinationnode_id) REFERENCES nodes (id)""";

    try (Connection connection = dataSourceInstance.get().getConnection();
        java.sql.Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
      Log.infof(
          "Recreated edges FK constraints and indexes after bulk load in %s",
          Duration.between(startTime, Instant.now()));
    } catch (SQLException e) {
      throw new GraphSaveException(
          "Failed to recreate edges FK constraints/indexes after bulk load", e);
    }
  }

  /** Optimized delete for edges added at a specific version rank */
  public void clearEdgesForVersionRank(int versionRank) {
    Instant startTime = Instant.now();
    String deleteSQL = "DELETE FROM edges WHERE added_in_version_rank = ?";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {

      stmt.setInt(1, versionRank);
      int deletedCount = stmt.executeUpdate();

      Log.debugf(
          "Cleared %d edges for version rank %d in %s",
          deletedCount, versionRank, Duration.between(startTime, Instant.now()));

      entityManager().getEntityManagerFactory().getCache().evict(EdgeEntity.class);

    } catch (SQLException e) {
      Log.errorf(e, "Failed to clear edges for version rank %d", versionRank);
      throw new GraphSaveException("Failed to clear edges for version rank " + versionRank, e);
    }
  }

  /** Reopen edges that were closed (removed) at a specific version rank. */
  public void reopenEdgesClosedAtRank(int versionRank) {
    String sql =
        "UPDATE edges SET removed_in_version_rank = NULL WHERE removed_in_version_rank = ?";

    try (Connection connection = dataSourceInstance.get().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

      stmt.setInt(1, versionRank);
      int updated = stmt.executeUpdate();

      Log.debugf("Reopened %d edges closed at version rank %d", updated, versionRank);
      entityManager().getEntityManagerFactory().getCache().evict(EdgeEntity.class);

    } catch (SQLException e) {
      throw new GraphSaveException("Failed to reopen edges closed at rank " + versionRank, e);
    }
  }
}
