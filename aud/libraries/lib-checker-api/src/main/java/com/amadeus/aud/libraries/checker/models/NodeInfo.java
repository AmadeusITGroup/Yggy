package com.amadeus.aud.libraries.checker.models;

import jakarta.annotation.Nullable;

public record NodeInfo(
    @Nullable String location,
    @Nullable String kind,
    @Nullable String objectName,
    @Nullable String scope) {}
