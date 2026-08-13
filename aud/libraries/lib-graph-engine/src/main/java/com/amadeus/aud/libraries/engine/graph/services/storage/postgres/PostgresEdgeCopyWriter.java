package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import io.quarkus.logging.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import javax.sql.DataSource;

/**
 * Handles PostgreSQL binary COPY operations for bulk edge inserts. Extracted from {@link
 * PostgresEdgeRepository} to reduce class coupling (S1200).
 */
final class PostgresEdgeCopyWriter {

  private static final int EDGE_COLUMN_COUNT = 9;
  private static final int BIGINT_BYTE_LENGTH = 8;
  private static final int COPY_BUFFER_SIZE = 65_536;

  private PostgresEdgeCopyWriter() {
    // utility class
  }

  /**
   * Ultra-high-performance batch insert for edges using PostgreSQL COPY with BINARY format.
   *
   * @param dataSource the JDBC data source
   * @param versionRank the version rank to tag inserted edges with
   * @param edges the edges to insert
   * @param nodeIdMap mapping from component instances to database node IDs
   * @param copyManagerFactory factory to create a CopyManager from a BaseConnection
   */
  static void batchInsertEdgesCopyBinary(
      DataSource dataSource,
      int versionRank,
      Collection<Edge> edges,
      Map<ComponentInstance, Long> nodeIdMap,
      CopyManagerFactory copyManagerFactory) {

    try (Connection connection = dataSource.getConnection()) {

      Connection unwrappedConnection = connection;
      if (connection.isWrapperFor(org.postgresql.core.BaseConnection.class)) {
        unwrappedConnection = connection.unwrap(org.postgresql.core.BaseConnection.class);
      } else {
        throw new GraphSaveException(
            "Connection is not a PostgreSQL connection, cannot use COPY BINARY");
      }

      ByteArrayOutputStream binaryData = new ByteArrayOutputStream();
      DataOutputStream dataStream = new DataOutputStream(binaryData);
      int validEdgeCount = writeBinaryEdgeData(dataStream, edges, nodeIdMap, versionRank);

      Log.debugf(
          "Processing %d valid edges out of %d total edges using COPY BINARY format",
          validEdgeCount, edges.size());

      byte[] data = binaryData.toByteArray();
      String copySQL =
          """
                    COPY edges (name, dependencyinstanceid, serviceid, governanceactive, \
                    sourcenode_id, destinationnode_id, added_in_version_rank, \
                    sourcecomponentinstanceid, destinationcomponentinstanceid) FROM STDIN WITH (FORMAT BINARY)""";
      org.postgresql.copy.CopyManager copyManager =
          copyManagerFactory.create((org.postgresql.core.BaseConnection) unwrappedConnection);
      executeBinaryCopyOperation(
          copyManager, copySQL, data, validEdgeCount, String.valueOf(versionRank));

    } catch (GraphSaveException e) {
      throw e;
    } catch (Exception e) {
      Log.errorf(
          e, "Failed to batch insert edges using COPY BINARY for version rank %d", versionRank);
      throw new GraphSaveException(
          "Failed to batch insert edges for version rank " + versionRank, e);
    }

    Instant startTime = Instant.now();
    Log.infof(
        "PostgreSQL COPY BINARY insert completed: %d edges in %s",
        edges.size(), Duration.between(startTime, Instant.now()));
  }

  /**
   * Writes edge data in PostgreSQL binary COPY format to the given output stream. Returns the count
   * of valid edges written.
   */
  static int writeBinaryEdgeData(
      DataOutputStream dataStream,
      Collection<Edge> edges,
      Map<ComponentInstance, Long> nodeIdMap,
      int versionRank) {
    int validEdgeCount = 0;
    try {
      writeBinaryHeader(dataStream);

      for (Edge edge : edges) {
        Long sourceNodeId = nodeIdMap.get(edge.getSource());
        Long targetNodeId = nodeIdMap.get(edge.getDestination());

        if (sourceNodeId != null && targetNodeId != null) {
          writeEdgeRow(dataStream, edge, sourceNodeId, targetNodeId, versionRank);
          validEdgeCount++;
        }
      }

      dataStream.writeShort(-1);
      dataStream.flush();

    } catch (IOException e) {
      Log.errorf(e, "Failed to build binary data for COPY for version rank %d", versionRank);
      throw new GraphSaveException(
          "Failed to build binary data for COPY for version rank " + versionRank, e);
    }
    return validEdgeCount;
  }

  /** Writes the PostgreSQL binary COPY format header. */
  private static void writeBinaryHeader(DataOutputStream dataStream) throws IOException {
    dataStream.write(new byte[] {'P', 'G', 'C', 'O', 'P', 'Y', '\n', (byte) 0xff, '\r', '\n', 0});
    dataStream.writeInt(0);
    dataStream.writeInt(0);
  }

  /** Writes a single edge row in PostgreSQL binary COPY format. */
  private static void writeEdgeRow(
      DataOutputStream dataStream, Edge edge, long sourceNodeId, long targetNodeId, int versionRank)
      throws IOException {
    dataStream.writeShort(EDGE_COLUMN_COUNT);

    writeNullableTextField(dataStream, edge.getName());
    writeNullableTextField(dataStream, edge.getDependencyInstance().id());
    writeNullableTextField(dataStream, edge.getService().id());

    dataStream.writeInt(1);
    dataStream.writeByte(edge.isGovernanceActive() ? 1 : 0);

    dataStream.writeInt(BIGINT_BYTE_LENGTH);
    dataStream.writeLong(sourceNodeId);

    dataStream.writeInt(BIGINT_BYTE_LENGTH);
    dataStream.writeLong(targetNodeId);

    // added_in_version_rank is INT (4 bytes)
    dataStream.writeInt(4);
    dataStream.writeInt(versionRank);

    writeNullableTextField(dataStream, edge.getSource().id());
    writeNullableTextField(dataStream, edge.getDestination().id());
  }

  /** Writes a nullable text field in PostgreSQL binary COPY format. */
  private static void writeNullableTextField(DataOutputStream dataStream, String value)
      throws IOException {
    if (value != null) {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      dataStream.writeInt(bytes.length);
      dataStream.write(bytes);
    } else {
      dataStream.writeInt(-1);
    }
  }

  /**
   * Executes a PostgreSQL COPY BINARY operation, streaming binary edge data in chunks and verifying
   * the inserted row count. Cancels the COPY operation on failure.
   */
  private static void executeBinaryCopyOperation(
      org.postgresql.copy.CopyManager copyManager,
      String copySQL,
      byte[] data,
      int expectedRowCount,
      String versionLabel)
      throws SQLException {
    org.postgresql.copy.CopyIn copyIn = copyManager.copyIn(copySQL);
    try {
      int chunkSize = COPY_BUFFER_SIZE;
      for (int offset = 0; offset < data.length; offset += chunkSize) {
        int len = Math.min(chunkSize, data.length - offset);
        copyIn.writeToCopy(data, offset, len);
      }
      long copiedRows = copyIn.endCopy();
      Log.debugf("COPY BINARY inserted %d rows", copiedRows);

      if (copiedRows != expectedRowCount) {
        throw new GraphSaveException(
            String.format(
                """
                                COPY BINARY edge row count mismatch for version %s: expected %d, got %d. \
                                Possible data corruption or driver error.""",
                versionLabel, expectedRowCount, copiedRows));
      }
    } catch (SQLException | GraphSaveException e) {
      cancelCopyOperation(copyIn, "edges", versionLabel, e);
      throw e;
    }
  }

  /**
   * Safely cancels an active COPY operation and logs the failure. This ensures the PostgreSQL COPY
   * stream is properly terminated even if the driver fails to propagate the error correctly.
   */
  static void cancelCopyOperation(
      org.postgresql.copy.CopyIn copyIn, String dataType, String graphVersion, Exception cause) {
    if (copyIn.isActive()) {
      try {
        copyIn.cancelCopy();
        Log.warnf(
            "COPY operation for %s cancelled after failure for graph %s: %s",
            dataType, graphVersion, cause.getMessage());
      } catch (SQLException cancelEx) {
        Log.errorf(
            cancelEx,
            """
                        Failed to cancel COPY operation for %s on graph %s. \
                        Connection may be in an inconsistent state.""",
            dataType,
            graphVersion);
        cause.addSuppressed(cancelEx);
      }
    }
  }

  /** Functional interface for CopyManager creation (allows test mocking). */
  @FunctionalInterface
  interface CopyManagerFactory {
    org.postgresql.copy.CopyManager create(org.postgresql.core.BaseConnection connection)
        throws SQLException;
  }
}
