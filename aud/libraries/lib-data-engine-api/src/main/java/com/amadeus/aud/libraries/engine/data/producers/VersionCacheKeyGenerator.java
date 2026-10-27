package com.amadeus.aud.libraries.engine.data.producers;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CompositeCacheKey;
import java.lang.reflect.Method;

public class VersionCacheKeyGenerator implements CacheKeyGenerator {
  @Override
  public Object generate(Method method, Object... methodParams) {
    if (methodParams.length == 0) {
      return new CompositeCacheKey(method.getName(), methodParams);
    }

    if (methodParams[0] instanceof Version v) {
      Object[] newParams = new Object[methodParams.length];
      newParams[0] = v.getVersionNumber();
      System.arraycopy(methodParams, 1, newParams, 1, methodParams.length - 1);
      return new CompositeCacheKey(method.getName(), newParams);
    }

    return new CompositeCacheKey(method.getName(), methodParams);
  }
}
