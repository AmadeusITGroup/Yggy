package com.amadeus.aud.libraries.yaml.models;

import com.amadeus.aud.libraries.yaml.models.resolvers.DefaultKindTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Interface representing a model element: an element that was parsed from data written by users,
 * but not processed to become a full-fledged model.
 *
 * <p>Once a model element is processed, it becomes a {@link
 * com.amadeus.aud.libraries.models.core.db.base.BomElement}
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind",
    visible = true)
@JsonTypeIdResolver(DefaultKindTypeIdResolver.class)
public interface ModelElement {
  default V1Metadata getMetadata() {
    final V1Metadata metadata = new V1Metadata();
    metadata.setName("Dummy");
    metadata.setScope("Dummy");
    return metadata;
  }

  default Object getSpec() {
    return null;
  }
}
