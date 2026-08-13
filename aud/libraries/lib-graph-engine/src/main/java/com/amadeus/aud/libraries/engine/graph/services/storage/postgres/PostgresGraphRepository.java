package com.amadeus.aud.libraries.engine.graph.services.storage.postgres;

import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class PostgresGraphRepository implements PanacheRepository<GraphEntity> {

  private static final int BATCH_SIZE = 100_000;

  @Inject Instance<EntityManager> entityManagerInstance;

  private EntityManager entityManager() {
    if (entityManagerInstance.isUnsatisfied()) {
      throw new IllegalStateException(
          "EntityManager is not available. Ensure Hibernate ORM is enabled when using Postgres graph storage.");
    }
    return entityManagerInstance.get();
  }

  public Optional<GraphEntity> findByVersion(String version) {
    return find("version", version).firstResultOptional();
  }

  public Optional<GraphStatistics> findStatisticsByVersion(String version) {
    return Optional.ofNullable(
        entityManager()
            .createQuery(
                """
                SELECT new com.amadeus.aud.libraries.engine.graph.models.GraphStatistics(
                  g.statisticsScopeNbLink,
                  g.statisticsScopeNbNode,
                  g.statisticsGlobalNodesCount,
                  g.statisticsGlobalLinksCount
                )
                FROM GraphEntity g
                WHERE g.version = :version
                """,
                GraphStatistics.class)
            .setParameter("version", version)
            .setHint("org.hibernate.readOnly", true)
            .setHint("org.hibernate.cacheable", true)
            .getSingleResultOrNull());
  }

  public Set<String> findAllVersions() {
    return new LinkedHashSet<>(
        entityManager()
            .createQuery(
                """
                        SELECT new java.lang.String(g.version) \
                        FROM GraphEntity g
                        """,
                String.class)
            // Large fetch size for PostgreSQL
            .setHint("org.hibernate.fetchSize", BATCH_SIZE)
            .setHint("org.hibernate.readOnly", true)
            .setHint("org.hibernate.cacheable", true)
            .getResultList());
  }

  public Optional<GraphEntity> findLatestVersion() {
    return Optional.ofNullable(
        entityManager()
            .createQuery(
                """
                    SELECT g FROM GraphEntity g ORDER BY \
                    CAST(SUBSTRING(g.version, 1, LOCATE('.', g.version) - 1) AS int) DESC, \
                    CAST(SUBSTRING(g.version, LOCATE('.', g.version) + 1, \
                    LOCATE('.', g.version, LOCATE('.', g.version) + 1) - LOCATE('.', g.version) - 1) AS int) DESC, \
                    CAST(SUBSTRING(g.version, LOCATE('.', g.version, LOCATE('.', g.version) + 1) + 1) AS int) DESC""",
                GraphEntity.class)
            .setMaxResults(1)
            .getSingleResultOrNull());
  }

  /** Computes the next sequential version rank for a new graph version. */
  public int computeNextVersionRank() {
    Integer maxRank =
        entityManager()
            .createQuery("SELECT MAX(g.versionRank) FROM GraphEntity g", Integer.class)
            .getSingleResult();
    return (maxRank != null ? maxRank : 0) + 1;
  }

  /** Finds the graph entity with the highest version rank (latest by insertion order). */
  public Optional<GraphEntity> findByMaxVersionRank() {
    return Optional.ofNullable(
        entityManager()
            .createQuery(
                "SELECT g FROM GraphEntity g ORDER BY g.versionRank DESC", GraphEntity.class)
            .setMaxResults(1)
            .getSingleResultOrNull());
  }
}
