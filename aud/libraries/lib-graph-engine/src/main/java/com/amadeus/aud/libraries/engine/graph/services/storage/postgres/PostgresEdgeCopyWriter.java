package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import io.quarkus.logging.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
 *
 * <p>Streams row data directly to {@link org.postgresql.copy.CopyIn} through a bounded buffer
 * ({@link CopyInOutputStream}) instead of building the whole payload in memory first - a prior
 * version that buffered everything in one growing byte array hit Java's array size limit on the
 * real ~5.1M edge dataset.
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

      String copySQL =
          """
                    COPY edges (name, dependencyinstanceid, serviceid, governanceactive, \
                    sourcenode_id, destinationnode_id, added_in_version_rank, \
                    sourcecomponentinstanceid, destinationcomponentinstanceid) FROM STDIN WITH (FORMAT BINARY)""";
      org.postgresql.copy.CopyManager copyManager =
          copyManagerFactory.create((org.postgresql.core.BaseConnection) unwrappedConnection);
      streamBinaryCopyOperation(copyManager, copySQL, edges, nodeIdMap, versionRank);

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
   * Opens the COPY BINARY stream and writes edge rows directly into it through a bounded buffer
   * ({@link CopyInOutputStream}), verifying the inserted row count and cancelling the COPY on
   * failure. Never holds the full edge payload in memory at once.
   */
  private static void streamBinaryCopyOperation(
      org.postgresql.copy.CopyManager copyManager,
      String copySQL,
      Collection<Edge> edges,
      Map<ComponentInstance, Long> nodeIdMap,
      int versionRank)
      throws SQLException {
    String versionLabel = String.valueOf(versionRank);
    org.postgresql.copy.CopyIn copyIn = copyManager.copyIn(copySQL);
    try {
      DataOutputStream dataStream =
          new DataOutputStream(new CopyInOutputStream(copyIn, COPY_BUFFER_SIZE));
      int validEdgeCount = writeBinaryEdgeData(dataStream, edges, nodeIdMap, versionRank);

      Log.debugf(
          "Processing %d valid edges out of %d total edges using COPY BINARY format",
          validEdgeCount, edges.size());

      long copiedRows = copyIn.endCopy();
      Log.debugf("COPY BINARY inserted %d rows", copiedRows);

      if (copiedRows != validEdgeCount) {
        throw new GraphSaveException(
            String.format(
                """
                                COPY BINARY edge row count mismatch for version %s: expected %d, got %d. \
                                Possible data corruption or driver error.""",
                versionLabel, validEdgeCount, copiedRows));
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

  /**
   * Buffers writes up to a fixed size and flushes each full buffer straight to {@link
   * org.postgresql.copy.CopyIn#writeToCopy}, so the caller never has to materialize the whole COPY
   * payload as a single byte array (that's what overflowed Java's array size limit on the real
   * ~5.1M edge dataset. Package-private (not private) so it can be exercised directly by tests.
   */
  static final class CopyInOutputStream extends OutputStream {
    private final org.postgresql.copy.CopyIn copyIn;
    private final byte[] buffer;
    private int position;

    CopyInOutputStream(org.postgresql.copy.CopyIn copyIn, int bufferSize) {
      this.copyIn = copyIn;
      this.buffer = new byte[bufferSize];
    }

    @Override
    public void write(int b) throws IOException {
      if (position == buffer.length) {
        flush();
      }
      buffer[position++] = (byte) b;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      int remaining = len;
      int srcOffset = off;
      while (remaining > 0) {
        if (position == buffer.length) {
          flush();
        }
        int toCopy = Math.min(buffer.length - position, remaining);
        System.arraycopy(b, srcOffset, buffer, position, toCopy);
        position += toCopy;
        srcOffset += toCopy;
        remaining -= toCopy;
      }
    }

    @Override
    public void flush() throws IOException {
      if (position > 0) {
        try {
          copyIn.writeToCopy(buffer, 0, position);
        } catch (SQLException e) {
          throw new IOException("Failed to stream binary COPY data to PostgreSQL", e);
        }
        position = 0;
      }
    }
  }
}
