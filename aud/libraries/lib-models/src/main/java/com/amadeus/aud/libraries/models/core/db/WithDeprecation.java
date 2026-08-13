package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import jakarta.annotation.Nullable;

/**
 * Interface for entities that support deprecation.
 *
 * <p>Entities implementing this interface support deprecation status and optional deprecation
 * messages to inform users about deprecated features and their replacements.
 *
 * <p>The deprecation status can be:
 *
 * <ul>
 *   <li>null - Not deprecated
 *   <li>DeprecatedStatus.WARNING - Deprecated with warning (informational, not blocking)
 *   <li>DeprecatedStatus.ERROR - Deprecated with error (blocking the merge of PR)
 * </ul>
 */
public interface WithDeprecation {

  /**
   * Gets the deprecation status of this entity.
   *
   * @return the deprecation status enum (WARNING or ERROR), or null if not deprecated
   */
  @Nullable
  DeprecatedStatus getDeprecatedStatus();

  /**
   * Gets the deprecation message providing additional information about the deprecation.
   *
   * <p>This message typically includes information about:
   *
   * <ul>
   *   <li>Replacement information (what to use instead)
   *   <li>Owner contact details
   *   <li>Migration guidance
   * </ul>
   *
   * @return the deprecation message, or null if not set
   */
  @Nullable
  String getDeprecatedMessage();
}
