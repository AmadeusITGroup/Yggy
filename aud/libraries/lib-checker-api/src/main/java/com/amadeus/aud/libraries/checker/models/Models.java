package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public interface Models {
  /** Request all objects, no matter their types. */
  Collection<ModelElement> all(@Nullable CharSequence domain);

  /**
   * Returns the latest integrated models for the given domain; this does NOT include the changes in
   * the current domain.
   */
  Map<Class<? extends ModelElement>, List<? extends ModelElement>> latestIntegrated(String domain);

  /**
   * Request a collection of objects with a specific type from the Models. If a non-null domain is
   * provided, select only objects of that domain.
   *
   * <p>e.g.: list(V1Dependency.class, null) will return a collection of all Dependencies
   */
  <T extends ModelElement> Collection<T> list(Class<T> type, @Nullable CharSequence domain);

  /**
   * Request a uniq object with a specific type from the Models.
   *
   * <p>e.g.: single(V1Dependency.class, "hello") will return a Dependency with name "hello".
   *
   * <p>It might return more than one object if the Models contain multiple objects with the same
   * name
   */
  <T extends ModelElement> SequencedCollection<T> single(Class<T> type, CharSequence name);

  /**
   * Request all objects as a map, no matter their types. If a non-null domain is provided, select
   * only objects of that domain.
   */
  ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> allAsMap(
      @Nullable CharSequence domain);

  /**
   * Load the latest known version, overriding the domains with the provided data.
   *
   * <p>This effectively return a 'candidate' AUD version as-if domains were merged with the latest
   * data.
   *
   * @param domains domains whose stored elements are replaced by {@code domainsData}; a listed
   *     domain without elements in {@code domainsData} ends up empty. Empty to load the stored data
   *     as is.
   * @param domainsData the replacement elements, per type
   */
  void loadLatestWithDomains(
      Set<String> domains, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData);

  /**
   * @return the AudDataRepository linked
   */
  AudDataRepository repository();

  List<String> allDomainNames();
}
