package com.amadeus.aud.libraries.json.schema.ports;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.function.Supplier;

@FunctionalInterface
public interface SchemaPort extends Supplier<JsonNode> {}
