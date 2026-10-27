package com.amadeus.aud.libraries.checker.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class TomlParser {
  private static final String DEFAULTS = "defaults";
  public static final long ONE_MONTH = 30L;

  private TomlParser() {
    throw new UnsupportedOperationException("Utility Class");
  }

  public static Configuration parse(byte[] tomlContent) throws IOException {
    TomlMapper mapper = new TomlMapper();
    Map<String, Map<String, String>> map =
        mapper
            .readerFor(new TypeReference<Map<String, Map<String, String>>>() {})
            .readValue(tomlContent);
    Defaults defaults = defaults(map);
    return new Configuration(defaults(map), specificErrors(map, defaults.noticePeriod()));
  }

  private static Defaults defaults(Map<String, Map<String, String>> map) {
    if (!map.containsKey(DEFAULTS)) {
      throw new IllegalArgumentException("Missing 'defaults' section in TOML configuration");
    }
    Map<String, String> defaultsMap = map.get(DEFAULTS);
    return new Defaults(
        LocalDate.parse(defaultsMap.getOrDefault("limit_date", "2030-01-01")),
        Duration.ofDays(
            ONE_MONTH * Integer.parseInt(defaultsMap.getOrDefault("notice_period_months", "6"))));
  }

  private static Map<String, SpecificError> specificErrors(
      Map<String, Map<String, String>> map, Duration defaultNoticePeriod) {
    Map<String, SpecificError> output = new HashMap<>();
    for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
      String k = entry.getKey();
      Map<String, String> specificErrorMap = entry.getValue();
      if (DEFAULTS.equals(k)) {
        continue;
      }
      SpecificError value;
      // If only a link is provided, consider it's only for documentation purposes, not for
      // error/warning change
      if (specificErrorMap.size() == 1 && specificErrorMap.containsKey("link")) {
        value = new SpecificError(null, null, URI.create(specificErrorMap.get("link")), null, null);
      } else {
        value =
            new SpecificError(
                LocalDate.parse(specificErrorMap.getOrDefault("limit_date", "2030-01-01")),
                specificErrorMap.containsKey("notice_period_months")
                    ? Duration.ofDays(
                        ONE_MONTH * Integer.parseInt(specificErrorMap.get("notice_period_months")))
                    : defaultNoticePeriod,
                specificErrorMap.containsKey("link")
                    ? URI.create(specificErrorMap.get("link"))
                    : null,
                specificErrorMap.containsKey("exceptional_domains_regexp")
                    ? Pattern.compile(specificErrorMap.get("exceptional_domains_regexp"))
                    : null,
                specificErrorMap.containsKey("exceptional_domains_limit_date")
                    ? LocalDate.parse(specificErrorMap.get("exceptional_domains_limit_date"))
                    : null);
      }
      output.put(k, value);
    }
    return output;
  }
}
