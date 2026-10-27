package com.amadeus.aud.libraries.checker.config;

import java.util.Map;

public record Configuration(Defaults defaults, Map<String, SpecificError> specificErrors) {}
