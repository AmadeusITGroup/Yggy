package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class DummyModels implements Models {
  @Override
  public Collection<ModelElement> all(@Nullable CharSequence domain) {
    return List.of();
  }

  @Override
  public Map<Class<? extends ModelElement>, List<? extends ModelElement>> latestIntegrated(
      String domain) {
    return Map.of();
  }

  @Override
  public <T extends ModelElement> Collection<T> list(Class<T> type, @Nullable CharSequence domain) {
    return List.of();
  }

  @Override
  public <T extends ModelElement> SequencedCollection<T> single(Class<T> type, CharSequence name) {
    return List.of();
  }

  @Override
  public ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> allAsMap(
      @Nullable CharSequence domain) {
    return new ConcurrentHashMap<>();
  }

  @Override
  public void loadLatestWithDomains(
      Set<String> domains, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData) {}

  @Override
  public @jakarta.annotation.Nullable AudDataRepository repository() {
    return null;
  }

  @Override
  public List<String> allDomainNames() {
    return List.of();
  }
}
