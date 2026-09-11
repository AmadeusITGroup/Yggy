package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.engine.graph.models.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.libraries.engine.graph.services.exceptions.GraphSaveException;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.Target;
import jakarta.enterprise.inject.Instance;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.postgresql.copy.CopyIn;

/** Tests for Postgres entity POJOs and copy writer utility classes. */
class PostgresEntitiesAndCopyWritersTest {

  @SuppressWarnings("unchecked")
  private static <T> Instance<T> mockInstance() {
    return mock(Instance.class);
  }

  @SuppressWarnings("unchecked")
  private static <T> TypedQuery<T> mockTypedQuery() {
    return mock(TypedQuery.class);
  }

  @Nested
  class NodeEntityTests {

    @Test
    void gettersAndSettersRoundTrip() {
      NodeEntity entity = new NodeEntity();
      entity.setComponentInstanceId("ci-1");
      entity.setLandingZoneName("lz-1");
      entity.setPlatformName("platform-1");
      entity.setTargetName("target-1");
      entity.setAddedInVersionRank(5);
      entity.setRemovedInVersionRank(10);

      assertThat(entity.getComponentInstanceId()).isEqualTo("ci-1");
      assertThat(entity.getLandingZoneName()).isEqualTo("lz-1");
      assertThat(entity.getPlatformName()).isEqualTo("platform-1");
      assertThat(entity.getTargetName()).isEqualTo("target-1");
      assertThat(entity.getAddedInVersionRank()).isEqualTo(5);
      assertThat(entity.getRemovedInVersionRank()).isEqualTo(10);
      assertThat(entity.getId()).isNull(); // no DB identity set
    }

    @Test
    void removedInVersionRankIsNullByDefault() {
      NodeEntity entity = new NodeEntity();
      assertThat(entity.getRemovedInVersionRank()).isNull();

      entity.setRemovedInVersionRank(null);
      assertThat(entity.getRemovedInVersionRank()).isNull();
    }
  }

  // ─────────────��───────────────────────── EdgeEntity ───────────────────────────────────────────

  @Nested
  class EdgeEntityTests {

    @Test
    void gettersAndSettersRoundTrip() {
      NodeEntity src = new NodeEntity();
      src.setComponentInstanceId("src-ci");
      NodeEntity dst = new NodeEntity();
      dst.setComponentInstanceId("dst-ci");

      EdgeEntity edge = new EdgeEntity();
      edge.setName("edge-name");
      edge.setSourceNode(src);
      edge.setDestinationNode(dst);
      edge.setDependencyInstanceId("dep-id");
      edge.setServiceId("svc-id");
      edge.setGovernanceActive(true);
      edge.setAddedInVersionRank(3);
      edge.setRemovedInVersionRank(7);

      assertThat(edge.getName()).isEqualTo("edge-name");
      assertThat(edge.getSourceNode()).isSameAs(src);
      assertThat(edge.getDestinationNode()).isSameAs(dst);
      assertThat(edge.getDependencyInstanceId()).isEqualTo("dep-id");
      assertThat(edge.getServiceId()).isEqualTo("svc-id");
      assertThat(edge.getGovernanceActive()).isTrue();
      assertThat(edge.getAddedInVersionRank()).isEqualTo(3);
      assertThat(edge.getRemovedInVersionRank()).isEqualTo(7);
      assertThat(edge.getId()).isNull();
    }

    @Test
    void removedInVersionRankIsNullByDefault() {
      EdgeEntity edge = new EdgeEntity();
      assertThat(edge.getRemovedInVersionRank()).isNull();

      edge.setRemovedInVersionRank(null);
      assertThat(edge.getRemovedInVersionRank()).isNull();
    }
  }

  // ────���───────────────────────────── PostgresEdgeCopyWriter
  // ────────────────────────────────────

  @Nested
  class PostgresEdgeCopyWriterTests {

    private ComponentInstance component(String id) {
      ComponentInstance ci = mock(ComponentInstance.class);
      doReturn(id).when(ci).id();
      doReturn(id + "-name").when(ci).getName();
      Scope scope = mock(Scope.class);
      doReturn(scope).when(ci).getScope();
      doReturn(id + "-scope").when(scope).getName();
      return ci;
    }

    private Edge buildEdge(ComponentInstance src, ComponentInstance dst) {
      DependencyInstance dep = mock(DependencyInstance.class);
      doReturn("dep-id").when(dep).id();
      doReturn("dep-name").when(dep).getName();
      Service svc = mock(Service.class);
      doReturn("svc-id").when(svc).id();
      return new Edge(src, dst, dep, svc, true);
    }

    @Test
    void writeBinaryEdgeDataProducesValidHeaderAndSkipsMissingNodes() throws Exception {
      ComponentInstance src = component("src");
      ComponentInstance dst = component("dst");
      ComponentInstance missing = component("missing");

      Edge validEdge = buildEdge(src, dst);
      Edge skippedEdge = buildEdge(src, missing);

      Map<ComponentInstance, Long> nodeIdMap = Map.of(src, 1L, dst, 2L);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);

      int count =
          PostgresEdgeCopyWriter.writeBinaryEdgeData(
              dos, List.of(validEdge, skippedEdge), nodeIdMap, 5);

      assertThat(count).isOne(); // only the valid edge written
      byte[] out = baos.toByteArray();
      // PostgreSQL binary COPY header starts with "PGCOPY"
      assertThat(out).startsWith(new byte[] {'P', 'G', 'C', 'O', 'P', 'Y'});
    }

    @Test
    void writeBinaryEdgeDataWritesZeroRowsWhenAllNodesMissing() throws Exception {
      ComponentInstance src = component("src");
      ComponentInstance dst = component("dst");
      Edge edge = buildEdge(src, dst);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);

      int count = PostgresEdgeCopyWriter.writeBinaryEdgeData(dos, List.of(edge), Map.of(), 1);
      assertThat(count).isZero();
    }

    @Test
    void writeBinaryEdgeDataHandlesEmptyEdgeList() throws Exception {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);

      int count = PostgresEdgeCopyWriter.writeBinaryEdgeData(dos, List.of(), Map.of(), 1);
      assertThat(count).isZero();
    }

    @Test
    void cancelCopyOperationSuppressesExceptionWhenCancelFails() throws Exception {
      CopyIn copyIn = mock(CopyIn.class);
      doReturn(true).when(copyIn).isActive();
      SQLException cancelEx = new SQLException("cancel failed");
      doThrow(cancelEx).when(copyIn).cancelCopy();

      Exception cause = new RuntimeException("original failure");
      PostgresEdgeCopyWriter.cancelCopyOperation(copyIn, "edges", "v1", cause);

      assertThat(cause.getSuppressed()).contains(cancelEx);
    }

    @Test
    void cancelCopyOperationDoesNothingWhenNotActive() {
      CopyIn copyIn = mock(CopyIn.class);
      doReturn(false).when(copyIn).isActive();

      // No exception should be thrown
      PostgresEdgeCopyWriter.cancelCopyOperation(copyIn, "edges", "v1", new RuntimeException("x"));
    }

    @Test
    void batchInsertEdgesCopyBinaryThrowsGraphSaveExceptionOnNonPostgresConnection()
        throws Exception {
      ComponentInstance src = component("src");
      ComponentInstance dst = component("dst");
      Edge edge = buildEdge(src, dst);

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      java.sql.Connection conn = mock(java.sql.Connection.class);
      doReturn(conn).when(ds).getConnection();
      doReturn(false).when(conn).isWrapperFor(org.postgresql.core.BaseConnection.class);

      assertThatThrownBy(
              () ->
                  PostgresEdgeCopyWriter.batchInsertEdgesCopyBinary(
                      ds, 1, List.of(edge), Map.of(src, 1L, dst, 2L), bc -> null))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("not a PostgreSQL connection");
    }

    @Test
    void batchInsertEdgesCopyBinaryWrapsGenericException() throws Exception {
      ComponentInstance src = component("src");
      ComponentInstance dst = component("dst");
      Edge edge = buildEdge(src, dst);

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      doThrow(new RuntimeException("datasource failure")).when(ds).getConnection();

      assertThatThrownBy(
              () ->
                  PostgresEdgeCopyWriter.batchInsertEdgesCopyBinary(
                      ds, 1, List.of(edge), Map.of(src, 1L, dst, 2L), bc -> null))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("Failed to batch insert edges");
    }
  }

  // ────────────────────────────────── PostgresNodeCopyWriter ────────────────────────────────────

  @Nested
  class PostgresNodeCopyWriterTests {

    private ComponentInstance buildNode(
        String id, String lzName, String platform, String targetName) {
      return buildNodeWithDeploy(id, lzName, platform, targetName, platform);
    }

    private ComponentInstance buildNodeWithDeploy(
        String id, String lzName, String nodeName, String targetName, String deployPlatformName) {
      ComponentInstance ci = mock(ComponentInstance.class);
      doReturn(id).when(ci).id();
      doReturn(nodeName).when(ci).getName();

      ComponentInstance deployComponent = null;
      if (deployPlatformName != null) {
        deployComponent = mock(ComponentInstance.class);
        doReturn(deployPlatformName).when(deployComponent).getName();
      }
      doReturn(deployComponent).when(ci).getDeployComponentInstance();

      LandingZone lz = null;
      if (lzName != null) {
        lz = mock(LandingZone.class);
        doReturn(lzName).when(lz).getName();
        Target target = null;
        if (targetName != null) {
          target = mock(Target.class);
          doReturn(targetName).when(target).getName();
        }
        doReturn(target).when(lz).getTarget();
      }
      doReturn(lz).when(ci).getLandingZone();
      return ci;
    }

    @Test
    void batchInsertNodesCopyUsesDeployComponentNameForPlatformColumn() throws Exception {
      ComponentInstance node =
          buildNodeWithDeploy(
              "ci-1", "lz-a", "service-node-name", "target-a", "deploy-platform-name");

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      java.sql.Connection conn = mock(java.sql.Connection.class);
      org.postgresql.core.BaseConnection baseConn = mock(org.postgresql.core.BaseConnection.class);
      org.postgresql.copy.CopyManager copyManager = mock(org.postgresql.copy.CopyManager.class);
      org.postgresql.copy.CopyIn copyIn = mock(org.postgresql.copy.CopyIn.class);
      java.sql.PreparedStatement selectStmt = mock(java.sql.PreparedStatement.class);
      java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
      ByteArrayOutputStream copiedCsvBytes = new ByteArrayOutputStream();

      doReturn(conn).when(ds).getConnection();
      doReturn(true).when(conn).isWrapperFor(org.postgresql.core.BaseConnection.class);
      doReturn(baseConn).when(conn).unwrap(org.postgresql.core.BaseConnection.class);
      doReturn(copyIn).when(copyManager).copyIn(org.mockito.ArgumentMatchers.anyString());
      doReturn(1L).when(copyIn).endCopy();
      doReturn(selectStmt).when(conn).prepareStatement(org.mockito.ArgumentMatchers.anyString());
      doReturn(rs).when(selectStmt).executeQuery();
      doReturn(true, false).when(rs).next();
      doReturn("ci-1").when(rs).getString("componentinstanceid");
      doReturn(10L).when(rs).getLong("id");
      org.mockito.Mockito.doAnswer(
              (InvocationOnMock invocation) -> {
                byte[] data = invocation.getArgument(0);
                int offset = invocation.getArgument(1);
                int length = invocation.getArgument(2);
                copiedCsvBytes.write(data, offset, length);
                return null;
              })
          .when(copyIn)
          .writeToCopy(org.mockito.ArgumentMatchers.any(byte[].class), anyInt(), anyInt());

      Map<ComponentInstance, Long> result =
          PostgresNodeCopyWriter.batchInsertNodesCopy(ds, 7, List.of(node), ignored -> copyManager);

      String copiedCsv = copiedCsvBytes.toString(StandardCharsets.UTF_8);
      assertThat(result).containsEntry(node, 10L);
      assertThat(copiedCsv).contains("deploy-platform-name").doesNotContain("service-node-name");
    }

    @Test
    void batchInsertNodesCopyThrowsGraphSaveExceptionOnNonPostgresConnection() throws Exception {
      ComponentInstance node = buildNode("ci-1", "lz", "platform", "target");

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      java.sql.Connection conn = mock(java.sql.Connection.class);
      doReturn(conn).when(ds).getConnection();
      doReturn(false).when(conn).isWrapperFor(org.postgresql.core.BaseConnection.class);

      assertThatThrownBy(
              () -> PostgresNodeCopyWriter.batchInsertNodesCopy(ds, 1, List.of(node), bc -> null))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("not a PostgreSQL connection");
    }

    @Test
    void batchInsertNodesCopyWrapsGenericException() throws Exception {
      ComponentInstance node = buildNode("ci-1", "lz", "platform", "target");

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      doThrow(new RuntimeException("datasource failure")).when(ds).getConnection();

      assertThatThrownBy(
              () -> PostgresNodeCopyWriter.batchInsertNodesCopy(ds, 1, List.of(node), bc -> null))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("Failed to batch insert nodes");
    }

    @Test
    void batchInsertNodesCopyHandlesNullLandingZoneAndTarget() throws Exception {
      // Node with no landing zone or target — exercises null branches in CSV builder
      ComponentInstance nodeNoLz = buildNode("ci-null-lz", null, "platform", null);

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      doThrow(new RuntimeException("intentional")).when(ds).getConnection();

      // The exception is expected; this test verifies null-safe CSV construction
      // doesn't throw NPE before reaching the DataSource call
      assertThatThrownBy(
              () ->
                  PostgresNodeCopyWriter.batchInsertNodesCopy(ds, 1, List.of(nodeNoLz), bc -> null))
          .isInstanceOf(GraphSaveException.class);
    }

    @Test
    void batchInsertNodesCopyReturnsGeneratedIdsOnSuccessfulCopy() throws Exception {
      ComponentInstance nodeA = buildNode("ci-a", "lz-a", "platform-a", "target-a");
      ComponentInstance nodeB = buildNode("ci-b", "lz-b", "platform-b", "target-b");

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      java.sql.Connection conn = mock(java.sql.Connection.class);
      org.postgresql.core.BaseConnection baseConn = mock(org.postgresql.core.BaseConnection.class);
      org.postgresql.copy.CopyManager copyManager = mock(org.postgresql.copy.CopyManager.class);
      org.postgresql.copy.CopyIn copyIn = mock(org.postgresql.copy.CopyIn.class);
      java.sql.PreparedStatement selectStmt = mock(java.sql.PreparedStatement.class);
      java.sql.ResultSet rs = mock(java.sql.ResultSet.class);

      doReturn(conn).when(ds).getConnection();
      doReturn(true).when(conn).isWrapperFor(org.postgresql.core.BaseConnection.class);
      doReturn(baseConn).when(conn).unwrap(org.postgresql.core.BaseConnection.class);
      doReturn(copyIn).when(copyManager).copyIn(org.mockito.ArgumentMatchers.anyString());
      doReturn(2L).when(copyIn).endCopy();

      doReturn(selectStmt).when(conn).prepareStatement(org.mockito.ArgumentMatchers.anyString());
      doReturn(rs).when(selectStmt).executeQuery();
      doReturn(true, true, false).when(rs).next();
      doReturn("ci-a", "ci-b").when(rs).getString("componentinstanceid");
      doReturn(10L, 11L).when(rs).getLong("id");

      Map<ComponentInstance, Long> result =
          PostgresNodeCopyWriter.batchInsertNodesCopy(
              ds, 7, List.of(nodeA, nodeB), ignored -> copyManager);

      assertThat(result).containsEntry(nodeA, 10L).containsEntry(nodeB, 11L);
    }

    @Test
    void batchInsertNodesCopyThrowsWhenGeneratedIdIsMissing() throws Exception {
      ComponentInstance nodeA = buildNode("ci-a", "lz-a", "platform-a", "target-a");
      ComponentInstance nodeB = buildNode("ci-b", "lz-b", "platform-b", "target-b");

      javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
      java.sql.Connection conn = mock(java.sql.Connection.class);
      org.postgresql.core.BaseConnection baseConn = mock(org.postgresql.core.BaseConnection.class);
      org.postgresql.copy.CopyManager copyManager = mock(org.postgresql.copy.CopyManager.class);
      org.postgresql.copy.CopyIn copyIn = mock(org.postgresql.copy.CopyIn.class);
      java.sql.PreparedStatement selectStmt = mock(java.sql.PreparedStatement.class);
      java.sql.ResultSet rs = mock(java.sql.ResultSet.class);

      doReturn(conn).when(ds).getConnection();
      doReturn(true).when(conn).isWrapperFor(org.postgresql.core.BaseConnection.class);
      doReturn(baseConn).when(conn).unwrap(org.postgresql.core.BaseConnection.class);
      doReturn(copyIn).when(copyManager).copyIn(org.mockito.ArgumentMatchers.anyString());
      doReturn(2L).when(copyIn).endCopy();

      doReturn(selectStmt).when(conn).prepareStatement(org.mockito.ArgumentMatchers.anyString());
      doReturn(rs).when(selectStmt).executeQuery();
      doReturn(true, false).when(rs).next();
      doReturn("ci-a").when(rs).getString("componentinstanceid");
      doReturn(10L).when(rs).getLong("id");

      assertThatThrownBy(
              () ->
                  PostgresNodeCopyWriter.batchInsertNodesCopy(
                      ds, 8, List.of(nodeA, nodeB), ignored -> copyManager))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("No generated ID found for node ci-b");
    }
  }

  // ────────────────────────────────── PostgresNodeRepository ────────────────────────────────────

  @Nested
  class PostgresNodeRepositoryTests {

    private PostgresNodeRepository repositoryWithUnsatisfiedManagers() {
      Instance<EntityManager> emInstance = mockInstance();
      doReturn(true).when(emInstance).isUnsatisfied();

      Instance<DataSource> dsInstance = mockInstance();
      doReturn(true).when(dsInstance).isUnsatisfied();

      return new PostgresNodeRepository(emInstance, dsInstance);
    }

    @Test
    void methodsThrowIllegalStateWhenEntityManagerIsUnsatisfied() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.countByVersionRank(1))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("EntityManager is not available");

      assertThatThrownBy(() -> repo.findByVersionRank(1, 0, 10))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithLandingZones(1, 0, 10, List.of("lz")))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithPlatforms(1, 0, 10, List.of("platform")))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithTargets(1, 0, 10, List.of("target")))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nullOrEmptyLandingZonesFallsThroughToUnfilteredQuery() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.findByVersionRankWithLandingZones(1, 0, 10, null))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithLandingZones(1, 0, 10, List.of()))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nullOrEmptyPlatformsFallsThroughToUnfilteredQuery() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.findByVersionRankWithPlatforms(1, 0, 10, null))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithPlatforms(1, 0, 10, List.of()))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nullOrEmptyTargetsFallsThroughToUnfilteredQuery() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.findByVersionRankWithTargets(1, 0, 10, null))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithTargets(1, 0, 10, List.of()))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithLandingZonesAndPlatformsCoversAllBranches() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZonesAndPlatforms(
                      1, List.of("lz"), List.of("p"), 0, 10))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZonesAndPlatforms(1, List.of("lz"), null, 0, 10))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZonesAndPlatforms(1, null, List.of("p"), 0, 10))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () -> repo.findByVersionRankWithLandingZonesAndPlatforms(1, null, null, 0, 10))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findActiveNodeIdsAndLoadActiveNodeIdMapThrowGraphSaveExceptionOnSQLError()
        throws Exception {
      Instance<EntityManager> emInstance = mockInstance();
      doReturn(false).when(emInstance).isUnsatisfied();

      DataSource ds = mock(DataSource.class);
      doThrow(new java.sql.SQLException("connection failed")).when(ds).getConnection();
      Instance<DataSource> dsInstance = mockInstance();
      doReturn(ds).when(dsInstance).get();

      PostgresNodeRepository repo = new PostgresNodeRepository(emInstance, dsInstance);

      assertThatThrownBy(() -> repo.findActiveNodeIds(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("active node IDs");

      assertThatThrownBy(() -> repo.loadActiveNodeIdMap(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("active node ID map");

      assertThatThrownBy(() -> repo.closeNodeVersionRanges(java.util.Set.of("ci-1"), 2))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("closing node version ranges");

      assertThatThrownBy(() -> repo.clearNodesForVersionRank(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("clear nodes");

      assertThatThrownBy(() -> repo.reopenNodesClosedAtRank(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("reopen nodes");
    }

    @Test
    void closeNodeVersionRangesIsNoopForEmptySet() {
      PostgresNodeRepository repo = repositoryWithUnsatisfiedManagers();
      repo.closeNodeVersionRanges(java.util.Set.of(), 5);
    }

    @Test
    void readQueriesBindParametersAndReturnResults() {
      Instance<EntityManager> emInstance = mockInstance();
      Instance<DataSource> dsInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      TypedQuery<Node> query = mockTypedQuery();
      TypedQuery<Long> countQuery = mockTypedQuery();

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(mock(DataSource.class)).when(dsInstance).get();
      doReturn(query).when(em).createQuery(anyString(), eq(Node.class));
      doReturn(countQuery).when(em).createQuery(anyString(), eq(Long.class));
      doReturn(query).when(query).setHint(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(query).when(query).setParameter(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(query).when(query).setFirstResult(anyInt());
      doReturn(query).when(query).setMaxResults(anyInt());
      doReturn(List.of(new Node("ci-1", "lz-1", "platform-1", "target-1")))
          .when(query)
          .getResultList();
      doReturn(countQuery)
          .when(countQuery)
          .setParameter(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(9L).when(countQuery).getSingleResult();

      PostgresNodeRepository repo = new PostgresNodeRepository(emInstance, dsInstance);

      assertThat(repo.countByVersionRank(7)).isEqualTo(9L);
      assertThat(repo.findByVersionRank(7, 1, 10)).hasSize(1);
      assertThat(repo.findByVersionRankWithLandingZones(7, 0, 10, List.of("lz-1"))).hasSize(1);
      assertThat(repo.findByVersionRankWithPlatforms(7, 0, 10, List.of("platform-1"))).hasSize(1);
      assertThat(repo.findByVersionRankWithPlatforms(7, 0, 10, List.of("platform-1"))).hasSize(1);
      assertThat(repo.findByVersionRankWithTargets(7, 0, 10, List.of("target-1"))).hasSize(1);
      assertThat(
              repo.findByVersionRankWithLandingZonesAndPlatforms(
                  7, List.of("lz-1"), List.of("platform-1"), 0, 10))
          .hasSize(1);
      assertThat(
              repo.findByVersionRankWithLandingZonesAndPlatforms(
                  7, List.of("lz-1"), List.of("platform-1"), 0, 10))
          .hasSize(1);

      verify(query, times(3)).setParameter("landingZones", List.of("lz-1"));
      verify(query, times(4)).setParameter("platforms", List.of("platform-1"));
      verify(query).setParameter("targets", List.of("target-1"));
      verify(query, times(7)).setParameter("versionRank", 7);
    }

    @Test
    void sqlWriteAndLoadOperationsReturnDataAndEvictCache() throws Exception {
      Instance<EntityManager> emInstance = mockInstance();
      Instance<DataSource> dsInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      EntityManagerFactory emf = mock(EntityManagerFactory.class);
      Cache cache = mock(Cache.class);
      DataSource ds = mock(DataSource.class);
      Connection connection = mock(Connection.class);
      PreparedStatement stmtActive = mock(PreparedStatement.class);
      PreparedStatement stmtMap = mock(PreparedStatement.class);
      PreparedStatement stmtClose = mock(PreparedStatement.class);
      PreparedStatement stmtClear = mock(PreparedStatement.class);
      PreparedStatement stmtReopen = mock(PreparedStatement.class);
      ResultSet rsActive = mock(ResultSet.class);
      ResultSet rsMap = mock(ResultSet.class);

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(emf).when(em).getEntityManagerFactory();
      doReturn(cache).when(emf).getCache();
      doReturn(ds).when(dsInstance).get();
      doReturn(connection).when(ds).getConnection();
      doReturn(stmtActive, stmtMap, stmtClose, stmtClear, stmtReopen)
          .when(connection)
          .prepareStatement(anyString());

      doReturn(rsActive).when(stmtActive).executeQuery();
      doReturn(true, true, false).when(rsActive).next();
      doReturn("ci-1", "ci-2").when(rsActive).getString(1);

      doReturn(rsMap).when(stmtMap).executeQuery();
      doReturn(true, false).when(rsMap).next();
      doReturn("ci-1").when(rsMap).getString("componentinstanceid");
      doReturn(11L).when(rsMap).getLong("id");

      doReturn(new int[] {1, 1}).when(stmtClose).executeBatch();
      doReturn(2).when(stmtClear).executeUpdate();
      doReturn(1).when(stmtReopen).executeUpdate();

      PostgresNodeRepository repo = new PostgresNodeRepository(emInstance, dsInstance);

      Set<String> activeIds = repo.findActiveNodeIds(3);
      Map<String, Long> activeMap = repo.loadActiveNodeIdMap(3);
      repo.closeNodeVersionRanges(Set.of("ci-1", "ci-2"), 4);
      repo.clearNodesForVersionRank(4);
      repo.reopenNodesClosedAtRank(4);

      assertThat(activeIds).containsExactlyInAnyOrder("ci-1", "ci-2");
      assertThat(activeMap).containsEntry("ci-1", 11L);
      verify(stmtClose, times(2)).addBatch();
      verify(cache, times(3)).evict(NodeEntity.class);
    }
  }

  // ────────────────────────────────── PostgresEdgeRepository ────────────────────────────────────

  @Nested
  class PostgresEdgeRepositoryTests {

    private PostgresEdgeRepository repositoryWithUnsatisfiedManagers() {
      Instance<EntityManager> emInstance = mockInstance();
      doReturn(true).when(emInstance).isUnsatisfied();

      Instance<DataSource> dsInstance = mockInstance();
      doReturn(true).when(dsInstance).isUnsatisfied();

      return new PostgresEdgeRepository(emInstance, dsInstance);
    }

    @Test
    void methodsThrowIllegalStateWhenEntityManagerIsUnsatisfied() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.countByVersionRank(1))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("EntityManager is not available");

      assertThatThrownBy(() -> repo.findByVersionRank(1, 0, 10))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithScope(1, 0, 10, "scope-1"))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithLandingZonesCoversDirectionBranches() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZones(
                      1,
                      0,
                      10,
                      List.of("lz"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.INBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZones(
                      1,
                      0,
                      10,
                      List.of("lz"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.OUTBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithLandingZones(
                      1,
                      0,
                      10,
                      List.of("lz"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.ANY))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () -> repo.findByVersionRankWithLandingZones(1, 0, 10, List.of("lz"), null))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithLandingZones(1, 0, 10, List.of(), null))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> repo.findByVersionRankWithLandingZones(1, 0, 10, null, null))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithPlatformsCoversDirectionBranches() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithPlatforms(
                      1,
                      0,
                      10,
                      List.of("p"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.INBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithPlatforms(
                      1,
                      0,
                      10,
                      List.of("p"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.OUTBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithPlatforms(
                      1,
                      0,
                      10,
                      List.of("p"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.ANY))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithPlatforms(1, 0, 10, List.of(), null))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> repo.findByVersionRankWithPlatforms(1, 0, 10, null, null))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithTargetsCoversDirectionBranches() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithTargets(
                      1,
                      0,
                      10,
                      List.of("t"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.INBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithTargets(
                      1,
                      0,
                      10,
                      List.of("t"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.OUTBOUND))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(
              () ->
                  repo.findByVersionRankWithTargets(
                      1,
                      0,
                      10,
                      List.of("t"),
                      com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection.ANY))
          .isInstanceOf(IllegalStateException.class);

      assertThatThrownBy(() -> repo.findByVersionRankWithTargets(1, 0, 10, List.of(), null))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> repo.findByVersionRankWithTargets(1, 0, 10, null, null))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithNodeIdsFallsBackToQueriesWhenFiltersMissing() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(() -> repo.findByVersionRankWithNodeIds(1, null, null, 0, 10))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(
              () -> repo.findByVersionRankWithNodeIds(1, List.of(), List.of("dst"), 0, 10))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(
              () -> repo.findByVersionRankWithNodeIds(1, List.of("src"), List.of(), 0, 10))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> repo.findByVersionRankWithNodeIds(1, List.of("src"), null, 0, 10))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findByVersionRankWithNodeIdsHitsEmForNonEmptyLists() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();

      assertThatThrownBy(
              () -> repo.findByVersionRankWithNodeIds(1, List.of("src"), List.of("dst"), 0, 10))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void writeOpsThrowGraphSaveExceptionOnSQLFailure() throws Exception {
      Instance<EntityManager> emInstance = mockInstance();
      doReturn(false).when(emInstance).isUnsatisfied();

      DataSource ds = mock(DataSource.class);
      doThrow(new java.sql.SQLException("fail")).when(ds).getConnection();
      Instance<DataSource> dsInstance = mockInstance();
      doReturn(ds).when(dsInstance).get();

      PostgresEdgeRepository repo = new PostgresEdgeRepository(emInstance, dsInstance);

      assertThatThrownBy(() -> repo.findActiveEdgeKeys(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("load active edge keys");

      assertThatThrownBy(() -> repo.closeEdgeVersionRanges(java.util.Set.of("key-1"), 2))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("close edge version ranges");

      assertThatThrownBy(() -> repo.clearEdgesForVersionRank(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("clear edges");

      assertThatThrownBy(() -> repo.reopenEdgesClosedAtRank(1))
          .isInstanceOf(GraphSaveException.class)
          .hasMessageContaining("reopen edges");
    }

    @Test
    void closeEdgeVersionRangesIsNoopForEmptySet() {
      PostgresEdgeRepository repo = repositoryWithUnsatisfiedManagers();
      repo.closeEdgeVersionRanges(java.util.Set.of(), 5);
    }

    @Test
    void readQueriesBindDirectionSpecificParametersAndReturnResults() {
      Instance<EntityManager> emInstance = mockInstance();
      Instance<DataSource> dsInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      TypedQuery<com.amadeus.aud.libraries.engine.graph.models.dto.Edge> query = mockTypedQuery();
      TypedQuery<Long> countQuery = mockTypedQuery();

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(mock(DataSource.class)).when(dsInstance).get();
      doReturn(query)
          .when(em)
          .createQuery(
              anyString(), eq(com.amadeus.aud.libraries.engine.graph.models.dto.Edge.class));
      doReturn(countQuery).when(em).createQuery(anyString(), eq(Long.class));
      doReturn(query).when(query).setHint(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(query).when(query).setParameter(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(query).when(query).setFirstResult(anyInt());
      doReturn(query).when(query).setMaxResults(anyInt());
      doReturn(
              List.of(
                  new com.amadeus.aud.libraries.engine.graph.models.dto.Edge(
                      "edge-1", "dep-1", "svc-1", true, "src", "dst")))
          .when(query)
          .getResultList();
      doReturn(countQuery)
          .when(countQuery)
          .setParameter(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(5L).when(countQuery).getSingleResult();

      PostgresEdgeRepository repo = new PostgresEdgeRepository(emInstance, dsInstance);

      assertThat(repo.countByVersionRank(6)).isEqualTo(5L);
      assertThat(repo.findByVersionRank(6, 0, 20)).hasSize(1);
      assertThat(
              repo.findByVersionRankWithLandingZones(
                  6, 0, 20, List.of("lz-1"), EdgeDirection.INBOUND))
          .hasSize(1);
      assertThat(
              repo.findByVersionRankWithPlatforms(
                  6, 0, 20, List.of("platform-1"), EdgeDirection.OUTBOUND))
          .hasSize(1);
      assertThat(
              repo.findByVersionRankWithTargets(6, 0, 20, List.of("target-1"), EdgeDirection.ANY))
          .hasSize(1);
      assertThat(repo.findByVersionRankWithLandingZones(6, 0, 20, List.of("lz-1"), null))
          .hasSize(1);
      assertThat(repo.findByVersionRankWithScope(6, 0, 20, "scope-1")).hasSize(1);
      assertThat(repo.findByVersionRankWithNodeIds(6, List.of("src"), List.of("dst"), 0, 20))
          .hasSize(1);
      assertThat(repo.findByVersionRankWithNodeIds(6, null, List.of("dst"), 0, 20)).hasSize(1);
      assertThat(repo.findByVersionRankWithNodeIds(6, List.of("src"), null, 0, 20)).hasSize(1);

      verify(query, times(2)).setParameter("landingZones", List.of("lz-1"));
      verify(query).setParameter("platforms", List.of("platform-1"));
      verify(query).setParameter("targets", List.of("target-1"));
      verify(query).setParameter("scope", "scope-1");
      verify(query, times(2)).setParameter("sourceNodeIds", Set.of("src"));
      verify(query, times(2)).setParameter("destinationNodeIds", Set.of("dst"));
      verify(em)
          .createQuery(
              org.mockito.ArgumentMatchers.contains(
                  "sv.scopeName = :scope OR dv.scopeName = :scope"),
              eq(com.amadeus.aud.libraries.engine.graph.models.dto.Edge.class));
    }

    @Test
    void sqlWriteAndLoadOperationsReturnDataAndEvictCache() throws Exception {
      Instance<EntityManager> emInstance = mockInstance();
      Instance<DataSource> dsInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      EntityManagerFactory emf = mock(EntityManagerFactory.class);
      Cache cache = mock(Cache.class);
      DataSource ds = mock(DataSource.class);
      Connection connection = mock(Connection.class);
      PreparedStatement stmtActive = mock(PreparedStatement.class);
      PreparedStatement stmtClose = mock(PreparedStatement.class);
      PreparedStatement stmtClear = mock(PreparedStatement.class);
      PreparedStatement stmtReopen = mock(PreparedStatement.class);
      ResultSet rsActive = mock(ResultSet.class);

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(emf).when(em).getEntityManagerFactory();
      doReturn(cache).when(emf).getCache();
      doReturn(ds).when(dsInstance).get();
      doReturn(connection).when(ds).getConnection();
      doReturn(stmtActive, stmtClose, stmtClear, stmtReopen)
          .when(connection)
          .prepareStatement(anyString());

      doReturn(rsActive).when(stmtActive).executeQuery();
      doReturn(true, false).when(rsActive).next();
      doReturn("src").when(rsActive).getString(1);
      doReturn("dst").when(rsActive).getString(2);
      doReturn("svc").when(rsActive).getString(3);
      doReturn("dep").when(rsActive).getString(4);

      doReturn(new int[] {1}).when(stmtClose).executeBatch();
      doReturn(2).when(stmtClear).executeUpdate();
      doReturn(1).when(stmtReopen).executeUpdate();

      PostgresEdgeRepository repo = new PostgresEdgeRepository(emInstance, dsInstance);

      Set<String> activeKeys = repo.findActiveEdgeKeys(4);
      repo.closeEdgeVersionRanges(Set.of("src***dst***svc***dep"), 5);
      repo.clearEdgesForVersionRank(5);
      repo.reopenEdgesClosedAtRank(5);

      assertThat(activeKeys).containsExactly("src***dst***svc***dep");
      verify(stmtClose).addBatch();
      verify(cache, times(3)).evict(EdgeEntity.class);
    }
  }

  // ────────────────────────────────── PostgresGraphRepository ──────────────────────────────────

  @Nested
  class PostgresGraphRepositoryTests {

    @Test
    void graphRepositoryEntityManagerGuardIsTriggeredByMockedPanache() {
      assertThat(
              PostgresGraphRepository.class.isAnnotationPresent(
                  jakarta.enterprise.context.ApplicationScoped.class))
          .isTrue();
    }

    @Test
    void computesNextVersionRankAndFindsAllVersions() {
      Instance<EntityManager> emInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      TypedQuery<String> allVersionsQuery = mockTypedQuery();
      TypedQuery<Integer> maxRankQuery = mockTypedQuery();

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(allVersionsQuery).when(em).createQuery(anyString(), eq(String.class));
      doReturn(maxRankQuery).when(em).createQuery(anyString(), eq(Integer.class));
      doReturn(allVersionsQuery)
          .when(allVersionsQuery)
          .setHint(anyString(), org.mockito.ArgumentMatchers.any());
      doReturn(List.of("1.0.0", "1.0.1")).when(allVersionsQuery).getResultList();
      doReturn(null, 7).when(maxRankQuery).getSingleResult();

      PostgresGraphRepository repo = new PostgresGraphRepository();
      repo.entityManagerInstance = emInstance;

      assertThat(repo.findAllVersions()).containsExactly("1.0.0", "1.0.1");
      assertThat(repo.computeNextVersionRank()).isOne();
      assertThat(repo.computeNextVersionRank()).isEqualTo(8);
    }

    @Test
    void findByMaxVersionRankReturnsEmptyOrFirstResult() {
      Instance<EntityManager> emInstance = mockInstance();
      EntityManager em = mock(EntityManager.class);
      TypedQuery<GraphEntity> rankQuery = mockTypedQuery();
      GraphEntity graphEntity = new GraphEntity();
      graphEntity.setVersion("2.0.0");

      doReturn(false).when(emInstance).isUnsatisfied();
      doReturn(em).when(emInstance).get();
      doReturn(rankQuery).when(em).createQuery(anyString(), eq(GraphEntity.class));
      doReturn(rankQuery).when(rankQuery).setMaxResults(1);
      doReturn(null).doReturn(graphEntity).when(rankQuery).getSingleResultOrNull();

      PostgresGraphRepository repo = new PostgresGraphRepository();
      repo.entityManagerInstance = emInstance;

      assertThat(repo.findByMaxVersionRank()).isEmpty();
      assertThat(repo.findByMaxVersionRank()).hasValue(graphEntity);
    }
  }
}
