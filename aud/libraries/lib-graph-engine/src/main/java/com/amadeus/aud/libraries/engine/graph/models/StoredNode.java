package com.amadeus.aud.libraries.engine.graph.models;

/** Denormalized node record for in-memory storage with metadata for filtering. */
public record StoredNode(
    String componentInstanceId, String landingZoneName, String platformName, String targetName) {}
