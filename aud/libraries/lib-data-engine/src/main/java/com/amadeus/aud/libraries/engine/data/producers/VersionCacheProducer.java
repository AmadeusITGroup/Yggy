package com.amadeus.aud.libraries.engine.data.producers;

import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.CaffeineCacheConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class VersionCacheProducer {
  private static final String DEFAULT_CACHE_NAME = "version-cache";

  private final AudModelConfig versionConfig;
  private final MeterRegistry meterRegistry;

  private LoadingCache<String, Version> cache;
  private CacheLoader<String, Version> loader;

  @Inject
  public VersionCacheProducer(AudModelConfig versionConfig, MeterRegistry meterRegistry) {
    this.versionConfig = versionConfig;
    this.meterRegistry = meterRegistry;
  }

  private LoadingCache<String, Version> buildCache() {
    if (loader == null) {
      throw new IllegalStateException("CacheLoader must be set before creating the cache");
    }

    Optional<CaffeineCacheConfig> maybeCache = versionConfig.caffeineCache();
    if (maybeCache.isEmpty()) {
      Log.warn("Caffeine cache configuration is not provided, using default settings.");
      return Caffeine.newBuilder().build(loader);
    } else {
      CaffeineCacheConfig caffeineCacheConfig = maybeCache.get();
      long maximumSize = caffeineCacheConfig.maximumGraphCacheSize();
      Log.infof("Creating version cache with maximum size: %d", maximumSize);

      Caffeine<Object, Object> caffeineCache = Caffeine.newBuilder().maximumSize(maximumSize);

      if (caffeineCacheConfig.enableMetrics()) {
        Log.info("Enabling metrics for graph cache");
        caffeineCache.recordStats();
      } else {
        Log.info("Graph cache metrics are disabled");
      }

      LoadingCache<String, Version> cache = caffeineCache.build(loader);

      if (caffeineCacheConfig.enableMetrics()) {
        Log.info("Registering graph cache metrics with Micrometer");
        CaffeineCacheMetrics.monitor(meterRegistry, cache, DEFAULT_CACHE_NAME);
      }
      return cache;
    }
  }

  public LoadingCache<String, Version> getCache() {
    if (this.cache == null) {
      this.cache = buildCache();
    }
    return cache;
  }

  public void setLoader(CacheLoader<String, Version> loader) {
    this.loader = loader;
  }
}
