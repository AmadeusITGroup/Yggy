package com.amadeus.aud.libraries.checker.config;

import com.amadeus.aud.libraries.checker.models.Transitional;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.regex.Pattern;

public record SpecificError(
    @Nullable LocalDate limitDate,
    @Nullable Duration noticePeriod,
    @Nullable URI link,
    @Nullable Pattern exceptionalDomainsRegexp,
    @Nullable LocalDate exceptionalDomainsLimitDate) {

  public @Nullable Transitional transitional(ChronoLocalDate now, @Nullable CharSequence domain) {
    if (now.isAfter(limitDate)) {
      if (exceptionalDomainsLimitDate == null || now.isAfter(exceptionalDomainsLimitDate)) {
        return null;
      }
      if (domain == null
          || exceptionalDomainsRegexp == null
          || !exceptionalDomainsRegexp.matcher(domain).matches()) {
        return null;
      }
      return new Transitional(exceptionalDomainsLimitDate);
    }
    return new Transitional(limitDate);
  }

  public boolean isOnlyLink() {
    return link != null
        && noticePeriod == null
        && limitDate == null
        && exceptionalDomainsRegexp == null
        && exceptionalDomainsLimitDate == null;
  }
}
