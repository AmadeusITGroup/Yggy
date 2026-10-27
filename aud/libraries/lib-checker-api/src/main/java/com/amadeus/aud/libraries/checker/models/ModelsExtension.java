package com.amadeus.aud.libraries.checker.models;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Adds deployment-specific elements to the {@link Models} loaded from the storage account, for
 * example governance data that is not part of the AUD data.
 *
 * <p>Implementations are CDI beans: every bean of this type on the classpath is used.
 */
public interface ModelsExtension {

  /**
   * Elements to add once the AUD data is loaded and the domain overrides are applied.
   *
   * <p>A type returned here replaces the elements of that type in the loaded data. Two extensions
   * must not return the same type.
   *
   * @return elements per type
   */
  Map<Class<? extends ModelElement>, List<? extends ModelElement>> load();

  /**
   * Type whose element names ({@code metadata.name}) are the domain names, see {@link
   * Models#allDomainNames()}.
   *
   * @return the type, or null to keep the default ({@code V1Scope}); at most one extension may
   *     return a type
   */
  default @Nullable Class<? extends ModelElement> domainType() {
    return null;
  }
}
