package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import static com.amadeus.aud.libraries.engine.graph.utils.StringUtils.escapeCsvValue;

import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import io.quarkus.logging.Log;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Handles PostgreSQL CSV COPY operations for bulk node inserts. Extracted from {@link
 * PostgresNodeRepository} to reduce class coupling (S1200).
 */
final class PostgresNodeCopyWriter {

  private static final int COPY_ESTIMATED_CHARS_PER_NODE = 50;
  private static final int COPY_BUFFER_SIZE = 65_536;

  private PostgresNodeCopyWriter() {
    // utility class
  }

  /**
   * Ultra-high-performance batch insert for nodes using PostgreSQL COPY.
   *
   * @param dataSource the JDBC data source
   * @param versionRank the version rank to tag inserted nodes with
   * @param nodes the nodes to insert
   * @param copyManagerFactory factory to create a CopyManager from a BaseConnection
   * @return mapping from ComponentInstance to generated database ID
   */
  static Map<ComponentInstance, Long> batchInsertNodesCopy(
      DataSource dataSource,
      int versionRank,
      Collection<ComponentInstance> nodes,
      CopyManagerFactory copyManagerFactory) {
    Map<ComponentInstance, Long> nodeIdMap = new HashMap<>();

    try (Connection connection = dataSource.getConnection()) {

      Connection unwrappedConnection = connection;
      if (connection.isWrapperFor(org.postgresql.core.BaseConnection.class)) {
        unwrappedConnection = connection.unwrap(org.postgresql.core.BaseConnection.class);
      } else {
        throw new GraphSaveException("Connection is not a PostgreSQL connection, cannot use COPY");
      }

      String copySQL =
          """
                    COPY nodes (componentinstanceid, landingzonename, platformname,\
                     targetname, scopeName, added_in_version_rank) FROM STDIN WITH (FORMAT CSV)""";

      org.postgresql.copy.CopyManager copyManager =
          copyManagerFactory.create((org.postgresql.core.BaseConnection) unwrappedConnection);

      int estimatedCapacity = nodes.size() * COPY_ESTIMATED_CHARS_PER_NODE;
      StringBuilder csvData = new StringBuilder(estimatedCapacity);

      String versionRankStr = String.valueOf(versionRank);

      List<ComponentInstance> orderedNodes = new ArrayList<>(nodes);

      for (ComponentInstance node : orderedNodes) {
        LandingZone landingZone = node.getLandingZone();
        Target target = landingZone != null ? landingZone.getTarget() : null;
        ComponentInstance deployComponent = node.getDeployComponentInstance();
        csvData
            .append(escapeCsvValue(node.id()))
            .append(',')
            .append(escapeCsvValue(landingZone != null ? landingZone.getName() : null))
            .append(',')
            .append(escapeCsvValue(deployComponent != null ? deployComponent.getName() : null))
            .append(',')
            .append(escapeCsvValue(target != null ? target.getName() : null))
            .append(',')
            .append(escapeCsvValue(node.getScopeName()))
            .append(',')
            .append(versionRankStr)
            .append('\n');
      }

      Log.debugf("Processing %d nodes using PostgreSQL COPY", nodes.size());

      byte[] csvBytes = csvData.toString().getBytes(StandardCharsets.UTF_8);
      executeCsvCopyOperation(copyManager, copySQL, csvBytes, orderedNodes.size(), versionRank);

      // Retrieve the generated IDs — query all active nodes at this version rank
      String selectSQL =
          "SELECT id, componentinstanceid FROM nodes WHERE added_in_version_rank = ?";
      Map<String, Long> idByComponentInstanceId = new HashMap<>();
      try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
        selectStmt.setInt(1, versionRank);

        try (ResultSet rs = selectStmt.executeQuery()) {
          while (rs.next()) {
            idByComponentInstanceId.put(rs.getString("componentinstanceid"), rs.getLong("id"));
          }
        }
      }

      for (ComponentInstance node : orderedNodes) {
        Long generatedId = idByComponentInstanceId.get(node.id());
        if (generatedId == null) {
          throw new GraphSaveException(
              "No generated ID found for node "
                  + node.id()
                  + " at version rank "
                  + versionRank
                  + ". Expected "
                  + orderedNodes.size()
                  + " nodes but only "
                  + idByComponentInstanceId.size()
                  + " were inserted.");
        }
        nodeIdMap.put(node, generatedId);
      }

    } catch (GraphSaveException e) {
      throw e;
    } catch (Exception e) {
      Log.errorf(e, "Failed to batch insert nodes using COPY for version rank %d", versionRank);
      throw new GraphSaveException(
          "Failed to batch insert nodes for version rank " + versionRank, e);
    }

    Instant startTime = Instant.now();
    Log.infof(
        "PostgreSQL COPY insert completed: %d nodes in %s",
        nodes.size(), Duration.between(startTime, Instant.now()));

    return nodeIdMap;
  }

  /**
   * Executes a PostgreSQL COPY CSV operation, streaming node data in chunks and verifying the
   * inserted row count.
   */
  private static void executeCsvCopyOperation(
      org.postgresql.copy.CopyManager copyManager,
      String copySQL,
      byte[] csvBytes,
      int expectedRowCount,
      int versionRank)
      throws SQLException {
    org.postgresql.copy.CopyIn copyIn = copyManager.copyIn(copySQL);
    try {
      int chunkSize = COPY_BUFFER_SIZE;
      for (int offset = 0; offset < csvBytes.length; offset += chunkSize) {
        int len = Math.min(chunkSize, csvBytes.length - offset);
        copyIn.writeToCopy(csvBytes, offset, len);
      }
      long copiedRows = copyIn.endCopy();
      Log.debugf("COPY inserted %d node rows", copiedRows);

      if (copiedRows != expectedRowCount) {
        throw new GraphSaveException(
            String.format(
                """
                                COPY node row count mismatch for version rank %d: expected %d, got %d. \
                                Possible data corruption or driver error.""",
                versionRank, expectedRowCount, copiedRows));
      }
    } catch (SQLException | GraphSaveException e) {
      PostgresEdgeCopyWriter.cancelCopyOperation(copyIn, "nodes", String.valueOf(versionRank), e);
      throw e;
    }
  }

  /** Functional interface for CopyManager creation (allows test mocking). */
  @FunctionalInterface
  interface CopyManagerFactory {
    org.postgresql.copy.CopyManager create(org.postgresql.core.BaseConnection connection)
        throws SQLException;
  }
}
