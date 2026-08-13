package com.amadeus.aud.libraries.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Interface representing an entity with an ID.
 *
 * <p>This ID is a fixed value denoting the nominal identity of the object.
 *
 * <p>It is therefore unique
 */
@FunctionalInterface
public interface WithId {
  @JsonProperty("id")
  String id();
}
