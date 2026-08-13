package com.amadeus.aud.libraries.engine.data.repositories.base;

import jakarta.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentRepository<T> extends BaseRepository<ConcurrentMap<String, T>> {
  public ConcurrentRepository() {
    super(new ConcurrentHashMap<>());
  }

  public @Nullable T get(@Nullable String id) {
    if (id == null) {
      return null;
    }
    return getData().get(id);
  }

  public @Nullable T put(String id, T object) {
    return getData().put(id, object);
  }
}
