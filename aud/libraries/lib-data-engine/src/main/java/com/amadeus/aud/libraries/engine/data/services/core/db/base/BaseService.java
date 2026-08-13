package com.amadeus.aud.libraries.engine.data.services.core.db.base;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.AbstractModelEntity;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.quarkus.logging.Log;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public abstract class BaseService<T extends AbstractBase<N>, N extends ModelElement>
    extends BaseLogService {
  protected abstract ConcurrentRepository<T> getRepository(Version version);

  protected String getScopeName(N object) {
    return object.getMetadata().getScope();
  }

  protected ConcurrentRepository<T> init(Version version, Class<T> toClazz, Class<N> fromClazz) {
    return init(version, toClazz, fromClazz, null);
  }

  protected ConcurrentRepository<T> init(
      Version version, Class<T> toClazz, Class<N> fromClazz, BiConsumer<T, Scope> callback) {
    AudDataRepository dataRepository = version.getAudDataRepository();
    if (dataRepository == null) {
      Log.errorf("Data Repository not found for version %s", version);
      return null;
    }
    return init(version, toClazz, fromClazz, dataRepository.getData().get(fromClazz), callback);
  }

  protected ConcurrentRepository<T> init(
      Version version,
      Class<T> toClazz,
      Class<N> fromClazz,
      List<? extends ModelElement> objectList,
      BiConsumer<T, Scope> callback) {
    String toClazzName = toClazz.getSimpleName();

    Log.debugf("Loading %s from aud-data...", toClazzName);

    ConcurrentRepository<Scope> scopeRepo = version.getScopesRepository();
    if (scopeRepo == null) {
      Log.errorf("Scopes Repository not found for version %s", version);
      return null;
    }

    if (objectList == null) {
      // Some objects are in different files, or do not exist yet
      logModelWarning("No data for %s", fromClazz.getName());
      objectList = List.of();
    }

    String versionNumber = version.getVersionNumber();
    AtomicReference<Integer> duplicatedCount = new AtomicReference<>(0);
    ConcurrentMap<String, T> map = new ConcurrentHashMap<>();
    for (Object object : objectList) {
      N o = fromClazz.cast(object);

      T entity;
      try {
        entity = toClazz.getDeclaredConstructor(o.getClass()).newInstance(o);
      } catch (Exception e) {
        Log.errorf("Failed to instantiate %s: %s", toClazzName, e.getMessage());
        continue;
      }

      entity.setVersion(versionNumber);

      String scopeName = getScopeName(entity.getData());
      Scope scope = scopeRepo.get(AbstractModelEntity.generateUniqueId(scopeName));
      if (scope == null) {
        logModelWarning(
            "%s %s references a non-existing Scope %s", toClazzName, entity.id(), scopeName);
      } else {
        entity.setScope(scope);
      }

      if (callback != null) {
        callback.accept(entity, scope);
      }

      String uniqueId = entity.id();
      T previous = map.get(uniqueId);
      if (previous != null) {
        if (audModelConfig.keepDuplicates()) {
          map.put(UUID.randomUUID().toString(), entity);
        }
        if (previous.equals(entity)) {
          logModelWarning("Duplicated %s with ID %s", toClazzName, previous.id());
          duplicatedCount.getAndSet(duplicatedCount.get() + 1);

        } else {
          logModelWarning(
              "%s with ID %s is conflicting with %s with ID %s.",
              toClazzName, previous.id(), toClazzName, uniqueId);
        }
      } else {
        map.put(uniqueId, entity);
      }
    }

    Log.debugf("Completed loading %d %s", map.size(), toClazzName);
    if (duplicatedCount.get() > 0) {
      Log.warnf("Found %d Duplicated %s", duplicatedCount.get(), toClazzName);
    }

    ConcurrentRepository<T> repository = getRepository(version);
    repository.addData(map);

    return repository;
  }
}
