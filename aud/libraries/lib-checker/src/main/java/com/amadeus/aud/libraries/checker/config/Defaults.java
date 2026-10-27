package com.amadeus.aud.libraries.checker.config;

import java.time.Duration;
import java.time.LocalDate;

public record Defaults(LocalDate limitDate, Duration noticePeriod) {}
