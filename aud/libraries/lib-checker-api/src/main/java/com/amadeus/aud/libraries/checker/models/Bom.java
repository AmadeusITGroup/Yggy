package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.annotation.Nullable;
import java.util.Collection;

/**
 * Interface allowing to request the Bill of Materials (BOM).
 *
 * <p>Objects at this point have passed syntax checks and validity checks.
 *
 * <p>At this point, the models are correctly linked to each other.
 *
 * <p>Automatic instantiation has also been done.
 */
public interface Bom {
  /**
   * Request the list of all objects in the BOM, no matter their types.
   *
   * <p>If domain is provided, it will return the objects of that domain.
   */
  Collection<BomElement> all(@Nullable CharSequence domain);

  /**
   * Request a collection of objects with a specific type from the BOM. If a non-null domain is
   * provided, select only objects of that domain.
   *
   * <p>e.g.: list(Dependency.class, null) will return a collection of all Dependencies
   */
  <T extends BomElement> Collection<T> list(Class<T> type, @Nullable CharSequence domain);

  /*
   * @return the version for which this BOM has been created
   * */
  Version getVersion();
}
