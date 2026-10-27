package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.VersionBuilder;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * \@Unremovable, because used programmatically, and quarkus is eager to remove if from the jar
 * otherwise.
 */
@Singleton
@Unremovable
public class BomFromAudRepository implements Bom {
  private final VersionBuilder versionBuilder;
  private Version version;
  private @Nullable VersionIndex versionIndex;

  @Inject
  public BomFromAudRepository(VersionBuilder versionBuilder) {
    this.versionBuilder = versionBuilder;
  }

  /**
   * Builds the {@link Version} for this BOM and indexes its elements by scope.
   *
   * <p>A built version is a snapshot: it must not be modified afterwards. Scoped {@link #list}
   * results reflect the version as it was when this method returned.
   */
  public void initWithAudDataRepository(AudDataRepository audDataRepository) {
    this.version = this.versionBuilder.buildVersion("999.999.999", audDataRepository);
    this.versionIndex = this.version == null ? null : VersionIndex.of(this.version);
  }

  @Override
  public Collection<BomElement> all(@Nullable CharSequence domain) {
    List<BomElement> output = new ArrayList<>();
    for (Iterator<BomElement> it = this.version.all(); it.hasNext(); ) {
      BomElement element = it.next();
      if (domain == null
          || (element.getScope() != null && element.getScope().getName().contentEquals(domain))) {
        output.add(element);
      }
    }
    return output;
  }

  /**
   * Lists the elements of a type, optionally restricted to a domain.
   *
   * <p>Scoped results come from an index built by {@link #initWithAudDataRepository}, so they
   * reflect the {@link Version} as built: changes made to it afterwards are not seen. If the index
   * does not cover the current version or repository, the repository is scanned instead.
   */
  @Override
  public <T extends BomElement> Collection<T> list(Class<T> type, @Nullable CharSequence domain) {
    ConcurrentRepository<T> repository = this.version.getRepository(type);
    if (repository == null) {
      Log.errorf("Repository (class: %s) not found", type.getName());
      return List.of();
    }
    if (domain == null) {
      return new ArrayList<>(repository.getData().values());
    }
    // Checks call list(type, domain) once per domain: use the scope index instead of rescanning.
    @Nullable ScopeIndex<T> index = scopeIndex(repository);
    if (index == null) {
      return scan(repository, domain);
    }
    List<T> scoped = index.byScope().getOrDefault(domain.toString(), List.of());
    List<T> output = new ArrayList<>(scoped.size() + index.alwaysIncluded().size());
    output.addAll(scoped);
    output.addAll(index.alwaysIncluded());
    return output;
  }

  @SuppressWarnings("unchecked")
  private <T extends BomElement> @Nullable ScopeIndex<T> scopeIndex(
      ConcurrentRepository<T> repository) {
    VersionIndex index = this.versionIndex;
    if (index == null || index.version() != this.version) {
      return null;
    }
    return (ScopeIndex<T>) index.byRepository().get(repository);
  }

  private static <T extends BomElement> List<T> scan(
      ConcurrentRepository<T> repository, CharSequence domain) {
    List<T> output = new ArrayList<>();
    for (T element : repository.getData().values()) {
      if ((element.getScope() != null && element.getScope().getName().contentEquals(domain))
          || isAlwaysIncluded(element)) {
        output.add(element);
      }
    }
    return output;
  }

  /** Model entities that are not {@link AbstractBase} are returned for every domain. */
  private static boolean isAlwaysIncluded(BomElement element) {
    return element instanceof AbstractModelEntity<?> && !(element instanceof AbstractBase<?>);
  }

  /**
   * Scope indexes of every repository of one {@link Version}, built once the version is built.
   *
   * @param version the version the index was built from, compared by identity
   * @param byRepository index per repository, keyed by repository identity
   */
  private record VersionIndex(
      Version version, Map<ConcurrentRepository<?>, ScopeIndex<?>> byRepository) {
    static VersionIndex of(Version version) {
      Map<ConcurrentRepository<?>, ScopeIndex<?>> byRepository = new IdentityHashMap<>();
      for (String name : version.getRepositoryNames()) {
        ConcurrentRepository<BomElement> repository = version.getRepository(name);
        if (repository != null) {
          byRepository.put(repository, ScopeIndex.of(repository.getData().values()));
        }
      }
      return new VersionIndex(version, Collections.unmodifiableMap(byRepository));
    }
  }

  /**
   * Elements of one repository grouped by scope name. Immutable.
   *
   * @param byScope elements per scope name
   * @param alwaysIncluded elements returned for every domain, see {@link #isAlwaysIncluded}
   */
  private record ScopeIndex<T extends BomElement>(
      Map<String, List<T>> byScope, List<T> alwaysIncluded) {
    static <T extends BomElement> ScopeIndex<T> of(Collection<T> elements) {
      Map<String, List<T>> byScope = new HashMap<>();
      List<T> alwaysIncluded = new ArrayList<>();
      for (T element : elements) {
        if (isAlwaysIncluded(element)) {
          alwaysIncluded.add(element);
        } else if (element.getScope() != null && element.getScope().getName() != null) {
          byScope
              .computeIfAbsent(element.getScope().getName(), (String scope) -> new ArrayList<>())
              .add(element);
        }
      }
      Map<String, List<T>> frozen = new HashMap<>(byScope.size());
      byScope.forEach((String scope, List<T> scoped) -> frozen.put(scope, List.copyOf(scoped)));
      return new ScopeIndex<>(Map.copyOf(frozen), List.copyOf(alwaysIncluded));
    }
  }

  @Override
  public Version getVersion() {
    return version;
  }
}
