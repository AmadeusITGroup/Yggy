package com.amadeus.aud.libraries.yaml.models.utils;

import com.amadeus.aud.libraries.yaml.models.resolvers.DefaultKindTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind",
    visible = true)
@JsonTypeIdResolver(DefaultKindTypeIdResolver.class)
public interface ModelElementMixin {}
