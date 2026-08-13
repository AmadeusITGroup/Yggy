package com.amadeus.aud.libraries.yaml.models;

public record JacksonProperty(
    String fieldName, Class<?> fieldClass, Class<?> contentClass, Class<?> keyClass) {}
